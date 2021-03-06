package com.giisoo.framework.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;

import com.giisoo.app.web.data.IFilter;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.Data;
import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.utils.Http;
import com.giisoo.framework.utils.Http.Response;
import com.giisoo.utils.base.DES;
import com.mongodb.BasicDBObject;

/**
 * the {@code SyncTask} Class lets sync the data from/to remote
 * 
 * the most important APIs:
 * 
 * <pre>
 * register(String name, String order), register the sync data
 * 
 * </pre>
 * 
 * @author joe
 *
 */
public class SyncTask extends WorkerTask {

    static Log log = LogFactory.getLog(SyncTask.class);

    public static SyncTask instance = new SyncTask();

    public static enum Type {
        set, get, mset;
    };

    private static Map<String, List<String>> groups = new LinkedHashMap<String, List<String>>();
    private static Map<String, DataFilter> collections = new LinkedHashMap<String, DataFilter>();

    /**
     * test the collection setting is support op "t"
     * 
     * @param collection
     * @param t
     * @return boolean
     */
    public boolean support(String collection, String t) {
        DataFilter df = collections.get(collection);
        if (df != null && df.type != null) {
            for (Type t1 : df.type) {
                if (t1.toString().equals(t)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * get the setting of the collection
     * 
     * @param collection
     * @return String
     */
    public String setting(String collection) {
        return SystemConfig.s("sync." + collection, null);
    }

    /**
     * get the lasttime of data which synced
     * 
     * @param collection
     * @return long
     */
    public long lasttime(String collection) {
        return SystemConfig.l("sync." + collection + ".lasttime", 0);
    }

    /**
     * get All Collections for syncing
     * 
     * @return Map
     */
    public static Map<String, DataFilter> getCollections() {
        return collections;
    }

    /**
     * register the collection that can be sync, with the order:
     * "{update_time:1}"
     * 
     * @param collection
     * @param order
     */
    public static void register(String collection, String order, int number) {
        register(collection, collection, order, number);
    }

    /**
     * register a collection under the "parent" setting, with the "order"
     * 
     * @param parent
     * @param collection
     * @param order
     */
    public static void register(String parent, String collection, String order, int number) {
        register(parent, collection, order, null, number);
    }

    /**
     * set default type: set and get
     * 
     * @param parent
     * @param collection
     * @param order
     * @param filter
     */
    public static void register(String parent, String collection, String order, IFilter filter, int number) {
        register(parent, collection, order, filter, new Type[] { Type.set, Type.get }, number);
    }

    /**
     * register a collection under parent, order by "order", and call filter
     * when syncing
     * 
     * @param collection
     * @param order
     * @param filter
     * @param t
     */
    public static void register(String parent, String collection, String order, IFilter filter, Type[] t, int number) {
        DataFilter df = new DataFilter();
        df.order = order;
        df.filter = filter;
        df.type = t;
        df.number = number;

        collections.put(collection, df);
        List<String> list = groups.get(parent);
        if (list == null) {
            list = new ArrayList<String>();
            groups.put(parent, list);
        }
        list.add(collection);
    }

    /**
     * get groups
     * 
     * @return Set
     */
    public static Set<String> getGroups() {
        return groups.keySet();
    }

    /**
     * get collection under a group
     * 
     * @param group
     * @return List
     */
    public List<String> collections(String group) {
        return groups.get(group);
    }

    private SyncTask() {
    }

    private void sync(final String collection, final String url, final String appid, final String appkey) {
        String type = SystemConfig.s("sync." + collection, X.EMPTY);

        if ("get".equals(type)) {
            new WorkerTask() {

                @Override
                public String getName() {
                    return "synctask." + collection;
                }

                @Override
                public void onExecute() {
                    JSONObject req = new JSONObject();
                    JSONObject query = new JSONObject();
                    JSONObject order = new JSONObject();

                    try {
                        DataFilter df = collections.get(collection);
                        if (df != null && !X.isEmpty(df.order)) {
                            order = JSONObject.fromObject(df.order);
                        } else {
                            order.put("updated", 1);
                        }

                        long updated = SystemConfig.l("sync." + collection + ".lasttime", 0);
                        JSONObject q = new JSONObject();
                        q.put("$gte", updated);
                        query.put(order.keys().next(), q);

                        req.put("query", query);
                        req.put("order", order);
                        req.put("collection", collection);

                        int s = 0;
                        int n = 10;

                        boolean hasmore = true;
                        while (hasmore) {
                            hasmore = false;

                            req.put("s", s);
                            req.put("n", n);
                            req.put("_time", System.currentTimeMillis());

                            String data = Base64.encode(DES.encode(req.toString().getBytes(), appkey.getBytes()));

                            Response r = Http.post(url, null, new String[][] { { "User-Agent", Publisher.USER_AGENT }, { "m", "get" } }, new String[][] { { "appid", appid }, { "data", data } });

                            // log.debug("resp=" + r.body);
                            if (r.status == 200) {
                                JSONObject jo = JSONObject.fromObject(r.body);
                                jo.convertBase64toString();
                                log.debug("resp=" + jo);

                                JSONArray arr = jo.getJSONArray("list");

                                if (arr != null && arr.size() > 0) {

                                    for (int i = 0; i < arr.size(); i++) {
                                        JSONObject j1 = arr.getJSONObject(i);
                                        if (df != null && df.filter != null) {
                                            df.filter.process("get", j1);
                                        }

                                        Data.update(collection, j1);

                                        long l1 = j1.getLong(order.keys().next().toString());
                                        if (l1 > updated) {
                                            updated = l1;
                                            SystemConfig.setConfig("sync." + collection + ".lasttime", updated);
                                        }
                                    }

                                    s += arr.size();
                                    hasmore = arr.size() >= n;
                                }
                            } // end if "status == 200"

                        }
                    } catch (Exception e) {
                        log.error("query=" + query + " order=" + order, e);
                        OpLog.warn("sync", e.getMessage(), e.getMessage());
                    }
                }

            }.schedule(0);
        } else if ("set".equals(type)) {
            new WorkerTask() {

                @Override
                public String getName() {
                    return "synctask." + collection;
                }

                @Override
                public void onExecute() {

                    // auto push
                    BasicDBObject query = new BasicDBObject().append("synced", new BasicDBObject().append("$ne", 1));
                    BasicDBObject order = new BasicDBObject().append(X._ID, 1);
                    int s = 0;

                    try {
                        Beans<Data> bs = Data.load(collection, query, order, s, 100);
                        JSONObject jo = new JSONObject();
                        jo.put("synced", 1);
                        DataFilter df = collections.get(collection);

                        while (bs != null && bs.getList() != null && bs.getList().size() > 0) {
                            for (Data p : bs.getList()) {
                                if (df != null && df.filter != null) {
                                    df.filter.process("set", p);
                                }
                                p.set("collection", collection);

                                if (Publisher.publish(p) > 0) {
                                    jo.put(X._ID, p.get(X._ID));
                                    Data.update(collection, jo);
                                }
                            }

                            // s += bs.getList().size();
                            bs = Data.load(collection, query, order, s, 100);
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                }

            }.schedule(0);
        }
    }

    @Override
    public void onExecute() {

        final String url = SystemConfig.s("sync.url", null);
        final String appid = SystemConfig.s("sync.appid", null);
        final String appkey = SystemConfig.s("sync.appkey", null);
        if (!X.isEmpty(url) && !X.isEmpty(appid) && !X.isEmpty(appkey)) {

            for (String collection : collections.keySet()) {
                sync(collection, url, appid, appkey);
            }
        }

    }

    @Override
    public String getName() {
        return "sync.task";
    }

    @Override
    public void onFinish() {

        log.info("sync.task done.......................");

        this.schedule(X.AHOUR);
    }

    private static class DataFilter {
        IFilter filter;
        Type[] type;
        String order;
        int number = 10;
    }
}
