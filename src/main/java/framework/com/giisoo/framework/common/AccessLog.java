/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;
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

    /**
     * Creates the AccessLog.
     * 
     * @param ip
     *            the ip
     * @param url
     *            the url
     * @param v
     *            the v
     */
    public static void create(String ip, String url, V v) {
        long created = System.currentTimeMillis();
        String id = UID.id(ip, url, created);
        Bean.insert(v.set("id", id).set("ip", ip).set("url", url).set("created", created), AccessLog.class);
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

}
