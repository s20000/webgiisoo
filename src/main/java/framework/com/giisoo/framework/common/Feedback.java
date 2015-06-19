/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.*;

import com.giisoo.core.bean.*;

@DBMapping(table = "tblfeedback")
public class Feedback extends Bean {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int id;
	String content;
	long created;
	String ip;

	public int getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

	public long getCreated() {
		return created;
	}

	public String getIp() {
		return ip;
	}

	/**
	 * Creates the.
	 * 
	 * @param content
	 *            the content
	 * @param ip
	 *            the ip
	 */
	public static void create(String content, String ip) {
		Bean.insert(
				V.create("content", content)
						.set("created", System.currentTimeMillis())
						.set("ip", ip), Feedback.class);
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
	public static Beans<Feedback> load(int offset, int limit) {
		return Bean.load(null, null, "order by created desc", offset, limit,
				Feedback.class);
	}

	/**
	 * Delete.
	 * 
	 * @param id
	 *            the id
	 */
	public static void delete(int id) {
		Bean.delete("id=?", new Object[] { id }, Feedback.class);
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		id = r.getInt("id");
		ip = r.getString("ip");
		created = r.getLong("created");
		content = r.getString("content");
	}

}
