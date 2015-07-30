/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.giisoo.core.bean.*;

/**
 * Access, name will used to load language
 * 
 * @author yjiang
 * 
 */
@DBMapping(table = "tblaccess")
public class Access extends Bean implements Exportable {
	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	String id;
	String name;

	/**
	 * Group name.
	 * 
	 * @return the string
	 */
	public String groupName() {
		int i = name.indexOf(".");
		if (i > 0) {
			int j = name.indexOf(".", i + 1);
			if (j > 0) {
				return name.substring(0, j);
			} else {
				return name.substring(0, i);
			}
		}
		return "access";
	}

	public String getName() {
		return name;
	}

	/**
	 * Add a access name, the access name MUST fit with "access.[group].[name]"
	 * .
	 * 
	 * @param name
	 *            the name
	 */
	public static void set(String name) {
		if (X.isEmpty(name) || !name.startsWith("access.")) {
			
			log.error("error access.name: " + name, new Exception(
					"error access name:" + name));
			
		} else if (!exists(name)) {

			if (Bean.insert(V.create("name", name).set("id", name),
					Access.class) > 0) {
				Bean.onChanged("tblaccess", IData.OP_CREATE, "name=?",
						new Object[] { name });
			}

		}
	}

	static private Set<String> cache = new HashSet<String>();

	public static boolean exists(String name) {
		if (cache.contains(name)) {
			return true;
		}

		if (Bean.exists("name=?", new Object[] { name }, Access.class)) {
			cache.add(name);
			return true;
		}
		return false;
	}

	/**
	 * Load all access and group by [group] name
	 * 
	 * @return the map
	 */
	public static Map<String, List<Access>> load() {
		List<Access> list = Bean.load(null, "name<>?",
				new Object[] { "access.admin" }, "order by name", 0, -1,
				Access.class);

		Map<String, List<Access>> r = new TreeMap<String, List<Access>>();
		String group = null;
		List<Access> last = null;
		for (Access a : list) {
			String name = a.groupName();
			if (group == null || !name.equals(group)) {
				group = name;
				last = new ArrayList<Access>();
				r.put(group, last);
			}
			last.add(a);
		}

		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		name = r.getString("name");
		id = r.getString("id");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Exportable#output(java.lang.String,
	 * java.lang.Object[], java.util.zip.ZipOutputStream)
	 */
	public JSONObject output(String where, Object[] args, ZipOutputStream out) {
		List<Access> list = Bean.load((String[]) null, where, args,
				Access.class);

		JSONObject jo = new JSONObject();
		JSONArray arr = new JSONArray();
		int count = 0;

		if (list != null && list.size() > 0) {
			for (Access d : list) {
				JSONObject j = new JSONObject();
				j.put("id", d.id);
				j.put("name", d.name);
				arr.add(j);
				count++;

			}
		}

		jo.put("list", arr);
		jo.put("total", count);

		return jo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Exportable#input(net.sf.json.JSONArray,
	 * java.util.zip.ZipFile)
	 */
	public int input(JSONArray list, ZipFile in) {
		int count = 0;
		int len = list.size();
		for (int i = 0; i < len; i++) {
			JSONObject name = list.getJSONObject(i);

			if (!exists(name.getString("name"))) {
				count += Bean.insert(
						V.create("name", name.get("name")).set("id",
								name.get("name")), Access.class);
			}
		}

		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Exportable#load(java.lang.String,
	 * java.lang.Object[], int, int)
	 */
	public Beans<Access> load(String where, Object[] args, int s, int n) {
		return Bean.load(where, args, "order by name", s, n, Access.class);
	}

	public String getExportableId() {
		return name;
	}

	public String getExportableName() {
		return name;
	}

	public long getExportableUpdated() {
		return 0;
	}

	public boolean isExportable() {
		return true;
	}

}
