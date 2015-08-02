/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.Bean.W;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.Dict;
import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.common.Repo;
import com.giisoo.framework.common.Temp;
import com.giisoo.framework.common.Repo.Entity;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Paging;
import com.giisoo.framework.web.Path;
import com.mongodb.BasicDBObject;

public class dict extends Model {

	/**
	 * Verify.
	 */
	@Path(path = "verify", login = true)
	public void verify() {
		String name = this.getString("name");
		String value = this.getString("value");

		JSONObject jo = new JSONObject();
		if ("name".equals(name)) {
			if (X.isEmpty(value)) {
				jo.put(X.STATE, 201);
				jo.put(X.MESSAGE, lang.get("name.empty.error"));
			} else {
				String parent = this.getString("parent");
				Dict d = Dict.find(parent, value);
				if (d == null) {
					jo.put(X.STATE, 200);
				} else {
					jo.put(X.STATE, 201);
					jo.put(X.MESSAGE, lang.get("name.exists.error"));
				}
			}
		} else if ("display".equals(name)) {
			if (X.isEmpty(value)) {
				jo.put(X.STATE, 201);
				jo.put(X.MESSAGE, lang.get("display.empty.error"));
			} else {
				jo.put(X.STATE, 200);
			}
		}

		this.response(jo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Path(login = true, access = "access.dict.query")
	public void onGet() {

		JSONObject jo = this.getJSON();
		if ("add".equals(this.path) || "edit".equals(this.path)
				|| "delete".equals(this.path) || "addbatch".equals(this.path)) {
			Object o = this.getSession().get("query");
			if (o != null && o instanceof JSONObject) {
				jo.clear();
				jo.putAll((JSONObject) o);
			}

			this.path = this.getString("parent");
		} else {
			this.path = this.path == null ? "root" : this.path;
		}

		jo.put("path", this.path);
		this.getSession().set("query", jo);

		this.set(jo);

		String parent = this.path;
		this.set("parent", parent);
		Dict d = Dict.load(parent);

		this.set("d", d);
		this.query.path("/admin/dict/" + parent).copy(jo, "name", "display");

		int s = this.getInt(jo, "s");
		int n = this.getInt(jo, "n", 10, "default.list.number");
		this.set("currentpage", s);

		W w = null;
		if (X.isEmpty(jo.getString("name"))
				&& X.isEmpty(jo.getString("display"))) {
			w = W.create("parent", parent);
		} else {
			this.path = "root";
			w = W.create().copy(jo, W.OP_LIKE, "name", "display");
		}

		Beans<Dict> bs = Dict.load(w, s, n);

		this.set(bs, s, n);

		this.show("/admin/dict.index.html");
	}

	/**
	 * Popup.
	 */
	@Path(path = "popup", login = true, access = "access.dict.query")
	public void popup() {

		String parent = this.getString("parent");
		String name = this.getString("name");

		int s = this.getInt("s");
		int n = this.getInt("n", 10, "default.list.number");

		Dict d = Dict.find(parent, name);
		if (d == null) {
			String id = UID.id(parent, name);
			Dict.insertOrUpdate(id, V.create("parent", parent)
					.set("name", name));
		} else {
			Beans<Dict> bs = d.loadSub(s, n);
			if (bs != null) {
				this.set("list", bs.getList());
				this.set("total", bs.getTotal());
				this.set("pages", Paging.create(bs.getTotal(), s, n));
			}
		}
		this.show("/admin/dict.popup.html");

	}

	/**
	 * Popup2.
	 */
	@Path(path = "popup2", login = true, access = "access.dict.query")
	public void popup2() {

		String parent = this.getString("parent");
		String name = this.getString("name");
		String value = this.getString("value");

		JSONObject jo = new JSONObject();
		Set<String> values = null;
		if (!X.isEmpty(value)) {
			String[] ss = value.split(",");
			values = new HashSet<String>();

			for (String s : ss) {
				if (!X.isEmpty(s)) {
					values.add(s.trim());
				}
			}
		}

		/**
		 * list all
		 */
		Dict d = Dict.find(parent, name);
		if (d == null) {
			String id = UID.id(parent, name);
			Dict.insertOrUpdate(id, V.create("parent", parent)
					.set("name", name));

			jo.put(X.STATE, 201);
		} else {
			Beans<Dict> bs = d.loadSub(0, 1000);
			if (bs != null && bs.getList() != null && bs.getList().size() > 0) {
				JSONArray arr = new JSONArray();

				/**
				 * ensure no duplicated
				 */
				Set<String> list = new HashSet<String>();

				for (Dict e : bs.getList()) {

					String s1 = e.getName().trim();
					if (values == null || values.contains(s1)) {
						if (!list.contains(e.getName())) {
							list.add(e.getName());

							JSONObject j = new JSONObject();
							j.put("value", e.getName());
							if (e.getDisplay().equals(e.getName())) {
								j.put("name", e.getDisplay());
							} else {
								j.put("name",
										e.getDisplay() + "(" + e.getName()
												+ ")");
							}
							arr.add(j);
						}
					}
				}

				jo.put(X.STATE, 200);
				jo.put("list", arr);
			} else {
				jo.put(X.STATE, 202);
			}
		}

		this.response(jo);

	}

	/**
	 * History.
	 */
	@Path(path = "history", login = true, access = "access.dict.admin")
	public void history() {
		int s = this.getInt("s");
		int n = this.getInt("n", 10, "default.list.number");

		JSONObject jo = this.getJSON();
		// W w = W.create().copy(jo, W.OP_EQ, "op")
		// .copyInt(jo, W.OP_EQ, "uid", "type").copy(jo, W.OP_LIKE, "ip");
		// w.and("module", Dict.class.getName());

		BasicDBObject q = new BasicDBObject().append("module",
				Dict.class.getName());
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

		this.set("cate", Dict.class.getName());

		this.set(jo);

		this.set("currentpage", s);

		Beans<OpLog> bs = OpLog.load(q, s, n);
		this.set(bs, s, n);

		this.show("/admin/dict.history.html");

	}

	/**
	 * Download.
	 */
	@Path(path = "download", login = true, access = "access.dict.edit")
	public void download() {
		String enc = this.getString("e");

		String id = UID.id(login.getId(), System.currentTimeMillis());
		File file = Temp.get(id, "dict.csv");
		file.getParentFile().mkdir();

		JSONObject jo = new JSONObject();
		BufferedWriter out = null;

		try {
			out = new BufferedWriter(new OutputStreamWriter(
					this.getOutputStream(), enc));
			out.write("\"FSNODEID\",\"FSPARENTID\",\"FILEVEL\",\"FIDISPLAYNO\",\"FSSTATNO\",\"FSNODENAME\",\"FSNODEVALUE\",\"FCACTIVESTAT\",\"FSFULLNUMBER\"\r\n");

			int s = 0;
			Beans<Dict> bs = Dict.load("root", s, 100);
			while (bs != null && bs.getList() != null
					&& bs.getList().size() > 0) {

				for (Dict d : bs.getList()) {
					output(null, d, out, 0);
				}

				s += bs.getList().size();
				bs = Dict.load("root", s, 100);
			}
			out.flush();
			out.close();

			jo.put(X.STATE, 200);
			jo.put("file", "/temp/" + id + "/dict.csv");

			OpLog.info(Dict.class, "download", X.EMPTY, null, login.getId(),
					this.getRemoteHost());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			jo.put(X.STATE, 201);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}

		this.response(jo);
	}

	private void output(String parentid, Dict d, BufferedWriter out, int level) {
		// out.println("\"FSNODEID\",\"FSPARENTID\",\"FILEVEL\",\"FIDISPLAYNO\",\"FSSTATNO\",\"FSNODENAME\",\"FSNODEVALUE\",\"FCACTIVESTAT\",\"FSFULLNUMBER");
		/**
		 * print self
		 */
		StringBuilder sb = new StringBuilder();
		sb.append("\"").append(d.getId()).append("\",\"");
		if (d.getParent() == null || "root".equals(d.getParent())) {
			sb.append("\",\"");
		} else {
			sb.append(d.getParent()).append("\",\"");
		}
		sb.append(level).append("\",\"");
		sb.append(d.getSeq()).append("\",\"");
		// FSTATNO
		sb.append("\",\"");
		// FSNODENAME
		if (X.isEmpty(d.getDisplay())) {
			sb.append("\",\"");
		} else {
			sb.append(d.getDisplay()).append("\",\"");
		}
		// FSNODEVALUE
		sb.append(d.getName()).append("\",\"");
		// FCACTIVESTAT
		sb.append("\",\"");
		// FSFULLNUMBER
		if (parentid != null) {
			sb.append(parentid).append(".");
		}
		sb.append(d.getId()).append("\"");
		try {
			out.write(sb.toString() + "\r\n");
		} catch (Exception e) {
			log.error(sb.toString(), e);
		}
		/**
		 * print all sub
		 */
		int s = 0;
		Beans<Dict> bs = d.loadSub(s, 100);
		while (bs != null && bs.getList() != null && bs.getList().size() > 0) {
			for (Dict d1 : bs.getList()) {
				if (parentid == null) {
					output(d.getId(), d1, out, level + 1);
				} else {
					output(parentid + "." + d.getId(), d1, out, level + 1);
				}
			}
			s += bs.getList().size();
			bs = d.loadSub(s, 100);
		}

	}

	/**
	 * Delete.
	 */
	@Path(path = "delete", login = true, access = "access.dict.edit")
	public void delete() {

		String ids = this.getString("id");

		int updated = 0;
		if (ids != null) {
			String[] ss = ids.split(",");
			for (String id : ss) {
				Dict d = Dict.load(id);
				if (Dict.remove(id) > 0) {
					this.set(X.MESSAGE, lang.get("delete.success"));

					OpLog.info(Dict.class, "delete",
							d.getDisplay() + "(" + d.getName() + ")", null,
							login.getId(), this.getRemoteHost());

					updated++;
				}
			}
		}

		if (updated == 0) {
			this.set(X.ERROR, lang.get("select.required"));
		} else {
			this.set(X.MESSAGE, lang.get("delete.success"));
		}

		onGet();
	}

	/**
	 * Edits the.
	 */
	@Path(path = "edit", login = true, access = "access.dict.edit")
	public void edit() {
		if (method.isPost()) {
			String id = this.getString("id");
			JSONObject jo = this.getJSON();
			if (Dict.insertOrUpdate(id,
					V.create().copy(jo, "parent", "name", "clazz", "display")
							.copyInt(jo, "seq")) > 0) {

				OpLog.info(
						Dict.class,
						"edit",
						"<a href='/admin/dict/" + id + "'>"
								+ jo.getString("display") + "("
								+ jo.getString("name") + ")</a>", null,
						login.getId(), this.getRemoteHost());

				this.set(X.MESSAGE, lang.get("save.success"));

				onGet();
				return;
			} else {
				this.set(jo);
				this.set(X.ERROR, lang.get("save.failed"));
			}

		} else {
			String ids = this.getString("id");

			boolean found = false;
			if (ids != null) {
				String[] ss = ids.split(",");
				if (ss.length > 0) {
					Dict d = Dict.load(ss[0]);
					if (d != null) {
						JSONObject jo = new JSONObject();
						d.toJSON(jo);
						this.set(jo);
						found = true;
						this.set("d", d);
					}
				}
			}

			if (!found) {
				this.set(X.ERROR, lang.get("select.required"));

				onGet();
				return;
			}
		}

		this.show("/admin/dict.edit.html");
	}

	/**
	 * Addbatch.
	 */
	@Path(path = "addbatch", login = true, access = "access.dict.edit")
	public void addbatch() {
		String url = this.getString("url");
		Entity e = Repo.loadByUri(url);
		if (e != null) {
			StringBuilder error = new StringBuilder();

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(e.getInputStream(), "UTF-8"));

				Map<String, Integer> cols = new HashMap<String, Integer>();

				int lines = 0;
				int errors = 0;
				String line = reader.readLine();
				while (line != null) {
					lines++;
					try {
						String[] ss = line.split("\"");
						if (cols.size() == 0 && ss.length > 2) {
							// add to cols
							for (int i = 0; i < ss.length; i++) {
								cols.put(ss[i].toLowerCase(), i);
							}
						} else {
							/**
							 * FSNODEID FSPARENTID FILEVEL FIDISPLAYNO FSSTATNO
							 * FSNODENAME, FSNODEVALUE
							 */

							String id = ss[cols.get("fsnodeid")];
							String parentid = ss[cols.get("fsparentid")];
							if (X.isEmpty(parentid)) {
								parentid = "root";
							}
							String name = ss[cols.get("fsnodevalue")];
							if (X.isEmpty(name)) {
								name = id;
							}
							int seq = Bean.toInt(ss[cols.get("fidisplayno")]);
							String display = ss[cols.get("fsnodename")];

							if (Dict.find(parentid, name) == null) {

								Dict.insertOrUpdate(
										id,
										V.create("parent", parentid)
												.set("name", name)
												.set("display", display)
												.set("seq", seq));

								OpLog.info(Dict.class, "add.batch",
										"<a href='/admin/dict/" + id + "'>"
												+ display + "(" + name
												+ ")</a>", null, login.getId(),
										this.getRemoteHost());
							}
						}

					} catch (Exception e1) {
						log.error(line, e1);

						errors++;
						if (error.length() > 0)
							error.append(",");
						error.append("line ").append(lines);
					}

					line = reader.readLine();
				}
				reader.close();

				this.set(
						X.MESSAGE,
						lang.get("add.success.dict.addbatch")
								+ lines
								+ "; "
								+ lang.get("add.failed.dict.addbatch")
								+ ":"
								+ errors
								+ (error.length() > 0 ? (", " + error
										.toString()) : X.EMPTY));
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
				this.set(X.ERROR, lang.get("add.failed"));
			} finally {
				e.delete();
			}
			onGet();

		} else {
			this.show("/admin/dict.addbatch.html");
		}

	}

	/**
	 * Adds the.
	 */
	@Path(path = "add", login = true, access = "access.dict.edit")
	public void add() {
		if (method.isPost()) {

			String parent = this.getString("parent");
			String name = this.getString("name");
			String clazz = this.getString("clazz");
			JSONObject jo = this.getJSON();

			String id = UID.id(parent, name, clazz);
			if (Dict.insertOrUpdate(id,
					V.create().copy(jo, "parent", "name", "clazz", "display")
							.copyInt(jo, "seq")) > 0) {

				OpLog.info(
						Dict.class,
						"add",
						"<a href='/admin/dict/" + id + "'>"
								+ jo.getString("display") + "("
								+ jo.getString("name") + ") </a>", null,
						login.getId(), this.getRemoteHost());

				this.set(X.MESSAGE, lang.get("save.success"));

				onGet();
				return;

			} else {
				this.set(jo);
				this.set(X.ERROR, lang.get("save.failed"));
			}

		} else {
			String parent = this.getString("parent");
			if (parent == null) {
				parent = "root";
			}

			Dict p = Dict.load(parent);
			if (p != null) {
				this.set("d", p);
			}

			this.set("parent", parent);
		}

		this.show("/admin/dict.add.html");
	}
}
