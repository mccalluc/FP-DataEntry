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

import java.util.List;

import org.eclipse.jetty.server.Server;
import org.filteredpush.dataentry.Constants;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.filteredpush.dataentry.enums.Term;
import org.filteredpush.dataentry.enums.Tuple;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsTest {

	private static Logger log = LoggerFactory.getLogger(JsTest.class);
	
	private static Server server;
	private static int frontEndPort;
	private static WebDriver driver;
	
	private static String getUrl(String path) {
		return "http://localhost:"+frontEndPort+path;
	}
	
	@BeforeClass
	public static void init() {
		EnumUtils.clearEnums();
		Term.init(Constants.TERMS_VALUE);
		Tuple.init(Constants.TUPLES_MAP_VALUE);
		
		server = new Utils.PortPicker(){
			public Server pick(int port) {
				frontEndPort = port;
				return FrontEndHandler.createServer(new MockFrontEndConfiguration(port));
			}
		}.pickFrom(8080,8082,8084,8086,8088);
	
		driver = new FirefoxDriver();
	}
	
	@AfterClass
	public static void quit() throws Exception {
		driver.close();
		server.stop();
		EnumUtils.clearEnums();
	}
	
	@Test
	public void testQUnit() {

		driver.get(getUrl("/js-test/test.html?noglobals=true"));

		int maxTries = 10;
		int sleepMS = 100;
		int tries = 0;
		while (tries < maxTries) {
			String title = driver.getTitle();
			log.debug("Try "+tries+"; QUnit title is: " + title);
			
			if (title.startsWith("\u2714")) {
				// (heavy checkmark)
				return;
			} else if (title.startsWith("\u2716")) {
				// (heavy X)
				String failsMessage = "QUnit reports these failures:\n";
				
				List<WebElement> fails = driver.findElements(By.cssSelector(".fail .fail"));
				assertThat(fails).isNotEmpty().as("Tests failed, but we can't find the failures on the page");
				
				for (WebElement fail : fails) {
					failsMessage += fail.getText();
				}
				
				Assert.fail(failsMessage);
			} else {
				tries++;
				try {
					Thread.sleep(sleepMS);
				} catch (InterruptedException e) {
					throw new Error(e);
				}
			}
		}
		
		Assert.fail("Waited "+(maxTries*sleepMS)+"ms but QUnit tests never completed.");

	}

}
