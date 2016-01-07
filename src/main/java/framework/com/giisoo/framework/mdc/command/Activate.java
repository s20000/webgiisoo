/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc.command;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.*;
import com.giisoo.framework.common.Cluster.Counter;
//import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.mdc.*;
import com.giisoo.utils.base.*;

/**
 * 
 * @author yjiang
 * 
 */
public class Activate extends Command {

    /**
   * 
   */
    private static final long serialVersionUID = 1L;

    public final byte COMMAND = ACTIVATE;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.giisoo.framework.mdc.command.Command#onResponse(com.giisoo.framework
     * .mdc.Request, com.giisoo.framework.mdc.TConn)
     */
    @Override
    public boolean onResponse(Request in, TConn d) throws Exception {
        String jsonstr = in.readString();
        JSONObject jo = JSONObject.fromObject(jsonstr);

        log.debug(jo.toString() + "-" + d.getRemoteIp());

        if (jo.has(X.STATE) && jo.has(X.CLIENTID) && X.OK_200 == jo.getInt(X.STATE)) {

            String clientid = jo.getString(X.CLIENTID);
            if (d.isSupportEncode()) {
                clientid = new String(RSA.decode(Base64.decode(clientid), TConn.pri_key));
            }
            d.onActivate(IResponse.STATE_OK, clientid, jo.getString(X.KEY));

        } else {
            d.onActivate(IResponse.STATE_FAIL, null, null);

        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.giisoo.framework.mdc.command.Command#onRequest(com.giisoo.framework
     * .mdc.Request, com.giisoo.framework.mdc.Response,
     * com.giisoo.framework.mdc.TConn)
     */
    @Override
    public boolean onRequest(Request req, Response resp, TConn d) {

        Counter.add("mdc", "activate", 1);

        String json = req.readString();
        try {
            JSONObject jo = JSONObject.fromObject(json);

            log.debug("recv:" + jo + "-" + d.getRemoteIp());

            /**
             * set the capability first for the connection
             */
            d.setCapability(jo.containsKey(X.CAPABILITY) ? jo.getInt(X.CAPABILITY) : 0);

            /**
             * get the client unique id and key if exists
             */
            String uid = jo.getString(X.UID);
            String key = jo.containsKey(X.KEY) ? jo.getString(X.KEY) : null;

            /**
             * do not set any attribute, the client will request login later, at
             * that time, the server side will load all attributes
             */
            jo = new JSONObject();

            /**
             * check the allowed uid
             */
            if (TConn.ALLOW_UID == null || "*".equals(TConn.ALLOW_UID) || uid.matches(TConn.ALLOW_UID)) {
                /**
                 * create a clientid, and make sure it's unique
                 */
                String clientid = UID.id(d.getRemoteIp(), uid);

                try {
                    /**
                     * test if exists, if so, create another one
                     */
                    int i = 0;
                    while (TConn.exists(clientid)) {
                        if (i > 10) {
                            log.warn("generate uid exceed 10 times, ip:" + d.getRemoteIp() + ", uid:" + uid);
                        }
                        clientid = UID.id(d.getRemoteIp(), uid, System.currentTimeMillis(), UID.random(10));
                        i++;
                    }

                    /**
                     * create the connection info
                     */
                    d.create(clientid, key, uid);

                    jo.put(X.STATE, X.OK_200);

                    if (d.isSupportEncode()) {
                        /**
                         * if client support encode, then encode the clientid
                         * using client pub key, and put the pub_key to client
                         * also
                         */
                        clientid = Base64.encode(RSA.encode(clientid.getBytes(), key));
                    }

                    jo.put(X.KEY, TConn.pub_key);
                    jo.put(X.CLIENTID, clientid);

                    // OpLog.log("mdc", "activate", "activate:" + clientid
                    // + ", uid:" + uid + ", key:" + key);

                } catch (Exception e) {
                    log.error("activate:" + clientid + ", uid:" + uid + ", key:" + key, e);

                    // OpLog.log("mdc", "activate", e.getMessage() + ", params:"
                    // + clientid + ", uid:" + uid + ", key:" + key);

                    jo.put(X.STATE, X.FAIL);
                }
            } else {
                jo.put(X.STATE, X.FAIL);
                jo.put(X.MESSAGE, "uid is not allowed");

                // OpLog.log("mdc", "activate", "forbidden the uid [" + uid
                // + "], allowed [" + TConn.ALLOW_UID);

            }

            /**
             * generate the response data
             */
            resp.writeString(jo.toString());
        } catch (Exception e) {
            log.error(json, e);
        }
        return true;
    }
}
