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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.filteredpush.dataentry.Json;

public class Tuple implements GenericTuple<Term> {

	private Tuple(String name, Term... terms) {
		this.name = name;
		this.terms = Arrays.asList(terms);
	}
	
	private final String name;

	private List<Term> terms;
	
	public String toString() {
		return name;
	}
	
	public List<Term> getTerms() {
		return terms;
	}
	
	public static String toJson() {
		List<Map<Tuple,List<String>>> pairList = new ArrayList<Map<Tuple,List<String>>>();
		// We want stable ordering (because this defines the UI ordering); otherwise, this could just be a map.
		for (Tuple value : Tuple.values()) {
			Map<Tuple,List<String>> pair = new HashMap<Tuple,List<String>>(); 
			List<String> list = new ArrayList<String>();
			for (Term term : value.getTerms()) {
				list.add(term.toString());
			}
			pair.put(value, list);
			pairList.add(pair);
		}
		return Json.toEscaped(pairList);
	}
	
	public String extractForLog(SolrInputDocument doc) {
		// TODO: This made more sense when the document structure was hard-coded,
		// so we knew which fields to extract. Think about a better toString now that we don't...
		
		// If this is used for anything other than logging, rename, and firm up the contract.
		// (Representing null with "" could be sketchy.)
		SolrInputField field = doc.getField(this.toString());
		if (field == null) {
			return "";
		} else {
			return doc.getField(this.toString()).toString();
		}
	}
	
	/* static */
	
	private static LinkedHashMap<String,Tuple> members;
	
	private static void confirmInit() {
		if (members == null) {
			throw new Error("Not yet initialized");
		}
	}
	
	public static void init(final Map<String, List<String>> map) {
		if (members != null) {
			throw new Error("Already initialized");
		}
		members = new LinkedHashMap<String, Tuple>();
		for (Entry<String, List<String>> entry : map.entrySet()) {
			String name = entry.getKey();
			Term[] terms = new Term[entry.getValue().size()];
			int i = 0;
			for (String term : entry.getValue()) {
				terms[i] = Term.valueOf(term);
				i++;
			}
			members.put(name, new Tuple(name, terms));
		}
	}
	
	static void clear() {
		members = null;
	}
	
	public static Tuple valueOf(String name) {
		confirmInit();
		Tuple tuple = members.get(name);
		if (tuple == null) {
			throw new IllegalArgumentException("'"+name+"' is not a member of the enumeration.");
		} else {
			return members.get(name);
		}
	}
	
	public static Set<String> asStringSet() {
		confirmInit();
		Set<String> set = new HashSet<String>();
		for (Tuple tuple : members.values()) {
			set.add(tuple.toString());
		}
		return set;
	}
	
	public static Tuple[] values() {
		confirmInit();
		Collection<Tuple> values = members.values();
		return values.toArray(new Tuple[values.size()]);
	}
}
