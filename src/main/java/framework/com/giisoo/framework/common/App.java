/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.X;

@DBMapping(table = "tblapp")
public class App extends Bean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String appid;
	String key;
	String memo;
	String company;
	String contact;
	String phone;
	String email;
	String logout;

	int locked;
	long lastlogin;
	long created;

	public String getLogout() {
		return logout;
	}

	public String getAppid() {
		return appid;
	}

	public String getKey() {
		return key;
	}

	public String getMemo() {
		return memo;
	}

	public String getCompany() {
		return company;
	}

	public String getContact() {
		return contact;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public int getLocked() {
		return locked;
	}

	public long getCreated() {
		return created;
	}

	/**
	 * Creates the.
	 * 
	 * @param appid
	 *            the appid
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int create(String appid, V v) {
		if (!Bean.exists("appid=?", new Object[] { appid }, App.class)) {
			return Bean.insert(
					v.set("appid", appid).set("created",
							System.currentTimeMillis()), App.class);
		}
		return 0;
	}

	/**
	 * Load.
	 * 
	 * @param appid
	 *            the appid
	 * @return the app
	 */
	public static App load(String appid) {
		return Bean.load("appid=?", new Object[] { appid }, App.class);
	}

	public boolean isLocked() {
		return locked > 0;
	}

	public long getLastlogin() {
		return lastlogin;
	}

	/**
	 * Update.
	 * 
	 * @param appid
	 *            the appid
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int update(String appid, V v) {
		return Bean.update("appid=?", new Object[] { appid }, v, App.class);
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		appid = r.getString("appid");
		key = r.getString("_key");
		memo = r.getString("memo");
		company = r.getString("company");
		contact = r.getString("contact");
		phone = r.getString("phone");
		email = r.getString("email");
		locked = r.getInt("locked");
		lastlogin = r.getLong("lastlogin");
		created = r.getLong("created");
		logout = r.getString("logout");
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#toJSON(net.sf.json.JSONObject)
	 */
	@Override
	public boolean toJSON(JSONObject jo) {
		jo.put("appid", appid);
		jo.put("key", key);
		jo.put("memo", memo);
		jo.put("company", company);
		jo.put("contact", contact);
		jo.put("phone", phone);
		jo.put("email", email);
		jo.put("locked", locked);
		jo.put("lastlogin", lastlogin);
		jo.put("created", created);
		jo.put("logout", logout);

		return true;
	}

	/**
	 * Load.
	 * 
	 * @param w
	 *            the w
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<App> load(W w, int s, int n) {
		return Bean.load(w == null ? null : w.where(),
				w == null ? null : w.args(), (w == null || X.isEmpty(w
						.orderby())) ? "order by appid" : w.orderby(), s, n,
				App.class);
	}

}
