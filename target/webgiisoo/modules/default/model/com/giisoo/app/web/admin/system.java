/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import net.sf.json.JSONObject;

import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.User;
import com.giisoo.framework.web.*;

public class system extends Model {

	/**
	 * Restart.
	 */
	@Path(path = "restart", login = true, access = "access.config.admin", log = Model.METHOD_POST)
	public void restart() {

		JSONObject jo = new JSONObject();
		User me = this.getUser();
		String pwd = this.getString("pwd");

		if (me.validate(pwd)) {
			jo.put("state", "ok");

			new WorkerTask() {

				@Override
				public String getName() {
					return "restart";
				}

				@Override
				public void onExecute() {
					System.exit(0);
				}

				@Override
				public void onFinish() {

				}

			}.schedule(1000);
		} else {
			jo.put("state", "fail");
			jo.put("message", lang.get("invalid.passwd"));
		}

		this.response(jo);
	}

}
