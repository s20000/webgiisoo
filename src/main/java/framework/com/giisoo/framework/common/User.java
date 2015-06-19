/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.*;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.giisoo.core.bean.*;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.framework.common.Company.Department;
import com.giisoo.framework.web.Module;

/**
 * User
 * 
 * @author yjiang
 * 
 */
@DBMapping(table = "tbluser")
public class User extends Bean implements Exportable {

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new user.
	 */
	public User() {

	}

	public Company getCompany_obj() {
		if (!this.containsKey("company_obj")) {
			Company c = Company.load(this.getInt("company"));
			this.set("company_obj", c);
		}
		return (Company) this.get("company_obj");
	}

	public Department getDepartment_obj() {
		if (!this.containsKey("department_obj")) {
			Department c = Department.load(this.getLong("department"));
			this.set("department_obj", c);
		}
		return (Department) this.get("department_obj");
	}

	/**
	 * Checks if is role.
	 * 
	 * @param r
	 *            the r
	 * @return true, if is role
	 */
	public boolean isRole(Role r) {
		getRoles();

		return roles.contains(r.id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new StringBuilder("User:[").append(getId()).append(",")
				.append(get("name")).append("]").toString();
	}

	private static int nextId() {
		int id = Bean.toInt(Module.home.get("user_prefix"))
				+ (int) UID.next("user.id");
		while (Bean.exists("id=?", new Object[] { id }, User.class)) {
			id = Bean.toInt(Module.home.get("user_prefix"))
					+ (int) UID.next("user.id");
		}

		return id;
	}

	/**
	 * Creates the.
	 * 
	 * @param v
	 *            the v
	 * @return the int
	 */
	public static int create(V v) {

		for (int i = 0; i < v.size(); i++) {
			if ("password".equals(v.name(i))) {
				String password = (String) v.value(i);
				v.set(i, encrypt(password));
			}
		}

		int id = nextId();

		if (Bean.insert(
				v.set("id", id).set("created", System.currentTimeMillis())
						.set("updated", System.currentTimeMillis()), User.class) > 0) {
			Bean.onChanged("tbluser", IData.OP_CREATE, "id=?",
					new Object[] { id });

			return id;
		}

		return -1;
	}

	/**
	 * Creates the.
	 * 
	 * @param name
	 *            the name
	 * @param password
	 *            the password
	 * @param jo
	 *            the jo
	 * @return the int
	 * @throws giException
	 *             the gi exception
	 */
	public static int create(String name, String password, JSONObject jo)
			throws giException {

		String allow = conf.getString("user.name", "^[a-zA-Z0-9]{4,16}$");

		if (X.isEmpty(name) || !name.matches(allow)) {
			/**
			 * the format of name is not correct
			 */
			throw new giException(-2,
					"the name format is not correct, or password is none");
		}

		if (Bean.exists("name=? and locked=0 and remote=0",
				new String[] { name }, User.class)) {
			/**
			 * exists, create failded
			 */
			throw new giException(-1, "the name exists");
		}

		int id = nextId();

		password = encrypt(password);
		V v = V.create("id", id).set("name", name)
				.set("password", password, true)
				.set("created", System.currentTimeMillis())
				.set("updated", System.currentTimeMillis());
		v.copy(jo, "company", "title", "department", "address", "email",
				"nickname", "description", "special", "certid").copyLong(jo,
				"total");

		if (Bean.insert(v, User.class) > 0) {
			Bean.onChanged("tbluser", IData.OP_CREATE, "id=?",
					new Object[] { id });

			return id;
		}

		throw new giException(-1, "unknown error");
	}

	/**
	 * Load.
	 * 
	 * @param name
	 *            the name
	 * @param password
	 *            the password
	 * @return the user
	 */
	public static User load(String name, String password) {

		password = encrypt(password);

		log.debug("name=" + name + ", passwd=" + password);
		// System.out.println("name=" + name + ", passwd=" + password);

		return Bean.load("tbluser",
				"name=? and password=? and deleted=0 and remote=0",
				new String[] { name, password }, User.class);

	}

	public boolean isDeleted() {
		return getInt("deleted") == 1;
	}

	public int getId() {
		return this.getInt("id");
	}

	/**
	 * Load.
	 * 
	 * @param name
	 *            the name
	 * @return the user
	 */
	public static User load(String name) {

		List<User> list = Bean.load("tbluser", null, "name=?",
				new String[] { name }, User.class);

		if (list != null && list.size() > 0) {
			for (User u : list) {

				/**
				 * if the user has been locked, then not allow to login
				 */
				if (u.isLocked() || u.isDeleted())
					continue;

				/**
				 * id == 0: admin; across.login ==0, not allow across login
				 */
				if (SystemConfig.i("across.login", 0) == 0 && u.getId() > 0) {
					int prefix = Bean.toInt(Module.home.get("user_prefix"));
					if ((u.getId() & prefix) != prefix) {
						/**
						 * the prefix is different, do not allow to login
						 */
						continue;
					}
				}

				return u;
			}
		}

		return null;
	}

	/**
	 * Load by id.
	 * 
	 * @param id
	 *            the id
	 * @return the user
	 */
	public static User loadById(int id) {
		return Bean.load("tbluser", "id=?", new Object[] { id }, User.class);
	}

	/**
	 * Load by access.
	 * 
	 * @param access
	 *            the access
	 * @return the list
	 */
	public static List<User> loadByAccess(String access) {
		return Bean
				.load("tbluser",
						null,
						"id in (select uid from tbluserrole where rid in (select rid from tblroleaccess where name=?)) and deleted=0 and locked=0",
						new Object[] { access }, User.class);
	}

	public static List<User> loadByAccess(String access, W w) {
		if (w == null) {
			w = W.create();
		}
		w.set("id in (select uid from tbluserrole where rid in (select rid from tblroleaccess where name=?)) and deleted=0 and locked=0",
				access);
		return Bean.load("tbluser", null, w.where(), w.args(), User.class);
	}

	/**
	 * Validate.
	 * 
	 * @param password
	 *            the password
	 * @return true, if successful
	 */
	public boolean validate(String password) {

		/**
		 * if the user has been locked, then not allow to login
		 */
		if (this.isLocked())
			return false;

		/**
		 * id == 0: admin; across.login ==0, not allow across login
		 */
		if (SystemConfig.i("across.login", 0) == 0 && getId() > 0) {
			int prefix = Bean.toInt(Module.home.get("user_prefix"));
			if ((getId() & prefix) != prefix) {
				/**
				 * the prefix is different, do not allow to login
				 */
				return false;
			}
		}

		password = encrypt(password);
		return get("password") != null && get("password").equals(password);
	}

	/**
	 * whether the user has been locked
	 * 
	 * @return boolean
	 */
	public boolean isLocked() {
		return getInt("locked") > 0;
	}

	/**
	 * Checks for access.
	 * 
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public boolean hasAccess(String... name) {
		if (role == null) {
			getRoles();
			role = new Roles(roles);
		}

		return role.hasAccess(name);
	}

	transient Roles role = null;

	public Roles getRole() {
		if (role == null) {
			getRoles();
			role = new Roles(roles);
		}
		return role;
	}

	transient List<Integer> roles = null;

	public List<Integer> getRoles() {
		if (roles == null) {
			roles = Bean.loadList("tbluserrole", "rid", "uid=?",
					new Integer[] { getId() }, Integer.class, null);

			if (roles == null) {
				roles = new ArrayList<Integer>();
			}
		}

		return roles;
	}

	/**
	 * set a role to a user with role id
	 * 
	 * @param rid
	 */
	public void setRole(int rid) {
		getRoles();

		if (!roles.contains(rid)) {
			// add
			Bean.insert("tbluserrole",
					V.create("uid", getId()).set("rid", rid), null);
			roles.add(rid);

			role = null;

			Bean.update("id=?", new Object[] { getId() },
					V.create("updated", System.currentTimeMillis()), User.class);
		}
	}

	/**
	 * Removes the role.
	 * 
	 * @param rid
	 *            the rid
	 */
	public void removeRole(String rid) {
		getRoles();

		if (roles.contains(rid)) {
			// remove it
			Bean.delete("tbluserrole", "uid=? and rid=?", new Object[] {
					getId(), rid }, null);
			roles.remove(rid);

			Bean.update("id=?", new Object[] { getId() },
					V.create("updated", System.currentTimeMillis()), User.class);
		}
	}

	/**
	 * Removes the all roles.
	 */
	public void removeAllRoles() {
		getRoles();
		roles.clear();
		Bean.delete("tbluserrole", "uid=?", new Object[] { getId() }, null);

		Bean.update("id=?", new Object[] { getId() },
				V.create("updated", System.currentTimeMillis()), User.class);
	}

	public void setSid(String sid) {
		set("sid", sid);

		Bean.update(
				"id=?",
				new Object[] { getId() },
				V.create("sid", sid).set("updated", System.currentTimeMillis()),
				User.class);
	}

	public void setIp(String ip) {
		set("ip", ip);

		Bean.update("id=?", new Object[] { getId() },
				V.create("ip", ip).set("updated", System.currentTimeMillis()),
				User.class);
	}

	private static String encrypt(String passwd) {
		if (X.isEmpty(passwd)) {
			return X.EMPTY;
		}
		return UID.id(passwd);
	}

	/**
	 * Load by id.
	 * 
	 * @param certid
	 *            the certid
	 * @return the user
	 */
	public static User loadById(String certid) {
		return Bean.load("certid=?", new Object[] { certid }, User.class);
	}

	/**
	 * Load by refer.
	 * 
	 * @param refer
	 *            the refer
	 * @return true, if successful
	 */
	public boolean loadByRefer(JSONObject refer) {
		return Bean.load("tbluser", "id=?", new Object[] { getId() }, this);
	}

	/**
	 * To refer.
	 * 
	 * @param refer
	 *            the refer
	 * @return true, if successful
	 */
	public boolean toRefer(JSONObject refer) {
		if (getId() > 0) {
			refer.put("id", getId());

			return true;
		}
		return false;
	}

	/**
	 * Load.
	 * 
	 * @param w
	 *            the w
	 * @param rank
	 *            the rank
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 * @return the beans
	 */
	public static Beans<User> load(W w, int rank, int offset, int limit) {
		if (w == null) {
			w = W.create();
		}

		if (rank >= 0) {
			w.and("id", 0, W.OP_GT).and("rank", rank);
			return Bean
					.load(w.where(),
							w.args(),
							w == null || X.isEmpty(w.orderby()) ? "order by created desc"
									: w.orderby(), offset, limit, User.class);
		} else {
			w.and("id", 0, W.OP_GT);
			return Bean.load(w.where(), w.args(),
					X.isEmpty(w.orderby()) ? "order by name" : w.orderby(),
					offset, limit, User.class);
		}
	}

	/**
	 * Exists.
	 * 
	 * @param w
	 *            the w
	 * @return true, if successful
	 */
	public static boolean exists(W w) {
		return Bean.exists(w.where(), w.args(), User.class);
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
	public static Beans<User> load(W w, int offset, int limit) {
		if (w == null) {
			w = W.create();
		}

		w.and("id", 0, W.OP_GT);

		return Bean.load(w.where(), w.args(),
				X.isEmpty(w.orderby()) ? "order by name" : w.orderby(), offset,
				limit, User.class);
	}

	/**
	 * Update.
	 * 
	 * @param v
	 *            the v
	 * @return the int
	 */
	public int update(V v) {
		int len = v.size();
		for (int i = 0; i < len; i++) {
			String name = v.name(i);
			if ("password".equals(name)) {
				String passwd = (String) v.value(i);
				if (!"".equals(passwd)) {
					passwd = encrypt(passwd);
					v.set("password", passwd, true);
				} else {
					v.remove(i);
				}
				break;
			}
		}
		return Bean.update("id=?", new Object[] { getId() },
				v.set("updated", System.currentTimeMillis()), User.class);
	}

	public void setRoles(String[] roles) {
		/**
		 * remove all
		 */
		if (roles != null) {
			Bean.delete("tbluserrole", "uid=?", new Object[] { getId() }, null);

			for (String r : roles) {
				int rid = Bean.toInt(r, -1);
				if (rid >= 0) {
					Bean.insert("tbluserrole",
							V.create("uid", getId()).set("rid", rid), null);
				}
			}

			Bean.update("id=?", new Object[] { getId() },
					V.create("updated", System.currentTimeMillis()), User.class);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Bean#toJSON(net.sf.json.JSONObject)
	 */
	@Override
	public boolean toJSON(JSONObject jo) {

		List<Integer> list = this.getRoles();
		JSONArray arr = new JSONArray();
		for (Integer i : list) {
			Role r = Role.loadById(i);
			if (r != null) {
				JSONObject j = new JSONObject();
				r.toJSON(j);
				arr.add(j);
			}
		}
		jo.put("roles", arr);

		super.toJSON(jo);

		return true;
	}

	/**
	 * Check free.
	 * 
	 * @param uid
	 *            the uid
	 * @return the long
	 */
	public static long checkFree(int uid) {

		Connection c = null;
		PreparedStatement stat = null;
		ResultSet r = null;
		try {
			c = Bean.getConnection();
			stat = c.prepareStatement("select total from tbluser where id=?");
			stat.setInt(1, uid);
			r = stat.executeQuery();
			long total = -1;
			if (r.next()) {
				total = r.getLong("total");
			}
			if (total < 0)
				return 0;
			r.close();
			stat.close();

			stat = c.prepareStatement("select sum(total) total from tblrepo where uid=?");
			stat.setInt(1, uid);
			r = stat.executeQuery();
			if (r.next()) {
				long t = r.getLong("total");
				return total - t;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			Bean.close(r, stat, c);
		}

		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Exportable#output(java.lang.String,
	 * java.lang.Object[], java.util.zip.ZipOutputStream)
	 */
	public JSONObject output(String where, Object[] args, ZipOutputStream out) {
		int s = 0;
		// Beans<User> bs = Bean.load(where, args, null, s, 10, User.class);
		Beans<User> bs = Bean.load((String) null, (Object[]) null, null, s, 10,
				User.class);

		JSONObject jo = new JSONObject();
		JSONArray arr = new JSONArray();
		int count = 0;

		while (bs != null && bs.getList() != null && bs.getList().size() > 0) {
			for (User d : bs.getList()) {
				/**
				 * avoid sync admin user
				 */
				if (d.getId() > 0) {
					JSONObject j = new JSONObject();
					d.toJSON(j);

					// log.debug(j);

					j.convertStringtoBase64();
					arr.add(j);

					count++;
					// } else {
					// log.debug("id=" + d.id);
				}
			}
			s += bs.getList().size();
			bs = Bean.load((String) null, (Object[]) null, null, s, 10,
					User.class);

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

			// log.debug(jo);

			int id = jo.getInt("id");
			if (id > 0) {
				count += Bean.insertOrUpdate(
						"tbluser",
						"id=?",
						new Object[] { id },
						V.create()
								.copy(jo, "name", "nickname", "email",
										"created", "address", "company",
										"title", "photo", "description",
										"special", "ip", "workspace", "spi",
										"certid", "lastfailip")
								.copyInt(jo, "rank", "locked", "id",
										"failtimes", "deleted")
								.copyLong(jo, "total", "free", "lastfailtime",
										"lastlogintime", "lockexpired",
										"updated").set("remote", 1), null);
			}

			if (jo.has("roles")) {
				/**
				 * the the role of the user
				 */
				@SuppressWarnings("unchecked")
				List<JSONObject> roles = jo.getJSONArray("roles");
				if (roles != null) {
					for (JSONObject j : roles) {
						int rid = j.getInt("id");
						Bean.insertOrUpdate("tbluserrole", "uid=? and rid=?",
								new Object[] { id, rid }, V.create("uid", id)
										.set("rid", rid), null);
					}
				}
			}
		}
		return count;
	}

	/**
	 * Failed.
	 * 
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public int failed(String ip) {
		set("failtimes", getInt("failtimes") + 1);

		return Bean
				.update("id=?",
						new Object[] { getId() },
						V.create("lastfailtime", System.currentTimeMillis())
								.set("lastfailip", ip)
								.set("failtimes", getInt("failtimes"))
								.set("updated", System.currentTimeMillis()),
						User.class);
	}

	public int failed(String ip, String sid, String useragent) {
		set("failtimes", getInt("failtimes") + 1);

		return Lock.locked(getId(), sid, ip, useragent);
	}

	/**
	 * Logout.
	 * 
	 * @return the int
	 */
	public int logout() {
		return Bean.update(
				"id=?",
				new Object[] { getId() },
				V.create("sid", X.EMPTY).set("updated",
						System.currentTimeMillis()), User.class);
	}

	/**
	 * Logined.
	 * 
	 * @param sid
	 *            the sid
	 * @param ip
	 *            the ip
	 * @return the int
	 */
	public int logined(String sid, String ip) {

		// update
		set("logintimes", getInt("logintimes") + 1);

		Lock.removed(getId(), sid);

		/**
		 * cleanup the old sid for the old logined user
		 */
		Bean.update("sid=?", new Object[] { sid }, V.create("sid", X.EMPTY),
				User.class);

		return Bean
				.update("id=?",
						new Object[] { getId() },
						V.create("lastlogintime", System.currentTimeMillis())
								.set("logintimes", getInt("logintimes"))
								.set("ip", ip).set("failtimes", 0)
								.set("locked", 0).set("lockexpired", 0)
								.set("sid", sid)
								.set("updated", System.currentTimeMillis()),
						User.class);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Exportable#load(java.lang.String,
	 * java.lang.Object[], int, int)
	 */
	@SuppressWarnings("unchecked")
	public Beans<User> load(String where, Object[] args, int s, int n) {
		return Bean.load(null, null, "order by id", s, n, User.class);
	}

	public String getExportableId() {
		return Integer.toString(getId());
	}

	public String getExportableName() {
		return get("nickname") + "(" + get("name") + ")";
	}

	public long getExportableUpdated() {
		return getLong("updated");
	}

	public boolean isExportable() {
		return getId() > 0;
	}

	@DBMapping(table = "tbluserlock")
	public static class Lock extends Bean {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public static int locked(int uid, String sid, String host,
				String useragent) {
			return Bean.insert(
					V.create("uid", uid).set("sid", sid).set("host", host)
							.set("useragent", useragent)
							.set("created", System.currentTimeMillis()),
					Lock.class);
		}

		public static int removed(int uid) {
			return Bean.delete("uid=?", new Object[] { uid }, Lock.class);
		}

		public static int removed(int uid, String sid) {
			return Bean.delete("uid=? and sid=?", new Object[] { uid, sid },
					Lock.class);
		}

		public static List<Lock> load(int uid, long time) {
			return Bean.load(null, "uid=? and created>?", new Object[] { uid,
					time }, null, 0, -1, Lock.class);
		}

		public static List<Lock> loadBySid(int uid, long time, String sid) {
			return Bean.load(null, "uid=? and created>? and sid=?",
					new Object[] { uid, time, sid }, null, 0, -1, Lock.class);
		}

		public static List<Lock> loadByHost(int uid, long time, String host) {
			return Bean.load(null, "uid=? and created>? and host=?",
					new Object[] { uid, time, host }, null, 0, -1, Lock.class);
		}

		@Override
		protected void load(ResultSet r) throws SQLException {
			uid = r.getInt("uid");
			created = r.getLong("created");
			sid = r.getString("sid");
			host = r.getString("host");
			useragent = r.getString("useragent");
		}

		public int getUid() {
			return uid;
		}

		public long getCreated() {
			return created;
		}

		public String getSid() {
			return sid;
		}

		public String getHost() {
			return host;
		}

		public String getUseragent() {
			return useragent;
		}

		int uid;
		long created;
		String sid;
		String host;
		String useragent;

	}

	/**
	 * List company.
	 * 
	 * @return the list
	 */
	public static List<String> listCompany() {
		return Bean.loadList("tbluser", "distinct company", "id>0", null,
				String.class, null);
	}

	public static class DummyUser extends User {
		Set<String> access = new HashSet<String>();

		/**
		 * Instantiates a new dummy user.
		 * 
		 * @param access
		 *            the access
		 */
		public DummyUser(String... access) {
			set("id", Integer.MIN_VALUE);
			for (String s : access) {
				this.access.add(s);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.framework.common.User#hasAccess(java.lang.String[])
		 */
		@Override
		public boolean hasAccess(String... name) {
			for (String s : name) {
				if (access.contains(s)) {
					return true;
				}
			}

			return false;
		}

	}

	/**
	 * Updated.
	 * 
	 * @return the long
	 */
	public static long updated() {
		return Bean.getOne("max(updated)", null, null, null, 0, User.class);
	}

	/**
	 * Delete.
	 * 
	 * @param id
	 *            the id
	 * @return the int
	 */
	public static int delete(int id) {
		return Bean.delete("id=?", new Object[] { id }, User.class);
	}

}