cat >$CONFIG <<END_CONFIG
<configuration>
	
	<!-- The <*-configuration> groupings are purely cosmetic: Only the tags that directly contain data matter. -->

	<solr-configuration>
		<solr-uri>http://localhost:8983/solr/</solr-uri>
		<solr-file-references>
			<!-- TODO: make the paths generic if we keep this. -->
			<item key="schema.xml">/home/chuck/FP-solr-demo/solr/collection1/conf/schema.xml</item>
			<!-- solrconfig.xml is needed for installation, but we don't check against it for anything else. -->
		</solr-file-references>
	</solr-configuration>

	<server-configuration>
		<port>$PORT</port><!-- Only used by Jetty; Ignored by Tomcat. -->
	</server-configuration>

	<term-tuple-configuration>
		<!--
			This controls the ordering and grouping of fields in your suggested matches.
			The outer items ("tuples") must correspond to fields in your solr index.
			The inner items ("terms") must correspond to fields in your data entry application.
		-->
		<tuples-map>
			<item key="recordedBy">
				<item>recordedBy</item>
			</item>
			<item key="collectorName_index">
				<item>collectorName_index</item>
			</item>
			
			<!-- TODO: add the other basic searches, at least. -->
		</tuples-map>
	</term-tuple-configuration>

	<ingester-configuration>
		<!-- Nothing is ingested. -->
	</ingester-configuration>

	<front-end-configuration>
	
		<title-html>DEMO: Solr via HTTP</title-html>
		<blurb-html>
			<p>
				The Data Entry Plugin needs a data source:
			</p>
			<ul>
				<li>Not Solr: Implement <code>GenericQueryEngine</code>. 
					Example: <a target="_blank" href="http://firuta.huh.harvard.edu:8080/fp-gbifapi-demo/">GBIF API</a>. 
					(TODO: make it possible to supply an outside jar.)</li>
				<li>Solr:
					<ul>
						<li>Embedded: All configuration and data ingest is managed by FP-DataEntry. 
							Examples: everything else listed on the <a target="_blank" href="http://sourceforge.net/p/filteredpush/svn/HEAD/tree/trunk/FP-DataEntry/">README</a></li>
						<li>HTTP: Configuration and data ingest is managed by something else. 
							Example: right here.</li>
					</ul>
				</li>
			</ul>
		</blurb-html>
		
		<!--
			Mapping from query parameters to solr fields.
			Over time, either the solr index or the client application could change:
			this allows them to be decoupled.
			TODO: It feels like, either this is gratuitous, or there should be something similar for a-name. Not sure which.
		-->
		<q-solr>
			<item key="_collectorName_index">_collectorName_index</item>
		</q-solr>
	
		<!--
			This defines the fields available in the front end demo.
			The "key" will used as the HTML "name" attribute on the inputs.
			"label" will be the human readable label beside it.
			"q-name" is the mapping of demo inputs to query terms. (You might want more than one field mapped to a single q-name.)
			"a-name" is the mapping of the query response from solr to input fields.
		-->
		<input-fields>
			<item key="recordedBy"				label="recordedBy"				a-name="recordedBy"/>
			<item key="_collectorName_index"	label="_collectorName_index"	q-name="_collectorName_index" default="Smith"/>
		</input-fields>

	</front-end-configuration>

</configuration>
END_CONFIG