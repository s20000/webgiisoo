/**
 * Copyright (C) 2010 Gifox Networks
 * 
 * @project mms
 * @author jjiang
 * @date 2010-10-23
 */
package com.giisoo.utils.startup;

import java.io.*;

import org.apache.commons.configuration.Configuration;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.cache.Cache;
import com.giisoo.core.conf.Config;
import com.giisoo.core.db.DB;
import com.giisoo.core.worker.*;

// TODO: Auto-generated Javadoc
/**
 * The Class Startup.
 */
public class Startup {

	/**
	 * The main method.
	 * 
	 * @param args
	 *          the arguments
	 */
	public void process(String[] args) {
		try {

			String self = Startup.class.getClassLoader().getResource("com/giisoo/startup/Startup.class").getPath();

			// System.out.println(self);

			if (self.startsWith("file:/")) {
				self = self.substring(6);
			}

			int i = self.indexOf(".jar!");
			if (i > 0) {
				self = self.substring(0, i);
				i = self.lastIndexOf("/");
				self = self.substring(0, i);
				i = self.lastIndexOf("/");
				self = self.substring(0, i);

			} else {
				i = self.indexOf("com/giisoo");
				self = self.substring(0, i);
				self = new File(self).getParentFile().getParentFile().getAbsolutePath();
			}

			System.out.println(self);

			/**
			 * test
			 */
			// self = "e:/doogoo";

			System.setProperty("home", self);

			Config.init("home", "giisoo");
			Config.getConfig().setProperty("home", self);
			if (args.length == 0 || "start".equals(args[0])) {
				startup();
			} else if ("stop".equals(args[0])) {
				System.out.println("args: " + args[0] + " " + (args.length > 1 ? args[1] : ""));

				_stop(args.length < 2 || !"achieve".equals(args[1]));
			} else if ("test".equals(args[0]) && args.length > 1) {
				test(args[1]);
			} else {
				printUsage();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints the usage.
	 */
	static void printUsage() {
		System.out.println("java Startup [start|stop|test url]");
	}

	/**
	 * Test.
	 *
	 * @param url the url
	 */
	protected void test(String url) {
	}

	/**
	 * _stop.
	 *
	 * @param fast the fast
	 */
	protected void _stop(boolean fast) {
		stop(fast);

		WorkerManager.stop(fast);
	}

	/**
	 * Stop.
	 *
	 * @param fast the fast
	 */
	protected void stop(boolean fast) {

	}

	/**
	 * Startup.
	 */
	protected void startup() {
		try {
			Thread.currentThread().setContextClassLoader(Startup.class.getClassLoader());

			Configuration conf = Config.getConfig();

			DB.init();
			Bean.init(conf);
			Cache.init(conf);

			beforeStart(conf);

			WorkerTask.init(conf.getInt("thread.number", 20), conf);

			IWorker[] workers = workers();
			if (workers != null) {
				for (IWorker s : workers) {
					WorkerManager.register(s);
				}
			}

			WorkerManager.init(conf);
			WorkerManager.start();

			afterStart(conf);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Workers.
	 *
	 * @return the i worker[]
	 */
	protected IWorker[] workers() {
		return null;
	}

	/**
	 * Before start.
	 *
	 * @param conf the conf
	 */
	protected void beforeStart(Configuration conf) {

	}

	/**
	 * After start.
	 *
	 * @param conf the conf
	 */
	protected void afterStart(Configuration conf) {

	}

}
