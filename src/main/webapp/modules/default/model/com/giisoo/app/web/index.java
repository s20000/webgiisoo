/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import com.giisoo.core.bean.Beans;
import com.giisoo.framework.common.Menu;
import com.giisoo.framework.web.*;

/**
 * Web首页
 * 
 * @author joe
 * 
 */
public class index extends Model {

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Override
	public void onGet() {
		this.set("me", this.getUser());

		Menu m = Menu.load(0, "home");
		if (m != null) {
			Beans<Menu> bs = m.submenu();
			if (bs != null) {
				this.set("menu", bs.getList());
			}
		}

		this.show("/docs/index.html");
	}

}
