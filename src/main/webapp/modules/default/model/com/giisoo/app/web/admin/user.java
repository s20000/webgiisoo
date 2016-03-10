/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.*;
import com.giisoo.framework.web.*;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * web api: /admin/user
 * <p>
 * used to manage user
 * 
 * @author joe
 *
 */
public class user extends Model {

    /**
     * Adds the.
     */
    @Path(path = "add", login = true, access = "access.user.admin")
    public void add() {
        if (method.isPost()) {

            JSONObject jo = this.getJSON();
            String name = this.getString("name").trim();
            // String password = this.getString("password");
            try {

                /**
                 * create the user
                 */
                if (User.exists(new BasicDBObject("name", name))) {
                    /**
                     * exists, create failded
                     */
                    this.set(X.ERROR, lang.get("user.name.exists"));
                } else {

                    V v = V.create().copy(jo, "name", "password", "title", "nickname", "email", "phone").set("locked", 0);

                    long id = User.create(v);

                    /**
                     * set the role
                     */
                    String[] roles = this.getStrings("role");
                    log.debug("roles=" + Bean.toString(roles));

                    if (roles != null) {
                        User u = User.loadById(id);
                        List<Long> list = new ArrayList<Long>();
                        for (String s : roles) {
                            list.add(Bean.toLong(s));
                        }
                        u.setRoles(list);
                    }

                    /**
                     * log
                     */
                    OpLog.info(user.class, "user.add", this.getJSONNonPassword().toString(), null, login.getId(), this.getRemoteHost());

                    this.set(X.MESSAGE, lang.get("save.success"));

                    onGet();
                    return;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);

                this.set(X.ERROR, lang.get("save.failed"));

                this.set(jo);
            }

        }

        Beans<Role> bs = Role.load(0, 1000);
        if (bs != null) {
            this.set("roles", bs.getList());
        }

        this.show("/admin/user.add.html");
    }

    /**
     * History.
     */
    @Path(path = "history", login = true, access = "access.user.admin")
    public void history() {

        int s = this.getInt("s");
        int n = this.getInt("n", 10, "default.list.number");

        BasicDBObject q = new BasicDBObject().append("module", user.class.getName());

        this.set("cate", user.class.getName());

        this.set("currentpage", s);

        Beans<OpLog> bs = OpLog.load(q, s, n);
        this.set(bs, s, n);

        this.show("/admin/user.history.html");

    }

    /**
     * Delete.
     */
    @Path(path = "delete", login = true, access = "access.user.admin")
    public void delete() {

        JSONObject jo = new JSONObject();

        long id = this.getLong("id");
        if (id > 0) {
            User.delete(id);
            OpLog.warn(user.class, "user.delete", this.getJSONNonPassword().toString(), null, login.getId(), this.getRemoteHost());
            jo.put(X.STATE, 200);
        } else {
            jo.put(X.MESSAGE, "删除错误，请稍后重试！");
        }

        this.response(jo);

    }

    /**
     * Edits the.
     */
    @Path(path = "edit", login = true, access = "access.user.admin")
    public void edit() {
        long id = this.getLong("id");

        if (method.isPost()) {

            JSONObject j = this.getJSON();
            V v = V.create().copy(j, "nickname", "password", "title", "email", "phone").copyInt(j, "failtimes");
            if (!"on".equals(this.getString("locked"))) {
                /**
                 * clean all the locked info
                 */
                User.Lock.removed(id);
                v.set("locked", 0);
            } else {
                v.set("locked", 1);
            }

            String[] roles = this.getStrings("role");
            if (roles != null) {
                List<Long> list = new ArrayList<Long>();
                for (String s : roles) {
                    list.add(Bean.toLong(s));
                }
                v.set("roles", list);
            }
            User.update(id, v);

            OpLog.info(user.class, "user.edit", this.getJSONNonPassword().toString(), null, login.getId(), this.getRemoteHost());

            this.set(X.MESSAGE, lang.get("save.success"));

            onGet();

        } else {

            User u = User.loadById(id);
            if (u != null) {
                this.set(u.getJSON());
                this.set("u", u);

                Beans<Role> bs = Role.load(0, 1000);
                if (bs != null) {
                    this.set("roles", bs.getList());
                }

                this.set("id", id);
                this.show("/admin/user.edit.html");
                return;
            }

            this.set(X.ERROR, lang.get("select.required"));
            onGet();

        }
    }

    /**
     * Detail.
     */
    @Path(path = "detail", login = true, access = "access.user.query")
    public void detail() {
        String id = this.getString("id");
        if (id != null) {
            long i = Bean.toLong(id, -1);
            User u = User.loadById(i);
            this.set("u", u);

            Beans<Role> bs = Role.load(0, 100);
            if (bs != null) {
                this.set("roles", bs.getList());
            }

            this.show("/admin/user.detail.html");
        } else {
            onGet();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.Model#onGet()
     */
    @Override
    @Path(login = true, access = "access.user.admin")
    public void onGet() {

        String name = this.getString("name");
        BasicDBObject q = new BasicDBObject();
        if (X.isEmpty(this.path) && !X.isEmpty(name)) {
            BasicDBList list = new BasicDBList();
            Pattern pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);

            list.add(new BasicDBObject().append("name", pattern));
            list.add(new BasicDBObject().append("title", pattern));
            list.add(new BasicDBObject().append("nickname", pattern));
            q.append("$or", list);

            this.set("name", name);
        }

        int s = this.getInt("s");
        int n = this.getInt("n", 10, "number.per.page");

        Beans<User> bs = User.load(q, s, n);
        this.set(bs, s, n);

        this.query.path("/admin/user");

        this.show("/admin/user.index.html");
    }

}
