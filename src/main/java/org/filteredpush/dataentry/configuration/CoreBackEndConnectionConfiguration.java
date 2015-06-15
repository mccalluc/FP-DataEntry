package org.filteredpush.dataentry.configuration;

import java.util.Map;

interface CoreBackEndConnectionConfiguration {
	public Map<String,String> getQSolr();
	public Class<?> getQueryEngineClass();
}
