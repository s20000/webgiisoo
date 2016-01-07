/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

public class H64 {
	
	/**
	 * To string.
	 * 
	 * @param l
	 *            the l
	 * @return the string
	 */
	public static String toString(long l) {
		StringBuilder sb = new StringBuilder();
		while (l >= DIGITAL) {
			int n = (int) (l % DIGITAL);
			sb.append(chars[n]);
			l = l / DIGITAL;
		}

		sb.append(chars[(int) l]);

		return sb.reverse().toString();
	}

	static final int DIGITAL = 64;
	static final char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".toCharArray();
	
	
}
