/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.Keypair;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Paging;

public class keypair extends Model {

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Override
	public void onGet() {
		String method = this.path;
		if ("add".equals(method)) {
			this.show("/admin/keypair.add.html");
			return;
		} else if ("edit".equals(method)) {
			long created = this.getLong("created");
			Keypair k = Keypair.load(created);
			JSONObject jo = new JSONObject();
			k.toJSON(jo);
			this.set(jo);
			this.show("/admin/keypair.edit.html");
			return;
		} else if ("detail".equals(method)) {
			long created = this.getLong("created");
			Keypair k = Keypair.load(created);
			JSONObject jo = new JSONObject();
			k.toJSON(jo);
			this.set(jo);
			this.show("/admin/keypair.detail.html");
			return;
		}

		int s = this.getInt("s");
		int n = this.getInt("n");
		Beans<Keypair> bs = Keypair.load(s, n);
		if (bs != null) {
			this.set("list", bs.getList());
			this.set("total", bs.getTotal());
			this.set("pages", Paging.create(bs.getTotal(), s, n));
		}

		this.show("/admin/keypair.index.html");
	}

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onPost()
	 */
	@Override
	public void onPost() {
		String method = this.path;
		if ("add".equals(method)) {
			int length = this.getInt("length");
			String memo = this.getString("memo");
			long created = Keypair.create(length, memo);
			if (created > 0) {
				Keypair k = Keypair.load(created);
				JSONObject jo = new JSONObject();
				k.toJSON(jo);
				this.set(jo);
				this.show("/admin/keypair.detail.html");
				return;
			} else {
				this.set(X.MESSAGE, lang.get("create_error"));
				this.set("length", length);
				this.set("memo", memo);
			}
		} else if ("edit".equals(method)) {
			long created = this.getLong("createc");
			String memo = this.getString("memo");

			if (Keypair.update(created, V.create("memo", memo)) > 0) {
				Keypair k = Keypair.load(created);
				JSONObject jo = new JSONObject();
				k.toJSON(jo);
				this.set(jo);
				this.show("/admin/keypair.detail.html");
				return;
			} else {
				this.set(X.MESSAGE, lang.get("edit_error"));
			}
		}

		onGet();
	}

}
