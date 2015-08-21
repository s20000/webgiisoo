/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.UID;
import com.giisoo.core.db.DB;
import com.giisoo.core.db.DB.IBackupCallback;
import com.giisoo.framework.common.Repo;
import com.giisoo.framework.common.Repo.Entity;
import com.giisoo.framework.common.Temp;
import com.giisoo.framework.common.User;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;

public class db extends Model implements IBackupCallback {

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Path(login = true, access = "access.config.admin")
	public void onGet() {

		int s = this.getInt("s");
		int n = this.getInt("n", 10, "default.list.number");

		Beans<Entity> bs = Repo.list("db.backup", s, n);
		this.set(bs, s, n);

		this.show("/admin/db.index.html");
	}

	/**
	 * Upload.
	 */
	@Path(path = "upload", login = true, access = "access.config.admin")
	public void upload() {
		show("/admin/db.upload.html");
	}

	/**
	 * Recover.
	 */
	@Path(path = "recover", login = true, access = "access.config.admin")
	public void recover() {
		progress = 0;
		String id = this.getString("id");
		Entity e = Repo.load(id);
		try {
			DB.recover(e.getInputStream(), this);
		} catch (IOException e1) {
			log.error(e1.getMessage(), e1);
		}
		this.show("/admin/db.recover.html");
	}

	/**
	 * Backup.
	 */
	@Path(path = "backup", login = true, access = "access.config.admin")
	public void backup() {
		progress = 0;

		DB.backup(
				Temp.get(UID.id(login.getId(), System.currentTimeMillis()),
						"backup.zip").getAbsolutePath(), this);

		this.show("/admin/db.backup.html");
	}

	/**
	 * Progress.
	 */
	@Path(path = "progress", login = true, access = "access.config.admin")
	public void progress() {
		JSONObject jo = new JSONObject();
		jo.put("progress", progress);
		this.response(jo);
	}

	/**
	 * Delete.
	 */
	@Path(path = "delete", login = true, access = "access.config.admin")
	public void delete() {
		String id = this.getString("id");
		Repo.delete(id);

		onGet();
	}

	static int progress = 0;

	/* (non-Javadoc)
	 * @see com.giisoo.db.DB.IBackupCallback#onProgress(java.lang.String, java.lang.String, int)
	 */
	public void onProgress(String filename, String op, int progress) {
		db.progress = progress;

		// log.debug("onProgress:" + progress);

		if (progress >= 100) {
			if ("backup".equals(op)) {
				User me = this.getUser();
				File f = new File(filename);

				try {
					Repo.store("backup",
							UID.id(filename, System.currentTimeMillis()),
							f.getName(), "db.backup", 0, f.length(),
							new FileInputStream(f), -1, false, (int)me.getId());

					f.delete();
				} catch (Exception e) {
					log.error(filename, e);
				}
			} else if ("recover".equals(op)) {
			}
		}
	}

}
