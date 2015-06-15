cat >$INPUT <<END_INPUT
title|subject|description|creator|source|publisher|date|contributor|rights|relation|format|language|type|identifier|coverage
The John Harvard family collection|John Harvard|John Harvard (1607-1638) was an English clergyman who settled in the American colonies, in Charlestown, Massachusetts, in 1637. Upon his death in 1638, Harvard left half of his estate and personal library to the newly founded college at Newtown which later became Cambridge, Massachusetts. The Massachusetts General Court named the college in his honor in 1639. The College became Harvard University in 1780. This collection consists of letters, photographs, legal documents (facsimiles and originals), news clippings, poetry, pamphlets, and postcards presenting what little information is available about John Harvard's life and family as well as the many honors and memorials bestowed upon Harvard after his death by generations of Harvard alumni.|Harvard University|||1577, 1622, and 1828-2007||The John Harvard family collection is open for research. Availability of materials that are fragile or otherwise require special handling may limited. Consult reference staff for details.||3 cubic feet (6 document boxes, 6 portfolio boxes)|English|Archival collection|HUG 1447|
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
						<field name='subject' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='description' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='creator' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='source' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='publisher' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='date' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='contributor' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='rights' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='relation' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='format' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='language' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='type' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='identifier' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='coverage' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
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
			<item key='subject'><item>subject</item></item>
			<item key='description'><item>description</item></item>
			<item key='creator'><item>creator</item></item>
			<item key='source'><item>source</item></item>
			<item key='publisher'><item>publisher</item></item>
			<item key='date'><item>date</item></item>
			<item key='contributor'><item>contributor</item></item>
			<item key='rights'><item>rights</item></item>
			<item key='relation'><item>relation</item></item>
			<item key='format'><item>format</item></item>
			<item key='language'><item>language</item></item>
			<item key='type'><item>type</item></item>
			<item key='identifier'><item>identifier</item></item>
			<item key='coverage'><item>coverage</item></item>
		</tuples-map>
	</term-tuple-configuration>

	<ingester-configuration>
		<!-- Replace this with the full path of the file you want to read. -->
		<ingest-file>$INPUT</ingest-file>
		<delimiter>|</delimiter>
	</ingester-configuration>

	<front-end-configuration>

		<title-html>DEMO: Omeka integration</title-html>
		<blurb-html>
			<p>This demonstrates how the FilteredPush Data Entry Plugin might be integrated with Omeka.
			(It is <i>only</i> a demonstration: There is just a single record in the back-end database.)</p>
			<ol>
				<li>Save the bookmarklet (in blue) at the bottom of the page.</li>
				<li>Bring up the <a href='http://omeka.org/sandbox/admin/items/add' target='_blank'>Omeka demo item-add page</a>
					and log in with user "demo"; password "sandbox".</li>
				<li>Enter "Harvard" as Title in Omeka.</li>
				<li>Click on the bookmarklet you have saved.</li>
				<li>At this point the Plugin takes over: it locates a record matching the information you have provided,
					and copies that information back to Omeka.</li>
				<li>From here, you can edit the suggested record, or you could refine your search.</li>
 			</ol>
			<p>The plugin is parameterizable, and you could easily target a different form in Omeka,
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
			<item key='Elements[50][0][text]' label='title' q-name='title' a-name='title' default='Harvard'/>
			<item key='Elements[49][0][text]' label='subject' a-name='subject'/>
			<item key='Elements[41][0][text]' label='description' a-name='description'/>
			<item key='Elements[39][0][text]' label='creator' a-name='creator'/>
			<item key='Elements[48][0][text]' label='source' a-name='source'/>
			<item key='Elements[45][0][text]' label='publisher' a-name='publisher'/>
			<item key='Elements[40][0][text]' label='date' a-name='date'/>
			<item key='Elements[37][0][text]' label='contributor' a-name='contributor'/>
			<item key='Elements[47][0][text]' label='rights' a-name='rights'/>
			<item key='Elements[46][0][text]' label='relation' a-name='relation'/>
			<item key='Elements[42][0][text]' label='format' a-name='format'/>
			<item key='Elements[44][0][text]' label='language' a-name='language'/>
			<item key='Elements[51][0][text]' label='type' a-name='type'/>
			<item key='Elements[43][0][text]' label='identifier' a-name='identifier'/>
			<item key='Elements[38][0][text]' label='coverage' a-name='coverage'/>
		</input-fields>

	</front-end-configuration>

</configuration>
END_CONFIG
