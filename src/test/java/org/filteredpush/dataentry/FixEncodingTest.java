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

import org.filteredpush.dataentry.backend.EncodingCorrection;
import org.junit.Test;

public class FixEncodingTest {

	private static void assertEncodingFixed(String input, String expected) {
		assertThat(EncodingCorrection.FIX_A_TILDE.correct(input)).isEqualTo(expected);
	}
	
	@Test
	public void testSimple() {
		String input = "Simple things should be simple.";
		assertEncodingFixed(input,  input);
	}
	
	@Test
	public void testPortuguese() {
		String input = "'S\u00C3O PAULO' is legit.";
		assertEncodingFixed(input,  input);
	}
	
	@Test
	public void testWeirderButOk() {
		String input = "A '\u2603' would melt in 'S\u00C3O PAULO'.";
		assertEncodingFixed(input,  input);
	}
	
	// Test helper:
	//   perl -e 'binmode STDIN, ":utf8"; while(<>){chomp; print map {/[[:ascii:]]/ ? $_ : sprintf("\\u%04x",ord($_))} split //, $_}'
	// Type a string, get the java representation.
	
	@Test
	public void testMisencoded() {
		// from http://dev.gbif.org/issues/browse/PF-1430
		String input = "M\u00c3\u00b6rbyl\u00c3\u00a5nga";
		String expected = "M\u00f6rbyl\u00e5nga";
		assertEncodingFixed(input,expected);
	}
	
	@Test
	public void testRecodingFails() {
		// from http://dev.gbif.org/issues/browse/PF-1430
		String input = "Stray '\u00c3' prevents recoding of 'M\u00c3\u00b6rbyl\u00c3\u00a5nga'?";
		assertEncodingFixed(input,input);
	}
	
	@Test
	public void testBadButNotHandled() {
		String input = "Is there a \u2603 in M\u00c3\u00b6rbyl\u00c3\u00a5nga?";
		// We have to assume the weird characters are right, because the lone snowman can't be a misencoding artifact.
		assertEncodingFixed(input,  input);
	}
	

}
