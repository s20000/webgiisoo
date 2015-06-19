/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.Exportable;
import com.giisoo.core.bean.IData;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;

@DBMapping(table = "tbldict")
public class Dict extends Bean implements Exportable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String id;
	String parent;

	String name;
	String display;
	String clazz;
	int seq;
	int count;
	long updated;

	/**
	 * Removes the.
	 * 
	 * @param id
	 *            the id
	 * @return the int
	 */
	public static int remove(String id) {
		return Bean.delete("id=?", new Object[] { id }, Dict.class);
	}

	/**
	 * Load.
	 * 
	 * @param id
	 *            the id
	 * @return the dict
	 */
	public static Dict load(String id) {
		return Bean.load("id=?", new Object[] { id }, Dict.class);
	}

	/**
	 * Insert or update.
	 * 
	 * @param id
	 *            the id
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int insertOrUpdate(String id, V v) {
		int i = 0;
		if (Bean.exists("id=?", new Object[] { id }, Dict.class)) {
			i = Bean.update("id=?", new Object[] { id },
					v.set("updated", System.currentTimeMillis()), Dict.class);
		} else {
			i = Bean.insert(
					v.set("id", id).set("updated", System.currentTimeMillis()),
					Dict.class);
			if (i > 0) {
				onChanged("tbldict", IData.OP_CREATE, "id=?",
						new Object[] { id });
			}
		}

		/**
		 * updated all parent id
		 */
		Dict d = Dict.load(id);
		if (d != null) {
			long updated = d.getUpdated();
			while (d != null && d.getParentdict() != null) {
				d = d.getParentdict();
				int count = d.count();
				Dict.update("id=?", new Object[] { d.getId() },
						V.create("updated", updated).set("count", count),
						Dict.class);
			}
		}
		return i;
	}

	/**
	 * Insert or update slient.
	 * 
	 * @param id
	 *            the id
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int insertOrUpdateSlient(String id, V v) {
		int i = 0;
		if (Bean.exists("id=?", new Object[] { id }, Dict.class)) {
			i = Bean.update("id=?", new Object[] { id }, v, Dict.class);
		} else {
			i = Bean.insert(v.set("id", id), Dict.class);
			if (i > 0) {
				onChanged("tbldict", IData.OP_CREATE, "id=?",
						new Object[] { id });
			}
		}

		return i;
	}

	private int count() {
		Beans<Dict> bs = Dict.load(id, 0, 1);
		if (bs != null && bs.getList() != null) {
			return bs.getTotal();
		}
		return 0;
	}

	/**
	 * Load.
	 * 
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 * @return the beans
	 */
	public static Beans<Dict> load(int offset, int limit) {
		return Bean.load(null, null, "order by id", offset, limit, Dict.class);
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Exportable#load(java.lang.String, java.lang.Object[], int, int)
	 */
	public Beans<Dict> load(String where, Object[] args, int offset, int limit) {
		return Bean.load(where, args, "order by id", offset, limit, Dict.class);
	}

	/**
	 * Load.
	 * 
	 * @param parent
	 *            the parent
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 * @return the beans
	 */
	public static Beans<Dict> load(String parent, int offset, int limit) {
		return Bean.load("parent=?", new Object[] { parent }, "order by seq",
				offset, limit, Dict.class);
	}

	/**
	 * Load.
	 * 
	 * @param w
	 *            the w
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 * @return the beans
	 */
	public static Beans<Dict> load(W w, int offset, int limit) {
		return Bean.load(w.where(), w.args(), "order by seq", offset, limit,
				Dict.class);
	}

	/**
	 * Find.
	 * 
	 * @param parent
	 *            the parent
	 * @param name
	 *            the name
	 * @return the dict
	 */
	public static Dict find(String parent, String name) {
		Dict d = Bean.load("parent=? and name=?",
				new Object[] { parent, name }, Dict.class);
		if (d != null)
			return d;

		List<Dict> list = Bean.load((String[]) null, "parent=?",
				new Object[] { parent }, Dict.class);

		if (list != null) {
			for (Dict d1 : list) {
				Dict d2 = find(d1.getId(), name);
				if (d2 != null) {
					return d2;
				}
			}
		}
		return null;
	}

	/**
	 * Find sub.
	 * 
	 * @param parent
	 *            the parent
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 * @return the beans
	 */
	public static Beans<Dict> findSub(String parent, String name, String clazz,
			int offset, int limit) {
		return Bean.load("parent=? and name=? and clazz=?", new Object[] {
				parent, name, clazz }, "order by seq", offset, limit,
				Dict.class);
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		id = r.getString("id");
		parent = r.getString("parent");
		name = r.getString("name");
		display = r.getString("display");
		clazz = r.getString("clazz");
		seq = r.getInt("seq");
		count = r.getInt("count");
		updated = r.getLong("updated");
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#toJSON(net.sf.json.JSONObject)
	 */
	public boolean toJSON(JSONObject jo) {
		jo.put("id", id);
		jo.put("parent", parent);
		jo.put("name", name);
		jo.put("display", display);
		jo.put("clazz", clazz);
		jo.put("seq", seq);
		jo.put("count", count);
		jo.put("updated", updated);

		return true;
	}

	public String getId() {
		return id;
	}

	public int getCount() {
		return count;
	}

	/**
	 * Load all.
	 * 
	 * @param path
	 *            the path
	 * @return the list
	 */
	public static List<Dict> loadAll(String path) {

		if (X.isEmpty(path)) {
			return null;
		}

		String[] ss = path.split("\\|");
		List<Dict> list = null;

		for (int ii = 0; ii < ss.length; ii++) {
			String s = ss[ii];

			if (list == null || list.size() == 0) {
				list = new ArrayList<Dict>();
				Dict d = Dict.load(s);
				if (d != null) {
					list.add(d);
				}
			} else {
				for (int i = list.size() - 1; i >= 0; i--) {
					Dict d = list.remove(i);
					String parent = d.getId();
					String[] ss1 = s.split(",");
					try {
						for (String s1 : ss1) {
							d = Dict.find(parent, s1);

							if (d == null) {
								if (ii == ss.length - 1) {
									/**
									 * if this is last node then insert
									 */
									String id = UID.id(parent, s1);
									Dict.insertOrUpdate(
											id,
											V.create("id", id)
													.set("parent", parent)
													.set("name", s));
								}
							} else {
								list.add(d);
							}
						}
					} catch (Exception e) {
						log.error(path + "/" + s, e);
					}
				}
			}
		}
		return list;
	}

	/**
	 * Load by path.
	 * 
	 * @param path
	 *            the path
	 * @return the dict
	 */
	public static Dict loadByPath(String path) {
		if (X.isEmpty(path)) {
			return null;
		}

		String[] ss = path.split("\\|");
		Dict d = null;
		for (String s : ss) {
			if (d == null) {
				d = Dict.load(s);
			} else {
				String parent = d.getId();
				d = Dict.find(parent, s);
				if (d == null) {
					String id = UID.id(parent, s);
					Dict.insertOrUpdate(
							id,
							V.create("id", id).set("parent", parent)
									.set("name", s));
				}
			}
		}
		return d;
	}

	public String getPath() {
		if (getParentdict() != null) {
			return getParentdict().getPath() + "|" + name;
		}

		return name;
	}

	transient Dict parentdict;

	public Dict getParentdict() {
		if (parentdict == null && parent != null) {
			parentdict = Dict.load(parent);
		}
		return parentdict;
	}

	/**
	 * the root parent
	 * 
	 * @return Dict
	 */
	public Dict getRoot() {
		return Dict.load("root");
	}

	public String getParent() {
		return parent;
	}

	public long getUpdated() {
		return updated;
	}

	public String getName() {
		return name;
	}

	public String getDisplay() {
		if (X.isEmpty(display)) {
			return name;
		}
		return display;
	}

	public String getClazz() {
		return clazz;
	}

	public int getSeq() {
		return seq;
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Exportable#output(java.lang.String, java.lang.Object[], java.util.zip.ZipOutputStream)
	 */
	public JSONObject output(String where, Object[] args, ZipOutputStream out) {

		int s = 0;
		Beans<Dict> bs = Bean.load(where, args, null, s, 10, Dict.class);

		JSONObject jo = new JSONObject();
		JSONArray arr = new JSONArray();
		int count = 0;

		while (bs != null && bs.getList() != null && bs.getList().size() > 0) {

			for (Dict d : bs.getList()) {
				JSONObject j = new JSONObject();
				d.toJSON(j);

				j.convertStringtoBase64();

				arr.add(j);
				count++;

				/**
				 * get sub
				 */
				count += output(d.getId(), arr);
			}

			s += bs.getList().size();
			bs = Bean.load(where, args, null, s, 10, Dict.class);

		}

		jo.put("list", arr);
		jo.put("total", count);

		return jo;

	}

	private int output(String parent, JSONArray arr) {
		int count = 0;

		int s = 0;
		Beans<Dict> bs = Bean.load("parent=?", new Object[] { parent }, null,
				s, 10, Dict.class);

		while (bs != null && bs.getList() != null && bs.getList().size() > 0) {
			for (Dict d : bs.getList()) {
				JSONObject j = new JSONObject();
				d.toJSON(j);

				j.convertStringtoBase64();
				arr.add(j);

				count++;

				/**
				 * get sub
				 */
				count += output(d.getId(), arr);
			}

			s += bs.getList().size();
			bs = Bean.load("parent=?", new Object[] { parent }, null, s, 10,
					Dict.class);
		}

		return count;
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Exportable#input(net.sf.json.JSONArray, java.util.zip.ZipFile)
	 */
	public int input(JSONArray list, ZipFile in) {
		int count = 0;

		// insert or update the data
		int len = list.size();
		for (int i = 0; i < len; i++) {
			JSONObject jo = list.getJSONObject(i);

			jo.convertBase64toString();

			// log.debug(jo);

			String id = jo.getString("id");

			count += Dict.insertOrUpdateSlient(
					id,
					V.create().copy(jo, "id", "parent", "name", "display",
							"clazz", "seq", "updated"));
		}

		return count;
	}

	/**
	 * Total.
	 * 
	 * @param parent
	 *            the parent
	 * @return the int
	 */
	public static int total(String parent) {
		int c = 0;

		List<Dict> list = Bean.load(null, "parent=?", new Object[] { parent },
				null, 0, 100000, Dict.class);
		if (list != null && list.size() > 0) {
			c += list.size();

			for (Dict d : list) {
				c += total(d.id);
			}
		}

		return c;
	}

	/**
	 * Load sub.
	 * 
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public Beans<Dict> loadSub(int s, int n) {
		return Dict.load(id, s, n);
	}

	public String getExportableId() {
		return id;
	}

	public String getExportableName() {
		return name + ":" + display;
	}

	public long getExportableUpdated() {
		return updated;
	}

	public boolean isExportable() {
		return true;
	}

}
