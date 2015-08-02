/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.Bean.W;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.*;
import com.giisoo.framework.web.*;
import com.mongodb.BasicDBObject;

public class user extends Model {

	/**
	 * Adds the.
	 */
	@Path(path = "add", login = true, access = "access.user.admin")
	public void add() {
		if (method.isPost()) {

			JSONObject jo = this.getJSON();
			String name = this.getString("name");
			// String password = this.getString("password");
			try {

				/**
				 * create the user
				 */
				if (User.exists(W.create("name", name).and("locked", 0)
						.and("remote", 0))) {
					/**
					 * exists, create failded
					 */
					this.set(X.ERROR, lang.get("user.name.exists"));
				} else {

					V v = V.create()
							.copy(jo, "name", "password", "company",
									"department", "nickname", "certid", "phone")
							.copyInt(jo, "rank").set("locked", 0);

					for (String s : this.getNames()) {
						v.set(s, this.getString(s));
					}

					// int space = this.getInt("total");
					// if (space > 0) {
					// space *= 1024 * 1024 * 1024;
					//
					// v.set("total", space);
					// }

					int id = User.create(v);

					/**
					 * set the role
					 */
					String[] roles = this.getStrings("role");
					if (roles != null) {
						User u = User.loadById(id);
						u.setRoles(roles);
					}

					/**
					 * set the shared place
					 */
					// Command.create("samba", "adduser",
					// V.create("params", name + "," + password));

					/**
					 * log
					 */
					OpLog.info(User.class, "user.add", null,
							"<a href='/admin/user/detail?id=" + id + "'>"
									+ name + "</a>", login.getId(),
							this.getRemoteHost());

					this.set(X.MESSAGE, lang.get("save.success"));

					onGet();
					return;
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);

				this.set(X.ERROR, lang.get("save.failed"));

				this.set(jo);
			}

		}

		Beans<Role> bs = Role.load(0, 1000);
		if (bs != null) {
			this.set("roles", bs.getList());
		}

		this.show("/admin/user.add.html");
	}

	/**
	 * History.
	 */
	@Path(path = "history", login = true, access = "access.user.admin")
	public void history() {

		int s = this.getInt("s");
		int n = this.getInt("n", 10, "default.list.number");

		JSONObject jo = this.getJSON();
		// W w = W.create().copy(jo, W.OP_EQ, "op")
		// .copyInt(jo, W.OP_EQ, "uid", "type").copy(jo, W.OP_LIKE, "ip");
		// if (jo.has("starttime")) {
		// long s1 = lang.parse(jo.getString("starttime"), "yyyy-MM-dd");
		// if (s1 > 0) {
		// w.and("created", s1, W.OP_GT_EQ);
		// }
		// }
		// if (jo.has("endtime")) {
		// long s1 = lang.parse(jo.getString("endtime"), "yyyy-MM-dd");
		// if (s1 > 0) {
		// w.and("created", s1, W.OP_LT_EQ);
		// }
		// }
		//
		// w.and("module", User.class.getName());

		BasicDBObject q = new BasicDBObject().append("module",
				User.class.getName());
		if (!X.isEmpty(jo.get("op"))) {
			q.append("op", jo.get("op"));
		}
		if (!X.isEmpty(jo.get("uid"))) {
			q.append("uid", Bean.toInt(jo.get("uid")));
		}
		if (!X.isEmpty(jo.get("type"))) {
			q.append("type", Bean.toInt(jo.get("type")));
		}
		if (!X.isEmpty(jo.get("ip"))) {
			q.append("ip", Pattern.compile(jo.getString("ip"),
					Pattern.CASE_INSENSITIVE));
		}
		if (!X.isEmpty(jo.getString("starttime"))) {
			q.append("created",
					new BasicDBObject().append("$gte",
							Bean.toInt(lang.format(lang.parse(
									jo.getString("starttime"), "yyyy-MM-dd"),
									"yyyyMMdd"))));

		} else {
			long today_2 = System.currentTimeMillis() - X.ADAY * 2;
			jo.put("starttime", lang.format(today_2, "yyyy-MM-dd"));
			q.append(
					"created",
					new BasicDBObject().append("$gte",
							Bean.toInt(lang.format(today_2, "yyyyMMdd"))));
		}

		if (!X.isEmpty(jo.getString("endtime"))) {
			q.append("created", new BasicDBObject().append("$lte", Bean
					.toInt(lang.format(
							lang.parse(jo.getString("endtime"), "yyyy-MM-dd"),
							"yyyyMMdd"))));
		}

		this.set("cate", User.class.getName());

		this.set(jo);

		this.set("currentpage", s);

		Beans<OpLog> bs = OpLog.load(q, s, n);
		this.set(bs, s, n);

		this.show("/admin/user.history.html");

	}

	/**
	 * Delete.
	 */
	@Path(path = "delete", login = true, access = "access.user.admin")
	public void delete() {
		String ids = this.getString("id");
		int updated = 0;
		if (ids != null) {
			String[] ss = ids.split(",");
			V v = V.create("locked", 1);
			for (String s : ss) {
				int id = Bean.toInt(s);
				if (id > 0) {
					User u = User.loadById(id);
					if (u != null && u.update(v) > 0) {

						OpLog.warn(User.class, "user.delete", u.get("name")
								+ "(" + id + ")", null, login.getId(),
								this.getRemoteHost());

						// Command.create("samba", "deluser",
						// V.create("params", u.getName()));

						updated++;
					}
				}
			}
		}

		if (updated > 0) {
			this.set(X.MESSAGE, lang.get("delete.success"));

			// Command.create("samba", "restart", V.create());

		} else {
			this.set(X.ERROR, lang.get("select.required"));
		}
		onGet();
	}

	/**
	 * Edits the.
	 */
	@Path(path = "edit", login = true, access = "access.user.admin")
	public void edit() {
		if (method.isPost()) {

			int id = this.getInt("id");
			JSONObject j = this.getJSON();
			User u = User.loadById(id);
			if (u != null) {
				V v = V.create()
						.copy(j, "nickname", "password", "title", "company",
								"department", "certid", "phone")
						.copyInt(j, "failtimes", "rank")
						.set("locked",
								"on".equals(this.getString("locked")) ? 1 : 0);

				/**
				 * get external
				 */
				for (String s : this.getNames()) {
					if (!"id".equals(s)) {
						v.set(s, this.getString(s));
					}
				}

				// long space = this.getInt("total");
				// if (space > 0) {
				// space *= 1024 * 1024 * 1024;
				//
				// v.set("total", space);
				// }

				u.update(v);

				if (!"on".equals(this.getString("locked"))) {
					/**
					 * clean all the locked info
					 */
					User.Lock.removed(id);
				}

				// if (!X.isEmpty(this.getString("password"))) {
				// Command.create(
				// "samba",
				// "passwd",
				// V.create(
				// "params",
				// u.getName() + ","
				// + this.getString("password")));
				// }

				this.set("message", lang.get("edit.success"));

				String[] roles = this.getStrings("role");
				if (roles != null) {
					u.setRoles(roles);
				}

				OpLog.info(
						User.class,
						"user.edit",
						null,
						"<a href='/admin/user/detail?id=" + id + "'>"
								+ u.get("name") + "</a>", login.getId(),
						this.getRemoteHost());

				this.set(X.MESSAGE, lang.get("save.success"));

			} else {
				this.set(X.ERROR, lang.get("save.failed"));
			}
			onGet();

		} else {
			String ids = this.getString("id");
			String[] ss = ids.split(",");
			for (String s : ss) {
				int i = Bean.toInt(s, -1);
				User u = User.loadById(i);
				if (u != null && u.getInt("remote") == 0) {
					JSONObject j = new JSONObject();
					u.toJSON(j);
					this.set(j);
					this.set("u", u);

					Beans<Role> bs = Role.load(0, 1000);
					if (bs != null) {
						this.set("roles", bs.getList());
					}

					this.show("/admin/user.edit.html");
					return;
				}
			}

			this.set(X.ERROR, lang.get("select.required"));
			onGet();

		}
	}

	/**
	 * Detail.
	 */
	@Path(path = "detail", login = true, access = "access.user.query")
	public void detail() {
		String id = this.getString("id");
		if (id != null) {
			int i = Bean.toInt(id, -1);
			User u = User.loadById(i);
			this.set("u", u);

			Beans<Role> bs = Role.load(0, 100);
			if (bs != null) {
				this.set("roles", bs.getList());
			}

			this.show("/admin/user.detail.html");
		} else {
			onGet();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Override
	@Path(login = true, access = "access.user.admin")
	public void onGet() {

		JSONObject jo = this.getJSON();

		W w = getW(jo);

		int s = this.getInt(jo, "s");
		int n = this.getInt(jo, "n", 10, "default.list.number");
		this.set("currentpage", s);

		this.query.clean().path("/admin/user").copy(jo);
		Beans<User> bs = User.load(w, -1, s, n);
		this.set(bs, s, n);

		/**
		 * make sure the link of pages correct
		 */
		this.path = null;

		this.show("/admin/user.index.html");
	}

	private W getW(JSONObject jo) {
		if ("add".equals(this.path) || "edit".equals(this.path)
				|| "delete".equals(this.path)) {
			Object o = this.getSession().get("query");
			if (o != null && o instanceof JSONObject) {
				jo.clear();
				jo.putAll((JSONObject) o);
			}
		} else {
			this.getSession().set("query", jo).store();
		}

		this.set(jo);
		return W.create().copy(jo, W.OP_LIKE, "name", "nickname", "company",
				"title", "certid");
	}

}
