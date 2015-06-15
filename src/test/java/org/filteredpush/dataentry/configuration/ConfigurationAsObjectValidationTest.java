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
package org.filteredpush.dataentry.configuration;

import java.util.List;
import java.util.Map;

import org.filteredpush.dataentry.Constants;

public class ConfigurationAsObjectValidationTest {
	
	// TODO: some actual tests

	@SuppressWarnings("unused")
	private static class MockConfiguration extends Configuration {
		public MockConfiguration() {
			super(null);
			// We override all the getters, and since the getters usually have the job of parsing the doc, 
			// it's good for it to be null here. If we get an NPE, it really is a hole we want to know about.
		}
		@Override
		public int getPort() {
			return 8080;
		}
		@Override
		public List<String> getTerms() {
			return Constants.TERMS_VALUE;
		}
		@Override
		public Map<String, List<String>> getTuplesMap() {
			return Constants.TUPLES_MAP_VALUE;
		}

		@Override
		public Map<String, String> getQSolr() {
			return null;
		}

		@Override
		public Map<String, String> getQNames() {
			return null;
		}

		@Override
		public Map<String, String> getInputDefaults() {
			return null;
		}

		@Override
		public Map<String, String> getInputLabels() {
			return null;
		}

		@Override
		public List<String> getSuggestionFields() {
			return null;
		}

		@Override
		public Map<String, Map<String, String>> getFakeAjaxData() {
			return null;
		}
		@Override
		public String getTitleHtml() {
			return null;
		}
		@Override
		public String getBlurbHtml() {
			return null;
		}
		
	}
	
}
