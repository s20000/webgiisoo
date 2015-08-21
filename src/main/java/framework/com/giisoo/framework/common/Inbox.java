/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.*;
import java.util.*;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.*;

/**
 * "message" inbox which may including message, subject, task of anythings which
 * owner is the user
 * 
 * @author yjiang
 * 
 */
@DBMapping(table = "tblinbox")
public class Inbox extends Bean {

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	public static final int FLAG_NEW = 1;
	public static final int FLAG_UNREAD = 2;
	public static final int FLAG_READ = 3;

	public static final int STATE_NEW = 1;
	public static final int STATE_SENDING = 2;
	public static final int SEND_DONE = 3;

	String id;

	/**
	 * user id
	 */
	long uid;

	/**
	 * created timestamp
	 */
	long created;

	/**
	 * object of the "message"
	 */
	transient Bean bean;

	String clazz;

	/**
	 * this refer string of the obj, json string
	 */
	String refer;

	/**
	 * state
	 */
	int flag;

	int attempt;

	long updated;

	int state;

	String comment;

	public String getComment() {
		return comment;
	}

	public String getId() {
		return id;
	}

	public long getUid() {
		return uid;
	}

	transient User user;

	public User getUser() {
		if (user == null) {
			user = User.loadById(uid);
		}
		return user;
	}

	public long getCreated() {
		return created;
	}

	public Bean getBean() {
		if (bean == null) {
			try {
				Bean b = (Bean) (Class.forName(clazz).newInstance());
				JSONObject jo = JSONObject.fromObject(refer);
				if (jo.containsKey("where") && jo.containsKey("args")) {
					if (b.load(jo.getString("where"), jo.get("args"))) {
						bean = b;
					}
				}
			} catch (Exception e) {
				log.error(clazz + "[" + refer + "]", e);
			}
		}
		return bean;
	}

	public String getClazz() {
		return clazz;
	}

	public String getRefer() {
		return refer;
	}

	public int getFlag() {
		return flag;
	}

	public int getAttempt() {
		return attempt;
	}

	public long getUpdated() {
		return updated;
	}

	/**
	 * Update.
	 * 
	 * @param id
	 *            the id
	 * @param v
	 *            the v
	 */
	public static void update(String id, V v) {
		Bean.update("id=?", new Object[] { id }, v, Inbox.class);
	}

	/**
	 * Creates the.
	 * 
	 * @param tos
	 *            the tos
	 * @param clazz
	 *            the clazz
	 * @param refer
	 *            the refer
	 */
	public static void create(Object[] tos, Class<? extends Bean> clazz,
			String refer) {
		Set<Object> ss = new HashSet<Object>();

		// cut off the duplicate
		for (Object to : tos) {
			if (!ss.contains(to)) {
				ss.add(to);
			}
		}

		for (Object to : ss) {
			String id = UID.id(to, clazz, refer);
			Bean.insert(
					V.create("id", id).set("_to", to)
							.set("clazz", clazz.getName()).set("refer", refer)
							.set("created", System.currentTimeMillis())
							.set("flag", Inbox.FLAG_NEW), Inbox.class);
		}
	}

	/**
	 * Eldest.
	 * 
	 * @param me
	 *            the me
	 * @param flag
	 *            the flag
	 * @return the inbox
	 */
	public static Inbox eldest(int me, int flag) {
		return Bean.load("_to=? and flag=?", new Object[] { me, flag },
				"order by attempt, created", Inbox.class);
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		id = r.getString("id");
		uid = r.getLong("uid");
		created = r.getLong("created");
		clazz = r.getString("clazz");
		refer = r.getString("refer");
		flag = r.getInt("flag");
		attempt = r.getInt("attempt");
		state = r.getInt("state");
		comment = r.getString("comment");
	}

	/**
	 * Load new.
	 * 
	 * @param uid
	 *            the uid
	 * @return the beans
	 */
	public static Beans<Inbox> loadNew(long uid) {
		return Bean.load("uid=? and flag=?", new Object[] { uid, FLAG_NEW },
				"order by created", 0, 1, Inbox.class);
	}

}
