/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import com.giisoo.framework.web.*;

/**
 * Web接口：/hello, MDC创建链接后，调用该接口
 * @author joe
 *
 */
public class hello extends Model {

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onMDC()
	 */
	@Override
	@Require(login = false)
	public void onMDC() {

	}

	/**
	 * Test.
	 * 
	 * @param id
	 *            the id
	 * @param param
	 *            the param
	 */
	@Path(path = "test/(.*)/(.*)", method = Model.METHOD_GET, login = true)
	public void test(String id, String param) {
		this.println("id=" + id);
		this.println("params=" + param);
	}

}
