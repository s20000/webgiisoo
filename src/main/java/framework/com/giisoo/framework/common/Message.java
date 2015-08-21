/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.UID;

/**
 * Message
 * 
 * @author yjiang
 * 
 */
@DBMapping(table = "tblmessage")
public class Message extends Bean {

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	public static final String FLAG_NEW = "new";
	public static final String FLAG_MARK = "mark";
	public static final String FLAG_READ = "read";
	public static final String FLAG_DONE = "done";

	String id;
	long uid;
	int from_uid;

	String subject;
	String body;
	String clazz;

	long created;
	String flag;

	public String getId() {
		return id;
	}

	public long getUid() {
		return uid;
	}

	transient User from;

	public User getFrom() {
		if (from == null && from_uid >= 0) {
			from = User.loadById(from_uid);
		}
		return from;
	}

	public int getFrom_uid() {
		return from_uid;
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}

	public String getClazz() {
		return clazz;
	}

	public long getCreated() {
		return created;
	}

	public String getFlag() {
		return flag;
	}

	private static String getID(long uid, String clazz, String subject) {
		return UID.id(uid, clazz, subject, System.currentTimeMillis());
	}

	/**
	 * Creates the.
	 * 
	 * @param uid
	 *            the uid
	 * @param clazz
	 *            the clazz
	 * @param subject
	 *            the subject
	 * @param refer
	 *            the refer
	 * @param from
	 *            the from
	 * @return the int
	 */
	public static int create(long uid, Class<?> clazz, String subject,
			String refer, int from) {

		String id = getID(uid, null, subject);
		return Bean.insert(
				V.create("uid", uid).set("clazz", clazz.getName())
						.set("subject", subject).set("body", refer)
						.set("id", id).set("flag", FLAG_NEW)
						.set("from_uid", from)
						.set("created", System.currentTimeMillis()),
				Message.class);
	}

	/**
	 * Creates the.
	 * 
	 * @param uid
	 *            the uid
	 * @param clazz
	 *            the clazz
	 * @param subject
	 *            the subject
	 * @param where
	 *            the where
	 * @param args
	 *            the args
	 * @param from
	 *            the from
	 * @return the int
	 */
	public static int create(long uid, Class<?> clazz, String subject,
			String where, Object[] args, int from) {

		JSONObject jo = new JSONObject();
		jo.put("where", where);
		jo.put("args", args);

		return create(uid, clazz, subject, jo.toString(), from);

	}

	/**
	 * Creates the.
	 * 
	 * @param uid
	 *            the uid
	 * @param subject
	 *            the subject
	 * @param body
	 *            the body
	 * @param from
	 *            the from
	 * @return the int
	 */
	public static int create(long uid, String subject, String body, int from) {
		String id = getID(uid, null, subject);
		return Bean.insert(
				V.create("uid", uid).set("subject", subject).set("body", body)
						.set("id", id).set("flag", FLAG_NEW)
						.set("from_uid", from)
						.set("created", System.currentTimeMillis()),
				Message.class);
	}

	/**
	 * Creates the.
	 * 
	 * @param uid
	 *            the uid
	 * @param id
	 *            the id
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int create(long uid, String id, V v) {
		if (!Bean.exists("uid=? and id=?", new Object[] { uid, id },
				Message.class)) {
			return Bean.insert(v.set("uid", uid).set("id", id), Message.class);
		}

		return 0;
	}

	/**
	 * Load.
	 * 
	 * @param uid
	 *            the uid
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<Message> load(long uid, int s, int n) {
		return Bean.load("uid=?", new Object[] { uid },
				"order by created desc", s, n, Message.class);
	}

	/**
	 * Load.
	 * 
	 * @param uid
	 *            the uid
	 * @param w
	 *            the w
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<Message> load(long uid, W w, int s, int n) {
		w.and("uid", uid);

		return Bean.load(w.where(), w.args(),
				w.orderby() == null ? "order by created desc" : w.orderby(), s,
				n, Message.class);
	}

	/**
	 * Load.
	 * 
	 * @param uid
	 *            the uid
	 * @param clazz
	 *            the clazz
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<Message> load(long uid, String clazz, int s, int n) {
		return Bean.load("uid=? and clazz=?", new Object[] { uid, clazz },
				"order by created desc", s, n, Message.class);
	}

	/**
	 * Group.
	 * 
	 * @param uid
	 *            the uid
	 * @param w
	 *            the w
	 * @return the list
	 */
	public static List<Group> group(long uid, W w) {
		w.and("uid", uid, W.OP_EQ);
		return Bean.load("tblmessage", new String[] { "clazz", "count(*) t" },
				w.where(), w.args(), "group by clazz", -1, -1, Group.class,
				null);
	}

	/**
	 * Group.
	 * 
	 * @param uid
	 *            the uid
	 * @param flag
	 *            the flag
	 * @return the list
	 */
	public static List<Group> group(long uid, String flag) {
		return Bean.load("tblmessage", new String[] { "clazz", "count(*) t" },
				"uid=? and flag=?", new Object[] { uid, flag },
				"group by clazz", -1, -1, Group.class, null);
	}

	/**
	 * Update.
	 * 
	 * @param uid
	 *            the uid
	 * @param id
	 *            the id
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int update(long uid, String id, V v) {
		return Bean.update("uid=? and id=?", new Object[] { uid, id }, v,
				Message.class);
	}

	/**
	 * Update.
	 * 
	 * @param v
	 *            the v
	 * @return the int
	 */
	public int update(V v) {
		return update(uid, id, v);
	}

	/**
	 * Delete.
	 * 
	 * @param uid
	 *            the uid
	 * @param id
	 *            the id
	 * @return the int
	 */
	public static int delete(long uid, String id) {
		return Bean.delete("uid=? and id=?", new Object[] { uid, id },
				Message.class);
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {

		id = r.getString("id");
		uid = r.getLong("uid");
		from_uid = r.getInt("from_uid");

		subject = r.getString("subject");
		body = r.getString("body");
		clazz = r.getString("clazz");

		created = r.getLong("created");
		flag = r.getString("flag");

	}

	public static class Group extends Bean {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		String clazz;
		int count;

		public String getClazz() {
			return clazz;
		}

		public int getCount() {
			return count;
		}

		/* (non-Javadoc)
		 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
		 */
		@Override
		protected void load(ResultSet r) throws SQLException {
			clazz = r.getString("clazz");
			count = r.getInt("t");
		}

	}

	/**
	 * Load.
	 * 
	 * @param uid
	 *            the uid
	 * @param id
	 *            the id
	 * @return the message
	 */
	public static Message load(long uid, String id) {
		return Bean.load("uid=? and id=?", new Object[] { uid, id },
				Message.class);
	}
}
