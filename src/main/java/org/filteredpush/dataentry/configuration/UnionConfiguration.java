package org.filteredpush.dataentry.configuration;

public interface UnionConfiguration extends //
		SolrInstallerConfiguration, //
		SolrIndexerConfiguration, //
		SolrFileIndexerConfiguration, //
		BackEndConfiguration, //
		FrontEndConfiguration {
	/*
	 * The point of this hierarchy is that a given method will be defined in just one interface.
	 * The return types can be complicated, and if they need to change, it's a pain to track them 
	 * down across every interface.
	 *
	 * grep -h -P '^\s*public .*();' * | perl -pne 's/\s(\w+\(\))/$1/' | sort | uniq -d
	 *
	 * ie: method definitions, cleaned up, find duplicates: There should be none.
	 */
}
