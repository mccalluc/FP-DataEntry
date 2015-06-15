package org.filteredpush.dataentry.configuration;

import java.util.List;
import java.util.Map;

public interface FrontEndConfiguration extends CoreTermTupleConfiguration, CoreBackEndConnectionConfiguration, EnumConfiguration, PortConfiguration {
	public List<String> getInputKeys();
	// provided already: public Map<String, String> getQSolr();
	public Map<String, String> getQNames();
	public Map<String, String> getInputDefaults();
	public Map<String, String> getInputLabels();
	public List<String> getSuggestionFields();
	public Map<String, Map<String, String>> getFakeAjaxData();
	
	public Map<String,String> getSelectors();
	public String getSelectorFunction();
	public boolean getHasSelectorFunction();
	
	public String getTitleHtml();
	public String getBlurbHtml();
	public boolean getRequireConfirmation();
	public String getPreJs();
}
