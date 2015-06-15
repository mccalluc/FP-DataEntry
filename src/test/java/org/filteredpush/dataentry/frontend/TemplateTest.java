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

import java.util.HashMap;
import java.util.Map;

import org.filteredpush.dataentry.Template;
import org.filteredpush.dataentry.Template.TemplateException;
import org.junit.Assert;
import org.junit.Test;

public class TemplateTest {

	@Test
	public void testTemplate() {
		final Template template = new Template("genus: ${genus}; species: ${species}");
		@SuppressWarnings("serial")
		final Map<String,String> model = new HashMap<String,String>(){{
			this.put("genus", "Quercus");
			this.put("species", "alba");
		}};
		String filled = template.apply(model);
		assertThat(filled).isEqualTo("genus: Quercus; species: alba");
	}

	@Test
	public void testNonMetaCharacters() {
		String orig = "nothing to replace: \\ $ { }";
		final Template template = new Template(orig);
		String filled = template.apply(new HashMap<String,String>());
		assertThat(filled).isEqualTo(orig);
	}
	
	@Test
	public void testUnmatched() {
		String orig = "This is ${not} replaced.";
		final Template template = new Template(orig);
		try {
			template.apply(new HashMap<String,String>());
		} catch (TemplateException e) {
			assertThat(e.getMessage()).isEqualTo("Unmatched pattern in template: ${not}");
			return;
		}
		Assert.fail("Should have thrown exception");
	}

	@SuppressWarnings("serial")
	@Test
	public void testProhibited() {
		String orig = "${nesting} is not allowed.";
		final Template template = new Template(orig);
		try {
			template.apply(new HashMap<String,String>(){{this.put("nesting", "deeper ${nesting}");}});
		} catch (TemplateException e) {
			assertThat(e.getMessage()).isEqualTo("Template replacement contains prohibited pattern: ${nesting}");
			return;
		}
		Assert.fail("Should have thrown exception");
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testNotConfusedByBackRefs() {
		String orig = "${replace} but not $/ back-refs.";
		final Template template = new Template(orig);
		assertThat(template.apply(new HashMap<String,String>(){{this.put("replace", "REPLACED");}})).isEqualTo("REPLACED but not $/ back-refs.");
	}

	@SuppressWarnings("serial")
	@Test
	public void testKeyFormat() {
		String orig = "${not a valid key}";
		final Template template = new Template(orig);
		try {
			template.apply(new HashMap<String,String>(){{this.put("not a valid key", "whatever");}});
		} catch (TemplateException e) {
			assertThat(e.getMessage()).isEqualTo("Key not of correct format: not a valid key");
			return;
		}
		Assert.fail("Should have thrown exception");
	}
	
}
