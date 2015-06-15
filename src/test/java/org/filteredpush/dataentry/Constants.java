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
package org.filteredpush.dataentry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface Constants {
	
	public static final List<String> TERMS_VALUE = Arrays.asList(
			"recordNumber", "recordedBy", "typeStatus", //
			"country", "countryCode", "stateProvince", "county", "municipality", "verbatimLocality", //
			"decimalLatitude", "decimalLongitude", //
			"eventDate", "habitat", "minimumElevationInMeters", "maximumElevationInMeters", //
			"kingdom", "phylum", "class", "order", "family", "scientificName", "scientificNameAuthorship", //
			"exsiccateTitle", "exsiccateNumber");
	
	public static final List<String> Q_NAMES_VALUE = Arrays.asList( //
			"taxon", "my_name", //
			"collector", "my_collector", //
			"number", "my_number", //
			"date", "my_date", //
			"geography", "my_country");
	
	@SuppressWarnings("serial")
	public static final Map<String,String> Q_SOLR_VALUE = new HashMap<String,String>(){{
		this.put("collector", "recordedBy");
		this.put("number", "recordNumber");
		this.put("taxon", "taxon_index");
		this.put("geography", "geography_index");
		this.put("date", "eventDate");
		this.put("exsiccate_title", "exsiccateTitle");
		this.put("exsiccate_number", "exsiccateNumber");
	}};
			
	public static final List<String> TUPLES_VALUE = Arrays.asList( //
			"recordNumber", "recordedBy", //
			"typeStatus", "country", //
			"stateCountyCity", "verbatimLocality", "latitudeLongitude", //
			"eventDate", "habitat", "elevationInMeters", //
			"kingdom", "phylum", "class", "order", "family", "scientificName", //
			"exsiccateNumber", "exsiccateTitle");
	
	@SuppressWarnings("serial")
	public static final Map<String,String> FIELD_MAP = new HashMap<String,String>(){{
		this.put("locality","verbatimLocality");
		this.put("exsnumber","exsiccateNumber");
		this.put("huh_title","exsiccateTitle");
	}};
	
	@SuppressWarnings("serial")
	public static final Map<String,List<String>> TUPLES_MAP_VALUE = new HashMap<String,List<String>>(){{
		this.put("recordNumber", Arrays.asList("recordNumber"));
		this.put("recordedBy", Arrays.asList("recordedBy"));
		
		this.put("typeStatus", Arrays.asList("typeStatus"));
		
		// clumping of geography is kind of arbitrary...
		this.put("country", Arrays.asList("countryCode", "country"));
		this.put("stateCountyCity", Arrays.asList("stateProvince", "county", "municipality"));
		this.put("verbatimLocality", Arrays.asList("verbatimLocality"));
		this.put("latitudeLongitude", Arrays.asList("decimalLatitude", "decimalLongitude"));
		
		this.put("eventDate", Arrays.asList("eventDate"));
		this.put("habitat", Arrays.asList("habitat"));
		this.put("elevationInMeters", Arrays.asList("minimumElevationInMeters", "maximumElevationInMeters"));
		
		this.put("kingdom", Arrays.asList("kingdom"));
		this.put("phylum", Arrays.asList("phylum"));
		this.put("class", Arrays.asList("class"));
		this.put("order", Arrays.asList("order"));
		this.put("family", Arrays.asList("family"));
		this.put("scientificName", Arrays.asList("scientificName", "scientificNameAuthorship"));
				
		this.put("exsiccateNumber", Arrays.asList("exsiccateNumber"));
		this.put("exsiccateTitle", Arrays.asList("exsiccateTitle"));
	}};
	

}