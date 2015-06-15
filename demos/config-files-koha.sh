cat >$INPUT <<END_INPUT 
tag_020_a|tag_050_a|tag_082_a|tag_100_a|tag_100_d|tag_245_a|tag_245_b|tag_245_c|tag_260_a|tag_260_b|tag_260_c
9780674032811 (alk. paper)|QH365|576.8/2|Darwin, Charles,|1809-1882.|The annotated Origin : |a facsimile of the first edition of On the origin of species / |Charles Darwin ; annotated by James T. Costa.|Cambridge, Mass. : |Belknap Press of Harvard University Press,|2009
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
						<field name='tag_020_a' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='tag_050_a' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='tag_082_a' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='tag_100_a' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='tag_100_d' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='tag_245_a' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='tag_245_b' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='tag_245_c' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='tag_260_a' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='tag_260_b' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='tag_260_c' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>
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
			<item key='tag_020_a'><item>tag_020_a</item></item>
			<item key='tag_050_a'><item>tag_050_a</item></item>
			<item key='tag_082_a'><item>tag_082_a</item></item>
			<item key='tag_100_a'><item>tag_100_a</item></item>
			<item key='tag_100_d'><item>tag_100_d</item></item>
			<item key='tag_245_a'><item>tag_245_a</item></item>
			<item key='tag_245_b'><item>tag_245_b</item></item>
			<item key='tag_245_c'><item>tag_245_c</item></item>
			<item key='tag_260_a'><item>tag_260_a</item></item>
			<item key='tag_260_b'><item>tag_260_b</item></item>
			<item key='tag_260_c'><item>tag_260_c</item></item>
		</tuples-map>
	</term-tuple-configuration>
	
	<ingester-configuration>
		<!-- Replace this with the full path of the file you want to read. -->
		<ingest-file>$INPUT</ingest-file>
		<delimiter>|</delimiter>
	</ingester-configuration>
	
	<front-end-configuration>
	
		<title-html>DEMO: Koha</title-html>
		<blurb-html>
			<p>This demonstrates how the FilteredPush Data Entry Plugin might be integrated with Koha.
				(It is <i>only</i> a demonstration: There is just a single record in the back-end database.)</p>
			<p>This project began in the museums community, where the idea of "copy-cataloging" isn't as familiar.
				Koha already has good support for copy-cataloging against Z39.50 data sources, but I could imagine
				times when something like CSV might suffice... or you might have other data entry applications
				which don't support copy-cataloging workflows: using a plugin like this could be easier than
				modifying the original application.</p>
			<ol>
				<li>Save the bookmarklet (in blue) at the bottom of the page.</li>
				<li>Login to the <a href='http://library.software.coop:8080/cgi-bin/koha/cataloguing/addbiblio.pl?frameworkcode=FA' target='_blank'>Koha demo</a> with username "demo"; password "demo".</li>
				<li>You should be on the "Add MARC record" page, under the "Fast Add Framework".</li>
				<li>Enter "Origin" in 245a (Title).</li>
				<li>Click on the bookmarklet you have saved.</li>
				<li>At this point the Plugin takes over: it locates a record matching the information you have provided,
					and copies that information back to Koha.</li>
				<li>From here, you can edit the suggested record, or you could refine your search.</li>
 			</ol>
			<p>The plugin is parameterizable, and you could easily target a different form in Koha,
				or a different application altogether.</p>
		</blurb-html>
		
		<!--
			Mapping from query parameters to solr fields.
		-->
		<q-solr>
			<item key='tag_020_a'>tag_020_a</item>
			<item key='tag_100_a'>tag_100_a</item>
			<item key='tag_245_a'>tag_245_a</item>
			<item key='tag_260_a'>tag_260_a</item>
			<item key='tag_260_b'>tag_260_b</item>
			<item key='tag_260_c'>tag_260_c</item>
		</q-solr>
		
		<!--
		The "key" will usually be the name attribute of the input, but if a "selector-function" is given below,
		it can be any string the selector-function can use to identify a unique input.
		
		The "label" is the human readable label beside it. (The name is used if unspecified.)
		
		At query time, inputs are mapped to query parameters with "q-name".
		
		When the results return, they are mapped back to inputs with "a-name".
		-->
		<input-fields>
			<item key='tag_020_subfield_a' label='020_a: ISBN' q-name='tag_020_a' a-name='tag_020_a'/>
			<item key='tag_050_subfield_a' label='050_a: LC' a-name='tag_050_a'/>
			<item key='tag_082_subfield_a' label='082_a: Dewey'  a-name='tag_082_a'/>
			<item key='tag_100_subfield_a' label='100_a: Author' q-name='tag_100_a' a-name='tag_100_a'/>
			<item key='tag_100_subfield_d' label='100_d: Author (dates)' a-name='tag_100_d'/>
			<item key='tag_245_subfield_a' label='245_a: Title' q-name='tag_245_a' a-name='tag_245_a' default='Origin'/>
			<item key='tag_245_subfield_b' label='245_b: Subtitle' a-name='tag_245_b'/>
			<item key='tag_245_subfield_c' label='245_c: Title (author)' a-name='tag_245_c'/>
			<item key='tag_260_subfield_a' label='260_a: Publication place' q-name='tag_260_a' a-name='tag_260_a'/>
			<item key='tag_260_subfield_b' label='260_b: Publisher' q-name='tag_260_b' a-name='tag_260_b'/>
			<item key='tag_260_subfield_c' label='260_c: Date' q-name='tag_260_c' a-name='tag_260_c'/>
		</input-fields>

		<selector-function>
			function(selector){
				if (typeof jQuery == 'undefined') {
					// Fallback just for the demo:
					return document.getElementsByName(selector)[0];
				} else {
					// In the real application, names are followed by random digits.
					var matches = jQuery('input[name^='+selector+']');
					if (matches.length != 1) {
						alert('Expected one match for "'+selector+'", not "'+matches.length+'".');
					} else {
						return matches[0];
					}
				}
			}
		</selector-function>
		
	</front-end-configuration>
	
</configuration>
END_CONFIG
