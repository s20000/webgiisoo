/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.util.List;

import com.giisoo.core.bean.X;
import com.giisoo.framework.common.UrlMapping;
import com.giisoo.framework.web.*;

public class url extends Model {

  /* (non-Javadoc)
   * @see com.giisoo.framework.web.Model#onGet()
   */
  @Override
  @Require(login = true, access="access.admin")
  public void onGet() {
    String method = this.path;

    if ("add".equals(method)) {
      this.show("/admin/url.edit.html");
      return;

    } else if ("edit".equals(method)) {
      String url = this.getString("url");
      UrlMapping m = UrlMapping.load(url);
      this.set("m", m);
      this.show("/admin/url.edit.html");
      return;

    } else if ("delete".equals(method)) {
      String url = this.getString("url");

      UrlMapping.delete(url);

      this.set(X.MESSAGE, "message.delete.success");
    }

    List<UrlMapping> list = UrlMapping.loadAll();

    this.set("list", list);
    this.show("/admin/url.index.html");
  }

  /* (non-Javadoc)
   * @see com.giisoo.framework.web.Model#onPost()
   */
  @Override
  @Require(login = true, access="access.admin")
  public void onPost() {
    String method = this.path;
    if ("add".equals(method)) {
      String url = this.getString("url");
      String dest = this.getString("dest");
      int seq = this.getInt("seq");
      UrlMapping.create(url, dest, seq);

      this.set(X.MESSAGE, "message.add.success");
      this.path = null;
      onGet();

    } else if ("edit".equals(method)) {
      String url = this.getString("url");
      String dest = this.getString("dest");
      int seq = this.getInt("seq");
      UrlMapping.update(url, dest, seq);

      this.set(X.MESSAGE, "message.edit.success");
      this.path = null;
      onGet();

    }
  }

}
