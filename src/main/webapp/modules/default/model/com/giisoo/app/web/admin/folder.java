/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.util.List;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.*;
import com.giisoo.framework.web.*;

public class folder extends Model {

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Override
	@Require(login = true, access = "access.folder.query")
	public void onGet() {

		String method = this.path;
		if ("add".equals(method)) {
			/**
			 * may add folder or article
			 */

			int parent = this.getInt("parent");
			this.put("parent", parent);
			/**
			 * add folder
			 */
			show("/admin/folder.add.html");

			return;
		} else if ("edit".equals(method)) {
			int id = this.getInt("id");

			/**
			 * edit a folder
			 */
			Folder f = Folder.load(id);
			JSONObject jo = new JSONObject();
			f.toJSON(jo);
			this.set(jo);
			show("/admin/folder.edit.html");

			return;
		} else if ("delete".equals(method)) {
			int id = this.getInt("id");
			Folder.remove(id);
			this.set(X.MESSAGE, lang.get("delete_success"));
		}

		/**
		 * get the root folder
		 */
		List<Folder> folders = Folder.subfolder(0);
		this.put("list", folders);

		show("/admin/folder.index.html");
	}

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onPost()
	 */
	@Override
	@Require(login = true, access = "access.folder.edit")
	public void onPost() {

		String method = this.path;

		if ("edit".equals(method)) {
			/**
			 * post a folder
			 */
			int id = this.getInt("id");

			/**
			 * modify a folder, get the data and update the folder
			 */
			String name = this.getString("name");
			String title = this.getString("title");
			int seq = this.getInt("seq");
			String tag = "default";
			String hot = this.getString("hot");
			String content = this.getString("content");
			String recommend = this.getString("recommend");

			Folder.update(
					id,
					V.create("name", name).set("title", title).set("seq", seq)
							.set("tag", tag).set("hot", hot)
							.set("content", content)
							.set("recommend", recommend));

			this.path = null;

			this.set("message", lang.get("message.add.success"));
			onGet();

			return;
		} else if ("add".equals(method)) {
			/**
			 * create a new folder, get the data and create the folder
			 */
			int parent = this.getInt("parent");
			String name = this.getString("name");
			String title = this.getString("title");
			int seq = this.getInt("seq");
			String tag = "default";
			String hot = this.getString("hot");
			String content = this.getString("content");
			String recommend = this.getString("recommend");
			String access = this.getString("access");

			Folder f = Folder.create(parent, name, tag, title, hot, recommend,
					content, seq, access);

			if (f != null) {
				this.set("message", lang.get("message.add.success"));
				/**
				 * let's onGet handle the remain action to show the index;
				 */
				this.path = null;
			} else {
				this.set("message", lang.get("message.add.fail"));
				this.set("parent", parent);
				this.set("name", name);
				this.set("title", title);
				this.set("seq", seq);
				this.set("hot", hot);
				this.set("content", content);
				this.set("recommend", recommend);
				this.set("access", access);

			}

			onGet();

			return;

		}

	}
}
