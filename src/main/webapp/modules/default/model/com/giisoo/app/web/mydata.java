package com.giisoo.app.web;

import java.util.List;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.giisoo.core.mq.MQ;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.MyData;
import com.giisoo.framework.mdc.TConn;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;
import com.mongodb.BasicDBObject;

public class mydata extends Model {

    @Path(path = "get", login = true)
    public void get() {
        String table = this.getString("table");
        String order = this.getString("order");

        BasicDBObject o = new BasicDBObject();
        if (!X.isEmpty(order)) {
            String[] ss = order.split(",");
            for (String s : ss) {
                String[] s1 = s.split(" ");
                if (s1.length > 1) {
                    o.append(s1[0], Bean.toInt(s1[1]));
                } else {
                    o.append(s1[0], 1);
                }
            }
        }

        JSONObject j = this.getJSON();
        for (String s : KEYWORDS) {
            j.remove(s);
        }
        BasicDBObject q = new BasicDBObject();
        for (Object name : j.keySet()) {
            String v = this.getHtml(name.toString());
            if (!X.isEmpty(v)) {
                q.append(name.toString().trim(), v);
            }
        }

        int s = this.getInt("s");
        int n = this.getInt("n", 10);

        Beans<MyData> bs = MyData.load(login.getId(), table, q, o, s, n);

        JSONObject jo = new JSONObject();
        if (bs != null) {
            jo.put("list", bs.getList());
            jo.put("total", bs.getTotal());
            jo.put("s", s);
            jo.put("n", n);
        }
        jo.put(X.STATE, 200);
        this.response(jo);
    }

    @Path(path = "delete", login = true)
    public void delete() {
        long id = this.getLong("id");
        JSONObject jo = new JSONObject();
        MyData.remove(id, login.getId());
        jo.put(X.STATE, 200);
        this.response(jo);
    }

    @Path(path = "set", login = true)
    public void set() {
        long id = this.getLong("id", -1);
        final JSONObject jo = new JSONObject();
        JSONObject j = this.getJSON();
        for (String s : KEYWORDS) {
            j.remove(s);
        }
        j.remove("id");
        V v = V.create();
        for (Object name : j.keySet()) {
            v.set(name.toString(), this.getHtml(name.toString()));
        }

        if (id > 0) {
            MyData.update(id, login.getId(), v.set("updated", System.currentTimeMillis()));
            jo.put("op", "update");
        } else {
            String table = this.getString("table");
            id = MyData.create(login.getId(), table, v.set("created", System.currentTimeMillis()).set("updated", System.currentTimeMillis()));
        }

        MyData d = MyData.load(id, login.getId());
        jo.put(X.STATE, 200);
        jo.put("data", d.getJSON());

        /**
         * broadcast to all except myself
         */
        final String clientid = this.getString("clientid");
        new WorkerTask() {

            @Override
            public void onExecute() {
                List<TConn> list = TConn.loadAll(login.getId());
                if (list != null && list.size() > 1) {
                    for (TConn c : list) {
                        if (!X.isSame(clientid, c.getClientid())) {
                            c.send(jo, null);
                        }
                    }
                }
                // MQ.send(seq, dest, to, message, bb, src, from, header);
            }

        }.schedule(0);

        this.response(jo);
    }

    static String[] KEYWORDS = { "table", "order", "s", "n", "uid" };
}
