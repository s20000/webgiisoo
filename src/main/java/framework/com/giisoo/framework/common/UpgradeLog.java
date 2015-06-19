/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;

@DBMapping(table = "tblupgradelog")
public class UpgradeLog extends Bean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int FLAG_CLIENT = 0;
	public static final int FLAG_SERVER = 1;

	long created;
	String modules;
	String release;
	String build;
	String url;
	String host;
	String remote;
	int flag;

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		created = r.getLong("created");
		modules = r.getString("modules");
		release = r.getString("_release");
		build = r.getString("build");
		url = r.getString("url");
		host = r.getString("host");
		flag = r.getInt("flag");
		remote = r.getString("remote");
	}

	/**
	 * Insert.
	 * 
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int insert(V v) {
		return Bean.insert(v.set("created", System.currentTimeMillis()),
				UpgradeLog.class);
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
	public static Beans<UpgradeLog> load(int s, int n) {
		return Bean.load(null, null, "order by created desc", s, n,
				UpgradeLog.class);
	}

	/**
	 * Cleanup.
	 * 
	 * @param max
	 *            the max
	 * @param min
	 *            the min
	 * @return the int
	 */
	public static int cleanup(int max, int min) {
		Beans<UpgradeLog> bs = load(0, 1);
		int total = bs.getTotal();
		if (total > max) {
			long created = Bean.getOne("tblupgradelog", "created", null, null,
					"order by created desc", min, null);
			if (created > 0) {
				return Bean.delete("created <?", new Object[] { created },
						UpgradeLog.class);
			}
		}

		return 0;
	}

	public String getRemote() {
		return remote;
	}

	public long getCreated() {
		return created;
	}

	public String getModules() {
		return modules;
	}

	public String getRelease() {
		return release;
	}

	public String getBuild() {
		return build;
	}

	public String getUrl() {
		return url;
	}

	public String getHost() {
		return host;
	}

	public int getFlag() {
		return flag;
	}

}
