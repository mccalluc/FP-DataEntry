package org.filteredpush.dataentry.configuration;

import java.util.List;
import java.util.Map;

interface CoreTermTupleConfiguration {
	public List<String> getTerms();
	public Map<String, List<String>> getTuplesMap();
}
