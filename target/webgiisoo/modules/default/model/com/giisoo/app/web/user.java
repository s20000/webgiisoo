/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.Bean.W;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.framework.common.App;
import com.giisoo.framework.common.Counter;
import com.giisoo.framework.common.Message;
import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.common.Role;
import com.giisoo.framework.common.Session;
import com.giisoo.framework.common.User;
import com.giisoo.framework.mdc.TConn;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Module;
import com.giisoo.framework.web.Path;
import com.giisoo.utils.base.Base64;
import com.giisoo.utils.base.DES;

/**
 * Web接口： /user/register, /user/login, /user/logout, /user/forget,
 * /user/callback, /user/message
 * 
 * @author joe
 * 
 */
public class user extends Model {

	/**
	 * Index.
	 */
	@Path()
	public void index() {
		if (login == null) {
			this.redirect("/user/login");
		} else if (login.hasAccess("access.admin")) {
			this.redirect("/admin");
		} else {
			this.redirect("/");
		}
	}

	/**
	 * Register.
	 */
	@Path(path = "register")
	public void register() {
		if (!"true".equals(SystemConfig.s("user.register", "true"))) {
			this.set(X.MESSAGE, lang.get("register.deny"));
			this.redirect("/user/login");
			return;
		}

		if (method.isPost()) {

			String name = this.getString("name");
			String email = this.getString("email");
			String pwd = this.getString("pwd");
			String nickname = this.getString("nickname");

			// Map<String, String> attr = new HashMap<String, String>();
			// attr.put("ip", this.getRemoteHost());
			// attr.put("browser", this.browser());

			log.debug("register:" + name + ", " + pwd + ", " + email + ", "
					+ nickname + ", browser=" + browser() + ", isMDC?"
					+ isMDC());

			JSONObject jo = this.getJSON();
			try {
				int id = User.create(name, pwd, jo);

				this.setUser(User.loadById(id));
				OpLog.log(User.class, "register", lang.get("create.success")
						+ ":" + name + ", uid=" + id);

				Session s = this.getSession();
				if (s.has("uri")) {
					this.redirect((String) s.get("uri"));
				} else {
					this.redirect("/");
				}

				return;
			} catch (Exception e) {
				log.error(e.getMessage(), e);

				this.put(X.MESSAGE, lang.get("create_user_error"));
				OpLog.log(User.class, "register", lang.get("create.failed")
						+ ":" + name);
			}

		} else if (method.isMdc()) {

			String name = this.getString("name");
			if (TConn.ALLOW_USER == null || "*".equals(TConn.ALLOW_USER)
					|| (name != null && name.matches(TConn.ALLOW_USER))) {
				String pwd = this.getString("password");
				if (pwd == null) {
					pwd = this.getString("uid");
				}
				String nickname = this.getString("nickname");

				// Map<String, String> attr = new HashMap<String, String>();
				// attr.put("ip", this.getRemoteHost());
				// attr.put("browser", this.browser());

				log.debug("register:" + name + ", " + pwd + ", " + nickname
						+ ", browser=" + browser() + ", isMDC?" + isMDC());

				JSONObject jo = this.getJSON();

				try {
					int id = User.create(name, pwd, jo);

					this.put(X.STATE, X.OK);
					this.put("id", id);
				} catch (Exception e) {
					log.error(e.getMessage(), e);

					this.put(X.STATE, X.FAIL201);
					this.put(X.MESSAGE, lang.get("unknown_error"));
				}
			} else {
				log.warn("deny mdc register, name:" + name + ", allowed: "
						+ TConn.ALLOW_USER);
				this.put(X.STATE, X.FAIL401);
				this.put(X.MESSAGE, lang.get("name_not_allow"));
			}

			return;

		}

		if ("1".equals(this.getString("mdc"))) {
			this.mockMdc();

			onMDC();

			this.set("jsonstr", mockMdc.toString());
			this.show("/ajax/json.html");
			return;
		}

		this.set("me", this.getUser());

		show("/user/user.register.html");

	}

	/**
	 * Message_unread.
	 */
	@Path(path = "message/unread", login = true, method = Model.METHOD_GET
			| Model.METHOD_POST)
	public void message_unread() {
		String ids = this.getString("id");
		int updated = 0;
		if (ids != null) {
			String[] ss = ids.split(",");
			V v = V.create("flag", Message.FLAG_NEW);
			for (String s : ss) {
				updated += Message.update(login.getId(), s, v);
			}
		}

		if (updated > 0) {
			this.set(X.MESSAGE, lang.get("set_success"));
		} else {
			this.set(X.MESSAGE, lang.get("select.required"));
		}

		message();
	}

	/**
	 * Go.
	 * 
	 * @return true, if successful
	 */
	@Path(path = "go", login = true)
	public boolean go() {

		Session s = this.getSession();
		if (s.has("oauth.callback")) {
			// it's come from oauth
			String url = (String) s.get("oauth.callback");
			String key = (String) s.get("oauth.appkey");

			try {

				JSONObject jo = new JSONObject();
				jo.put("uid", login.getId());
				jo.put("time", System.currentTimeMillis());
				jo.put("method", "login");
				JSONObject j1 = new JSONObject();
				login.toJSON(j1);
				jo.put("user", j1);

				jo.convertStringtoBase64();

				String data = URLEncoder.encode(
						Base64.encode(DES.encode(jo.toString().getBytes(),
								key.getBytes())), "UTF-8");

				if (url.indexOf("?") > 0) {
					this.redirect(url + "&data=" + data);
				} else {
					this.redirect(url + "?data=" + data);
				}
				s.remove("oauth.callback").remove("oauth.appkey").store();

				return true;
			} catch (Exception e) {
				log.error("url=" + url + ", key=" + key, e);
			}
		} else if (s.has("uri")) {
			String uri = (String) s.get("uri");

			log.debug("redirecting:" + uri);

			if (uri.endsWith("/index")) {
				uri = uri.substring(0, uri.length() - 6);
			}

			if (X.isEmpty(uri)) {
				this.redirect("/");
			} else {
				this.redirect(uri);
			}
			s.remove("uri").store();

			return true;
		}

		this.redirect("/");

		/**
		 * default, return false for "inherit" module to re-write it;
		 */
		return false;

	}

	/**
	 * Login_popup.
	 */
	@Path(path = "login/popup")
	public void login_popup() {
		login();
	}

	/**
	 * Login.
	 */
	@Path(path = "login", log = Model.METHOD_POST)
	public void login() {
		if (method.isPost()) {

			String name = this.getString("name");
			String pwd = this.getString("pwd");

			User me = User.load(name, pwd);
			log.debug("login: " + sid() + "-" + me);
			if (me != null) {

				int uid = me.getId();
				long time = System.currentTimeMillis() - X.AHOUR;
				List<User.Lock> list = User.Lock.loadByHost(uid, time,
						this.getRemoteHost());

				if (me.isLocked() || (list != null && list.size() >= 6)) {
					// locked by the host
					me.failed(this.getRemoteHost(), sid(), this.browser());
					this.set(X.MESSAGE, lang.get("account.locked.error"));

					this.set("name", name);
					this.set("pwd", pwd);
				} else {
					list = User.Lock.loadBySid(uid, time, sid());
					if (list != null && list.size() >= 3) {
						me.failed(this.getRemoteHost(), sid(), this.browser());
						this.set(X.MESSAGE, lang.get("account.locked.error"));
						this.set("name", name);
						this.set("pwd", pwd);
					} else {

						this.setUser(me);

						/**
						 * logined, to update the stat data
						 */
						me.logined(sid(), this.getRemoteHost());

						this.redirect("/user/go");

						return;
					}
				}

			} else {

				OpLog.warn(User.class, "user.login", lang.get("login.failed")
						+ ":" + name + ", ip:" + this.getRemoteHost(), null);

				User u = User.load(name);
				if (u == null) {
					this.put("message", lang.get("login.name_password.error"));
				} else {
					u.failed(this.getRemoteHost(), sid(), this.browser());

					List<User.Lock> list = User.Lock.loadByHost(u.getId(),
							System.currentTimeMillis() - X.AHOUR,
							this.getRemoteHost());

					if (list != null && list.size() >= 6) {
						this.put("message", lang.get("login.locked.error"));
					} else {
						list = User.Lock.loadBySid(u.getId(),
								System.currentTimeMillis() - X.AHOUR, sid());
						if (list != null && list.size() >= 3) {
							this.put("message", lang.get("login.locked.error"));
						} else {
							this.put(
									"message",
									String.format(
											lang.get("login.name_password.error.times"),
											list.size()));
						}
					}
				}

				this.set("name", name);
				this.set("pwd", pwd);
			}
		} else if (method.isMdc()) {

			String name = this.getString("name");
			if (TConn.ALLOW_USER == null || "*".equals(TConn.ALLOW_USER)
					|| (name != null && name.matches(TConn.ALLOW_USER))) {

				Counter.add("mdc.login", 1);

				try {
					String uid = this.getString("uid");
					if (uid == null) {
						uid = this.getString("password");
					}

					User u = User.load(name, uid);
					if (u == null) {
						// createTestUser(name, uid);
						this.put(X.STATE, X.FAIL);
						this.set(X.MESSAGE,
								lang.get("login.name_password.error"));
					} else if (u.isLocked()) {
						OpLog.warn(User.class, "login",
								lang.get("login.locked") + ":" + name + ", ip:"
										+ this.getRemoteHost(), null);
						this.set(X.MESSAGE, lang.get("login.locked"));
					} else {

						log.debug("id=" + u.getId() + ", u=" + u);
						this.setUser(u);

						String clientid = this.getHeader("clientid");
						TConn.update(clientid, V.create("uid", u.getId()));

						// TConn c = this.getConnection();
						// c.setId(u.getId());
						// TConnCenter.add(this.getConnection());

						this.put("rank", u.getInt("rank"));
						this.put("id", u.getId());
						this.put(X.STATE, X.OK);
						// this.put(X.STATE, X.FAIL);
						// this.put(X.MESSAGE,
						// "login failed, name and uid not match");

						OpLog.info(User.class, "login",
								lang.get("login.success") + ":" + name
										+ ", ip:" + this.getRemoteHost(), null);

						Counter.add("mdc.logined", 1);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					this.put(X.STATE, X.FAIL);
					this.put(X.MESSAGE,
							"name, uid are required, and must match as register");

					OpLog.warn(User.class, "login", lang.get("login.forbidden")
							+ ":" + name + ", allowed: " + TConn.ALLOW_USER
							+ ", ip:" + this.getRemoteHost(), null);

				}
			} else {
				log.warn("deny the user login:" + name + ", allowed: "
						+ TConn.ALLOW_USER);
				this.put(X.STATE, X.FAIL);
				this.put(X.MESSAGE, "the name is not allowed");

				OpLog.warn(User.class, "login", lang.get("login.forbidden")
						+ ":" + name + ", allowed: " + TConn.ALLOW_USER
						+ ", ip:" + this.getRemoteHost(), null, -1,
						this.getRemoteHost());

			}

			return;
		}

		String refer = this.getString("refer");
		if (!X.isEmpty(refer)) {
			try {
				this.getSession().set("uri", URLDecoder.decode(refer, "UTF-8"))
						.store();
			} catch (Exception e) {
				log.error(refer, e);
			}
		}

		/**
		 * test the oauth authenticated is true, if so then redirect to SSO
		 */
		if ("true".equals(SystemConfig.s("oauth.enabled", null))) {
			String oauth = SystemConfig.s("oauth.url", null);
			String appid = SystemConfig.s("oauth.appid", null);
			String key = SystemConfig.s("oauth.key", null);
			if (oauth != null && appid != null && key != null) {
				String callback = SystemConfig.s("oauth.callback", "");
				try {
					StringBuilder url = new StringBuilder(oauth);
					if (!oauth.endsWith("/")) {
						url.append("/");
					}
					JSONObject jo = new JSONObject();
					jo.put("callback", callback);
					jo.put("force", false);
					jo.put("time", System.currentTimeMillis());

					log.debug("data=" + jo);
					String data = Base64.encode(DES.encode(jo.toString()
							.getBytes(), key.getBytes()));
					data = URLEncoder.encode(data, "UTF-8");

					url.append(appid).append("/login").append("?data=")
							.append(data);

					this.redirect(url.toString());

					return;
				} catch (Exception e) {
					log.error("oauth=" + oauth + ", appid=" + appid + ", key="
							+ key, e);
				}
			}
		}

		/**
		 * normal authentication
		 */
		show("/user/user.login.html");
	}

	/**
	 * Logout.
	 */
	@Path(path = "logout", method = Model.METHOD_GET | Model.METHOD_POST
			| Model.METHOD_MDC)
	public void logout() {
		if (this.getUser() != null) {
			this.getUser().logout();
		}

		if (method.isMdc()) {
			String clientid = this.getHeader("clientid");
			TConn.update(clientid, V.create("uid", -1));

			setUser(null);

			this.put(X.STATE, X.OK);
			this.put(X.MESSAGE, "logout");
			return;
		}

		if (this.getUser() != null) {
			/**
			 * clear the user in session, but still keep the session
			 */
			setUser(null);

			/**
			 * test the oauth authenticated is true
			 */
			if ("true".equals(SystemConfig.s("oauth.enabled", null))) {
				String oauth = SystemConfig.s("oauth.url", null);
				String appid = SystemConfig.s("oauth.appid", null);
				String key = SystemConfig.s("oauth.key", null);
				if (oauth != null && appid != null && key != null) {
					String callback = SystemConfig.s("oauth.callback", "");
					try {
						StringBuilder url = new StringBuilder(oauth);
						if (!oauth.endsWith("/")) {
							url.append("/");
						}

						JSONObject jo = new JSONObject();
						jo.put("callback", callback);
						jo.put("force", false);
						jo.put("time", System.currentTimeMillis());

						String data = Base64.encode(DES.encode(jo.toString()
								.getBytes(), key.getBytes()));
						data = URLEncoder.encode(data, "UTF-8");
						log.debug("data=" + data);

						url.append(appid).append("/logout").append("?data=")
								.append(data);

						this.redirect(url.toString());

						return;
					} catch (Exception e) {
						log.error("oauth=" + oauth + ", appid=" + appid
								+ ", key=" + key, e);
					}
				}
			}
		}

		int s = 0;
		Beans<App> bs = App.load(null, s, 10);
		while (bs != null && bs.getList() != null && bs.getList().size() > 0) {
			for (App a : bs.getList()) {
				String key = "sso.oauth." + a.getAppid();
				if ("1".equals(this.getSession().get(key))) {
					this.getSession().remove(key).store();
					if (!X.isEmpty(a.getLogout())) {
						this.redirect(a.getLogout());
						return;
					}
				}
			}
			s += bs.getList().size();
			bs = App.load(null, s, 10);

		}

		/**
		 * redirect to home
		 */
		this.redirect("/");

	}

	/**
	 * Verify.
	 */
	@Path(path = "verify", login = true, access = "access.user.query")
	public void verify() {
		String name = this.getString("name");
		String value = this.getString("value");

		JSONObject jo = new JSONObject();
		if ("name".equals(name)) {
			if (User.exists(W.create("name", value).and("deleted", 0, W.OP_EQ)
					.and("locked", 0, W.OP_EQ).and("remote", 0, W.OP_EQ))) {

				jo.put(X.STATE, 201);
				jo.put(X.MESSAGE, lang.get("user.name.exists"));

			} else if (User.exists(W.create("name", value)
					.and("locked", 0, W.OP_EQ).and("remote", 0, W.OP_EQ))) {
				jo.put(X.STATE, 202);
				jo.put(X.MESSAGE, lang.get("user.override.exists"));
			} else {
				String allow = SystemConfig.s("user.name",
						"^[a-zA-Z0-9]{4,16}$");

				if (X.isEmpty(value) || !value.matches(allow)) {
					jo.put(X.STATE, 201);
					jo.put(X.MESSAGE, lang.get("user.name.format.error"));
				} else {
					jo.put(X.STATE, 200);
				}
			}
		} else if ("certid".equals(name)) {
			if (X.isEmpty(value)) {
				jo.put(X.STATE, 200);
			} else {
				User u = User.loadById(value);
				if (u != null) {
					jo.put(X.STATE, 201);
					jo.put(X.MESSAGE, lang.get("user.certid.exists"));
				} else {
					value = value.toLowerCase();
					String allow = Module._conf.getString("user.certid",
							"^[0-9]{17}[0-9x]$");
					if (!value.matches(allow)) {
						jo.put(X.STATE, 201);
						jo.put(X.MESSAGE, lang.get("user.certid.format.error"));
					} else {
						jo.put(X.STATE, 200);
					}
				}
			}
		} else if ("password".equals(name)) {
			if (X.isEmpty(value)) {
				jo.put(X.STATE, 201);
				jo.put(X.MESSAGE, lang.get("user.password.format.error"));
			} else {

				String allow = SystemConfig.s("user.password",
						"^[a-zA-Z0-9]{6,16}$");
				if (!value.matches(allow)) {
					jo.put(X.STATE, 201);
					jo.put(X.MESSAGE, lang.get("user.password.format.error"));
				} else {
					jo.put(X.STATE, 200);
				}
			}
		} else {
			if (X.isEmpty(value)) {
				jo.put(X.STATE, 201);
				jo.put(X.MESSAGE, lang.get("user.not.empty"));
			} else {
				jo.put(X.STATE, 200);
			}
		}

		this.response(jo);
	}

	/**
	 * Popup2.
	 */
	@Path(path = "popup2", login = true, access = "access.user.query")
	public void popup2() {
		String access = this.getString("access");
		List<User> list = null;
		if (!X.isEmpty(access)) {
			list = User.loadByAccess(access);
		} else {
			Beans<User> bs = User.load(W.create().and("id", 0, W.OP_GT), 0,
					1000);
			if (bs != null) {
				list = bs.getList();
			}
		}

		JSONObject jo = new JSONObject();
		if (list != null && list.size() > 0) {
			JSONArray arr = new JSONArray();
			for (User e : list) {
				JSONObject j = new JSONObject();
				j.put("value", e.getId());
				j.put("name", e.get("nickname") + "(" + e.get("name") + ")");
				arr.add(j);
			}
			jo.put("list", arr);
			jo.put(X.STATE, 200);

		} else {
			jo.put(X.STATE, 201);
		}

		this.response(jo);

	}

	/**
	 * Message_count.
	 */
	@Path(path = "message/count", login = true)
	public void message_count() {
		JSONObject jo = new JSONObject();
		Beans<Message> bs = Message.load(login.getId(),
				W.create("flag", Message.FLAG_NEW), 0, 1);
		if (bs != null && bs.getTotal() > 0) {
			jo.put("count", bs.getTotal());
		} else {
			jo.put("count", 0);
		}
		jo.put(X.STATE, 200);
		this.response(jo);
	}

	/**
	 * Message_delete.
	 */
	@Path(path = "message/delete", login = true)
	public void message_delete() {
		String ids = this.getString("id");
		int updated = 0;
		if (ids != null) {
			String[] ss = ids.split(",");
			for (String s : ss) {
				updated += Message.delete(login.getId(), s);
			}
		}

		if (updated > 0) {
			this.set(X.MESSAGE, lang.get("delete_success"));
		} else {
			this.set(X.MESSAGE, lang.get("select.required"));
		}

		message();
	}

	/**
	 * Message_detail.
	 */
	@Path(path = "message/detail", login = true)
	public void message_detail() {
		String id = this.getString("id");
		if (id == null) {
			message();
			return;
		}

		Message m = Message.load(login.getId(), id);
		if (m == null) {
			message();
			return;
		}

		this.set("m", m);

		this.show("/user/message.detail.html");
	}

	/**
	 * Message_mark.
	 */
	@Path(path = "message/mark", login = true)
	public void message_mark() {
		String ids = this.getString("id");
		int updated = 0;
		if (ids != null) {
			String[] ss = ids.split(",");
			V v = V.create("flag", Message.FLAG_MARK);
			for (String s : ss) {
				updated += Message.update(login.getId(), s, v);
			}
		}

		if (updated > 0) {
			this.set(X.MESSAGE, lang.get("save.success"));
		} else {
			this.set(X.MESSAGE, lang.get("select.required"));
		}

		message();
	}

	/**
	 * Message_done.
	 */
	@Path(path = "message/done", login = true)
	public void message_done() {
		String ids = this.getString("id");
		int updated = 0;
		if (ids != null) {
			String[] ss = ids.split(",");
			V v = V.create("flag", Message.FLAG_DONE);
			for (String s : ss) {
				updated += Message.update(login.getId(), s, v);
			}
		}

		if (updated > 0) {
			this.set(X.MESSAGE, lang.get("save.success"));
		} else {
			this.set(X.MESSAGE, lang.get("select.required"));
		}

		message();

	}

	/**
	 * Message.
	 */
	@Path(path = "message", login = true)
	public void message() {

		JSONObject jo = this.getJSON();
		if (!"message".equals(this.path)) {
			Object o = this.getSession().get("query");
			if (o != null && o instanceof JSONObject) {
				jo.clear();
				jo.putAll((JSONObject) o);
			}
		} else {
			this.getSession().set("query", jo).store();
		}
		W w = W.create();
		w.copy(jo, W.OP_LIKE, "subject").copy(jo, W.OP_EQ, "flag");
		this.set(jo);

		int s = this.getInt(jo, "s");
		int n = this.getInt(jo, "n", 10, "default.list.number");

		Beans<Message> bs = Message.load(login.getId(), w, s, n);
		this.set(bs, s, n);
		if (bs != null && bs.getList() != null && bs.getList().size() > 0) {
			for (Message m : bs.getList()) {
				if (Message.FLAG_NEW.equals(m.getFlag())) {
					m.update(V.create("flag", Message.FLAG_READ));
				}
			}
		}

		this.show("/user/user.message.html");
	}

	/**
	 * Dashboard.
	 */
	@Path(path = "dashboard", login = true)
	public void dashboard() {

		/**
		 * get the total of user messages, new messages
		 */
		Beans<Message> bs = Message.load(login.getId(),
				W.create("flag", Message.FLAG_NEW), 0, 1);
		if (bs != null && bs.getTotal() > 0) {
			this.set("message_new", bs.getTotal());
		}

		bs = Message.load(login.getId(), W.create(), 0, 1);
		if (bs != null && bs.getTotal() > 0) {
			this.set("message_total", bs.getTotal());
		}

		this.show("/user/user.dashboard.html");

	}

	/**
	 * Edits the.
	 */
	@Path(path = "edit", login = true, log = Model.METHOD_POST)
	public void edit() {
		if (method.isPost()) {
			int id = login.getId();
			JSONObject j = this.getJSON();
			User u = User.loadById(id);
			if (u != null) {
				u.update(V.create().copy(j, "nickname", "password", "title",
						"department", "phone"));

				this.set("message", lang.get("message.edit.success"));

				this.set(X.MESSAGE, lang.get("save.success"));

			} else {
				this.set(X.ERROR, lang.get("save.failed"));
			}

			dashboard();
		} else {
			User u = User.loadById(login.getId());
			this.set("u", u);
			JSONObject jo = new JSONObject();
			u.toJSON(jo);
			this.set(jo);

			this.show("/user/user.edit.html");
		}
	}

	/**
	 * Callback.
	 */
	@Path(path = "callback")
	public void callback() {
		String data = this.getString("data");
		String key = SystemConfig.s("oauth.key", null);
		if (data != null && key != null) {
			try {
				// data = URLDecoder.decode(data, "UTF-8");
				byte[] bb = Base64.decode(data);
				bb = DES.decode(bb, key.getBytes());
				data = new String(bb);

				JSONObject jo = JSONObject.fromObject(data);
				jo.convertBase64toString();

				String method = jo.getString("method");
				if ("login".equals(method)) {
					if (jo.has("uid")
							&& jo.has("time")
							&& System.currentTimeMillis() - jo.getLong("time") < X.AMINUTE) {
						int uid = jo.getInt("uid");

						User me = User.loadById(uid);
						log.debug("uid=" + uid + ", user=" + me);

						if (me != null) {
							this.setUser(me);

							if ("true".equals(SystemConfig.s("cross.context",
									X.EMPTY))) {
								String sessionkey = SystemConfig.s(
										"session.key", "user");
								JSONObject j1 = new JSONObject();
								me.toJSON(j1);
								HttpSession s = this.getHttpSession(true);
								s.getServletContext().setAttribute(sessionkey,
										j1);

								// log.debug("set session: " + s + ", "
								// + sessionkey + "=" + j1);
							}

							this.redirect("/user/go");

							return;
						} else {
							log.warn("can not found uid=" + uid);

							/**
							 * force login again
							 */
							String oauth = SystemConfig.s("oauth.url", null);
							String appid = SystemConfig.s("oauth.appid", null);
							key = SystemConfig.s("oauth.key", null);
							if (oauth != null && appid != null && key != null) {
								String callback = SystemConfig.s(
										"oauth.callback", "");
								try {
									StringBuilder url = new StringBuilder(oauth);
									if (!oauth.endsWith("/")) {
										url.append("/");
									}
									jo = new JSONObject();
									jo.put("callback", callback);
									jo.put("force", true);

									bb = jo.toString().getBytes();
									bb = DES.encode(bb, key.getBytes());

									url.append(appid)
											.append("/login")
											.append("?data=")
											.append(URLEncoder.encode(
													Base64.encode(bb), "UTF-8"));

									this.redirect(url.toString());

									return;
								} catch (Exception e) {
									log.error("oauth=" + oauth + ", appid="
											+ appid + ", key=" + key, e);
								}
							}

						}
					}
				} else if ("logout".equals(method)) {

					if (jo.has("time")
							&& System.currentTimeMillis() - jo.getLong("time") < X.AMINUTE) {

						this.setUser(null);

						this.redirect("/user/go");

						return;
					}
				}

			} catch (Exception e) {
				log.error("data=" + data + ", key=" + key, e);
			}
		}

		this.println(lang.get("callback.failed"));

	}

	/**
	 * Forget.
	 */
	@Path(path = "forget", login = true)
	public void forget() {

		this.set("me", this.getUser());

		show("/user/user.forget.html");

	}

	/**
	 * Oauth.
	 * 
	 * @param appid
	 *            the appid
	 * @param method
	 *            the method
	 */
	@Path(path = "oauth/(.*)/(.*)")
	public void oauth(String appid, String method) {
		if ("login".equals(method)) {
			App a = App.load(appid);
			if (a != null) {
				String data = this.getString("data");
				if (data != null) {
					try {

						this.getSession().set("sso.oauth." + appid, "1")
								.store();

						// data = URLDecoder.decode(data, "UTF-8");
						// log.debug("data=" + data);
						data = new String(DES.decode(Base64.decode(data), a
								.getKey().getBytes()));

						JSONObject jo = JSONObject.fromObject(data);

						log.debug("data=" + jo);

						String url = jo.getString("callback");

						if (!jo.has("force") || !jo.getBoolean("force")) {
							User me = this.getUser();
							if (me != null) {
								jo = new JSONObject();
								jo.put("uid", me.getId());
								jo.put("time", System.currentTimeMillis());
								jo.put("method", "login");
								JSONObject j1 = new JSONObject();
								login.toJSON(j1);
								jo.put("user", j1);

								jo.convertStringtoBase64();

								data = URLEncoder
										.encode(Base64.encode(DES.encode(jo
												.toString().getBytes(), a
												.getKey().getBytes())), "UTF-8");

								if (url.indexOf("?") > 0) {
									this.redirect(url + "&data=" + data);
								} else {
									this.redirect(url + "?data=" + data);
								}
								return;
							}
						}

						this.getSession().set("oauth.callback", url)
								.set("oauth.appkey", a.getKey()).store();

						this.redirect("/user/login");

					} catch (Exception e) {
						log.error("appid=" + appid + ", data=" + data, e);
					}
				}
			}

			this.println(lang.get("bad.appid"));
		} else if ("logout".equals(method)) {
			App a = App.load(appid);
			if (a != null) {
				String data = this.getString("data");
				if (data != null) {
					try {

						/**
						 * callback directly
						 */
						data = new String(DES.decode(Base64.decode(data), a
								.getKey().getBytes()));

						JSONObject jo = JSONObject.fromObject(data);

						String url = jo.getString("callback");

						if (this.getUser() != null) {
							this.setUser(null);

							jo = new JSONObject();
							jo.put("method", "logout");
							jo.put("time", System.currentTimeMillis());

							jo.convertStringtoBase64();

							data = URLEncoder.encode(Base64.encode(DES.encode(
									jo.toString().getBytes(), a.getKey()
											.getBytes())), "UTF-8");

							if (url.indexOf("?") > 0) {
								this.redirect(url + "&data=" + data);
							} else {
								this.redirect(url + "?data=" + data);
							}
							return;
						} else {
							logout();
						}
					} catch (Exception e) {
						log.error("appid=" + appid + ", data=" + data, e);
					}
				}
			}

			this.println(lang.get("bad.appid"));
		}
	}
}
