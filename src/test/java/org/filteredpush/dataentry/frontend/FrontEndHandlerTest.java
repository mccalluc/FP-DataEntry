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

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.eclipse.jetty.server.Server;
import org.filteredpush.dataentry.Constants;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.filteredpush.dataentry.enums.Term;
import org.filteredpush.dataentry.enums.Tuple;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FrontEndHandlerTest {

	private static Server server;
	private static int frontEndPort;
	
	private static String getUrl(String path) {
		return "http://localhost:"+frontEndPort+path;
	}
	
	@BeforeClass
	public static void init() {
		Term.init(Constants.TERMS_VALUE);
		Tuple.init(Constants.TUPLES_MAP_VALUE);
		
		server = new Utils.PortPicker(){
			public Server pick(int port) {
				frontEndPort = port;
				return FrontEndHandler.createServer(new MockFrontEndConfiguration(port));
			}
		}.pickFrom(8080,8082,8084,8086,8088);
	}
	
	@AfterClass
	public static void quit() throws Exception {
		server.stop();
		EnumUtils.clearEnums();
	}
	
	static void assertUrlMimeContent(String url, String mime, String substring) {
		Content content;
		try {
			content = Request.Get(getUrl(url)).execute().returnContent();
		} catch (ClientProtocolException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}
		assertThat(content.getType().getMimeType()).isEqualTo(mime);
		assertThat(content.asString()).contains(substring);
	}
	
	@Test
	public void testGET() {
		assertUrlMimeContent("/",                    "text/html",               "<html>");
		assertUrlMimeContent("/css/data-entry.css",  "text/css",                "body {");
		assertUrlMimeContent("/js/core.js",          "application/javascript",  "$(function(){");
		assertUrlMimeContent("/robots.txt",          "text/plain",              "User-agent: *\nDisallow: /");
		assertUrlMimeContent("/config.xml",          "text/xml",                "<configuration>");
	}

}
