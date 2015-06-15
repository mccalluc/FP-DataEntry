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
import java.util.HashMap;
import java.util.Map;

import org.filteredpush.dataentry.backend.solr.SolrIndexer;
import org.filteredpush.dataentry.backend.solr.SolrInstaller;

public class Main {

	//private static Logger log = LoggerFactory.getLogger(Main.class);

	private Main() {}
	
	private static File copyDemoConfigurationToDisk() {
		String archiveContent = Utils.readResource("back-end/demo-dwc-download.txt");
		String archivePath = Utils.saveToDisk(archiveContent, "archive-", ".txt").toString();
		
		Map<String,String> model = new HashMap<String,String>();
		model.put("solr", Utils.createTempDir("solr-").toString());
		model.put("archive", archivePath);

		String configTemplate = Utils.readResource("demo-configuration.xml");
		String configXml = new Template(configTemplate).apply(model);
		return Utils.saveToDisk(configXml, "config-", ".xml");
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length > 1) {
			throw new Error("One optional argument, a config file, is allowed. Instead there were "+args.length);
		}
		String config = args.length == 1
				? args[0]
				: copyDemoConfigurationToDisk().toString();
		
		SolrInstaller.main(config);
		SolrIndexer.main(config);
		BothEndsHandler.main(config);
	}

}
