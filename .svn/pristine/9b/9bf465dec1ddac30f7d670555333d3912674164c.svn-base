/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.io.*;
import java.util.zip.*;

import com.giisoo.framework.common.*;
import com.giisoo.framework.common.Repo.Entity;
import com.giisoo.framework.web.*;

public class importupgrade extends Model {

	static String ROOT = "/tmp/import/";

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Override
	@Require(login = true, access = "access.admin")
	public void onGet() {
		String method = this.path;
		if ("add".equals(method)) {
			this.show("/admin/import.upgrade.add.html");

			return;
		} else if ("delete".equals(method)) {
			String name = this.getString("name");
			File f = new File(Model.HOME + ROOT + name);
			f.delete();
		} else if ("apply".equals(method)) {
			String name = this.getString("name");
			File f = new File(Model.HOME + ROOT + name);
			if (f.exists()) {
				ZipInputStream in = null;
				try {
					in = new ZipInputStream(new FileInputStream(f));
					ZipEntry z = in.getNextEntry();
					byte[] bb = new byte[4 * 1024];
					while (z != null) {
						f = new File(Model.HOME + z.getName());
						if (!f.exists()) {
							f.getParentFile().mkdirs();
						}
						OutputStream out = new FileOutputStream(f);
						int len = in.read(bb);
						while (len > 0) {
							out.write(bb, 0, len);
							len = in.read(bb);
						}
						out.close();
						z = in.getNextEntry();
					}

					this.set("message", lang.get("message.apply.success"));
				} catch (Exception e) {
					log.error(f.getAbsolutePath(), e);
					this.set("message", e.getMessage());
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							log.error(e);
						}
					}
				}
			} else {
				this.set(
						"message",
						"[" + f.getAbsolutePath() + "] "
								+ lang.get("message.notexists"));
			}
		}

		File f = new File(Model.HOME + ROOT);

		this.set("list", f.listFiles());
		this.show("/admin/import.upgrade.index.html");

	}

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onPost()
	 */
	@Override
	@Require(login = true, access = "access.admin")
	public void onPost() {
		String method = this.path;
		if ("add".equals(method)) {
			String url = this.getString("url");
			Entity e = Repo.loadByUri(url);
			if (e != null) {
				OutputStream out = null;
				try {
					File f = new File(Model.HOME + ROOT + e.name);
					if (!f.exists()) {
						f.getParentFile().mkdirs();
					}

					out = new FileOutputStream(f);
					copy(e.getInputStream(), out);

					/**
					 * delete the old file in repo
					 */
					e.delete();
				} catch (Exception e1) {
					log.error(e.toString(), e1);
					/**
					 * the file is bad, delete it
					 */
					e.delete();
				} finally {
					e.close();
					if (out != null) {
						try {
							out.close();
						} catch (IOException e1) {
							log.error(e1);
						}
					}
				}
			}
		}
	}

}
