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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.filteredpush.dataentry.enums.Term;
import org.gbif.file.CSVReader;

public class GbifRecordsForIndexAndUpdate implements GenericRecordsForIndexAndUpdate<TupleSingleRecord> {

	// TODO: The download should be a DwC Archive, and the Archive
	// Reader code should be able to read it.
	// http://dev.gbif.org/issues/browse/PF-1426
	// ... but it's not, so for now, we fake it.

	// TODO: ... and when it is a real DwC Archive, revisit the handling of justUpdates,
	// and see what alternate, multiple-valued fields we want to try to extract.
	
	private File unzippedArchive;

	public GbifRecordsForIndexAndUpdate(File unzippedArchive) {
		this.unzippedArchive = unzippedArchive;
	}

	public Iterable<TupleSingleRecord> forIndex() {
		return new DownloadDwcTupleRecordIterable(unzippedArchive, false);
	}

	public Iterable<TupleSingleRecord> forUpdate() {
		return new DownloadDwcTupleRecordIterable(unzippedArchive, true);
	}

	private static class DownloadDwcTupleRecordIterable implements Iterable<TupleSingleRecord> {

		private Iterator<TermRecord> termRecordIterator;
		
		public DownloadDwcTupleRecordIterable(File unzippedArchive, boolean justUpdates) {
			Iterable<Map<String,String>> mapIterable = new MapIterable(unzippedArchive);
			Iterable<TermRecord> termRecordIterable = new DwcTermRecordIterable(mapIterable.iterator(), justUpdates);
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
		private boolean justUpdates;
		
		public DwcTermRecordIterable(Iterator<Map<String, String>> mapIterator, boolean justUpdates) {
			this.mapIterator = mapIterator;
			this.justUpdates = justUpdates;
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
						if (entry.getValue().length() > 0) { 
							// Are there cases where an empty value should be preserved in pulldown?
							String key = entry.getKey();
							try {
								// TODO: Reconsider this when GBIF gets the export fixed.
								// We might even want multiple update records generated.
	
								// Make it conform to DwC naming conventions:
								if (!key.contains("decimal_") && (key.contains("latitude") || key.contains("longitude"))) {
									key = key.replace("latitude", "decimal_latitude");
									key = key.replace("longitude", "decimal_longitude");
								}
								if (key.contains("_in_meters")) {
									key = key.replace("_in_meters", "");
								}
								if (key.equals("locality")) {
									key = "verbatim_" + key;
								}
	
								String encodingFixed = EncodingCorrection.FIX_A_TILDE.correct(entry.getValue());
	
								if (justUpdates) {
									if (key.startsWith("verbatim_")) {
										key = key.replace("verbatim_", "");
										Term term = Term.valueOf(key);
										record.put(term, encodingFixed);
									} else {
										// ignore: we are only interested in
										// verbatim_* fields for updates.
									}
								} else {
									Term term = Term.valueOf(key);
									record.put(term, encodingFixed);
								}
							} catch (IllegalArgumentException e) {
								// ignore and skip
							}
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
		
		public MapIterable(File unzippedArchive) {
			CSVReader reader;
			try {
				File csv = new File(unzippedArchive, "occurrence.txt");
				reader = CSVReader.build(csv);
			} catch (IOException e) {
				throw new Error(e);
			}
			header = Arrays.asList(reader.header);
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
						map.put(field, strings[i]);
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
