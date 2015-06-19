/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.Bean.W;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.common.Session;
import com.giisoo.framework.common.Temp;
import com.giisoo.framework.web.*;

public class log extends Model {

	/**
	 * Popup2.
	 */
	@Path(path = "popup2", login = true, access = "access.log.query")
	public void popup2() {
		String type = this.getString("type");
		String cate = this.getString("cate");

		JSONObject jo = new JSONObject();
		List<String> list = OpLog.loadCategory(type, cate);
		if (list != null && list.size() > 0) {
			JSONArray arr = new JSONArray();
			for (String e : list) {
				JSONObject j = new JSONObject();
				j.put("value", e);
				if ("module".equals(type)) {
					j.put("name", lang.get("log.module_" + e));
				} else if ("op".equals(type)) {
					j.put("name", lang.get("log.opt_" + e));
				} else {
					j.put("name", e);
				}
				arr.add(j);
			}
			jo.put("list", arr);
			jo.put(X.STATE, 200);

		} else {
			jo.put(X.STATE, 201);
		}

		this.response(jo);

	}

	private W getW(JSONObject jo) {

		W w = W.create().copy(jo, W.OP_EQ, "op").copy(jo, W.OP_LIKE, "ip")
				.copyInt(jo, W.OP_EQ, "type", "uid");

		if (!X.isEmpty(jo.getString("_module"))) {
			w.and("module", jo.getString("_module"));
		}

		if (!X.isEmpty(jo.getString("_system"))) {
			w.and("system", jo.getString("_system"));
		}

		if (!X.isEmpty(jo.getString("starttime"))) {
			w.and("created", Bean.toInt(lang.format(
					lang.parse(jo.getString("starttime"), "yyyy-MM-dd"),
					"yyyyMMdd")), W.OP_GT_EQ);

		} else {
			long today_2 = System.currentTimeMillis() - X.ADAY * 2;
			jo.put("starttime", lang.format(today_2, "yyyy-MM-dd"));
			w.and("created", Bean.toInt(lang.format(today_2, "yyyyMMdd")),
					W.OP_GT_EQ);

		}

		if (!X.isEmpty(jo.getString("endtime"))) {
			w.and("created", Bean.toInt(lang.format(
					lang.parse(jo.getString("endtime"), "yyyy-MM-dd"),
					"yyyyMMdd")), W.OP_LT);
		}

		this.set(jo);

		return w;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Path(login = true, access = "access.log.query")
	public void onGet() {

		int s = this.getInt("s");
		int n = this.getInt("n", 10, "default.list.number");

		this.set("currentpage", s);

		JSONObject jo = this.getJSON();
		W w = getW(jo);

		Beans<OpLog> bs = OpLog.load(w, s, n);
		this.set(bs, s, n);

		this.show("/admin/oplog.index.html");
	}

	/**
	 * Clear.
	 */
	@Path(path = "clear", login = true, access = "access.log.admin")
	public void clear() {
		OpLog.cleanup(SystemConfig.i("oplog.min", 20000),
				SystemConfig.i("oplog.min", 20000));

		JSONObject jo = new JSONObject();
		jo.put(X.STATE, 200);

		this.response(jo);
	}

	/**
	 * Export.
	 */
	@Path(path = "export", login = true, access = "access.log.export")
	public void export() {

		/**
		 * export the logs to "csv" file, and redirect to the cvs file
		 */

		final JSONObject jo = this.getJSON();
		final W w = getW(jo);

		String id = UID.id(login.get("name"), jo.toString());
		String name = "oplog_" + Bean.millis2Date(System.currentTimeMillis())
				+ ".csv";
		final File f = Temp.get(id, name);

		if (f.exists()
				&& System.currentTimeMillis() - f.lastModified() > X.AHOUR) {
			f.delete();
		} else {
			f.getParentFile().mkdirs();
		}

		if (!f.exists()) {
			final Session session = this.getSession();
			session.set("oplog.exporting", 1).store();
			new WorkerTask() {

				@Override
				public void onExecute() {
					try {
						int s = 0;

						BufferedWriter out = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(f),
										"UTF-8"));

						/**
						 * output the header
						 */
						StringBuilder sb = new StringBuilder();
						sb.append("\"").append(lang.get("log.created"))
								.append("\",\"");
						sb.append(lang.get("log.user")).append("\",\"");
						sb.append(lang.get("log.ip")).append("\",\"");
						sb.append(lang.get("log.system")).append("\",\"");
						sb.append(lang.get("log.module")).append("\",\"");
						sb.append(lang.get("log.op")).append("\", \"");
						sb.append(lang.get("log.message")).append("\"");
						out.write(sb.toString() + "\r\n");

						Beans<OpLog> bs = OpLog.load(w, s, 100);
						while (bs != null && bs.getList() != null
								&& bs.getList().size() > 0) {
							for (OpLog p : bs.getList()) {
								sb = new StringBuilder();
								sb.append("\"")
										.append(lang.format(p.getCreated(),
												"yyyy-MM-dd hh:mm:ss"))
										.append("\",\"");

								if (p.getUser() != null) {
									sb.append(p.getUser().get("name")).append(
											"\",\"");
								} else {
									sb.append("\",\"");
								}

								if (p.getIp() != null) {
									sb.append(p.getIp()).append("\",\"");
								} else {
									sb.append("\",\"");
								}

								if (p.getSystem() != null) {
									sb.append(p.getSystem()).append("\",\"");
								} else {
									sb.append("\",\"");
								}
								if (p.getModule() != null) {
									sb.append(
											lang.get("log.module_"
													+ p.getModule())).append(
											"\",\"");
								} else {
									sb.append("\",\"");
								}

								if (p.getOp() != null) {
									sb.append(lang.get("log.opt_" + p.getOp()))
											.append("\"");
								} else {
									sb.append("\",\"");
								}

								if (p.getMessage() != null) {
									sb.append(p.getMessage()).append("\"");
								} else {
									sb.append("\",\"");
								}

								out.write(sb.toString() + "\r\n");
							}
							s += bs.getList().size();
							bs = OpLog.load(w, s, 100);
						}

						out.close();

						OpLog.info(OpLog.class, "export", jo.toString(), null,
								login.getId(), log.this.getRemoteHost());

					} catch (Exception e) {
						log.error(e.getMessage(), e);
					} finally {
						Session.load(session.sid()).remove("oplog.exporting")
								.store();
					}
				}

			}.schedule(0);

		}

		JSONObject jo1 = new JSONObject();
		jo1.put(X.STATE, 200);
		jo1.put("file", "/temp/" + id + "/" + name);
		jo1.put("size", f.length());
		jo1.put("updated", f.lastModified());
		if (this.getSession().get("oplog.exporting") != null) {
			jo1.put("exporting", 1);
		} else {
			jo1.put("exporting", 0);
		}

		this.response(jo1);
	}
}
