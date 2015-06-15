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
package org.filteredpush.dataentry;

import static org.fest.assertions.Assertions.assertThat;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableSet;

public class UtilsTest {

	@Test
	public void testSimpleQueryMapToString() {
		Map<String,String> map = new HashMap<String,String>();
		map.put("a", "b");
		assertThat(Utils.queryMapToString(map)).isEqualTo("a:\"b\"");
	}
	
	@Test
	public void testComplexQueryMapToString() {
		Map<String,String> map = new HashMap<String,String>();
		map.put("a", "~!@#$%^&*()_+`-={}|[]\\:\";'<>?,./");
		map.put("b", "foo bar");
		map.put("c", "far boo");
		assertThat(Utils.queryMapToString(map))
			.doesNotContain(" OR ")
			.contains(" AND ")
			.contains("c:\"far boo\"")
			.contains("b:\"foo bar\"")
			.contains("a:\"\\~\\!@#$%\\^\\&\\*\\(\\)_\\+`\\-=\\{\\}\\|\\[\\]\\\\\\:\\\";'<>\\?,.\\/");
	}
	
	@Test
	public void testXPathNullAttribute() {
		InputStream is = IOUtils.toInputStream("<root><child>Bert</child><child>Ernie</child></root>");
		Document doc = Utils.parseXml(is);
		assertThat(
				new ImmutableSet.Builder<String>().add("Bert").add("Ernie").build()
			).isEqualTo(
				Utils.evalXpath(doc, "//child", null)
			);
	}
	
	@Test
	public void testXPathAttribute() {
		InputStream is = IOUtils.toInputStream("<root><child color='yellow'>Bert</child><child color='orange'>Ernie</child></root>");
		Document doc = Utils.parseXml(is);
		assertThat(
				new ImmutableSet.Builder<String>().add("yellow").add("orange").build()
			).isEqualTo(
				Utils.evalXpath(doc, "//child", "color")
			);
	}
	
	@Test
	public void testXPathBad() {
		InputStream is = IOUtils.toInputStream("<root></root>");
		Document doc = Utils.parseXml(is);
		try {
			Utils.evalXpath(doc, "!no!good!", null);
		} catch (Error e) {
			assertThat(e.getMessage()).contains("XPathExpressionException");
			return;
		}
		Assert.fail("Should have thrown exception");
	}
	
}
