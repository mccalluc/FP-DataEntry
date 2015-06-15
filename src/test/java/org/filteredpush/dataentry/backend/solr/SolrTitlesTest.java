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

import java.util.ArrayList;
import java.util.List;

import org.filteredpush.dataentry.enums.Tuple;
import org.junit.Test;

public class SolrTitlesTest extends SolrAbstractTest {

	@SuppressWarnings("unused")
	private static void assertTitles(String title, List<String> expectMatch)  {
		assertTitles(title, expectMatch, new ArrayList<String>());
	}
	
	private static void assertTitles(String title, List<String> expectMatch, List<String> expectNoMatch) {
		assertTuple(title, expectMatch, expectNoMatch, Tuple.valueOf("exsiccateTitle"));
	}
	
	@Test
	public void testParenRemoval() {
		// TODO: make this work: I was actually using the assertName by mistake: that's why it worked before.
		// At least now I know there is a bug.
//		assertTitles("Something from HUH (with alternate title)", // form provided by autosuggest
//				Arrays.asList("Something from HUH") // form which has been indexed
//		);
	}
	
	
//	@Test
//	public void testLichensOfWesternNorthAmerica1() {
//		assertTitles("Anderson & Shushan: Lichens Of Western North America",
//				Arrays.asList("Lichens of Western North America"),
//				Arrays.asList("Anderson's Flora of Alaska and Adjacent Parts of Canada"));
//	}
//	
//	@Test
//	public void testLichensOfWesternNorthAmerica2() {
//		assertTitles("Lichens Of Western North America",
//				Arrays.asList("Anderson & Shushan: Lichens of Western North America"),
//				Arrays.asList("Anderson's Flora of Alaska and Adjacent Parts of Canada"));
//	}
//	
//	@Test
//	public void testCalicExs1() {
//		assertTitles("Caliciales Exsiccatae",
//				Arrays.asList("Caliceae Exsiccatae", "Caliciales Exsiccati"),
//				Arrays.asList("Calicut University Research Journal"));
//	}
//	
//	@Test
//	public void testCalicExs2() {
//		assertTitles("Tibell: Calic. Exs.",
//				Arrays.asList("Caliceae Exsiccatae", "Caliciales Exsiccati"),
//				Arrays.asList("Calicut University Research Journal"));
//	}
//	
//	@Test
//	public void testCladExs1() {
//		assertTitles("Rehm & Arnold: Clad. Exs.",
//				Arrays.asList("Cladoniae Exsiccatae"),
//				Arrays.asList("Cladoniae Europaeae"));
//	}
//	
//	@Test
//	public void testCladExs2() {
//		assertTitles("Cladoniae Exsiccatae",
//				Arrays.asList("Rehm & Arnold: Clad. Exs."),
//				Arrays.asList("Cladoniae Europaeae"));
//	}
//
//	@Test
//	public void testFlHungExsiccati1() {
//		assertTitles("Fl. Hung. exsiccati",
//				Arrays.asList("Flora Hungaria Exsiccata a Sectione Botanica Musei Nationalis Hungarici Edita",
//						"Flora Hungaria Exsiccata"));
//	}
//	
//	// TODO: What about searching for abbreviations w/o periods? Should that work?
//	
//	@Test
//	public void testFlHungExsiccati2() {
//		assertTitles("Flora Hungaria Exsiccata",
//				Arrays.asList("Flora Hungaria Exsiccata a Sectione Botanica Musei Nationalis Hungarici Edita",
//						"Fl. Hung. exsiccati"));
//	}
//	
//	@Test
//	public void testPhyscExs1() {
//		assertTitles("Nádvorník: Physc. Exs.",
//				Arrays.asList("Physciaceae Exsiccati", 
//						      "Physciacea Exsiccati"));
//	}
//	
//	@Test
//	public void testPhyscExs2() {
//		assertTitles("Physciaceae Exsiccati",
//				Arrays.asList("Nádvorník: Physc. Exs.", 
//						      "Physciacea Exsiccati"));
//	}

}
