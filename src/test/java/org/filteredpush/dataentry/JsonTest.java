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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fest.assertions.MapAssert;
import org.filteredpush.dataentry.Json;
import org.junit.Test;

public class JsonTest {
	
	@Test
	public void testFromMap() {
		Map<String,String> map = new HashMap<String,String>();
		map.put("a", "1");
		map.put("b", "2");
		String json = Json.toUTF8(map);
		assertThat(json)
			.contains("\"a\":\"1\"")
			.contains("\"b\":\"2\"");
	}

	@Test
	public void testFromArray() {
		List<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		String json = Json.toUTF8(list);
		assertThat(json)
			.isEqualTo("[\"a\",\"b\"]");
	}
	
	@Test
	public void testToMap() {
		String json = "{\"a\":\"1\",\"b\":\"2\"}";
		@SuppressWarnings("unchecked")
		Map<String,String> map = (Map<String,String>)Json.from(json);
		assertThat(map)
			.includes(MapAssert.entry("a","1"))
			.includes(MapAssert.entry("b","2"));
	}
	
	@Test
	public void testToArray() {
		String json = "[\"a\",\"b\"]";
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>)Json.from(json);
		assertThat(list)
			.containsExactly("a","b");
	}
	
	@Test
	public void testNonAsciiToJson() {
		String eAcute = "\u00C9";
		String eu = eAcute+"tats-Unis";
		String snowman = "\u2603";
		
		List<String> list = Arrays.asList(eu,snowman);
		assertThat(Json.toUTF8(list))
			.isEqualTo("[\""+StringUtils.join(list, "\",\"")+"\"]");
	}
	
	@Test
	public void testSpecialCharsToJson() {
		List<String> input = Arrays.asList("\"","'","\\","[");
		List<String> output = Arrays.asList("\\\"","'","\\\\","[");
		
		assertThat(Json.toUTF8(input))
			.isEqualTo("[\""+StringUtils.join(output, "\",\"")+"\"]");
	}
	
	@Test
	public void testJsonToNonAscii() {
		String json = "[\"\\u00C9tats-Unis\",\"\\u2603\"]";
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>)Json.from(json);
		assertThat(list)
			.containsExactly("\u00C9tats-Unis","\u2603");
	}
	
}
