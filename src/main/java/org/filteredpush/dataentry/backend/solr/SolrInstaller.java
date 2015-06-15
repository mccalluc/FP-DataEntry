/*
FP-DataEntry
(C) 2014 President and Fellows of Harvard College

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.filteredpush.dataentry.backend.solr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.filteredpush.dataentry.CliOptionsParser;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.configuration.SolrInstallerConfiguration;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.filteredpush.dataentry.enums.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class SolrInstaller {

	private static Logger log = LoggerFactory.getLogger(SolrInstaller.class);

	private SolrInstaller() {}
	
	public static void main(String... args) throws Exception {
		SolrInstallerConfiguration config = new CliOptionsParser( //
				"Creates a solr installation at a location you specify. " //
				+ "Please do not hand-edit the resulting solr directory: " //
				+ "If it needs changes, make them in your configuration and re-run. ")
			.parseOrExit(args);
		
		if (config.getQueryEngineClass().equals(EmbeddedSolrQueryEngine.class)) {
			EnumUtils.setEnums(config);
			try {
				install(config.getSolrDirectory(), config.getSolrFiles());
			} catch (NoSuchElementException e) {
				log.warn("Since required information was not provided, not actually going to install solr. "
						+ "(Hopefully you're using Solr over HTTP, or a different QueryEngine entirely.)");
				// (Quietly failing seems better than requiring the caller to look at the config
				// and determine whether this is needed or not.)
			}
			EnumUtils.clearEnums();
		}
	}
	
	public static void install(File solrDir, Map<String,String> solrFiles) {
		try {
			if (!solrDir.exists()) {
				Utils.mkdirs(solrDir);
				log.debug("Created " + solrDir);
			} else if (solrDir.list().length == 0) {
				log.info("Solr using existing directory: "+solrDir);
			} else {
				throw new Error("Solr directory at "+solrDir+" is not empty; Exiting rather than clearing directory.");
			}
			
			File coreDir = Utils.mkdirs(solrDir, "collection1"); // Built-in default value
			File confDir = Utils.mkdirs(coreDir, "conf");
			Utils.mkdirs(coreDir, "data");
			
			for (Entry<String,String> entry : solrFiles.entrySet()) {
				File newFile = new File(confDir, entry.getKey());
				Writer writer = new BufferedWriter(new FileWriter(newFile));
				writer.write(entry.getValue());
				writer.close();
			}
			
			validate(solrDir);
			
			log.debug("Solr installed at "+solrDir);
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	private static void validate(File solrDir) {
		try {
			Utils.startSolr(solrDir).shutdown(); // just to make sure it works.
			validateJustSchema(
					new File(new File(new File(solrDir, "collection1"), "conf"), "schema.xml"),
					Tuple.asStringSet());
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	public static void validateJustSchema(File schema, Set<String> tuples) throws SolrInstallerException {
		Document doc = Utils.parseXml(schema);
		
		Set<String> schemaFields = Utils.evalXpath(doc,"/schema/fields/field","name");
		Set<String> schemaCopies = Utils.evalXpath(doc,"/schema/copyField","dest");
		
		if (!schemaFields.containsAll(schemaCopies)) {
			throw new SolrInstallerException("In the Solr schema, the 'dest' of a copyField does not match a known field: "
				+ Sets.difference(schemaCopies, schemaFields));
		}
		if (!schemaFields.containsAll(tuples)) {
			throw new SolrInstallerException("In the Solr schema, there is a Tuple value missing from the list of fields: "
				+ Sets.difference(tuples, schemaFields));
		}
		if (tuples.size() + schemaCopies.size() + 1 != schemaFields.size()) {
			throw new SolrInstallerException( //
					"In the Solr schema, the fields are not the disjoint union of the tuple enumeration and the copyFields and " //
					+ "the internal ID (" + UpdatingSolrServer.ID + "): " //
					+ tuples.size() + " (tuples) + " //
					+ schemaCopies.size() + " (schema copies) + 1 != " //
					+ schemaFields.size() + " (schema fields)");
		}
		
		if (!UpdatingSolrServer.ID.equals(doc.getElementsByTagName("uniqueKey").item(0).getTextContent())) {
			throw new SolrInstallerException("In the Solr schema 'uniqueKey' must be '"+UpdatingSolrServer.ID+"'.");
		}
		
		String dataFieldsXPath = "/schema/fields/field[@name!='"+UpdatingSolrServer.ID+"']";
		String idFieldXPath = "/schema/fields/field[@name='"+UpdatingSolrServer.ID+"']";
		
		Set<String> typesOfIndexedFields = Utils.evalXpath(doc, dataFieldsXPath + "[@indexed='true']", "type");
		Set<String> typesOfUnindexedFields = Utils.evalXpath(doc, dataFieldsXPath + "[@indexed='false']", "type");
		Set<String> typeOfIdField = Utils.evalXpath(doc, idFieldXPath, "type");
		
		ImmutableSet.Builder<String> builder = new ImmutableSet.Builder<String>();
		Set<String> typesOfFields = builder.addAll(typesOfIndexedFields)
			.addAll(typesOfUnindexedFields)
			.addAll(typeOfIdField)
			.build();
		
		Set<String> typesUnused = Sets.difference(
				Utils.evalXpath(doc, "/schema/types/fieldType", "name"), typesOfFields);
		if (!typesUnused.isEmpty()) {
			throw new SolrInstallerException("In the Solr schema, these types are defined but not used: "+typesUnused);
		}
		
		for (String type : typesOfIndexedFields) {
			if (!type.startsWith("fp")) {
				throw new SolrInstallerException("In the Solr schema, indexed fields should always be one of the fp* types. At least one exception: "+type);
			}
		}
		for (String type : typesOfUnindexedFields) {
			if (!type.equals("string")) {
				throw new SolrInstallerException("In the Solr schema, unindexed fields should simply be 'string'. At least one exception: "+type);
			}
		}
		for (String type : typeOfIdField) {
			if (!type.equals("fpID")) {
				throw new SolrInstallerException("In the Solr schema, "+UpdatingSolrServer.ID+" needs to be type 'fpID', just because.");
			}
		}
		// TODO: more validation of the ID field.
	}
	
	@SuppressWarnings("serial")
	public static class SolrInstallerException extends Exception {
		public SolrInstallerException(String message) {
			super(message);
		}
	}

}
