# State quarters data from Wikipedia. Note the non-ASCII characters in Hawaii's motto.
cat >$INPUT <<END_INPUT
state|issued|statehood|image|text|engraver
Delaware|January 1, 1999|December 7, 1787|Caesar Rodney on horseback|Captions: "The First State", "Caesar Rodney"|William Cousins
Pennsylvania|March 8, 1999|December 12, 1787|Commonwealth statue, state outline, keystone|Caption: "Virtue, Liberty, Independence"|John Mercanti
New Jersey|May 17, 1999|December 18, 1787|Washington Crossing the Delaware, which includes George Washington (standing) and James Monroe (holding the flag)|Caption: "Crossroads of the Revolution"|Alfred Maletsky
Georgia|July 19, 1999|January 2, 1788|Peach, live oak (state tree) sprigs, state outline|Banner with text: "Wisdom, Justice, Moderation" (the state motto)|T. James Ferrell
Connecticut|October 12, 1999|January 9, 1788|Charter Oak|Caption: "The Charter Oak"|T. James Ferrell
Massachusetts|January 3, 2000|February 6, 1788|The Minuteman statue, state outline|Caption: "The Bay State"|Thomas D. Rodgers
Maryland|March 13, 2000|April 28, 1788|Dome of the Maryland State House, white oak (state tree) clusters|Caption: "The Old Line State"|Thomas D. Rodgers
South Carolina|May 22, 2000|May 23, 1788|Carolina wren (state bird), yellow jessamine (state flower), cabbage palmetto (state tree), state outline|Caption: The Palmetto State"|Thomas D. Rodgers
New Hampshire|August 7, 2000|June 21, 1788|Old Man of the Mountain, nine stars|Captions: "Old Man of the Mountain", "Live Free or Die"|William Cousins
Virginia|October 16, 2000|June 25, 1788|Ships Susan Constant, Godspeed, Discovery|Captions: "Jamestown, 1607–2007", "Quadricentennial"|Edgar Z. Steever
New York|January 2, 2001|July 26, 1788|Statue of Liberty, 11 stars, state outline with line tracing Hudson River and Erie Canal|Caption: "Gateway to Freedom"|Alfred Maletsky
North Carolina|March 12, 2001|November 21, 1789|Wright Flyer, John T. Daniels's iconic photo of the Wright brothers|Caption: "First Flight"|John Mercanti
Rhode Island|May 21, 2001|May 29, 1790|America's Cup yacht Reliance on Narragansett Bay, Pell Bridge|Caption: "The Ocean State"|Thomas D. Rodgers
Vermont|August 6, 2001|March 4, 1791|Maple trees with sap buckets, Camel's Hump Mountain|Caption: "Freedom and Unity"|T. James Ferrell
Kentucky|October 15, 2001|June 1, 1792|Thoroughbred racehorse behind fence, Bardstown mansion, Federal Hill|Caption: "My Old Kentucky Home"|T. James Ferrell
Tennessee|January 2, 2002|June 1, 1796|Fiddle, trumpet, guitar, musical score, three stars|Banner with text: "Musical Heritage"|Donna Weaver
Ohio|March 18, 2002|March 1, 1803|Wright Flyer (built by the Wright Brothers who were born in Dayton, Ohio); astronaut (Neil Armstrong was a native of Wapakoneta, Ohio); state outline|Caption: "Birthplace of Aviation Pioneers"|Donna Weaver
Louisiana|May 30, 2002|April 30, 1812|Brown pelican (state bird); trumpet with musical notes, outline of Louisiana Purchase on map of U.S.|Caption: "Louisiana Purchase"|John Mercanti
Indiana|August 8, 2002|December 11, 1816|IndyCar, state outline, 19 stars|Caption: "Crossroads of America"|Donna Weaver
Mississippi|October 15, 2002|December 10, 1817|Two magnolia blossoms (state flower)|Caption: "The Magnolia State"|Donna Weaver
Illinois|January 2, 2003|December 3, 1818|Young Abraham Lincoln; farm scene; Chicago skyline; state outline; 21 stars, 11 on left edge and 10 on right|Captions: "Land of Lincoln" "21st state/century"|Donna Weaver
Alabama|March 17, 2003|December 14, 1819|Helen Keller, seated, longleaf pine (state tree) branch, magnolia blossoms|Banner with text: "Spirit of Courage" Caption: "Helen Keller" in standard print and Braille|Norman E. Nemeth
Maine|June 2, 2003|March 15, 1820|Pemaquid Point Lighthouse; the schooner Victory Chimes at sea||Donna Weaver
Missouri|August 4, 2003|August 10, 1821|Gateway Arch, Lewis and Clark and York returning down Missouri River|Caption: "Corps of Discovery 1804–2004"|Alfred Maletsky
Arkansas|October 20, 2003|June 15, 1836|Diamond (state gem), rice stalks, mallard flying above a lake||John Mercanti
Michigan|January 26, 2004|January 26, 1837|State outline, outline of Great Lakes system|Caption: "Great Lakes State"|Donna Weaver
Florida|March 29, 2004|March 3, 1845|Spanish galleon, cabbage palmettos (state tree), Space Shuttle|Caption: "Gateway to Discovery"|T. James Ferrell
Texas|June 1, 2004|December 29, 1845|State outline, star, lariat|Caption: "The Lone Star State"|Norman E. Nemeth
Iowa|August 30, 2004|December 28, 1846|Schoolhouse, teacher and students planting a tree|Captions: "Foundation in Education", "Grant Wood"|John Mercanti
Wisconsin|October 25, 2004|May 29, 1848|Head of a cow, round of cheese and ear of corn (state grain)|Banner with text: "Forward"|Alfred Maletsky
California|January 31, 2005|September 9, 1850|John Muir, California condor, Half Dome|Captions: "John Muir," "Yosemite Valley"|Don Everhart
Minnesota|April 4, 2005|May 11, 1858|Common loon (state bird), fishing, state map|Caption: "Land of 10,000 Lakes"|Charles L. Vickers
Oregon|June 6, 2005|February 14, 1859|Crater Lake National Park|Caption: "Crater Lake"|Donna Weaver
Kansas|August 29, 2005|January 29, 1861|American bison (state mammal), sunflowers (state flower)||Norman E. Nemeth
West Virginia|October 14, 2005|June 20, 1863|New River Gorge Bridge|Caption: "New River Gorge"|John Mercanti
Nevada|January 31, 2006|October 31, 1864|Mustangs, mountains, rising sun, sagebrush (state flower)|Banner with text: "The Silver State"|Don Everhart
Nebraska|April 3, 2006|March 1, 1867|Chimney Rock, covered wagon|Caption: "Chimney Rock"|Charles L. Vickers
Colorado|June 14, 2006|August 1, 1876|Longs Peak|Banner with text: "Colorful Colorado"|Norman E. Nemeth
North Dakota|August 28, 2006|November 2, 1889|American bison, badlands||Donna Weaver
South Dakota|November 6, 2006|November 2, 1889|Mount Rushmore, ring-necked pheasant (state bird), wheat (state grass)||John Mercanti
Montana|January 29, 2007|November 8, 1889|American bison skull in the center with mountains and the Missouri River in the background.|Caption: "Big Sky Country"|Don Everhart
Washington|April 11, 2007|November 11, 1889|Salmon leaping in front of Mount Rainier|Caption: "The Evergreen State"|Charles L. Vickers
Idaho|June 5, 2007|July 3, 1890|Peregrine falcon, state outline|Caption: "Esto Perpetua"|Don Everhart
Wyoming|September 4, 2007|July 10, 1890|Bucking horse and rider|Caption: "The Equality State"|Norman E. Nemeth
Utah|November 5, 2007|January 4, 1896|Golden Spike and the completion of the Transcontinental Railroad|Caption: "Crossroads of the West"|Joseph F. Menna
Oklahoma|January 28, 2008|November 16, 1907|Scissor-tailed flycatcher (state bird), with Indian blankets (state wildflower) in background||Phebe Hemphill
New Mexico|April 7, 2008|January 6, 1912|State outline, Zia Sun Symbol from flag|Caption: "Land of Enchantment"|Don Everhart
Arizona|June 2, 2008|February 14, 1912|Grand Canyon, saguaro closeup.|Banner with text: "Grand Canyon State"|Joseph F. Menna
Alaska|August 25, 2008|January 3, 1959|Grizzly bear with salmon (state fish) and North Star|Caption: "The Great Land"|Charles L. Vickers
Hawaii|November 3, 2008|August 21, 1959|Statue of Kamehameha I with state outline and motto|Caption: "Ua Mau ke Ea o ka ʻĀina i ka Pono"|Don Everhart
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
						<field name="_fp_internal_id"            type="fpID" indexed="true" stored="true" required="true"  multiValued="false"/>
						<field name="state"     type="fpMinimallySearchable" indexed="true" stored="true" required="false" multiValued="true"/>
						<field name="issued"    type="fpMinimallySearchable" indexed="true" stored="true" required="false" multiValued="true"/>
						<field name="statehood" type="fpMinimallySearchable" indexed="true" stored="true" required="false" multiValued="true"/>
						<field name="image"     type="fpMinimallySearchable" indexed="true" stored="true" required="false" multiValued="true"/>
						<field name="text"      type="fpMinimallySearchable" indexed="true" stored="true" required="false" multiValued="true"/>
						<field name="engraver"  type="fpMinimallySearchable" indexed="true" stored="true" required="false" multiValued="true"/>
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
			<item key="state">
				<item>state</item>
			</item>
			<item key="issued">
				<item>issued</item>
			</item>
			<item key="statehood">
				<item>statehood</item>
			</item>
			<item key="image">
				<item>image</item>
			</item>
			<item key="text">
				<item>text</item>
			</item>
			<item key="engraver">
				<item>engraver</item>
			</item>
		</tuples-map>
	</term-tuple-configuration>

	<ingester-configuration>
		<!-- Replace this with the full path of the file you want to read. -->
		<ingest-file>$INPUT</ingest-file>
		<delimiter>|</delimiter>
	</ingester-configuration>

	<front-end-configuration>
	
		<title-html>DEMO: State Quarters</title-html>
		<blurb-html>$BLURB</blurb-html>
		
		<!--
			Mapping from query parameters to solr fields.
		-->
		<q-solr>
			<item key="state">state</item>
		</q-solr>
	
		<!--
			This defines the fields available in the front end demo.
			The "key" will used as the HTML "name" attribute on the inputs.
			"label" will be the human readable label beside it.
			"q-name" is the mapping of demo inputs to query terms. (You might want more than one field mapped to a single q-name.)
			"a-name" is the mapping of the query response from solr to input fields.
		-->
		<input-fields>
			<item key="state"     label="state"     q-name="state"     a-name="state"     default="Hawaii"/>
			<item key="issued"    label="issued"                       a-name="issued"/>
			<item key="statehood" label="statehood"                    a-name="statehood"/>
			<item key="image"     label="image"                        a-name="image"/>
			<item key="text"      label="text"                         a-name="text"/>
			<item key="engraver"  label="engraver"                     a-name="engraver"/>
		</input-fields>
		
		<!--
			If specified, rather than automatically filling in the parent form,
			an explicit button click is required.
		-->
		<require-confirmation>true</require-confirmation>

	</front-end-configuration>

</configuration>
END_CONFIG
