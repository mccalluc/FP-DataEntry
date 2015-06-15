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
package org.filteredpush.dataentry.enums;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class Term implements GenericTerm {
	
	public static final String ID_SUFFIX = "_id";
	
	private Term(String name) {
	    this.name = name;
	}
	
	private final String name;
	
	public String toString() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// auto-generated
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Term other = (Term) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	/* static */
	
	private static LinkedHashMap<String,Term> members;
	
	private static void confirmInit() {
		if (members == null) {
			throw new Error("Not yet initialized");
		}
	}
	
	// TODO: to remove "public", fix tests
	public static void init(List<String> terms) {
		if (members != null) {
			throw new Error("Already initialized");
		}
		members = new LinkedHashMap<String,Term>();
		for (String term : terms) {
			members.put(term,new Term(term));
		}
	}
	
	static void clear() {
		members = null;
	}
	
	public static Term valueOf(String name) {
		confirmInit();
		Term term = members.get(name);
		if (term == null) {
			throw new IllegalArgumentException("'"+name+"' is not a member of the enumeration.");
		} else {
			return members.get(name);
		}
	}
	
	public static Term[] values() {
		confirmInit();
		Collection<Term> values = members.values();
		return values.toArray(new Term[values.size()]);
	}
	
	public static Set<String> asStringSet() {
		confirmInit();
		Set<String> set = new HashSet<String>();
		for (Term term : Term.values()) {
			set.add(term.toString());
		}
		return set;
	}
	
	public static Set<String> asStringSetPlusIds() {
		confirmInit();
		Set<String> set = new HashSet<String>();
		for (String term : asStringSet()) {
			set.add(term);
			set.add(term + ID_SUFFIX);
		}
		return set;
	}
}
