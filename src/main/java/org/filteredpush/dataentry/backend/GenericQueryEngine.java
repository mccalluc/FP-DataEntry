package org.filteredpush.dataentry.backend;

import java.util.List;
import java.util.Map;

public interface GenericQueryEngine {

	public List<TupleMultiRecord> query(Map<String,String[]> urlParams);
	
	public void shutdown();
	
}
