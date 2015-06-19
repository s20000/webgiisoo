/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.*;
import java.util.*;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.DBMapping;

/**
 * Folder
 * 
 * @author yjiang
 * 
 */
@DBMapping(table = "tblfolder")
public class Folder extends Bean {

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	/**
	 * id of the folder which is unique in whole system
	 */
	int id;

	/**
	 * parent id
	 */
	int parent;

	/**
	 * name of the folder, which is used to load language also
	 */
	String name;

	/**
	 * tag of the folder, is used to indicate who create it and remove when
	 * module is remove
	 */
	String tag;

	/**
	 * title of the folder which is used to display in view
	 */
	String title;

	/**
	 * hot words
	 */
	String hot;

	/**
	 * recommend words
	 */
	String recommend;

	/**
	 * sequence of position
	 */
	int seq;

	/**
	 * up to 4KB
	 */
	String content;

	/**
	 * created timestamp
	 */
	long created;

	/**
	 * the required access for this folder, null or "" for all
	 */
	String access;

	/**
	 * Creates the.
	 * 
	 * @param parent
	 *            the parent
	 * @param name
	 *            the name
	 * @param tag
	 *            the tag
	 * @param title
	 *            the title
	 * @param hot
	 *            the hot
	 * @param recommend
	 *            the recommend
	 * @param content
	 *            the content
	 * @param seq
	 *            the seq
	 * @param access
	 *            the access
	 * @return the folder
	 */
	public static Folder create(int parent, String name, String tag,
			String title, String hot, String recommend, String content,
			int seq, String access) {
		/**
		 * same parent and name, only can be create once
		 */
		if (!Bean.exists("parent=? and name=?", new Object[] { parent, name },
				Folder.class)) {
			long now = System.currentTimeMillis();

			Bean.insert(
					V.create("parent", parent).set("name", name)
							.set("tag", tag).set("title", title)
							.set("hot", hot).set("recommend", recommend)
							.set("content", content).set("seq", seq)
							.set("created", now).set("access", access),
					Folder.class);
		}

		return Bean.load("tblfolder", "parent=? and name=?", new Object[] {
				parent, name }, Folder.class);

	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		id = r.getInt("id");
		parent = r.getInt("parent");
		name = r.getString("name");
		tag = r.getString("tag");
		title = r.getString("title");
		hot = r.getString("hot");
		recommend = r.getString("recommend");
		created = r.getLong("created");
		seq = r.getInt("seq");
		access = r.getString("access");
	}

	/* (non-Javadoc)
	 * @see com.giisoo.bean.Bean#toJSON(net.sf.json.JSONObject)
	 */
	@Override
	public boolean toJSON(JSONObject jo) {
		jo.put("id", id);
		jo.put("parent", parent);
		jo.put("name", name);
		jo.put("tag", tag);
		jo.put("title", title);
		jo.put("hot", hot);
		jo.put("recommend", recommend);
		jo.put("created", created);
		jo.put("seq", seq);
		jo.put("access", access);
		return true;
	}

	public int getId() {
		return id;
	}

	public int getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	public String getTag() {
		return tag;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * Gets the parent.
	 * 
	 * @param list
	 *            the list
	 * @return the parent
	 */
	public void getParent(List<Folder> list) {
		Folder f = Folder.load(parent);
		if (f != null) {
			list.add(0, f);
			f.getParent(list);
		}
	}

	public String getHot() {
		return hot;
	}

	public String getRecommend() {
		return recommend;
	}

	public int getSeq() {
		return seq;
	}

	public String getAccess() {
		return access;
	}

	public String getContent() {
		if (content == null) {
			content = Bean.getString("tblfolder", "content", "id=?",
					new Object[] { id }, null);
		}
		return content;
	}

	public long getCreated() {
		return created;
	}

	/**
	 * Load.
	 * 
	 * @param id
	 *            the id
	 * @return the folder
	 */
	public static Folder load(int id) {
		return Bean
				.load("tblfolder", "id=?", new Object[] { id }, Folder.class);
	}

	/**
	 * Subfolder.
	 * 
	 * @param parent
	 *            the parent
	 * @return the list
	 */
	public static List<Folder> subfolder(int parent) {
		return Bean.load(null, "parent=?", new Object[] { parent },
				"order by seq", 0, 100, Folder.class);
	}

	/**
	 * Update.
	 * 
	 * @param id
	 *            the id
	 * @param v
	 *            the v
	 */
	public static void update(int id, V v) {
		Bean.update("id=?", new Object[] { id }, v, Folder.class);
	}

	/**
	 * Subfolder.
	 * 
	 * @return the list
	 */
	public List<Folder> subfolder() {
		return subfolder(id);
	}

	/**
	 * Removes the.
	 * 
	 * @param id
	 *            the id
	 * @return true, if successful
	 */
	public static boolean remove(int id) {
		/**
		 * looking for all the subfolder and remove them
		 */
		List<Folder> list = subfolder(id);
		if (list != null && list.size() > 0) {
			for (Folder f : list) {
				remove(f.id);
			}
		}

		return Bean.delete("id=?", new Object[] { id }, Folder.class) > 0;
	}

	/**
	 * Filter access.
	 * 
	 * @param list
	 *            the list
	 * @param me
	 *            the me
	 */
	public static void filterAccess(List<Folder> list, User me) {
		if (list == null) {
			return;
		}

		int len = list.size();
		for (int i = len - 1; i >= 0; i--) {
			Folder f = list.get(i);

			if (f.access == null || f.access.length() == 0)
				continue;

			if (me != null && me.hasAccess(f.access))
				continue;

			/**
			 * not access for this folder, remove it
			 */
			list.remove(i);
		}
	}

	/**
	 * Load.
	 * 
	 * @param parent
	 *            the parent
	 * @param name
	 *            the name
	 * @return the folder
	 */
	public static Folder load(int parent, String name) {
		return Bean.load("tblfolder", "parent=? and name=?", new Object[] {
				parent, name }, Folder.class);
	}

}
