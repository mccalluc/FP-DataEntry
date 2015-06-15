package org.filteredpush.dataentry.frontend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.filteredpush.dataentry.backend.solr.EmbeddedSolrQueryEngine;
import org.filteredpush.dataentry.configuration.FrontEndConfiguration;

class MockFrontEndConfiguration implements FrontEndConfiguration {
	private static final Map<String,String> MAP = new HashMap<String,String>();
	private static final List<String> LIST = new ArrayList<String>();
	private final int port;
	public MockFrontEndConfiguration(int port) {
		this.port = port;
	}
	@Override
	public int getPort() {
		return port;
	}
	// The rest are placeholders:
	@Override
	public List<String> getTerms() {
		return LIST;
	}
	@Override
	public Map<String, List<String>> getTuplesMap() {
		return new HashMap<String, List<String>>();
	}
	@Override
	public Map<String, String> getQSolr() {
		return MAP;
	}
	@Override
	public Map<String, String> getQNames() {
		return MAP;
	}
	@Override
	public Map<String, String> getInputDefaults() {
		return MAP;
	}
	@Override
	public Map<String, String> getInputLabels() {
		return MAP;
	}
	@Override
	public List<String> getSuggestionFields() {
		return LIST;
	}
	@Override
	public Map<String, Map<String, String>> getFakeAjaxData() {
		return new HashMap<String,Map<String,String>>();
	}
	@Override
	public String getTitleHtml() {
		return "";
	}
	@Override
	public String getBlurbHtml() {
		return "";
	}
	@Override
	public String toString() {
		return "<configuration><!-- This is a mock. --></configuration>";
	}
	@Override
	public Class<?> getQueryEngineClass() {
		return EmbeddedSolrQueryEngine.class;
	}
	@Override
	public boolean getRequireConfirmation() {
		return false;
	}
	@Override
	public String getPreJs() {
		return "";
	}
	@Override
	public Map<String, String> getSelectors() {
		return MAP;
	}
	@Override
	public String getSelectorFunction() {
		return "";
	}
	@Override
	public List<String> getInputKeys() {
		return LIST;
	}
	@Override
	public boolean getHasSelectorFunction() {
		return false;
	}
}
