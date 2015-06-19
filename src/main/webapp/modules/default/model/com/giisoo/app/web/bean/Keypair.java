/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.bean;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.utils.base.RSA;
import com.giisoo.utils.base.RSA.Key;

@DBMapping(table = "tblkeypair")
public class Keypair extends Bean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	long created;
	String memo;
	int length;

	String pubkey;
	String prikey;

	/**
	 * Creates the.
	 * 
	 * @param length
	 *            the length
	 * @param memo
	 *            the memo
	 * @return the long
	 */
	public static long create(int length, String memo) {

		Key k = RSA.generate(length);
		if (k != null) {
			long created = System.currentTimeMillis();
			if (Bean.insert(
					V.create("created", created).set("length", length)
							.set("memo", memo).set("pubkey", k.pub_key)
							.set("prikey", k.pri_key), Keypair.class) > 0) {
				return created;
			}
		}

		return 0;
	}

	/**
	 * Load.
	 * 
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<Keypair> load(int s, int n) {
		return Bean.load(null, null, "order by created desc", s, n,
				Keypair.class);
	}

	/**
	 * Update.
	 * 
	 * @param created
	 *            the created
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int update(long created, V v) {
		return Bean.update("created=?", new Object[] { created }, v,
				Keypair.class);
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		created = r.getLong("created");
		memo = r.getString("memo");
		length = r.getInt("length");

		pubkey = r.getString("pubkey");
		prikey = r.getString("prikey");
	}

	public long getCreated() {
		return created;
	}

	public String getMemo() {
		return memo;
	}

	public int getLength() {
		return length;
	}

	public String getPubkey() {
		return pubkey;
	}

	public String getPrikey() {
		return prikey;
	}

	/**
	 * Load.
	 * 
	 * @param created
	 *            the created
	 * @return the keypair
	 */
	public static Keypair load(long created) {
		return Bean.load("tblkeypair", "created=?", new Object[] { created },
				Keypair.class);
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#toJSON(net.sf.json.JSONObject)
	 */
	@Override
	public boolean toJSON(JSONObject jo) {
		jo.put("created", created);
		jo.put("memo", memo);
		jo.put("length", length);
		jo.put("pubkey", pubkey);
		jo.put("prikey", prikey);
		return true;
	}

}
