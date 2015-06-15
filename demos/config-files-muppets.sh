cat >$INPUT <<END_INPUT
full|short|common|genus|species|subspecies
Kermit the Frog|Kermit|frog|||
Miss Piggy|Piggy|pig|Sus|scrofa|domesticus
Beaker||human|Homo|sapiens|
Dr. Bunsen Honeydew|Bunsen|human|Homo|sapiens|
Fozzie Bear|Fozzie|bear|Ursus|arctos|
Rowlf the Dog|Rowlf|dog|Canis|lupus|familiaris
Animal||monster|||
Gonzo the Great|Gonzo|alien|||
|Gonzo|anteater?|Myrmecophaga|tridactyla|
|Gonzo|vulture?|Necrosyrtes|monachus|
|Gonzo|turkey?|Meleagris|gallopavo|
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
						<field name='muppet' type='fpFuzzy' indexed='true' stored='true' required='false' multiValued='true'/>
						<field name='species' type='fpPartial' indexed='true' stored='true' required='false' multiValued='true'/>
					</fields>
					<uniqueKey>_fp_internal_id</uniqueKey>
					<types>
						<fieldType name="fpFuzzy" class="solr.TextField" sortMissingLast="true" omitNorms="true">
							<analyzer>
								<tokenizer class="solr.StandardTokenizerFactory"/>
								<filter class="solr.LowerCaseFilterFactory"/>
								<filter class="org.apache.lucene.analysis.phonetic.PhoneticFilterFactory" encoder="Soundex" inject="false"/>
							</analyzer>
						</fieldType>
						<fieldType name="fpPartial" class="solr.TextField" sortMissingLast="true" omitNorms="true">
							<analyzer type="index">
								<tokenizer class="solr.StandardTokenizerFactory"/>
								<filter class="solr.LowerCaseFilterFactory"/>
							</analyzer>
							<analyzer type="query">
								<tokenizer class="solr.LowerCaseTokenizerFactory"/>
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
			<item key='muppet'><item>muppet</item></item>
			<item key='species'>
				<item>common</item>
				<item>genus</item>
				<item>species</item>
				<item>subspecies</item>
			</item>
		</tuples-map>
	</term-tuple-configuration>

	<ingester-configuration>
		<!-- Replace this with the full path of the file you want to read. -->
		<ingest-file>$INPUT</ingest-file>
		<delimiter>|</delimiter>
		<column-map>
			<item key="full">muppet</item>
			<item key="short">muppet</item>
		</column-map>
	</ingester-configuration>

	<front-end-configuration>
	
		<title-html>DEMO: Muppets</title-html>
		<blurb-html>
			<style>
			.data td,th {
				background: lightblue;
				font-size: 70%;
			}
			</style>
			<p>
				<i>(For an introduction, see the <a href="https://sourceforge.net/p/filteredpush/svn/HEAD/tree/trunk/FP-DataEntry/">README</a>.
					Scroll all the way down for the form.)</i>
			</p>
			<p>
				The FilteredPush Data Entry plugin has a few different ways of handling
				multi-valued fields. First, here's the data that was uploaded for this demo:
			</p>
			<table class='data'>
				<tr><th>full</th><th>short</th><th>common</th><th>genus</th><th>species</th><th>subspecies</th></tr>
				<tr><td>Kermit the Frog</td><td>Kermit</td><td>frog</td><td></td><td></td><td></td></tr>
				<tr><td>Miss Piggy</td><td>Piggy</td><td>pig</td><td>Sus</td><td>scrofa</td><td>domesticus</td></tr>
				<tr><td>Beaker</td><td></td><td>human</td><td>Homo</td><td>sapiens</td><td></td></tr>
				<tr><td>Dr. Bunsen Honeydew</td><td>Bunsen</td><td>human</td><td>Homo</td><td>sapiens</td><td></td></tr>
				<tr><td>Fozzie Bear</td><td>Fozzie</td><td>bear</td><td>Ursus</td><td>arctos</td><td></td></tr>
				<tr><td>Rowlf the Dog</td><td>Rowlf</td><td>dog</td><td>Canis</td><td>lupus</td><td>familiaris</td></tr>
				<tr><td>Animal</td><td></td><td>monster</td><td></td><td></td><td></td></tr>
				<tr><td>Gonzo the Great</td><td>Gonzo</td><td>alien</td><td></td><td></td><td></td></tr>
				<tr><td></td><td>Gonzo</td><td>anteater?</td><td>Myrmecophaga</td><td>tridactyla</td><td></td></tr>
				<tr><td></td><td>Gonzo</td><td>vulture?</td><td>Necrosyrtes</td><td>monachus</td><td></td></tr>
				<tr><td></td><td>Gonzo</td><td>turkey?</td><td>Meleagris</td><td>gallopavo</td><td></td></tr>
			</table>
			<p>
				The details of the configuration for this demo are given <a href='config.xml' target='_blank'>here</a>.
			</p>
			<p>
				First, just try searching for "Kermit". Note that, although the full "Kermit the Frog" is filled into the form,
				in the iframe the shorter version (just "Kermit") is also available in a pull-down. This happens because
				the config.xml maps both columns in the input to a single field in the solr index. By default it will select
				the longest one, but you can choose a shorter one if you prefer.
			</p>
			<pre>	
  &lt;column-map&gt;
    &lt;item key="full"&gt;muppet&lt;/item&gt;
    &lt;item key="short"&gt;muppet&lt;/item&gt;
  &lt;/column-map&gt;
			</pre>
			<p>
				Next, hit "Clear", and let's try the dog that played piano... was his name "Ralph"? If you enter that,
				and hit "Find", it will be corrected to "Rowlf". Behind the scenes, the plugin uses Solr, which has
				whole books dedicated to it. In this case, the fuzzy searching was enabled by this part of the configuration:
			</p>
			<pre>
  &lt;filter class="....PhoneticFilterFactory" encoder="Soundex" inject="false"/&gt;
			</pre>
			<p>
				What if we search for "Gonzo"? Try it. There are several different theories about what Gonzo is
				(anteater? turkey? ...), but whichever one you decide is right for your database, you want a set
				of fields that go together: If you think he's an anteater, then the genus needs to be "Myrmecophaga",
				obviously. The UI helps with this by presenting your choices in a pull-down. One you have one you like,
				you can tweak it by clicking "Edit". This grouping of fields is controlled by this part of the configuration:
			</p>
			<pre>
  &lt;item key='species'&gt;
    &lt;item&gt;common&lt;/item&gt;
    &lt;item&gt;genus&lt;/item&gt;
    &lt;item&gt;species&lt;/item&gt;
    &lt;item&gt;subspecies&lt;/item&gt;
  &lt;/item&gt;
			</pre>
			<p>
				This grouping is useful in a number of situations where values always belong together:
				taxonomic hierarchies; geographic hierarchies; first-name/last-name; latitute/longitude; dates.
			</p>
			<p>
				It is always possible that you haven't given enough information to uniquely identify an instance.
				For example, enter "Human" under species. The other fields of the scientific name get filled in
				correctly, but note that the pull down includes three values: "Dr. Bunsen Honeydew", "Bunsen" (the short form), 
				and "Beaker" (a different character entirely).
			</p>
			<p>
				The point is that the software tries its best with the information it has, and when there are choices,
				the user needs to make a choice, if the default isn't the best. When information from multiple
				rows is combined, in your mind it might be because of a multi-valued field (the case with Gonzo),
				or it might be that the query was underspecified (the case with Human), but this is not a distinction
				the software can make: It's up to the user.
			</p>
		</blurb-html>
		
		<!--
			Mapping from query parameters to solr fields.
		-->
		<q-solr>
			<item key='muppet'>muppet</item>
			<item key='species'>species</item>
		</q-solr>
	
		<!--
			This defines the fields available in the front end demo.
			The "key" will be used as the HTML "name" attribute on the inputs.
			"label" will be the human readable label beside it.
			"q-name" is the mapping of demo inputs to query terms.
			"a-name" is the mapping of the query response from solr to input fields.
		-->
		<input-fields>
			<item key='muppet'     label='muppet'     q-name='muppet'  a-name='muppet' default='Kermit'/>
			<item key='common'     label='common'     q-name='species' a-name='common'/>
			<item key='genus'      label='genus'      q-name='species' a-name='genus'/>
			<item key='species'    label='species'    q-name='species' a-name='species'/>
			<item key='subspecies' label='subspecies' q-name='species' a-name='subspecies'/>
		</input-fields>

	</front-end-configuration>

</configuration>
END_CONFIG
