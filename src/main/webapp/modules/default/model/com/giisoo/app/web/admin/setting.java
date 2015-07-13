/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.util.LinkedHashMap;
import java.util.Map;

import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.utils.Shell;
import com.giisoo.framework.web.*;

public class setting extends Model {

	private static Map<String, setting> settings = new LinkedHashMap<String, setting>();

	final public static void register(String name, setting m) {
		settings.put(name, m);
	}

	@Path(path = "get/(.*)", login = true, access = "access.config.admin")
	final public void get(String name) {
		setting s = settings.get(name);
		if (s != null) {
			s.req = this.req;
			s.resp = this.resp;
			s.login = this.login;
			s.lang = this.lang;
			s.module = this.module;
			s.get();

			s.set("lang", lang);
			s.set("module", module);
			s.set("name", name);
			s.set("settings", settings.keySet());
			s.show("/admin/setting.html");
		}
	}

	@Path(path = "set/(.*)", login = true, access = "access.config.admin", log = Model.METHOD_POST)
	final public void set(String name) {
		setting s = settings.get(name);
		if (s != null) {
			s.req = this.req;
			s.resp = this.resp;
			s.lang = this.lang;
			s.login = this.login;
			s.module = this.module;
			s.set();

			s.set("lang", lang);
			s.set("module", module);
			s.set("name", name);
			s.set("settings", settings.keySet());
			s.show("/admin/setting.html");
		}
	}

	/**
	 * invoked when post setting form
	 * 
	 */
	public void set() {

	}

	/**
	 * invoked when get the setting form
	 * 
	 */
	public void get() {

	}

	@Path(login = true, access = "access.config.admin")
	public final void onGet() {

		if (settings.size() > 0) {
			String name = settings.keySet().iterator().next();
			this.set("name", name);
			get(name);
		}

		this.println("not find page");

	}

	public static class system extends setting {

		@Override
		public void set() {
			SystemConfig.setConfig("node.name", this.getString("nodename"));
			SystemConfig.setConfig("system.code", this.getString("systemcode"));
			SystemConfig.setConfig("deploy.code", this.getString("deploycode"));

			SystemConfig.setConfig("oplog.max", this.getInt("oplog_max"));
			SystemConfig.setConfig("oplog.min", this.getInt("oplog_min"));

			// if (!X.isEmpty(this.getString("prikey"))) {
			// SystemConfig.setConfig("pri_key", this.getString("prikey")
			// .trim());
			// }
			// SystemConfig.setConfig("pub_key",
			// this.getString("pubkey").trim());

			this.set(
					X.MESSAGE,
					lang.get("save.success") + ", "
							+ lang.get("restart.required"));

			SystemConfig.setConfig("ntp.server", this.getString("ntp"));

			if (!X.isEmpty(this.getString("ntp"))) {

				try {
					String r = Shell.run("ntpdate " + this.getString("ntp"));
					OpLog.info("ntp", null, "时钟同步： " + r);
				} catch (Exception e) {
					OpLog.error("ntp", null, "时钟同步： " + e.getMessage());
					log.error(e.getMessage(), e);
				}

			}

			get();
		}

		@Override
		public void get() {

			this.set("deploycode", SystemConfig.s("deploy.code", null));
			this.set("systemcode", SystemConfig.s("system.code", null));
			this.set("nodename", SystemConfig.s("node.name", null));

			this.set("oplog_max", SystemConfig.i("oplog.max", 50000));
			this.set("oplog_min", SystemConfig.i("oplog.min", 20000));

			// this.set("prikey", SystemConfig.s("pri_key", null));
			this.set("pubkey", SystemConfig.s("pub_key", null));

			this.set("ntp", SystemConfig.s("ntp.server", null));

			this.set("time", lang.format(System.currentTimeMillis(),
					"yyyy-MM-dd HH:mm:ss"));

			this.set("page", "/admin/setting.system.html");
		}

	}

}
