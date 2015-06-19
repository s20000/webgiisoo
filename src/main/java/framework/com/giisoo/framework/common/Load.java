/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.X;

@DBMapping(table = "tblload")
public class Load extends Bean {

	String name;
	String node;
	int count;
	long updated;

	/**
	 * Update.
	 * 
	 * @param name
	 *            the name
	 * @param node
	 *            the node
	 * @param count
	 *            the count
	 * @return the int
	 */
	public static int update(String name, String node, int count) {
		if (Bean.exists("name=? and node=?", new Object[] { name, node },
				Load.class)) {
			return Bean.update(
					"name=? and node=?",
					new Object[] { name, node },
					V.create("count", count).set("updated",
							System.currentTimeMillis()), Load.class);
		} else {
			return Bean.insert(
					V.create("count", count)
							.set("updated", System.currentTimeMillis())
							.set("name", name).set("node", node), Load.class);
		}
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		name = r.getString("name");
		node = r.getString("node");
		count = r.getInt("count");
		updated = r.getLong("updated");
	}

	public String getName() {
		return name;
	}

	public String getNode() {
		return node;
	}

	public int getCount() {
		return count;
	}

	public long getUpdated() {
		return updated;
	}

	/**
	 * Last.
	 * 
	 * @param name
	 *            the name
	 * @return the load
	 */
	public static Load last(String name) {
		return Bean
				.load("name =? and updated>?",
						new Object[] { name,
								System.currentTimeMillis() - 2 * X.AMINUTE },
						"order by count", Load.class);
	}

	/**
	 * Top.
	 * 
	 * @param name
	 *            the name
	 * @return the load
	 */
	public static Load top(String name) {
		return Bean
				.load("name=? and updated>?",
						new Object[] { name,
								System.currentTimeMillis() - 2 * X.AMINUTE },
						"order by count desc", Load.class);
	}

}
