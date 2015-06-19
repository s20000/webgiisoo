/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.io.*;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.*;

import com.giisoo.core.bean.*;
import com.giisoo.framework.mdc.*;

/**
 * repository of file system
 * 
 * @author yjiang
 * 
 */
@DBMapping(table = "tblrepo")
public class Repo extends Bean {

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	static Log log = LogFactory.getLog(Repo.class);

	public static String ROOT;

	/**
	 * Inits the.
	 * 
	 * @param conf
	 *            the conf
	 */
	public static void init(Configuration conf) {
		ROOT = conf.getString("repo.path", "/opt/repo/f1");
	}

	/**
	 * Used.
	 * 
	 * @param u
	 *            the u
	 * @return the long
	 */
	public static long used(User u) {
		BigDecimal l = Bean.getOne("tblrepo", "sum(total)", "uid=?",
				new Object[] { u.getId() }, null, 0, null);
		if (l == null) {
			return 0;
		}
		return l.longValue();
	}

	/**
	 * List.
	 * 
	 * @param uid
	 *            the uid
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 * @return the beans
	 */
	public static Beans<Entity> list(int uid, int offset, int limit) {
		return Bean.load("uid=?", new Object[] { uid },
				"order by created desc", offset, limit, Entity.class);
	}

	/**
	 * List.
	 * 
	 * @param tag
	 *            the tag
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 * @return the beans
	 */
	public static Beans<Entity> list(String tag, int offset, int limit) {
		return Bean.load("tag=?", new Object[] { tag },
				"order by created desc", offset, limit, Entity.class);
	}

	/**
	 * Store.
	 * 
	 * @param folder
	 *            the folder
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param tag
	 *            the tag
	 * @param position
	 *            the position
	 * @param total
	 *            the total
	 * @param in
	 *            the in
	 * @param expired
	 *            the expired
	 * @param share
	 *            the share
	 * @param uid
	 *            the uid
	 * @return the long
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static long store(String folder, String id, String name, String tag,
			long position, long total, InputStream in, long expired,
			boolean share, int uid) throws IOException {
		Entity e = new Entity();
		e.folder = folder;
		e.name = name;
		e.id = id;
		e.total = total;
		e.expired = expired;
		e.uid = uid;

		return e.store(tag, position, in, total, name,
				(byte) (share ? 0x01 : 0));
	}

	/**
	 * Gets the id.
	 * 
	 * @param uri
	 *            the uri
	 * @return the id
	 */
	public static String getId(String uri) {
		if (X.isEmpty(uri))
			return null;

		String id = uri;
		int i = id.indexOf("/");
		while (i >= 0) {
			if (i > 0) {
				String s = id.substring(0, i);
				if (s.equals("repo") || s.equals("download")) {
					id = id.substring(i + 1);
					i = id.indexOf("/");
					if (i > 0) {
						id = id.substring(0, i);
					}
				} else {
					id = s;
					break;
				}
			} else {
				id = id.substring(1);
			}

			i = id.indexOf("/");
		}

		log.info("loadbyuri: uri=" + uri + ", id=" + id);
		return id;
	}

	/**
	 * Load by uri.
	 * 
	 * @param uri
	 *            the uri
	 * @return the entity
	 */
	public static Entity loadByUri(String uri) {
		String id = getId(uri);
		if (!X.isEmpty(id)) {
			return load(id);
		}
		return null;
	}

	/**
	 * Load.
	 * 
	 * @param id
	 *            the id
	 * @return the entity
	 */
	public static Entity load(String id) {
		return load(null, id);
	}

	public static void delete(String folder, String id) {
		File f = new File(path(folder, id));

		if (f.exists()) {
			f.delete();
		}
	}

	/**
	 * Load.
	 * 
	 * @param folder
	 *            the folder
	 * @param id
	 *            the id
	 * @return the entity
	 */
	public static Entity load(String folder, String id) {
		File f = new File(path(folder, id));

		if (f.exists()) {
			Entity e = null;
			if (folder != null) {
				e = Bean.load("tblrepo", "folder=? and id=?", new Object[] {
						folder, id }, Entity.class);
			} else {
				e = Bean.load("tblrepo", "id=?", new Object[] { id },
						Entity.class);
			}

			if (e == null) {
				try {
					InputStream in = new FileInputStream(f);

					/**
					 * will not close the inputstream
					 */
					return Entity.create(in);

				} catch (Exception e1) {
					log.error("load: id=" + id, e1);
				}
			}

			return e;
		}
		return null;
	}

	/**
	 * Delete.
	 * 
	 * @param id
	 *            the id
	 * @param uid
	 *            the uid
	 * @return the int
	 */
	public static int delete(String id, int uid) {
		/**
		 * delete the file in the repo
		 */
		Repo.delete(null, id);

		/**
		 * delete the info in table
		 */
		if (uid > 0) {
			Bean.delete("tblrepo", "id=? and uid=?", new Object[] { id, uid },
					null);
		} else {
			Bean.delete("tblrepo", "id=?", new Object[] { id }, null);
		}

		return 1;
	}

	/**
	 * Delete.
	 * 
	 * @param id
	 *            the id
	 * @return the int
	 */
	public static int delete(String id) {
		return delete(id, -1);
	}

	/**
	 * entity of repo
	 * 
	 * @author yjiang
	 * 
	 */
	@DBMapping(table = "tblrepo")
	public static class Entity extends Bean {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private byte version = 1;

		public long pos;
		public int flag;
		public long expired;
		public long total;
		public int uid;
		public String id;
		public String name;
		public long created;
		public String folder;
		String memo;

		private transient InputStream in;
		private transient int headsize;

		public String getMemo() {
			return memo;
		}

		transient User user;

		public String getUrl() {
			return "/repo/" + id + "/" + name;
		}

		public byte getVersion() {
			return version;
		}

		public int getFlag() {
			return flag;
		}

		public long getExpired() {
			return expired;
		}

		public long getTotal() {
			return total;
		}

		public int getUid() {
			return uid;
		}

		public String getId() {
			return id;
		}

		public String getFiletype() {
			if (name != null) {
				int i = name.lastIndexOf(".");
				if (i > 0) {
					return name.substring(i + 1);
				}
			}
			return X.EMPTY;
		}

		public String getName() {
			return name;
		}

		public long getCreated() {
			return created;
		}

		public User getUser() {
			if (user == null) {
				user = User.loadById(uid);
			}
			return user;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return new StringBuilder("Repo.Entity[").append(id)
					.append(", name=").append(name).append(", pos:")
					.append(pos).append(", total:").append(total).append("]")
					.toString();
		}

		/**
		 * Delete.
		 */
		public void delete() {
			Repo.delete(id);
		}

		private long store(String tag, long position, InputStream in,
				long total, String name, int flag) throws IOException {
			File f = new File(path(folder, id));

			if (f.exists()) {
				InputStream tmp = null;
				try {
					tmp = new FileInputStream(f);
					if (load(tmp)
							&& (total != this.total || !name.equals(this.name))) {

						log.error("file: " + f.getCanonicalPath());

						/**
						 * this file is not original file
						 */
						throw new IOException("same filename[" + id + "/"
								+ this.name + "], but different size");
					}
				} finally {
					close();
				}
			} else {
				f.getParentFile().mkdirs();
			}

			if (!f.exists() || total != this.total) {
				/**
				 * initialize the storage, otherwise append
				 */
				OutputStream out = null;
				try {
					out = new FileOutputStream(f);
					pos = in.available();

					Response resp = new Response();
					resp.writeLong(pos);
					resp.writeInt(flag);
					resp.writeLong(expired);
					resp.writeLong(total);
					resp.writeInt(uid);
					resp.writeString(id);
					resp.writeString(name);
					byte[] bb = resp.getBytes();
					resp = new Response();

					resp.writeByte(version);
					resp.writeInt(bb.length);

					resp.writeBytes(bb);
					bb = resp.getBytes();
					out.write(bb);
					pos = 0;
					bb = new byte[4 * 1024];

					int len = in.read(bb);
					while (len > 0) {
						out.write(bb, 0, len);
						pos += len;
						len = in.read(bb);
					}

					long pp = pos;
					if (total > 0) {
						while (pp < total) {
							len = (int) Math.min(total - pp, bb.length);
							out.write(bb, 0, len);
							pp += len;
						}
					}

					if (Bean.exists("id=?", new Object[] { id }, Entity.class)) {
						Bean.update(
								"id=?",
								new Object[] { id },
								V.create("total", pp).set("tag", tag)
										.set("expired", expired), Entity.class);
					} else {
						Bean.insert(
								V.create("id", id)
										.set("uid", uid)
										.set("total", pp)
										.set("tag", tag)
										.set("expired", expired)
										.set("created",
												System.currentTimeMillis())
										.set("flag", flag).set("name", name),
								Entity.class);
					}

					/**
					 * check the free of the user
					 */
					long free = User.checkFree(uid);
					if (free < 0) {
						throw new IOException("repo.no.space");
					}
					return pos;
				} catch (IOException e) {
					Repo.delete(id);

					throw e;
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							log.error(e);
						}
					}

					try {
						in.close();
					} catch (IOException e) {
						log.error(e);
					}
				}

			} else {
				/**
				 * append
				 */
				RandomAccessFile raf = null;
				/**
				 * load head, and skip
				 */
				try {
					raf = new RandomAccessFile(f, "rws");
					byte[] bb = new byte[17]; // version(1) + head.length(4) +
					// pos(8) + flag(4)
					raf.read(bb);
					Request req = new Request(bb, 0);

					version = req.readByte();
					int head = req.readInt();
					pos = req.readLong();

					if (pos >= position) {
						raf.seek(head + 5 + position);

						bb = new byte[4 * 1024];
						int len = in.read(bb);
						while (len > 0) {
							raf.write(bb, 0, len);
							position += len;
							len = in.read(bb);
						}

						if (position > pos) {
							Response resp = new Response();
							resp.writeLong(position);
							raf.seek(5);
							raf.write(resp.getBytes());
							pos = position;
						}
					}

					return pos;
				} finally {
					if (raf != null) {
						try {
							raf.close();
						} catch (IOException e) {
							log.error(e);
						}
					}
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							log.error(e);
						}
					}
				}
			}

		}

		/**
		 * get the inputstream of the repo Entity
		 * 
		 * @return InputStream
		 * @throws IOException
		 */
		public InputStream getInputStream() throws IOException {
			if (in == null) {
				File f = new File(path(folder, id));

				if (f.exists()) {
					try {
						in = new FileInputStream(f);
						load(in);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}

			return in;
		}

		/**
		 * Close.
		 */
		public synchronized void close() {
			if (in != null) {
				try {
					in.close();
					in = null;
				} catch (IOException e) {
					log.error(e);
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#finalize()
		 */
		@Override
		protected void finalize() throws Throwable {
			close();
			super.finalize();
		}

		private boolean load(InputStream in) {
			try {
				byte[] bb = new byte[1];
				in.read(bb);

				version = bb[0];
				bb = new byte[4];
				in.read(bb);
				Request req = new Request(bb, 0);
				headsize = req.readInt();
				bb = new byte[headsize];
				in.read(bb);
				req = new Request(bb, 0);

				pos = req.readLong();
				flag = req.readInt();
				expired = req.readLong();
				total = req.readLong();
				uid = req.readInt();
				id = req.readString();
				name = req.readString();

				this.in = in;

				return true;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			return false;
		}

		public boolean isShared() {
			return (flag & 0x01) != 0;
		}

		private static Entity create(InputStream in) throws IOException {
			Entity e = new Entity();

			e.load(in);
			return e;
		}

		/**
		 * Update.
		 * 
		 * @param v
		 *            the v
		 * @return the int
		 */
		public int update(V v) {
			return Bean.update("id=? and uid=?", new Object[] { id, uid }, v,
					Entity.class);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
		 */
		@Override
		protected void load(ResultSet r) throws SQLException {
			id = r.getString("id");
			uid = r.getInt("uid");
			total = r.getLong("total");
			expired = r.getLong("expired");
			flag = r.getInt("flag");
			created = r.getLong("created");
			name = r.getString("name");
			memo = r.getString("memo");

			// folder = r.getString("folder");
			// pos = r.getLong("pos");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.bean.Bean#toJSON(net.sf.json.JSONObject)
		 */
		@Override
		public boolean toJSON(JSONObject jo) {
			jo.put("id", id);
			jo.put("uid", uid);
			jo.put("total", total);
			jo.put("expired", expired);
			jo.put("flag", flag);
			jo.put("created", created);
			jo.put("name", name);
			jo.put("folder", folder);
			jo.put("pos", pos);

			return true;
		}

		/**
		 * Move to.
		 * 
		 * @param folder
		 *            the folder
		 */
		public void moveTo(String folder) {

			File f1 = new File(path(this.folder, id));
			File f2 = new File(path(folder, id));
			if (f2.exists()) {
				f2.delete();
			} else {
				f2.getParentFile().mkdirs();
			}
			f1.renameTo(f2);

			Bean.update("tblrepo", "id=?", new Object[] { id },
					V.create("folder", folder), null);

		}
	}

	static private String path(String folder, String path) {
		long id = Math.abs(UID.hash(path));
		char p1 = (char) (id % 23 + 'a');
		char p2 = (char) (id % 19 + 'A');
		char p3 = (char) (id % 17 + 'a');
		char p4 = (char) (id % 13 + 'A');

		StringBuilder sb = new StringBuilder(ROOT);

		if (folder != null && "".equals(folder)) {
			sb.append("/").append(folder);
		}

		sb.append("/").append(p1).append("/").append(p2).append("/").append(p3)
				.append("/").append(p4).append("/").append(id);
		return sb.toString();
	}

}
