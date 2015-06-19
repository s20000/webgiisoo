/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.UID;

@DBMapping(table = "tblalert")
public class Alert extends Bean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int STATE_ALL = -1;
	public static final int STATE_PENDING = 0;

	String tag;
	String number;

	String id;
	String content;
	int state;
	long sent;

	/**
	 * Load.
	 * 
	 * @param state
	 *            the state
	 * @param n
	 *            the n
	 * @return the list
	 */
	public static List<Alert> load(int state, int n) {
		return Bean.load(null, "state=? and sent>0", new Object[] { state },
				"order by tag, number", 0, n, Alert.class);
	}

	/**
	 * Load.
	 * 
	 * @param state
	 *            the state
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<Alert> load(int state, int s, int n) {
		if (state == STATE_ALL) {
			return Bean.load(null, null, "order by tag, number", s, n,
					Alert.class);
		} else {
			return Bean.load("state=?", new Object[] { state },
					"order by tag, number", s, n, Alert.class);
		}
	}

	/**
	 * Clean.
	 * 
	 * @return the int
	 */
	public static int clean() {
		return Bean.delete(null, null, Alert.class);
	}

	/**
	 * Update.
	 * 
	 * @param v
	 *            the v
	 * @return the int
	 */
	public int update(V v) {
		return Bean.update("tag=? and number=? and id=?", new Object[] { tag,
				number, id }, v, Alert.class);
	}

	/**
	 * Creates the.
	 * 
	 * @param tag
	 *            the tag
	 * @param content
	 *            the content
	 * @return the int
	 */
	public static int create(String tag, String content) {
		int count = 0;
		count += Alert.insertOrUpdate(tag, "default", content);

		return count;
	}

	/**
	 * Insert or update.
	 * 
	 * @param tag
	 *            the tag
	 * @param number
	 *            the number
	 * @param content
	 *            the content
	 * @return the int
	 */
	public static int insertOrUpdate(String tag, String number, String content) {
		String id = UID.id(tag, number, content);

		Alert a = Bean.load("tblalert", "tag=? and number=?", new Object[] {
				tag, number }, Alert.class);
		if (a == null) {
			// insert
			return Bean.insert(
					V.create("tag", tag).set("number", number).set("id", id)
							.set("content", content)
							.set("state", STATE_PENDING), Alert.class);
		} else if (!id.equals(a.id)) {
			// reset
			return Bean.update(
					"tag=? and number=?",
					new Object[] { tag, number },
					V.create("id", id).set("content", content)
							.set("state", STATE_PENDING).set("sent", 0),
					Alert.class);
		}
		// ignore
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		tag = r.getString("tag");
		number = r.getString("number");
		id = r.getString("id");
		content = r.getString("content");
		sent = r.getLong("sent");
		state = r.getInt("state");
	}

	public int getState() {
		return state;
	}

	public String getTag() {
		return tag;
	}

	public String getNumber() {
		return number;
	}

	public String getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

	public long getSent() {
		return sent;
	}

	/**
	 * Delete.
	 * 
	 * @param tag
	 *            the tag
	 * @param number
	 *            the number
	 * @return the int
	 */
	public static int delete(String tag, String number) {
		return Bean.delete("tag=? and number=?", new Object[] { tag, number },
				Alert.class);
	}

}
