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
package org.filteredpush.dataentry.backend;

import java.util.HashMap;
import java.util.Map;

import org.filteredpush.dataentry.enums.Term;

public class TermRecord implements GenericTermRecord<Term> {

	private Map<Term, String> map;

	public TermRecord() {
		// Outside this class, constructor should only be used in tests.
		map = new HashMap<Term, String>();
	}
	
	public String toString() {
		return map.toString();
	}
	
	/*-------------
	   Parts of a map interface:
	   We can actually have a tighter interface than a normal Map.
	--------------*/
	
	public boolean containsKey(Term term) {
		return map.containsKey(term);
	}
	
	public String get(Term term) {
		return map.get(term);
	}
	
	public void put(Term term, String string) {
		map.put(term,string);
	}
	
}
