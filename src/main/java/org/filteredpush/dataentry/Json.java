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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Json {
	
	private static ObjectMapper mapper = new ObjectMapper();

	private Json() {}
	
	public static Object from(String json) {
		try {
			return mapper.readValue(json, Object.class);
		} catch (JsonParseException e) {
			throw new JsonException(e);
		} catch (JsonMappingException e) {
			throw new JsonException(e);
		} catch (IOException e) {
			throw new JsonException(e);
		}
	}
	
	public static String toEscaped(Object obj) {
		return to(obj,true);
	}
	
	public static String toUTF8(Object obj) {
		return to(obj,false);
	}
	
	private static String to(Object obj, boolean escape) {
		try {
			OutputStream output = new ByteArrayOutputStream();
			JsonGenerator generator = new JsonFactory().createGenerator(output);
			generator.setCodec(new ObjectMapper());
			if (escape) {
				// Perhaps better to get to the root of the character encoding bugs... but this works.
				generator.setHighestNonEscapedChar(127);
			}
			generator.writeObject(obj);
			return output.toString();
		} catch (IOException e) {
			throw new JsonException(e);
		}
	}
	
	@SuppressWarnings("serial")
	public static class JsonException extends Error {
		public JsonException(Exception e) {
			super(e);
		}
	}

}
