/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.*;
import com.giisoo.framework.common.Feedback;
import com.giisoo.framework.web.*;

public class feedback extends Model {

  /* (non-Javadoc)
   * @see com.giisoo.framework.web.Model#onGet()
   */
  @Override
  @Require(login = true, access = "access.feedback")
  public void onGet() {
    int s = this.getInt("s");
    int n = this.getInt("n", 10, "default.table.number");
    
    Beans<Feedback> bs = Feedback.load(s, n);

    if (bs != null) {
      this.put("list", bs.getList());
      this.put("pages", Paging.create(bs.getTotal(), s, n));
    }

    this.show("admin/feedback.html");

  }

  /* (non-Javadoc)
   * @see com.giisoo.framework.web.Model#onPost()
   */
  @Override
  @Require(login = true, access = "access.feedback")
  public void onPost() {
    String method = this.path;
    if ("delete".equals(method)) {
      int id = this.getInt("id");
      Feedback.delete(id);
    }

    this.setContentType(Model.MIME_JSON);
    JSONObject jo = new JSONObject();
    jo.put(X.STATE, X.OK);
    this.put("jsonstr", jo.toString());
    this.show("ajax/json.html");
  }
}
