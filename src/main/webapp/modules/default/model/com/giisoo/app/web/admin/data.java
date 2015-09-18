package com.giisoo.app.web.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.Data;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;
import com.mongodb.BasicDBObject;

public class data extends Model {

    @Path(login = true, access = "access.config.admin")
    public void onGet() {

        String collection = this.getString("collection");
        String query = this.getHtml("_query");

        this.set("collection", collection);
        this.set("_query", query);

        BasicDBObject q = new BasicDBObject();
        if (!X.isEmpty(query)) {
            JSONObject jo = JSONObject.fromObject(query);
            for (Object name : jo.keySet()) {
                q.append(name.toString(), jo.get(name));
            }
        }

        int s = this.getInt("s");
        int n = this.getInt("n", 20, "number.per.page");

        Beans<Data> bs = Data.load(collection, q, new BasicDBObject().append(X._ID, 1), s, n);

        List<String> fields = new ArrayList<String>();
        if (bs != null && bs.getList() != null && bs.getList().size() > 0) {
            Data d = bs.getList().get(0);
            for (String name : d.keySet()) {
                if (!X._ID.equals(name)) {
                    Object o = d.get(name);
                    if (o instanceof List || o instanceof Map) {
                        continue;
                    }

                    if (fields.size() > 10) {
                        this.set("hasmore", 1);
                        break;
                    }
                    fields.add(name);
                }
            }
        }

        this.set("fields", fields);
        this.set(bs, s, n);
        this.show("/admin/data.index.html");

    }

    @Path(path = "detail", login = true, access = "access.config.admin")
    public void detail() {
        String collection = this.getString("collection");
        String id = this.getString("id");

        this.set("collection", collection);
        this.set("id", id);

        Data d = Data.load(collection, id);

        this.set("d", d);

        this.show("/admin/data.detail.html");
    }

    @Path(path = "update", login = true, access = "access.config.admin", log = Model.METHOD_POST)
    public void update() {
        String collection = this.getString("collection");
        String body = this.getHtml("body");

        this.set("collection", collection);
        this.set("body", body);

        if (method.isPost()) {
            if (!X.isEmpty(collection) && !X.isEmpty(body)) {
                Data.update(collection, JSONObject.fromObject(body));

                detail();

                return;
            }
        }

        this.show("/admin/data.update.html");
    }

}
