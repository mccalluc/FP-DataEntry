package org.filteredpush.dataentry;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.filteredpush.dataentry.configuration.MockConfiguration;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;

import com.google.common.collect.Sets;

public class UITest {

	// private static Logger log = LoggerFactory.getLogger(BothEndsHandlerTest.class);
	
	private static Server server;
	private static int port;
	private static WebDriver driver;
	
	private static String getUrl(String path) {
		return "http://localhost:"+port+path;
	}
	
	@BeforeClass
	public static void init() {
		server = new Utils.PortPicker(){
			public Server pick(int tryThisPort) {
				port = tryThisPort;
				return BothEndsHandler.createServer(new MockConfiguration(port));
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

	private static void sleep(int ms) {
		// TODO: I shouldn't need a hack like this to avoid race conditions.
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new Error(e);
		}
	}
	
	@Test
	public void basicTest() {
		driver.get(getUrl(""));
		// This isn't magic, but if the globals we create change, we want to know.
		assertGlobals("methods", "console", "document", "window", "external", "FpDataEntryPlugin", "InstallTrigger", "sidebar", "getInterface");
		
		Set<String> globals = getGlobals();
		assertThat(driver.getTitle()).isEqualTo("mock configuration title");
		assertText("mock configuration blurb");
		assertText("<\"test-escaping\">");
		
		// Before:
		assertValues("input", "", "Smith");
		
		List<WebElement> links = driver.findElements(By.tagName("a"));
		assertThat(links).hasSize(4);
		
		List<WebElement> buttons = driver.findElements(By.tagName("button"));
		assertThat(buttons).hasSize(2);
		
		Asserter asserter = new Asserter(){
			@Override
			public void asserts(WebElement div) {
				WebElement iframe = div.findElement(By.tagName("iframe"));
				driver.switchTo().frame(iframe);
				
				// This isn't magic, but if the globals we create change, we want to know.
				assertGlobals( "location", "external", "$", "sidebar", "console", "document", 
						"jQuery###", "window", "jQuery", "InstallTrigger", "getInterface", "fp");
				
				sleep(1000); // TODO: Without this we hit a race condition.
				assertValues("#input input", "Smith");
				assertValues("#output input", "John", "Smith");
				
				driver.switchTo().defaultContent();
				List<WebElement> controls = div.findElements(By.tagName("div"));
				assertThat(controls).hasSize(2);
				assertDrag(div, controls.get(0));
				assertClose(div, controls.get(1));
			}
		};
		
		assertBookmarklet(links.get(0), "iframe-parent-id", asserter);
		assertButton(buttons.get(0), "iframe-parent-id", asserter);
		
		assertLink(links.get(1), "https://sourceforge.net/p/filteredpush/svn/HEAD/tree/trunk/FP-DataEntry/");
		assertLink(links.get(2), "http://localhost:"+port+"/config.xml");
		
		assertBookmarklet(links.get(3), "fp-bookmarklet-div", new Asserter(){
			@Override
			public void asserts(WebElement div) {
				assertBashScript(div.getText());
				assertClose(div, div.findElement(By.tagName("button")));
			}
		});
		
		// After:
		assertValues("input", "John", "Smith");
		
		Set<String> newGlobals = Sets.difference(getGlobals(), globals);
		assertThat(newGlobals).isEqualTo(new HashSet<String>(Arrays.asList(
				"fp_data_entry_plugin", // OK
				"location", "field", // Not sure where these come from.
				"fxdriver_id", "__fxdriver_unwrapped" // From Selenium, I'm pretty sure.
		)));
	}
	
	@SuppressWarnings("unchecked")
	private static Set<String> getGlobals() {
		return new HashSet<String>((List<String>)((JavascriptExecutor)driver).executeScript("return Object.keys(window)"));
	}
	
	private static void assertGlobals(String... expected) {
		Set<String> globals = getGlobals();
		for (String global : new HashSet<String>(globals)) {
			if (global.matches("^jQuery\\d+$")) {
				globals.remove(global);
				globals.add("jQuery###");
			}
		}
		// This isn't magic, but if the globals we create change, we want to know.
		assertThat(globals).as("javascript globals").isEqualTo(new HashSet<String>(Arrays.asList(expected)));
	}
	
	private static void assertAlert(String substring) {
		Alert alert = driver.switchTo().alert();
		assertThat(alert.getText()).contains(substring);
		alert.accept();
	}
	
	private static void assertText(String substring) {
		assertThat(driver.findElement(By.tagName("body")).getText()).contains(substring);
	}
	
	private static void assertValues(String cssSelector, String... values) {
		List<WebElement> els = driver.findElements(By.cssSelector(cssSelector));
		assertThat(els).hasSize(values.length);
		for (int i=0; i<values.length; i++) {
			assertThat(els.get(i).getAttribute("value")).isEqualTo(values[i]);
		}
	}
	
	private static void assertLink(WebElement link, String url) {
		assertThat(link.getAttribute("href")).isEqualTo(url);
		assertThat(link.getAttribute("target")).isEqualTo("_blank");
	}
	
	private static void assertBookmarklet(WebElement bookmarklet, String bookmarkletDivId, Asserter asserter) {
		assertThat(bookmarklet.getText()).isEqualTo("bookmarklet");
		bookmarklet.click();
		assertAlert("Drag this to your bookmarks menu,\nor right click and save it as a bookmark.");
		
		String href = bookmarklet.getAttribute("href");
		assertThat(href).matches("^javascript:.*");
		driver.get(href);
		WebElement div = driver.findElement(By.id(bookmarkletDivId));
		asserter.asserts(div);
	}
	
	private static void assertButton(WebElement button, String bookmarkletDivId, Asserter asserter) {
		button.click();
		WebElement div = driver.findElement(By.id(bookmarkletDivId));
		asserter.asserts(div);
	}
	
	private static int height = 0;
	private static int width = 0;
	
	private static abstract class Asserter {
		// TODO: anonymous functions. :)
		abstract public void asserts(WebElement div);
		public void assertClose(WebElement div, WebElement closeTarget) {
			closeTarget.click();
			try {
				div.getText();
				Assert.fail("div should not exist at this point.");
			} catch (StaleElementReferenceException e) {
				// (expected)
			}
		}
		public void assertDrag(WebElement div, WebElement dragTarget) {
			int oldHeight = div.getSize().getHeight();
			int oldWidth = div.getSize().getWidth();
			
			if (height > 0 && width > 0) {
				// Make sure size restoration from cookie works:
				assertThat(height).isEqualTo(oldHeight);
				assertThat(width).isEqualTo(oldWidth);
			}
			
			int offset = 100;
			Actions builder = new Actions(driver);
			builder.clickAndHold(dragTarget) //
				.moveByOffset(-offset, -offset) //
				.release();
			builder.build().perform();
			height = div.getSize().getHeight();
			width = div.getSize().getWidth();
			assertTrue(height > oldHeight);
			assertTrue(width > oldWidth);
			// TODO: The numbers aren't precisely what I want.
		}
		public void assertBashScript(String script) {
			// Just using lots of substring matches will be reasonably robust,
			// and easy to debug if necessary.
			assertThat(script)
				.contains("cat >$INPUT <<END_INPUT")
				.contains("first________name|last")
				.contains("first________name example|last example")
				.contains("END_INPUT")
				
				.contains("cat >$CONFIG <<END_CONFIG")
				.contains("<configuration> ")
				.contains("  <solr-directory>$SOLR_DIR</solr-directory>")
				.contains("  <solr-files>")
				.contains("   <item key=\"schema.xml\">")
				.contains("    <schema name=\"example\" version=\"1.5\">")
				.contains("     <fields>")
				.contains("      <field name=\"_fp_internal_id\" type=\"fpID\" indexed=\"true\" stored=\"false\" required=\"true\" multiValued=\"false\"/>")
				.contains("      <field name='first________name' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>")
				.contains("      <field name='last' type='fpMinimallySearchable' indexed='true' stored='true' required='false' multiValued='true'/>")
				.contains("     </fields>")
				.contains("     <uniqueKey>_fp_internal_id</uniqueKey>")
				.contains("     <types>")
				.contains("      <fieldType name=\"fpMinimallySearchable\" class=\"solr.TextField\" sortMissingLast=\"true\" omitNorms=\"true\">")
				.contains("       <analyzer>")
				.contains("        <tokenizer class=\"solr.StandardTokenizerFactory\"/>")
				.contains("        <filter class=\"solr.LowerCaseFilterFactory\"/>")
				.contains("       </analyzer>")
				.contains("      </fieldType>")
				.contains("      <fieldType name=\"fpID\" class=\"solr.StrField\" sortMissingLast=\"true\"/>")
				.contains("     </types>")
				.contains("    </schema>")
				.contains("   </item>")
				.contains("   <item key=\"solrconfig.xml\">")
				.contains("    <config>")
				.contains("     <luceneMatchVersion>4.5</luceneMatchVersion>")
				.contains("     <requestHandler name=\"/select\" class=\"solr.SearchHandler\">")
				.contains("      <lst name=\"defaults\">")
				.contains("       <int name=\"rows\">10</int>")
				.contains("       <str name=\"df\">text</str>")
				.contains("      </lst>")
				.contains("     </requestHandler>")
				.contains("     <requestHandler name=\"/update\" class=\"solr.UpdateRequestHandler\"/>")
				.contains("    </config> ")
				.contains("   </item>")
				.contains("  </solr-files>")
				.contains("  <port>$PORT</port>")
				.contains("  <tuples-map>")
				.contains("   <item key='first________name'><item>first________name</item></item>")
				.contains("   <item key='last'><item>last</item></item>")
				.contains("  </tuples-map>")
				.contains("  <ingest-file>$INPUT</ingest-file>")
				.contains("  <delimiter>|</delimiter>")
				.contains("  <title-html>DEMO: localhost-"+port+"</title-html>")
				.contains("  <blurb-html>$BLURB</blurb-html>")
				.contains("  <q-solr>")
				.contains("   <item key='first________name'>first________name</item>")
				.contains("   <item key='last'>last</item>")
				.contains("  </q-solr>")
				.contains("  <input-fields>")
				.contains("   <item key='first!@#%^?()name' label='first________name' q-name='first________name' a-name='first________name'/>")
				.contains("   <item key='last' label='last' q-name='last' a-name='last' default='last example'/>")
				.contains("  </input-fields>")
				.contains("</configuration>")
				.contains("END_CONFIG");
		}
	}

}
