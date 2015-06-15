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

/*
 * If CSS isn't flexible enough, you could manipulate the DOM here.
 */

/*
 * These fields will be checked against a local controlled vocabulary.
 */

fp.suggestion_fields = ${suggestionFieldsJson};

fp.autosuggest_ajax = function(field_name,search_value,callback_name) {
	/*
	 * To create your own autosuggest, you probably want to
	 *   -- copy this JS to your own server, 
	 *   -- delete everything below that's not commented, 
	 *   -- uncomment the lines at the end of this block,
	 *   -- change the url to match your institution, 
	 *   -- and on your server implement a JSONP service that responds with something like:
	 * 
	 * 	<callback_name>({
	 * 		name: '<field_name>',
	 * 		list: [
	 * 			['<id>', '<value>'],
	 * 			...
	 * 		]
	 * 	});
	 * 
	 * The first result in the list should be the best match: If it is an exact case-insensitive match,
	 * it will be selected. If there is no exact match, the user will need to click to choose one.
	 * 
	 * If you already have a JSONP service, but it returns results in a different format,
	 * rather than adding new code on the server, you might define a little transformation function
	 * here, and designate it as the callback, instead of the callback_name parameter.
	 * 
	 * OK: Here's the code you should uncomment and tweak as appropriate:

	var ajax_url = 'http://example.edu/collections-management/autosuggest-jsonp'
		+ '?name='+encodeURIComponent(field_name)
		+ '&value='+encodeURIComponent(search_value)
		+ '&callback='+encodeURIComponent(callback_name);
	
	$.getScript(ajax_url);

	* ... and just delete everything below:
	*/
	
	var fake_ajax_data = ${fakeAjaxJson};
	
	eval(callback_name).call(null,{
		name: field_name,
		list: (function() {
			var list_of_pairs = [];
			$.each( // $.map flattens nested lists, so we can't use it.
				$.grep(
					Object.keys(fake_ajax_data[field_name]),
					function(key) {
						// Just return all records that match on the first letter:
						// you'll probably want something smarter in your server-side code.
						return key.substr(0,1).toUpperCase() == search_value.substr(0,1).toUpperCase();
					}
				),
				function(i, grep_match) {
					return list_of_pairs.push([fake_ajax_data[field_name][grep_match], grep_match]);
				}
			);
			return list_of_pairs;
		})()
	});
	
	
};