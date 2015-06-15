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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class QParameter {

	private String name;
	
	private String solrField;
	
	private QParameter(String name, String solrField) {
		this.name = name;
		this.solrField = solrField;
	}
	
	public String toString() {
		return name;
	}
	
	public String getSolrField() {
		return solrField;
	}
	
	/* static */

	private static LinkedHashMap<String,QParameter> members;
	
	private static void confirmInit() {
		if (members == null) {
			throw new Error("Not yet initialized");
		}
	}
	
	// TODO: to remove "public", fix tests
	public static void init(Map<String, String> map) { // Should be given an instance with stable order; not sure how that would be expressed in signature.
		if (members != null) {
			throw new Error("Already initialized");
		}
		members = new LinkedHashMap<String, QParameter>();
		for (Map.Entry<String,String> entry : map.entrySet()) {
			members.put(entry.getKey(),new QParameter(entry.getKey(),entry.getValue()));
		}
	}
	
	static void clear() {
		members = null; // TODO: kind of ugly: Can't think of a reason this would be used outside tests.
	}
	
	public static Set<String> toStringSet() {
		confirmInit();
		ImmutableSet.Builder<String> builder = ImmutableSet.builder();
		for (QParameter member : QParameter.members.values()) {
			builder.add(member.toString());
		}
		return builder.build();
	}
	
	public static Map<String,String> apply(Map<String,String[]> urlParams) {
		confirmInit();
		Map<String,String> solrMap = new HashMap<String,String>();
		for (Map.Entry<String,String[]> urlParam : urlParams.entrySet()) {
			QParameter target = QParameter.members.get(urlParam.getKey());
			if (target != null) {
				String solrField = target.getSolrField();
				String[] values = urlParam.getValue();
				if (values.length != 1) {
					throw new Error("Parameters must be single-valued. Instead, '"+solrField+"' has "+values.length+" values.");
				}
				String value = urlParam.getValue()[0];
				solrMap.put(solrField, value);
			}
		}
		return solrMap;
	}
	
}