package org.filteredpush.dataentry.configuration;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.backend.EncodingCorrection;
import org.filteredpush.dataentry.backend.solr.EmbeddedSolrQueryEngine;
import org.filteredpush.dataentry.enums.Term;
import org.w3c.dom.Document;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class Configuration extends ConfigurationBase implements UnionConfiguration {
	
	public Configuration(Document doc) {
		super(doc);
		validate(doc);
	}
	
	private void validate(Document doc) {
		List<String> errors = new ArrayList<String>();
		
		if (!getQSolr().keySet().equals(new HashSet<String>(getQNames().values()))) {
			errors.add(Q_NAME + " values " + getQNames().values() + " must equal " //
					+ Q_SOLR + " keys " + getQSolr().keySet());
		}
		
		if (getQueryEngineClass().equals(EmbeddedSolrQueryEngine.class)) {
			boolean hasDirectory = false;
			try {
				getSolrDirectory();
				hasDirectory = true;
			} catch (NoSuchElementException e) {}
			boolean hasUri = false;
			try {
				getSolrUri();
				hasUri = true;
			} catch (NoSuchElementException e) {}
			if (hasDirectory && hasUri) {
				errors.add("If using the "+EmbeddedSolrQueryEngine.class.getName()+", you must not have both "+SOLR_DIRECTORY+" and "+SOLR_URI);
			}
			if (!hasDirectory && !hasUri) {
				errors.add("If using the "+EmbeddedSolrQueryEngine.class.getName()+", you must have either "+SOLR_DIRECTORY+" or "+SOLR_URI);
			}
			
			Document schemaXml = Utils.parseXml(IOUtils.toInputStream(getSolrFiles().get("schema.xml")));
			
			Set<String> qSolrValues = new HashSet<String>(getQSolr().values());
			Set<String> indexedFields = Utils.evalXpath(schemaXml, "/schema/fields/field[@indexed='true']", "name");
			Set<String> qSolrNotIndexed = Sets.difference(qSolrValues, indexedFields);
			if (!qSolrNotIndexed.isEmpty()) {
				errors.add("Every " + Q_SOLR + " must be indexed: " + qSolrNotIndexed);
				// Note that reverse is not required: You may declare an index in solr but not actually use it.
			}
		} else {
			try {
				getSolrDirectory();
				errors.add("If there is a " + QUERY_ENGINE_CLASS + " there shouldn't be a " + SOLR_DIRECTORY);
			} catch (NoSuchElementException e) {}
			try {
				getSolrUri();
				errors.add("If there is a " + QUERY_ENGINE_CLASS + " there shouldn't be a " + SOLR_URI);
			} catch (NoSuchElementException e) {}
			try {
				getSolrFiles(); // We don't actually distinguish between files and file-references here.
				errors.add("If there is a " + QUERY_ENGINE_CLASS + " there shouldn't be a " + SOLR_FILES
						+ " or a " + SOLR_FILE_REFERENCES);
			} catch (NoSuchElementException e) {}
		}
		
		Set<String> aNameNotTermOrTermId = new HashSet<String>(Utils.evalXpath(doc, "//input-fields/item","a-name"));
		for (String term : Utils.evalXpath(doc, "//tuples-map/item/item", null)) { // Terms enum has not yet been initialized.
			aNameNotTermOrTermId.remove(term);
			aNameNotTermOrTermId.remove(term+Term.ID_SUFFIX);
		}
		if (!aNameNotTermOrTermId.isEmpty()) {
			errors.add(A_NAME + " attribute is not a term or term"+Term.ID_SUFFIX+": " + aNameNotTermOrTermId);
		}
		
		try {
			getTuplesMap();
		} catch (IllegalArgumentException e) {
			errors.add(e.getMessage());
		}
		
		if (errors.size() != 0) {
			throw new IllegalArgumentException(errors.toString());
		}
	}
	
	public static final String QUERY_ENGINE_CLASS = "query-engine-class";
	@Override
	public Class<?> getQueryEngineClass() {
		try {
			try {
				return Class.forName(getString(QUERY_ENGINE_CLASS));
			} catch (ClassNotFoundException e) {
				throw new Error(e);
			}
		} catch (NoSuchElementException e) {
			return EmbeddedSolrQueryEngine.class;
		}
	}
	
	public static final String Q_SOLR = "q-solr";
	@Override
	public Map<String,String> getQSolr() {
		return getMap(Q_SOLR);
	}

	public static final String INGEST_FILE = "ingest-file";
	@Override
	public File getIngestFile() {
		String name = getString(INGEST_FILE);
		File file = new File(name);
		if (!file.canRead()) {
			throw new Error("Can't read "+INGEST_FILE+" '"+name+"'");
		}
		return file;
	}
	
	public static final String INGEST_TYPE = "ingest-type";
	@Override
	public IndexerType getIngestType() {
		try {
			String content = getString(INGEST_TYPE);
			return IndexerType.valueOf(content);
		} catch (NoSuchElementException e) {
			return IndexerType.FILE;
		}
	}
	
	public static final String ENCODING_CORRECTION = "encoding-correction";
	@Override
	public EncodingCorrection getEncodingCorrection() {
		try {
			String content = getString(ENCODING_CORRECTION);
			return EncodingCorrection.valueOf(content);
		} catch (NoSuchElementException e) {
			return EncodingCorrection.NONE;
		}
	}

	public static final String SOLR_URI = "solr-uri";
	@Override
	public URI getSolrUri() {
		try {
			return new URI(getString(SOLR_URI));
		} catch (URISyntaxException e) {
			throw new Error(e);
		}
	}
	
	public static final String SOLR_DIRECTORY = "solr-directory";
	@Override
	public File getSolrDirectory() {
		return new File(getString(SOLR_DIRECTORY));
	}
	
	public static final String SOLR_FILES = "solr-files";
	public static final String SOLR_FILE_REFERENCES = "solr-file-references";
	@Override
	public Map<String,String> getSolrFiles() {
		try {
			return getMapOfXml(SOLR_FILES);
		} catch (NoSuchElementException e) {
			return getMapOfXmlReferences(SOLR_FILE_REFERENCES);
		}
	}

	@Override
	public List<String> getTerms() {
		Map<String,List<String>> mapOfLists = getMapOfLists(TUPLES_MAP);
		ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
		for (List<String> list : mapOfLists.values()) {
			setBuilder.addAll(list);
		}
		return setBuilder.build().asList();
	}

	public static final String TUPLES_MAP = "tuples-map";
	@Override
	public Map<String,List<String>> getTuplesMap() {
		return getMapOfLists(TUPLES_MAP);
	}

	public static final String PORT = "port";
	@Override
	public int getPort() {
		String content = getString(PORT);
		return Integer.parseInt(content);
	}
	
	private static final String BLURB_HTML = "blurb-html";
	@Override
	public String getBlurbHtml() {
		return getXml(BLURB_HTML);
	}
	
	private static final String TITLE_HTML = "title-html";
	@Override
	public String getTitleHtml() {
		return getXml(TITLE_HTML);
	}
	
	private static final String REQUIRE_CONFIRMATION = "require-confirmation";
	@Override
	public boolean getRequireConfirmation() {
		return getBoolean(REQUIRE_CONFIRMATION);
	}

	public static final String INPUT_FIELDS = "input-fields";
	@SuppressWarnings("serial")
	private Map<String,Map<String,String>> getInputFields() {
		return new HashMap<String,Map<String,String>>(getTransposedMapOfMaps(INPUT_FIELDS)){
			@Override
			public Map<String,String> get(Object key) {
				Map<String,String> value = super.get(key);
				if (value == null) {
					return new HashMap<String,String>();
				}
				return value;
			}
		};
	}
	
	public static final String Q_NAME = "q-name";
	@Override
	public Map<String,String> getQNames() {
		ImmutableMap.Builder<String,String> builder = new ImmutableMap.Builder<String, String>();
		for (Entry<String,String> entry : getInputFields().get(Q_NAME).entrySet()) {
			builder.put(entry.getKey(), entry.getValue());
		}
		return builder.build();
	}

	@Override
	public List<String> getInputKeys() {
		ImmutableList.Builder<String> builder = new ImmutableList.Builder<String>();
		for (String key : getMapOfMaps(INPUT_FIELDS).keySet()) {
			builder.add(key);
		}
		return builder.build();
	}
	
	public static final String A_NAME = "a-name";
	@Override
	public Map<String,String> getSelectors() {
		ImmutableMap.Builder<String,String> invertedBuilder = new ImmutableMap.Builder<String, String>();
		for (Entry<String,String> entry : getInputFields().get(A_NAME).entrySet()) {
			invertedBuilder.put(entry.getValue(), entry.getKey());
		}
		return invertedBuilder.build();
	}
	
	public static final String SELECTOR_FUNCTION = "selector-function";
	@Override
	public String getSelectorFunction() {
		try {
			return getString(SELECTOR_FUNCTION);
		} catch (NoSuchElementException e) {
			return "function(name) {"
				+   "var els = document.getElementsByName(name);"
				+   "if (els.length > 1) {"
				+     "throw Error('Name \"'+name+'\" is not unique in this document.');"
				+   "};"
				+   "return els[0];"
				+ "}";
		}
	}
	
	@Override
	public boolean getHasSelectorFunction() {
		try {
			getString(SELECTOR_FUNCTION);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public static final String DEFAULT = "default";
	@Override
	public Map<String, String> getInputDefaults() {
		return getInputFields().get(DEFAULT);
	}

	public static final String LABEL = "label";
	@Override
	public Map<String,String> getInputLabels() {
		return getInputFields().get(LABEL);
	}

	static final String CONTROLLED_VOCABULARIES = "controlled-vocabularies";
	Map<String,Map<String,String>> getControlledVocabularies() {
		try {
			return getMapOfMaps(CONTROLLED_VOCABULARIES);
		} catch (NoSuchElementException e) {
			return ImmutableMap.copyOf(new HashMap<String,Map<String,String>>());
		}
	}
	
	public static final String SUGGESTION_FIELDS = "suggestion-fields";
	@Override
	public List<String> getSuggestionFields() {
		return ImmutableList.copyOf(getControlledVocabularies().keySet());
	}

	public static final String FAKE_AJAX_DATA = "fake-ajax-data";
	@Override
	public Map<String,Map<String,String>> getFakeAjaxData() {
		return getControlledVocabularies(); 
		// This is redundant, but I think it makes things more clear to have them separate, since the uses are different.
	}

	public static final String ENCODING = "encoding";
	@Override
	public Charset getEncoding() {
		try {
			String content = getString(ENCODING);
			return Charset.forName(content);
		} catch (NoSuchElementException e) {
			return Charsets.UTF_8;
		}
	}

	public static final String DELIMITER = "delimiter";
	@Override
	public String getDelimiter() {
		try {
			return getString(DELIMITER);
		} catch (NoSuchElementException e) {
			return "\t";
		}
	}

	public static final String QUOTE_CHARACTER = "quote-character";
	@Override
	public Character getQuoteCharacter() {
		try {
			String content = getString(QUOTE_CHARACTER);
			if (content.length() != 1) {
				throw new Error("Quote character should be exactly one character, not " + content.length());
			}
			return content.charAt(0);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public static final String NULL_MARKER = "null-marker";
	@Override
	public String getNullMarker() {
		try {
			return getString(NULL_MARKER);
		} catch (NoSuchElementException e) {
			return "";
		}
	}

	public static final String COLUMN_MAP = "column-map";
	@Override
	public Map<String,String> getColumnMap() {
		try {
			return getMap(COLUMN_MAP);
		} catch (NoSuchElementException e) {
			return new ImmutableMap.Builder<String,String>().build();
		}
	}

	public static final String PRE_JS = "pre-js";
	@Override
	public String getPreJs() {
		try {
			return getString(PRE_JS);
		} catch (NoSuchElementException e) {
			return "";
		}
	}

}
