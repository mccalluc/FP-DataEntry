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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.filteredpush.dataentry.CliOptionsParser;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.backend.EncodingCorrection;
import org.filteredpush.dataentry.backend.FileRecordsForIndexAndUpdate;
import org.filteredpush.dataentry.backend.GbifRecordsForIndexAndUpdate;
import org.filteredpush.dataentry.backend.GenericRecordsForIndexAndUpdate;
import org.filteredpush.dataentry.backend.TupleSingleRecord;
import org.filteredpush.dataentry.configuration.Configuration;
import org.filteredpush.dataentry.configuration.IndexerType;
import org.filteredpush.dataentry.configuration.SolrFileIndexerConfiguration;
import org.filteredpush.dataentry.configuration.SolrIndexerConfiguration;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.filteredpush.dataentry.enums.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrIndexer implements GenericSolrIndexer<TupleSingleRecord> {
	
	private static Logger log = LoggerFactory.getLogger(SolrIndexer.class);
	private static final int INTERVAL = 100;
	
	private static SolrIndexer instance;
	
	private SolrIndexer() {}
	
	public static void main(String... args) {
		SolrIndexerConfiguration config = new CliOptionsParser( //
				"Loads data into a Solr installation. It should only be run once on a given installation: " //
				+ "If the data changes, or you find a bug, start over from scratch.")
			.parseOrExit(args);
		
		if (config.getQueryEngineClass().equals(EmbeddedSolrQueryEngine.class)) {
			EnumUtils.setEnums(config);
			indexAndUpdate(config);
			EnumUtils.clearEnums();
		}
	}
	
	private static void indexAndUpdate(SolrIndexerConfiguration config) {
		log.info("Starting to index... (This may take a while.)");
		UpdatingSolrServer solrServer;
		try {
			solrServer = new UpdatingSolrServer(Utils.startSolr(config.getSolrDirectory()));
		} catch (NoSuchElementException e) {
			log.warn("No "+Configuration.SOLR_DIRECTORY+": Hopefully you're using not trying to use embedded Solr. Will continue.");
			return; 
		}
		IndexerType type = config.getIngestType();
		GenericRecordsForIndexAndUpdate<TupleSingleRecord> records;
		// TODO: Should this be more OO?
		if (IndexerType.GBIF.equals(type)) {
			File downloadedZip = config.getIngestFile();
			File unzipped = Utils.unzipToTemp(downloadedZip);
			records = new GbifRecordsForIndexAndUpdate(unzipped);
		} else if (IndexerType.FILE.equals(type)) {
			records = readFile((SolrFileIndexerConfiguration) config);
		} else {
			throw new Error(Configuration.INGEST_TYPE+" must be 'GBIF' or 'FILE'");
		}
		
		SolrIndexer instance = SolrIndexer.getInstance();
		String dataSetId = java.util.UUID.randomUUID().toString();
		instance.index(records.forIndex(), solrServer, dataSetId);
		instance.update(records.forUpdate(), solrServer, dataSetId);
		
		solrServer.shutdown();
	}
	
	static GenericRecordsForIndexAndUpdate<TupleSingleRecord> readFile(SolrFileIndexerConfiguration config) {
		File file = config.getIngestFile();
		Charset encoding = config.getEncoding();
		EncodingCorrection correction = config.getEncodingCorrection();
		String delimiter = config.getDelimiter();
		String countsAsNull = config.getNullMarker();
		Character quoteChar = config.getQuoteCharacter();
		Map<String,String> columnMap = config.getColumnMap();
		
		return new FileRecordsForIndexAndUpdate(file, encoding, correction, delimiter, quoteChar, countsAsNull, columnMap);
	}

	
	public static SolrIndexer getInstance() {
		if (instance == null) {
			instance = new SolrIndexer();
		}
		return instance;
	}
	
	private void logDoc(String comment, int i, SolrInputDocument doc) {
		log.info(comment + " #" + i + ": " + Tuple.values()[0].extractForLog(doc));
	}
	
	public void index(Iterable<TupleSingleRecord> records, SolrServer server, String dataSetId) {
		try {
			int row = 0;
			for (TupleSingleRecord record : records) {
				SolrInputDocument doc = record.asSolr(dataSetId, row);
				server.add(doc);
				
				if (row % INTERVAL == 0) {
					logDoc("Adding doc", row, doc);
				}
				row++;
			};
			server.commit();
			log.info("Added "+row+" docs.");
		} catch (SolrServerException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	public void update(Iterable<TupleSingleRecord> records, UpdatingSolrServer server, String dataSetId) {
		try {
			int row = 0;
			for (TupleSingleRecord record : records) {
				// TODO record should only contain multi-valued fields... but instead it has them all, filled with nulls.
				SolrInputDocument doc = record.asSolr(dataSetId, row);
				server.update(doc); 
				
				if (row % INTERVAL == 0) {
					logDoc("Updating doc", row,doc);
				}
				row++;
			};
			server.commit();
			log.info("Updated "+row+" docs.");
		} catch (SolrServerException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	

}
