package org.filteredpush.dataentry.backend;

import java.nio.ByteBuffer;

import com.google.common.base.Charsets;

public enum EncodingCorrection {
	NONE, FIX_A_TILDE;
	public String correct(String maybeBad) {
		switch (this) {
			case NONE:
				return maybeBad;
			case FIX_A_TILDE:
				// There is misencoded data in GBIF.
				// ( http://dev.gbif.org/issues/browse/PF-1430 )
				// ... so this attempts to fix it. (although it makes me feel dirty.)
				if (maybeBad.matches(".*\\xC3[^a-zA-Z].*") // A-tilde followed by non-letter
						&& maybeBad.matches("[\\x00-\\xFF]*") // If there were a character outside this gamut, it's not a simple utf8-as-iso8859 bug.
						) {
					ByteBuffer bytes = Charsets.ISO_8859_1.encode(maybeBad);
					String maybeGood = Charsets.UTF_8.decode(bytes).toString();
					if (maybeGood.contains("\ufffd")) {
						// re-encoding failed on at least one character.
						return maybeBad;
					} else {
						return maybeGood;
					}
				} else {
					return maybeBad;
				}
			default:
				throw new Error("There should be a case for each enum value, but there wasn't one for: " + this);
		}
		
	}
}
