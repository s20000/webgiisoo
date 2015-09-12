/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import java.io.*;

import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;

import com.giisoo.core.bean.Bean;

/**
 * default model for which model has not found
 * 
 * @author yjiang
 * 
 */
public class DummyModel extends Model {

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Override
	public void onGet() {
		onPost();
	}

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onPost()
	 */
	@Override
	public void onPost() {
		/**
		 * if the file exists, and the extension is not .html and htm then get
		 * back directly, and set contenttype
		 */
		// log.debug("uri=" + uri);

		File f = Module.home.loadResource(uri);
		if (f != null) {
			/**
			 * this file exists, check is end with ".htm|.html"
			 */
			if (uri.endsWith(".htm") || uri.endsWith(".html")) {
				/**
				 * parse it as template
				 */
				show(uri);
				return;
			} else if (f.isFile()) {
				/**
				 * copy the file directly
				 */
				// log.debug(f.getAbsolutePath());

				InputStream in = null;
				OutputStream out = null;
				try {
					in = new FileInputStream(f);
					out = resp.getOutputStream();
					this.setContentType(getMimeType(uri));

					String date = this.getHeader("If-Modified-Since");
					String date2 = lang.format(f.lastModified(),
							"yyyy-MM-dd HH:mm:ss z");
					if (date != null && date.equals(date2)) {
						resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
						return;
					}

					this.setHeader("Last-Modified", date2);
					this.setHeader("Content-Length", Long.toString(f.length()));
					this.setHeader("Accept-Ranges", "bytes");

					// RANGE: bytes=2000070-
					String range = this.getHeader("RANGE");
					long start = 0;
					long end = f.length();
					if (range != null) {
						String[] ss = range.split("=| |-");
						if (ss.length > 1) {
							start = Bean.toLong(ss[1]);
						}
						if (ss.length > 2) {
							end = Bean.toLong(ss[2]);
						}
						// Content-Range=bytes 2000070-106786027/106786028
						this.setHeader("Content-Range", "bytes " + start + "-"
								+ end + "/" + f.length());

					}

					Model.copy(in, out, start, end, false);
					out.flush();

					return;
				} catch (Exception e) {
					log.error(uri, e);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							log.error(e);
						}
					}
				}
			}
		}

		// check where has .html or htm
		Template t1 = getTemplate(uri + ".html", true);
		if (t1 != null) {
			String date = this.getHeader("If-Modified-Since");
			String date2 = lang.format(t1.getLastModified(),
					"yyyy-MM-dd HH:mm:ss z");
			if (date != null && date.equals(date2)) {
				resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}

			this.setHeader("Last-Modified", date2);

			show(uri + ".html");
			return;
		}

		// check where has .html or htm
		t1 = getTemplate(uri + ".htm", true);
		if (t1 != null) {
			String date = this.getHeader("If-Modified-Since");
			String date2 = lang.format(t1.getLastModified(),
					"yyyy-MM-dd HH:mm:ss z");
			if (date != null && date.equals(date2)) {
				resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}

			this.setHeader("Last-Modified", date2);

			show(uri + ".htm");
			return;
		}

		// not found
		this.notfound();

	}

}
