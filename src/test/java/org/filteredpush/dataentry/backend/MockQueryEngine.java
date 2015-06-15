package org.filteredpush.dataentry.backend;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.filteredpush.dataentry.enums.Tuple;

public class MockQueryEngine implements GenericQueryEngine {

	private static final String NAME = "name";
	
	@Override
	public List<TupleMultiRecord> query(Map<String, String[]> urlParams) {
		TupleMultiRecord record = new TupleMultiRecord();
		Tuple tuple = Tuple.valueOf(NAME);
		@SuppressWarnings("unchecked")
		List<List<String>> listList = Arrays.asList(Arrays.asList("John","Smith"));
		record.put(tuple, listList);
		return Arrays.asList(record);
	}

	@Override
	public void shutdown() {
		// no-op
	}

}
