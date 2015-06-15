package org.filteredpush.dataentry.configuration;

import java.io.File;

import org.filteredpush.dataentry.backend.EncodingCorrection;

public interface SolrIndexerConfiguration extends CoreSolrConfiguration, CoreTermTupleConfiguration, EnumConfiguration {
	public File getIngestFile();
	public IndexerType getIngestType();
	public EncodingCorrection getEncodingCorrection();
	public Class<?> getQueryEngineClass(); // (Just so we can see if solr can be skipped.)
}
