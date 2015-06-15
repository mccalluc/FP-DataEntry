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
package org.filteredpush.dataentry.backend.solr;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.filteredpush.dataentry.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdatingSolrServer extends SolrServer {

	private static final long serialVersionUID = 1L;
	private final EmbeddedSolrServer server;
	
	public static final String ID = "_fp_internal_id"; // This string must not be used as a column header, and must be specify in each schema.xml.
	
	private static Logger log = LoggerFactory.getLogger(UpdatingSolrServer.class);
	
	public UpdatingSolrServer(EmbeddedSolrServer server) {
		this.server = server;
	}
	
	public void update(SolrInputDocument doc) {
		/*
		 * In version 4, in the XML interface, Solr added support for updating records...
		 * but it's really just sugar: the underlying Lucene index is write-once.
		 * 
		 * It's not available in the SolrJ API, but this is good enough:
		 */
		
		// doc should only contain fields which can be multi-valued (except for ID).
		String id = (String) doc.get(ID).getValue();
		try {
			SolrDocument oldDoc = Utils.query(server, ID, id).get(0);
			server.deleteById(id);
			for (String name : oldDoc.getFieldNames()) {
				if (!name.equals(ID)) {
					for (Object value : oldDoc.getFieldValues(name)) {
						doc.addField(name, value);
					}
				}
			}
			server.add(doc);
		} catch (IndexOutOfBoundsException e) {
			log.warn("Skipped update for non-existant record ID="+id);
		} catch (IOException e) {
			throw new Error(e);
		} catch (SolrServerException e) {
			throw new Error(e);
		}
	}

	@Override
	public NamedList<Object> request(SolrRequest request) {
		try {
			return server.request(request);
		} catch (SolrServerException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	@Override
	public void shutdown() {
		this.server.getCoreContainer().shutdown();
	}

}
