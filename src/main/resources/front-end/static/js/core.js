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
$(function(){

	/***************
	 * Mock console
	 ***************/
	
	if(typeof console == "undefined") {
		console = {};
		$.each(["log", "debug", "info", "warn", "exception", "assert", "dir", "dirxml", "trace",
		        "group", "groupEnd", "groupCollapsed", "profile", "profileEnd", "count", "clear", 
		        "time", "timeEnd", "timeStamp", "table", "error"],
			function(i,method){
				console[method] = function(){};
			});
	}
	
	/************************************
	 * Create tuple->term index
	 ************************************/
	
	fp.term_index = {};
	$.each(fp.term_pair_list,function(i,pair){
		var pair_list = fp.pair_as_list(pair);
		fp.term_index[pair_list[0]] = pair_list[1];
	});

	/************
	 * Handlers
	 ************/
	
	$('#input').change(function(){
		var query = $('#input').serialize();
		console.log('back-end: ',query);
		$.getJSON(fp.BACK_END_URL, query, fp.back_end_callback)
			.fail(function(){
				alert('Request to back-end server failed:\nThe back-end server may be down,\nor there may be a problem with the request.');
			});
	});
	
	$(window).resize(function(){
		fp.stretch_inputs();
	});
	
	 
	/***********************
	 * UI Setup
	 ***********************/
	 
	 fp.stretch_inputs();
	 fp.init_from_url(window.location);

});
