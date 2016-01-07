package com.giisoo.core.conf;

import java.util.*;

import com.giisoo.core.bean.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * The Class SystemConfig.
 * 
 * @author yjiang
 */
@DBMapping(collection = "gi_config")
public class SystemConfig extends Bean {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    Object var;

    private static SystemConfig owner = new SystemConfig();

    public static SystemConfig getInstance() {
        return owner;
    }

    /**
     * I.
     * 
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     * @return the int
     */
    public static int i(String name, int defaultValue) {
        SystemConfig c = getConfig(name);
        if (c != null) {
            return Bean.toInt(c.var);
        }

        c = Bean.load(new BasicDBObject(X._ID, name), SystemConfig.class);
        if (c != null) {
            data.put(name, c);
            return Bean.toInt(c.var);
        } else {
            c = new SystemConfig();
            c.var = conf.getInt(name, defaultValue);
            data.put(name, c);
            return Bean.toInt(c.var);
        }
    }

    /**
     * D.
     * 
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     * @return the double
     */
    public static double d(String name, double defaultValue) {
        SystemConfig c = getConfig(name);
        if (c != null) {
            return Bean.toDouble(c.var);
        }

        c = Bean.load(new BasicDBObject(X._ID, name), SystemConfig.class);
        if (c != null) {
            data.put(name, c);
            return Bean.toDouble(c.var);
        } else {
            c = new SystemConfig();
            c.var = conf.getDouble(name, defaultValue);
            data.put(name, c);
            return Bean.toDouble(c.var);
        }
    }

    /**
     * S.
     * 
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     * @return the string
     */
    public static String s(String name, String defaultValue) {
        SystemConfig c = getConfig(name);
        if (c != null) {
            return c.var != null ? c.var.toString() : null;
        }

        c = Bean.load(new BasicDBObject(X._ID, name), SystemConfig.class);
        if (c != null) {
            data.put(name, c);
            return c.var != null ? c.var.toString() : null;
        } else {
            c = new SystemConfig();
            c.var = conf.getString(name, defaultValue);
            data.put(name, c);
            return c.var != null ? c.var.toString() : null;
        }
    }

    /**
     * L.
     * 
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     * @return the long
     */
    public static long l(String name, long defaultValue) {
        SystemConfig c = getConfig(name);
        if (c != null) {
            return Bean.toLong(c.var);
        }

        c = Bean.load(new BasicDBObject(X._ID, name), SystemConfig.class);
        if (c != null) {
            data.put(name, c);
            return Bean.toLong(c.var);
        } else {
            c = new SystemConfig();
            c.var = conf.getLong(name, defaultValue);
            data.put(name, c);
            return Bean.toLong(c.var);
        }
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    /**
     * Sets the.
     * 
     * @param name
     *            the name
     * @param o
     *            the o
     */
    public synchronized static void setConfig(String name, Object o) {
        if (X.isEmpty(name)) {
            return;
        }

        data.remove(name);

        if (o == null) {
            Bean.delete(new BasicDBObject(X._ID, name), SystemConfig.class);
            return;
        }

        if (Bean.exists(new BasicDBObject(X._ID, name), SystemConfig.class)) {
            Bean.updateCollection(name, V.create("var", o), SystemConfig.class);
        } else {
            Bean.insertCollection(V.create("var", o).set(X._ID, name), SystemConfig.class);
        }
    }

    @Override
    protected void load(DBObject d) {
        var = d.get("var");
    }

    /**
     * Gets the.
     * 
     * @param name
     *            the name
     * @return the system config
     */
    private static SystemConfig getConfig(String name) {
        SystemConfig c = data.get(name);
        if (c != null && c.age() < X.AMINUTE * 10) {
            return c;
        }

        return null;
    }

    /** The data. */
    transient static private Map<String, SystemConfig> data = new HashMap<String, SystemConfig>();

}
