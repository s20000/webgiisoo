/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.util.List;

import net.sf.json.JSONObject;

import com.giisoo.framework.utils.Host;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;

/**
 * 系统状态仪表盘
 */
public class gauge extends Model {

	public void onGet() {
		this.redirect("/user");
	}

	/**
	 * Cpu.
	 */
	@Path(path = "cpu", login = true, access = "access.config.admin")
	public void cpu() {
		this.show("/admin/gauge.cpu.html");
	}

	/**
	 * Cpu_status.
	 */
	@Path(path = "cpu/status", login = true, access = "access.config.admin")
	public void cpu_status() {
		// todo
		JSONObject jo = new JSONObject();
		jo.put("usage", Host.getCpuUsage());

		this.response(jo);

	}

	/**
	 * Mem_status.
	 */
	@Path(path = "mem/status", login = true, access = "access.config.admin")
	public void mem_status() {
		// todo
		JSONObject jo = new JSONObject();
		jo.put("used", Host.getMemUsed());

		this.response(jo);

	}

	/**
	 * Mem.
	 */
	@Path(path = "mem", login = true, access = "access.config.admin")
	public void mem() {
		this.set("total", Host.getMemTotal());
		this.show("/admin/gauge.mem.html");
	}

	/**
	 * Disk.
	 */
	@Path(path = "disk", login = true, access = "access.config.admin")
	public void disk() {
		this.set("list", Host.getDisks());
		this.show("/admin/gauge.disk.html");
	}

}
