cat >$INPUT <<END_INPUT
displayName|mainName|foundingDate|foundingPlace|group|function|history|emailType|email|phoneType|phone|faxType|fax|web1Type|web1|web2Type|web2|addressType|street|city|postalCode|state|country
Harvard University Herbaria||1842|Cambridge, Massachusetts|Harvard Museum of Natural History|Museum|Founded in 1842 by Asa Gray|business|fake@harvard.edu|business|123-456-7890|business|123-456-7890|business|http://www.huh.harvard.edu/|other|http://en.wikipedia.org/wiki/Harvard_University_Herbaria|business|22 Divinity Ave.|Cambridge|02138|Massachusetts|US
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
						<field name='displayName' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='mainName' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='foundingDate' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='foundingPlace' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='group' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='function' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='history' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='emailType' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='email' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='phoneType' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='phone' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='faxType' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='fax' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='web1Type' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='web1' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='web2Type' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='web2' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='addressType' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='street' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='city' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='postalCode' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='state' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='country' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
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
			<item key='displayName'><item>displayName</item></item>
			<item key='mainName'><item>mainName</item></item>
			<item key='foundingDate'><item>foundingDate</item></item>
			<item key='foundingPlace'><item>foundingPlace</item></item>
			<item key='group'><item>group</item></item>
			<item key='function'><item>function</item></item>
			<item key='history'><item>history</item></item>
			<item key='emailType'><item>emailType</item></item>
			<item key='email'><item>email</item></item>
			<item key='phoneType'><item>phoneType</item></item>
			<item key='phone'><item>phone</item></item>
			<item key='faxType'><item>faxType</item></item>
			<item key='fax'><item>fax</item></item>
			<item key='web1Type'><item>web1Type</item></item>
			<item key='web1'><item>web1</item></item>
			<item key='web2Type'><item>web2Type</item></item>
			<item key='web2'><item>web2</item></item>
			<item key='addressType'><item>addressType</item></item>
			<item key='street'><item>street</item></item>
			<item key='city'><item>city</item></item>
			<item key='postalCode'><item>postalCode</item></item>
			<item key='state'><item>state</item></item>
			<item key='country'><item>country</item></item>
		</tuples-map>
	</term-tuple-configuration>

	<ingester-configuration>
		<!-- Replace this with the full path of the file you want to read. -->
		<ingest-file>$INPUT</ingest-file>
		<delimiter>|</delimiter>
	</ingester-configuration>

	<front-end-configuration>

		<title-html>DEMO: CollectionSpace Integration</title-html>
		<blurb-html>
			<p>This demonstrates how the FilteredPush Data Entry Plugin might be integrated with CollectionSpace.
				(It is <i>only</i> a demonstration: There is just a single record in the back-end database.)</p>
			<ol>
				<li>Save the bookmarklet (in blue) at the bottom of the page.</li>
				<li>Login to the <a href='http://demo.collectionspace.org'>CollectionSpace demo</a> with the admin credentials provided on that page.</li>
				<li>Once you're in, go to <a href='http://demo.collectionspace.org/collectionspace/ui/core/html/organization.html?vocab=organization'>Create New &gt; Organization</a>.</li>
				<li>Enter "Harvard" as "Display Name".</li>
				<li>Click on the bookmarklet you have saved.</li>
				<li>At this point the Plugin takes over: it locates a record matching the information you have provided,
					and copies that information back to CollectionSpace.</li>
				<li>From here, you can edit the suggested record, or you could refine your search.</li>
 			</ol>
 			<p>This also demonstrates the use of the <code>pre-js</code> configuration option.
				Here we're using it to emulate a UI click that gives us a second field for URLs.</p>
			<p>The plugin is parameterizable, and you could easily target a different form in CollectionSpace,
				or a different application altogether.</p>
		</blurb-html>

		<!--
			Mapping from query parameters to solr fields.
		-->
		<q-solr>
			<item key='displayName'>displayName</item>
		</q-solr>

		<!--
			This defines the fields available in the front end demo.
			The "key" will used as the HTML "name" attribute on the inputs.
			"label" will be the human readable label beside it.
			"q-name" is the mapping of demo inputs to query terms.
			"a-name" is the mapping of the query response from solr to input fields.
		-->
		<input-fields>
			<item key='repeat::.csc-orgAuthority-termDisplayName' label='displayName' q-name='displayName' a-name='displayName' default='Harvard'/>
			<item key='repeat::.csc-orgAuthority-mainBodyName' label='mainName' a-name='mainName'/>
			<item key='.csc-organizationAuthority-foundingDate' label='foundingDate' a-name='foundingDate'/>
			<item key='.csc-orgAuthority-foundingPlace' label='foundingPlace' a-name='foundingPlace'/>
			<item key='repeat::.csc-orgAuthority-group' label='group' a-name='group'/>
			<item key='repeat::.csc-orgAuthority-function' label='function' a-name='function'/>
			<item key='repeat::.csc-orgAuthority-history' label='history' a-name='history'/>
			<item key='repeat::.csc-contact-emailType-selection' label='emailType' a-name='emailType'/>
			<item key='repeat::.csc-contact-email' label='email' a-name='email'/>
			<item key='repeat::.csc-contact-telephoneNumberType-selection' label='phoneType' a-name='phoneType'/>
			<item key='repeat::.csc-contact-telephoneNumber' label='phone' a-name='phone'/>
			<item key='repeat::.csc-contact-faxNumberType-selection' label='faxType' a-name='faxType'/>
			<item key='repeat::.csc-contact-faxNumber' label='fax' a-name='fax'/>
			<item key='repeat::.csc-contact-webAddressType-selection' label='web1Type' a-name='web1Type'/>
			<item key='repeat:1:.csc-contact-webAddressType-selection' label='web2Type' a-name='web2Type'/>
			<item key='repeat::.csc-contact-webAddress' label='web1' a-name='web1'/>
			<item key='repeat:1:.csc-contact-webAddress' label='web2' a-name='web2'/>
			<item key='repeat::.csc-contact-addressType-selection' label='addressType' a-name='addressType'/>
			<item key='repeat::.csc-contact-addressPlace1' label='street' a-name='street'/>
			<item key='repeat::.csc-contact-addressMunicipality' label='city' a-name='city'/>
			<item key='repeat::.csc-contact-addressPostCode' label='postalCode' a-name='postalCode'/>
			<item key='repeat::.csc-contact-addressStateOrProvince' label='state' a-name='state'/>
			<item key='repeat::.csc-contact-addressCountry-selection' label='country' a-name='country'/>
		</input-fields>
		
		<pre-js>
			jQuery('.csc-contact-webAddressGroup-label').parent().siblings().find('[value="+"]').click();
		</pre-js>

	</front-end-configuration>

</configuration>
END_CONFIG
