/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.Alert;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Paging;
import com.giisoo.framework.web.Path;

/**
 * Web接口： /alert, /alert/delete, /alert/index
 * @author joe
 *
 */
public class alert extends Model {

	/**
	 * Delete.
	 */
	@Path(path = "delete", login = true, access = "access.admin", method = Model.METHOD_GET)
	public void delete() {

		String tag = this.getString("tag");
		String number = this.getString("number");
		if (Alert.delete(tag, number) > 0) {
			this.set(X.MESSAGE, lang.get("delete_success"));
		} else {
			this.set(X.MESSAGE, lang.get("delete_fail"));
		}
		index();
		return;

	}

	/**
	 * Index.
	 */
	@Path(path = "index", login = true, access = "access.admin", method = Model.METHOD_GET)
	public void index() {
		int s = this.getInt("s");
		int n = this.getInt("n");
		Beans<Alert> bs = Alert.load(Alert.STATE_ALL, s, n);
		if (bs != null && bs.getList() != null) {
			this.set("list", bs.getList());
			this.set("total", bs.getTotal());
			this.set("pages", Paging.create(bs.getTotal(), s, n));
		}

		this.show("/admin/alert.index.html");
	}

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onPost()
	 */
	@Override
	public void onPost() {
		String tag = this.getString("tag");
		String content = this.getString("content");

		if (tag != null && content != null) {
			Alert.insertOrUpdate(tag, "default", content);
		}

	}
}
