package com.giisoo.core.bean;

import java.util.List;

import net.sf.json.JSONObject;

import com.giisoo.core.worker.WorkerTask;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * the key fields for the collection, used to create index
 * 
 * @author joe
 *
 */

@DBMapping(collection = "gi_key")
public class KeyField extends Bean {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String getId() {
        return this.getString(X._ID);
    }

    public String getCollection() {
        return this.getString("collection");
    }

    public String getQ() {
        return this.getString("q");
    }

    // -------------------
    public static KeyField load(String id) {
        return Bean.load(new BasicDBObject(X._ID, id), KeyField.class);
    }

    public static Beans<KeyField> load(BasicDBObject q, BasicDBObject order, int s, int n) {
        return Bean.load(q, order, s, n, KeyField.class);
    }

    public static void deleteAll() {
        Bean.delete(new BasicDBObject(), KeyField.class);
    }

    public static void delete(String id) {
        Bean.delete(new BasicDBObject(X._ID, id), KeyField.class);
    }

    public static void update(String id, V v) {
        Bean.updateCollection(id, v, KeyField.class);
    }

    public static void create(final String collection, final DBObject q, final DBObject order) {
        new WorkerTask() {
            @Override
            public void onExecute() {
                BasicDBObject r = new BasicDBObject();
                if (q != null) {
                    for (String s : q.keySet()) {
                        Object v = q.get(s);

                        if (!X._ID.equals(s) && !s.startsWith("$") && !r.containsField(s)) {
                            r.append(s, 1);
                        }

                        if (v instanceof DBObject) {
                            general((DBObject) v, r);
                        } else if (v instanceof List) {
                            general((List) v, r);
                        }

                    }
                }

                if (order != null) {
                    for (String s : order.keySet()) {
                        Object v = order.get(s);

                        if (!X._ID.equals(s) && !s.startsWith("$") && !r.containsField(s)) {
                            r.append(s, 1);
                        }

                        if (v instanceof DBObject) {
                            general((DBObject) v, r);
                        } else if (v instanceof List) {
                            general((List) v, r);
                        }

                    }
                }

                if (r.size() > 0) {
                    String id = UID.id(collection, r);
                    if (!exists(id)) {
                        log.debug("r=" + r);

                        Bean.insertCollection(V.create(X._ID, id).set("collection", collection).set("q", r.toString()).set("created", System.currentTimeMillis()), KeyField.class);
                    }
                }
            }

        }.schedule(0);

    }

    private static boolean exists(String id) {
        return Bean.exists(new BasicDBObject(X._ID, id), KeyField.class);
    }

    private static void general(List l, BasicDBObject r) {
        for (int i = 0; i < l.size(); i++) {
            Object v = l.get(i);
            if (v instanceof DBObject) {
                general((DBObject) v, r);
            } else if (v instanceof List) {
                general((List) v, r);
            }
        }
    }

    private static void general(DBObject q, BasicDBObject r) {
        if (q instanceof BasicDBList) {
            BasicDBList l = (BasicDBList) q;
            for (int i = 0; i < l.size(); i++) {
                Object v = l.get(i);
                if (v instanceof DBObject) {
                    general((DBObject) v, r);
                } else if (v instanceof List) {
                    general((List) v, r);
                }
            }
        } else {
            for (String s : q.keySet()) {
                Object v = q.get(s);
                if (v instanceof DBObject) {
                    general((DBObject) v, r);
                } else if (!X._ID.equals(s) && !s.startsWith("$") && !r.containsField(s)) {
                    r.append(s, 1);
                }
            }
        }
    }

    public void run() {
        new WorkerTask() {

            @Override
            public void onExecute() {
                try {
                    String q = getQ();
                    String collection = getCollection();
                    JSONObject jo = JSONObject.fromObject(q);
                    BasicDBObject keys = new BasicDBObject();
                    for (Object name : jo.keySet()) {
                        keys.append((String) name, 1);
                    }
                    Bean.getCollection(collection).ensureIndex(keys);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

        }.schedule(0);

        update(this.getId(), V.create("status", "done"));
    }

}
