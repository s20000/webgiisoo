/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import java.util.List;

import net.sf.json.*;

import com.giisoo.core.bean.X;
import com.giisoo.framework.common.*;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Require;

/**
 * Web接口：/folder, 用户目录
 * @deprecated
 * @author joe
 *
 */
public class folder extends Model {

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Override
	public void onGet() {
		if ("1".equals(this.getString("mdc"))) {
			mockMdc();
			onMDC();

			this.set("jsonstr", mockMdc.toString());
			this.show("/ajax/json.html");
		} else {
			onPost();
		}
	}

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onMDC()
	 */
	@Override
	@Require(login = false)
	public void onMDC() {
		String method = this.path;
		if ("get".equals(method)) {
			int id = this.getInt("parent");
			String name = this.getString("name");

			Folder f = Folder.load(id, name);
			if (f != null) {
				List<Folder> list = Folder.subfolder(f.getId());
				JSONArray arr = new JSONArray();
				for (Folder f1 : list) {
					JSONObject jo = new JSONObject();
					jo.put(X.ID, f1.getId());
					jo.put(X.NAME, f1.getName());
					jo.put(X.TITLE, f1.getTitle());
					arr.add(jo);
				}
				this.set(X.RESULT, arr);
				this.set(X.STATE, X.OK);
			} else {
				this.set(X.STATE, X.FAIL);
				this.set(X.MESSAGE, "not found the [" + name
						+ "] under the parent[" + id + "]");
			}

		} else {
			this.set(X.STATE, X.FAIL);
			this.set(X.MESSAGE, "unknown method");
		}
	}

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onPost()
	 */
	public void onPost() {

		User me = this.getUser();

		String method = this.path;
		if ("popup".equals(method)) {
			List<Folder> list = Folder.subfolder(0);
			this.set("list", list);
			this.show("/admin/folder.popup.html");
			return;
		}

		int parent = this.getInt("parent");

		List<Folder> list = Folder.subfolder(parent);

		/**
		 * filter out all the folder that the user has not access for it
		 */
		Folder.filterAccess(list, me);

		/**
		 * get the length of the list
		 */
		int len = list == null ? 0 : list.size();

		/**
		 * convert the list to json array
		 */
		JSONArray arr = new JSONArray();
		for (int i = 0; i < len; i++) {
			JSONObject jo = new JSONObject();
			Folder f = list.get(i);

			/**
			 * set the text width language
			 */
			jo.put("name", f.getName());
			jo.put("id", f.getId());
			jo.put("title", f.getTitle());
			jo.put("tag", f.getTag());
			jo.put("hot", f.getHot());
			jo.put("recommend", f.getRecommend());
			jo.put("content", f.getContent());
			jo.put("seq", f.getSeq());
			jo.put("access", f.getAccess());

			arr.add(jo);
		}

		this.setContentType(Model.MIME_JSON);
		this.put("jsonstr", arr.toString());
		this.show("ajax/json.html");
	}

}
