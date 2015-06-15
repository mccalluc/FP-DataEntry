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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.common.SolrInputDocument;
import org.filteredpush.dataentry.Json;
import org.filteredpush.dataentry.backend.solr.UpdatingSolrServer;
import org.filteredpush.dataentry.enums.Term;
import org.filteredpush.dataentry.enums.Tuple;

import com.fasterxml.jackson.annotation.JsonValue;

//@JsonSerialize(using = DwcTupleRecord.Serializer.class)
public class TupleSingleRecord implements GenericTupleSingleRecord<Tuple> {

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TupleSingleRecord other = (TupleSingleRecord) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}


	private Map<Tuple, List<String>> map;

	private TupleSingleRecord() {
		map = new HashMap<Tuple, List<String>>();
	}
	
	public String toString() {
		return map.toString();
	}
	
	@JsonValue
	public Map<Tuple, List<String>> getMap() {
		return map;
	}
	
	/*-------------
	   Factories
	--------------*/
	
	public static TupleSingleRecord fromTermRecord(TermRecord termRecord) {
		TupleSingleRecord tupleRecord = new TupleSingleRecord();
		for (Tuple tuple : Tuple.values()) {
			List<String> values = new ArrayList<String>();
			boolean containsTerm = false;
			for (Term term : tuple.getTerms()) {
				containsTerm = containsTerm || termRecord.containsKey(term);
				values.add(termRecord.get(term));
				// Include all values, even nulls, because they correspond to the elements of the tuple.
			}
			if (containsTerm) {
				// ... but skip if there are no values.
				tupleRecord.map.put(tuple, values);
			}
		}
		return tupleRecord;
	}

	/*-------------
	   Transform
	--------------*/
	
	public SolrInputDocument asSolr(String dataSetId, int rowNumber) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField(UpdatingSolrServer.ID, dataSetId + ":" + rowNumber);
		for (Entry<Tuple,List<String>> entry : map.entrySet()) {
			String key = entry.getKey().toString();
			if (UpdatingSolrServer.ID.equals(key)) {
				throw new Error("No value should be explicitly set for " + UpdatingSolrServer.ID);
			}
			List<String> value = entry.getValue();
			doc.setField(key, serialize(value));
			// TODO: would this be good?
			//	if (value.size() > 0) { ... }
		}
		return doc;
	}
	
	
	/*----------------
	   Serialization
	------------------*/	
	
	private static String serialize(List<String> strings) {
		return Json.toUTF8(strings);
	}

}
