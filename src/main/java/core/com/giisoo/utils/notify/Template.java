package com.giisoo.utils.notify;

import java.io.*;
import java.util.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.*;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

// TODO: Auto-generated Javadoc
/**
 * The Class Template.
 */
public class Template {

	/** The log. */
	static Log log = LogFactory.getLog(Template.class);

	/** The subject. */
	StringBuffer subject = new StringBuffer();
	
	/** The body. */
	StringBuffer body = new StringBuffer();

	/** The _conf. */
	private static Configuration _conf;
	
	/** The cache. */
	private static TreeMap<String, Template> cache = new TreeMap<String, Template>();

	/**
	 * Subject.
	 *
	 * @param context the context
	 * @return the string
	 */
	public String subject(VelocityContext context) {
		try {
			StringWriter w = new StringWriter();
			Velocity.evaluate(context, w, "velocity", subject.toString());

			return w.toString().trim();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * Body.
	 *
	 * @param context the context
	 * @return the string
	 */
	public String body(VelocityContext context) {

		try {
			StringWriter w = new StringWriter();
			Velocity.evaluate(context, w, "velocity", body.toString());

			return w.toString().trim();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * Inits the.
	 *
	 * @param conf the conf
	 */
	public static void init(Configuration conf) {
		try {
			if(_conf != null) return;
			
			_conf = conf;
			
			Velocity.init();
			Email.init(conf);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Load.
	 *
	 * @param name the name
	 * @return the template
	 */
	public synchronized static Template load(String name) {
		Template t = cache.get(name);
		if (t == null) {
			t = _init(name);
		}

		return t;
	}

	/**
	 * _init.
	 *
	 * @param name the name
	 * @return the template
	 */
	private static Template _init(String name) {
		String home = _conf.getString("home");
		File f = new File(home + "/templates/" + name);

		if (f.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
				String line = null;
				Template t = new Template();
				cache.put(name, t);

				while ((line = reader.readLine()) != null) {
					t.append(line);
				}

				reader.close();

				return t;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

		} else {
			log.error("can not found the resource [" + f.getAbsolutePath() + "]");
		}

		return null;
	}

	/**
	 * Append.
	 *
	 * @param line the line
	 */
	private void append(String line) {
		if (line == null)
			return;

		int i = 0;
		if (state == 0) {
			i = line.indexOf("<subject>");
			if (i > -1) {
				state = 1;
				line = line.substring(i + 9);
			}
		}

		if (state == 1) {
			String s = line;
			i = line.indexOf("</subject>");
			if (i > -1) {
				s = line.substring(0, i);
				state = 0;
				line = line.substring(i + 10);
			}

			subject.append(s).append("\r\n");
		}

		if (state == 0) {
			i = line.indexOf("<body>");
			if (i > -1) {
				state = 2;
				line = line.substring(i + 6);
			}
		}

		if (state == 2) {
			i = line.indexOf("</body>");
			String s = line;
			if (i > -1) {
				s = line.substring(0, i);
				state = 0;
			}

			body.append(s).append("\r\n");
		}
	}

	/** The state. */
	int state = 0;

}
