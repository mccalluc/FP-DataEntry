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
package org.filteredpush.dataentry.frontend;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.filteredpush.dataentry.BothEndsHandler;
import org.filteredpush.dataentry.CliOptionsParser;
import org.filteredpush.dataentry.Json;
import org.filteredpush.dataentry.Template;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.configuration.Configuration;
import org.filteredpush.dataentry.configuration.FrontEndConfiguration;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.filteredpush.dataentry.enums.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrontEndHandler extends AbstractHandler {
	
	private static Logger log = LoggerFactory.getLogger(FrontEndHandler.class);
	public static final String CONFIG_URL = "/js/CONFIG.js";
	
	private String pairsJson;
	
	private static Map<String,String> mimeMap = new HashMap<String,String>();
	
	private Map<String,String> templateModel;
	
	private String configAsString;
	
	static {
		mimeMap.put("html", "text/html;charset=utf-8");
		mimeMap.put("js", "application/javascript");
		mimeMap.put("css", "text/css");
		mimeMap.put("png", "image/png");
		mimeMap.put("text", "text/plain");
		mimeMap.put("xml", "text/xml");
	}
	
	@SuppressWarnings("serial")
	public FrontEndHandler(final FrontEndConfiguration config) {
		this.pairsJson = Tuple.toJson();

		this.templateModel = new HashMap<String, String>() {{
			this.put("inputsHtml", getInputsHtml(config.getHasSelectorFunction(), config.getInputKeys(), config.getInputDefaults(), config.getInputLabels()));
			this.put("qNamesJson", escapeHtml4(Json.toEscaped(config.getQNames())));
			this.put("selectorsJson", escapeHtml4(Json.toEscaped(config.getSelectors())));
			this.put("selectorFunction", escapeJsHtml(config.getSelectorFunction()));
			this.put("suggestionFieldsJson", Json.toEscaped(config.getSuggestionFields()));
			this.put("fakeAjaxJson", Json.toEscaped(config.getFakeAjaxData()));
			this.put("qInputsHtml", getQInputsHtml(new ArrayList<String>(config.getQNames().values())));
			this.put("titleHtml", config.getTitleHtml());
			this.put("blurbHtml", config.getBlurbHtml());
			this.put("requireConfirmation", config.getRequireConfirmation() ? "true" : "false");
			this.put("confirmationButtonHtml", config.getRequireConfirmation() ? " <button id='confirm'>confirm</button>" : ""); // TODO: really ugly.
			this.put("preJs", escapeJsHtml(config.getPreJs()));
		}};
		
		configAsString = config.toString();
	}
	

	// TODO: Rather adhoc, and I'm sure I'm missing edge cases. Use compositions of real escape utilities.
	private static String escapeJsHtml(String input) {
		return input.replace("'", "&#39;").replace("\"", "&quot;");
	}
	
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response) {
		
		String path = request.getPathInfo();
		{
			String query = request.getQueryString() != null
					? "?"+request.getQueryString()
					: "";
			log.debug("front-end request: "+path+query);
		}
		
		try {
			if (CONFIG_URL.equals(path)) {
				handleConfigRequest(request, response);
			} else if ("/robots.txt".equals(path)) {
				handleRobotsTxtRequest(request, response);
			} else if ("/config.xml".equals(path)) {
				handleConfigXmlRequest(request, response);
			} else {
				handleOtherRequest(request, response);
			}
		} catch (IOException e) {
			throw new Error(e);
		}
		
		if (baseRequest != null) {
			baseRequest.setHandled(true);
		}
	}
	
	/********* Cases **********/
	
	private void handleConfigRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType(mimeMap.get("js"));
		
		response.getOutputStream().print(
			"fp.BACK_END_URL = '"+BothEndsHandler.BACK_END_PATH+"';"
			+ "fp.term_pair_list = "+pairsJson+";"
		);
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	private void handleRobotsTxtRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType(mimeMap.get("txt"));
		response.getOutputStream().print("User-agent: *\nDisallow: /");
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	private void handleConfigXmlRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType(mimeMap.get("xml"));
		response.getOutputStream().print(configAsString);
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	private void handleOtherRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getPathInfo();
		String fileName = "/front-end/static"
				+ ( "/".equals(path)
					? "/index.html"
					: path );
		String extension = fileName.replaceFirst(".*\\.", "");
		if (mimeMap.containsKey(extension)) {
			response.setContentType(mimeMap.get(extension));
			try {
				String server = request.getScheme() + "://" +request.getServerName()+":"+request.getServerPort();
				String context = request.getContextPath() == null
						? "" // ie Jetty
						: request.getContextPath(); // ie Servlet
				templateModel.put("baseUrl", server + context);
				
				String template = Utils.readResource(fileName.substring(1));
				String filled = new Template(template).apply(templateModel);
				response.getOutputStream().print(filled);
				
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (NullPointerException e) {
				response.sendError(404);
			}
		} else {
			response.sendError(404);
		}
		
	}
	
	/********************************/
	
	private static String getInputsHtml(boolean hasSelectorFunction, List<String> inputKeys, Map<String, String> inputDefaults, Map<String, String> inputLabels) {
		if (inputKeys.size() == 0) {
			return "<strong>(The <code>"+Configuration.INPUT_FIELDS+"</code> element is missing from the configuration, so no demo is available.)</strong>";
		} else {
			StringBuilder htmlBuilder = new StringBuilder();
			if (hasSelectorFunction) {
					htmlBuilder.append("<strong>The presence of the <code>"+Configuration.SELECTOR_FUNCTION+"</code> element means that "
							+ "not all fields are identified simply by name. Those fields will not work in this demo, but the bookmarklet will still work.</strong>");
					// ie: If you specify a selector-function, you are not limited to extracting by field name...
					// but we can't make the demo UI arbitrarily complicated to match the target application.
			}
			
			for (String key : inputKeys) {
				String nameHtml = escapeHtml4(key);
				String labelHtml = inputLabels.containsKey(key) //
						? escapeHtml4(inputLabels.get(key)) //
						: nameHtml;
				String valueAttr = inputDefaults.containsKey(key) //
						? " value='" + escapeHtml4(inputDefaults.get(key)) + "'" //
						: "";
				htmlBuilder.append(String.format("<label>%s <input name='%s' %s/></label>", labelHtml, nameHtml, valueAttr));
			}
			return htmlBuilder.toString();
		}
	}
	
	private static String getQInputsHtml(List<String> qNamesInOrder) {
		Set<String> seen = new HashSet<String>();
		StringBuilder htmlBuilder = new StringBuilder();
		for (String name : qNamesInOrder) {
			if (!seen.contains(name)) {
				seen.add(name);
				String nameHtml = escapeHtml4(name);
				htmlBuilder.append(String.format("<label id='q-%s'>%s <input name='%s'/></label>", nameHtml, nameHtml, nameHtml));
			}
		}
		return htmlBuilder.toString();
	}
	
	/********************************/
	
	protected static Server createServer(FrontEndHandler handler, int frontEndPort) {
		Server server = new Server(frontEndPort);
		server.setHandler(handler);
		log.info("Front-end server created for port "+frontEndPort);
		return server;
	}
	
	public static Server createServer(FrontEndConfiguration config) {
		return createServer(new FrontEndHandler(config), config.getPort());
	}
	
	public static void main(String... args) throws Exception {
		FrontEndConfiguration config = new CliOptionsParser(
				"Starts an HTTP server on the indicated port. "
				+ "This server provides all the necessary static content for the plugin: "
				+ "The real work is done by the back-end server.")
			.parseOrExit(args);
		
		EnumUtils.setEnums(config);
		createServer(config).start();
	}

}
