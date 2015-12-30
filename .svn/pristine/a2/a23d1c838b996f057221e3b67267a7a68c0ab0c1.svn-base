package com.giisoo.utils.url;

import java.util.HashMap;
import java.util.Iterator;

// TODO: Auto-generated Javadoc
/**
 * The Class Cookie.
 */
public class Cookie {

	/** The cookie. */
	HashMap<String, String> cookie = new HashMap<String, String>();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> it = cookie.keySet().iterator(); it.hasNext();) {
			String n = it.next();
			if (sb.length() > 0) {
				sb.append(";");
			}

			sb.append(n).append("=").append(cookie.get(n));
		}

		return sb.toString();
	}

	/**
	 * Adds the.
	 *
	 * @param s the s
	 */
	public void add(String s) {
		if (s != null) {
			String ss[] = s.split(";");
			for (String s1 : ss) {
				String s2[] = s1.split("=");
				if (s2.length > 1) {
					cookie.put(s2[0].trim(), s2[1]);
				}
			}
		}
	}

}
