package com.giisoo.app.web.admin;

import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.AccessLog;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;
import com.mongodb.BasicDBObject;

public class accesslog extends Model {

    @Path(login = true, access = "acess.config.admin")
    public void onGet() {
        String uri = this.getString("guri");
        String ip = this.getString("ip");

        BasicDBObject q = new BasicDBObject();
        if (!X.isEmpty(uri)) {
            q.append("url", uri);
            this.set("guri", uri);
        }
        if (!X.isEmpty(ip)) {
            q.append("ip", ip);
            this.set("ip", ip);
        }
        int s = this.getInt("s");
        int n = this.getInt("n", 10, "number.per.page");

        Beans<AccessLog> bs = AccessLog.load(q, new BasicDBObject().append("created", -1), s, n);

        this.set(bs, s, n);

        this.show("/admin/accesslog.index.html");
    }

    @Path(path = "deleteall", login = true, access = "acess.config.admin")
    public void deleteall() {
        AccessLog.deleteAll();
    }

}
