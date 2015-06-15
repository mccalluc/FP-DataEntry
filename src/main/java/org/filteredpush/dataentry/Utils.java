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
package org.filteredpush.dataentry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.parser.QueryParser;
import org.eclipse.jetty.server.Server;
import org.filteredpush.dataentry.backend.solr.SolrInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class Utils {
	
	private static Logger log = LoggerFactory.getLogger(SolrInstaller.class);

	private Utils() {}
	
	public static File mkdirs(File dir) { // Feel free to change this if we migrate to 1.7
		if (dir.mkdirs()) {
			return dir;
		} else {
			throw new Error("Could not make "+dir);
		}
	}
	
	public static File mkdirs(File dir, String file) {
		File dirFile = new File(dir, file);
		return mkdirs(dirFile);
	}
	
	public static File createTempDir(String prefix) {
		File temp = Files.createTempDir();
		File dest = new File(temp.getParentFile(), prefix + temp.getName());
		if (temp.renameTo(dest)) {
			return dest;
		} else {
			throw new Error("Failed to rename "+temp+" to "+dest);
		}
	}
	
	public static abstract class PortPicker {
		private Server server;
		public abstract Server pick(int port) throws Exception;
		public Server pickFrom(int... ports) {
			Exception lastException = null;
			for (int port : ports) {
				try {
					server = pick(port);
					server.start();
					log.debug("Port "+port+" seems to be free.");
					break;
				} catch (Exception e) {
					lastException = e;
					log.debug("Problem: Port "+port+" is probably occupied. Trying again.");
				}
			}
			if (server == null || !server.isRunning()) {
				throw new Error("Couldn't open any port", lastException);
			}
			return server;
		}
	}
	
	static public String queryMapToString(Map<String,String> map) {
		List<String> parts = new ArrayList<String>();
		for (Entry<String,String> entry : map.entrySet()) {
			// TODO: Understand this better: With the changes in this commit, empty query terms can block the return on results.
			// This makes the empty string check necessary. Something changed in the indexing?
			if (!entry.getValue().equals("")) {
				parts.add(entry.getKey() + ":\"" + QueryParser.escape(entry.getValue()) + "\"");
			}
		}
		return StringUtils.join(parts, " AND ");
	}
	
	public static class QueryResponseWrapper {
		// TODO: Facets could be useful, but nothing is using them right now.
		private SolrDocumentList results;
//		private Map<String,List<String>> facetResults;
		public QueryResponseWrapper(QueryResponse response) {
			results = response.getResults();
//			facetResults = new HashMap<String,List<String>>();
//			if (response.getFacetFields() != null) {
//				for (FacetField facet : response.getFacetFields()) {
//					List<String> list = new ArrayList<String>();
//					for (Count facetValue : facet.getValues()) {
//						list.add(facetValue.getName());
//					}
//					facetResults.put(facet.getName(), list);
//				}
//			}
		}
		public SolrDocumentList getResults() {
			return results;
		}
//		public Map<String,List<String>> getFacetResults() {
//			return facetResults;
//		}
	}
	
	static public QueryResponseWrapper query(SolrServer server, String query) {
		// TODO change visibility so that it's clear this is just for testing.
		SolrQuery solrQuery = new SolrQuery()
			.setQuery(query);
			// .setFacet(true)
			// .addFacetField(DwcTerm.ID.toString(), DwcTerm.STATE_PROVINCE.toString()); // TODO: figure out how facets are specified.
		try {
			return new QueryResponseWrapper(server.query(solrQuery));
		} catch (SolrServerException e) {
			throw new Error(e);
		}
	}
	
	static public QueryResponseWrapper query(SolrServer server, Map<String,String> query) {
		return query(server,queryMapToString(query));
	}
	
	static public SolrDocumentList query(SolrServer server, String field, String value) {
		Map<String,String> queryMap = new HashMap<String,String>();
		queryMap.put(field, value);
		return query(server, queryMap).getResults();
	}
	
	public static File unzipToTemp(File zipped) {
		try {
			// Mostly copy-and-paste from SO.
			if (zipped.toString().matches("^jar:.*!.*$")) {
				// Entries in a jar aren't really files.
				// This is probably only used for demo and test files, 
				// so it's not the end of the world for it to be this ugly:
				InputStream input = Utils.class.getResourceAsStream(zipped.toString().replaceFirst(".*!", ""));
				File actualFile = File.createTempFile("", ".zip");
				log.debug("Can't open a zip straight from jar file, so make it a real file first: "+actualFile);
				OutputStream output = new FileOutputStream(actualFile);
				IOUtils.copy(input, output);
				zipped = actualFile;
			}
			ZipFile zipFile = new ZipFile(zipped);
			File tempDir = com.google.common.io.Files.createTempDir(); // dependency not explicit in pom.
			Enumeration<? extends ZipArchiveEntry> entries = zipFile.getEntries();
			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				File entryDestination = new File(tempDir, entry.getName());
				entryDestination.getParentFile().mkdirs();
				InputStream in = zipFile.getInputStream(entry);
				OutputStream out = new FileOutputStream(entryDestination);
				IOUtils.copy(in, out);
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
			zipFile.close();
			return tempDir;
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	public static EmbeddedSolrServer startSolr(File solrDir) {
		if (!solrDir.exists()) {
			throw new Error("No solr directory at "+solrDir);
		}
		CoreContainer coreContainer = new CoreContainer(solrDir.toString());
		coreContainer.load();
		return new EmbeddedSolrServer(coreContainer, "");
	}

	public static String readResource(String resource) {
		try {
			return Resources.toString(Resources.getResource(resource), Charsets.UTF_8);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	private static NodeList evalXpath(Document doc, String xpath) {
		NodeList nodes;
		try {
			XPathExpression xpathObj;
			xpathObj = XPathFactory.newInstance().newXPath().compile(xpath);
			nodes = (NodeList)xpathObj.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new Error(e);
		}
		return nodes;
	}

	public static Set<String> evalXpath(Document doc, String xpath, String attribute) {
		NodeList nodes = evalXpath(doc, xpath);
		ImmutableSet.Builder<String> builder = new ImmutableSet.Builder<String>();
		for (int i = 0; i<nodes.getLength(); i++) {
			if (null == attribute) {
				builder.add(nodes.item(i).getTextContent());
			} else {
				// TODO: Why not just put the attribute selector in the XPath?
				Node attributeNode = nodes.item(i).getAttributes().getNamedItem(attribute);
				if (attributeNode != null) {
					builder.add(attributeNode.getNodeValue());
				}
			}
		}
		return builder.build();
	}

	public static File saveToDisk(String content, String pre, String post) {
		File temp;
		try {
			temp = File.createTempFile(pre, post);
			Writer writer = new BufferedWriter(new FileWriter(temp));
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			throw new Error(e);
		}
		return temp;
	}

	private static DocumentBuilder getIgnoreDtdBuilder(){
		DocumentBuilder docBuilder;
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new Error(e);
		}
		docBuilder.setEntityResolver(
			new EntityResolver() {
				public InputSource resolveEntity(String publicId, String systemId) {
					return new InputSource(new StringReader("")); // Returns a valid dummy source
					// The configuration isn't actually valid against the DTD, but it's still useful for the IDE.
					// TODO: Actually make a schema that works, and is maintainable. (I'm not excited about XSD.)
				}
			}
		);
		return docBuilder;
	}
	
	public static Document parseXml(File file) {
		Document doc;
		try {
			doc = getIgnoreDtdBuilder().parse(file);
		} catch (SAXException e) {
			throw new Error("Problem parsing "+file.getAbsolutePath(),e);
		} catch (IOException e) {
			throw new Error("Problem reading "+file.getAbsolutePath(),e);
		}
		return doc;
	}
	
	public static Document parseXml(InputStream is) {
		Document doc;
		try {
			doc = getIgnoreDtdBuilder().parse(is);
		} catch (SAXException e) {
			throw new Error("Problem parsing",e);
		} catch (IOException e) {
			throw new Error("Problem reading",e);
		}
		return doc;
	}

}
