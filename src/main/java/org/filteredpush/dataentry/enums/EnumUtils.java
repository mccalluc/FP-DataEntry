package org.filteredpush.dataentry.enums;

import java.util.List;
import java.util.Map;

import org.filteredpush.dataentry.configuration.EnumConfiguration;

public class EnumUtils {

	private EnumUtils() {}

	public static void setEnums(EnumConfiguration config) {
		final List<String> terms = config.getTerms();
		final Map<String,List<String>> tuplesMap = config.getTuplesMap();
		final Map<String,String> qSolr = config.getQSolr();
		
		QParameter.init(qSolr);
		Term.init(terms);
		Tuple.init(tuplesMap);
	}

	public static void clearEnums() {
		QParameter.clear();
		Term.clear();
		Tuple.clear();
	}

}
