/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.Bean.W;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.App;
import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;
import com.mongodb.BasicDBObject;

public class app extends Model {

    /**
     * History.
     */
    @Path(path = "history", login = true, access = "access.user.admin")
    public void history() {
        int s = this.getInt("s");
        int n = this.getInt("n", 10, "default.list.number");

        JSONObject jo = this.getJSON();
        // W w = W.create().copy(jo, W.OP_EQ, "op");
        // w.and("module", App.class.getName(), W.OP_EQ);
        BasicDBObject q = new BasicDBObject().append("module", App.class.getName());
        if (!X.isEmpty(jo.get("op"))) {
            q.append("op", jo.get("op"));
        }
        if (!X.isEmpty(jo.get("uid"))) {
            q.append("uid", Bean.toInt(jo.get("uid")));
        }
        if (!X.isEmpty(jo.get("type"))) {
            q.append("type", Bean.toInt(jo.get("type")));
        }
        if (!X.isEmpty(jo.get("ip"))) {
            q.append("ip", Pattern.compile(jo.getString("ip"), Pattern.CASE_INSENSITIVE));
        }

        this.set(jo);

        if (s > 0) {
            this.set("currentpage", 1);
        }

        Beans<OpLog> bs = OpLog.load(q, s, n);
        this.set(bs, s, n);

        this.show("/admin/app.history.html");

    }

    /**
     * Verify.
     */
    @Path(path = "verify", login = true, access = "access.admin")
    public void verify() {
        String name = this.getString("name");
        String value = this.getString("value");
        JSONObject jo = new JSONObject();
        if (X.isEmpty(value)) {
            jo.put(X.STATE, 201);
            jo.put(X.MESSAGE, lang.get("id.empty.error"));
        } else {
            if (App.load(value) != null) {
                jo.put(X.STATE, 201);
                jo.put(X.MESSAGE, lang.get("id.exists.error"));
            } else {
                jo.put(X.STATE, 200);
            }
        }

        this.response(jo);
    }

    /**
     * Adds the.
     */
    @Path(path = "add", login = true, access = "access.admin", log = Model.METHOD_POST)
    public void add() {
        if (method.isPost()) {
            JSONObject jo = this.getJSON();
            String appid = this.getString("appid");
            if (App.create(appid, V.create("appid", appid).copy(jo, "memo", "company", "contact", "phone", "logout", "email").set("setrule", this.getHtml("setrule")).set("getrule",
                    this.getHtml("getrule")).set("_key", UID.random(24))) > 0) {
                this.set(X.MESSAGE, lang.get("add.success"));

                onGet();

                return;
            } else {
                this.set(X.MESSAGE, lang.get("add.fail"));
                this.set(jo);
            }
        }

        this.show("/admin/app.add.html");
    }

    /**
     * Lock.
     */
    @Path(path = "lock", login = true, access = "access.admin")
    public void lock() {
        String appid = this.getString("appid");
        int updated = 0;
        if (appid != null) {
            String[] ss = appid.split(",");
            V v = V.create("locked", 1);
            for (String s : ss) {
                App a = App.load(s);
                if (a != null) {
                    int i = App.update(s, v);
                    if (i > 0) {
                        OpLog.log(App.class, "lock", a.getAppid(), null, login.getId(), this.getRemoteHost());
                        updated += i;
                    }
                }
            }
        }

        if (updated > 0) {
            this.set(X.MESSAGE, lang.get("edit.seccuss"));
        } else {
            this.set(X.MESSAGE, lang.get("select.required"));
        }

        onGet();
    }

    /**
     * Edits the.
     */
    @Path(path = "edit", login = true, access = "access.admin", log = Model.METHOD_POST)
    public void edit() {
        String appid = this.getString("appid");
        if (method.isPost()) {
            JSONObject jo = this.getJSON();
            V v = V.create().copy(jo, "memo", "_key", "company", "contact", "phone", "logout", "email").set("locked", "on".equals(this.getString("locked")) ? 1 : 0).set("setrule",
                    this.getHtml("setrule")).set("getrule", this.getHtml("getrule"));

            if (App.update(appid, v) > 0) {
                this.set(X.MESSAGE, lang.get("save.success"));

                onGet();

                return;
            } else {
                this.set(X.MESSAGE, lang.get("save.fail"));
                this.set(jo);
            }
        } else {
            if (appid != null) {
                String[] ss = appid.split(",");
                for (String s : ss) {
                    App a = App.load(s);
                    JSONObject jo = new JSONObject();
                    a.toJSON(jo);
                    this.set(jo);
                    break;
                }
            } else {
                this.set(X.MESSAGE, lang.get("select.required"));
                onGet();
                return;
            }
        }

        this.show("/admin/app.edit.html");

    }

    /**
     * Newkey.
     */
    @Path(path = "newkey", login = true, access = "access.admin")
    public void newkey() {
        this.println(UID.random(24));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.Model#onGet()
     */
    @Path(login = true, access = "access.admin")
    public void onGet() {
        JSONObject jo = this.getJSON();
        W w = getW(jo);
        int s = this.getInt("s");
        int n = this.getInt("n");

        Beans<App> bs = App.load(w, s, n);
        this.set(bs, s, n);

        show("/admin/app.index.html");
    }

    private W getW(JSONObject jo) {
        if ("add".equals(this.path) || "edit".equals(this.path) || "lock".equals(this.path))
            return null;

        return W.create().copy(jo, W.OP_LIKE, "appid", "company", "contact", "phone");
    }
}
