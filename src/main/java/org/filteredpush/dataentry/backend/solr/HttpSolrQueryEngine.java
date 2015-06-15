package org.filteredpush.dataentry.backend.solr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrDocument;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.Utils.QueryResponseWrapper;
import org.filteredpush.dataentry.backend.GenericQueryEngine;
import org.filteredpush.dataentry.backend.TupleMultiRecord;
import org.filteredpush.dataentry.enums.QParameter;
import org.filteredpush.dataentry.enums.Tuple;

public class HttpSolrQueryEngine implements GenericQueryEngine {

	private SolrServer server;
	
	public HttpSolrQueryEngine(SolrServer server) {
		this.server = server;
	}
	

	@Override
	public void shutdown() {
		// no-op
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
				Tuple key;
				try {
					 key = Tuple.valueOf(entry.getKey());
				} catch (IllegalArgumentException e) {
					continue; // With an outside solr, we don't need to recognize every field: Tolerate out-of-sync-ness.
				}
				List<List<String>> listList;
				Object rawValue = entry.getValue();
				if (rawValue instanceof String) {
					listList = Arrays.asList(Arrays.asList((String)rawValue));
				} else if (rawValue instanceof List) {
					listList = new ArrayList<List<String>>();
					for (String item : (List<String>)rawValue) {
						listList.add(Arrays.asList((String)item)); // TODO: maybe we want to concatenate these? I'm not sure what the semantics of multi should be here.
					}
				} else {
					throw new Error("Expected either String or List: instead we got: "+rawValue.getClass());
				}
				record.put(key, listList);
			}
			records.add(record);
		}
		return records;
	}

}
