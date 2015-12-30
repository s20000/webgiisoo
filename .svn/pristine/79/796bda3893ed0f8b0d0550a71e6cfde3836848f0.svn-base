/*

 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import java.io.*;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.*;
import com.giisoo.framework.common.Repo.Entity;
import com.giisoo.framework.web.*;
import com.giisoo.utils.image.GImage;

/**
 * Web接口： /repo/[id]/[filename], /repo/download/[id]/[filename]
 * 
 * @author yjiang
 * 
 */
public class repo extends Model {

	/**
	 * Download.
	 */
	@Path(path = "download", login = true)
	public void download() {
		if (path != null) {
			String id = path;
			Entity e = null;
			// log.debug("e:" + e);

			User me = this.getUser();

			try {

				e = Repo.loadByUri(id);

				/**
				 * check the privilege via session, the app will put the access
				 * in session according to the app logic
				 */
				if (e != null) {
					if (e.isShared()
							|| (me != null && e.getUid() == me.getId())
							|| (Session.load(sid()).has("access.repo." + id))) {

						this.setContentType("application/octet-stream");
						this.addHeader("Content-Disposition",
								"attachment; filename=\"" + e.getName() + "\"");

						String date2 = lang.format(e.created,
								"yyyy-MM-dd HH:mm:ss z");

						/**
						 * if not point-transfer, then check the
						 * if-modified-since
						 */
						String range = this.getString("RANGE");
						if (X.isEmpty(range)) {
							String date = this.getHeader("If-Modified-Since");
							if (date != null && date.equals(date2)) {
								resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
								return;
							}
						}

						this.addHeader("Last-Modified", date2);

						try {

							String size = this.getString("size");
							if (size != null && size.indexOf("x") < 0) {
								size = lang.get("size." + size);
							}

							if (size != null) {
								String[] ss = size.split("x");

								if (ss.length == 2) {
									File f = Temp.get(id, size);
									if (!f.exists()) {
										f.getParentFile().mkdirs();

										File src = Temp.get(id, null);
										if (!src.exists()) {
											src.getParentFile().mkdirs();
										}
										OutputStream out = new FileOutputStream(
												src);
										Model.copy(e.getInputStream(), out,
												false);
										out.close();

										GImage.scale3(src.getAbsolutePath(),
												f.getAbsolutePath(),
												Bean.toInt(ss[0]),
												Bean.toInt(ss[1]));
									}

									if (f.exists()) {
										InputStream in = new FileInputStream(f);
										OutputStream out = this
												.getOutputStream();

										Model.copy(in, out, false);
										in.close();
										return;
									}
								}
							}

							OutputStream out = this.getOutputStream();
							InputStream in = e.getInputStream();

							long total = e.getTotal() <= 0 ? in.available() : e
									.getTotal();
							long start = 0;
							long end = total;
							if (range != null) {
								String[] ss = range.split("(=|-)");
								if (ss.length > 1) {
									start = Bean.toLong(ss[1]);
								}

								if (ss.length > 2) {
									end = Math.min(total, Bean.toLong(ss[2]));
								}
							}

							if (end <= start) {
								end = start + 16 * 1024;
							}

							if (method.isMdc()) {
								this.set("Content-Range", "bytes " + start
										+ "-" + end + "/" + total);
							} else {
								this.setHeader("Content-Range", "bytes "
										+ start + "-" + end + "/" + total);

							}

							log.info(start + "-" + end + "/" + total);
							Model.copy(in, out, start, end, true);

							return;
						} catch (IOException e1) {
							log.error(e1);
						}
					}
				}
			} finally {
				if (e != null) {
					e.close();
				}
			}
		}

		this.notfound();
	}

	/**
	 * Delete.
	 */
	@Path(path = "delete", login = true)
	public void delete() {

		this.setContentType(Model.MIME_JSON);
		JSONObject jo = new JSONObject();

		String repo = this.getString("repo");
		Entity e = Repo.loadByUri(repo);
		if (e != null) {
			if (e.getUid() == login.getId()
					|| login.hasAccess("access.repo.admin")) {
				e.delete();

				jo.put(X.STATE, 200);
				jo.put(X.MESSAGE, "ok");

			} else {
				jo.put(X.STATE, 201);
				jo.put(X.MESSAGE, "no access");
			}
		} else {
			jo.put(X.STATE, 201);
			jo.put(X.MESSAGE, "parameters error");
		}

		this.response(jo);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Override
	@Path(login = false)
	public void onGet() {
		if ("download".equals(this.getString("op"))) {
			download();
			return;
		}

		/**
		 * test session first
		 */

		// log.debug("path:" + path);

		if (path != null) {
			String id = path;
			Entity e = null;
			// log.debug("e:" + e);

			User me = this.getUser();

			try {

				e = Repo.loadByUri(id);

				/**
				 * check the privilege via session, the app will put the access
				 * in session according to the app logic
				 */
				if (e != null) {
					if (e.isShared()
							|| (me != null && e.getUid() == me.getId())
							|| (Session.load(sid()).has("access.repo." + id))) {

						this.setContentType(Model.getMimeType(e.name));

						String date2 = lang.format(e.created,
								"yyyy-MM-dd HH:mm:ss z");

						this.addHeader("Last-Modified", date2);

						try {

							String size = this.getString("size");

							/**
							 * if "size" presented, and has "x"
							 */
							if (!X.isEmpty(size)) {
								String[] ss = size.split("x");

								if (ss.length == 2) {
									File f = Temp.get(id, "s_" + size);

									if (!f.exists()) {
										f.getParentFile().mkdirs();

										File src = Temp.get(id, null);
										if (!src.exists()) {
											src.getParentFile().mkdirs();
										} else {
											src.delete();
										}
										OutputStream out = new FileOutputStream(
												src);
										Model.copy(e.getInputStream(), out,
												false);
										out.close();

										/**
										 * using scale3 to cut the middle of the
										 * image
										 */
										GImage.scale3(src.getAbsolutePath(),
												f.getAbsolutePath(),
												Bean.toInt(ss[0]),
												Bean.toInt(ss[1]));
									}

									if (f.exists()) {
										InputStream in = new FileInputStream(f);
										OutputStream out = this
												.getOutputStream();

										Model.copy(in, out, false);
										in.close();
										return;
									}
								}
							}

							/**
							 * if "size1" presented, has "x"
							 */
							size = this.getString("size1");

							if (!X.isEmpty(size)) {

								String[] ss = size.split("x");

								if (ss.length == 2) {
									File f = Temp.get(id, "s1_" + size);
									if (!f.exists()) {
										f.getParentFile().mkdirs();

										File src = Temp.get(id, null);
										if (!src.exists()) {
											src.getParentFile().mkdirs();
										} else {
											src.delete();
										}
										OutputStream out = new FileOutputStream(
												src);
										Model.copy(e.getInputStream(), out,
												false);
										out.close();

										/**
										 * using scale to smooth the original
										 * image
										 */
										GImage.scale(src.getAbsolutePath(),
												f.getAbsolutePath(),
												Bean.toInt(ss[0]),
												Bean.toInt(ss[1]));
									}

									if (f.exists()) {
										InputStream in = new FileInputStream(f);
										OutputStream out = this
												.getOutputStream();

										Model.copy(in, out, false);
										in.close();
										return;
									}
								}
							}

							/**
							 * if not point-transfer, then check the
							 * if-modified-since
							 */
							String range = this.getString("RANGE");
							if (X.isEmpty(range)) {
								String date = this
										.getHeader("If-Modified-Since");
								if (date != null && date.equals(date2)) {
									resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
									return;
								}
							}

							/**
							 * else get all repo output to response
							 */
							OutputStream out = this.getOutputStream();
							InputStream in = e.getInputStream();

							long total = e.getTotal() <= 0 ? in.available() : e
									.getTotal();
							long start = 0;
							long end = total;
							if (range != null) {
								String[] ss = range.split("(=|-)");
								if (ss.length > 1) {
									start = Bean.toLong(ss[1]);
								}

								if (ss.length > 2) {
									end = Math.min(total, Bean.toLong(ss[2]));
								}
							}

							if (end <= start) {
								end = start + 16 * 1024;
							}

							if (method.isMdc()) {
								this.set("Content-Range", "bytes " + start
										+ "-" + end + "/" + total);
							} else {
								this.setHeader("Content-Range", "bytes "
										+ start + "-" + end + "/" + total);
							}

							log.info(start + "-" + end + "/" + total);
							Model.copy(in, out, start, end, true);

							return;
						} catch (IOException e1) {
							log.error(e1);
						}
					}
				}
			} finally {
				if (e != null) {
					e.close();
				}
			}
		}

		this.notfound();

	}

}
