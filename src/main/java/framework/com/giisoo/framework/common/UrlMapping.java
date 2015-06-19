/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.*;
import java.util.*;

import jregex.*;

import com.giisoo.core.bean.*;

@DBMapping(table = "tblurlmapping")
public class UrlMapping extends Bean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String url;
	String dest;
	int seq;

	static List<UrlMapping> cache = null;

	transient Replacer replacer;
	transient Pattern pattern;

	/**
	 * Replacement.
	 * 
	 * @param src
	 *            the src
	 * @return the string
	 */
	public String replacement(String src) {
		if (pattern == null) {
			pattern = new Pattern(url);
		}

		if (replacer == null) {
			replacer = pattern.replacer(dest);
		}

		String result = replacer.replace(src);
		return result;
	}

	/**
	 * Matches.
	 * 
	 * @param src
	 *            the src
	 * @return true, if successful
	 */
	public boolean matches(String src) {
		if (pattern == null) {
			pattern = new Pattern(url);
		}

		return pattern.matches(src);
	}

	/**
	 * Load all.
	 * 
	 * @return the list
	 */
	public static List<UrlMapping> loadAll() {
		if (cache == null) {
			cache = Bean.load(null, null, null, "order by seq", 0,
					Integer.MAX_VALUE, UrlMapping.class);
		}

		return cache;
	}

	/**
	 * Update.
	 * 
	 * @param url
	 *            the url
	 * @param dest
	 *            the dest
	 * @param seq
	 *            the seq
	 * @return the int
	 */
	public static int update(String url, String dest, int seq) {
		cache = null;
		return Bean.update("url=?", new Object[] { url }, V
				.create("dest", dest).set("seq", seq), UrlMapping.class);
	}

	/**
	 * Creates the.
	 * 
	 * @param url
	 *            the url
	 * @param dest
	 *            the dest
	 * @param seq
	 *            the seq
	 * @return the int
	 */
	public static int create(String url, String dest, int seq) {
		cache = null;
		return Bean.insert(V.create("url", url).set("dest", dest),
				UrlMapping.class);
	}

	/**
	 * Gets the dest.
	 * 
	 * @param url
	 *            the url
	 * @return the dest
	 */
	public static String getDest(String url) {
		return Bean.getString("tblurlmapping", "dest", "url=?",
				new String[] { url }, null);
	}

	/**
	 * Load.
	 * 
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 * @return the beans
	 */
	public static Beans<UrlMapping> load(int offset, int limit) {
		return Bean.load(null, null, "order by url", offset, limit,
				UrlMapping.class);
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		url = r.getString("url");
		dest = r.getString("dest");
		seq = r.getInt("seq");
	}

	public int getSeq() {
		return seq;
	}

	public String getUrl() {
		return url;
	}

	public String getDest() {
		return dest;
	}

	/**
	 * Load.
	 * 
	 * @param url
	 *            the url
	 * @return the url mapping
	 */
	public static UrlMapping load(String url) {
		return Bean.load("tblurlmapping", "url=?", new Object[] { url },
				UrlMapping.class);
	}

	/**
	 * Delete.
	 * 
	 * @param url
	 *            the url
	 */
	public static void delete(String url) {
		Bean.delete("url=?", new Object[] { url }, UrlMapping.class);
		cache = null;
	}

}
