package com.giisoo.framework.utils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.framework.utils.Http;
import com.giisoo.framework.utils.Http.Response;
import com.giisoo.utils.base.DES;

public class Publisher {

    static Log log = LogFactory.getLog(Publisher.class);

    public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36";

    public static int publish(Bean b) throws Exception {

        String url = SystemConfig.s("sync.url", null);
        String appid = SystemConfig.s("sync.appid", null);
        String appkey = SystemConfig.s("sync.appkey", null);
        if (!X.isEmpty(url) && !X.isEmpty(appid) && !X.isEmpty(appkey)) {

            // b.toJSON(jo);

            JSONObject jo = new JSONObject();
            b.toJSON(jo);

            /**
             * get the require annotation onGet
             */
            DBMapping mapping = (DBMapping) b.getClass().getAnnotation(DBMapping.class);
            if (mapping == null) {
                String collection = b.getString("collection");
                if (X.isEmpty(collection)) {
                    log.error("mapping missed in [" + b.getClass() + "] declaretion", new Exception("nothing but log"));
                    return 0;
                }
            } else {
                if (!X.isEmpty(mapping.collection())) {
                    jo.put("collection", mapping.collection());
                } else {
                    jo.put("table", mapping.table());
                }
            }

            return publish(jo);

        }

        return 0;
    }

    public static int publish(JSONObject jo) throws Exception {
        String url = SystemConfig.s("sync.url", null);
        String appid = SystemConfig.s("sync.appid", null);
        String appkey = SystemConfig.s("sync.appkey", null);
        if (!X.isEmpty(url) && !X.isEmpty(appid) && !X.isEmpty(appkey)) {

            // JSONObject j1 = new JSONObject();
            // for (Object name : jo.keySet()) {
            // Object o = jo.get(name);
            // j1.put(name, o.getClass().getName());
            // }
            //
            // jo.put("definition", j1);
            JSONArray arr = new JSONArray();
            arr.add(jo);

            JSONObject req = new JSONObject();
            req.put("list", arr);
            req.put("_time", System.currentTimeMillis());

            // try {
            req.convertStringtoBase64();

            String data = Base64.encode(DES.encode(req.toString().getBytes(), appkey.getBytes()));

            Response r = Http.post(url, null, new String[][] { { "User-Agent", Publisher.USER_AGENT }, { "m", "set" } }, new String[][] { { "appid", appid }, { "data", data } });

            log.debug("synced: resp=" + r.body + ", request=" + jo);
            JSONObject j = JSONObject.fromObject(r.body);
            j.convertBase64toString();
            if (j.getInt("state") == 200 && j.getInt("updated") > 0) {
                return 1;
            }
            // } catch (Exception e) {
            // log.error(e.getMessage(), e);
            // OpLog.warn("sync", e.getMessage(), e.getMessage());
            // }

        }

        return 0;

    }
}
