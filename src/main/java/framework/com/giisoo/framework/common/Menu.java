/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.json.*;

import com.giisoo.core.bean.*;
import com.mongodb.BasicDBObject;

/**
 * Menu
 * 
 * @author yjiang
 * 
 */
@DBMapping(collection = "gi_menu")
public class Menu extends Bean {

    /**
   * 
   */
    private static final long serialVersionUID = 1L;

    // int id;

    /**
     * the name of the node, is the key of the display in language
     */

    /**
     * Insert or update.
     * 
     * @param arr
     *            the arr
     * @param tag
     *            the tag
     */
    public static void insertOrUpdate(JSONArray arr, String tag) {
        if (arr == null) {
            return;
        }

        int len = arr.size();
        for (int i = 0; i < len; i++) {
            JSONObject jo = arr.getJSONObject(i);

            /**
             * test and create from the "root"
             */

            jo.put("tag", tag);
            insertOrUpdate(jo, 0);
        }
    }

    public long getId() {
        return this.getLong(X._ID);
    }

    public String getName() {
        return this.getString("name");
    }

    public String getLoad() {
        return this.getString("load");
    }

    public int getChilds() {
        return this.getInt("childs");
    }

    public String getUrl() {
        return this.getString("url");
    }

    public String getTag() {
        return this.getString("tag");
    }

    public String getClasses() {
        return this.getString("classes");
    }

    public String getClick() {
        return this.getString("click");
    }

    public String getContent() {
        return this.getString("content");
    }

    public int getSeq() {
        return this.getInt("seq");
    }

    public String getAccess() {
        return this.getString("access");
    }

    /**
     * Insert or update.
     * 
     * @param jo
     *            the jo
     * @param parent
     *            the parent
     */
    public static void insertOrUpdate(JSONObject jo, long parent) {
        try {
            // log.info(jo);

            String name = jo.getString("name");
            if (!X.isEmpty(name)) {
                /**
                 * create menu if not exists
                 */
                V v = V.create().copy(jo, "url", "click", "classes", "content", "tag", "access", "seq", "tip", "style", "load");

                /**
                 * create the access if not exists
                 */
                if (jo.containsKey("access")) {
                    String[] ss = jo.getString("access").split("[|&]");
                    for (String s : ss) {
                        Access.set(s);
                    }
                }

                if (log.isDebugEnabled())
                    log.debug(jo.toString());

                /**
                 * create the menu item is not exists
                 */
                Menu m = insertOrUpdate(parent, name, v);

                /**
                 * get all childs from the json
                 */
                if (jo.containsKey("childs")) {
                    JSONArray arr = jo.getJSONArray("childs");
                    int len = arr.size();
                    for (int i = 0; i < len; i++) {
                        JSONObject j = arr.getJSONObject(i);
                        if (jo.containsKey("tag")) {
                            j.put("tag", jo.get("tag"));
                        }
                        insertOrUpdate(j, m.getId());
                    }
                }
            } else {
                // is role ?
                String role = jo.getString("role");
                String access = jo.getString("access");
                if (!X.isEmpty(role)) {
                    String memo = jo.getString("memo");

                    if (log.isInfoEnabled())
                        log.info("create role: role=" + role + ", memo=" + memo);

                    long rid = Role.create(role, memo);
                    if (rid <= 0) {
                        Role r = Role.loadByName(role);
                        if (r != null) {
                            rid = r.getId();
                        }
                    }
                    if (rid > 0) {
                        String[] ss = access.split("[|&]");
                        for (String s : ss) {
                            if (!X.isEmpty(s)) {
                                Access.set(s);
                                Role.setAccess(rid, s);
                            }
                        }
                    } else {
                        log.error("can not create or load the role: " + role);
                        OpLog.warn("initial", "can not create or load the role:" + role, null);
                    }
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * test and create new menu if not exists
     * 
     * @param parent
     * @param name
     * @param url
     * @param classes
     * @param click
     * @param content
     * @return Menu
     */
    private static Menu insertOrUpdate(long parent, String name, V v) {
        BasicDBObject q = new BasicDBObject().append("parent", parent).append("name", name);
        if (!Bean.exists(q, Menu.class)) {
            long id = UID.next("menu.id");
            while (Bean.exists(new BasicDBObject(X._ID, id), Menu.class)) {
                id = UID.next("menu.id");
            }
            Bean.insertCollection(v.set(X._ID, id).set("id", id).set("parent", parent).set("name", name), Menu.class);

            long count = Bean.count(new BasicDBObject("parent", parent), Menu.class);
            Bean.updateCollection(parent, V.create("childs", count), Menu.class);

        } else {
            /**
             * update
             */
            Bean.updateCollection(q, v, Menu.class);

        }

        return Bean.load(q, Menu.class);
    }

    public String getTip() {
        return this.getString("tip");
    }

    /**
     * Submenu.
     * 
     * @param id
     *            the id
     * @return the beans
     */
    public static Beans<Menu> submenu(long id) {
        // load it
        Beans<Menu> bb = Bean.load(new BasicDBObject("parent", id), new BasicDBObject("seq", -1), 0, -1, Menu.class);
        return bb;
    }

    /**
     * Load.
     * 
     * @param parent
     *            the parent
     * @param name
     *            the name
     * @return the menu
     */
    public static Menu load(long parent, String name) {
        Menu m = Bean.load(new BasicDBObject("parent", parent).append("name", name), Menu.class);
        return m;
    }

    /**
     * Submenu.
     * 
     * @return the beans
     */
    public Beans<Menu> submenu() {
        return submenu(this.getId());
    }

    /**
     * Removes the by tag.
     * 
     * @param tag
     *            the tag
     */
    public void removeByTag(String tag) {
        Bean.delete("tag=?", new String[] { tag }, Menu.class);
    }

    /**
     * Removes the.
     * 
     * @param id
     *            the id
     */
    public static void remove(long id) {
        Bean.delete(new BasicDBObject(X.ID, id), Menu.class);

        /**
         * remove all the sub
         */
        Beans<Menu> bs = submenu(id);
        List<Menu> list = bs.getList();

        if (list != null) {
            for (Menu m : list) {
                remove(m.getId());
            }
        }
    }

    /**
     * Filter access.
     * 
     * @param list
     *            the list
     * @param me
     *            the me
     * @return the collection
     */
    public static Collection<Menu> filterAccess(List<Menu> list, User me) {
        if (list == null) {
            return null;
        }

        /**
         * filter according the access, and save seq
         */
        Map<Integer, Menu> map = new TreeMap<Integer, Menu>();

        for (Menu m : list) {
            String access = m.getAccess();
            boolean has = false;
            if (X.isEmpty(access)) {
                has = true;
            }

            if (!has && me != null) {
                if (access.indexOf("|") > 0) {
                    String[] ss = access.split("\\|");
                    if (me.hasAccess(ss)) {
                        has = true;
                    }
                } else if (access.indexOf("&") > 0) {
                    String[] ss = access.split("\\&");
                    for (String s : ss) {
                        if (!me.hasAccess(s)) {
                            has = false;
                            break;
                        }
                    }
                } else if (me.hasAccess(access)) {
                    has = true;
                }
            }

            if (has) {
                int seq = m.getSeq();
                Menu m1 = map.get(seq);
                if (m1 != null) {
                    /**
                     * get short's name first
                     */
                    if (m1.getName().indexOf(m.getName()) > -1) {
                        map.put(seq, m);
                    } else if (m.getName().indexOf(m1.getName()) > -1) {
                        map.put(seq, m1);
                    } else {
                        map.put(seq + 1, m);
                    }
                } else {
                    map.put(seq, m);
                }
            }
        }

        return map.values();
    }

    /**
     * Removes the.
     * 
     * @param tag
     *            the tag
     */
    public static void remove(String tag) {
        Bean.delete(new BasicDBObject("tag", tag), Menu.class);
    }

    /**
     * Reset.
     */
    public static void reset() {
        Bean.updateCollection(new BasicDBObject(), V.create("seq", -1), Menu.class);
    }

    /**
     * Cleanup.
     */
    public static void cleanup() {
        Bean.delete(new BasicDBObject("seq", new BasicDBObject("$lt", 0)), Menu.class);
    }

    public String getStyle() {
        return this.getString("style");
    }
}
