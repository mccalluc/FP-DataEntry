package org.filteredpush.dataentry.configuration;

import java.util.List;
import java.util.Map;

public interface EnumConfiguration {
	// All the information necessary for initializing the run-time enums.
	public List<String> getTerms();
	public Map<String,List<String>> getTuplesMap();
	public Map<String,String> getQSolr();
}
