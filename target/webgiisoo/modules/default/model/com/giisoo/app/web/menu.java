/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.json.*;

import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.*;
import com.giisoo.framework.web.*;

/**
 * Web接口： /menu
 * 
 * @author joe
 * 
 */
public class menu extends Model {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	@Override
	@Require(login = false)
	public void onGet() {
		onPost();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.framework.web.Model#onPost()
	 */
	@Require(login = false)
	public void onPost() {
		User me = this.getUser();

		int id = this.getInt("root");
		String name = this.getString("name");

		Beans<Menu> bs = null;
		Menu m = null;
		if (name != null) {
			/**
			 * load the menu by id and name
			 */
			m = Menu.load(id, name);

			if (m != null) {

				/**
				 * load the submenu of the menu
				 */
				bs = m.submenu();
			}
		} else {
			/**
			 * load the submenu by id
			 */
			bs = Menu.submenu(id);

		}
		List<Menu> list = bs == null ? null : bs.getList();

		/**
		 * filter out the item which no access
		 */
		Collection<Menu> ll = Menu.filterAccess(list, me);

		log.debug("load menu: id=" + id + ", size="
				+ (list == null ? 0 : list.size()) + ", filtered="
				+ (ll == null ? 0 : ll.size()));

		/**
		 * convert the list to json array
		 */
		JSONArray arr = new JSONArray();

		if (ll != null) {
			Iterator<Menu> it = ll.iterator();

			while (it.hasNext()) {
				JSONObject jo = new JSONObject();
				m = it.next();

				/**
				 * set the text width language
				 */
				jo.put("text", lang.get(m.getName()));
				jo.put("id", m.getId());
				if (!X.isEmpty(m.getClasses())) {
					jo.put("classes", m.getClasses());
				}

				if (!X.isEmpty(m.getStyle())) {
					jo.put("style", m.getStyle());
				}

				/**
				 * set the url
				 */
				if (!X.isEmpty(m.getUrl())) {
					jo.put("url", m.getUrl());
				}

				/**
				 * set children
				 */
				if (m.getChilds() > 0) {
					jo.put("hasChildren", true);
				}

				if (!X.isEmpty(m.getClick())) {
					jo.put("click", m.getClick());
				}

				if (!X.isEmpty(m.getContent())) {
					jo.put("content", m.getContent());
				}

				arr.add(jo);
			}
		}

		this.response(arr);
	}
}
