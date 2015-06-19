/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.json.*;

import com.giisoo.core.bean.*;

/**
 * Menu
 * 
 * @author yjiang
 * 
 */
@DBMapping(table = "tblmenu")
public class Menu extends Bean {

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	int id;

	/**
	 * the name of the node, is the key of the display in language
	 */
	String name;

	/**
	 * 0: no child
	 */
	int childs;

	/**
	 * optional, the link of the menu
	 */
	String url;

	/**
	 * the class of the node
	 */
	String classes;

	/**
	 * the javascript when click
	 */
	String click;

	/**
	 * extra content associated this node
	 */
	String content;

	/**
	 * what's access need to access this menu
	 */
	String access;

	/**
	 * the sequence of the position
	 */
	int seq;

	String tip;

	/**
	 * Insert or update.
	 * 
	 * @param arr
	 *            the arr
	 * @param tag
	 *            the tag
	 */
	public static void insertOrUpdate(JSONArray arr, String tag) {
		if (arr == null) {
			return;
		}

		int len = arr.size();
		for (int i = 0; i < len; i++) {
			JSONObject jo = arr.getJSONObject(i);

			/**
			 * test and create from the "root"
			 */

			jo.put("tag", tag);
			insertOrUpdate(jo, 0);
		}
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getChilds() {
		return childs;
	}

	public String getUrl() {
		return url;
	}

	public String getClasses() {
		return classes;
	}

	public String getClick() {
		return click;
	}

	public String getContent() {
		return content;
	}

	public String getAccess() {
		return access;
	}

	/**
	 * Insert or update.
	 * 
	 * @param jo
	 *            the jo
	 * @param parent
	 *            the parent
	 */
	public static void insertOrUpdate(JSONObject jo, int parent) {
		try {
			// log.info(jo);

			String name = jo.containsKey("name") ? jo.getString("name") : null;
			if (name != null) {
				/**
				 * create menu if not exists
				 */
				V v = V.create().copy(jo, "url", "click", "classes", "content",
						"tag", "access", "seq", "tip");

				/**
				 * create the access if not exists
				 */
				if (jo.containsKey("access")) {
					String[] ss = jo.getString("access").split("[|&]");
					for (String s : ss) {
						Access.set(s);
					}
				}

				// log.debug(jo.toString());

				/**
				 * create the menu item is not exists
				 */
				Menu m = insertOrUpdate(parent, name, v);

				/**
				 * get all childs from the json
				 */
				if (jo.containsKey("childs")) {
					JSONArray arr = jo.getJSONArray("childs");
					int len = arr.size();
					for (int i = 0; i < len; i++) {
						JSONObject j = arr.getJSONObject(i);
						if (jo.containsKey("tag")) {
							j.put("tag", jo.get("tag"));
						}
						insertOrUpdate(j, m.getId());
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * test and create new menu if not exists
	 * 
	 * @param parent
	 * @param name
	 * @param url
	 * @param classes
	 * @param click
	 * @param content
	 * @return Menu
	 */
	private static Menu insertOrUpdate(int parent, String name, V v) {
		if (!Bean.exists("parent=? and name=?", new Object[] { parent, name },
				Menu.class)) {
			Bean.insert(v.set("parent", parent).set("name", name), Menu.class);

			/**
			 * and update the child of the parent menu
			 */
			Connection c = null;
			PreparedStatement stat = null;
			ResultSet r = null;

			try {
				c = Bean.getConnection();

				if (c != null) {
					/**
					 * count the childs
					 */
					stat = c.prepareStatement("select count(*) t from tblmenu where parent=?");
					stat.setInt(1, parent);
					r = stat.executeQuery();
					int childs = 0;
					if (r.next()) {
						childs = r.getInt("t");
					}
					r.close();
					r = null;
					stat.close();

					/**
					 * update the childs field
					 */
					stat = c.prepareStatement("update tblmenu set childs=? where id=?");
					stat.setInt(1, childs);
					stat.setInt(2, parent);
					stat.executeUpdate();
				} else {
					log.warn("no database confirgured!!!");
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				Bean.close(r, stat, c);
			}
		} else {
			/**
			 * update
			 */
			Bean.update("parent=? and name=?", new Object[] { parent, name },
					v, Menu.class);

		}

		return Bean.load("parent=? and name=?", new Object[] { parent, name },
				Menu.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		id = r.getInt("id");
		name = r.getString("name");
		url = r.getString("url");
		classes = r.getString("classes");
		click = r.getString("click");
		content = r.getString("content");
		childs = r.getInt("childs");
		access = r.getString("access");
		seq = r.getInt("seq");
		tip = r.getString("tip");
	}

	public String getTip() {
		return tip;
	}

	/**
	 * Submenu.
	 * 
	 * @param id
	 *            the id
	 * @return the beans
	 */
	public static Beans<Menu> submenu(int id) {
		// load it
		Beans<Menu> bb = Bean.load("parent=?", new Object[] { id },
				"order by seq", 0, -1, Menu.class);
		return bb;
	}

	/**
	 * Load.
	 * 
	 * @param parent
	 *            the parent
	 * @param name
	 *            the name
	 * @return the menu
	 */
	public static Menu load(int parent, String name) {
		Menu m = Bean.load("tblmenu", "parent=? and name=?", new Object[] {
				parent, name }, Menu.class);
		return m;
	}

	/**
	 * Submenu.
	 * 
	 * @return the beans
	 */
	public Beans<Menu> submenu() {
		return submenu(id);
	}

	/**
	 * Removes the by tag.
	 * 
	 * @param tag
	 *            the tag
	 */
	public void removeByTag(String tag) {
		Bean.delete("tag=?", new String[] { tag }, Menu.class);
	}

	/**
	 * Removes the.
	 * 
	 * @param id
	 *            the id
	 */
	public static void remove(int id) {
		Bean.delete("id=?", new Object[] { id }, Menu.class);

		/**
		 * remove all the sub
		 */
		Beans<Menu> bs = submenu(id);
		List<Menu> list = bs.getList();

		if (list != null) {
			for (Menu m : list) {
				remove(m.getId());
			}
		}
	}

	/**
	 * Filter access.
	 * 
	 * @param list
	 *            the list
	 * @param me
	 *            the me
	 * @return the collection
	 */
	public static Collection<Menu> filterAccess(List<Menu> list, User me) {
		if (list == null) {
			return null;
		}

		/**
		 * filter according the access, and save seq
		 */
		Map<Integer, Menu> map = new TreeMap<Integer, Menu>();

		for (Menu m : list) {

			boolean has = false;
			if (X.isEmpty(m.access)) {
				has = true;
			}

			if (!has && me != null) {
				if (m.access.indexOf("|") > 0) {
					String[] ss = m.access.split("\\|");
					if (me.hasAccess(ss)) {
						has = true;
					}
				} else if (m.access.indexOf("&") > 0) {
					String[] ss = m.access.split("\\&");
					for (String s : ss) {
						if (!me.hasAccess(s)) {
							has = false;
							break;
						}
					}
				} else if (me.hasAccess(m.access)) {
					has = true;
				}
			}

			if (has) {
				int seq = m.seq;
				Menu m1 = map.get(seq);
				if (m1 != null) {
					/**
					 * get short's name first
					 */
					if (m1.name.indexOf(m.name) > -1) {
						map.put(seq, m);
					} else if (m.name.indexOf(m1.name) > -1) {
						map.put(seq, m1);
					} else {
						map.put(seq + 1, m);
					}
				} else {
					map.put(seq, m);
				}
			}
		}

		return map.values();
	}

	/**
	 * Removes the.
	 * 
	 * @param tag
	 *            the tag
	 */
	public static void remove(String tag) {
		Bean.delete("tag=?", new Object[] { tag }, Menu.class);
	}

	/**
	 * Reset.
	 */
	public static void reset() {
		Bean.update(null, null, V.create("seq", -1), Menu.class);
	}

	/**
	 * Cleanup.
	 */
	public static void cleanup() {
		Bean.delete("seq<0", null, Menu.class);
	}
}
