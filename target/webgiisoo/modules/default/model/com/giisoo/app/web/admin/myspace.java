/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import com.giisoo.core.bean.Beans;
import com.giisoo.framework.common.Repo;
import com.giisoo.framework.common.Repo.Entity;
import com.giisoo.framework.common.User;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Paging;
import com.giisoo.framework.web.Require;

public class myspace extends Model {

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Override
	@Require(login = true)
	public void onGet() {
		User me = this.getUser();

		String method = this.path;
		if ("add".equals(method)) {
			this.show("/admin/myspace.add.html");
			return;
		} else if ("delete".equals(method)) {
			String id = this.getString("id");
			if (me.hasAccess("access.repo.admin")) {
				/**
				 * admin, delete it
				 */
				Repo.delete(id);
			} else {
				/**
				 * normal user, test the owner and delete it
				 */
				Repo.delete(id, me.getId());
			}
		}

		long uid = this.getLong("uid", me.getId());
		if (!me.hasAccess("access.repo.admin")) {
			uid = me.getId();
		}
		int offset = this.getInt("offset");
		int limit = this.getInt("limit", 10, "default.list.number");

		Beans<Entity> bs = Repo.list(uid, offset, limit);
		if (bs != null) {
			this.set("list", bs.getList());
			this.set("total", bs.getTotal());

			this.set("pages", Paging.create(bs.getTotal(), offset, limit));

		}
		this.show("/admin/myspace.html");
	}

}
