/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.*;

import com.giisoo.core.bean.*;

@DBMapping(table = "tblrank")
public class Rank extends Bean {

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	/**
	 * the object name of the refer
	 */
	String obj;

	/**
	 * refer id is unique which means one object only can be review once
	 */
	int refer;

	/**
	 * the user id who was reviewed
	 */
	int uid;

	/**
	 * the reviewer id
	 */
	int reviewer;

	/**
	 * rank
	 */
	int rank;

	/**
	 * comment
	 */
	String comment;

	/**
	 * created timestamp
	 */
	long created;

	public String getObj() {
		return obj;
	}

	public int getRefer() {
		return refer;
	}

	public int getUid() {
		return uid;
	}

	public int getReviewer() {
		return reviewer;
	}

	public int getRank() {
		return rank;
	}

	public String getComment() {
		return comment;
	}

	public long getCreated() {
		return created;
	}

	/**
	 * Load by reviewer.
	 * 
	 * @param reviewer
	 *            the reviewer
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 * @return the beans
	 */
	public static Beans<Rank> loadByReviewer(int reviewer, int offset, int limit) {
		return Bean.load("reviewer=?", new Object[] { reviewer },
				"order by created desc", offset, limit, Rank.class);
	}

	/**
	 * Load by uid.
	 * 
	 * @param uid
	 *            the uid
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 * @return the beans
	 */
	public static Beans<Rank> loadByUid(int uid, int offset, int limit) {
		return Bean.load("uid=?", new Object[] { uid },
				"order by created desc", offset, limit, Rank.class);
	}

	/**
	 * Load.
	 * 
	 * @param obj
	 *            the obj
	 * @param refer
	 *            the refer
	 * @return the rank
	 */
	public static Rank load(String obj, int refer) {
		return Bean.load("tblrank", "obj=? and refer=?", new Object[] { obj,
				refer }, Rank.class);
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		obj = r.getString("obj");
		refer = r.getInt("refer");
		uid = r.getInt("uid");
		reviewer = r.getInt("reviewer");
		rank = r.getInt("rank");
		comment = r.getString("comment");
		created = r.getLong("created");
	}

	/**
	 * Creates the.
	 * 
	 * @param obj
	 *            the obj
	 * @param refer
	 *            the refer
	 * @param uid
	 *            the uid
	 * @param reviewer
	 *            the reviewer
	 * @param rank
	 *            the rank
	 * @param comment
	 *            the comment
	 */
	public static void create(String obj, int refer, int uid, int reviewer,
			int rank, String comment) {
		if (!Bean.exists("obj=? and refer=?", new Object[] { obj, refer },
				Rank.class)) {
			Bean.insert(
					V.create("obj", obj).set("refer", refer).set("uid", uid)
							.set("reviewer", reviewer).set("rank", rank)
							.set("comment", comment)
							.set("created", System.currentTimeMillis()),
					Rank.class);
		}
	}

	/**
	 * Update.
	 * 
	 * @param obj
	 *            the obj
	 * @param refer
	 *            the refer
	 * @param rank
	 *            the rank
	 * @param comment
	 *            the comment
	 */
	public static void update(String obj, int refer, int rank, String comment) {
		Bean.update(
				"obj=? and refer=?",
				new Object[] { obj, refer },
				V.create("rank", rank).set("comment", comment)
						.set("created", System.currentTimeMillis()), Rank.class);
	}
}
