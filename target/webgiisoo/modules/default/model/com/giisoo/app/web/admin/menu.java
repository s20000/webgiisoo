/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Beans;
import com.giisoo.framework.common.*;
import com.giisoo.framework.web.*;

/**
 * setting menu
 * 
 * @author yjiang
 * 
 */
public class menu extends Model {

  /* (non-Javadoc)
   * @see com.giisoo.framework.web.Model#onGet()
   */
  @Override
  @Require(login = true, access = "access.menu")
  public void onGet() {

    String a = this.getString("a");
    if (a != null) {
      onPost();
    }

    Beans<Menu> bs = Menu.submenu(0);
    this.put("list", bs.getList());

    show("admin/menu.index.html");
  }

  /* (non-Javadoc)
   * @see com.giisoo.framework.web.Model#onPost()
   */
  @Override
  @Require(login = true, access = "access.menu")
  public void onPost() {
    JSONObject jo = new JSONObject();

    String a = this.getString("a");
    if ("delete".equals(a)) {
      int id = this.getInt("id");

      Menu.remove(id);
      jo.put("state", "ok");
    }

    this.setContentType(Model.MIME_JSON);

    this.put("jsonstr", jo.toString());

    show("ajax/json.html");

  }

}
