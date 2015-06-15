package org.filteredpush.dataentry.configuration;

import java.nio.charset.Charset;
import java.util.Map;

public interface SolrFileIndexerConfiguration extends SolrIndexerConfiguration {
	public Charset getEncoding();
	public String getDelimiter();
	public Character getQuoteCharacter();
	public String getNullMarker();
	public Map<String, String> getColumnMap();
}
