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
import java.util.Arrays;
import java.util.List;

import org.filteredpush.dataentry.enums.Tuple;
import org.junit.Test;

public class SolrNamesTest extends SolrAbstractTest {
	
	protected static void assertNames(String collectorNames, List<String> expectMatch)  {
		assertNames(collectorNames, expectMatch, new ArrayList<String>());
	}
	
	protected static void assertNames(String collectorNames, List<String> expectMatch, List<String> expectNoMatch) {
		assertTuple(collectorNames, expectMatch, expectNoMatch, Tuple.valueOf("recordedBy"));
	}
	
	@Test
	public void testLast()  {
		// Potential metacharacters are escaped before they hit solr.
		assertNames("Smith",
				Arrays.asList("Smith"),
				Arrays.asList("Smit%","Smit","Smit?","Smit*"));
	}
	
	@Test
	public void testLastLast()  {
		// There are cases of "First Last" and "Last First" in the database,
		// so we just need to index both, since it's impossible to reliably ID the last name.
		assertNames("Able Baker",
				Arrays.asList("Able","Baker","able","baker","aBLE","bAKER"));
	}
	
	@Test
	public void testLastLastLast()  {
		// But we don't want to index the whole string, so stop after the first two.
		assertNames("Able Baker Charlie",
				Arrays.asList("Able","Baker"),
				Arrays.asList("Charlie"));
	}
	
	@Test
	public void testInitialsLast()  {
		assertNames("A.Z.Smith",
				Arrays.asList("Smith"),
				Arrays.asList("A.Z."));
	}
	
	@Test
	public void testLastCommaFirst()  {
		assertNames("Last, First",
				Arrays.asList("Last"),
				Arrays.asList("First"));
	}
	
	@Test
	public void testBillAndMelinda()  {
		// Counter example: this doesn't get the name we actually want, but since it's rare, it doesn't matter.
		assertNames("Bill and Melinda Gates",
				Arrays.asList("Bill","and"),
				Arrays.asList("Melinda","Gates"));
	}
	
	
	/* 
	 * What follows is less systematic: instead, it's pulled from the real corpus and documents the current behavior.
	 * ... and in many cases the current behavior falls short of the ideal. It's all very fuzzy.
	 * 
	 * (There is also a lot of mis-encoded data in GBIF, 
	 * but I think my ingest machinery may handle that. So, no tests here.) */
	
	
	@Test
	public void testKuylenstierna()  {
		assertNames("Kuylenstierna, Nils / Kuylenstierna, Carl",
				Arrays.asList("Kuylenstierna"),
				Arrays.asList("Nils","Carl"));
	}
	
	@Test
	public void testAnonymous()  {
		assertNames("Anonymous collector s.n. [s.d.]",
				Arrays.asList("Anonymous","anonymous","collector"),
				Arrays.asList("s.n.","s.d."));
	}
	
	@Test
	public void testMJASimpson()  {
		assertNames("M.J.A. Simpson & A.P. Druce",
				Arrays.asList("Simpson"),
				Arrays.asList("Druce","M.J.A.","MJA","A.P.","AP"));
	}
	
	@Test
	public void testNodeler()  {
		assertNames("N\u00F6deler s.n. [1847-07]",
				Arrays.asList("N\u00F6deler","Nodeler"),
				Arrays.asList("1847","s.n"));
	}
	
	@Test
	public void testPolarstern()  {
		assertNames("R. Mooi and S. Lockhart aboard the \"PFS Polarstern\"",
				Arrays.asList("Mooi","and"), // TODO: Not doing any harm, but would we want to stop-word this?
				Arrays.asList("R","R.","Lockhart","Polarstern"));
	}
	
	@Test
	public void testMriPas()  {
		assertNames("MRI PAS",
				Arrays.asList("MRI","PAS","MRI PAS","mri","pas"));
	}
	
	@Test
	public void testCombiningCedilla()  {
		// TODO: I fabricated this example, but still good to note that we're not normalizing combined characters.
		assertNames("G. Malenc\u0327on",
				Arrays.asList("Malenc\u0327on"),
				Arrays.asList("Malencon","Malen\u00E7on"));
	}
	
	@Test
	public void testLongLast()  {
		assertNames("J. Duarte deSilva",
				Arrays.asList("Duarte","deSilva"));
	}
	
	@Test
	public void testMiddleInitial()  {
		assertNames("Magnus E. Fries",
				Arrays.asList("Magnus","Fries"));
	}
	
	@Test
	public void testVanDenHaute()  {
		assertNames("Jan Van Den Haute; Plantenwerkgroep Wielewaal Gent", 
				Arrays.asList("Jan","Haute"),
				Arrays.asList("Den","Van","Plantenwerkgroep"));
	}
	
	@Test
	public void testHarinHala()  {
		assertNames("R. Harin'Hala", 
				Arrays.asList("Harin","Hala","Harin'Hala"), 
				Arrays.asList("R"));
	}

	@Test
	public void testRV()  {
		assertNames("DFO R/V Lady Hammond", 
				Arrays.asList("DFO"), // 'Slash' is a breaking punctuation character, though that's not what it means here. 
				Arrays.asList("R/V","Lady","Hammond"));
	}

	@Test public void testQuotes()  {
		assertNames("\"Ouren, Inger; Ouren, Tore\"",
			Arrays.asList("Ouren"),
			Arrays.asList("Inger","Tore"));}	
	
	@Test
	public void testRavizza()  {
		assertNames("Ravizza Carlalberto, Ravizza-Dematteis Elisabetta", 
				Arrays.asList("Ravizza","Carlalberto"), 
				Arrays.asList("Dematteis","Elisabetta"));
	}

	@Test
	public void testDierlForsterSchacht()  {
		assertNames("Dierl-Forster-Schacht", 
				Arrays.asList("Dierl","Forster","Dierl-Forster"), 
				Arrays.asList("Schacht"));
	}

	@Test
	public void testDataHeld()  {
		assertNames("Data held by the BSBI", 
				Arrays.asList("Data","held"), 
				Arrays.asList("BSBI"));
	}

	@Test
	public void testMacKee() {
		assertNames("H.S. MacKee 37061", 
				Arrays.asList("MacKee"), 
				Arrays.asList("H.S.","McKee","37061"));
	}

	@Test
	public void testLLAMA() {
		assertNames("LLAMA", 
				Arrays.asList("LLAMA"));
	}

	@Test
	public void testBrunner() {
		assertNames("Brunner,J. s.n. [1937-08-25]", 
				Arrays.asList("Brunner","Brunner, J.","Brunner,J"), 
				Arrays.asList("J","1937"));
	}

	@Test
	public void testCollector() {
		assertNames("Collector(s): Bruce L. Christman, Sandie Kilpatrick", 
				Arrays.asList("Bruce", "Christman"), 
				Arrays.asList("Collector", "Kilpatrick"));
	}

	@Test
	public void testVlina() {
		assertNames("Vlina C9702", 
				Arrays.asList("Vlina"), 
				Arrays.asList("C9702"));
	}

	@Test
	public void testDrHermann() {
		assertNames("Dr. Hermann Poeverlein", 
				Arrays.asList("Hermann", "Poeverlein"),
				Arrays.asList("Dr."));
	}

	@Test
	public void testJeanMarie() {
		assertNames("Royer Jean-Marie, Didier Brnard", 
				Arrays.asList("Royer","Jean","Jean-Marie"), 
				Arrays.asList("Marie","Didier","Brnard"));
	}

	@Test
	public void testGOAnMalme() {
		// No idea what this means, but it was in the database.
		assertNames("G. O. A:n Malme", 
				Arrays.asList("Malme"), 
				Arrays.asList("G. O. A"));
	}

	@Test
	public void testKuckuck() {
		assertNames("Collector: Kuckuck, Paul", 
				Arrays.asList("Kuckuck"), 
				Arrays.asList("Collector","Paul"));
	}

	@Test
	public void testGibsonDr() {
		assertNames("Gibson, Dr.J.A.", 
				Arrays.asList("Gibson"), 
				Arrays.asList("J.A.","Dr"));
	}

	@Test
	public void testMeulenaar() {
		assertNames("Etienne De Meulenaar;Dirk De Roose", 
				Arrays.asList("Meulenaar","Etienne"), 
				Arrays.asList("Dirk","de"));
	}

	@Test
	public void testReptile() {
		assertNames("ITE reptile survey 1992 (Geraldine McGowan)", 
				Arrays.asList("ITE","reptile"), 
				Arrays.asList("survey","Geraldine","McGowan"));
	}

	@Test
	public void testPerTheodor() {
		assertNames("Per Theodor Cleve", 
				Arrays.asList("Per Theodor","Per","Theodor"), 
				Arrays.asList("Cleve"));
	}

	@Test
	public void testLintutieteellinen() {
		// No idea how this works as a name.
		assertNames("Porin Lintutieteellinen Yhdistys ry", 
				Arrays.asList("Porin Lintutieteellinen","Porin","Lintutieteellinen"), 
				Arrays.asList("Yhdistys","ry"));
	}

	@Test
	public void testVexin() {
		assertNames("P.N.R. du Vexin fran\u00E7ais", 
				Arrays.asList("vexin","fran\u00E7ais"), 
				Arrays.asList("du","P.N.R."));
	}

	@Test
	public void testFloresCrespo() {
		// Note that order of search terms doesn't matter.
		assertNames("J. Flores-Crespo", 
				Arrays.asList("Flores Crespo","Flores-Crespo","Crespo","Flores","Crespo-Flores","Crespo Flores"));
	}

}
