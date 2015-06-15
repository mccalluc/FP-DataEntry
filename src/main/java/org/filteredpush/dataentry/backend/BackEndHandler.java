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

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.filteredpush.dataentry.CliOptionsParser;
import org.filteredpush.dataentry.Json;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.backend.solr.EmbeddedSolrQueryEngine;
import org.filteredpush.dataentry.backend.solr.HttpSolrQueryEngine;
import org.filteredpush.dataentry.backend.solr.UpdatingSolrServer;
import org.filteredpush.dataentry.configuration.BackEndConfiguration;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.filteredpush.dataentry.enums.QParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackEndHandler extends AbstractHandler{
	
	private GenericQueryEngine queryEngine;
	
	private static Logger log = LoggerFactory.getLogger(BackEndHandler.class);
	
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		
		String path = request.getPathInfo();
		{
			String query = request.getQueryString() != null
					? "?"+request.getQueryString()
					: "";
			log.debug("back-end request: "+path+query);
		}
		
		try {
			ServletOutputStream output = response.getOutputStream();
			
			Map<String,String[]> params = request.getParameterMap();
			Set<String> expectedParams = QParameter.toStringSet();

			if (path.equals("/robots.txt")) {
				response.setContentType("text/plain");
				response.setStatus(HttpServletResponse.SC_OK);
				
				output.print("User-agent: *\nDisallow: /");
			} else if (params.isEmpty()) {
				response.setContentType("text/plain");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				
				output.print("400: No query params\n"
						+ "Sorry, this back end server is only for API requests.\n"
						+ "(Did you mean to hit a front end server?)\n"
						+ "Expected params are: "+QParameter.toStringSet());
			} else if (!expectedParams.containsAll(params.keySet())) {
				response.setContentType("text/plain");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				
				String got = params.keySet().toString();
				output.print("400: Unexpected query param\n"
						+ "Got "+got+" but expected : "+QParameter.toStringSet());
			} else {
				response.setContentType("text/plain");
				response.setStatus(HttpServletResponse.SC_OK);
				response.addHeader("Access-Control-Allow-Origin", "*"); // CORS: http://enable-cors.org
				
				Iterable<TupleMultiRecord> results = queryEngine.query(params);
				output.print(Json.toEscaped(results));
			}
			
			output.close();
			baseRequest.setHandled(true);
		} catch (Throwable e) {
			// TODO: not really sure this is best, but I wasn't getting the stack traces otherwise.
			e.printStackTrace();
			throw new ServletException(e);
		}

	}
	
	@Override
	public void destroy() {
		queryEngine.shutdown();
	}
	
	public BackEndHandler(BackEndConfiguration config) {
		Class<?> queryEngineClass = config.getQueryEngineClass();
		if (queryEngineClass.equals(EmbeddedSolrQueryEngine.class)) {
			try {
				SolrServer solrServer = Utils.startSolr(config.getSolrDirectory());
				queryEngine = new EmbeddedSolrQueryEngine(solrServer);
			} catch (NoSuchElementException e) {
				SolrServer solrServer = new HttpSolrServer(config.getSolrUri().toString());
				queryEngine = new HttpSolrQueryEngine(solrServer);
			}
			
		} else {
			try {
				// TODO: No provision right now for providing constructor arguments to other QueryEngines.
				queryEngine = (GenericQueryEngine)queryEngineClass.newInstance();
			} catch (InstantiationException e) {
				throw new Error(e);
			} catch (IllegalAccessException e) {
				throw new Error(e);
			}
		}
		
	}
	
	public BackEndHandler(EmbeddedSolrServer solrServer) {
		queryEngine = new EmbeddedSolrQueryEngine(new UpdatingSolrServer(solrServer));
	}
	
	public static Server createServer(BackEndConfiguration config) {
		return createServer(config, Utils.startSolr(config.getSolrDirectory()));
	}
	
	public static Server createServer(BackEndConfiguration config, EmbeddedSolrServer solrServer) {
		int port = config.getPort();
		Server server = new Server(port);
		server.setHandler(new BackEndHandler(solrServer));
		log.info("Back-end server created for port "+port);
		return server;
	}

	public static void main(String... args) throws Exception {
		BackEndConfiguration config = new CliOptionsParser( //
				"Starts an HTTP server on the indicated port which responds with matches from the solr index.")
			.parseOrExit(args);
		
		EnumUtils.setEnums(config);
		createServer(config).start();
	}
}
