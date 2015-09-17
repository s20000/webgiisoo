/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.giisoo.core.bean.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Access, name will used to load language
 * 
 * @author yjiang
 * 
 */
@DBMapping(collection = "gi_access")
public class Access extends Bean {
    /**
   * 
   */
    private static final long serialVersionUID = 1L;

    String id;
    String name;

    /**
     * Group name.
     * 
     * @return the string
     */
    public String groupName() {
        int i = name.indexOf(".");
        if (i > 0) {
            int j = name.indexOf(".", i + 1);
            if (j > 0) {
                return name.substring(0, j);
            } else {
                return name.substring(0, i);
            }
        }
        return "access";
    }

    public String getName() {
        return name;
    }

    /**
     * Add a access name, the access name MUST fit with "access.[group].[name]"
     * .
     * 
     * @param name
     *            the name
     */
    public static void set(String name) {
        if (X.isEmpty(name) || !name.startsWith("access.")) {
            log.error("error access.name: " + name, new Exception("error access name:" + name));
        } else if (!exists(name)) {
            Bean.insertCollection(V.create(X._ID, name), Access.class);
        }
    }

    static private Set<String> cache = new HashSet<String>();

    public static boolean exists(String name) {
        if (cache.contains(name)) {
            return true;
        }

        if (Bean.exists(new BasicDBObject(X._ID, name), Access.class)) {
            cache.add(name);
            return true;
        }
        return false;
    }

    /**
     * Load all access and group by [group] name
     * 
     * @return the map
     */
    public static Map<String, List<Access>> load() {
        Beans<Access> bs = Bean.load(new BasicDBObject().append(X._ID, new BasicDBObject("$ne", "access.admin")), new BasicDBObject(X._ID, 1), 0, Integer.MAX_VALUE, Access.class);
        List<Access> list = bs.getList();
        Map<String, List<Access>> r = new TreeMap<String, List<Access>>();
        String group = null;
        List<Access> last = null;
        for (Access a : list) {
            String name = a.groupName();
            if (group == null || !name.equals(group)) {
                group = name;
                last = new ArrayList<Access>();
                r.put(group, last);
            }
            last.add(a);
        }

        return r;
    }

    @Override
    protected void load(DBObject d) {
        name = String.valueOf(d.get(X._ID));
        id = name;
    }

}
