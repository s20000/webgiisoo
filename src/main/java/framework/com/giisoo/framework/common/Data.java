package com.giisoo.framework.common;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.mongodb.BasicDBObject;

public class Data extends Bean {

    public String getId() {
        return this.getString(X._ID);
    }

    public static Beans<Data> load(String collection, BasicDBObject query, BasicDBObject order, int s, int n) {
        return Bean.load(collection, query, order, s, n, Data.class);
    }

    public static Data load(String collection, String id) {
        return Bean.load(collection, new BasicDBObject().append(X._ID, id), Data.class);
    }

    public static void update(String collection, JSONObject jo) {
        V v = V.create();

        String id = jo.getString(X._ID);
        jo.remove(X._ID);

        for (Object name : jo.keySet()) {
            v.set(name.toString(), jo.get(name));
        }

        if (Bean.load(collection, new BasicDBObject(X._ID, id)) == null) {
            // new , insert
            Bean.insertCollection(collection, v.set(X._ID, id), null);
        } else {
            Bean.updateCollection(collection, id, v);
        }
    }

}
