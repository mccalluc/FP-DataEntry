package org.filteredpush.dataentry.configuration;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.configuration.ConfigurationBase.UnexpectedElementException;
import org.filteredpush.dataentry.configuration.ConfigurationBase.UnexpectedTextException;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableMap;

public class ConfigurationBaseTest {
	
	private static ConfigurationBase parse(String xml) {
		Document doc = Utils.parseXml(IOUtils.toInputStream(xml));
		return new ConfigurationBase(doc);
	}

	// Data extraction:
	
	@Test
	public void testGetString() {
		ConfigurationBase config = parse("<root><group><hide-and> <!-- ignore --> go-seek <!-- ignore --> </hide-and></group></root>");
		assertThat(config.getString("hide-and")).isEqualTo("go-seek");
	}
	
	@Test 
	public void testGetList() {
		ConfigurationBase config = parse("<root>\n<list>\n<item>\nA</item><item>B</item><item>C</item></list></root>");
		assertThat(config.getList("list")).isEqualTo(Arrays.asList("A","B","C"));
	}
	
	@Test 
	public void testGetEmptyList() {
		ConfigurationBase config = parse("<root>\n<list>\n</list></root>");
		assertThat(config.getList("list")).isEqualTo(new ArrayList<String>());
	}
	
	@Test 
	public void testGetMap() {
		ConfigurationBase config = parse("<root>\n<map>\n<item key='k1'>\nv1</item><item key='k2'>v2</item></map></root>");
		assertThat(config.getMap("map")).isEqualTo(ImmutableMap.of(
				"k1", "v1", //
				"k2", "v2"
		));
	}
	
	@Test 
	public void testGetMapOfLists() {
		String list1 = "<item>a1</item><item>a2</item>";
		String list2 = "<item>b</item>";
		ConfigurationBase config = parse("<root>\n<map>\n<item key='k1'>\n"+list1+"</item><item key='k2'>"+list2+"</item></map></root>");
		assertThat(config.getMapOfLists("map")).isEqualTo(ImmutableMap.of(
			"k1", Arrays.asList("a1","a2"), //
			"k2", Arrays.asList("b")
		));
	}
	
	@Test 
	public void testGetMapOfMaps() {
		String list1 = "<item key='k11'>a</item>";
		String list2 = "<item key='k22'>b</item>";
		ConfigurationBase config = parse("<root>\n<map>\n<item key='k1'>\n"+list1+"</item><item key='k2'>"+list2+"</item></map></root>");
		assertThat(config.getMapOfMaps("map")).isEqualTo(ImmutableMap.of(
				"k1", ImmutableMap.of("k11", "a"), //
				"k2", ImmutableMap.of("k22", "b")));
	}
	
	@Test
	public void testGetTransposedMapOfMaps() {
		String item1 = "<item key='k1' letter='a' number='1'/>";
		String item2 = "<item key='k2' letter='b' number='2'/>";
		ConfigurationBase config = parse("<root>\n<map>\n"+item1+item2+"</map></root>");
		assertThat(config.getTransposedMapOfMaps("map")).isEqualTo(ImmutableMap.of(
				"key", ImmutableMap.of("k1", "k1", "k2", "k2"),
				"letter", ImmutableMap.of("k1", "a", "k2", "b"),
				"number", ImmutableMap.of("k1", "1", "k2", "2")));
	}
	
	@Test
	public void testGetXml() {
		ConfigurationBase config = parse("<root><xml>\nstuff<sub/>more stuff\n</xml></root>");
		assertThat(config.getXml("xml")).isEqualTo("stuff<sub/>more stuff");
	}
	
	@Test
	public void testGetMapOfXml() {
		ConfigurationBase config = parse("<root><map>\n<item key='text'>some\ntext</item><item key='xml'><some><xml/></some></item>\n</map></root>");
		assertThat(config.getMapOfXml("map")).isEqualTo(ImmutableMap.of(
				"text", "some\ntext",
				"xml", "<some><xml/></some>"));
	}
	
	// Error conditions:
	
	@Test
	public void testRedundantException() {
		ConfigurationBase config = parse("<root><a>1</a><a>2</a></root>");
		try {
			config.getString("a");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage()).contains("There should only be one 'a' in document.");
			return;
		}
		Assert.fail("Should have been an exception");
	}
	
	@Test
	public void testNoSuchException() {
		ConfigurationBase config = parse("<root></root>");
		try {
			config.getString("no-such");
		} catch (NoSuchElementException e) {
			return; // expected
		}
		Assert.fail("Should have been an exception");
	}
	
	@Test
	public void testUnexpectedTextException() {
		ConfigurationBase config = parse("<root><list>extraneous</list></root>");
		try {
			config.getList("list");
		} catch (UnexpectedTextException e) {
			return; // expected
		}
		Assert.fail("Should have been an exception");
	}
	
	@Test
	public void testUnexpectedElementException() {
		ConfigurationBase config = parse("<root><list><surprise/></list></root>");
		try {
			config.getList("list");
		} catch (UnexpectedElementException e) {
			return; // expected
		}
		Assert.fail("Should have been an exception");
	}
	
	@Test
	public void testTransposedMapUnexpectedElementException() {
		String item1 = "<item key='k1' letter='a' number='1'/>";
		String item2 = "<item key='k2' letter='b' number='2'/>";
		ConfigurationBase config = parse("<root>\n<map>\n"+item1+item2+"<surprise/></map></root>");
		try {
			config.getTransposedMapOfMaps("map");
		} catch (UnexpectedElementException e) {
			return; // expected
		}
		Assert.fail("Should have been an exception");
	}
	
	@Test
	public void testTransposedMapUnexpectedTextException() {
		String item1 = "<item key='k1' letter='a' number='1'/>";
		String item2 = "<item key='k2' letter='b' number='2'/>";
		ConfigurationBase config = parse("<root>\n<map>\n"+item1+item2+"surprise!</map></root>");
		try {
			config.getTransposedMapOfMaps("map");
		} catch (UnexpectedTextException e) {
			return; // expected
		}
		Assert.fail("Should have been an exception");
	}
	
}
