package com.giisoo.app.web.admin;

import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.KeyField;
import com.giisoo.core.bean.X;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;
import com.mongodb.BasicDBObject;

public class keyfield extends Model {

    @Path(login = true, access = "access.config.admin")
    public void onGet() {

        int s = this.getInt("s");
        int n = this.getInt("n", 20);
        BasicDBObject q = new BasicDBObject();

        if (X.isEmpty(this.path)) {
            String collection = this.getString("collection");
            if (!X.isEmpty(collection)) {
                q.append("collection", collection);
            }
            String status = this.getString("status");
            if (!X.isEmpty(status)) {
                q.append("status", status);
            } else {
                q.append("status", new BasicDBObject("$ne", "done"));
            }

            this.set(this.getJSON());
        } else {
            q.append("status", new BasicDBObject("$ne", "done"));
        }

        Beans<KeyField> bs = KeyField.load(q, new BasicDBObject("collection", 1).append("q", 1), s, n);
        this.set(bs, s, n);

        this.query.path("/admin/keyfield");
        this.show("/admin/keyfield.index.html");
    }

    @Path(path = "run", login = true, access = "access.config.admin")
    public void run() {
        String id = this.getString("id");
        KeyField k = KeyField.load(id);
        if (k != null) {
            k.run();
            this.set(X.MESSAGE, "索引成功！");
        }

        onGet();
    }

    @Path(path = "deleteall", login = true, access = "access.config.admin")
    public void deleteall() {
        KeyField.deleteAll();
        onGet();
    }

    @Path(path = "delete", login = true, access = "access.config.admin")
    public void delete() {
        String id = this.getString("id");
        KeyField.delete(id);
        onGet();
    }

}
