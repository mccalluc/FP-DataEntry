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

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.filteredpush.dataentry.Constants;
import org.filteredpush.dataentry.Utils;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.filteredpush.dataentry.enums.Term;
import org.filteredpush.dataentry.enums.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GbifDownloadReaderTest {
	
	@Before
	public void init() {
		EnumUtils.clearEnums();
		Term.init(Constants.TERMS_VALUE);
		Tuple.init(Constants.TUPLES_MAP_VALUE);
	}
	
	@After
	public void quit() {
		EnumUtils.clearEnums();
	}
	
	@Test
	public void testGbifDownloadReader() {
		URI zipUri;
		try {
			zipUri = GbifDownloadReaderTest.class.getResource("/small-dwc-download.zip").toURI();
		} catch (URISyntaxException e) {
			throw new Error(e);
		}
		File downloadedZip = new File(zipUri);
		assertThat(downloadedZip).isFile();
		File unzipped = Utils.unzipToTemp(downloadedZip);
		assertThat(unzipped).isDirectory();
		
		GenericRecordsForIndexAndUpdate<TupleSingleRecord> records = new GbifRecordsForIndexAndUpdate(unzipped);
		
		// Extract for index:
		
		Iterable<TupleSingleRecord> forIndex = records.forIndex();
		List<TupleSingleRecord> indexList = new ArrayList<TupleSingleRecord>();
		for (TupleSingleRecord record : forIndex) {
			indexList.add(record);
		}
		
		assertThat(indexList).hasSize(19);
		Map<Tuple, List<String>> indexMap = indexList.get(5).getMap();
		assertThat(indexMap.get(Tuple.valueOf("kingdom")).get(0)).isEqualTo("Fungi");
		assertThat(indexMap.get(Tuple.valueOf("family")).get(0)).isEqualTo("Pertusariaceae");
		
		// Extract for update:
		
		Iterable<TupleSingleRecord> forUpdate = records.forUpdate();
		List<TupleSingleRecord> updateList = new ArrayList<TupleSingleRecord>();
		for (TupleSingleRecord record : forUpdate) {
			updateList.add(record);
		}
		
		assertThat(updateList).hasSize(19);
		Map<Tuple, List<String>> updateMap = updateList.get(5).getMap();
		assertThat(updateMap.get(Tuple.valueOf("kingdom"))).isNull();
		assertThat(updateMap.get(Tuple.valueOf("family")).get(0)).isEqualTo("Lichenes"); // note: different from the indexList.
	}

}
