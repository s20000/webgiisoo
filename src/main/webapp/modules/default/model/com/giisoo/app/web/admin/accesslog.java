package com.giisoo.app.web.admin;

import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.AccessLog;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;
import com.mongodb.BasicDBObject;

/**
 * web api: /admin/accesslog
 * <p>
 * used to access the "accesslog"
 * 
 * @author joe
 *
 */
public class accesslog extends Model {

    @Path(login = true, access = "acess.config.admin")
    public void onGet() {
        String uri = this.getString("guri");
        String ip = this.getString("ip");
        String gsid = this.getString("gsid");
        String sortby = this.getString("sortby");
        int sortby_type = this.getInt("sortby_type", -1);

        BasicDBObject q = new BasicDBObject();
        if (!X.isEmpty(uri)) {
            q.append("url", uri);
            this.set("guri", uri);
        }
        if (!X.isEmpty(ip)) {
            q.append("ip", ip);
            this.set("ip", ip);
        }
        if (!X.isEmpty(gsid)) {
            q.append("sid", gsid);
            this.set("gsid", gsid);
        }
        int s = this.getInt("s");
        int n = this.getInt("n", 10, "number.per.page");

        if (X.isEmpty(sortby)) {
            sortby = "created";
        }
        this.set("sortby", sortby);
        this.set("sortby_type", sortby_type);

        BasicDBObject order = new BasicDBObject(sortby, sortby_type);
        Beans<AccessLog> bs = AccessLog.load(q, order, s, n);

        this.set(bs, s, n);

        this.query.path("/admin/accesslog");
        this.show("/admin/accesslog.index.html");
    }

    @Path(path = "deleteall", login = true, access = "acess.config.admin")
    public void deleteall() {
        AccessLog.deleteAll();
    }

}
