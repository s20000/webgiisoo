package com.giisoo.framework.common;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.mongodb.BasicDBObject;

public class Data extends Bean {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String getId() {
        return this.getString(X._ID);
    }

    public static Beans<Data> load(String collection, BasicDBObject query, BasicDBObject order, int s, int n) {
        return Bean.load(collection, query, order, s, n, Data.class);
    }

    public static Data load(String collection, String id) {
        Data d = Bean.load(collection, new BasicDBObject(), Data.class);
        if (d != null) {
            Object _id = id;
            Object o = d.get(X._ID);
            if (o instanceof Long) {
                _id = Bean.toLong(id);
            }
            return Bean.load(collection, new BasicDBObject().append(X._ID, _id), Data.class);
        }
        return null;
    }

    public static int update(String collection, JSONObject jo) {
        V v = V.create();

        Object id = jo.get(X._ID);
        jo.remove(X._ID);

        for (Object name : jo.keySet()) {
            v.set(name.toString(), jo.get(name));
        }

        Data d = Bean.load(collection, new BasicDBObject(), Data.class);

        if (d != null) {

            if (Bean.load(collection, new BasicDBObject(X._ID, id)) == null) {
                // new , insert
                log.debug("inserted: " + v);
                return Bean.insertCollection(collection, v.set(X._ID, id), null);
            } else {
                return Bean.updateCollection(collection, id, v);
            }
        } else {
            log.debug("inserted: " + v);
            return Bean.insertCollection(collection, v.set(X._ID, id), null);
        }
    }

}
