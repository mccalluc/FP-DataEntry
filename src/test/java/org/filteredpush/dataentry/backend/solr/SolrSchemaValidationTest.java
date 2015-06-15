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

import java.io.File;
import java.net.URISyntaxException;
import java.util.Set;

import org.filteredpush.dataentry.backend.solr.SolrInstaller.SolrInstallerException;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class SolrSchemaValidationTest {

	private static Set<String> tupleSet = new ImmutableSet.Builder<String>().add(
			"recordNumber","recordedBy","country","stateCountyCity","verbatimLocality"
	).build();
	
	private static void assertException(String path, String substring) {
		File schema;
		try {
			schema = new File(SolrSchemaValidationTest.class.getResource(path).toURI());
		} catch (URISyntaxException e) {
			throw new Error(e);
		}
		
		try {
			SolrInstaller.validateJustSchema(schema, tupleSet);
		} catch (SolrInstallerException e) {
			// Awesome! We were expecting one.
			if (!e.getMessage().contains(substring)) {
				Assert.fail("Expected error message '"+substring+"' instead of '"+e.getMessage()+"' from '"+schema+"'.");
			}
			return;
		}
		
		Assert.fail("There should have been a SolrInstallerException, but there wasn't: " + path);
	}
	
	@Test
	public void testMissingCopyField() {
		assertException("/schemas/schema-missing-copy-field.xml", "the 'dest' of a copyField does not match a known field");
	}
	
	@Test
	public void testMissingTupleField() {
		assertException("/schemas/schema-missing-tuple-field.xml", "there is a Tuple value missing from the list of fields");
	}
	
	@Test
	public void testFieldCount() {
		assertException("/schemas/schema-field-count.xml", "5 (tuples) + 1 (schema copies) + 1 != 9 (schema fields)");
	}
	
	@Test
	public void testIndexedFields() {
		assertException("/schemas/schema-indexed-fields.xml", "5 (tuples) + 1 (schema copies) + 1 != 8 (schema fields)");
	}
	
	@Test
	public void testUnindexedFields() {
		assertException("/schemas/schema-unindexed-fields.xml", "5 (tuples) + 1 (schema copies) + 1 != 8 (schema fields)");
	}
	
	@Test
	public void testBadUniqueKey() {
		assertException("/schemas/schema-bad-unique-key.xml", "'uniqueKey' must be '_fp_internal_id'");
	}
	
	@Test
	public void testUnusedTypes() {
		assertException("/schemas/schema-unused-types.xml", "these types are defined but not used: [fpUnused]");
	}
	
	@Test
	public void testFpTypes() {
		assertException("/schemas/schema-fp-types.xml", "indexed fields should always be one of the fp* types.");
	}
	
	@Test
	public void testStringType() {
		assertException("/schemas/schema-string-type.xml", "unindexed fields should simply be 'string'.");
	}
	
	@Test
	public void testFpIDType() {
		assertException("/schemas/schema-fp-id-type.xml", "_fp_internal_id needs to be type 'fpID");
	}
	
}
