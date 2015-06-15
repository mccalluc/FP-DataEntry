package org.filteredpush.dataentry.configuration;

import java.io.InputStream;

import org.filteredpush.dataentry.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableMap;

public class ConfigurationAsXmlValidationTest {
	
	private static void assertException(String path, String substring) {
		InputStream stream = ConfigurationAsXmlValidationTest.class.getResourceAsStream(path);
		try {
			Document doc = Utils.parseXml(stream);
			new Configuration(doc);
		} catch (IllegalArgumentException e) {
			// Awesome! We were expecting one.
			if (e.getMessage().contains(substring)) {
				return;
			}
			Assert.fail("Expected error message '"+substring+"' instead of '"+e.getMessage()+"' from '"+path+"'.");
		}
		Assert.fail("There should have been an IllegalArgumentException, but there wasn't: " + path);
	}
	
	@Test
	public void testQNameQSolrMismatch() {
		assertException("/configurations/configuration-qname-qsolr-mismatch.xml", "q-name values [geographyTHAT] must equal q-solr keys [geographyTHIS]");
	}
	
	@Test
	public void testSolrDirectoryAndUri() {
		assertException("/configurations/configuration-solr-directory-and-uri.xml", "you must not have both solr-directory and solr-uri");
	}
	
	@Test
	public void testSolrDirectoryNorUri() {
		assertException("/configurations/configuration-solr-directory-nor-uri.xml", "you must have either solr-directory or solr-uri");
	}
	
	@Test
	public void testQSolrNotSchema() {
		assertException("/configurations/configuration-qsolr-not-indexed.xml", "Every q-solr must be indexed: [geography_not_indexed]");
	}
	
	@Test
	public void testQueryEnginePlusSolr() {
		assertException("/configurations/configuration-queryengine-plus-solr.xml", "If there is a query-engine-class there shouldn't be a solr-directory, "
				+"If there is a query-engine-class there shouldn't be a solr-uri, "
				+"If there is a query-engine-class there shouldn't be a solr-files or a solr-file-references");
	}

	@Test
	public void testANameNotTerm() {
		assertException("/configurations/configuration-aname-not-term.xml", "a-name attribute is not a term or term_id: [country_A]");
	}
	
	@Test
	public void testDuplicates() {
		// These aren't actually checked in the validation phase, 
		// so even if a file had multiple problems, we would only
		// catch one of them.
		assertException("/configurations/configuration-duplicate-element.xml", "There should only be one 'solr-directory' in document");
		assertException("/configurations/configuration-duplicate-input-field.xml", "Key 'my_country' should be used only once in 'input-fields'");
		assertException("/configurations/configuration-duplicate-q-solr.xml", "Multiple entries with same key: geography=geography_index_duplicate and geography=geography_index");
		assertException("/configurations/configuration-duplicate-solr-file.xml", "Multiple entries with same key: schema.xml=<duplicate/>");
		assertException("/configurations/configuration-duplicate-tuple.xml", "Multiple entries with same key: country");
	}
	
	@Test
	public void testMissingFile() {
		assertException("/configurations/configuration-missing.xml", "InputStream cannot be null");
	}

	@Test
	public void testRedundant() {
		// Just making sure Guava works the way I think it does:
		// ie, that there is a warning if we try to put in the same key twice.
		ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
		builder.put("a", "1");
		builder.put("a", "1");
		try {
			builder.build();
		} catch (IllegalArgumentException e) {
			String expected = "Multiple entries with same key: a=1 and a=1";
			if (!e.getMessage().contains(expected)) {
				Assert.fail("Expected error message '"+expected+"' instead of '"+e.getMessage());
			}
			return;
		}
		Assert.fail("There should have been an IllegalArgumentException");
	}
}
