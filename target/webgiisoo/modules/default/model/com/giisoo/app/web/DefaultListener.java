/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.app.web.admin.setting;
import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.core.db.DB;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.Menu;
import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.mdc.utils.IP;
import com.giisoo.framework.utils.Shell;
import com.giisoo.framework.web.LifeListener;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Module;

/**
 * 启动监听器，当系统启动时，初始化数据库、资源、服务等
 * 
 * @author joe
 * 
 */
public class DefaultListener implements LifeListener {

	private class NtpTask extends WorkerTask {

		@Override
		public void onExecute() {
			String ntp = SystemConfig.s("ntp.server", null);
			if (!X.isEmpty(ntp)) {

				try {
					String r = Shell.run("ntpdate " + ntp);
					OpLog.info("ntp", X.EMPTY, "时钟同步： " + r);
				} catch (Exception e) {
					OpLog.error("ntp", X.EMPTY, "时钟同步： " + e.getMessage());
				}
			}
		}

		@Override
		public void onFinish() {
			this.schedule(X.AHOUR);
		}
	}

	static Log log = LogFactory.getLog(DefaultListener.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.framework.web.LifeListener#onStart(org.apache.commons.
	 * configuration.Configuration, com.giisoo.framework.web.Module)
	 */
	public void onStart(Configuration conf, Module module) {

		/**
		 * clean up the old version's jar
		 */
		if (cleanup(new File(Model.HOME), new HashMap<String, Object[]>())) {
			System.exit(0);
			return;
		}

		if ("true".equals(SystemConfig.s(conf.getString("node")
				+ ".upgrade.framework.enabled", "false"))) {
			new UpgradeTask().schedule(X.AMINUTE
					+ (long) (2 * X.AMINUTE * Math.random()));
		}

		// cleanup
		File f = new File(Model.HOME + "/WEB-INF/lib/mina-core-2.0.0-M4.jar");
		if (f.exists()) {
			f.delete();
			System.exit(0);
		}

		IP.init(conf);

		new NtpTask().schedule(X.AMINUTE);

		setting.register("system", new setting.system());
		setting.register("base", new setting.base());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.framework.web.LifeListener#onStop()
	 */
	public void onStop() {
	}

	public static void runDBScript(File f) throws IOException, SQLException {
		BufferedReader in = null;
		Connection c = null;
		Statement s = null;
		try {
			c = Bean.getConnection();
			if (c != null) {
				in = new BufferedReader(new InputStreamReader(
						new FileInputStream(f), "utf-8"));
				StringBuilder sb = new StringBuilder();
				try {
					String line = in.readLine();
					while (line != null) {
						line = line.trim();
						if (!"".equals(line) && !line.startsWith("#")) {

							sb.append(line).append("\r\n");

							if (line.endsWith(";")) {
								String sql = sb.toString().trim();

								try {
									s = c.createStatement();
									s.executeUpdate(sql);
									s.close();
								} catch (Exception e) {
									log.error(sb.toString(), e);
								}
								s = null;
								sb = new StringBuilder();
							}
						}
						line = in.readLine();
					}

					String sql = sb.toString().trim();
					if (!"".equals(sql)) {
						s = c.createStatement();
						s.executeUpdate(sql);
					}
				} catch (Exception e) {
					log.error(sb.toString(), e);
				}
			} else {
				log.warn("database not configured !!");
			}
		} finally {
			if (in != null) {
				in.close();
			}
			Bean.close(s, c);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.framework.web.LifeListener#upgrade(org.apache.commons.
	 * configuration.Configuration, com.giisoo.framework.web.Module)
	 */
	public void upgrade(Configuration conf, Module module) {
		log.debug(module + " upgrading...");

		/**
		 * test database connection has configured?
		 */
		try {
			/**
			 * test the database has been installed?
			 */
			String dbname = DB.getDriver();

			/**
			 * initial the database
			 */
			File f = module.loadResource("/install/" + dbname + "/initial.sql",
					false);
			if (f != null && f.exists()) {
				String key = module.getName() + ".db.initial." + dbname + "."
						+ f.lastModified();
				int b = SystemConfig.i(key, 0);
				if (b == 0) {
					log.warn("db[" + key
							+ "] has not been initialized! initializing...");

					try {
						runDBScript(f);
						SystemConfig.setConfig(key, (int) 1);
						log.warn("db[" + key + "] has been initialized! ");
					} catch (Exception e) {
						log.error(f.getAbsolutePath(), e);
					}

				}
			} else {
				log.warn("db[" + module.getName() + "." + dbname
						+ "] not exists ! ");
			}

			f = module.loadResource("/install/" + dbname + "/upgrade.sql",
					false);
			if (f != null && f.exists()) {
				String key = module.getName() + ".db.upgrade." + dbname + "."
						+ f.lastModified();
				int b = SystemConfig.i(key, 0);

				if (b == 0) {

					try {
						runDBScript(f);

						SystemConfig.setConfig(key, (int) 1);

						log.warn("db[" + key + "] has been upgraded! ");
					} catch (Exception e) {
						log.error(f.getAbsolutePath(), e);
					} finally {
					}

				}
			}

			/**
			 * recover all tables from db.zip
			 */
			f = module.loadResource("/install/db.zip", false);
			if (f != null && f.exists()) {
				int b = SystemConfig.i("db.zip", 0);
				if (b == 0) {
					DB.recover(new FileInputStream(f), (String) null);
					SystemConfig.setConfig("db.zip", 1);

					System.exit(0);
				}
			}
		} catch (Exception e) {
			log.error("database is not configured!", e);
			return;
		}

		/**
		 * check the menus
		 * 
		 */
		File f = module.getFile("/install/menu.json");
		if (f != null && f.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(f), "UTF-8"));
				StringBuilder sb = new StringBuilder();
				String line = reader.readLine();
				while (line != null) {
					sb.append(line).append("\r\n");
					line = reader.readLine();
				}

				/**
				 * convert the string to json array
				 */
				JSONArray arr = JSONArray.fromObject(sb.toString());
				Menu.insertOrUpdate(arr, module.getName());

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						log.error(e);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.framework.web.LifeListener#uninstall(org.apache.commons.
	 * configuration.Configuration, com.giisoo.framework.web.Module)
	 */
	public void uninstall(Configuration conf, Module module) {
		Menu.remove(module.getName());
	}

	private boolean cleanup(File f, Map<String, Object[]> map) {
		/**
		 * list and compare all jar files
		 */
		boolean changed = false;

		if (f.isDirectory()) {
			for (File f1 : f.listFiles()) {
				if (cleanup(f1, map)) {
					changed = true;
				}
			}
		} else if (f.isFile() && f.getName().endsWith(".jar")) {
			String name = f.getName();
			String[] ss = name.split("[-_]");
			if (ss.length > 1) {
				String ver = ss[ss.length - 1];
				name = name.substring(0, name.length() - ver.length() - 1);
				ver = ver.substring(0, ver.length() - 4); // cut off ".jar"

				if (ver.matches("[\\d\\.]+")) {
					// check the version
					Object[] pp = map.get(name); // p[0] = version, p[1] = file
					if (pp == null) {
						map.put(name, new Object[] { ver, f });
					} else {

						if (ver.compareTo((String) pp[0]) > 0) {
							/**
							 * delete old file
							 */
							OpLog.warn("upgrade", X.EMPTY,
									"delete duplicated jar file, but low version:"
											+ ((File) pp[1]).getName()
											+ ", keep: " + f.getName());

							log.warn("delete duplicated jar file, but low version:"
									+ ((File) pp[1]).getName()
									+ ", keep: "
									+ f.getName());
							((File) pp[1]).delete();
							changed = true;
							map.put(name, new Object[] { ver, f });

						} else {
							// System.out.println("yes, " + ver + "<" + pp[0]);
							/**
							 * delete old version
							 */
							OpLog.warn("upgrade", X.EMPTY,
									"delete duplicated jar file, but low version:"
											+ f.getName() + ", keep: "
											+ ((File) pp[1]).getName());

							log.warn("delete duplicated jar file, but low version:"
									+ f.getName()
									+ ", keep: "
									+ ((File) pp[1]).getName());
							
							f.delete();
							changed = true;
							// map.put(name, new Object[] { ver, f });

						}
					}
				} else {
					System.out.println("ignore [" + ver + "] " + f.getName());
				}
			} else {
				// ignore the file if no version
			}
		}

		return changed;
	}

	/**
	 * @deprecated
	 * @param args
	 */
	public static void main(String[] args) {
		DefaultListener d = new DefaultListener();
		File f = new File("/home/joe/d/workspace/");
		Map<String, Object[]> map = new HashMap<String, Object[]>();
		d.cleanup(f, map);
		System.out.println(map);

	}
}
