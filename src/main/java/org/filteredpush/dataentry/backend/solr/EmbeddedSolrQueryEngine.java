package org.filteredpush.dataentry.backend.solr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrDocument;
import org.filteredpush.dataentry.Json;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.Utils.QueryResponseWrapper;
import org.filteredpush.dataentry.backend.GenericQueryEngine;
import org.filteredpush.dataentry.backend.TupleMultiRecord;
import org.filteredpush.dataentry.enums.QParameter;
import org.filteredpush.dataentry.enums.Tuple;

public class EmbeddedSolrQueryEngine implements GenericQueryEngine {

	private SolrServer server;
	
	public EmbeddedSolrQueryEngine(SolrServer server) {
		this.server = server;
	}
	
	@Override
	public void shutdown() {
		server.shutdown();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TupleMultiRecord> query(Map<String, String[]> urlParams) {
		Map<String, String> map = QParameter.apply(urlParams);
		List<TupleMultiRecord> records = new ArrayList<TupleMultiRecord>();
		
		QueryResponseWrapper queryResponse = Utils.query(server, map);
		
		for (SolrDocument doc : queryResponse.getResults()) {
			TupleMultiRecord record = new TupleMultiRecord();
			for (Entry<String, Object> entry : doc.entrySet()) {
				if (UpdatingSolrServer.ID.equals(entry.getKey())) {
					continue;
				};
				Tuple key = Tuple.valueOf(entry.getKey());

				List<List<String>> listList;
				Object rawValue = entry.getValue();
				if (rawValue instanceof String) {
					// single-valued fields as defined in schema.xml
					// (JSON arrays may have still have size > 1)
					// example: latitudeLongitude: a pair, but each record only has one.
					listList = Arrays.asList(deserialize((String)rawValue));
				} else if (rawValue instanceof List) {
					// multi-valued fields as defined in schema.xml
					// (but JSON arrays could have size = 1)
					// example: recordNumber: JSON list has one value, but there may be many.
					listList = new ArrayList<List<String>>();
					for (String json : (List<String>)rawValue) {
						listList.add(deserialize(json));
					}
				} else {
					throw new Error("Expected either ID or String or List: instead we got: "+rawValue.getClass());
				}
				record.put(key, listList);
			}
			records.add(record);
		}
		return records;
	}
	
	@SuppressWarnings("unchecked")
	private static List<String> deserialize(String serialized) {
		return (List<String>) Json.from(serialized);
	}

}
