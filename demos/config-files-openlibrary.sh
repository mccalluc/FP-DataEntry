cat >$CONFIG <<END_CONFIG
<configuration>
	
	<!-- The <*-configuration> groupings are purely cosmetic: Only the tags that directly contain data matter. -->
	
	<back-end-configuration>
		<query-engine-class>org.filteredpush.dataentry.extras.OpenLibraryQueryEngine</query-engine-class>
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
			<item key="lccn">
				<item>lccn</item>
			</item>
			<item key="isbn_10">
				<item>isbn_10</item>
			</item>
			<item key="oclc">
				<item>oclc</item>
			</item>
			<item key="dewey_decimal_class">
				<item>dewey_decimal_class</item>
			</item>
			<item key="lc_classifications">
				<item>lc_classifications</item>
			</item>
			<item key="title">
				<item>title</item>
			</item>
			<item key="by_statement">
				<item>by_statement</item>
			</item>
			<item key="authors">
				<item>authors</item>
			</item>
			<item key="notes">
				<item>notes</item>
			</item>
			<item key="number_of_pages">
				<item>number_of_pages</item>
			</item>
			<item key="pagination">
				<item>pagination</item>
			</item>
			<item key="subject_places">
				<item>subject_places</item>
			</item>
			<item key="subjects">
				<item>subjects</item>
			</item>
			<item key="subject_people">
				<item>subject_people</item>
			</item>
			<item key="subject_times">
				<item>subject_times</item>
			</item>
			<item key="publishers">
				<item>publishers</item>
			</item>
			<item key="publish_places">
				<item>publish_places</item>
			</item>
			<item key="publish_date">
				<item>publish_date</item>
			</item>
		</tuples-map>
	</term-tuple-configuration>

	<front-end-configuration>
	
		<title-html>DEMO: OpenLibrary</title-html>
		<blurb-html>
			<p>
				This demonstrates how the FP-DataEntry plugin could use the 
				<a href='https://openlibrary.org/dev/docs/api/books'>OpenLibrary API</a>.
				The end result is a bookmarklet which can fill in all the fields in a form when 
				the ISBN is provided. If you would find this useful for production work,
				you could configure it by modifying the JS on this page, or you might run your own server:
			</p>
			<pre>
$ svn checkout https://svn.code.sf.net/p/filteredpush/svn/trunk/FP-DataEntry
$ cd FP-DataEntry
$ bash demos/run-jetty.sh -f demos/config-files-openlibrary.sh
			</pre>
		</blurb-html>
		
		<!--
			Mapping from query parameters to solr fields.
			Over time, either the solr index or the client application could change:
			this allows them to be decoupled.
			TODO: It feels like, either this is gratuitous, or there should be something similar for a-name. Not sure which.
		-->
		<q-solr>
			<item key="ISBN">isbn_10</item>
		</q-solr>
	
		<!--
			This defines the fields available in the front end demo.
			The "key" will used as the HTML "name" attribute on the inputs.
			"label" will be the human readable label beside it.
			"q-name" is the mapping of demo inputs to query terms. (You might want more than one field mapped to a single q-name.)
			"a-name" is the mapping of the query response from solr to input fields.
		-->
		<input-fields>
			<item key="lccn" label="lccn" a-name="lccn"/>
			<item key="isbn_10" label="isbn_10" q-name="ISBN" a-name="isbn_10" default="0451526538"/>
			<item key="oclc" label="oclc" a-name="oclc"/>
			<item key="dewey_decimal_class" label="dewey_decimal_class" a-name="dewey_decimal_class"/>
			<item key="lc_classifications" label="lc_classifications" a-name="lc_classifications"/>
			<item key="title" label="title" a-name="title"/>
			<item key="by_statement" label="by_statement" a-name="by_statement"/>
			<item key="authors" label="authors" a-name="authors"/>
			<item key="notes" label="notes" a-name="notes"/>
			<item key="number_of_pages" label="number_of_pages" a-name="number_of_pages"/>
			<item key="pagination" label="pagination" a-name="pagination"/>
			<item key="subject_places" label="subject_places" a-name="subject_places"/>
			<item key="subjects" label="subjects" a-name="subjects"/>
			<item key="subject_people" label="subject_people" a-name="subject_people"/>
			<item key="subject_times" label="subject_times" a-name="subject_times"/>
			<item key="publishers" label="publishers" a-name="publishers"/>
			<item key="publish_places" label="publish_places" a-name="publish_places"/>
			<item key="publish_date" label="publish_date" a-name="publish_date"/>
		</input-fields>

	</front-end-configuration>

</configuration>
END_CONFIG
