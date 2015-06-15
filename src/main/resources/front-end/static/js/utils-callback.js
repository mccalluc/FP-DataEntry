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
fp.back_end_callback = function(term_list_list) {
	var set_hash = new fp.SetHash(term_list_list);
	
	console.log('back_end_callback: ',term_list_list);
	$('#output').html('');
	
	if (!Array.isArray(term_list_list)) {
		throw Error("Expected an array: instead we got: "+Object.prototype.toString.call(term_list_list));
	}
	
	if (term_list_list.length) {
		$.each(fp.term_pair_list,function(i,pair){
			if (Object.keys(pair).length != 1) {
				throw Error("Expected an array of 1-element objects.");
			}
			var pair_list = fp.pair_as_list(pair);
			var value_list = set_hash.get_set_as_list(pair_list[0]);
			var non_empty_list = value_list.length 
				? value_list 
				: ['['+fp.repeat('""',pair_list[1].length || 1,',')+']']; 
			// Some fields may have been null in all matched records,
			// but we need some value to render the control,
			// and the length of the json array should match that of the underlying tuple.

			fp.create_select(pair_list[0],non_empty_list);
			fp.attach_autosuggestion(pair_list[0]);
		});
	}
	
	$('#output').change(); // If there are auto-suggests, change will be fired again soon.
	
	fp.stretch_inputs();

	fp.flash($('#back-end-message').html(
		term_list_list.length == 0
			? 'Found no matches.'
			: (( term_list_list.length == 1
				? 'Found one match.'
				: ( term_list_list.length == 10 // The max is set in solrconfig.xml
					? 'Maxed out at ' + term_list_list.length + ': fill in more fields to limit the results.' 
					: 'Found ' + term_list_list.length + ' matches.'
				)
			)+"${confirmationButtonHtml}") // TODO: ugh: maybe put confirm-or-not in a top-level data? or always display, and sometimes hide with css?
		)
	);

};

fp.autosuggest_callback = function(wrapped_list) {
	if (typeof wrapped_list.name != "string") {
		throw Error("Expected a string 'name' in JSON.");
	}
	if (!Array.isArray(wrapped_list.list)) {
		throw Error("Expected an array 'list' in JSON.");
	}
	var $select = $('[name='+wrapped_list.name+'_id]');
	var searched_for = $('[name='+wrapped_list.name+']').eq(0).val();
	if (!wrapped_list.list.length && searched_for.length) {
		// Autosuggest results really depend on the backing software: just making it shorter may not help.
		alert('Found no matches for "'+wrapped_list.name+'".\nChanging the search string may help.');
	} else {
		$select.find('*').remove();
		$.each(wrapped_list.list, function(i, pair) {
			var $option = $('<option>')
				.val(JSON.stringify([pair[0]]))
				.text(pair[1]);
			$select.append($option);
		});
		var $first_option = $select.find('option').eq(0)
		if ($first_option.text().toLowerCase() == JSON.parse(searched_for)[0].toLowerCase()) {
			// TODO: Is this the right level of strictness for every scenario?
			// TODO: Could we preserve the earlier selection?
			$first_option.attr('selected','selected');
		}
	}
}