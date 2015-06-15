cat >$INPUT <<END_INPUT
title|identifier|levelOfDescription|resourceType|containerSummary|physicalDetails|dimensions|findingAidTitle|findingAidDate|findingAidAuthor|findingAidRevisionDate|findingAidRevisionDescription
The John Harvard family collection|HUG 1447|collection|collection|The collection is arranged in four series: I. Biographical materials, II. Harvard family deeds and legal documents, III. Writings about John Harvard and his family, and IV. Memorials and commemorations.|6 document boxes, 6 portfolio boxes|3 cubic feet|The John Harvard family collection, 1577, 1622 and 1828-2007.|April 2007|Dominic P. Grandinetti|February 24, 2014|
END_INPUT

cat >$CONFIG <<END_CONFIG
<configuration>

	<!-- The <*-configuration> groupings are purely cosmetic. -->

	<solr-configuration>
		<solr-directory>$SOLR_DIR</solr-directory>
		<solr-files>
			<item key="schema.xml">
				<!-- For more information, see http://wiki.apache.org/solr/SchemaXml -->
				<schema name="example" version="1.5">
					<fields>
						<field name="_fp_internal_id" type="fpID" indexed="true" stored="false" required="true" multiValued="false"/>
						<field name='title' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='identifier' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='levelOfDescription' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='resourceType' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='containerSummary' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='physicalDetails' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='dimensions' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='findingAidTitle' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='findingAidDate' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='findingAidAuthor' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='findingAidRevisionDate' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='findingAidRevisionDescription' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
					</fields>
					<uniqueKey>_fp_internal_id</uniqueKey>
					<types>
						<fieldType name="fpMinimallySearchable" class="solr.TextField" sortMissingLast="true" omitNorms="true">
							<!-- Remember that everything is wrapped in a JSON list, so some kind of parsing is essential, even if you only want exact matches. -->
							<analyzer>
								<tokenizer class="solr.StandardTokenizerFactory"/>
								<filter class="solr.LowerCaseFilterFactory"/>
							</analyzer>
						</fieldType>
						<fieldType name="fpID" class="solr.StrField" sortMissingLast="true"/>
					</types>
				</schema>
			</item>
			<item key="solrconfig.xml">
				<!-- For more information, see http://wiki.apache.org/solr/SolrConfigXml. -->
				<config>
					<luceneMatchVersion>4.5</luceneMatchVersion>
					<requestHandler name="/select" class="solr.SearchHandler">
						<lst name="defaults">
							<int name="rows">10</int>
							<str name="df">text</str>
						</lst>
					</requestHandler>
					<requestHandler name="/update" class="solr.UpdateRequestHandler"/>
				</config> 
			</item>
		</solr-files>
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
			<item key='title'><item>title</item></item>
			<item key='identifier'><item>identifier</item></item>
			<item key='levelOfDescription'><item>levelOfDescription</item></item>
			<item key='resourceType'><item>resourceType</item></item>
			<item key='containerSummary'><item>containerSummary</item></item>
			<item key='physicalDetails'><item>physicalDetails</item></item>
			<item key='dimensions'><item>dimensions</item></item>
			<item key='findingAidTitle'><item>findingAidTitle</item></item>
			<item key='findingAidDate'><item>findingAidDate</item></item>
			<item key='findingAidAuthor'><item>findingAidAuthor</item></item>
			<item key='findingAidRevisionDate'><item>findingAidRevisionDate</item></item>
			<item key='findingAidRevisionDescription'><item>findingAidRevisionDescription</item></item>
		</tuples-map>
	</term-tuple-configuration>

	<ingester-configuration>
		<!-- Replace this with the full path of the file you want to read. -->
		<ingest-file>$INPUT</ingest-file>
		<delimiter>|</delimiter>
	</ingester-configuration>

	<front-end-configuration>

		<title-html>DEMO: ArchivesSpace integration</title-html>
		<blurb-html>
			<p>This demonstrates how the FilteredPush Data Entry Plugin might be integrated with ArchivesSpace.
			(It is <i>only</i> a demonstration: There is just a single record in the back-end database.)</p>
			<ol>
				<li>Save the bookmarklet (in blue) at the bottom of the page.</li>
				<li>Login to the <a href='http://sandbox.archivesspace.org/?login=true'>ArchivesSpace demo</a> with username "admin" / password "admin".</li>
				<li>Once you're in, go to <a href='http://sandbox.archivesspace.org/resources/new'>Create &gt; Resource</a>.</li>
				<li>Enter "Harvard" as "Title".</li>
				<li>Click on the bookmarklet you have saved.</li>
				<li>At this point the Plugin takes over: it locates a record matching the information you have provided,
					and copies that information back to ArchivesSpace.</li>
				<li>From here, you can edit the suggested record, or you could refine your search.</li>
 			</ol>
			<p>The plugin is parameterizable, and you could easily target a different form in ArchivesSpace,
			or a different application altogether.</p>
		</blurb-html>

		<!--
			Mapping from query parameters to solr fields.
		-->
		<q-solr>
			<item key='title'>title</item>
		</q-solr>

		<!--
			This defines the fields available in the front end demo.
			The "key" will used as the HTML "name" attribute on the inputs.
			"label" will be the human readable label beside it.
			"q-name" is the mapping of demo inputs to query terms.
			"a-name" is the mapping of the query response from solr to input fields.
		-->
		<input-fields>
			<item key='resource[title]' label='title' q-name='title' a-name='title' default='Harvard'/>
			<item key='resource[id_0]' label='identifier' a-name='identifier'/>
			<item key='resource[level]' label='levelOfDescription' a-name='levelOfDescription'/>
			<item key='resource[resource_type]' label='resourceType' a-name='resourceType'/>
			<item key='resource[extents][0][container_summary]' label='containerSummary' a-name='containerSummary'/>
			<item key='resource[extents][0][physical_details]' label='physicalDetails' a-name='physicalDetails'/>
			<item key='resource[extents][0][dimensions]' label='dimensions' a-name='dimensions'/>
			<item key='resource[finding_aid_title]' label='findingAidTitle' a-name='findingAidTitle'/>
			<item key='resource[finding_aid_date]' label='findingAidDate' a-name='findingAidDate'/>
			<item key='resource[finding_aid_author]' label='findingAidAuthor' a-name='findingAidAuthor'/>
			<item key='resource[finding_aid_revision_date]' label='findingAidRevisionDate' a-name='findingAidRevisionDate'/>
			<item key='resource[finding_aid_revision_description]' label='findingAidRevisionDescription' a-name='findingAidRevisionDescription'/>
		</input-fields>

	</front-end-configuration>

</configuration>
END_CONFIG
