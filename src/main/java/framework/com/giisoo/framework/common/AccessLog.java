/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.Config;
import com.giisoo.core.worker.WorkerTask;
import com.mongodb.BasicDBObject;

/**
 * record the web access log
 * 
 * @author joe
 * 
 */
@DBMapping(collection = "gi_accesslog")
public class AccessLog extends Bean {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    static AtomicLong seq = new AtomicLong(0);
    static String node = Config.getConfig().getString("node");

    public String getUrl() {
        return this.getString("url");
    }

    /**
     * Creates the AccessLog.
     * 
     * @param ip
     *            the ip address
     * @param url
     *            the url
     * @param v
     *            the values
     */
    public static void create(final String ip, final String url, final V v) {
        new WorkerTask() {

            @Override
            public void onExecute() {
                long created = System.currentTimeMillis();
                String id = UID.id(ip, url, created, node, seq.incrementAndGet());
                Bean.insert(v.set(X._ID, id).set("ip", ip).set("url", url).set("created", created), AccessLog.class);
            }

        }.schedule(0);
    }

    public static Beans<AccessLog> load(BasicDBObject q, BasicDBObject order, int s, int n) {
        return Bean.load(q, order, s, n, AccessLog.class);
    }

    public static void cleanup() {
        Bean.delete(new BasicDBObject().append("created", new BasicDBObject().append("$lt", System.currentTimeMillis() - X.AMONTH)), AccessLog.class);
    }

    public static void deleteAll() {
        Bean.delete(new BasicDBObject(), AccessLog.class);
    }

    public static List<Object> distinct() {
        return Bean.distinct("url", new BasicDBObject("status", 200), AccessLog.class);
    }

}
