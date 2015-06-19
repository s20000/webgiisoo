/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.Exportable;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;
import com.giisoo.framework.web.Module;
import com.giisoo.utils.base.Zip;
import com.sun.mail.imap.protocol.Item;

@DBMapping(table = "tblcart")
public class Cart extends Bean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int STATE_INIT = 0;
	public static final int STATE_PENDING = 1;
	public static final int STATE_EXPORTED = 2;
	public static final int STATE_IMPORTED = 3;
	public static final int STATE_DOWNLOAD = 5;
	public static final int STATE_FAIL = 4;

	String id;
	int uid;
	long exported;
	String destclazz;
	String destination;
	int state;
	int count;
	String no;
	String pubkey;
	String repo;
	String memo;
	long length;

	public void setNo(String no) {
		this.no = no;
	}

	/**
	 * Clean.
	 * 
	 * @param destclazz
	 *            the destclazz
	 * @param destination
	 *            the destination
	 * @param uid
	 *            the uid
	 * @return the int
	 */
	public static int clean(String destclazz, String destination, int uid) {
		return Bean.delete(
				"state=? and destclazz=? and destination=? and uid=?",
				new Object[] { Cart.STATE_INIT, destclazz, destination, uid },
				Cart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Bean#toJSON(net.sf.json.JSONObject)
	 */
	@Override
	public boolean toJSON(JSONObject jo) {
		jo.put("id", id);
		jo.put("exported", exported);
		jo.put("destclazz", destclazz);
		jo.put("destination", destination);
		jo.put("count", count);
		jo.put("no", no);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Bean#fromJSON(net.sf.json.JSONObject)
	 */
	@Override
	public boolean fromJSON(JSONObject jo) {
		id = jo.has("id") ? jo.getString("id") : null;
		exported = jo.has("exported") ? jo.getLong("exported") : 0;
		destclazz = jo.has("destclazz") ? jo.getString("destclazz") : null;
		destination = jo.has("destination") ? jo.getString("destination")
				: null;
		count = jo.has("count") ? jo.getInt("count") : 0;
		no = jo.has("no") ? jo.getString("no") : null;
		return true;

	}

	public long getLength() {
		return length;
	}

	public String getMemo() {
		return memo;
	}

	public int getCount() {
		return count;
	}

	public String getPubkey() {
		return pubkey;
	}

	/**
	 * Copy.
	 * 
	 * @param uid
	 *            the uid
	 * @param destclazz
	 *            the destclazz
	 * @param destination
	 *            the destination
	 */
	public void copy(int uid, String destclazz, String destination) {
		List<Item> list = this.getList();
		if (list != null) {
			for (Item e : list) {
				Cart.put(e.getClazz(), e.getRefer(), uid, e.getName(),
						e.getDescription(), e.getCount(), destclazz,
						destination);
			}
		}
	}

	/**
	 * Merge.
	 * 
	 * @param c
	 *            the c
	 */
	public void merge(Cart c) {
		for (Item i : c.getList()) {
			Cart.put(i.clazz, i.refer, uid, i.name, i.description, i.count,
					destclazz, destination);
		}
	}

	/**
	 * Copy.
	 * 
	 * @param uid
	 *            the uid
	 */
	public void copy(int uid) {
		copy(uid, X.EMPTY, X.EMPTY);
	}

	/**
	 * Current.
	 * 
	 * @param uid
	 *            the uid
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<Cart> current(int uid, int s, int n) {
		return Bean.load("uid=? and state=?", new Object[] { uid,
				Cart.STATE_INIT }, "order by destclazz, destination", s, n,
				Cart.class);
	}

	/**
	 * Count.
	 * 
	 * @param uid
	 *            the uid
	 * @return the int
	 */
	public static int count(int uid) {
		BigDecimal i = Bean.getOne("sum(count)", "uid=? and state=?",
				new Object[] { uid, Cart.STATE_INIT }, null, 0, Cart.class);
		return i == null ? 0 : i.intValue();
	}

	/**
	 * Current.
	 * 
	 * @param uid
	 *            the uid
	 * @param destclazz
	 *            the destclazz
	 * @param destination
	 *            the destination
	 * @return the cart
	 */
	public static Cart current(int uid, String destclazz, String destination) {
		return Bean.load("tblcart",
				"uid=? and state=? and destclazz=? and destination=?",
				new Object[] { uid, Cart.STATE_INIT, destclazz, destination },
				Cart.class);
	}

	/**
	 * Load.
	 * 
	 * @param id
	 *            the id
	 * @return the cart
	 */
	public static Cart load(String id) {
		return Bean.load("tblcart", "id=?", new Object[] { id }, Cart.class);
	}

	/**
	 * Load exported.
	 * 
	 * @param uid
	 *            the uid
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<Cart> loadExported(int uid, int s, int n) {
		return Bean.load("uid=? and state in (?, ?, ?, ?)", new Object[] { uid,
				Cart.STATE_PENDING, Cart.STATE_FAIL, Cart.STATE_EXPORTED,
				Cart.STATE_DOWNLOAD }, "order by exported desc", s, n,
				Cart.class);
	}

	/**
	 * Load.
	 * 
	 * @param id
	 *            the id
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<Item> load(String id, int s, int n) {
		Beans<Item> bs = Bean.load("cartid=?", new Object[] { id },
				"order by clazz, created desc", s, n, Item.class);
		if (bs != null) {
			Bean.update("id=?", new Object[] { id },
					V.create("count", bs.getTotal()), Cart.class);
		}

		return bs;
	}

	/**
	 * get the items in the cart
	 * 
	 * @return List<Item>
	 */
	public List<Item> getList() {
		return Bean.load(null, "cartid=?", new Object[] { id },
				"order by clazz, created desc", 0, -1, Item.class);
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
	public Beans<Item> load(int s, int n) {
		return load(id, s, n);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		id = r.getString("id");
		destclazz = r.getString("destclazz");
		destination = r.getString("destination");
		uid = r.getInt("uid");
		exported = r.getLong("exported");
		state = r.getInt("state");
		count = r.getInt("count");
		no = r.getString("no");
		pubkey = r.getString("pubkey");
		repo = r.getString("repo");
		memo = r.getString("memo");
		length = r.getLong("length");
	}

	public String getDestclazz() {
		return destclazz;
	}

	public String getDestination() {
		return destination;
	}

	public String getId() {
		return id;
	}

	public int getUid() {
		return uid;
	}

	public long getExported() {
		return exported;
	}

	public String getRepo() {
		return repo;
	}

	public String getNo() {
		return no;
	}

	public int getState() {
		return state;
	}

	/**
	 * Removes the.
	 * 
	 * @param cid
	 *            the cid
	 * @param id
	 *            the id
	 * @param uid
	 *            the uid
	 * @return the int
	 */
	public static int remove(String cid, String id, int uid) {
		if (id != null) {
			int r = Bean.delete("cartid=? and id=?", new Object[] { cid, id },
					Item.class);
			if (r > 0) {
				Bean.update("tblcart", "count=count -1", "id=?",
						new Object[] { cid }, null);
			}
			return r;
		}
		return 0;
	}

	/**
	 * Removes the.
	 * 
	 * @param cid
	 *            the cid
	 * @param uid
	 *            the uid
	 * @return the int
	 */
	public static int remove(String cid, int uid) {
		return Bean.delete("tblcart", "id=? and uid=?",
				new Object[] { cid, uid }, null);
	}

	/**
	 * Export.
	 * 
	 * @param destclazz
	 *            the destclazz
	 * @param destination
	 *            the destination
	 * @param no
	 *            the no
	 * @param pubkey
	 *            the pubkey
	 * @return the int
	 */
	public int export(String destclazz, String destination, String no,
			String pubkey) {
		long t = System.currentTimeMillis();
		int r = update(
				id,
				uid,
				V.create("no", no).set("destclazz", destclazz)
						.set("destination", destination)
						.set("state", Cart.STATE_PENDING).set("exported", t)
						.set("pubkey", pubkey));

		if (r > 0) {
			this.no = no;
			this.destclazz = destclazz;
			this.destination = destination;
			this.state = Cart.STATE_PENDING;
			this.pubkey = pubkey;
			this.exported = t;
		}
		return r;
	}

	/**
	 * Update.
	 * 
	 * @param id
	 *            the id
	 * @param uid
	 *            the uid
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int update(String id, int uid, V v) {
		return Bean.update("id=? and uid=?", new Object[] { id, uid }, v,
				Cart.class);
	}

	/**
	 * Put.
	 * 
	 * @param clazz
	 *            the clazz
	 * @param where
	 *            the where
	 * @param args
	 *            the args
	 * @param uid
	 *            the uid
	 * @param name
	 *            the name
	 * @param desc
	 *            the desc
	 * @param count
	 *            the count
	 * @return the string
	 */
	public static String put(String clazz, String where, Object[] args,
			int uid, String name, String desc, int count) {
		JSONObject jo = new JSONObject();
		jo.put("where", where);
		jo.put("args", args);

		String refer = jo.toString();

		return put(clazz, refer, uid, name, desc, count, X.EMPTY, X.EMPTY);

	}

	/**
	 * Put.
	 * 
	 * @param clazz
	 *            the clazz
	 * @param where
	 *            the where
	 * @param args
	 *            the args
	 * @param uid
	 *            the uid
	 * @param name
	 *            the name
	 * @param desc
	 *            the desc
	 * @param count
	 *            the count
	 * @param destclazz
	 *            the destclazz
	 * @param destination
	 *            the destination
	 * @return the string
	 */
	public static String put(String clazz, String where, Object[] args,
			int uid, String name, String desc, int count, String destclazz,
			String destination) {

		JSONObject jo = new JSONObject();
		jo.put("where", where);
		jo.put("args", args);

		String refer = jo.toString();

		return put(clazz, refer, uid, name, desc, count, destclazz, destination);

	}

	/**
	 * put the data in the cart, if no cart, then cart a new one
	 * <p>
	 * return the cart id if success, otherwise return null
	 * 
	 * @param clazz
	 * @param refer
	 * @param uid
	 * @param name
	 * @param desc
	 * @return String
	 */
	private static String put(String clazz, String refer, int uid, String name,
			String desc, int count, String destclazz, String destination) {

		String cartid = Bean.getString("tblcart", "id",
				"state=? and destclazz=? and destination=? and uid=?",
				new Object[] { Cart.STATE_INIT, destclazz, destination, uid },
				null);

		if (cartid == null) {
			cartid = UID.id(uid, System.currentTimeMillis());

			Bean.insert(
					V.create("id", cartid).set("uid", uid)
							.set("destclazz", destclazz)
							.set("destination", destination)
							.set("state", Cart.STATE_INIT), Cart.class);
		}

		String id = UID.id(clazz, refer);
		if (!Bean.exists("cartid=? and id=?", new Object[] { cartid, id },
				Item.class)) {

			int r = Bean.insert(
					V.create("cartid", cartid).set("id", id)
							.set("clazz", clazz).set("refer", refer)
							.set("created", System.currentTimeMillis())
							.set("name", name).set("description", desc)
							.set("count", count), Item.class);

			if (r > 0) {
				Bean.update("tblcart", "count=count +1", "id=?",
						new Object[] { cartid }, null);

				return cartid;
			}

		} else {
			if (Bean.update(
					"cartid=? and id=?",
					new Object[] { cartid, id },
					V.create("created", System.currentTimeMillis())
							.set("name", name).set("description", desc)
							.set("count", count), Item.class) > 0) {
				return cartid;
			}
		}

		return null;
	}

	/**
	 * Cart Item object
	 * 
	 * @author joe
	 * 
	 */
	@DBMapping(table = "tblcartitem")
	public static class Item extends Bean {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		String cartid;
		String id;
		String name;
		String description;
		String clazz;
		String refer;
		int count;
		int state;
		String memo;
		long created;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder().append("Item[id=")
					.append(id).append(",name=").append(name).append(",clazz=")
					.append(clazz).append("]");
			return sb.toString();
		}

		/**
		 * Update.
		 * 
		 * @param v
		 *            the v
		 * @return the int
		 */
		public int update(V v) {
			return Bean.update("cartid=? and id=?",
					new Object[] { cartid, id }, v, Item.class);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
		 */
		@Override
		protected void load(ResultSet r) throws SQLException {
			cartid = r.getString("cartid");
			id = r.getString("id");
			clazz = r.getString("clazz");
			refer = r.getString("refer");
			created = r.getLong("created");
			name = r.getString("name");
			description = r.getString("description");
			state = r.getInt("state");
			memo = r.getString("memo");
			count = r.getInt("count");
		}

		/**
		 * Creates the.
		 * 
		 * @param id
		 *            the id
		 * @param clazz
		 *            the clazz
		 * @param refer
		 *            the refer
		 * @param name
		 *            the name
		 * @param description
		 *            the description
		 * @param destination
		 *            the destination
		 * @return the item
		 */
		public static Item create(String id, String clazz, String refer,
				String name, String description, String destination) {
			Item i = new Item();
			i.id = id;
			i.clazz = clazz;
			i.refer = refer;
			i.name = name;
			i.description = description;
			return i;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.bean.Bean#toJSON(net.sf.json.JSONObject)
		 */
		@Override
		public boolean toJSON(JSONObject jo) {
			jo.put("id", id);
			jo.put("clazz", clazz);
			jo.put("refer", refer);
			jo.put("name", name);
			jo.put("description", description);
			jo.put("count", count);
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.bean.Bean#fromJSON(net.sf.json.JSONObject)
		 */
		@Override
		public boolean fromJSON(JSONObject jo) {
			id = jo.has("id") ? jo.getString("id") : null;
			clazz = jo.has("clazz") ? jo.getString("clazz") : null;
			refer = jo.has("refer") ? jo.getString("refer") : null;
			name = jo.has("name") ? jo.getString("name") : null;
			description = jo.has("description") ? jo.getString("description")
					: null;
			count = jo.has("count") ? jo.getInt("count") : 0;
			return true;
		}

		public int getCount() {
			return count;
		}

		public int getState() {
			return state;
		}

		public String getMemo() {
			return memo;
		}

		public void setState(int state) {
			this.state = state;
		}

		public String getCartid() {
			return cartid;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public String getClazz() {
			return clazz;
		}

		transient Exportable obj;

		/**
		 * Input.
		 * 
		 * @param zip
		 *            the zip
		 * @return the int
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public int input(ZipFile zip) throws IOException {

			int count = 0;

			try {
				/**
				 * get the json data of the item
				 */
				JSONObject jo = Zip.getJSON(id + ".json", zip);

				jo.convertBase64toString();

				/**
				 * create the instance of bean to import the data
				 */
				Object o = Class.forName(clazz, true, Module.classLoader)
						.newInstance();
				if (o instanceof Exportable) {
					Exportable e = (Exportable) o;

					/**
					 * import the data using the bean
					 */
					count = e.input(jo.getJSONArray("list"), zip);

					this.memo = Integer.toString(count);
				} else {
					log.warn("o=" + o + ", clazz=" + clazz + ", instanceof="
							+ (o instanceof Exportable));
				}
				/**
				 * set the state to imported
				 */
				this.state = Cart.STATE_IMPORTED;

			} catch (Exception e) {
				log.error(e.getMessage(), e);

				/**
				 * put the error info back
				 */
				this.state = Cart.STATE_FAIL;
				this.memo = e.getMessage();
			}

			return count;
		}

		/**
		 * Output.
		 * 
		 * @return the JSON object
		 */
		public JSONObject output() {
			getObject();

			if (obj != null) {
				try {
					/**
					 * get the refer of data
					 */
					JSONObject jo = JSONObject.fromObject(refer);
					String where = jo.has("where") ? jo.getString("where")
							: null;
					Object[] args = jo.has("args") ? jo.getJSONArray("args")
							.toArray() : null;

					jo = obj.output(where, args, null);
					if (jo != null) {
						/**
						 * put the item info
						 */
						jo.put("name", name);
						jo.put("description", description);
						jo.put("clazz", obj.getClass().getName());
						jo.put("refer", refer);

						return jo;
					}
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			}

			return null;
		}

		/**
		 * Output.
		 * 
		 * @param zip
		 *            the zip
		 * @return true, if successful
		 */
		public boolean output(ZipOutputStream zip) {
			getObject();

			if (obj != null) {
				try {
					/**
					 * get the refer of data
					 */
					JSONObject jo = JSONObject.fromObject(refer);
					String where = jo.has("where") ? jo.getString("where")
							: null;
					Object[] args = jo.has("args") ? jo.getJSONArray("args")
							.toArray() : null;

					jo = obj.output(where, args, zip);
					if (jo != null) {
						/**
						 * put the item info
						 */
						jo.put("name", name);
						jo.put("description", description);
						jo.put("clazz", obj.getClass().getName());
						jo.put("refer", refer);

						jo.convertStringtoBase64();

						/**
						 * create the .json for the item
						 */
						ZipEntry e = new ZipEntry(id + ".json");
						zip.putNextEntry(e);
						zip.write(jo.toString().getBytes());
						zip.closeEntry();

						return true;
					}
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			}

			return false;
		}

		/**
		 * get the Exportable object
		 * 
		 * @return Exportable
		 */
		public Exportable getObject() {
			if (obj == null) {
				try {
					Class<?> c = Class.forName(clazz, true, Module.classLoader);
					if (c != null) {
						Object o = c.newInstance();
						if (o instanceof Exportable) {
							obj = (Exportable) o;
						}
					} else {
						log.error("not found class: " + clazz);
					}
				} catch (Throwable e) {
					log.error("\"" + clazz + "\"", e);
				}
			}
			return obj;
		}

		public String getRefer() {
			return refer;
		}

		public long getCreated() {
			return created;
		}

	}

}
