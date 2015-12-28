/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.*;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.giisoo.core.bean.*;
import com.giisoo.core.cache.Cache;
import com.giisoo.framework.web.Module;

/**
 * Role
 * 
 * @author yjiang
 * 
 */
@DBMapping(table = "tblrole")
public class Role extends Bean {

    /**
   * 
   */
    private static final long serialVersionUID = 1L;

    int id;
    String name;
    String memo;
    long updated;

    /**
     * Checks for.
     * 
     * @param a
     *            the a
     * @return true, if successful
     */
    public boolean has(Access a) {
        List<String> list = getAccesses();

        return list == null ? false : list.contains(a.name);
    }

    public String getMemo() {
        return memo;
    }

    private static int nextId() {
        int id = Math.abs((int) UID.next("role.id"));
        while (Bean.exists("id=?", new Object[] { id }, Role.class)) {
            id = Math.abs((int) UID.next("role.id"));
        }
        return id;
    }

    /**
     * Creates the.
     * 
     * @param name
     *            the name
     * @param memo
     *            the memo
     * @return the int
     */
    public static int create(String name, String memo) {
        Role r = Role.loadByName(name);
        if (r != null) {
            /**
             * exists, create failded
             */
            return r.getId();
        }

        int id = nextId();

        if (Bean.insert(V.create("id", id).set("name", name).set("memo", memo).set("updated", System.currentTimeMillis()), Role.class) > 0) {
            return id;
        }

        return -1;
    }

    /**
     * Gets the access.
     * 
     * @return the access
     */
    public List<String> getAccesses() {

        if (!this.containsKey("accesses")) {
            List<String> list = Bean.loadList("tblroleaccess", "name", "rid=?", new Object[] { id }, String.class, null);

            this.set("accesses", list);

            recache();
        }

        return (List<String>) this.get("accesses");
    }

    private void recache() {
        Cache.set("role://" + id, this);
    }

    /**
     * Sets the access.
     * 
     * @param rid
     *            the rid
     * @param name
     *            the name
     */
    public static void setAccess(int rid, String name) {
        if (!Bean.exists("tblroleaccess", "rid=? and name=?", new Object[] { rid, name }, null)) {
            Bean.insert("tblroleaccess", V.create("rid", rid).set("name", name).set("id", UID.id(rid, name)), null);

            Bean.update("id=?", new Object[] { rid }, V.create("updated", System.currentTimeMillis()), Role.class);
        }
    }

    /**
     * Removes the access.
     * 
     * @param rid
     *            the rid
     * @param name
     *            the name
     */
    public static void removeAccess(int rid, String name) {
        Bean.delete("tblroleaccess", "rid=? and name=?", new Object[] { rid, name }, null);

        Bean.update("id=?", new Object[] { rid }, V.create("updated", System.currentTimeMillis()), Role.class);

    }

    /**
     * Load all.
     * 
     * @param roles
     *            the roles
     * @return the list
     */
    public static List<Role> loadAll(List<Integer> roles) {
        List<Role> list = new ArrayList<Role>();
        for (int rid : roles) {
            Role r = Role.load(rid);
            if (r != null) {
                list.add(r);
            }
        }
        return list;
    }

    public static boolean exist(int rid) {
        return Bean.exists("id=?", new Object[] { rid }, Role.class);
    }

    private static Role load(int rid) {
        Role r = (Role) Cache.get("role://" + rid);

        if (r == null) {
            r = Bean.load("tblrole", "id=?", new Object[] { rid }, Role.class);
        }

        if (r != null) {
            r.setExpired(60);
            Cache.set("role://" + rid, r);
        }

        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
     */
    @Override
    protected void load(ResultSet r) throws SQLException {
        id = r.getInt("id");
        name = r.getString("name");
        memo = r.getString("memo");
        updated = r.getLong("updated");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Bean#toJSON(net.sf.json.JSONObject)
     */
    @Override
    public boolean toJSON(JSONObject jo) {
        jo.put("id", id);
        jo.put("name", name);
        jo.put("memo", memo);
        jo.put("updated", updated);
        jo.put("accesses", this.getAccesses());

        return true;
    }

    /**
     * Load by name.
     * 
     * @param name
     *            the name
     * @return the role
     */
    public static Role loadByName(String name) {
        return Bean.load("tblrole", "name=?", new Object[] { name }, Role.class);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Load.
     * 
     * @param offset
     *            the offset
     * @param limit
     *            the limit
     * @return the beans
     */
    public static Beans<Role> load(int offset, int limit) {
        return Bean.load("id>0", null, "order by name", offset, limit, Role.class);
    }

    /**
     * Load by id.
     * 
     * @param id
     *            the id
     * @return the role
     */
    public static Role loadById(int id) {
        return Bean.load("id=?", new Object[] { id }, Role.class);
    }

    /**
     * Update.
     * 
     * @param v
     *            the v
     * @return the int
     */
    public int update(V v) {
        return Bean.update("id=?", new Object[] { id }, v.set("updated", System.currentTimeMillis()), Role.class);
    }

    public void setAccess(String[] accesses) {
        if (accesses != null) {
            Bean.delete("tblroleaccess", "rid=?", new Object[] { id }, null);

            for (String a : accesses) {
                Bean.insert("tblroleaccess", V.create("rid", id).set("name", a).set("id", UID.id(id, a)), null);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Exportable#output(java.lang.String,
     * java.lang.Object[], java.util.zip.ZipOutputStream)
     */
    public JSONObject output(String where, Object[] args, ZipOutputStream out) {
        int s = 0;
        // Beans<Role> bs = Bean.load(where, args, null, s, 10, Role.class);
        Beans<Role> bs = Bean.load(null, (Object[]) null, null, s, 10, Role.class);

        JSONObject jo = new JSONObject();
        JSONArray arr = new JSONArray();
        int count = 0;

        while (bs != null && bs.getList() != null && bs.getList().size() > 0) {
            for (Role d : bs.getList()) {
                /**
                 * avoid sync admin user
                 */
                if (d.id > 0) {
                    JSONObject j = new JSONObject();
                    d.toJSON(j);

                    j.convertStringtoBase64();
                    arr.add(j);

                    count++;
                }
            }
            s += bs.getList().size();
            bs = Bean.load(null, (Object[]) null, null, s, 10, Role.class);

        }

        jo.put("list", arr);
        jo.put("total", count);

        return jo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Exportable#input(net.sf.json.JSONArray,
     * java.util.zip.ZipFile)
     */
    public int input(JSONArray list, ZipFile in) {
        int count = 0;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            JSONObject jo = list.getJSONObject(i);
            jo.convertBase64toString();

            int id = jo.getInt("id");
            count += Bean.insertOrUpdate("tblrole", "id=?", new Object[] { id }, V.create().copy(jo, "name", "memo").set("id", id).copyLong(jo, "updated"), null);

            if (jo.has("accesses")) {
                /**
                 * the accesses of the role
                 */
                @SuppressWarnings("unchecked")
                List<String> accesses = jo.getJSONArray("accesses");
                if (accesses != null) {
                    for (String a : accesses) {
                        Bean.insertOrUpdate("tblroleaccess", "rid=? and name=?", new Object[] { id, a }, V.create("rid", id).set("name", a).set("id", UID.id(id, a)), null);
                    }
                }
            }
        }
        return count;
    }

    /**
     * Delete.
     * 
     * @param id
     *            the id
     * @return the int
     */
    public static int delete(int id) {
        return Bean.delete("id=?", new Object[] { id }, Role.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Exportable#load(java.lang.String,
     * java.lang.Object[], int, int)
     */
    public Beans<Role> load(String where, Object[] args, int s, int n) {
        return Bean.load(null, null, "order by id", s, n, Role.class);
    }

    public String getExportableId() {
        return Integer.toString(id);
    }

    public String getExportableName() {
        return name;
    }

    public long getExportableUpdated() {
        return updated;
    }

    public boolean isExportable() {
        return id > 0;
    }

    /**
     * Updated.
     * 
     * @return the long
     */
    public static long updated() {
        return Bean.getOne("max(updated)", null, null, null, 0, Role.class);
    }

}
