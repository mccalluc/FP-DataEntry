(function(){
	var div_id = 'fp-bookmarklet-div';
	
	if (document.getElementById(div_id)) {
		document.getElementById(div_id).remove();
	}
	
	var names = [];
	var clean_names_set = {};
	var MAX_FIELDS = 20;
	
	function populate_names(els) {
		var name;
		var clean_name;
		for (var i = 0; i < els.length && names.length < MAX_FIELDS; i++) {
			name = els[i].name;
			clean_name = clean(name);
			if (name && !clean_names_set[clean_name]) {
				names.push(els[i].name);
			}
			clean_names_set[name] = 1;
		}
	}
	
	function clean(name) {
		return name.replace(/\W/g,'_');
	}
	
	populate_names(document.getElementsByTagName("input"));
	populate_names(document.getElementsByTagName("textarea"));
	
	if (names.length >= MAX_FIELDS) {
		alert('Stopped at '+MAX_FIELDS+' fields, just to keep\nthe demo script to a reasonable size.');
	}
	
	if (names.length == 0) {
		alert('No "input" or "textarea" elements found on page.');
	} else {
		var host = location.host.replace(/[^\w]/ig,'-');
		var script_name = 'config-files-' + host + '.sh';
		
		var data_suffix = ' example';
		var clean_names = names.map(function(current,index,array){
			return clean(current);
		});
		var input_file_header = clean_names.join('|');
		var input_file_data = clean_names.map(function(current,index,array){
			return current + data_suffix;
		}).join('|');
		var solr_fields = clean_names.map(function(current,index,array){
			return "<field name='"+current+"' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>";
		}).join('\n\t\t\t\t\t\t');
		var tuples_map = clean_names.map(function(current,index,array){
			return "<item key='"+current+"'><item>"+current+"</item></item>";
		}).join('\n\t\t\t');
		var q_solr = clean_names.map(function(current,index,array){
			return "<item key='"+current+"'>"+current+"</item>";
		}).join('\n\t\t\t');
		var input_fields = names.map(function(current,index,array){
			var clean_name = clean(current);
			return "<item key='"+current+"' label='"+clean_name+"' q-name='"+clean_name+"' a-name='"+clean_name+"'/>";
		}).join('\n\t\t\t');
		var default_value = names[names.length-1] + data_suffix;
		input_fields = input_fields.replace(/\/>$/," default='"+default_value+"'/>");
	
		var bash = 
'cat >$INPUT <<END_INPUT \n\
'+input_file_header+'\n\
'+input_file_data+'\n\
END_INPUT\n\
\n\
cat >\$CONFIG <<END_CONFIG \n\
<configuration> \n\
\n\
	<solr-configuration>\n\
		<solr-directory>$SOLR_DIR</solr-directory>\n\
		<solr-files>\n\
			<item key="schema.xml">\n\
				<!-- For more information, see http://wiki.apache.org/solr/SchemaXml -->\n\
				<schema name="example" version="1.5">\n\
					<fields>\n\
						<field name="_fp_internal_id" type="fpID" indexed="true" stored="false" required="true" multiValued="false"/>\n\
						'+solr_fields+'\n\
					</fields>\n\
					<uniqueKey>_fp_internal_id</uniqueKey>\n\
					<types>\n\
						<fieldType name="fpMinimallySearchable" class="solr.TextField" sortMissingLast="true" omitNorms="true">\n\
							<!-- Remember that everything is wrapped in a JSON list, so some kind of parsing is essential, even if you only want exact matches. -->\n\
							<analyzer>\n\
								<tokenizer class="solr.StandardTokenizerFactory"/>\n\
								<filter class="solr.LowerCaseFilterFactory"/>\n\
							</analyzer>\n\
						</fieldType>\n\
						<fieldType name="fpID" class="solr.StrField" sortMissingLast="true"/>\n\
					</types>\n\
				</schema>\n\
			</item>\n\
			<item key="solrconfig.xml">\n\
				<!-- For more information, see http://wiki.apache.org/solr/SolrConfigXml. -->\n\
				<config>\n\
					<luceneMatchVersion>4.5</luceneMatchVersion>\n\
					<requestHandler name="/select" class="solr.SearchHandler">\n\
						<lst name="defaults">\n\
							<int name="rows">10</int>\n\
							<str name="df">text</str>\n\
						</lst>\n\
					</requestHandler>\n\
					<requestHandler name="/update" class="solr.UpdateRequestHandler"/>\n\
				</config> \n\
			</item>\n\
		</solr-files>\n\
	</solr-configuration>\n\
	\n\
	<server-configuration>\n\
		<port>$PORT</port><!-- Only used by Jetty; Ignored by Tomcat. -->\n\
	</server-configuration>\n\
	\n\
	<term-tuple-configuration>\n\
		<!--\n\
			This controls the ordering and grouping of fields in your suggested matches.\n\
			The outer items ("tuples") must correspond to fields in your solr index.\n\
			The inner items ("terms") must correspond to fields in your data entry application.\n\
		-->\n\
		<tuples-map>\n\
			'+tuples_map+'\n\
		</tuples-map>\n\
	</term-tuple-configuration>\n\
	\n\
	<ingester-configuration>\n\
		<!-- Replace this with the full path of the file you want to read. -->\n\
		<ingest-file>$INPUT</ingest-file>\n\
		<delimiter>|</delimiter>\n\
	</ingester-configuration>\n\
	\n\
	<front-end-configuration>\n\
	\n\
		<title-html>DEMO: '+host+'</title-html>\n\
		<blurb-html>$BLURB</blurb-html>\n\
		\n\
		<!--\n\
			Mapping from query parameters to solr fields.\n\
		-->\n\
		<q-solr>\n\
			'+q_solr+'\n\
		</q-solr>\n\
		\n\
		<!--\n\
		The "key" will usually be the name attribute of the input, but if a "selector-function" is given below,\n\
		it can be any string the selector-function can use to identify a unique input.\n\
		\n\
		The "label" is the human readable label beside it. (The name is used if unspecified.)\n\
		\n\
		At query time, inputs are mapped to query parameters with "q-name".\n\
		\n\
		When the results return, they are mapped back to inputs with "a-name".\n\
		-->\n\
		<input-fields>\n\
			'+input_fields+'\n\
		</input-fields>\n\
		\n\
	</front-end-configuration>\n\
	\n\
</configuration>\n\
END_CONFIG\n\
';
		
		var style = document.createElement('style');
		style.innerHTML = 
			// This ends up applying rules on elements where they don't make sense, 
			// but I don't want to bother with every "#id el1", "#id el2", "id el3"... combo.
			// TODO: button style still finicky.
			'#'+div_id+' *:not(button) { \
				margin: 0; \
				padding: 0; \
				border: 0; \
				background: #FFF; \
				color: #000; \
				font: inherit; \
				vertical-align: baseline; \
				font-family: sans-serif; \
				font-size: 10pt; \
				text-align: left; \
				overflow: visible; } \
			#'+div_id+' pre, #'+div_id+' code { \
				font-family: monospace; \
				font-weight: bold; } \
			#'+div_id+' pre { \
				white-space: pre; } \
			#'+div_id+' button { \
				color: #000; \
				-webkit-border-radius: 0.5em; \
				-moz-border-radius: 0.5em; \
				border-radius: 0.5em; \
				background-image: -webkit-gradient( \
					linear, \
					left bottom, \
					left top, \
					color-stop(0.16, rgb(207,207,207)), \
					color-stop(0.79, rgb(252,252,252)) \
				); \
				background-image: -moz-linear-gradient( \
					center bottom, \
					rgb(207,207,207) 16%, \
					rgb(252,252,252) 79% \
				); \
				padding: 0 0.2em; \
				border:1px solid #999;} \
			#'+div_id+' button:hover { \
				cursor: pointer; }';
			// http://stackoverflow.com/questions/6696432
		
		document.body.appendChild(style);
		
		var div = document.createElement("div");
		document.body.appendChild(div);
		div.setAttribute("id", div_id);
		div.setAttribute("style","width:50%; height:80%; position:fixed; bottom:5%; right:5%; z-index:9999; border:1px solid black; background:#FFF; padding:0.5em; overflow:scroll;");
		
		div.innerHTML = '<p>Copy the code below and save it as "<code>'+script_name+'</code>". '
			+ 'Then, from your checkout of FP-DataEntry, run it: "<code>bash demos/run-jetty.sh -f ~/'+script_name+'</code>" '
			+ '<button onclick="document.getElementById(\'fp-bookmarklet-div\').remove()">close</button></p><br/>'
			+ '<pre>'+bash.replace(/</g, '&lt;').replace(/>/g, '&gt;')+'</pre>';
	}
})();