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

import org.apache.solr.client.solrj.SolrServer;
import org.filteredpush.dataentry.backend.GenericTupleSingleRecord;

interface GenericSolrIndexer<T extends GenericTupleSingleRecord<?>> {

	public void index(Iterable<T> records, SolrServer server, String dataSetId);

	public void update(Iterable<T> records, UpdatingSolrServer server,  String dataSetId);
	
}
