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
import com.giisoo.core.bean.UID;

@DBMapping(table = "tblnamemapping")
public class NameMapping extends Bean {
	String type;
	String name;
	String value;

	/**
	 * Gets the.
	 * 
	 * @param type
	 *            the type
	 * @param name
	 *            the name
	 * @return the string
	 */
	public static synchronized String get(String type, String name) {
		NameMapping e = Bean.load("type=? and (name=? or value=?)",
				new Object[] { type, name, name }, NameMapping.class);

		if (e == null) {
			String value = Long.toString(UID.next("namemapping." + type));

			Bean.insert(
					V.create("type", type).set("name", name)
							.set("value", value), NameMapping.class);

			return value;
		}

		if (name.equals(e.name)) {
			return e.value;
		}
		return e.name;
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		type = r.getString("type");
		name = r.getString("name");
		value = r.getString("value");
	}

}
