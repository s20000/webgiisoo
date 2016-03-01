/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.*;
import com.giisoo.framework.web.*;

/**
 * web api: /admin/role
 * <p>
 * used toe manage role
 * 
 * @author joe
 *
 */
public class role extends Model {

    /**
     * Adds the.
     */
    @Path(path = "add", login = true, access = "access.user.admin")
    public void add() {
        if (method.isPost()) {

            String name = this.getString("name");
            String memo = this.getString("memo");
            long id = Role.create(name, memo);
            if (id > 0) {
                String[] access = this.getStrings("access");
                for (String s : access) {
                    Role.setAccess(id, s);
                }

                this.set(X.MESSAGE, lang.get("save.success"));
            } else {
                this.set("name", name);
                this.set(X.ERROR, lang.get("save.failed"));
            }

            onGet();
            return;
        }

        Map<String, List<Access>> bs = Access.load();
        this.set("accesses", bs);

        this.show("/admin/role.add.html");
    }

    /**
     * Verify.
     */
    @Path(path = "verify", login = true, access = "access.user.admin")
    public void verify() {
        String name = this.getString("name");
        String value = this.getString("value");

        JSONObject jo = new JSONObject();
        if (X.isEmpty(value)) {
            jo.put(X.STATE, 201);
            jo.put(X.MESSAGE, lang.get("name.empty.error"));
        } else {
            if (Role.loadByName(value) != null) {
                jo.put(X.STATE, 201);
                jo.put(X.MESSAGE, lang.get("name.exists.error"));
            } else {
                jo.put(X.STATE, 200);
            }
        }

        this.response(jo);
    }

    /**
     * Edits the.
     */
    @Path(path = "edit", login = true, access = "access.user.admin")
    public void edit() {
        if (method.isPost()) {

            long id = this.getLong("id");
            String name = this.getString("name");
            Role r = Role.loadById(id);
            if (r != null) {
                if (r.update(V.create("name", name).set("memo", this.getString("memo"))) > 0) {
                    this.path = null;
                    this.set(X.MESSAGE, lang.get("save.success"));

                    String[] accesses = this.getStrings("access");
                    r.setAccess(accesses);

                } else {
                    this.set("name", name);
                    this.set(X.ERROR, lang.get("save.failed"));
                }
            } else {
                this.set("name", name);
                this.set(X.ERROR, lang.get("save.failed"));
            }

            this.set(X.MESSAGE, lang.get("save.success"));

            onGet();
            return;

        } else {

            String ids = this.getString("id");
            if (ids != null) {
                String[] ss = ids.split(",");
                for (String s : ss) {
                    long id = Bean.toLong(s);
                    Role r = Role.loadById(id);
                    this.set("r", r);
                    JSONObject jo = new JSONObject();
                    r.toJSON(jo);
                    this.set(jo);

                    Map<String, List<Access>> bs = Access.load();
                    this.set("accesses", bs);

                    this.show("/admin/role.edit.html");
                    return;
                }
            }

            this.set(X.ERROR, lang.get("select.required"));

            onGet();
        }

    }

    /**
     * Delete.
     */
    @Path(path = "delete", login = true, access = "access.user.admin")
    public void delete() {
        String ids = this.getString("id");
        int updated = 0;
        if (ids != null) {
            String[] ss = ids.split(",");
            for (String s : ss) {
                long id = Bean.toLong(s);
                Role r = Role.loadById(id);
                int i = Role.delete(id);
                if (i > 0) {
                    updated += i;
                    OpLog.info(Role.class, "delete", r.getName(), null, login.getId(), this.getRemoteHost());
                }
            }
        }

        if (updated > 0) {
            this.set(X.MESSAGE, lang.get("delete.success"));
        } else {
            this.set(X.ERROR, lang.get("select.required"));
        }

        onGet();

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.Model#onGet()
     */
    @Path(login = true, access = "access.user.admin")
    public void onGet() {

        int s = this.getInt("s");
        int n = this.getInt("n", 10, "number.per.page");

        Beans<Role> bs = Role.load(s, n);
        this.set(bs, s, n);

        this.query.path("/admin/role");

        this.show("/admin/role.index.html");
    }

}
