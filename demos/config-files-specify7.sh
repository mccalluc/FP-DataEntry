cat >$INPUT <<END_INPUT 
altCatalogNumber|taxon|preferredTaxon|determiner|collectingEvent|size|sex|weight|remarks
1234|Tetraodon mola|Mola mola|Chuck McCallum|1234: Adriatic Sea|1.8m long / 2.5m fin-to-fin|Male|1000kg|Largest bony fish
END_INPUT

cat >$CONFIG <<END_CONFIG 
<configuration> 

	<solr-configuration>
		<solr-directory>$SOLR_DIR</solr-directory>
		<solr-files>
			<item key="schema.xml">
				<!-- For more information, see http://wiki.apache.org/solr/SchemaXml -->
				<schema name="example" version="1.5">
					<fields>
						<field name="_fp_internal_id" type="fpID" indexed="true" stored="false" required="true" multiValued="false"/>
						<field name='altCatalogNumber' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='taxon' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='preferredTaxon' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='determiner' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='collectingEvent' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='size' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='sex' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='weight' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='remarks' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
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
			<item key='altCatalogNumber'><item>altCatalogNumber</item></item>
			<item key='taxon'><item>taxon</item></item>
			<item key='preferredTaxon'><item>preferredTaxon</item></item>
			<item key='determiner'><item>determiner</item></item>
			<item key='collectingEvent'><item>collectingEvent</item></item>
			<item key='size'><item>size</item></item>
			<item key='sex'><item>sex</item></item>
			<item key='weight'><item>weight</item></item>
			<item key='remarks'><item>remarks</item></item>
		</tuples-map>
	</term-tuple-configuration>
	
	<ingester-configuration>
		<!-- Replace this with the full path of the file you want to read. -->
		<ingest-file>$INPUT</ingest-file>
		<delimiter>|</delimiter>
	</ingester-configuration>
	
	<front-end-configuration>
	
		<title-html>DEMO: Specify Integration</title-html>
		<blurb-html>
			<p>This demonstrates how the FilteredPush Data Entry Plugin might be integrated with Specify.
				(It is <i>only</i> a demonstration: There is just a single record in the back-end database.)</p>
			<ol>
				<li>Save the bookmarklet (in blue) at the bottom of the page.</li>
				<li>Login to the <a href='http://7beta.specifysoftware.org/specify/view/collectionobject/new/' target='_blank'>Specify demo</a> with username "sp7demofish"; password "sp7demofish".</li>
				<li>You should be on the "New Collection Object" page: Enter "1234" as "Prev/Exch #".</li>
				<li>Click on the bookmarklet you have saved.</li>
				<li>At this point the Plugin takes over: it locates a record matching the information you have provided,
					and copies that information back to Specify.</li>
				<li>From here, you can edit the suggested record, or you could refine your search.</li>
 			</ol>
 			<p>This also demonstrates the two javascript hooks available in the configuration: 
				<code>pre-js</code> lets us expand the "Determinations" and "Col Obj Attribute" sections automatically, and 
				<code>selector-function</code> lets us define an alternate selector, for those fields which do not have unique names.</p>
			<p>The plugin is parameterizable, and you could easily target a different form in Specify,
				or a different application altogether.</p>
		</blurb-html>
		
		<!--
			Mapping from query parameters to solr fields.
		-->
		<q-solr>
			<item key='altCatalogNumber'>altCatalogNumber</item>
		</q-solr>
		
		<!--
		The "key" will usually be the name attribute of the input, but if a "selector-function" is given below,
		it can be any string the selector-function can use to identify a unique input.
		
		The "label" is the human readable label beside it. (The name is used if unspecified.)
		
		At query time, inputs are mapped to query parameters with "q-name".
		
		When the results return, they are mapped back to inputs with "a-name".
		-->
		<input-fields>
			<item key='altCatalogNumber' label='altCatalogNumber' q-name='altCatalogNumber' a-name='altCatalogNumber' default='1234'/>
			<item key='taxon' label='taxon' a-name='taxon'/>
			<item key='preferredTaxon' label='preferredTaxon' a-name='preferredTaxon'/>
			<item key='determiner' label='determiner' a-name='determiner'/>
			<item key='collectingEvent' label='collectingEvent' a-name='collectingEvent'/>
			<item key='#specify-field-5-1' label='size' a-name='size'/>
			<item key='#specify-field-5-4' label='sex' a-name='sex'/>
			<item key='#specify-field-5-3' label='weight' a-name='weight'/>
			<item key='remarks' label='remarks' a-name='remarks'/>
		</input-fields>

		<selector-function>
			function(selector){
				if (selector.match(/^\w+$/)) {
					return document.getElementsByName(selector).item(0);
				} else if (typeof jQuery != 'undefined') {
					return jQuery(selector)[0];
				}
			}
		</selector-function>

		<pre-js>
			if (typeof jQuery != 'undefined') {
				jQuery('[data-specify-field-name=determinations] .specify-add-related').click();
				jQuery('[data-specify-field-name=collectionObjectAttribute] .specify-add-related').click();
			}
		</pre-js>
		
	</front-end-configuration>
	
</configuration>
END_CONFIG
