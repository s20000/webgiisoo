package com.giisoo.utils.base;

// TODO: Auto-generated Javadoc
/**
 * The Class H32.
 */
public class H32 {

	/**
	 * To string.
	 *
	 * @param l the l
	 * @return the string
	 */
	public static String toString(long l) {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < 13; i++) {
	      int t = (int) (l & 0x1f);
	      l = l >> 5;
	      sb.append(chars[t]);
	    }
	    return sb.reverse().toString();
	}

	/** The Constant DIGITAL. */
	static final int DIGITAL = 32;
	
	/** The Constant chars. */
	static final char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
}
