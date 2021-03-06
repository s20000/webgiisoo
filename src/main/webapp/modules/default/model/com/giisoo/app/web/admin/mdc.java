/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.Load;
import com.giisoo.framework.mdc.TConn;
import com.giisoo.framework.mdc.TConnCenter;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;
import com.mongodb.BasicDBObject;

/**
 * web api: /admin/mdc
 * <p>
 * used to manage mdc connection
 * 
 * @author joe
 *
 */
public class mdc extends Model {

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.Model#onGet()
     */
    @Override
    @Path(login = true, access = "access.config.admin")
    public void onGet() {
        int s = this.getInt("s");
        int n = this.getInt("n", 10, "number.per.page");
        Beans<TConn> bs = TConn.load(new BasicDBObject("updated", new BasicDBObject("$gt", System.currentTimeMillis() - 5 * X.AMINUTE)).append("uid", new BasicDBObject("$gte", TConn.UID_INVALID)), s,
                n);
        this.set(bs, s, n);

        this.query.path("/admin/mdc");

        this.show("/admin/mdc.index.html");

    }

    @Path(path = "all", login = true, access = "access.config.admin")
    public void all() {
        int s = this.getInt("s");
        int n = this.getInt("n", 10, "default.list.number");
        Beans<TConn> bs = TConn.load(new BasicDBObject(), s, n);
        this.set(bs, s, n);

        this.show("/admin/mdc.all.html");

    }

    @Path(path = "load", login = true, access = "access.config.admin")
    public void load() {
        int s = this.getInt("s");
        int n = this.getInt("n", 10, "number.per.page");
        Beans<Load> bs = Load.load("mdc", s, n);
        this.set(bs, s, n);

        this.show("/admin/mdc.load.html");

    }

    /**
     * Close.
     */
    @Path(path = "close", login = true, access = "access.config.admin")
    public void close() {
        String clientids = this.getString("clientid");
        int updated = 0;
        if (clientids != null) {
            String[] ss = clientids.split(",");
            for (String clientid : ss) {
                // MQ.close(clientid);
                TConn t = TConnCenter.get(clientid);
                if (t != null) {
                    t.close();
                }
                updated++;
            }
        }

        if (updated > 0) {
            this.set(X.MESSAGE, lang.get("close.success"));
        } else {
            this.set(X.MESSAGE, lang.get("select.required"));
        }

        onGet();
    }

}
