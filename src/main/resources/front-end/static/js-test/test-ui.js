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
module('ui');

test('fp.reset_timeout_function', function() {
	expect(3);
	stop();
	fp.long_timeout_has_run = false;
	
	(fp.reset_timeout_function(
		function () {
			ok(!fp.long_timeout_has_run, 'Long test should only run once.');
			fp.long_timeout_has_run = true;
			start();
		},
		'long',50))();
	
	(fp.reset_timeout_function(
		function () {
			ok(false, 'This should never run...');
		},
		'short',5))();
	(fp.reset_timeout_function(
		function () {
			ok(true, '... because it gets reset.');
			ok(!fp.long_timeout_has_run, 'Long should not have run first.')
		},
		'short',5))();
});

test('fp.get_suggestions', function() {
	var countdown = 2;
	expect(2);
	stop();
	
	$('#output').html(
		  "<input name='get_suggestions_test_input' value='yeah input!'></input>"
		+ "<select name='get_suggestions_test_select'>"
		+	"<option value='[&quot;yeah first option!&quot;]'>first!</option>"
		+	"<option value='[&quot;yeah second option!&quot;]'>second!</option>"
		+ "</select>"
	)
	
	fp.autosuggest_ajax_orig = fp.autosuggest_ajax;
	
	fp.autosuggest_ajax = function(name, val, callback) {
		countdown--;
		if (name == 'get_suggestions_test_input') {
			equal('yeah input!',val);
		} else if (name == 'get_suggestions_test_select') {
			equal('yeah first option!',val);
		} else {
			throw Error('not expecting '+name);
		}
		
		if (countdown == 0) {
			start();
			fp.autosuggest_ajax = fp.autosuggest_ajax_orig
		}
	}
	
	fp.get_suggestions('get_suggestions_test_input');
	fp.get_suggestions('get_suggestions_test_select');
})

function create_attach_autosuggestion_test(input_value, ajax_return, selected_attr) {
	return function() {
		expect(1);
		stop();
		
		var label_and_field_html = 
			  '<label>fakeField'
			+   '<input name="fakeField" value="[&quot;'+input_value+'&quot;]">'
			+ '</label>';
		
		fp.autosuggest_ajax_orig = fp.autosuggest_ajax;
		fp.autosuggest_ajax = function(name, val, callback_name) {
			eval(callback_name).call(null,{
				name: name,
				list: [
				   	['1', ajax_return],
					['2', 'Baker, B.']
				]
			});
			QUnit.assert.htmlEqual(
				$('#output').html(),
				  label_and_field_html
				+ '<label>officially...'
				+   '<select size="3" name="fakeField_id">'
				+     '<option '+selected_attr+' value="[&quot;1&quot;]">'+ajax_return+'</option>'
				+     '<option value="[&quot;2&quot;]">Baker, B.</option>'
				+   '</select>'
				+ '</label>');
			start();
			
			fp.suggestion_fields = fp.suggestion_fields_orig;
			fp.autosuggest_ajax = fp.autosuggest_ajax_orig;
		};
		
		fp.suggestion_fields_orig = fp.suggestion_fields;
		fp.suggestion_fields = ['fakeField'];
		
		$('#output').html(label_and_field_html)
		
		fp.attach_autosuggestion('fakeField');
	}
}

test('fp.attach_autosuggestion (match)', create_attach_autosuggestion_test('Case Does Not Matter', 'case does NOT matter', 'selected="selected"'));

test('fp.attach_autosuggestion (no match)', create_attach_autosuggestion_test('but spelling does', 'but spilling does', ''));

function set_fp() {
	fp.term_index_orig = fp.term_index;
	fp.suggestion_fields_orig = fp.suggestion_fields;
	
	fp.term_index = {recordedBy: ['recordedBy']};
	fp.suggestion_fields = [];
}

function reset_fp() {
	fp.term_index = fp.term_index_orig;
	fp.suggestion_fields = fp.suggestion_fields_orig;
}

test('fp.create_select: single', function() {
	set_fp();
	
	fp.create_select('recordedBy', ['["Smith"]']);
	QUnit.assert.htmlEqual(
		$('#output').html().replace(/style="[^"]*"/g, ''),
		  '<label id="term-recordedBy">recordedBy'
		+   '<input name="recordedBy" value="Smith"></input>'
		+ '</label>');
	
	reset_fp();
});

test('fp.create_select: multiple', function() {
	set_fp();
	
	fp.create_select('recordedBy', ['["Smith"]','["Jones"]']);
	QUnit.assert.htmlEqual(
		$('#output').html(),
		  '<label id="term-recordedBy">recordedBy'
		+   '<select name="recordedBy">'
		+     '<option value="[&quot;Smith&quot;]">Smith</option>'
		+     '<option value="[&quot;Jones&quot;]">Jones</option>'
		+   '</select>'
		+   '<button>edit</button>'
		+ '</label>');
	
	reset_fp();
});

test('fp.create_select: multi-multiple', function() {
	set_fp();
	
	fp.create_select('latLong', ['["0","0"]','["1","111"]','["1","1"]']);
	QUnit.assert.htmlEqual(
		$('#output').html(),
		  '<label id="term-latLong">latLong'
		+   '<select name="latLong">'
		+     '<option value="[&quot;1&quot;,&quot;111&quot;]">1 / 111</option>'
		+     '<option value="[&quot;1&quot;,&quot;1&quot;]">1 / 1</option>'
		+     '<option value="[&quot;0&quot;,&quot;0&quot;]">0 / 0</option>'
		+   '</select>'
		+   '<button>edit</button>'
		+ '</label>');
	
	reset_fp();
});
