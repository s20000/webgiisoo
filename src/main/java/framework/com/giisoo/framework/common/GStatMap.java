/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GStatMap {
	Map<String, Integer> m = new HashMap<String, Integer>();

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public int size() {
		return m.size();
	}

	/**
	 * Put.
	 * 
	 * @param o
	 *            the o
	 * @param i
	 *            the i
	 */
	public void put(String o, int i) {
		Integer ii = m.get(o);
		if (ii == null) {
			m.put(o, i);
		} else {
			m.put(o, i + ii);
		}
	}

	/**
	 * Key set.
	 * 
	 * @return the sets the
	 */
	public Set<String> keySet() {
		return m.keySet();
	}

	/**
	 * Gets the.
	 * 
	 * @param k
	 *            the k
	 * @return the int
	 */
	public int get(String k) {
		return m.get(k);
	}
}
