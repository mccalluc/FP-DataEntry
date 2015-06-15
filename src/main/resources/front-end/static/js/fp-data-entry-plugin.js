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
if(typeof console == "undefined") {
	console = {};
	var methods = ["log", "debug", "info", "warn", "exception", "assert", "dir", "dirxml", "trace",
	        "group", "groupEnd", "groupCollapsed", "profile", "profileEnd", "count", "clear", 
	        "time", "timeEnd", "timeStamp", "table", "error"];
	for (i in methods) {
		console[methods[i]] = function(){};
	}
}

function FpDataEntryPlugin(props) {
	
	this.props = props;
	
	function esc(str) {
		return encodeURIComponent(str);
	}

	var cookies = {
		/*
			https://developer.mozilla.org/en-US/docs/DOM/document.cookie
			This framework is released under the GNU Public License, version 3 or later.
			http://www.gnu.org/licenses/gpl-3.0-standalone.html
		*/
		getItem : function(sKey) {
			return decodeURIComponent(document.cookie.replace(new RegExp(
					"(?:(?:^|.*;)\\s*"
							+ encodeURIComponent(sKey).replace(/[\-\.\+\*]/g,
									"\\$&") + "\\s*\\=\\s*([^;]*).*$)|^.*$"),
					"$1"))
					|| null;
		},
		setItem : function(sKey, sValue, vEnd, sPath, sDomain, bSecure) {
			if (!sKey
					|| /^(?:expires|max\-age|path|domain|secure)$/i.test(sKey)) {
				return false;
			}
			var sExpires = "";
			if (vEnd) {
				switch (vEnd.constructor) {
				case Number:
					sExpires = vEnd === Infinity ? "; expires=Fri, 31 Dec 9999 23:59:59 GMT"
							: "; max-age=" + vEnd;
					break;
				case String:
					sExpires = "; expires=" + vEnd;
					break;
				case Date:
					sExpires = "; expires=" + vEnd.toUTCString();
					break;
				}
			}
			document.cookie = encodeURIComponent(sKey) + "="
					+ encodeURIComponent(sValue) + sExpires
					+ (sDomain ? "; domain=" + sDomain : "")
					+ (sPath ? "; path=" + sPath : "")
					+ (bSecure ? "; secure" : "");
			return true;
		},
		removeItem : function(sKey, sPath, sDomain) {
			if (!sKey || !this.hasItem(sKey)) {
				return false;
			}
			document.cookie = encodeURIComponent(sKey)
					+ "=; expires=Thu, 01 Jan 1970 00:00:00 GMT"
					+ (sDomain ? "; domain=" + sDomain : "")
					+ (sPath ? "; path=" + sPath : "");
			return true;
		},
		hasItem : function(sKey) {
			return (new RegExp("(?:^|;\\s*)"
					+ encodeURIComponent(sKey).replace(/[\-\.\+\*]/g, "\\$&")
					+ "\\s*\\=")).test(document.cookie);
		},
		keys : /* optional method: you can safely remove it! */ function() {
			var aKeys = document.cookie.replace(
					/((?:^|\s*;)[^\=]+)(?=;|$)|^\s*|\s*(?:\=[^;]*)?(?:\1|$)/g,
					"").split(/\s*(?:\=[^;]*)?;\s*/);
			for (var nIdx = 0; nIdx < aKeys.length; nIdx++) {
				aKeys[nIdx] = decodeURIComponent(aKeys[nIdx]);
			}
			return aKeys;
		}
	};
	
	// get:
	
	function get_by_id(id) {
		return document.getElementById(id);
	}
	function get_by_name(name) {
		var els = document.getElementsByName(name);
		if (els.length > 1) {
			throw Error('Name "'+name+'" is not unique in this document.');
		}
		return els[0];
	}
	
	// get value:
	
	function get_value_by_id(id) {
		try {
			return get_by_id(id).value;
		} catch (e) {
			console.warn('No such element: id='+id);
			return ''; // TODO: Remind me why we need this?
		}
	}
	function get_value_by_name(name) {
		try {
			return get_by_name(name).value;
		} catch (e) {
			console.warn('No such element: name='+name);
			return '';
		}
	}
	
	// set value:
	
	function set_value(el,value) {
		try {
			el.value = value;
		} catch (e) {}
		if (typeof jQuery == 'function') {
			jQuery(el).change(); // This works for collectionspace. TODO: generalize for other applications / frameworks
		}
	}
	
	// drag to resize:
	
	var target_css = '-webkit-user-select: none; -khtml-user-select: none; -moz-user-select: none; -ms-user-select: none; -o-user-select: none; user-select: none; '
		+ 'width: 1.5em; height: 1.5em; ';
	var drag_target_css = 'position:absolute; top:0px; left:0px; z-index:9999; cursor:nwse-resize; ' + target_css;
	var close_target_css = 'position:absolute; bottom:0px; left:0px; z-index:9999; cursor:pointer; ' + target_css;
	
	function start_drag_function(drag_target) {
		return function(e) {
			console.log('down',e);
			get_by_id('fp-drag-target').setAttribute('style', drag_target_css + ' width:100%; height:100%; ');
			// Without the 100%s, mouseups could be lost in the iframe.
			document.body.addEventListener('mousemove', drag_handler, false);
		};
	}
	
	function drag_handler(e) {
		var iframe = get_by_id('fp-iframe');
		// TODO: not sure this math is right: figure out mouse position within drag-target?
		iframe.width = window.innerWidth - e.clientX - 5;
		iframe.height = window.innerHeight - e.clientY - 5;
	}
	
	function end_drag_function(drag_target_css) {
		return function end_drag(e) {
			console.log('up',e);
			var iframe = get_by_id('fp-iframe');
			cookies.setItem('fp-iframe-width',  iframe.width,  Infinity, '/');
			cookies.setItem('fp-iframe-height', iframe.height, Infinity, '/');
			get_by_id('fp-drag-target').setAttribute('style', drag_target_css);
			document.body.removeEventListener('mousemove', drag_handler, false);
		};
	}
	
	// HTML5 Web Messaging:
	
	function message_handler(message) {
		console.log('Received message: ', message.data);
		if (props.url.substr(0,message.origin.length) != message.origin) {
			console.warn('Recieved unexpected message: origin was "'+message.origin
				+'"\nbut it should have matched the origin of "'+props.url+'".');
		} else {
			for (field in message.data) {
				try {
					set_value(
						props.selector_function(props.selectors[field]),
						message.data[field]);
				} catch (e) {
					console.warn('Hit error while populating form, but will continue.', e);
				}
			}
		}
	}
	
	// Main:
	
	this.main = function() {
		var props = this.props;
		try {
			props.prehook_function();
			
			var width =  Math.min(
					cookies.getItem('fp-iframe-width')  || parseInt(props.width)  || 800, 
					window.innerWidth - 50); // "-50" to make up for the width of the iframe border, and to leave some of the form behind visible.
			var height = Math.min(
					cookies.getItem('fp-iframe-height') || parseInt(props.height) || 400,
					window.innerHeight - 50);
			
			var iframe_url = props.url;
			var css_urls = esc((props.css_urls || []).join(' '));
			var js_urls = esc((props.js_urls || []).join(' '));
			var require_confirmation = props.require_confirmation;
			var q = '';
			var q_lists = {};
			var i, key, list_key, value;
			
			var q_names_keys = Object.keys(props.q_names);
			for (i = 0; i < q_names_keys.length; i++) {
				key = q_names_keys[i];
				list_key = props.q_names[key];
				value = props.selector_function(key).value;
				if (q_lists[list_key]) {
					q_lists[list_key].push(value);
				} else {
					q_lists[list_key] = [value];
				}
			}
			
			var q_lists_keys = Object.keys(q_lists);
			for (i = 0; i < q_lists_keys.length; i++) {
				q += q_lists_keys[i] + '=' + esc(q_lists[q_lists_keys[i]].join(' ')) + '&'; // Extra spaces won't hurt.
			}
			
			var origin = document.location.origin;
			if (origin == 'null') {
				throw Error('Web Message API requires an origin, and we do not have that on file URIs.');
			}
			q += 'css_urls=' + css_urls + '&'
				+ 'js_urls=' + js_urls + '&'
				+ 'origin=' + esc(origin) + '&'
				+ 'require_confirmation=' + (require_confirmation ? 1 : 0);
			
			if (!props.iframe_parent_id) {
				throw Error('iframe_parent_id must be defined.');
			}
			var target = get_by_id(props.iframe_parent_id);
			if (target == null) {
				target = document.createElement('div');
				target.id = props.iframe_parent_id;
				document.body.appendChild(target);
			}
			target.setAttribute('style',props.iframe_parent_style); // 'target.style=' was ignored by Chrome.
			
			if (!iframe_url.match(/^https?:\/\//)) {
				throw Error('URL missing or bad format: '+url);
			}
			
			target.innerHTML = 
				'<div id="fp-drag-target" style="'+drag_target_css+'" >&#x21F1;</div>'
				+ '<div id="fp-close-target" style="'+close_target_css+'" onclick="document.getElementById(\''+props.iframe_parent_id+'\').remove()" >&times;</div>'
				+ '<iframe id="fp-iframe" src="'+iframe_url+'/iframe.html#'+q+'" width="'+width+'" height="'+height+'"></iframe>';
			
			var start_drag_handler = start_drag_function(drag_target_css);
			var end_drag_handler = end_drag_function(drag_target_css);
			
			window.addEventListener('message', message_handler, false);
			get_by_id('fp-close-target').onclick = function(){
				window.removeEventListener('message', message_handler, false);
				document.body.removeEventListener('mousemove', drag_handler, false);
				get_by_id('fp-drag-target').removeEventListener('mousedown', start_drag_handler, false);
				document.body.removeEventListener('mouseup', end_drag_handler, false);
				get_by_id(props.iframe_parent_id).remove();
			};
			get_by_id('fp-drag-target').addEventListener('mousedown', start_drag_handler, false);
			document.body.addEventListener('mouseup', end_drag_handler, false);
			
		} catch (e) {
			alert(e + ' at line ' + e.lineNumber);
			// TODO: handle chrome: http://stackoverflow.com/questions/1340872
			// We need to end cleanly so that the 'return false' in the caller still runs.
		}
	};
	
}