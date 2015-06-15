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
/********************
 * non-UI utilities
 ********************/

fp.repeat = function(s, n, j){
    var a = [];
    while(a.length < n){
        a.push(s);
    }
    return a.join(j);
};

fp.pair_as_list = function(pair) {
	var key = Object.keys(pair)[0];
	var value = pair[key];
	
	if (Object.keys(pair).length != 1) {
		throw Error('Pair maps should have exactly one member.');
	}
	return [key,value];
};

fp.SetHash = function(record_list) {
	/* 
	 * Given something like:
	 *	var sh = new SetHash([{a:[1,2]},{a:[2,3]})
	 * It can return the union of the lists for each key:
	 *	sh.get_set_as_list('a').sort() == ['1','2','3']
	 */
		
	var value_set_hash = {};
	
	$.each(record_list,function(i,record) {
		$.each(record,function(key,values) {
			if (typeof value_set_hash[key] == 'undefined') {
				value_set_hash[key] = {};
			}
			$.each(values,function(i,value){
				value_set_hash[key][JSON.stringify(value)] = 1;
			});
		});
	});
	
	this.get_set_as_list = function(key) {
		if (typeof value_set_hash[key] == 'undefined') {
			return [];
		} else {
			return Object.keys(value_set_hash[key]);
		}
	};
	
	this.inspect = function() {
		// for developers
		return value_set_hash;
	};
};

fp.zip = function(a,b) {
	// Given lists of equal length a and b,
	// return an object with keys from a and values from b.
	var obj = {};
	if (a.length != b.length) {
		throw new Error("List lengths do not match: a: "+a.length+" / b: "+b.length);
	}
	$.each(a,function(i,key) {
		obj[key] = b[i];
	});
	return obj;
};

fp.tuple_serialize = function($form) {
	var obj = {};
	$form.find('input[name]').each(function(i,el){
		var $el = $(el); 
		
		obj[$el.attr('name')] = $el.val();
	});
	$form.find('select[name]').each(function(i,el){
		var $el = $(el); 
		
		var names = fp.term_index[$el.attr('name')] || [$el.attr('name')];
		var vals = JSON.parse($el.val());
		
		if (vals) {
			$.each(fp.zip(names,vals),function(name,val) {
				obj[name] = val;
			});
		}
	});
	
	// overwrite values if there is a controlled vocabulary:
	$form.find('select[name]').each(function(i,el){
		var $el = $(el);
		var name = $el.attr('name');
		if (name.match(/_id$/)) {
			obj[name.replace(/_id$/,'')] = $el.find('option:selected').text();
		}
	});
	return obj;
};

fp.parse_query = function(query) {
	var params = {};
	if (query.match(/=/)) {
		$.each(
			query.split('&'),
			function(i,key_value){
				var pair = key_value.split('=');
				if (pair.length == 2) {
					params[pair[0]] = decodeURIComponent(pair[1].replace(/\+/g, ' '));
				};
			});
	}
	return params;
};
