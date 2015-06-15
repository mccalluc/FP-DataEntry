package org.filteredpush.dataentry.extras;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.filteredpush.dataentry.Json;
import org.filteredpush.dataentry.backend.GenericQueryEngine;
import org.filteredpush.dataentry.backend.TupleMultiRecord;
import org.filteredpush.dataentry.configuration.EnumConfiguration;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.filteredpush.dataentry.enums.QParameter;
import org.filteredpush.dataentry.enums.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public class OpenLibraryQueryEngine implements GenericQueryEngine {

	private static Logger log = LoggerFactory.getLogger(OpenLibraryQueryEngine.class);
	
	public static final String LCCN = "lccn";
	public static final String ISBN_10 = "isbn_10";
	public static final String OCLC = "oclc";
	
	public static final String DDC = "dewey_decimal_class";
	public static final String LCC = "lc_classifications";
	
	public static final String TITLE = "title";
	public static final String BY_STATEMENT = "by_statement";
	public static final String AUTHORS = "authors";
	public static final String NOTES = "notes";
	
	public static final String NUMBER_OF_PAGES = "number_of_pages";
	public static final String PAGINATION = "pagination";
	
	public static final String SUBJECT_PLACES = "subject_places";
	public static final String SUBJECTS = "subjects";
	public static final String SUBJECT_PEOPLE = "subject_people";
	public static final String SUBJECT_TIMES = "subject_times";
	
	public static final String PUBLISHERS = "publishers";
	public static final String PUBLISH_PLACES = "publish_places";
	public static final String PUBLISH_DATE = "publish_date";
	
	public OpenLibraryQueryEngine() {}
	
	@Override
	public void shutdown() {}

	@SuppressWarnings("unchecked")
	@Override
	public List<TupleMultiRecord> query(Map<String, String[]> urlParams) {
		Map<String, String> map = QParameter.apply(urlParams);
		List<String> queryTerms = new ArrayList<String>();
		queryTerms.add("jscmd=data");
		queryTerms.add("format=json");
		
		for(Entry<String,String> entry : map.entrySet()) {
			try {
				queryTerms.add("bibkeys=" //
					+ URLEncoder.encode(entry.getKey(), "UTF-8") //
					+ ":" + URLEncoder.encode(entry.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new Error(e);
			}
		}
		
		String json;
		try {
			URL url = new URL("http://openlibrary.org/api/books?"+StringUtils.join(queryTerms, "&"));
			log.debug("OpenLibrary API Request: "+url);
			InputStream stream = url.openStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(stream, writer, Charsets.UTF_8.name());
			json = writer.toString();
			log.debug("OpenLibrary API Response: "+json.substring(0, Math.min(80, json.length()))+"...");
		} catch (IOException e) {
			throw new Error(e);
		}
		
		/* 
		   OpenLibrary JSON currently looks like:
		
			{
				"ISBN:0451526538": {
					"key": "/books/OL1017798M", 
					"url": "https://openlibrary.org/books/OL1017798M/The_adventures_of_Tom_Sawyer",
					"identifiers": {"lccn": ["96072233"], "openlibrary": ["OL1017798M"], "isbn_10": ["0451526538"], "oclc": ["36792831"], "librarything": ["2236"], "project_gutenberg": ["74"], "goodreads": ["1929684"]},
					"classifications": {"dewey_decimal_class": ["813/.4"], "lc_classifications": ["PS1306 .A1 1997"]}, 

					"title": "The adventures of Tom Sawyer", 
					"by_statement": "Mark Twain ; with an introduction by Robert S. Tilton.", 
					"authors": [{"url": "https://openlibrary.org/authors/OL18319A/Mark_Twain", "name": "Mark Twain"}], 

					"notes": "Includes bibliographical references (p. 213-216).", 
					"number_of_pages": 216, 
					"pagination": "xxi, 216 p. ;", 
					"cover": {"small": "https://covers.openlibrary.org/b/id/295577-S.jpg", ...}, 
					
					"subject_places": [{"url": "https://openlibrary.org/subjects/place:missouri", "name": "Missouri"}, ...}], 
					"subjects": [{"url": "https://openlibrary.org/subjects/fiction", "name": "Fiction"}, ...], 
					"subject_people": [{"url": "https://openlibrary.org/subjects/person:mark_twain_(1835-1910)", "name": "Mark Twain (1835-1910)"}], 
					"subject_times": [{"url": "https://openlibrary.org/subjects/time:19th_century", "name": "19th century"}],
					
					"publishers": [{"name": "Signet Classic"}], 
					"publish_places": [{"name": "New York"}], 
					"publish_date": "1997", 
				}
			}
		*/
		
		Map<String, Map<String,Object>> results = (Map<String, Map<String,Object>>) Json.from(json);
		List<TupleMultiRecord> records = new ArrayList<TupleMultiRecord>();
		
		for (Map<String,Object> result : results.values()) {
			TupleMultiRecord record = new TupleMultiRecord();
			
			Map<String,List<String>> identifiers = (Map<String,List<String>>)result.get("identifiers");
			record.put(Tuple.valueOf(LCCN), rewrap(identifiers.get(LCCN)));
			record.put(Tuple.valueOf(ISBN_10), rewrap(identifiers.get(ISBN_10)));
			record.put(Tuple.valueOf(OCLC), rewrap(identifiers.get(OCLC)));
			
			Map<String,List<String>> classifications = (Map<String,List<String>>)result.get("classifications");
			record.put(Tuple.valueOf(DDC), rewrap(classifications.get(DDC)));
			record.put(Tuple.valueOf(LCC), rewrap(classifications.get(LCC)));
			
			record.put(Tuple.valueOf(TITLE), doubleWrap(result.get(TITLE)));
			record.put(Tuple.valueOf(BY_STATEMENT), doubleWrap(result.get(BY_STATEMENT)));
			record.put(Tuple.valueOf(AUTHORS), rewrapNames(result.get(AUTHORS)));
			record.put(Tuple.valueOf(NOTES), wrap((String)result.get(NOTES)));
			
			record.put(Tuple.valueOf(NUMBER_OF_PAGES), wrap(((Integer)result.get(NUMBER_OF_PAGES)).toString()));
			record.put(Tuple.valueOf(PAGINATION), wrap((String)result.get(PAGINATION)));
			
			// "joinNames" here, because what you want is all the data concatenated...
			record.put(Tuple.valueOf(SUBJECT_PLACES), joinNames(result.get(SUBJECT_PLACES)));
			record.put(Tuple.valueOf(SUBJECTS), joinNames(result.get(SUBJECTS)));
			record.put(Tuple.valueOf(SUBJECT_PEOPLE), joinNames(result.get(SUBJECT_PEOPLE)));
			record.put(Tuple.valueOf(SUBJECT_TIMES), joinNames(result.get(SUBJECT_TIMES)));
			
			// but "rewrapNames" here, because presumably the copy you have in hand has a single publisher, and you need to pick from the choices.
			record.put(Tuple.valueOf(PUBLISHERS), rewrapNames(result.get(PUBLISHERS)));
			record.put(Tuple.valueOf(PUBLISH_PLACES), rewrapNames(result.get(PUBLISH_PLACES)));
			record.put(Tuple.valueOf(PUBLISH_DATE), doubleWrap(result.get(PUBLISH_DATE)));
			
			records.add(record);
		}
		
		return records;
	}
	
	@SuppressWarnings("unchecked")
	private static List<List<String>> wrap(String string) {
		return Arrays.asList(Arrays.asList(string));
	}
	
	private static List<List<String>> rewrap(List<String> list) {
		List<List<String>> wrapped = new ArrayList<List<String>>();
		for (String item : list) {
			wrapped.add(Arrays.asList(item));
		}
		return wrapped;
	}
	
	@SuppressWarnings("unchecked")
	private static List<List<String>> rewrapNames(Object mapList) {
		List<List<String>> wrapped = new ArrayList<List<String>>();
		for (Map<String,String> map : (List<Map<String,String>>)mapList) {
			wrapped.add(Arrays.asList(map.get("name")));
		}
		return wrapped;
	}
	
	@SuppressWarnings("unchecked")
	private static List<List<String>> joinNames(Object mapList) {
		List<String> names = new ArrayList<String>();
		for (Map<String,String> map : (List<Map<String,String>>)mapList) {
			names.add(map.get("name"));
		}
		return wrap(StringUtils.join(names, "; "));
	}
	
	@SuppressWarnings("unchecked")
	private static List<List<String>> doubleWrap(Object string) {
		return  Arrays.asList(Arrays.asList((String)string));
	}
	
	public static void main(String[] args) {
		// Just a demo...
		EnumUtils.setEnums(new EnumConfiguration() {
			@Override
			public Map<String, List<String>> getTuplesMap() {
				Map<String, List<String>> map = new HashMap<String, List<String>>();
				for (String term : getTerms()) {
					map.put(term, Arrays.asList(term));
				}
				return map;
			}
			
			@Override
			public List<String> getTerms() {
				return Arrays.asList(
					LCCN, ISBN_10, OCLC, //
					DDC, LCC, //
					TITLE, BY_STATEMENT, AUTHORS, NOTES, NUMBER_OF_PAGES, PAGINATION, //
					SUBJECT_PLACES, SUBJECTS, SUBJECT_PEOPLE, SUBJECT_TIMES, //
					PUBLISHERS, PUBLISH_PLACES, PUBLISH_DATE);
			}
			
			@SuppressWarnings("serial")
			@Override
			public Map<String, String> getQSolr() {
				return new HashMap<String, String>(){{
					this.put("ISBN", "ISBN");
				}};
			}
		});
		
		OpenLibraryQueryEngine engine = new OpenLibraryQueryEngine();
		@SuppressWarnings("serial")
		Map<String,String[]> params = new HashMap<String,String[]>(){{
			this.put("ISBN", new String[]{"0451526538"});
		}};
		System.out.print(engine.query(params));
	}
}
