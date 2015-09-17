package com.giisoo.app.web.admin;

import com.giisoo.core.bean.Beans;
import com.giisoo.framework.common.Cluster;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;
import com.mongodb.BasicDBObject;

public class cluster extends Model {

    @Override
    public void onGet() {
        
        BasicDBObject q = new BasicDBObject();

        int s = this.getInt("s");
        int n = this.getInt("n", 20, "number.per.page");

        Beans<Cluster> bs = Cluster.load(q, new BasicDBObject().append("node", 1), s, n);

        this.set(bs, s, n);

        this.show("/admin/cluster.index.html");
    }

    @Path(login = true, path = "delete")
    public void delete() {
        
        String id = this.getString("id");
        Cluster.remove(id);
        this.print("ok");
        
    }

    @Path(login = true, path = "traffic")
    public void traffic() {

        
        
    }

}
