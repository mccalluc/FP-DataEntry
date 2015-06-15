package org.filteredpush.dataentry.backend;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.filteredpush.dataentry.configuration.EnumConfiguration;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.filteredpush.dataentry.enums.Term;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

public class FileRecordsTest {

	@BeforeClass
	public static void init() {
		EnumUtils.setEnums(new EnumConfiguration() {
			@Override
			public Map<String, List<String>> getTuplesMap() {
				Map<String, List<String>> map = new HashMap<String, List<String>>();
				for (String term : getTerms()) {
					map.put(term, Arrays.asList(term));
				}
				return map;
			}
			
			@Override
			public List<String> getTerms() {
				return Arrays.asList("first","last");
			}
			
			@Override
			public Map<String, String> getQSolr() {
				return new HashMap<String, String>();
			}
		});
	}
	
	@AfterClass
	public static void quit() {
		EnumUtils.clearEnums();
	}
	
	
	@Test
	public void testSingleMap() throws IOException {
		File file = createFile(
				"first%%last", //
				"Chuck%%M\u00c3\u00b6rbyl\u00c3\u00a5nga", //
				"NOTHING%%!JustLast!", //
				"!JustFirst!%%NOTHING", //
				"NOTHING%%NOTHING");
		Map<String,String> map = new ImmutableMap.Builder<String,String>()
				.put("first", "first")
				.put("last", "last")
				.build();
		FileRecordsForIndexAndUpdate records = new FileRecordsForIndexAndUpdate( //
				file, Charsets.UTF_8, EncodingCorrection.FIX_A_TILDE, "%%", '!', "NOTHING", map);
		
		List<TupleSingleRecord> expected = new TupleSingleRecordListBuilder() //
			.add("first","Chuck","last","M\u00f6rbyl\u00e5nga") //
			.add("last","JustLast") //
			.add("first","JustFirst") //
			.add() //
			.build();
		
		assertEqual(records.forIndex(), expected);
		assertEqual(records.forUpdate(), new TupleSingleRecordListBuilder().add().add().add().add().build()); // Not sure that I like this behavior, but it doesn't hurt.
	}
	
	@Test
	public void testDoubleMap() throws IOException {
		File file = createFile(
				"first|middle|last", //
				"Chuck|Clem|McCallum");
		Map<String,String> map = new ImmutableMap.Builder<String,String>()
				.put("first", "first")
				.put("middle","first")
				.put("last", "last")
				.build();
		FileRecordsForIndexAndUpdate records = new FileRecordsForIndexAndUpdate( //
				file, Charsets.UTF_8, EncodingCorrection.NONE, "|", null, null, map);
		
		List<TupleSingleRecord> expectedForIndex = new TupleSingleRecordListBuilder() //
			.add("first","Chuck","last","McCallum") //
			.build();
		List<TupleSingleRecord> expectedForUpdate = new TupleSingleRecordListBuilder() //
			.add("first","Clem")
			.build();
		
		assertEqual(records.forIndex(), expectedForIndex);
		assertEqual(records.forUpdate(), expectedForUpdate);
	}
	
	@Test
	public void testMultiMap() throws IOException {
		File file = createFile(
				"1|2|3|last", //
				"John|Ronald|Reuel|Tolkien", //
				"Carl|Philipp|Emanuel|Bach");
		Map<String,String> map = new ImmutableMap.Builder<String,String>()
				.put("1", "first")
				.put("2","first")
				.put("3","first")
				.put("last", "last")
				.build();
		try {
			// FileRecordsForIndexAndUpdate records = 
			new FileRecordsForIndexAndUpdate( //
				file, Charsets.UTF_8, EncodingCorrection.NONE, "|", null, null, map);
		} catch (Error e) {
			return; // TODO: fix this!
		}
		Assert.fail("Expected Error");
		
//		List<TupleSingleRecord> expectedForIndex = new TupleSingleRecordListBuilder() //
//			.add("first","Chuck","last","McCallum") //
//			.build();
//		List<TupleSingleRecord> expectedForUpdate = new TupleSingleRecordListBuilder() //
//			.add("first","Clem")
//			.build();
//		
//		assertEqual(records.forIndex(), expectedForIndex);
//		assertEqual(records.forUpdate(), expectedForUpdate);
//		records.forUpdate();
	}
	
	private static class TupleSingleRecordListBuilder {
		private List<TupleSingleRecord> list = new ArrayList<TupleSingleRecord>();
		public TupleSingleRecordListBuilder add(String... keyValues) {
			if (keyValues.length % 2 != 0) {
				throw new Error("Argument list must have even length; this didn't: "+Arrays.asList(keyValues));
			}
			TermRecord record = new TermRecord();
			for (int i = 0; i < keyValues.length; i += 2) {
				record.put(Term.valueOf(keyValues[i]), keyValues[i+1]);
			}
			list.add(TupleSingleRecord.fromTermRecord(record));
			return this;
		}
		public List<TupleSingleRecord> build() {
			return list;
		}
	}

	private static File createFile(String... lines) {
		try {
			File file = File.createTempFile("test", "txt");
			OutputStream stream = new FileOutputStream(file);
			IOUtils.write(StringUtils.join(lines, "\n"),stream);
			stream.close();
			return file;
		} catch (FileNotFoundException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	private static void assertEqual(Iterable<TupleSingleRecord> actual, List<TupleSingleRecord> expected) {
		List<TupleSingleRecord> actualList = new ArrayList<TupleSingleRecord>(); 
		for (TupleSingleRecord actualRecord : actual) {
			actualList.add(actualRecord);
		}
		assertThat(actualList).isEqualTo(expected);
	}
}
