cat >$CONFIG <<END_CONFIG
<configuration>
	
	<!-- The <*-configuration> groupings are purely cosmetic: Only the tags that directly contain data matter. -->
	
	<back-end-configuration>
		<query-engine-class>org.filteredpush.dataentry.extras.GbifApiQueryEngine</query-engine-class>
	</back-end-configuration>

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
			<item key="scientificName">
				<item>scientificName</item>
			</item>
			<item key="collectorName">
				<item>collectorName</item>
			</item>
			<item key="occurrenceDate">
				<item>occurrenceDate</item>
			</item>
			<item key="locality">
				<item>locality</item>
			</item>
		</tuples-map>
	</term-tuple-configuration>

	<front-end-configuration>
	
		<title-html>DEMO: GBIF API</title-html>
		<blurb-html>
			<p>
				The Data Entry Plugin needs a data source:
			</p>
			<ul>
				<li>Not Solr: Implement <code>GenericQueryEngine</code>. 
					Example: right here.</li>
				<li>Solr:
					<ul>
						<li>Embedded: All configuration and data ingest is managed by FP-DataEntry. 
							Examples: everything else listed on the <a target="_blank" href="http://sourceforge.net/p/filteredpush/svn/HEAD/tree/trunk/FP-DataEntry/">README</a></li>
						<li>HTTP: Configuration and data ingest is managed by someone else. 
							Example: TODO.</li>
					</ul>
				</li>
			</ul>
			<p>
				(TODO: make it possible to supply an outside jar.)
			</p>
		</blurb-html>
		
		<!--
			Mapping from query parameters to solr fields.
			Over time, either the solr index or the client application could change:
			this allows them to be decoupled.
			TODO: It feels like, either this is gratuitous, or there should be something similar for a-name. Not sure which.
		-->
		<q-solr>
			<item key="scientificName">scientificName</item>
			<item key="collectorName">collectorName</item>
			<item key="occurrenceDate">occurrenceDate</item>
			<item key="locality">locality</item>
		</q-solr>
	
		<!--
			This defines the fields available in the front end demo.
			The "key" will used as the HTML "name" attribute on the inputs.
			"label" will be the human readable label beside it.
			"q-name" is the mapping of demo inputs to query terms. (You might want more than one field mapped to a single q-name.)
			"a-name" is the mapping of the query response from solr to input fields.
		-->
		<input-fields>
			<item key="scientificName"	label="scientificName"	q-name="scientificName"	a-name="scientificName"/>
			<item key="collectorName"	label="collectorName"	q-name="collectorName"	a-name="collectorName" default="Smith"/>
			<item key="occurrenceDate"	label="occurrenceDate"	q-name="occurrenceDate"	a-name="occurrenceDate" />
			<item key="locality"		label="locality"		q-name="locality"		a-name="locality"/>
		</input-fields>

		<!--
			This lists the terms where the UI should offer choices from a controlled vocabulary.
			The sub-elements, if present, support a mock-up of the AJAX interface which you
			would need to implement on your collection management application.
		-->
		<!--
		<controlled-vocabularies>
			<item key="recordedBy">
				<item key="Zimmerman, Z.">28</item>
			</item>
		</controlled-vocabularies>
		-->

	</front-end-configuration>

</configuration>
END_CONFIG
