/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.giisoo.core.bean.X;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.conf.Config;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.common.UpgradeLog;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Module;

/**
 * 自动升级类，根據節點配置，支持并行中的单一节点、多模块升级。
 * 
 * @author joe
 * 
 */
public class UpgradeTask extends WorkerTask {

	static Log log = LogFactory.getLog(UpgradeTask.class);

	long interval = X.AMINUTE;

	@Override
	public String getName() {
		return "upgrade.task";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.worker.WorkerTask#onExecute()
	 */
	@Override
	public void onExecute() {

		interval = X.AMINUTE;

		Configuration conf = Config.getConfig();

		String url = SystemConfig.s(conf.getString("node")
				+ ".upgrade.framework.url", null);

		if (X.isEmpty(url)) {
			OpLog.log("autoupgrade", "upgrade.framework.url missed", null);
			interval = X.AHOUR;
			return;
		}

		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url + "/admin/upgrade/ver?modules="
					+ getModules());

			HttpResponse resp = client.execute(get);
			HttpEntity e = resp.getEntity();
			if (e == null) {
				// OpLog.log("autoupgrade",
				// "can not get the ver info from remote");
				interval = X.AMINUTE;
				return;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					e.getContent(), "utf8"));

			StringBuilder sb = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				sb.append(line).append("\r\n");
				line = reader.readLine();
			}
			reader.close();
			try {
				JSONObject jo = JSONObject.fromObject(sb.toString());

				String release = jo.has("release") ? jo.getString("release")
						: null;
				String build = jo.has("build") ? jo.getString("build") : null;
				if (release == null || build == null) {
					// OpLog.log("autoupgrade", "response error: " +
					// sb.toString());
					interval = X.AMINUTE;
					return;
				}

				if (!checkBuild(jo)) {
					/**
					 * server has different version or build
					 */

					get = new HttpGet(url + "/admin/upgrade/get?modules="
							+ getModules());

					resp = client.execute(get);
					e = resp.getEntity();
					if (e == null) {
						// OpLog.log("autoupgrade",
						// "can not get the ver info from remote");
						interval = X.AMINUTE;
						return;
					}

					try {
						// TODO, enhancement, during unzip, the system was
						// shutdown ?!
						StringBuilder getmodules = new StringBuilder(
								"framework;default");
						String modules = getModules();
						if (modules != null) {
							String[] ss = modules.split(",");
							for (String s : ss) {
								if (!X.isEmpty(s)) {
									if (jo.has(s)) {
										JSONObject j = jo.getJSONObject(s);
										getmodules.append(";").append(s)
												.append(":")
												.append(j.getString("version"))
												.append(".")
												.append(j.getString("build"));
									}
								}
							}
						}

						UpgradeLog.insert(V.create("url", url)
								.set("modules", getmodules.toString())
								.set("flag", UpgradeLog.FLAG_CLIENT)
								.set("_release", release).set("build", build));

						/**
						 * catch all error avoid "jvm" hot-plug-in issue
						 */
						ZipInputStream in = new ZipInputStream(e.getContent());
						ZipEntry e1 = in.getNextEntry();
						while (e1 != null) {
							File f = new File(Model.HOME + e1.getName());
							if (e1.isDirectory()) {
								f.mkdirs();
							} else {
								f.getParentFile().mkdirs();
								OutputStream out = new FileOutputStream(f);
								Model.copy(in, out, false);
								out.close();
							}

							e1 = in.getNextEntry();
						}

						// SystemConfig.setConfig(conf.getString("node")
						// + ".build", build);
						// SystemConfig.setConfig(conf.getString("node")
						// + ".release", release);

						OpLog.log("autoupgrade", "upgrade success to "
								+ release + "_" + build, null);
					} catch (Throwable e2) {
						/**
						 * because of the libary changed, this method may
						 * inaccessiable
						 */
						log.error(e2.getMessage(), e2);
					}

					/**
					 * upgrade success, shutdown the application and let's
					 * appdog to restart it
					 */
					System.exit(0);
				} else {
					interval = X.AHOUR;
				}
			} catch (Exception e1) {
				interval = X.AMINUTE;
				log.error(sb.toString(), e1);
			}
		} catch (Exception e) {
			// OpLog.log("autoupgrade", "upgrade failed");

			interval = X.AMINUTE;
			log.error(e.getMessage(), e);
		}
	}

	private String getModules() {
		return SystemConfig.s(conf.getString("node")
				+ ".upgrade.framework.modules", "");
	}

	private boolean checkBuild(JSONObject jo) {
		String release = jo.getString("release");
		String build = jo.getString("build");

		/**
		 * 比较框架的版本和build
		 */
		if (!release.equals(Module.load("default").getVersion())) {
			return false;
		}

		if (!build.equals(Module.load("default").getBuild())) {
			return false;
		}

		/**
		 * check each modules
		 */
		String modules = getModules();
		if (modules != null) {
			String[] ss = modules.split(",");
			for (String s : ss) {
				if (!X.isEmpty(s)) {
					if (jo.has(s)) {
						JSONObject j = jo.getJSONObject(s);
						Module m = Module.load(s);
						JSONObject j1 = new JSONObject();
						j1.put("version", m.getVersion());
						j1.put("build", m.getBuild());
						if (!j.equals(j1)) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.worker.WorkerTask#onFinish()
	 */
	@Override
	public void onFinish() {
		this.schedule(interval);
	}

}
