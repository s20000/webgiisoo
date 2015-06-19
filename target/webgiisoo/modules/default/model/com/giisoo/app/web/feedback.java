/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import org.json.simple.JSONObject;

import com.giisoo.bean.X;
import com.giisoo.framework.common.Feedback;
import com.giisoo.framework.web.*;

/**
 * @deprecated
 * @author joe
 * 
 */
public class feedback extends Model {

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Override
	@Require(login = false)
	public void onGet() {
		this.onPost();
	}

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onPost()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Require(login = false)
	public void onPost() {
		String content = this.getString("content");
		if (content != null && content.length() > 0) {
			Feedback.create(content, this.getRemoteHost());
		}

		JSONObject jo = new JSONObject();
		jo.put(X.STATE, X.OK);
		this.put("jsonstr", jo.toString());
		this.show("ajax/json.html");

	}

	@Override
	protected String getContentType() {
		return Model.MIME_JSON;
	}

}
