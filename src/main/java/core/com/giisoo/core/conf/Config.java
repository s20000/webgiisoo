/**
 * Copyright (C) 2010 Gifox Networks
 * 
 * @project mms
 * @author jjiang
 * @date 2010-10-23
 */
package com.giisoo.core.conf;

import java.io.File;
import java.util.*;

import org.apache.commons.configuration.*;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.PropertyConfigurator;

/**
 * The Class Config.
 */
public class Config {

	/** The conf. */
	private static PropertiesConfiguration conf;

	/** The home. */
	private static String home;

	/** The conf name. */
	private static String confName;

	/**
	 * Inits the.
	 * 
	 * @param homePropName
	 *            the home prop name
	 * @param confName
	 *            the conf name
	 * @throws Exception
	 *             the exception
	 */
	public static void init(String homePropName, String confName)
			throws Exception {

		Config.confName = confName;

		home = System.getProperty(homePropName);
		if (home == null) {
			home = System.getenv(homePropName);
		}

		if (home == null)
			throw new Exception(homePropName + " does not exist.");

		if (new File(home + "/conf/log4j.properties").exists()) {
			PropertyConfigurator.configure(home + "/conf/log4j.properties");
		} else {
			Properties prop = new Properties();
			prop.setProperty("log4j.rootLogger", "error, stdout");
			prop.setProperty("log4j.appender.stdout",
					"org.apache.log4j.ConsoleAppender");
			prop.setProperty("log4j.appender.stdout.layout",
					"org.apache.log4j.PatternLayout");
			prop.setProperty("log4j.logger.com.giisoo", "info");

			PropertyConfigurator.configure(prop);
		}

		// String overridePath = new
		// File(home).getParentFile().getAbsolutePath();
		String overridePath = new File(home).getAbsoluteFile().getParent(); // fixed
																			// if
																			// the
																			// home='.',
																			// getParentFile
																			// returns
																			// null
		if (overridePath != null) {
			if (new File(overridePath + "/log4j_overrides.properties").exists()) {
				PropertyConfigurator.configure(overridePath
						+ "/log4j.properties");
			}

			// System.out.println(homePropName+ " = " + home);

			if (new File(overridePath + "/" + confName
					+ "_overrides.properties").exists()) {
				System.out.println("found the override properties in: "
						+ overridePath);

				PropertiesConfiguration pconf = new PropertiesConfiguration(
						overridePath + "/" + confName + "_overrides.properties");
				pconf.setReloadingStrategy(new FileChangedReloadingStrategy());

				conf = pconf;
			}

		}

		FileReloader reloader = new FileReloader();

		PropertiesConfiguration c1 = null;
		String file = home + File.pathSeparator + "conf" + File.pathSeparator
				+ confName + ".properties";
		if (new File(file).exists()) {
			c1 = new PropertiesConfiguration(file);
			c1.setEncoding("utf-8");
			reloader.add(file);

			System.out.println("load config: " + file);
		} else {
			System.out.println(file + " no found!");
		}

		if (c1 != null) {
			if (conf == null) {
				conf = c1;
			} else {
				conf.append(c1);
			}
		}

		if (conf == null) {
			conf = new PropertiesConfiguration();
		}

		conf.addProperty("home", home);
		reloader.setConf(conf).start();

		List<String> list = conf.getList("@include");
		Set<String> ss = new HashSet<String>();
		ss.addAll(list);
		// System.out.println("include:" + ss);

		for (String s : ss) {
			if (s.startsWith(File.separator)) {
				if (new File(s).exists()) {
					PropertiesConfiguration c = new PropertiesConfiguration(s);
					c.setEncoding("utf-8");
					reloader.add(s);

					conf.append(c);
				} else {
					System.out
							.println("Can't find the configuration file, file="
									+ s);
				}
			} else {
				String s1 = home + "/conf/" + s;
				if (new File(s1).exists()) {
					PropertiesConfiguration c = new PropertiesConfiguration(s1);
					c.setEncoding("utf-8");
					reloader.add(s1);

					conf.append(c);
				} else {
					System.out
							.println("Can't find the configuration file, file="
									+ s1);
				}

			}
		}

		/**
		 * set some default value
		 */
		if (!conf.containsKey("site.name")) {
			conf.setProperty("site.name", "default");
		}

	}

	/**
	 * Gets the config.
	 * 
	 * @return the config
	 */
	public static Configuration getConfig() {
		return conf;
	}

	/**
	 * set the configuration back to the file.
	 */
	public static void save() {

		if (conf != null) {
			String file = home + "/conf/" + confName + ".properties";

			try {
				conf.save(file);
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * The Class FileReloader.
	 */
	static class FileReloader {

		/** The _conf. */
		PropertiesConfiguration _conf;

		/** The last. */
		Map<String, Long> last = new HashMap<String, Long>();

		/**
		 * Instantiates a new file reloader.
		 */
		public FileReloader() {
		}

		/**
		 * Sets the conf.
		 * 
		 * @param conf
		 *            the conf
		 * @return the file reloader
		 */
		public FileReloader setConf(PropertiesConfiguration conf) {
			this._conf = conf;
			return this;
		}

		/**
		 * Adds the.
		 * 
		 * @param file
		 *            the file
		 * @return the file reloader
		 */
		public FileReloader add(String file) {
			// System.out.println("add:" + file);
			if (!last.containsKey(file)) {
				synchronized (last) {
					last.put(file, 0L);
				}
			}
			return this;
		}

		/**
		 * Start.
		 */
		public void start() {
			new Thread() {
				public void run() {
					while (true) {
						try {
							synchronized (last) {
								boolean changed = false;
								for (String file : last.keySet()) {
									File f = new File(file);
									if (f.exists()) {
										long l = f.lastModified();
										// System.out.println(file + ":" + l);
										long l0 = last.get(file);
										if (l0 > 0 && l != l0) {
											changed = true;
										}
										last.put(file, l);
									} else {
										System.out.println(file + ":not found");
									}
								}

								if (changed) {
									for (String file : last.keySet()) {
										PropertiesConfiguration c = new PropertiesConfiguration(
												file);
										c.setEncoding("utf-8");
										Iterator<String> it = c.getKeys();
										while (it.hasNext()) {
											String k = it.next();
											Object o = c.getProperty(k);
											_conf.setProperty(k, o);
										}
									}
								}
							}

							List<String> list = _conf.getList("@include");
							Set<String> ss = new HashSet<String>();
							ss.addAll(list);
							// System.out.println("include:" + list);
							for (String s : ss) {
								if (s.startsWith(File.separator)) {
									if (new File(s).exists()) {
										FileReloader.this.add(s);
									} else {
										System.out
												.println("Can't find the configuration file, file="
														+ s);
									}
								} else {
									String s1 = home + "/conf/" + s;
									if (new File(s1).exists()) {
										FileReloader.this.add(s1);
									} else {
										System.out
												.println("Can't find the configuration file, file="
														+ s1);
									}
								}
							}

							Thread.sleep(10000);
						} catch (Exception e) {

						}
					}
				}
			}.start();
		}
	}

}
