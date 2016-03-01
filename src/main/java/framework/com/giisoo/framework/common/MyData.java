package com.giisoo.framework.common;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;
import com.mongodb.BasicDBObject;

/**
 * MyData, used to record user customer data, please refer model ("/mydata")
 * 
 * @author joe
 *
 */
@DBMapping(collection = "gi_mydata")
public class MyData extends Bean {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public long getId() {
        return this.getLong(X._ID);
    }

    public long getUid() {
        return this.getLong("uid");
    }

    public String getTable() {
        return this.getString("table");
    }

    public static long create(long uid, String table, V v) {
        long id = UID.next("userdata.id");

        while (Bean.exists(new BasicDBObject(X._ID, id), MyData.class)) {
            id = UID.next("userdata.id");
        }
        Bean.insertCollection(v.set(X._ID, id, true).set("id", id, true).set("uid", uid, true).set("table", table, true), MyData.class);
        return id;
    }

    public static int update(long id, long uid, V v) {
        return Bean.updateCollection(new BasicDBObject(X._ID, id).append("uid", uid), v, MyData.class);
    }

    public static int remove(long id, long uid) {
        return Bean.delete(new BasicDBObject(X._ID, id).append("uid", uid), MyData.class);
    }

    public static MyData load(long id, long uid) {
        return Bean.load(new BasicDBObject(X._ID, id).append("uid", uid), MyData.class);
    }

    public static Beans<MyData> load(long uid, String table, BasicDBObject q, BasicDBObject order, int s, int n) {
        if (q == null) {
            q = new BasicDBObject();
        }
        return Bean.load(q.append("uid", uid).append("table", table), order, s, n, MyData.class);
    }

}
