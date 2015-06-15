/*
FP-DataEntry
(C) 2014 President and Fellows of Harvard College

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.filteredpush.dataentry.backend.solr;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.filteredpush.dataentry.Constants;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.backend.TermRecord;
import org.filteredpush.dataentry.backend.TupleSingleRecord;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.filteredpush.dataentry.enums.QParameter;
import org.filteredpush.dataentry.enums.Term;
import org.filteredpush.dataentry.enums.Tuple;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.google.common.collect.ImmutableMap;

public abstract class SolrAbstractTest {

	protected static SolrServer server;

	protected static final String RECORD_NUMBER = "recordNumber";
	protected static final String TAXON_INDEX = "taxon_index";
	protected static int row = 0;
	protected static String dataSetId = java.util.UUID.randomUUID().toString();

	/*------------------
	   init and cleanup
	 -------------------*/

	@BeforeClass
	public static void init() throws IOException {
		QParameter.init(Constants.Q_SOLR_VALUE);
		Term.init(Constants.TERMS_VALUE);
		Tuple.init(Constants.TUPLES_MAP_VALUE);
		File solrDir = Utils.createTempDir("solr");
		Map<String,String> solrFiles = ImmutableMap.of( //
				"schema.xml", Utils.readResource("schemas/schema.xml"), //
				"solrconfig.xml", Utils.readResource("solrconfig.xml"), //
				"stopwords_en.txt", Utils.readResource("stopwords_en.txt"));
		SolrInstaller.install(solrDir, solrFiles);
		server = Utils.startSolr(solrDir);
	}

	@AfterClass
	public static void quit() {
		server.shutdown();
		EnumUtils.clearEnums();
	}

	@Before
	public void clear()  {
		try {
			server.deleteByQuery("*:*");
			server.commit();
		} catch (SolrServerException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	/*---------------
	   static utils
	 ----------------*/
	
	protected static SolrInputDocument getMockDoc() {
		row++;
		TermRecord termRecord = new TermRecord();
		for (Term term : Term.values()) {
			termRecord.put(term, "fake " + term);
		}
		return TupleSingleRecord.fromTermRecord(termRecord).asSolr(dataSetId, row);
	}

	protected static void resetValue(SolrInputDocument doc, Tuple field, Object... values) {
		String fieldName = field.toString();
		doc.removeField(fieldName);
		doc.addField(fieldName, values);
	}

	protected static SolrDocumentList justResults(String query) {
		return Utils.query(server, query).getResults();
	}
	
//	protected static Map<String,List<String>> justFacets(String query) {
//		return Utils.query(server, query).getFacetResults();
//	}
	
	protected static void assertTuple(String search, List<String> expectMatch, List<String> expectNoMatch, Tuple tuple) {
		SolrInputDocument doc = getMockDoc();
		resetValue(doc, tuple, search);

		try {
			server.add(doc);
			server.commit();
		
			for (final String match : expectMatch) {
				assertThat(Utils.query(server, query(match)).getResults())
					.overridingErrorMessage("Expected '"+match+"' to match").isNotEmpty();
			}
			for (String noMatch : expectNoMatch) {
				assertThat(Utils.query(server, query(noMatch)).getResults())
					.overridingErrorMessage("Expected '"+noMatch+"' not to match").isEmpty();
			}
		} catch (SolrServerException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	@SuppressWarnings("serial")
	protected static Map<String,String> query(final String term) {
		return new HashMap<String,String>(){{
			put(Term.valueOf("recordedBy").toString(), term);
		}};
	}
	
	protected static void addCommit(SolrInputDocument doc) {
		try {
			server.add(doc);
			server.commit();
		} catch (SolrServerException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

}
