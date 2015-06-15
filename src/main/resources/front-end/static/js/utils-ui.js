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
/****************
 * UI utilities
 ****************/

fp.reset_timeout_function = function(callback,name,wait_ms) {
	var id_name = name + '_timeout_id';
	return function() {
		if (typeof fp.timeout_registry == 'undefined') {
			fp.timeout_registry = {};
		}
		var old_timeout_id = fp.timeout_registry[id_name];
		var new_timeout_id = setTimeout(callback,wait_ms,name);
		
		clearTimeout(old_timeout_id);
		fp.timeout_registry[id_name] = new_timeout_id;
	};
};

fp.flash = function($el) {
	$el.fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100);
};

fp.stretch_inputs = function() {
	var window_width = $(window).width();
	var left_offset = 200;
	var button_width = 30;
	var fudge = 35; // To make up for the default padding given to elements.
	$('input').width(window_width-left_offset);
	$('button').width(button_width);
	$('select').width(window_width-left_offset-button_width-fudge);
};

fp.init_from_url = function(url) {
	// TODO: find some way to unit test this...
	var css_applied = false;
	var origin = false;
	var require_confirmation = false;
	
	function send_message_function(parent_url) {
		// TODO: This is vulnerable to race conditions if the auto-suggest is slow to return.
		// I think it would fix it if the auto-suggest fired a change event.
		return fp.reset_timeout_function(
			function(){
				window.parent.postMessage(fp.tuple_serialize($('#output')),parent_url);
			},
			'message',
			1000
		);
	}
	
	$.each(fp.parse_query(location.hash.substr(1)),function(key,value){
		if (key.match(/_urls$/)) {
			var urls = value.split(' ');
			if (key == 'js_urls') {
				$.each(urls,function(i,url){
					$('body').append('<script src="'+url+'"></script>');
				});
			} else if (key == 'css_urls') {
				$.each(urls,function(i,url){
					css_applied = true;
					$('head').append('<link rel="stylesheet" href="'+url+'"></link>');
				});
			} else {
				throw Error('Unrecognized param: '+key);
			}
		} else if (key == 'origin') {
			origin = value;
		} else if (key == 'require_confirmation') {
			require_confirmation = parseInt(value);
		} else {
			$('[name="'+key+'"]').eq(0).val(value);
		}
	});
	if (!origin) {
		alert('No origin was set, so this page will not be able to send back its results.');
	} else if (require_confirmation) {
		$('#back-end-message').on('click', '#confirm', send_message_function(origin));
	} else {
		$('#output').change(send_message_function(origin));
	}
	if (!css_applied) {
		alert('This looks ugly because no css has been specified.');
	}
	$('#input').change();
};

fp.get_name_selector = function(name) {
	return '#output [name="'+name+'"]';
};

fp.get_suggestions = function(name) {
	var el = $(fp.get_name_selector(name))[0];
	var val = el.tagName == 'SELECT'
		? JSON.parse(el.value)[0]
		: el.value;
	console.log('get_suggestions for:',name, val);
	fp.autosuggest_ajax(name, val, 'fp.autosuggest_callback');
};

fp.attach_autosuggestion = function(name) {
	var autosuggest_name = name+'_id';
	
	if (fp.suggestion_fields.indexOf(name) != -1 && $('[name='+autosuggest_name+']').length == 0) {
		var $select = $('<select>').attr('name',autosuggest_name).attr('size',3);
		var name_selector = fp.get_name_selector(name);
		
		// We don't delete the old one till after (It's the landmark for insertion), so the eq(0) is needed.
		$(name_selector).eq(0).parent().after(
			$("<label>").text('officially...').append($select)
		);
		
		$('#output') // Note: the selector does not include #output, because it's only filtering down, not up, the DOM.
			.on('change',   '[name='+name+']', fp.reset_timeout_function(fp.get_suggestions, name, 0))
			.on('keypress', '[name='+name+']', fp.reset_timeout_function(fp.get_suggestions, name, 500));
		
		$(name_selector).change();
	}
};

fp.create_select = function(term,values) {

	function make_label(name) {
		return $("<label>").attr('id','term-'+name).text(name);
	}
	
	function make_input(name,value) {
		return $('<input>').attr('name',name).attr('value',value);
	}
	
	function replace_select_with_input($button) {
		var $select = $button.siblings('select');
		var tuple_name = $select.attr('name');
		
		var term_names = fp.term_index[tuple_name];
		var vals = JSON.parse($select.val());
		
		var $parent = $button.closest('label');
		
		$('[name='+$select.attr('name')+'_id'+']').parent().remove();
		
		$.each(fp.zip(term_names,vals),function(name,val) {
			var $label = make_label(name).insertBefore($parent);
			$label.append(make_input(name,val));
			fp.attach_autosuggestion(name);
		});

		$parent.remove();
		fp.stretch_inputs();
	}
	
	var $label = make_label(term);
	var $menu = $("<select>").attr('name',term);
	var $button = $("<button>").text('edit')
		.click(function(event){replace_select_with_input($(event.target)); return false;})
		.data('target',term);
	var values_sorted = values.sort(function(a,b) {
		return a.length != b.length
			? b.length - a.length
			: a < b; // so date selector is sorted with recent first.
	});
	
	$.each(values_sorted,function(i,value){
		var $option = $("<option>").val(value).text(JSON.parse(value).join(' / '));
		$menu.append($option);
	});
	// WARNING: If $button precedes $menu, then a no-change click on $menu
	// will trigger a click event on $button: this feels like a browser bug?
	// (FF 23.0, Ubuntu 12.04 (precise) 64-bit)
	// In any case, changing the order seemed to fix it.
	$('#output').append($label.append($menu).append($button));
	if (values_sorted.length == 1) {
		replace_select_with_input($button);
	}
};
