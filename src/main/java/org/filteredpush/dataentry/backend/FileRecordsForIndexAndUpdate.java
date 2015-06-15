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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.filteredpush.dataentry.enums.Term;
import org.gbif.file.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRecordsForIndexAndUpdate implements GenericRecordsForIndexAndUpdate<TupleSingleRecord> {
	
	private static Logger log = LoggerFactory.getLogger(FileRecordsForIndexAndUpdate.class);
	
	private Iterable<TupleSingleRecord> forIndex;
	private Iterable<TupleSingleRecord> forUpdate;

	public FileRecordsForIndexAndUpdate(File file, Charset encoding, EncodingCorrection correction, String delimiter, Character quotes, String countsAsNull, Map<String,String> fieldMap) {
		// Would be nice to accept something more abstract than File, but that's what the underlying CSVReader wants.
		Map<String,String> fieldMapForIndex = new HashMap<String,String>();
		Map<String,String> fieldMapForUpdate = new HashMap<String,String>();
		for (Entry<String,String> entry : fieldMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (!fieldMapForIndex.containsValue(value)) {
				fieldMapForIndex.put(key, value);
			} else if (!fieldMapForUpdate.containsValue(value)) {
				fieldMapForUpdate.put(key, value);
			} else {
				throw new Error("You're trying to map three separate columns from the file to "+entry.getValue()+". That could be possible, but it is a TODO.");
			}
		}
		
		forIndex  = new DwcTupleRecordIterable(file, encoding, correction, delimiter, quotes, countsAsNull, fieldMapForIndex,  true);
		forUpdate = new DwcTupleRecordIterable(file, encoding, correction, delimiter, quotes, countsAsNull, fieldMapForUpdate, false);
		// If fieldMapForUpdate.size() < 1, new ArrayList<TupleSingleRecord>() would suffice,
		// but better to avoid special cases.
	}

	public Iterable<TupleSingleRecord> forIndex() {
		return forIndex;
	}

	public Iterable<TupleSingleRecord> forUpdate() {
		return forUpdate;
	}

	private static class DwcTupleRecordIterable implements Iterable<TupleSingleRecord> {

		private Iterator<TermRecord> termRecordIterator;
		
		public DwcTupleRecordIterable(File file, Charset encoding, EncodingCorrection correction, String delimiter, Character quotes, String countsAsNull, Map<String,String> fieldMap, boolean doImplicitMaps) {
			Iterable<TermRecord> termRecordIterable = new DwcTermRecordIterable(file, encoding, correction, delimiter, quotes, countsAsNull, fieldMap, doImplicitMaps);
			termRecordIterator = termRecordIterable.iterator();
		}

		public Iterator<TupleSingleRecord> iterator() {

			return new Iterator<TupleSingleRecord>() {

				public boolean hasNext() {
					return termRecordIterator.hasNext();
				}

				public TupleSingleRecord next() {
					TermRecord record = termRecordIterator.next();
					return TupleSingleRecord.fromTermRecord(record);
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}

			};
		}

	}
	
	private static class DwcTermRecordIterable implements Iterable<TermRecord> {
		
		private Iterator<Map<String, String>> mapIterator;
		private Map<String,String> fieldMap;
		private boolean doImplicitMaps;
			
		public DwcTermRecordIterable(File file, Charset encoding, EncodingCorrection correction, String delimiter, Character quotes, String countsAsNull, Map<String,String> fieldMap, boolean doImplicitMaps) {
			
			Set<String> allowed = Term.asStringSet();
			allowed.add(null);
			for (String target : fieldMap.values()) {
				if (!allowed.contains(target)) {
					// TODO: sort the list so it's easier to read?
					throw new IllegalArgumentException("Right-hand-side of map contains unrecognized value: '"+target+"'. These are the allowed values: "+allowed);
				}
			}
			
			this.mapIterator = new MapIterable(file, encoding, correction, delimiter, quotes, countsAsNull, fieldMap.keySet()).iterator();
			this.fieldMap = fieldMap;
			this.doImplicitMaps = doImplicitMaps;
		}

		public Iterator<TermRecord> iterator() {
			
			return new Iterator<TermRecord>() {
				public boolean hasNext() {
					return mapIterator.hasNext();
				}
	
				public TermRecord next() {
					Map<String, String> map = mapIterator.next();
					TermRecord record = new TermRecord();
					for (Entry<String, String> entry : map.entrySet()) {
						String mapped;
						if (fieldMap.containsKey(entry.getKey())) {
							mapped = fieldMap.get(entry.getKey());
							if (mapped == null) {
								continue; // ie, explicitly drop this value.
							}
						} else if (doImplicitMaps) {
							mapped = entry.getKey();
						} else {
							continue;
						}
						
						Term term;
						try {
							term = Term.valueOf(mapped);
						} catch (IllegalArgumentException e) {
							// Not a term we map: perfectly ok, and not unusual if there were a lot of columns.
							term = null;
						}

						if (term != null && entry.getValue() != null) {
							record.put(term, entry.getValue());
						}
					}
					return record;
				}
	
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
	}
	
	private static class MapIterable implements Iterable<Map<String,String>> {
		
		final List<String> header;
		final Iterator<String[]> iterator;
		final String countsAsNull;
		final EncodingCorrection correction;
		
		public MapIterable(File file, Charset encoding, EncodingCorrection correction, String delimiter, Character quotes, String countsAsNull, Set<String> expectedHeaders) {
			this.countsAsNull = countsAsNull;
			this.correction = correction;
			CSVReader reader;
			try {
				reader = CSVReader.build(file, encoding.name(), delimiter, quotes, 1);
			} catch (IOException e) {
				throw new Error("Error reading "+file,e);
			}
			header = Arrays.asList(reader.header);
			for (String expected : expectedHeaders) {
				if (!header.contains(expected)) {
					throw new IllegalArgumentException("Left-hand-side of map contains unrecognized value: '"+expected+"'. These are the allowed values: "+header);
				}
			}
			iterator = reader.iterator();
		}

		public Iterator<Map<String, String>> iterator() {
			return new Iterator<Map<String, String>>() {
				public boolean hasNext() {
					return iterator.hasNext();
				}
				public Map<String, String> next() {
					String[] strings = iterator.next();
					int i = 0;
					Map<String, String> map = new HashMap<String, String>();
					for (String field : header) {
						if (strings.length <= i) {
							log.warn("Unexpected short line: processing will continue on next line: "+Arrays.asList(strings));
							break;
						}
						if (!strings[i].equals(countsAsNull)) {
							map.put(field, correction.correct(strings[i]));
						}
						i++;
					}
					return map;
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
	}

}
