/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;
import com.giisoo.core.bean.Bean.W.Entity;
import com.giisoo.core.conf.Config;
import com.giisoo.core.index.PendIndex;
import com.giisoo.core.index.Searchable;
import com.giisoo.framework.common.User;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@DBMapping(collection = "article")
public class Article extends Searchable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String id;

	@Override
	public String getId() {
		return id;
	}

	/**
	 * Store.
	 * 
	 * @return the int
	 */
	public int store() {
		Map<String, Object> datas = this.getAll();
		V v = V.create();
		for (String name : datas.keySet()) {
			v.set(name, datas.get(name));
		}
		v.remove(X._ID);

		if (id == null) {
			id = UID.id(getString("title"), getString("body"));
			return create(id, v);
		} else {
			return update(id, v);
		}
	}

	/**
	 * Reindex.
	 */
	public static void reindex() {
		DBCollection c = Bean.getCollection(Bean.getCollection(Article.class));
		DBCursor cur = c.find();

		int i = 0;
		while (cur.hasNext()) {
			DBObject d = cur.next();
			String id = (String) d.get(X._ID);
			PendIndex.create(Article.class, id);
			i++;

			if (i % 1000 == 0) {
				log.debug("reindexed: " + i);
			}
		}

		cur.close();

		log.debug("reindexed, done: " + i);
	}

	/**
	 * Update.
	 * 
	 * @return the int
	 */
	public int update() {
		Map<String, Object> datas = this.getAll();
		V v = V.create();
		for (String name : datas.keySet()) {
			v.set(name, datas.get(name));
		}
		v.remove(X._ID);
		return update(id, v);
	}

	/**
	 * Delete.
	 * 
	 * @return the int
	 */
	public int delete() {
		return delete(id);
	}

	/**
	 * Delete.
	 * 
	 * @param id
	 *            the id
	 * @return the int
	 */
	public static int delete(String id) {
		int i = Bean.delete(new BasicDBObject(X._ID, id), Article.class);
		if (i > 0) {
			PendIndex.create(Article.class, id);
		}
		return i;
	}

	/**
	 * Creates the.
	 * 
	 * @param id
	 *            the id
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int create(String id, V v) {
		if (!Bean.exists(new BasicDBObject(X._ID, id), Article.class)) {
			int i = Bean.insertCollection(
					v.set(X._ID, id).set("created", System.currentTimeMillis())
							.set("updated", System.currentTimeMillis()),
					Article.class);
			if (i > 0) {
				PendIndex.create(Article.class, id);
			}
			return i;
		}
		return 0;
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
		if (Bean.load(new BasicDBObject(X._ID, id), Article.class) == null) {
			return create(id, v);
		} else {
			return update(id, v);
		}
	}

	/**
	 * Update.
	 * 
	 * @param id
	 *            the id
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int update(String id, V v) {
		return updateSilent(id, v.set("updated", System.currentTimeMillis()));
	}

	/**
	 * Update silent.
	 * 
	 * @param id
	 *            the id
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int updateSilent(String id, V v) {
		int i = Bean.updateCollection(id, v, Article.class);
		if (i > 0) {
			PendIndex.create(Article.class, id);
		}
		return i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.index.Searchable#load(java.lang.String)
	 */
	@Override
	public boolean load(String id) {
		return Bean.load(new BasicDBObject(X._ID, id), null, this) != null;
	}

	/**
	 * Load by id.
	 * 
	 * @param id
	 *            the id
	 * @return the article
	 */
	public static Article loadById(String id) {
		Article a = new Article();
		a.load(id);
		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Bean#load(com.mongodb.DBObject)
	 */
	@Override
	protected void load(DBObject d) {
		id = (String) d.get(X._ID);

		super.load(d);
	}

	/**
	 * Load.
	 * 
	 * @param clazz
	 *            the clazz
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<Article> load(String clazz, int s, int n) {
		return Bean.load(new BasicDBObject("clazz", clazz), new BasicDBObject(
				"updated", -1), s, n, Article.class);
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
	public static Beans<Article> load(W w, int s, int n) {
		BasicDBObject q = new BasicDBObject();
		BasicDBObject order = new BasicDBObject();
		if (w != null) {
			List<Entity> list = w.getAll();
			for (Entity e : list) {
				String name = e.getName();
				Object o = e.getValue();
				int op = e.getOp();

				switch (op) {
				case W.OP_EQ:
					q.append(name, o);
					break;
				case W.OP_GT:
					q.append(name, new BasicDBObject("$gt", o));
					break;
				case W.OP_LT:
					q.append(name, new BasicDBObject("$lt", o));
					break;
				}
			}

			if (w.orderby() != null) {
				order.append(w.orderby(), 1);
			}
		}

		return Bean.load(q, order, s, n, Article.class);

	}

	/**
	 * Latest.
	 * 
	 * @param clazz
	 *            the clazz
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<Article> latest(String clazz, int s, int n) {
		return Bean.load(new BasicDBObject("clazz", clazz), new BasicDBObject(
				"created", -1), s, n, Article.class);
	}

	/**
	 * Load.
	 * 
	 * @param query
	 *            the query
	 * @param order
	 *            the order
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<Article> load(DBObject query, DBObject order, int s,
			int n) {
		return Bean.load(query, order, s, n, Article.class);
	}

	/**
	 * Load.
	 * 
	 * @param clazz
	 *            the clazz
	 * @param parentid
	 *            the parentid
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<Article> load(String clazz, String parentid, int s,
			int n) {
		return Bean.load(
				new BasicDBObject("clazz", clazz).append("parentid", parentid),
				new BasicDBObject("created", -1), s, n, Article.class);
	}

	transient User user;

	public User getUser() {
		if (user == null) {
			long uid = getLong("uid");
			if (uid >= 0) {
				user = User.loadById(uid);
			}
		}

		return user;

	}

	static SearchableField[] searchablefields = null;

	@SuppressWarnings("unchecked")
	@Override
	public SearchableField[] getSearchableFields() {
		if (searchablefields == null) {
			Configuration conf = Config.getConfig();
			List<String> list = conf.getList("article.searchable");
			SearchableField[] tt = new SearchableField[list.size()];
			for (int i = 0; i < list.size(); i++) {
				String[] ss = list.get(i).split(":");
				if (ss.length > 2) {
					tt[i] = new SearchableField(ss[0], Integer.parseInt(ss[1]),
							Integer.parseInt(ss[2]));
				} else if (ss.length > 1) {
					tt[i] = new SearchableField(ss[0], Integer.parseInt(ss[1]));
				}
				if (ss.length > 3) {
					tt[i].display = "{" + ss[3] + "}";
				}
			}
			searchablefields = tt;
		}
		return searchablefields;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.index.Searchable#getValue(com.giisoo.index.Searchable.
	 * SearchableField)
	 */
	@Override
	public String getValue(SearchableField field) {
		Object o = get(field.field);
		return o == null ? X.EMPTY : o.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.index.Searchable#setValue(com.giisoo.index.Searchable.
	 * SearchableField, java.lang.String)
	 */
	@Override
	public void setValue(SearchableField field, String value) {
		set(field.field, value);
	}

}
