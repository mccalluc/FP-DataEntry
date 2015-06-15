package org.filteredpush.dataentry.configuration;

import java.io.File;
import java.net.URI;

interface CoreSolrConfiguration {
	public File getSolrDirectory();
	public URI getSolrUri(); // TODO: maybe we should just return a SolrServer and hide the details?
}
