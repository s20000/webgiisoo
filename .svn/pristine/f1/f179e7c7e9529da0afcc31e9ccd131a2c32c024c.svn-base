/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import com.giisoo.core.bean.UID;
import com.giisoo.framework.web.Model;

/**
 * Web接口： /sequence, 获取一个自增值
 * @author joe
 *
 */
public class sequence extends Model {

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onPost()
	 */
	@Override
	public void onPost() {
		String name = this.getString("name");
		if (name != null) {
			long id = UID.next("seq." + name);
			println(id);
		}
	}

}
