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

import java.io.File;

import org.filteredpush.dataentry.configuration.Configuration;

public class CliOptionsParser {

	//private static Logger log = LoggerFactory.getLogger(BackEndHandler.class);
	
	private final String about;
	
	public CliOptionsParser() {
		this("(No documentation provided.)");
	}
	
	public CliOptionsParser(String about) {
		this.about = about;
	}
	
	public Configuration parse(String[] args) {
		if (args.length != 1) {
			throw new Error("Expects a single parameter giving the path for a configuration file");
		}
		File configFile = new File(args[0]);
		return new Configuration(Utils.parseXml(configFile));
	}
	
	public Configuration parseOrExit(String[] args) {
		try {
			return parse(args);
		} catch (Error e) {
			throw new Error(this.about, e);
		}
	}

}
