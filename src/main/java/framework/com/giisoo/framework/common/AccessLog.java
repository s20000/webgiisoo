/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.UID;

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

//    String id;
//    String ip;
//    String method;
//    String agent;
//    String url;
//    String sid;
//    String header;
//    String query;
//    long created;
//    long cost;
//    String handler;

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
        try {
            long created = System.currentTimeMillis();
            String id = UID.id(ip, url, created);

            Bean.insert(v.set("id", id).set("ip", ip).set("url", url).set("created", created), AccessLog.class);
        } catch (Exception e) {
            // log.error(url, e);
            // ignore
        }

    }
}
