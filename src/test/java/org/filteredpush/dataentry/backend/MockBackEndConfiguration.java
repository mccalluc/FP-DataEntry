package org.filteredpush.dataentry.backend;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.filteredpush.dataentry.backend.solr.EmbeddedSolrQueryEngine;
import org.filteredpush.dataentry.configuration.BackEndConfiguration;

class MockBackEndConfiguration implements BackEndConfiguration {
	private static final Map<String,String> MAP = new HashMap<String,String>();
	private static final List<String> LIST = new ArrayList<String>();
	private final int port;
	private final File solrDirectory;
	public MockBackEndConfiguration(File solrDirectory, int port) {
		this.solrDirectory = solrDirectory;
		this.port = port;
	}
	@Override
	public URI getSolrUri() {
		throw new NoSuchElementException("(As if this element were missing from the XML.)");
	}
	@Override
	public File getSolrDirectory() {
		return solrDirectory;
	}
	@Override
	public List<String> getTerms() {
		return LIST;
	}
	@Override
	public Map<String, List<String>> getTuplesMap() {
		return null;
	}
	@Override
	public Map<String, String> getQSolr() {
		return MAP;
	}
	@Override
	public int getPort() {
		return port;
	}
	@Override
	public Class<?> getQueryEngineClass() {
		return EmbeddedSolrQueryEngine.class;
	}
}
