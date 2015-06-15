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
module('data');

test('fp.repeat', function() {
	equal(fp.repeat('a',3,'-'), 'a-a-a');
	equal(fp.repeat('a',0,'-'), '');
	equal(fp.repeat('',3,'-'), '--');
	equal(fp.repeat('a',3,''), 'aaa');
});

test('fp.pair_as_list', function() {
	deepEqual(fp.pair_as_list({a:1}), ['a',1]);
	throws(function(){fp.pair_as_list({})});
	throws(function(){fp.pair_as_list({a:1,b:2})});
});

test('fp.SetHash', function() {
	var empty = new fp.SetHash([]);
	deepEqual(empty.get_set_as_list('a'), []);
	
	var easy = new fp.SetHash([{a:[1]}]);
	deepEqual(easy.get_set_as_list('a'), ['1']);
	deepEqual(easy.get_set_as_list('z'), []);
	
	var hard = new fp.SetHash([{a:[1]},{a:[1,2]},{b:[2]},{c:[3]},{d:[]}]);
	deepEqual(hard.get_set_as_list('a').sort(), ['1','2']);
	deepEqual(hard.get_set_as_list('b'), ['2']);
	deepEqual(hard.get_set_as_list('c'), ['3']);
	deepEqual(hard.get_set_as_list('d'), []);
	deepEqual(hard.get_set_as_list('z'), []);
	
	var nested = new fp.SetHash([{a:[[1]]},{b:[{a:1}]}]);
	deepEqual(nested.get_set_as_list('a'), ['[1]']);
	deepEqual(nested.get_set_as_list('b'), ['{\"a\":1}']);
	deepEqual(nested.get_set_as_list('z'), []);
});

test('fp.zip', function() {
	deepEqual(
		fp.zip(['a','b'], [1,2]),
		{a:1,b:2});
	throws(function(){fp.zip([1],[])});
});

// TODO: This is a little complicated. 
// At the very least, it depends on the initialization of fp.term_index.
//test('fp.tuple_serialize', function() {
//});

test('fp.parse_query', function() {
	deepEqual(
		fp.parse_query('a=1'),
		{a:'1'});
	
	// I don't think anything requires this particular handling of edge cases,
	// but I wanted to get it documented so we'll notice if something does change.
	deepEqual(
		fp.parse_query('&a=1'),
		{a:'1'});
	deepEqual(
		fp.parse_query('a=1&'),
		{a:'1'});
	deepEqual(
		fp.parse_query('pets=cat+dog'),
		{pets:'cat dog'});
	deepEqual(
		fp.parse_query('dup=1&dup=2'),
		{dup:'2'});
	deepEqual(
		fp.parse_query('=val&key='),
		{'':'val','key':''});
	deepEqual(
		fp.parse_query('a=b=c'),
		{});
	deepEqual(
		fp.parse_query('esc=%61%62%63'),
		{esc:'abc'});

});