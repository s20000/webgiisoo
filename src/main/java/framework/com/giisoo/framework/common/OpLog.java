/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.*;
import java.util.List;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.giisoo.core.bean.*;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.framework.utils.Shell;
import com.giisoo.framework.web.Language;

/**
 * Operation Log
 * 
 * @author yjiang
 * 
 */
@DBMapping(table = "tbloplog")
public class OpLog extends Bean implements Exportable {

	private static final long serialVersionUID = 1L;

	public static final int TYPE_INFO = 0;
	public static final int TYPE_WARN = 1;
	public static final int TYPE_ERROR = 2;

	String id;
	long created;
	String system;
	String module;
	int uid;
	String ip;
	int type;
	String op;
	String brief;
	String message;

	/**
	 * Removes the.
	 * 
	 * @return the int
	 */
	public static int remove() {
		return Bean.delete(null, null, OpLog.class);
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
		Beans<OpLog> bs = load(null, 0, 1);
		int total = bs.getTotal();
		if (total >= max) {
			long created = Bean.getOne("created", null, null,
					"order by created desc", min, OpLog.class);
			if (created > 0) {
				int i = Bean.delete("created <?", new Object[] { created },
						OpLog.class);

				if (i > 0) {
					OpLog.log("cleanup", "cleanup log: " + i, null);
				}
				return i;
			}
		}

		return 0;

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
	public static Beans<OpLog> load(W w, int offset, int limit) {
		return Bean.load(w == null ? null : w.where(),
				w == null ? null : w.args(),
				w == null || X.isEmpty(w.orderby()) ? "order by created desc"
						: w.orderby(), offset, limit, OpLog.class);
	}

	/**
	 * Log.
	 * 
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @return the int
	 */
	public static int log(String op, String brief, String message) {
		return log(op, brief, message, -1, null);
	}

	/**
	 * Log.
	 * 
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int log(String op, String brief, String message, int uid,
			String ip) {
		return log(X.EMPTY, op, brief, message, uid, ip);
	}

	/**
	 * Log.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @return the int
	 */
	public static int log(String module, String op, String brief, String message) {
		return log(module, op, brief, message, -1, null);
	}

	/**
	 * @deprecated
	 * @param op
	 * @param message
	 * @return
	 */
	public static int log(String op, String message) {
		return info("default", op, message, -1, null);
	}

	/**
	 * Log.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param message
	 *            the message
	 * @return the int
	 */
	public static int log(Class<?> module, String op, String message) {
		return info(module.getName(), op, message, -1, null);
	}

	/**
	 * Log.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int log(String module, String op, String brief,
			String message, int uid, String ip) {
		return info(SystemConfig.s("node", X.EMPTY), module, op, brief,
				message, uid, ip);
	}

	/**
	 * Log.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int log(Class<?> module, String op, String brief,
			String message, int uid, String ip) {
		return info(SystemConfig.s("node", X.EMPTY), module.getName(), op,
				brief, message, uid, ip);
	}

	/**
	 * Log.
	 * 
	 * @param system
	 *            the system
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int log(String system, Class<?> module, String op,
			String brief, String message, int uid, String ip) {
		return info(system, module.getName(), op, brief, message, uid, ip);
	}

	/**
	 * Log.
	 * 
	 * @param system
	 *            the system
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int log(String system, String module, String op,
			String brief, String message, int uid, String ip) {
		return info(system, module, op, brief, message, uid, ip);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		id = r.getString("id");
		created = r.getLong("created");
		system = r.getString("system");
		module = r.getString("module");
		uid = r.getInt("uid");
		type = r.getInt("type");
		op = r.getString("op");
		message = r.getString("message");
		ip = r.getString("ip");
		brief = r.getString("brief");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Bean#toJSON(net.sf.json.JSONObject)
	 */
	@Override
	public boolean toJSON(JSONObject jo) {
		jo.put("id", id);
		jo.put("created", created);
		jo.put("system", system);
		jo.put("module", module);
		jo.put("uid", uid);
		jo.put("type", type);
		jo.put("op", op);
		jo.put("message", message);
		jo.put("ip", ip);
		jo.put("brief", brief);

		return true;
	}

	public String getIp() {
		return ip;
	}

	public long getCreated() {
		return created;
	}

	public String getSystem() {
		return system;
	}

	public String getModule() {
		return module;
	}

	public int getUid() {
		return uid;
	}

	public int getType() {
		return type;
	}

	public String getOp() {
		return op;
	}

	public String getMessage() {
		return message;
	}

	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Exportable#output(java.lang.String,
	 * java.lang.Object[], java.util.zip.ZipOutputStream)
	 */
	public JSONObject output(String where, Object[] args, ZipOutputStream out) {
		int s = 0;
		Beans<OpLog> bs = Bean.load(where, args, null, s, 10, OpLog.class);

		JSONObject jo = new JSONObject();
		JSONArray arr = new JSONArray();
		int count = 0;

		while (bs != null && bs.getList() != null && bs.getList().size() > 0) {
			for (OpLog d : bs.getList()) {
				JSONObject j = new JSONObject();
				d.toJSON(j);

				j.convertStringtoBase64();

				arr.add(j);

				count++;
			}
			s += bs.getList().size();
			bs = Bean.load(where, args, null, s, 10, OpLog.class);

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
			JSONObject jo = list.getJSONObject(i);
			jo.convertBase64toString();

			if (jo.has("id")) {
				String id = jo.getString("id");
				long created = jo.getLong("created");
				count += Bean.insertOrUpdate(
						"tbloplog",
						"id=? and created=?",
						new Object[] { id, created },
						V.create()
								.copy(jo, "id", "system", "module", "op", "ip",
										"brief", "message")
								.copyInt(jo, "type", "uid")
								.copyLong(jo, "created"), null);
			}
		}
		return count;
	}

	public String getBrief() {
		return brief;
	}

	transient User user;

	public User getUser() {
		if (user == null && uid >= 0) {
			user = User.loadById(uid);
		}
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Exportable#load(java.lang.String,
	 * java.lang.Object[], int, int)
	 */
	public Beans<OpLog> load(String where, Object[] args, int s, int n) {
		return Bean.load(where, args, "order by created", s, n, OpLog.class);
	}

	public String getExportableId() {
		return id;
	}

	public String getExportableName() {
		return message;
	}

	public long getExportableUpdated() {
		return created;
	}

	/**
	 * Load category.
	 * 
	 * @param type
	 *            the type
	 * @param cate
	 *            the cate
	 * @return the list
	 */
	public static List<String> loadCategory(String type, String cate) {
		return Category.load(type, cate);
	}

	@DBMapping(table = "tbloplog_cate")
	public static class Category extends Bean {

		String type;
		String cate;
		String name;

		long updated;

		public String getCate() {
			return cate;
		}

		/**
		 * Load.
		 * 
		 * @param type
		 *            the type
		 * @param cate
		 *            the cate
		 * @return the list
		 */
		public static List<String> load(String type, String cate) {
			W w = W.create();
			if (!X.isEmpty(type)) {
				w.and("type", type);
			}
			if (!X.isEmpty(cate)) {
				w.and("cate", cate);
			}
			w.and("updated", System.currentTimeMillis() - X.AYEAR, W.OP_GT_EQ);
			return Bean.getList("distinct name", w.where(), w.args(),
					"order by name", 0, 1000, Category.class);
		}

		public String getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		public long getUpdated() {
			return updated;
		}

		/**
		 * Update.
		 * 
		 * @param system
		 *            the system
		 * @param module
		 *            the module
		 * @param op
		 *            the op
		 */
		public static void update(String system, String module, String op) {
			/**
			 * record system
			 */
			Long update = Bean.getOne("tbloplog_cate", "updated",
					"type=? and cate=? and name=?", new Object[] { "system",
							X.EMPTY, system }, null, 0, null);
			if (update == null) {
				// insert
				Bean.insert(
						V.create("type", "system").set("cate", X.EMPTY)
								.set("name", system)
								.set("updated", System.currentTimeMillis()),
						Category.class);
			} else if (System.currentTimeMillis() - update > X.AWEEK) {
				// update
				Bean.update("type=? and cate=? and name=?", new Object[] {
						"system", X.EMPTY, system },
						V.create("updated", System.currentTimeMillis()),
						Category.class);
			}

			/**
			 * record module
			 */
			update = Bean.getOne("tbloplog_cate", "updated",
					"type=? and cate=? and name=?", new Object[] { "module",
							system, module }, null, 0, null);
			if (update == null) {
				// insert
				Bean.insert(
						V.create("type", "module").set("cate", system)
								.set("name", module)
								.set("updated", System.currentTimeMillis()),
						Category.class);
			} else if (System.currentTimeMillis() - update > X.AWEEK) {
				// update
				Bean.update("type=? and cate=? and name=?", new Object[] {
						"module", system, module },
						V.create("updated", System.currentTimeMillis()),
						Category.class);
			}

			/**
			 * record op
			 */
			update = Bean.getOne("tbloplog_cate", "updated",
					"type=? and cate=? and name=?", new Object[] { "op",
							module, op }, null, 0, null);
			if (update == null) {
				// insert
				Bean.insert(
						V.create("type", "op").set("cate", module)
								.set("name", op)
								.set("updated", System.currentTimeMillis()),
						Category.class);
			} else if (System.currentTimeMillis() - update > X.AWEEK) {
				// update
				Bean.update("type=? and cate=? and name=?", new Object[] {
						"op", module, op },
						V.create("updated", System.currentTimeMillis()),
						Category.class);
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
		 */
		@Override
		protected void load(ResultSet r) throws SQLException {
			type = r.getString("type");
			cate = r.getString("cate");
			name = r.getString("name");
		}
	}

	public boolean isExportable() {
		return true;
	}

	/**
	 * Info.
	 * 
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @return the int
	 */
	public static int info(String op, String brief, String message) {
		return info(op, brief, message, -1, null);
	}

	/**
	 * Info.
	 * 
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int info(String op, String brief, String message, int uid,
			String ip) {
		return info(X.EMPTY, op, brief, message, uid, ip);
	}

	/**
	 * Info.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @return the int
	 */
	public static int info(String module, String op, String brief,
			String message) {
		return info(module, op, brief, message, -1, null);
	}

	/**
	 * Info.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @return the int
	 */
	public static int info(Class<?> module, String op, String brief,
			String message) {
		return info(module.getName(), op, brief, message, -1, null);
	}

	/**
	 * Info.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int info(String module, String op, String brief,
			String message, int uid, String ip) {
		return info(SystemConfig.s("node", X.EMPTY), module, op, brief,
				message, uid, ip);
	}

	/**
	 * Info.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int info(Class<?> module, String op, String brief,
			String message, int uid, String ip) {
		return info(SystemConfig.s("node", X.EMPTY), module.getName(), op,
				brief, message, uid, ip);
	}

	/**
	 * Info.
	 * 
	 * @param system
	 *            the system
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int info(String system, Class<?> module, String op,
			String brief, String message, int uid, String ip) {
		return info(system, module.getName(), op, brief, message, uid, ip);
	}

	/**
	 * Info.
	 * 
	 * @param system
	 *            the system
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int info(String system, String module, String op,
			String brief, String message, int uid, String ip) {
		long t = System.currentTimeMillis();
		String id = UID.id(t, op, message);
		int i = Bean.insert(
				V.create("id", id).set("created", t).set("system", system)
						.set("module", module).set("op", op)
						.set("brief", brief).set("message", message)
						.set("uid", uid).set("ip", ip)
						.set("type", OpLog.TYPE_INFO), OpLog.class);

		if (i > 0) {
			Category.update(system, module, op);

			/**
			 * 记录系统日志
			 */
			if (SystemConfig.i("logger.rsyslog", 0) == 1) {
				Language lang = Language.getLanguage();
				// 192.168.1.1#系统名称#2014-10-31#ERROR#日志消息#程序名称
				Shell.log(ip, Shell.Logger.info,
						lang.get("log.module_" + module),
						lang.get("log.opt_" + op) + "//" + brief + ", uid="
								+ uid);
			}

			onChanged("tbloplog", IData.OP_CREATE, "created=? and id=?",
					new Object[] { t, id });
		}

		return i;
	}

	/**
	 * Warn.
	 * 
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @return the int
	 */
	/**
	 * 
	 * @param op
	 * @param message
	 */
	public static int warn(String op, String brief, String message) {
		return warn(op, brief, message, -1, null);
	}

	/**
	 * Warn.
	 * 
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int warn(String op, String brief, String message, int uid,
			String ip) {
		return warn(X.EMPTY, op, brief, message, uid, ip);
	}

	/**
	 * Warn.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @return the int
	 */
	public static int warn(String module, String op, String brief,
			String message) {
		return warn(module, op, brief, message, -1, null);
	}

	/**
	 * Warn.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @return the int
	 */
	public static int warn(Class<?> module, String op, String brief,
			String message) {
		return warn(module.getName(), op, brief, message, -1, null);
	}

	/**
	 * Warn.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int warn(String module, String op, String brief,
			String message, int uid, String ip) {
		return warn(SystemConfig.s("node", X.EMPTY), module, op, brief,
				message, uid, ip);
	}

	/**
	 * Warn.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int warn(Class<?> module, String op, String brief,
			String message, int uid, String ip) {
		return warn(SystemConfig.s("node", X.EMPTY), module.getName(), op,
				brief, message, uid, ip);
	}

	/**
	 * Warn.
	 * 
	 * @param system
	 *            the system
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int warn(String system, Class<?> module, String op,
			String brief, String message, int uid, String ip) {
		return warn(system, module.getName(), op, brief, message, uid, ip);
	}

	/**
	 * Warn.
	 * 
	 * @param system
	 *            the system
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int warn(String system, String module, String op,
			String brief, String message, int uid, String ip) {
		long t = System.currentTimeMillis();
		String id = UID.id(t, op, message);
		int i = Bean.insert(
				V.create("id", id).set("created", t).set("system", system)
						.set("module", module).set("op", op)
						.set("brief", brief).set("message", message)
						.set("uid", uid).set("ip", ip)
						.set("type", OpLog.TYPE_WARN), OpLog.class);

		if (i > 0) {
			Category.update(system, module, op);

			/**
			 * 记录系统日志
			 */
			if (SystemConfig.i("logger.rsyslog", 0) == 1) {
				Language lang = Language.getLanguage();

				// 192.168.1.1#系统名称#2014-10-31#ERROR#日志消息#程序名称
				Shell.log(ip, Shell.Logger.warn,
						lang.get("log.module_" + module),
						lang.get("log.opt_" + op) + "//" + brief + ", uid="
								+ uid);

			}

			onChanged("tbloplog", IData.OP_CREATE, "created=? and id=?",
					new Object[] { t, id });
		}

		return i;
	}

	/**
	 * Error.
	 * 
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @return the int
	 */
	/**
	 * 
	 * @param op
	 * @param message
	 */
	public static int error(String op, String brief, String message) {
		return error(op, brief, message, -1, null);
	}

	/**
	 * Error.
	 * 
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int error(String op, String brief, String message, int uid,
			String ip) {
		return error(X.EMPTY, op, brief, message, uid, ip);
	}

	/**
	 * Error.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @return the int
	 */
	public static int error(String module, String op, String brief,
			String message) {
		return error(module, op, brief, message, -1, null);
	}

	/**
	 * Error.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @return the int
	 */
	public static int error(Class<?> module, String op, String brief,
			String message) {
		return error(module.getName(), op, brief, message, -1, null);
	}

	/**
	 * Error.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int error(String module, String op, String brief,
			String message, int uid, String ip) {
		return error(SystemConfig.s("node", X.EMPTY), module, op, brief,
				message, uid, ip);
	}

	/**
	 * Error.
	 * 
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int error(Class<?> module, String op, String brief,
			String message, int uid, String ip) {
		return error(SystemConfig.s("node", X.EMPTY), module.getName(), op,
				brief, message, uid, ip);
	}

	/**
	 * Error.
	 * 
	 * @param system
	 *            the system
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int error(String system, Class<?> module, String op,
			String brief, String message, int uid, String ip) {
		return error(system, module.getName(), op, brief, message, uid, ip);
	}

	/**
	 * Error.
	 * 
	 * @param system
	 *            the system
	 * @param module
	 *            the module
	 * @param op
	 *            the op
	 * @param brief
	 *            the brief
	 * @param message
	 *            the message
	 * @param uid
	 *            the uid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public static int error(String system, String module, String op,
			String brief, String message, int uid, String ip) {
		long t = System.currentTimeMillis();
		String id = UID.id(t, op, message);
		int i = Bean.insert(
				V.create("id", id).set("created", t).set("system", system)
						.set("module", module).set("op", op)
						.set("brief", brief).set("message", message)
						.set("uid", uid).set("ip", ip)
						.set("type", OpLog.TYPE_ERROR), OpLog.class);

		if (i > 0) {
			Category.update(system, module, op);

			/**
			 * 记录系统日志
			 */
			if (SystemConfig.i("logger.rsyslog", 0) == 1) {
				Language lang = Language.getLanguage();

				// 192.168.1.1#系统名称#2014-10-31#ERROR#日志消息#程序名称
				Shell.log(ip, Shell.Logger.error,
						lang.get("log.module_" + module),
						lang.get("log.opt_" + op) + "//" + brief + ", uid="
								+ uid);
			}

			onChanged("tbloplog", IData.OP_CREATE, "created=? and id=?",
					new Object[] { t, id });
		}

		return i;
	}

}
