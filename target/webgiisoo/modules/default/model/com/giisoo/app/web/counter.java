/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import com.giisoo.framework.common.Counter;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;

/**
 * Web 接口: /counter/get/[name]; /counter/max/[name]; /counter <br/>
 * 
 * @author joe
 * 
 */
public class counter extends Model {

	/**
	 * Gets the.
	 * 
	 * @param name
	 *            the name
	 */
	@Path(path = "get/(.*)", method = Model.METHOD_GET)
	public void get(String name) {
		this.println(Counter.get(name));
	}

	/**
	 * Max.
	 * 
	 * @param name
	 *            the name
	 */
	@Path(path = "max/(.*)", method = Model.METHOD_GET)
	public void max(String name) {
		this.println(Counter.max(name));
	}

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Override
	public void onGet() {
		StringBuilder sb = new StringBuilder();
		Counter.toString(sb);
		this.println(sb);
	}

}
