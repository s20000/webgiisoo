package com.giisoo.core.conf;

import java.sql.*;
import java.util.*;

import com.giisoo.core.bean.*;

/**
 * The Class SystemConfig.
 * 
 * @author yjiang
 */
@DBMapping(table = "tblconfig")
public class SystemConfig extends Bean {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The s. */
    String s;

    /** The i. */
    int i;

    /** The l. */
    long l;

    /** The d. */
    double d;

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
            return c.i;
        }

        c = Bean.load("name=?", new Object[] { name }, SystemConfig.class);
        if (c != null) {
            data.put(name, c);
            return c.i;
        } else {
            c = new SystemConfig();
            c.i = conf.getInt(name, defaultValue);
            data.put(name, c);
            return c.i;
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
            return c.d;
        }

        c = Bean.load("name=?", new Object[] { name }, SystemConfig.class);
        if (c != null) {
            data.put(name, c);
            return c.d;
        } else {
            c = new SystemConfig();
            c.d = conf.getDouble(name, defaultValue);
            data.put(name, c);
            return c.d;
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
            return c.s;
        }

        c = Bean.load("name=?", new Object[] { name }, SystemConfig.class);
        if (c != null) {
            data.put(name, c);
            return c.s;
        } else {
            c = new SystemConfig();
            c.s = conf.getString(name, defaultValue);
            data.put(name, c);
            return c.s;
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
            return c.l;
        }

        c = Bean.load("name=?", new Object[] { name }, SystemConfig.class);
        if (c != null) {
            data.put(name, c);
            return c.l;
        } else {
            c = new SystemConfig();
            c.l = conf.getLong(name, defaultValue);
            data.put(name, c);
            return c.l;
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
            Bean.delete("name=?", new Object[] { name }, SystemConfig.class);
            return;
        }

        if (Bean.exists("name=?", new Object[] { name }, SystemConfig.class)) {
            if (o instanceof Long) {
                Bean.update("name=?", new Object[] { name }, V.create("l", o), SystemConfig.class);
            } else if (o instanceof Integer) {
                Bean.update("name=?", new Object[] { name }, V.create("i", o), SystemConfig.class);
            } else if (o instanceof Double || o instanceof Float) {
                Bean.update("name=?", new Object[] { name }, V.create("d", o), SystemConfig.class);
            } else {
                Bean.update("name=?", new Object[] { name }, V.create("s", o.toString()), SystemConfig.class);
            }
        } else {
            if (o instanceof Long) {
                Bean.insert(V.create("l", o).set("name", name), SystemConfig.class);
            } else if (o instanceof Integer) {
                Bean.insert(V.create("i", o).set("name", name), SystemConfig.class);
            } else if (o instanceof Double || o instanceof Float) {
                Bean.insert(V.create("d", o).set("name", name), SystemConfig.class);
            } else {
                Bean.insert(V.create("s", o.toString()).set("name", name), SystemConfig.class);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
     */
    protected void load(ResultSet r) throws SQLException {
        s = r.getString("s");
        i = r.getInt("i");
        l = r.getLong("l");
        d = r.getDouble("d");
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
        if (c != null && c.youngerThan(X.AMINUTE * 10)) {
            return c;
        }

        return null;
    }

    /** The data. */
    transient static private Map<String, SystemConfig> data = new HashMap<String, SystemConfig>();

}
