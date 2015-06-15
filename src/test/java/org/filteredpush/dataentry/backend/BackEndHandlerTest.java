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
package org.filteredpush.dataentry.backend;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.eclipse.jetty.server.Server;
import org.filteredpush.dataentry.Constants;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.backend.solr.SolrIndexer;
import org.filteredpush.dataentry.backend.solr.SolrInstaller;
import org.filteredpush.dataentry.backend.solr.UpdatingSolrServer;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.filteredpush.dataentry.enums.QParameter;
import org.filteredpush.dataentry.enums.Term;
import org.filteredpush.dataentry.enums.Tuple;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

public class BackEndHandlerTest {

	private static Server server;
	private static int backEndPort;
	
	private static String get(String path) {
		try {
			String url = "http://localhost:"+backEndPort+path;
			return Request.Get(url).execute().returnContent().asString();
		} catch (ClientProtocolException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	@BeforeClass
	public static void init() throws IOException {
		QParameter.init(Constants.Q_SOLR_VALUE);
		Term.init(Constants.TERMS_VALUE);
		Tuple.init(Constants.TUPLES_MAP_VALUE);

		final File solrDir = Utils.createTempDir("solr-");
		
		// install:
		Map<String,String> solrFiles = ImmutableMap.of( //
				"schema.xml", Utils.readResource("schemas/schema.xml"), //
				"solrconfig.xml", Utils.readResource("solrconfig.xml"), //
				"stopwords_en.txt", Utils.readResource("stopwords_en.txt"));
		SolrInstaller.install(solrDir, solrFiles);

		// index:
		UpdatingSolrServer solrServer = new UpdatingSolrServer(Utils.startSolr(solrDir));
		SolrIndexer indexer = SolrIndexer.getInstance();
		FileRecordsForIndexAndUpdate records = new FileRecordsForIndexAndUpdate( //
				Utils.saveToDisk(Utils.readResource("tab-utf8-null.txt"), "demo-", ".txt"), //
				Charsets.UTF_8, EncodingCorrection.FIX_A_TILDE, "\t", null, "NULL", Constants.FIELD_MAP);
		String dataSetId = java.util.UUID.randomUUID().toString();
		indexer.index(records.forIndex(), solrServer, dataSetId);
		indexer.update(records.forUpdate(), solrServer, dataSetId);
		solrServer.shutdown();
		
		final EmbeddedSolrServer embeddedSolrServer = Utils.startSolr(solrDir);
		server = new Utils.PortPicker(){
			public Server pick(int port) {
				backEndPort = port;
				return BackEndHandler.createServer(new MockBackEndConfiguration(solrDir, backEndPort), embeddedSolrServer);
			}
		}.pickFrom(8081,8083,8085,8087,8089);
	}
	
	@AfterClass
	public static void quit() throws Exception {
		server.stop();
		server.destroy();
		EnumUtils.clearEnums();
	}
	
	// TODO: assert correct mime-types
	
	private static void assertMatch(String url) {
		String content = get(url);
		assertThat(content)
			.as("There should be a match for: "+url)
			.startsWith("[{");
	}

	private static void assertNoMatch(String url) {
		String content = get(url);
		assertThat(content)
			.as("There should be NO match for: "+url)
			.isEqualTo("[]");
	}
	
	private static void assert400(String url) {
		try {
			Response r = Request.Get("http://localhost:"+backEndPort+"?bad&params").execute();
			assertThat(r.returnResponse().getStatusLine().getStatusCode())
				.isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
			// TODO: Kind of a pain to also get content:
			// http://stackoverflow.com/questions/20864367/get-content-and-status-code-from-httpresponse
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	@Test
	public void testBadParams() {
		assert400("/?bad&params");
	}

	@Test
	public void testNoParams() {
		assert400("/");
	}
	
	@Test
	public void testCollectorQuery() {
		assertMatch("/?collector=santesson");
	}
	
	@Test
	public void testNumberQuery() {
		assertMatch("/?number=172");
		assertNoMatch("/?number=173");
	}
	
	@Test
	public void testTaxonQuery() {
		assertMatch("/?taxon=Plectocarpon");
	}
	
	@Test
	public void testGeographyQuery() {
		assertMatch("/?geography=sweden");
		assertNoMatch("/?geography=norway");
	}
	
	@Test
	public void testDateQuery() {
		assertMatch("/?date=1991-08");
	}
	
	@Test
	public void testExsiccateTitleQuery() {
		assertMatch("/?exsiccate_title=Special+HUH+Title+()");
		assertNoMatch("/?exsiccate_title=nope"); // TODO: maybe we should support substrings?
	}
	
	@Test
	public void testExsiccateNumberQuery() {
		assertMatch("/?exsiccate_number=172");
		assertNoMatch("/?exsiccate_number=173");
	}
	
	@Test
	public void testExsiccateTitleAndNumberQuery() {
		assertMatch("/?exsiccate_title=Special+HUH+Title+()&exsiccate_number=172");
	}
	
	@Test
	public void testRobotsTxt() {
		String content = get("/robots.txt");
		assertThat(content).isEqualTo("User-agent: *\nDisallow: /");
	}

}
