package org.filteredpush.dataentry.extras;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.filteredpush.dataentry.Json;
import org.filteredpush.dataentry.backend.GenericQueryEngine;
import org.filteredpush.dataentry.backend.TupleMultiRecord;
import org.filteredpush.dataentry.enums.QParameter;
import org.filteredpush.dataentry.enums.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public class GbifApiQueryEngine implements GenericQueryEngine {

	private static Logger log = LoggerFactory.getLogger(GbifApiQueryEngine.class);
	
	public GbifApiQueryEngine() {}
	
	@Override
	public void shutdown() {}

	@SuppressWarnings("unchecked")
	@Override
	public List<TupleMultiRecord> query(Map<String, String[]> urlParams) {
		Map<String, String> map = QParameter.apply(urlParams);
		List<String> queryTerms = new ArrayList<String>();
		for(Entry<String,String> entry : map.entrySet()) {
			try {
				queryTerms.add(URLEncoder.encode(entry.getKey(), "UTF-8") //
					+ "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new Error(e);
			}
		}
		queryTerms.add("limit=10");
		
		List<TupleMultiRecord> records = new ArrayList<TupleMultiRecord>();
		
		String json;
		try {
			URL url = new URL("http://api.gbif.org/v0.9/occurrence/search?"+StringUtils.join(queryTerms, "&"));
			InputStream stream = url.openStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(stream, writer, Charsets.UTF_8.name());
			json = writer.toString();
			log.debug("GBIF API Response: "+json.substring(0, Math.min(80, json.length()))+"...");
		} catch (IOException e) {
			throw new Error(e);
		}
		
		// GBIF JSON currently looks like:
		// {"offset":0,"limit":10,"endOfRecords":false,"count":2565,"results":[{"key":792381384,"kingdom":"Animalia", ...
		
		Map<String,Object> response = (Map<String, Object>) Json.from(json);
		List<Map<String,Object>> results = (List<Map<String, Object>>) response.get("results");
		
		for (Map<String,Object> result : results) {
			TupleMultiRecord record = new TupleMultiRecord();
			for (Entry<String, Object> entry : result.entrySet()) {
				Tuple key;
				try {
					 key = Tuple.valueOf(entry.getKey());
				} catch (IllegalArgumentException e) {
					continue;
				}

				List<List<String>> listList;
				Object rawValue = entry.getValue();
				if (rawValue instanceof String) {
					listList = Arrays.asList(Arrays.asList((String)rawValue));
				} else {
					throw new Error("Expected String: instead we got: "+rawValue.getClass());
				}
				record.put(key, listList);
			}
			records.add(record);
		}
		
		return records;
	}

}
