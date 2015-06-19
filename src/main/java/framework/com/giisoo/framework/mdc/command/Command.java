/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc.command;

import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import net.sf.json.JSONObject;

import org.apache.commons.logging.*;

import com.giisoo.core.bean.X;
import com.giisoo.framework.mdc.*;
import com.giisoo.framework.web.Require;

/**
 * 
 * @author yjiang
 * 
 */
public abstract class Command implements Serializable {

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	static Log log = LogFactory.getLog(Command.class);

	/**
	 * using in the first time, to get clientid and pub_key for client
	 */
	public final static byte ACTIVATE = 0x01;

	/**
	 * after get clientid, to get session with key for each connecting.
	 */
	public final static byte HELLO = 0x02;

	/**
	 * release the connection
	 */
	public final static byte BYE = 0x03;

	/**
	 * @deprecated upload a raw, support resuming transfer
	 */
	public final static byte RAW = 0x04;

	/**
	 * app packet
	 */
	public final static byte APP = 0x05;

	/**
	 * empty operation, nothing todo
	 */
	public final static byte NOP = 0x06;

	public final static int STATE_OK = X.OK_200;
	public final static int STATE_FAIL = X.FAIL;

	/**
	 * Process.
	 * 
	 * @param b
	 *            the b
	 * @param d
	 *            the d
	 * @return true, if successful
	 */
	public static boolean process(byte[] b, TConn d) {
		// System.out.println(b.length);

		// log.debug("process: " + d);

		if (b != null && b.length > 0) {
			// byte cmd = b[0];
			byte cmd = (byte) (b[0] & 0x7F);
			boolean ack = (b[0] & 0x80) != 0;
			Command c = processor.get(cmd);

			// log.debug(b[0] + ", " + processor.size() + ", ack:" + ack +
			// ", cmd:" + cmd + ", command:" + c);
			try {
				if (c != null) {
					Request in = new Request(b, (short) 1);
					if (ack) {
						/**
						 * handle it as response
						 */
						c.onResponse(in, d);

					} else {

						Require require = c
								.getClass()
								.getMethod("onRequest", Request.class,
										Response.class, TConn.class)
								.getAnnotation(Require.class);

						if ((require == null || (!require.hello()) || d.valid())) {
							/**
							 * process it as request
							 */
							Response out = new Response();

							/**
							 * put response code first
							 */
							out.writeByte((byte) (cmd | 0x80));

							if (c.onRequest(in, out, d)) {

								/**
								 * response without decoded
								 */
								if (out.length() > 1) {
									d.send(out);
								}
							} else {
								// do nothing, the upon layer app is
								// Responsibility to send back
							}

							/**
							 * updated the last-updated time for the clientid
							 */
							d.update();
						} else {
							/**
							 * access deny
							 */
							log.warn("access deny - " + d.getRemoteIp()
									+ ", required [hello]");
							Response out = new Response();

							/**
							 * put response code first
							 */
							out.writeByte((byte) (cmd | 0x80));
							JSONObject jo = new JSONObject();
							jo.put(X.STATE, X.FAIL201); // required hello
							jo.put(X.MESSAGE, "access deny, required [hello]");
							out.writeString(jo.toString());
							d.send(out);

							log.debug("response: " + jo.toString());
						}
					}
					return true;
				} else {
					log.error("error command: " + cmd);
					d.send("error.command:" + cmd, null);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				d.send("exception." + e.getMessage(), null);
			}
		} else {
			d.send("request is empty", null);
		}

		return false;

	}

	static private boolean inited = false;

	/**
	 * Inits the.
	 */
	public synchronized static void init() {

		if (inited)
			return;

		inited = true;

		load("com.giisoo.framework.mdc.command");

	}

	/**
	 * Load.
	 * 
	 * @param packname
	 *            the packname
	 */
	public static void load(String packname) {

		// log.debug("loading command from: " + packname);

		/**
		 * get all the class in the package
		 */
		Set<Class<?>> list = getClasses(packname);

		if (list != null && list.size() > 0) {
			for (Class<?> c : list) {
				try {
					/**
					 * test the class is extended from Command ? and not the
					 * Command itself
					 */
					if (Command.class.isAssignableFrom(c)
							&& !Command.class.equals(c)) {
						Field f = c.getField("COMMAND");
						if (f != null) {
							/**
							 * get the command byte
							 */
							Command c1 = (Command) c.newInstance();
							byte cmd = f.getByte(c1);
							processor.put(cmd, c1);

							// log.info("found command [" + c.getName() + "]");
						} else {
							// log.debug("[" + c + "] has not 'COMMAND' field");
						}
					} else {
						// log.debug("[" + c + "] is not extend from Command");
					}
				} catch (Exception e) {
					// e.printStackTrace();
					log.error(e.getMessage(), e);
				}
			}
		} else {
			log.error("no command found!");
			// System.out.println("no command found!");
		}

	}

	/**
	 * Gets the classes.
	 * 
	 * @param pack
	 *            the pack
	 * @return the classes
	 */
	protected static Set<Class<?>> getClasses(String pack) {

		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		boolean recursive = true;
		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');

		// log.debug("package: " + packageDirName);

		Enumeration<URL> dirs;
		try {
			// dirs =
			// Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			dirs = Command.class.getClassLoader().getResources(packageDirName);
			while (dirs.hasMoreElements()) {
				URL url = dirs.nextElement();

				// log.debug("url:" + url);

				String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					findAndAddClassesInPackageByFile(packageName, filePath,
							recursive, classes);
				} else if ("jar".equals(protocol)) {
					JarFile jar;
					try {
						jar = ((JarURLConnection) url.openConnection())
								.getJarFile();
						Enumeration<JarEntry> entries = jar.entries();
						while (entries.hasMoreElements()) {
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							if (name.charAt(0) == '/') {
								name = name.substring(1);
							}
							if (name.startsWith(packageDirName)) {
								int idx = name.lastIndexOf('/');
								if (idx != -1) {
									packageName = name.substring(0, idx)
											.replace('/', '.');
								}
								if ((idx != -1) || recursive) {
									if (name.endsWith(".class")
											&& !entry.isDirectory()) {
										String className = name.substring(
												packageName.length() + 1,
												name.length() - 6);
										try {
											classes.add(Class
													.forName(packageName + '.'
															+ className));
										} catch (ClassNotFoundException e) {
											// e.printStackTrace();
										}
									}
								}
							}
						}
					} catch (IOException e) {
						// e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			// e.printStackTrace();
		}

		// log.debug("loading classes: " + classes);

		return classes;
	}

	/**
	 * find the package
	 * 
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param classes
	 */
	private static void findAndAddClassesInPackageByFile(String packageName,
			String packagePath, final boolean recursive, Set<Class<?>> classes) {
		File dir = new File(packagePath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		File[] dirfiles = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return (recursive && file.isDirectory())
						|| (file.getName().endsWith(".class"));
			}
		});
		for (File file : dirfiles) {
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(
						packageName + "." + file.getName(),
						file.getAbsolutePath(), recursive, classes);
			} else {
				String className = file.getName().substring(0,
						file.getName().length() - 6);
				try {
					classes.add(Thread.currentThread().getContextClassLoader()
							.loadClass(packageName + '.' + className));
				} catch (ClassNotFoundException e) {
					// e.printStackTrace();
				}
			}
		}
	}

	/**
	 * cache the command
	 */
	private static Map<Byte, Command> processor = new HashMap<Byte, Command>();

	/**
	 * On request.
	 * 
	 * @param in
	 *            the in
	 * @param out
	 *            the out
	 * @param d
	 *            the d
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	public boolean onRequest(Request in, Response out, TConn d)
			throws Exception {
		return false;
	}

	/**
	 * On response.
	 * 
	 * @param in
	 *            the in
	 * @param d
	 *            the d
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	public boolean onResponse(Request in, TConn d) throws Exception {
		return false;
	}

}
