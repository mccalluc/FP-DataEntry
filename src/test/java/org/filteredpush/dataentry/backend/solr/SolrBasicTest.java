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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.filteredpush.dataentry.Json;
import org.filteredpush.dataentry.backend.GenericQueryEngine;
import org.filteredpush.dataentry.backend.TupleMultiRecord;
import org.filteredpush.dataentry.enums.Term;
import org.filteredpush.dataentry.enums.Tuple;
import org.junit.Test;

public class SolrBasicTest extends SolrAbstractTest {

	private static void assertSmith() {
		String weirdS = "\u015B";
		assertThat(justResults(Term.valueOf("recordedBy") + ":smith")).isNotEmpty();
		assertThat(justResults(Term.valueOf("recordedBy") + ":" + weirdS + "mith")).isNotEmpty();
		assertThat(justResults(Term.valueOf("recordedBy") + ":Smith-Schmith")).isNotEmpty();

		assertThat(justResults(Term.valueOf("recordedBy") + ":Zmith")).isEmpty();
		assertThat(justResults(Term.valueOf("recordedBy") + ":smi")).isEmpty();
		assertThat(justResults(Term.valueOf("recordedBy") + ":a.b.")).isEmpty();
	}
	
	@SuppressWarnings("serial")
	private static void assertGeography(SolrServer server, final String search, int matches) {
		GenericQueryEngine queryEngine = new EmbeddedSolrQueryEngine(server);
		Map<String,String[]> lcSearch = new HashMap<String,String[]>(){{
			this.put("geography", new String[]{search});
		}};
		assertThat(queryEngine.query(lcSearch).size()).isEqualTo(matches);
		Map<String,String[]> ucSearch = new HashMap<String,String[]>(){{
			this.put("geography", new String[]{search.toUpperCase()});
		}};
		List<TupleMultiRecord> results = queryEngine.query(ucSearch);
		assertThat(results.size()).isEqualTo(matches);
		assertThat(Json.toEscaped(results))
			.matches("^(\\[\\{\"[^\"]+\":\\[\\[\"[^\"]+\".*)|\\[\\]")
			.as("bad JSON serialization.");
	}
	
	@Test
	public void testCollectorNumber() {
		SolrInputDocument doc = getMockDoc();
		String orig1 = " AB C1-23%^xyz&$";
		String orig2 = "can-be-multi-valued";
		resetValue(doc, Tuple.valueOf("recordNumber"), orig1, orig2);
		addCommit(doc);

		assertThat(
				justResults(RECORD_NUMBER + ":abc123xyz").get(0).getFieldValue(RECORD_NUMBER)
		).isEqualTo(
				Arrays.asList(orig1, orig2)
		);

		assertThat(
				justResults(RECORD_NUMBER + ":\"!@#$%A BC_1\t23   XYZ\"").get(0).getFieldValue(RECORD_NUMBER)
		).isEqualTo(
				Arrays.asList(orig1, orig2)
		);

		// make sure we're not stemming by mistake:
		assertThat(justResults(RECORD_NUMBER + ":abc123")).isEmpty();
	}

	@Test
	public void testCollectorNameNoInitials() {
		SolrInputDocument doc = getMockDoc();
		String weirdS = "\u015B";
		String orig = weirdS + "mith-schmith, Zmith, et al.";
		resetValue(doc, Tuple.valueOf("recordedBy"), orig);
		addCommit(doc);

		assertSmith();
	}

	@Test
	public void testCollectorNameWithInitials() {
		SolrInputDocument doc = getMockDoc();
		String weirdS = "\u015B";
		String orig = "A.B. " + weirdS + "mith-schmith, Zmith, et al.";
		resetValue(doc, Tuple.valueOf("recordedBy"), orig);
		addCommit(doc);

		assertSmith();
	}
	
	@Test
	public void testCollectorNameWithFirstName() {
		SolrInputDocument doc = getMockDoc();
		String weirdS = "\u015B";
		String orig = "Anne " + weirdS + "mith-schmith, Zmith, et al.";
		resetValue(doc, Tuple.valueOf("recordedBy"), orig);
		addCommit(doc);

		assertSmith();
	}

	@Test
	public void testTaxonomy() {
		List<Tuple> taxa = Arrays.asList(Tuple.valueOf("kingdom"), //
				Tuple.valueOf("phylum"), //
				Tuple.valueOf("class"), //
				Tuple.valueOf("order"), //
				Tuple.valueOf("family"), //
				Tuple.valueOf("scientificName"));

		final String TARGET = "searchtarget";

		for (Tuple taxon : taxa) {
			assertThat(justResults(TAXON_INDEX + ":" + TARGET)).isEmpty();

			SolrInputDocument doc = getMockDoc();
			resetValue(doc, taxon, TARGET);
			addCommit(doc);

			assertThat(justResults(TAXON_INDEX + ":" + TARGET)).isNotEmpty();

			clear();
		}
	}

	@Test
	public void testDate() {
		String y = "1977";
		String ym = y + "-03";
		String ymd = ym + "-08";
		String field = "eventDate";

		for (String dateFragment : Arrays.asList(y, ym, ymd)) {
			SolrInputDocument doc = getMockDoc();
			resetValue(doc, Tuple.valueOf(field), dateFragment);
			addCommit(doc);
		}

		assertThat(justResults(field + ":" + y).size()).isEqualTo(3);
		assertThat(justResults(field + ":" + ym).size()).isEqualTo(2);
		assertThat(justResults(field + ":" + ymd).size()).isEqualTo(1);
	}

	@Test
	public void testGeography() {
		String a = "Atlanta";
		String ab = a + ", Georgia";
		String abc = ab + " (south of the airport)";

		List<String> localities = Arrays.asList(a, ab, abc);
		String eAcute = "\u00C9";
		List<String> countries = Arrays.asList( // Variation in translation is
												// more typical of
												// state/province, but I haven't
												// implemented that yet.
				"United States of America", "Franco-Deutschland", eAcute + "tats-Unis von Amerika");

		for (String locality : localities) {
			for (String country : countries) {
				SolrInputDocument doc = getMockDoc();
				resetValue(doc, Tuple.valueOf("verbatimLocality"), Json.toUTF8(Arrays.asList(locality)));
				resetValue(doc, Tuple.valueOf("country"), Json.toUTF8(Arrays.asList(country)));
				addCommit(doc);
			}
		}

		// If we do more than 9 combinations, then we hit the default results size limit.
		assertGeography(server, "Atlanta", localities.size() * countries.size());
		assertGeography(server, "Atlanta, Georgia", (localities.size() - 1) * countries.size());
		assertGeography(server, "Atlanta,Georgia", (localities.size() - 1) * countries.size());
		assertGeography(server, "Georgia", (localities.size() - 1) * countries.size());
		assertGeography(server, "South", countries.size());
		assertGeography(server, "Sud", 0);
		assertGeography(server, "America", localities.size());
		assertGeography(server, "Amerika", localities.size());
		assertGeography(server, "of", 0);
		assertGeography(server, "of the", 0);
		assertGeography(server, "d", 0);
		assertGeography(server, "von", 3); // Just English stopwords right now. Might reconsider. 
		assertGeography(server, "Etats", localities.size());
		assertGeography(server, "Deutschland", localities.size());
		assertGeography(server, "Germany", 0);
	}

	@Test
	public void testGbifDwcRecordQuerySolr() {
		@SuppressWarnings("serial")
		Map<String,String[]> search = new HashMap<String,String[]>(){{
			this.put("name", new String[]{"name"});
			this.put("number", new String[]{"number"});
			this.put("taxon", new String[]{"taxon"});
			this.put("geography", new String[]{"geography"});
			this.put("date", new String[]{"date"});
			this.put("exsiccate_title", new String[]{"exsiccate_title"});
			this.put("exsiccate_number", new String[]{"exsiccate_number"});
			
		}};
		new EmbeddedSolrQueryEngine(server).query(search);
	}

}
