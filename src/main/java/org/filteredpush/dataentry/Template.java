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

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Template {

	private final String template;
	
	public Template(String template) {
		this.template = template;
	}
	
	private static void confirmNotTemplate(String notTemplate, String message) {
		Pattern pattern = Pattern.compile(".*(\\$\\{[^}]*\\}?).*");
		Matcher matcher = pattern.matcher(notTemplate);
		if (matcher.matches()) {
			throw new TemplateException(message+": "+matcher.group(1));
		}
	}
	
	public String apply(Map<String,String> map) {
		String inProgress = template;
		
		for (Entry<String,String> entry : map.entrySet()) {
			if (!entry.getKey().matches("^\\w+$")) {
				throw new TemplateException("Key not of correct format: "+entry.getKey());
			}
			confirmNotTemplate(entry.getValue(), "Template replacement contains prohibited pattern");
			inProgress = inProgress.replaceAll("\\$\\{"+entry.getKey()+"\\}", Matcher.quoteReplacement(entry.getValue()));
		}
		confirmNotTemplate(inProgress, "Unmatched pattern in template");
		return inProgress;
	}
	
	@SuppressWarnings("serial")
	public static class TemplateException extends Error{
		public TemplateException(String message) {
			super(message);
		}
	}

}
