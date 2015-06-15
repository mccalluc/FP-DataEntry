package org.filteredpush.dataentry.configuration;

import java.util.Map;


public interface SolrInstallerConfiguration extends CoreTermTupleConfiguration, CoreSolrConfiguration, EnumConfiguration {
	public Map<String,String> getSolrFiles();
	public Class<?> getQueryEngineClass(); // (Just so we can see if solr can be skipped.)
}
