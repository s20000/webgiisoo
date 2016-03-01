/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.common.User;
import com.giisoo.framework.web.*;

/**
 * web api: /admin/password
 * <p>
 * used to change password
 * 
 * @author joe
 *
 */
public class password extends Model {

    /**
     * Edits the.
     */
    @Path(path = "edit", login = true)
    public void edit() {
        if (method.isPost()) {
            String old = this.getString("old");
            String new1 = this.getString("new1");
            String new2 = this.getString("new1");

            if (new1 == null || "".equals(new1) || !new1.equals(new2)) {
                this.set(X.MESSAGE, lang.get("new_password_error"));
            } else {

                User me = this.getUser();
                User u = User.load(me.getString("name"));

                // TODO
                if (u == null || (X.isEmpty(old))) {
                    this.set(X.MESSAGE, lang.get("old_password_error"));
                } else {
                    u.update(V.create("password", new1));
                    this.setUser(u);

                    // Command.create("samba", "passwd",
                    // V.create("params", u.getName() + "," + new1));

                    OpLog.info(User.class, "password", null, "<a href='/admin/user/detail?id=" + u.getId() + "'>" + u.get("name") + "</a>", login.getId(), this.getRemoteHost());

                    this.set(X.MESSAGE, lang.get("edit.success"));
                }
            }
        }

        show("/admin/password.html");
    }

}
