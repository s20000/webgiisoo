package com.giisoo.framework.common;

import java.util.HashMap;
import java.util.Map;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;
import com.mongodb.BasicDBObject;

@DBMapping(collection = "gi_cluster")
public class Cluster extends Bean {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String getId() {
        return this.getString(X._ID);
    }

    public String getNodeid() {
        return this.getString("nodeid");
    }

    // ---------------

    public static String update(String nodeid, String home, V v) {
        String id = UID.id(nodeid, home);

        if (Cluster.exists(new BasicDBObject().append(X._ID, id), Cluster.class)) {
            // update
            Bean.updateCollection(id, v.set("updated", System.currentTimeMillis()), Cluster.class);
        } else {
            // insert
            Bean.insertCollection(v.set(X._ID, id).set("nodeid", nodeid).set("home", home).set("created", System.currentTimeMillis()), Cluster.class);
        }

        return id;
    }

    public static Cluster load(String id) {
        return Bean.load(new BasicDBObject().append(X._ID, id), Cluster.class);
    }

    public static int remove(String id) {
        Cluster c = Cluster.load(id);
        if (id != null) {
            Bean.delete(new BasicDBObject().append("nodeid", c.getNodeid()), Counter.class);
            return Bean.delete(new BasicDBObject().append(X._ID, id), Cluster.class);
        }

        return 0;
    }

    public static Beans<Cluster> load(BasicDBObject query, BasicDBObject order, int s, int n) {
        return Bean.load(query, order, s, n, Cluster.class);
    }

    @DBMapping(collection = "gi_cluster_counter")
    public static class Counter extends Bean {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // {name: {type: count}}
        static Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
        static long time = 0;

        public static Map<String, Map<String, Integer>> getCounters() {
            return map;
        }

        public static int add(String name, String type, int count) {
            long now = System.currentTimeMillis() / X.AMINUTE * X.AMINUTE;
            Map<String, Map<String, Integer>> tmp = null;
            long old = 0;

            synchronized (Counter.class) {
                if (time == 0) {
                    time = now;
                } else if (now != time) {
                    old = time;
                    time = now;
                    tmp = map;
                    map = new HashMap<String, Map<String, Integer>>();
                }
            }

            if (tmp != null) {
                for (String name1 : tmp.keySet()) {
                    Map<String, Integer> map1 = tmp.get(name1);
                    for (String type1 : map1.keySet()) {
                        int c1 = map1.get(type1);
                        String id = UID.id(old, name1, type1);
                        Bean.insertCollection(V.create(X._ID, id).set("name", name).set("type", type).set("time", old).set("count", count), Counter.class);
                    }
                }
            }

            Map<String, Integer> m = map.get(name);
            if (m == null) {
                m = new HashMap<String, Integer>();
                map.put(name, m);
            }

            if (m.containsKey(type)) {
                m.put(type, count + m.get(type));
            } else {
                m.put(type, count);
            }
            return m.get(type);
        }

        // -------------------------

        public static String update(String nodeid, long time, V v) {
            String id = UID.id(nodeid, time);
            if (Bean.exists(new BasicDBObject().append(X._ID, id), Counter.class)) {
                // update
                Bean.updateCollection(id, v, Counter.class);
            } else {
                // insert
                Bean.insertCollection(v.set(X._ID, id).set("nodeid", nodeid).set("time", time), Counter.class);
            }
            return id;
        }

        public static Beans<Counter> load(BasicDBObject query, BasicDBObject order, int s, int n) {
            return Bean.load(query, order, s, n, Counter.class);
        }
    }

}
