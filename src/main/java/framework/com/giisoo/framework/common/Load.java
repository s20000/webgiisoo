/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;
import com.mongodb.BasicDBObject;

@DBMapping(collection = "gi_load")
public class Load extends Bean {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // String name;
    // String node;
    // int count;
    // long updated;

    /**
     * Update.
     * 
     * @param name
     *            the name
     * @param node
     *            the node
     * @param count
     *            the count
     * @return the int
     */
    public static int update(String name, String node, int count) {

        String id = UID.id(name, node);
        if (Bean.exists(new BasicDBObject(X._ID, id), Load.class)) {
            return Bean.updateCollection(id, V.create("count", count).set("updated", System.currentTimeMillis()), Load.class);
        } else {
            return Bean.insertCollection(V.create(X._ID, id).set("count", count).set("updated", System.currentTimeMillis()).set("name", name).set("node", node), Load.class);
        }
    }

    public String getName() {
        return this.getString("name");
    }

    public String getNode() {
        return this.getString("node");
    }

    public int getCount() {
        return this.getInt("count");
    }

    public long getUpdated() {
        return this.getLong("updated");
    }

    /**
     * Last.
     * 
     * @param name
     *            the name
     * @return the load
     */
    public static Load last(String name) {
        return Bean.load(new BasicDBObject("name", name).append("updated", new BasicDBObject("$gt", System.currentTimeMillis() - 2 * X.AMINUTE)), new BasicDBObject("count", 1), Load.class);
    }

    /**
     * Top.
     * 
     * @param name
     *            the name
     * @return the load
     */
    public static Load top(String name) {
        return Bean.load(new BasicDBObject("name", name).append("updated", new BasicDBObject("$gt", System.currentTimeMillis() - 2 * X.AMINUTE)), new BasicDBObject("count", -1), Load.class);
    }

}
