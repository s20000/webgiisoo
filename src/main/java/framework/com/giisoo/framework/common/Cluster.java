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

        static Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();

        public static Map<String, Map<String, Integer>> getCounters() {
            return map;
        }

        /**
         * To string.
         * 
         * @param sb
         *            the sb
         */
        // public static void toString(StringBuilder sb) {
        // sb.append("---------------------------------\r\n");
        // for (String name : counters.keySet()) {
        // sb.append(name).append("=").append(counters.get(name))
        // .append("\r\n");
        // }
        //
        // sb.append("---------------------------------\r\n");
        // for (String name : maxs.keySet()) {
        // sb.append(name).append("=").append(Bean.toString(maxs.get(name)))
        // .append("\r\n");
        // }
        // }

        /**
         * Reset.
         * 
         * @param name
         *            the name
         */
        // public static void reset(String name) {
        // counters.remove(name);
        // }

        /**
         * Gets the.
         * 
         * @param name
         *            the name
         * @return the int
         */
        // public static int get(String name) {
        // if (counters.containsKey(name)) {
        // return counters.get(name);
        // }
        //
        // return -1;
        // }

        /**
         * Adds the.
         * 
         * @param name
         *            the name
         * @param count
         *            the count
         * @return the int
         */
        // public synchronized static int add(String name, int count) {
        // if (counters.containsKey(name)) {
        // count = counters.get(name) + count;
        // }
        //
        // counters.put(name, count);
        // return count;
        // }

        /**
         * Max.
         * 
         * @param name
         *            the name
         * @param value
         *            the value
         * @param memo
         *            the memo
         * @return the long
         */
        // public static long max(String name, long value, String memo) {
        // Object[] o = maxs.get(name);
        // if (o == null) {
        // o = new Object[2];
        // } else {
        // long i = (Long) o[0];
        // if (i > value)
        // return i;
        // }
        // o[0] = value;
        // o[1] = memo;
        // maxs.put(name, o);
        //
        // return value;
        // }

        /**
         * Max.
         * 
         * @param name
         *            the name
         * @return the string
         */
        // public static String max(String name) {
        // Object[] o = maxs.get(name);
        // StringBuilder sb = new StringBuilder();
        // if (o != null) {
        // sb.append("max=").append(o[0]).append(",memo=").append(o[1]);
        // } else {
        // sb.append("max=,memo=");
        // }
        // return sb.toString();
        // }

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
