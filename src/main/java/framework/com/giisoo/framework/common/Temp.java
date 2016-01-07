/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.io.File;

import org.apache.commons.configuration.Configuration;

import com.giisoo.core.bean.UID;

public class Temp {

	public static String ROOT;

	/**
	 * Inits the.
	 * 
	 * @param conf
	 *            the conf
	 */
	public static void init(Configuration conf) {
		ROOT = conf.getString("temp.path", "/opt/temp/");
	}

	/**
	 * Gets the.
	 * 
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @return the file
	 */
	public static File get(String id, String name) {
		return new File(path(id, name));
	}

	static private String path(String path, String name) {
		long id = Math.abs(UID.hash(path));
		char p1 = (char) (id % 23 + 'a');
		char p2 = (char) (id % 19 + 'A');
		char p3 = (char) (id % 17 + 'a');
		char p4 = (char) (id % 13 + 'A');

		StringBuilder sb = new StringBuilder(ROOT);

		sb.append("/").append(p1).append("/").append(p2).append("/").append(p3)
				.append("/").append(p4).append("/").append(id);

		if (name != null)
			sb.append("_").append(name);

		return sb.toString();
	}

}
