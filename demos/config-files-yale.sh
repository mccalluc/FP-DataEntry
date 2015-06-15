cat >$INPUT <<END_INPUT
genus|specificEpithet|infraspecificRank|infraspecificEpithet|scientificNameAuthorship|identificationQualifier|collector|collectionNumber|verbatimCollectionDate|country|state|county|city
genus|specificEpithet|infraspecificRank|infraspecificEpithet|scientificNameAuthorship|identificationQualifier|collector|collectionNumber|verbatimCollectionDate|country|state|county|city
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
						<field name='genus' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='specificEpithet' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='infraspecificRank' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='infraspecificEpithet' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='scientificNameAuthorship' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='identificationQualifier' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='collector' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='collectionNumber' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='verbatimCollectionDate' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='country' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='state' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='county' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='city' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
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
			<item key='genus'><item>genus</item></item>
			<item key='specificEpithet'><item>specificEpithet</item></item>
			<item key='infraspecificRank'><item>infraspecificRank</item></item>
			<item key='infraspecificEpithet'><item>infraspecificEpithet</item></item>
			<item key='scientificNameAuthorship'><item>scientificNameAuthorship</item></item>
			<item key='identificationQualifier'><item>identificationQualifier</item></item>
			<item key='collector'><item>collector</item></item>
			<item key='collectionNumber'><item>collectionNumber</item></item>
			<item key='verbatimCollectionDate'><item>verbatimCollectionDate</item></item>
			<item key='country'><item>country</item></item>
			<item key='state'><item>state</item></item>
			<item key='county'><item>county</item></item>
			<item key='city'><item>city</item></item>
		</tuples-map>
	</term-tuple-configuration>

	<ingester-configuration>
		<!-- Replace this with the full path of the file you want to read. -->
		<ingest-file>$INPUT</ingest-file>
		<delimiter>|</delimiter>
	</ingester-configuration>

	<front-end-configuration>

		<title-html>DEMO</title-html>
		<blurb-html>$BLURB</blurb-html>
	
		<!--
			Mapping from query parameters to solr fields.
		-->
		<q-solr>
			<item key='genus'>genus</item>
			<item key='specificEpithet'>specificEpithet</item>
			<item key='infraspecificRank'>infraspecificRank</item>
			<item key='infraspecificEpithet'>infraspecificEpithet</item>
			<item key='scientificNameAuthorship'>scientificNameAuthorship</item>
			<item key='identificationQualifier'>identificationQualifier</item>
			<item key='collector'>collector</item>
			<item key='collectionNumber'>collectionNumber</item>
			<item key='verbatimCollectionDate'>verbatimCollectionDate</item>
			<item key='country'>country</item>
			<item key='state'>state</item>
			<item key='county'>county</item>
			<item key='city'>city</item>
		</q-solr>
	
		<!--
			This defines the fields available in the front end demo.
			The "key" will used as the HTML "name" attribute on the inputs.
			"label" will be the human readable label beside it.
			"q-name" is the mapping of demo inputs to query terms.
			"a-name" is the mapping of the query response from solr to input fields.
		-->
		<input-fields>
			<item key='genus' label='genus' q-name='genus' a-name='genus' default='genus'/>
			<item key='specificEpithet' label='specificEpithet' q-name='specificEpithet' a-name='specificEpithet'/>
			<item key='infraspecificRank' label='infraspecificRank' q-name='infraspecificRank' a-name='infraspecificRank'/>
			<item key='infraspecificEpithet' label='infraspecificEpithet' q-name='infraspecificEpithet' a-name='infraspecificEpithet'/>
			<item key='scientificNameAuthorship' label='scientificNameAuthorship' q-name='scientificNameAuthorship' a-name='scientificNameAuthorship'/>
			<item key='identificationQualifier' label='identificationQualifier' q-name='identificationQualifier' a-name='identificationQualifier'/>
			<item key='collector' label='collector' q-name='collector' a-name='collector'/>
			<item key='collectionNumber' label='collectionNumber' q-name='collectionNumber' a-name='collectionNumber'/>
			<item key='verbatimCollectionDate' label='verbatimCollectionDate' q-name='verbatimCollectionDate' a-name='verbatimCollectionDate'/>
			<item key='country' label='country' q-name='country' a-name='country'/>
			<item key='state' label='state' q-name='state' a-name='state'/>
			<item key='county' label='county' q-name='county' a-name='county'/>
			<item key='city' label='city' q-name='city' a-name='city'/>
		</input-fields>

		<require-confirmation>true</require-confirmation>

	</front-end-configuration>

</configuration>
END_CONFIG
