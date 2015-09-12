/**
 * @author jjiang
 * @date 2010-10-23
 */
package com.giisoo.core.bean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.core.cache.DefaultCachable;
import com.giisoo.framework.common.OpLog;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;

/**
 * The Class Bean. <br>
 * 
 */
@SuppressWarnings("deprecation")
public abstract class Bean extends DefaultCachable implements Map<String, Object> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2L;

    /** The log. */
    protected static Log log = LogFactory.getLog(Bean.class);
    protected static Log sqllog = LogFactory.getLog("sql");

    /** The conf. */
    protected static Configuration conf;

    /** The mongo. */
    private static Map<String, DB> mongo = new HashMap<String, DB>();

    /**
     * @deprecated
     */
    private static Map<Object, List<IData>> datalisteners = new HashMap<Object, List<IData>>();

    /**
     * @deprecated
     * @param tables
     * @param listener
     */
    public synchronized static void addListener(String[] tables, IData listener) {
        synchronized (datalisteners) {
            for (String table : tables) {
                List<IData> list = datalisteners.get(table);
                if (list == null) {
                    list = new ArrayList<IData>();
                    datalisteners.put(table, list);
                }

                if (!list.contains(listener)) {
                    list.add(listener);
                }
            }
        }
    }

    /**
     * @deprecated
     * @param json
     */
    public static void insertJSON(String json) {
        JSONObject jo = JSONObject.fromObject(json);
        jo.convertBase64toString();

        V v = V.create();
        for (Object name : jo.keySet()) {
            if (!"table".equals(name)) {
                v.set(name.toString(), jo.get(name));
            }
        }

        Bean.insert(jo.getString("table"), v, null);
    }

    /**
     * add a data change listener
     * @deprecated
     * @param clazzes
     * @param listener
     */
    public synchronized static void addListener(Class<? extends Bean>[] clazzes, IData listener) {
        synchronized (datalisteners) {
            for (Class<? extends Bean> clazz : clazzes) {
                List<IData> list = datalisteners.get(clazz);
                if (list == null) {
                    list = new ArrayList<IData>();
                    datalisteners.put(clazz, list);
                }

                if (!list.contains(listener)) {
                    list.add(listener);
                }
            }
        }
    }

    /**
     * issue a event of data change
     * @deprecated
     * @param table
     * @param op
     * @param where
     * @param args
     */
    public static void onChanged(String table, byte op, String where, Object... args) {
        synchronized (datalisteners) {
            List<IData> list = datalisteners.get(table);
            if (list != null && list.size() > 0) {
                JSONObject jo = new JSONObject();
                // jo.put("table", table);
                // jo.put("op", op);
                jo.put("where", where);
                jo.put("args", args);
                /**
                 * support referable
                 */
                for (IData d : list) {
                    d.onChanged(table, op, jo);
                }
            }
        }
    }

    /**
     * @deprecated
     * @param clazz
     * @param where
     * @param args
     */
    public static void onChanged(Class<? extends Bean> clazz, String where, Object... args) {
        onChanged(clazz, IData.OP_UPDATE, where, args);
    }

    /**
     * @deprecated
     * @param clazz
     * @param op
     * @param where
     * @param args
     */
    public static void onChanged(Class<? extends Bean> clazz, byte op, String where, Object... args) {

        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) clazz.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + clazz + "] declaretion");
            return;
        }

        synchronized (datalisteners) {
            List<IData> list = datalisteners.get(mapping.table());
            if (list != null && list.size() > 0) {
                JSONObject jo = new JSONObject();
                // jo.put("table", table);
                // jo.put("op", op);
                jo.put("where", where);
                jo.put("args", args);
                /**
                 * support referable
                 */
                for (IData d : list) {
                    d.onChanged(mapping.table(), op, jo);
                }
            }
        }
    }

    /**
     * remove a data change listener
     * 
     * @deprecated
     * @param listener
     */
    public static void removeListener(IData listener) {
        if (listener == null)
            return;

        synchronized (datalisteners) {
            String[] names = datalisteners.keySet().toArray(new String[datalisteners.size()]);

            for (String name : names) {
                List<IData> list = datalisteners.get(name);
                list.remove(listener);
                if (list.size() == 0) {
                    datalisteners.remove(name);
                }
            }
        }
    }

    /**
     * initialize the Bean with the configuration.
     * 
     * @param conf
     *            the conf
     */
    public static void init(Configuration conf) {
        Bean.conf = conf;

        if (conf.containsKey("mongo.url")) {
            String url = conf.getString("mongo.url");
            String hosts[] = url.split(";");

            ArrayList<ServerAddress> list = new ArrayList<ServerAddress>();
            for (String s : hosts) {
                try {
                    String s2[] = s.split(":");
                    String host;
                    int port = 27017;
                    if (s2.length > 1) {
                        host = s2[0];
                        port = Integer.parseInt(s2[1]);
                    } else {
                        host = s2[0];
                    }

                    list.add(new ServerAddress(host, port));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

        }
    }

    /**
     * update the table with sets sql.
     * 
     * @param table
     *            the table
     * @param sets
     *            the sets
     * @param where
     *            the where
     * @param whereArgs
     *            the where args
     * @return the int
     */
    static protected int update(String table, String sets, String where, Object[] whereArgs, String db) {
        /**
         * create the sql statement
         */
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(table).append(" set ").append(sets);

        if (where != null) {
            sql.append(" where ").append(where);
        }

        /**
         * update it in database
         */
        Connection c = null;
        PreparedStatement p = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return -1;

            p = c.prepareStatement(sql.toString());

            int order = 1;
            if (whereArgs != null) {
                for (int i = 0; i < whereArgs.length; i++) {
                    Object o = whereArgs[i];

                    setParameter(p, order++, o);
                }
            }

            return p.executeUpdate();

        } catch (Exception e) {
            log.error(sql.toString() + toString(whereArgs), e);
        } finally {
            close(c, p, r);
        }

        return 0;
    }

    /**
     * Update.
     * 
     * @param table
     *            the table
     * @param v
     *            the v
     * @param where
     *            the where
     * @param whereArgs
     *            the where args
     * @return the int
     * @deprecated
     */
    static protected int update(String table, Values v, String where, Object[] whereArgs, String db) {

        /**
         * create the sql statement
         */
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(table).append(" set ");
        ArrayList<String> key = v.keySet();
        for (int i = 0; i < key.size() - 1; i++) {
            sql.append(key.get(i)).append("=?,");
        }
        sql.append(key.get(key.size() - 1)).append("=? ");

        if (where != null) {
            sql.append(" where ").append(where);
        }

        /**
         * update it in database
         */
        Connection c = null;
        PreparedStatement p = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return -1;

            p = c.prepareStatement(sql.toString());

            ArrayList<Object> values = v.values();
            int order = 1;
            for (int i = 0; i < values.size(); i++) {
                Object o = values.get(i);

                setParameter(p, order++, o);

            }

            if (whereArgs != null) {
                for (int i = 0; i < whereArgs.length; i++) {
                    Object o = whereArgs[i];

                    setParameter(p, order++, o);

                }
            }

            return p.executeUpdate();

        } catch (Exception e) {
            log.error(sql.toString() + toString(whereArgs), e);
        } finally {
            close(c, p, r);
        }

        return 0;
    }

    /**
     * convert a millis time to date with offset.
     * @deprecated
     * @param millis
     *            the millis
     * @param field
     *            the field
     * @param offset
     *            the offset
     * @return the int
     */
    public static int date(long millis, int field, int offset) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        if (offset != 0) {
            c.add(field, offset);
        }
        return millis2Date(c.getTimeInMillis());
    }

    /**
     * @deprecated
     */
    private static DateFormat dateformat = new SimpleDateFormat("yyyyMMdd");

    /**
     * @deprecated
     */
    private static DateFormat timeformat = new SimpleDateFormat("HHmmss");

    /**
     * Millis2 date.
     * @deprecated
     * @param millis
     *            the millis
     * @return the int
     */
    public static int millis2Date(long millis) {
        return Integer.parseInt(dateformat.format(new Date(millis)));
    }

    /**
     * Millis2 time.
     * @deprecated
     * @param millis
     *            the millis
     * @return the int
     */
    public static int millis2Time(long millis) {
        return Integer.parseInt(timeformat.format(new Date(millis)));
    }

    /**
     * Date2 millis.
     * @deprecated
     * @param date
     *            the date
     * @return the long
     */
    public static long date2Millis(int date) {
        try {
            return dateformat.parse(Integer.toString(date)).getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Day.
     * 
     * @param time
     *            the time
     * @return the int
     * @deprecated
     */
    public static int day(long time) {
        long d = time - DAYBASE;
        return (int) (d / X.ADAY + 2011000);

        // Calendar cal = Calendar.getInstance();
        // cal.setTimeInMillis(time);
        // int y = cal.get(Calendar.YEAR);
        // int d = cal.get(Calendar.DAY_OF_YEAR);
        //
        // return y * 1000 + d;
    }

    /**
     * Insert.
     * 
     * @param table
     *            the table
     * @param v
     *            the v
     * @return true, if successful
     * @deprecated
     */
    protected static boolean insert(String table, Values v, String db) {
        /**
         * create the sql statement
         */
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ").append(table).append(" (");
        ArrayList<String> key = v.keySet();
        for (int i = 0; i < key.size(); i++) {
            sql.append(key.get(i)).append(",");
        }

        sql.append(" create_date) values( ");
        for (int i = 0; i < key.size(); i++) {
            sql.append("?, ");
        }
        sql.append("?)");

        /**
         * insert it in database
         */
        Connection c = null;
        PreparedStatement p = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return false;

            p = c.prepareStatement(sql.toString());

            ArrayList<Object> values = v.values();
            int order = 1;
            for (int i = 0; i < values.size(); i++) {
                Object o = values.get(i);

                setParameter(p, order++, o);

            }

            p.setLong(order++, System.currentTimeMillis());

            p.executeUpdate();

            return true;
        } catch (Exception e) {

            log.error(sql.toString() + v.toString(), e);
        } finally {
            close(c, p, r);
        }
        return false;
    }

    /**
     * Sets the parameter.
     * 
     * @param p
     *            the p
     * @param i
     *            the i
     * @param o
     *            the o
     * @throws SQLException
     *             the SQL exception
     */
    private static void setParameter(PreparedStatement p, int i, Object o) throws SQLException {
        if (o == null) {
            p.setObject(i, null);
        } else if (o instanceof Integer) {
            p.setInt(i, (Integer) o);
        } else if (o instanceof Date) {
            p.setTimestamp(i, new java.sql.Timestamp(((Date) o).getTime()));
        } else if (o instanceof Long) {
            p.setLong(i, (Long) o);
        } else if (o instanceof Float) {
            p.setFloat(i, (Float) o);
        } else if (o instanceof Double) {
            p.setDouble(i, (Double) o);
        } else if (o instanceof Boolean) {
            p.setBoolean(i, (Boolean) o);
        } else if (o instanceof Timestamp) {
            p.setTimestamp(i, (Timestamp) o);
        } else {
            p.setString(i, o.toString());
        }

    }

    /**
     * Delete.
     * 
     * @param collection
     *            the collection
     * @param query
     *            the query
     * @return the int
     */
    protected static int delete(String collection, DBObject query) {
        DBCollection db = Bean.getCollection(collection);
        db.remove(query);
        return 1;
    }

    protected static int delete(DBObject query, Class<? extends Bean> t) {
        String collection = getCollection(t);
        if (collection != null) {
            delete(collection, query);
        }
        return -1;
    }

    protected static int delete(String where, Object[] args, Class<? extends Bean> t) {
        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return -1;
        }

        return delete(mapping.table(), where, args, mapping.db());
    }

    /**
     * Delete.
     * 
     * @param table
     *            the table
     * @param where
     *            the where
     * @param whereArgs
     *            the where args
     * @return the int
     */
    protected static int delete(String table, String where, Object[] whereArgs, String db) {
        /**
         * create the sql statement
         */
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ").append(table);
        if (where != null) {
            sql.append(" where ").append(where);
        }

        /**
         * update it in database
         */
        Connection c = null;
        PreparedStatement p = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return -1;

            p = c.prepareStatement(sql.toString());

            if (whereArgs != null) {
                int order = 1;

                for (int i = 0; i < whereArgs.length; i++) {
                    Object o = whereArgs[i];

                    setParameter(p, order++, o);

                }
            }

            return p.executeUpdate();

        } catch (Exception e) {
            log.error(sql.toString() + toString(whereArgs), e);
        } finally {
            close(p, c);
        }

        return 0;
    }

    /**
     * Inits the db.
     * @deprecated
     * @param database
     *            the database
     * @return the db
     */
    private static synchronized DB initDB(String database) {
        DB g = mongo.get(database);
        if (g == null) {
            String url = conf.getString("mongo[" + database + "].url", X.EMPTY);
            if (!X.EMPTY.equals(url)) {
                String hosts[] = url.split(";");

                ArrayList<ServerAddress> list = new ArrayList<ServerAddress>();
                for (String s : hosts) {
                    try {
                        String s2[] = s.split(":");
                        String host;
                        int port = 27017;
                        if (s2.length > 1) {
                            host = s2[0];
                            port = Integer.parseInt(s2[1]);
                        } else {
                            host = s2[0];
                        }

                        list.add(new ServerAddress(host, port));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }

                String dbname = conf.getString("mongo[" + database + "].db", X.EMPTY);
                if (!X.EMPTY.equals(dbname)) {
                    MongoOptions mo = new MongoOptions();
                    mo.connectionsPerHost = conf.getInt("mongo[" + database + "].conns", 50);
                    mo.autoConnectRetry = true;
                    Mongo mongodb = new Mongo(list, mo);
                    g = mongodb.getDB(dbname);

                    mongo.put(database, g);
                }
            }
        }

        return g;
    }

    /**
     * Checks for db.
     * @deprecated
     * @param database
     *            the database
     * @return true, if successful
     */
    protected static boolean hasDB(String database) {
        DB g = mongo.get(database);
        if (g == null) {
            g = initDB(database);
        }

        return g != null;
    }

    /**
     * get Mongo DB connection
     * 
     * @return
     */
    public static DB getDB() {
        return getDB("prod");
    }

    /**
     * get Mongo DB connection
     * 
     * @param database
     * @return
     */
    public static DB getDB(String database) {
        DB g = null;
        if (X.isEmpty(database)) {
            database = "prod";
        }

        synchronized (mongo) {
            g = mongo.get(database);
            if (g == null) {
                String url = conf.getString("mongo[" + database + "].url", X.EMPTY);
                if (!X.EMPTY.equals(url)) {
                    String hosts[] = url.split(";");

                    ArrayList<ServerAddress> list = new ArrayList<ServerAddress>();
                    for (String s : hosts) {
                        try {
                            String s2[] = s.split(":");
                            String host;
                            int port = 27017;
                            if (s2.length > 1) {
                                host = s2[0];
                                port = Integer.parseInt(s2[1]);
                            } else {
                                host = s2[0];
                            }

                            list.add(new ServerAddress(host, port));
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    String dbname = conf.getString("mongo[" + database + "].db", X.EMPTY);
                    if (!X.EMPTY.equals(dbname)) {
                        MongoOptions mo = new MongoOptions();
                        mo.connectionsPerHost = conf.getInt("mongo[" + database + "].conns", 50);
                        mo.autoConnectRetry = true;
                        Mongo mongodb = new Mongo(list, mo);
                        g = mongodb.getDB(dbname);

                        mongo.put(database, g);
                    }
                }
            }
        }

        return g;
    }

    /**
     * Gets the collection.
     * 
     * @param database
     *            the database
     * @param collection
     *            the collection
     * @return the collection
     */
    protected static DBCollection getCollection(String database, String collection) {
        DB g = getDB(database);

        if (g != null) {
            return g.getCollection(collection);
        }

        return null;
    }

    /**
     * Gets the collection.
     * 
     * @param name
     *            the name
     * @return the collection
     */
    protected static DBCollection getCollection(String name) {
        return getCollection("prod", name);
    }

    /**
     * Gets the connection.
     * 
     * @return the connection
     * @throws SQLException
     *             the SQL exception
     */
    public static Connection getConnection() throws SQLException {
        try {
            long tid = Thread.currentThread().getId();
            if (outdoor.size() > 0) {
                Connection[] cs = null;
                synchronized (outdoor) {
                    cs = outdoor.keySet().toArray(new Connection[outdoor.size()]);
                }

                for (Connection c : cs) {
                    Long[] dd = outdoor.get(c);
                    if (dd != null && dd[0] == tid) {
                        dd[2]++;
                        synchronized (outdoor) {
                            outdoor.put(c, dd);
                        }
                        return c;
                    }
                }
            }
            Connection c = com.giisoo.core.db.DB.getConnection();
            synchronized (outdoor) {
                outdoor.put(c, new Long[] { tid, System.currentTimeMillis(), 0L });
            }
            return c;
        } catch (SQLException e1) {
            /**
             * print the fuck who hold the connections;
             */
            StringBuilder sb = new StringBuilder();
            sb.append("====================begin of thread dump=============================\r\n");
            sb.append("outdoor:" + outdoor.size());

            Map<Thread, StackTraceElement[]> m = Thread.getAllStackTraces();
            for (Iterator<Thread> it = m.keySet().iterator(); it.hasNext();) {
                Thread t = it.next();
                long tid = t.getId();
                Long[][] d0 = null;

                // TODO, there is big performance issue
                synchronized (outdoor) {
                    d0 = outdoor.values().toArray(new Long[outdoor.size()][]);
                }

                for (Long[] dd : d0) {
                    if (dd[0] == tid) {
                        StackTraceElement[] st = m.get(t);
                        sb.append(t.getName()).append(" - ").append(t.getState()).append(" - ").append((System.currentTimeMillis() - dd[1]) / 1000).append("ms/").append(dd[2]).append(t.toString())
                                .append("\r\n");
                        for (StackTraceElement e : st) {
                            sb.append("\t").append(e.getClassName()).append(".").append(e.getMethodName()).append("(").append(e.getLineNumber()).append(")").append("\r\n");
                        }
                        break;
                    }
                }

            }
            sb.append("====================end of thread dump=============================");

            log.error(sb.toString());

            throw e1;
        }
    }

    private static Map<Connection, Long[]> outdoor = new HashMap<Connection, Long[]>();

    /**
     * Gets the connection.
     * 
     * @param name
     *            the name
     * @return the connection
     * @throws SQLException
     *             the SQL exception
     */
    public static Connection getConnection(String name) throws SQLException {
        return com.giisoo.core.db.DB.getConnection(name);
    }

    /**
     * Equals.
     * 
     * @param o1
     *            the o1
     * @param o2
     *            the o2
     * @return true, if successful
     */
    static public boolean equals(Object o1, Object o2) {
        if (o1 == o2)
            return true;

        if (o1 != null) {
            return o1.equals(o2);
        }

        return false;
    }

    /**
     * Close.
     * 
     * @param objs
     *            the objs
     */
    static public void close(Object... objs) {
        for (Object o : objs) {
            try {
                if (o == null)
                    continue;

                if (o instanceof ResultSet) {
                    ((ResultSet) o).close();
                } else if (o instanceof Statement) {
                    ((Statement) o).close();
                } else if (o instanceof PreparedStatement) {
                    ((PreparedStatement) o).close();
                } else if (o instanceof Connection) {
                    // local.remove();
                    Connection c = (Connection) o;
                    Long[] dd = outdoor.get(c);
                    if (dd == null || dd[2] <= 0) {
                        try {
                            if (!c.getAutoCommit()) {
                                c.commit();
                            }
                        } catch (Exception e1) {
                        } finally {
                            c.close();
                        }
                        synchronized (outdoor) {
                            outdoor.remove(c);
                        }
                    } else {
                        dd[2]--;
                        synchronized (outdoor) {
                            outdoor.put(c, dd);
                        }
                    }
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * parse a object to a integer.
     * 
     * @param v
     *            the v
     * @param defaultValue
     *            the default value
     * @return the int
     */
    public static int toInt(Object v, int defaultValue) {
        if (v != null) {
            if (v instanceof Integer) {
                return (Integer) v;
            }
            if (v instanceof Float) {
                return (int) ((Float) v).floatValue();
            }
            if (v instanceof Double) {
                return (int) ((Double) v).doubleValue();
            }
            String s = v.toString();
            if (X.EMPTY.equals(s)) {
                return defaultValue;
            }
            try {
                if (s.indexOf(".") > 0) {
                    return (int) toFloat(s);
                }
                return Integer.parseInt(s);
            } catch (Exception e) {
                log.error(e);

                StringBuilder sb = new StringBuilder();
                s = s.trim();
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (c >= '0' && c <= '9') {
                        sb.append(c);
                    } else {
                        break;
                    }
                }
                // log.debug("s=" + sb.toString());

                if (sb.length() > 0) {
                    try {
                        return Integer.parseInt(sb.toString());
                    } catch (Exception e1) {
                        log.error(e1);
                    }
                }
            }
        }

        return defaultValue;
    }

    /**
     * parse a object to float, the default value is "0".
     * 
     * @param v
     *            the v
     * @return the float
     */
    public static float toFloat(Object v) {
        return toFloat(v, 0);
    }

    /**
     * parse a object to a float with defaultvalue.
     * 
     * @param v
     *            the v
     * @param defaultValue
     *            the default value
     * @return the float
     */
    public static float toFloat(Object v, float defaultValue) {
        if (v != null) {
            if (v instanceof Integer) {
                return (Integer) v;
            }
            if (v instanceof Float) {
                return (Float) v;
            }
            if (v instanceof Double) {
                return (float) ((Double) v).doubleValue();
            }
            String s = v.toString();
            if (X.EMPTY.equals(s)) {
                return defaultValue;
            }
            try {
                return Float.parseFloat(s);
            } catch (Exception e) {
                log.error(e);

                StringBuilder sb = new StringBuilder();
                s = s.trim();
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (c >= '0' && c <= '9') {
                        sb.append(c);
                    } else if (c == '.') {
                        if (sb.indexOf(".") > -1) {
                            break;
                        } else {
                            sb.append(c);
                        }
                    } else {
                        break;
                    }
                }
                try {
                    return Float.parseFloat(sb.toString());
                } catch (Exception e1) {
                    log.error(e1);
                }
            }
        }
        return defaultValue;
    }

    /**
     * To double.
     * 
     * @param v
     *            the v
     * @return the double
     */
    public static double toDouble(Object v) {
        if (v != null) {
            if (v instanceof Integer) {
                return (Integer) v;
            }
            if (v instanceof Float) {
                return (Float) v;
            }
            if (v instanceof Double) {
                return ((Double) v).doubleValue();
            }
            String s = v.toString();
            if (X.EMPTY.equals(s)) {
                return 0;
            }
            try {
                return Double.parseDouble(s);
            } catch (Exception e) {
                log.error(e);

                StringBuilder sb = new StringBuilder();
                s = s.trim();
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (c >= '0' && c <= '9') {
                        sb.append(c);
                    } else if (c == '.') {
                        if (sb.indexOf(".") > -1) {
                            break;
                        } else {
                            sb.append(c);
                        }
                    } else {
                        break;
                    }
                }
                try {
                    return Double.parseDouble(sb.toString());
                } catch (Exception e1) {
                    log.error(e1);
                }

            }
        }
        return 0;
    }

    final protected int insertOrUpdate(String where, Object[] args, V sets) {
        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) this.getClass().getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + this.getClass() + "] declaretion");
            return -1;
        }

        return insertOrUpdate(mapping.table(), where, args, sets, mapping.db());
    }

    /**
     * Insert or update.
     * 
     * @param table
     *            the table
     * @param where
     *            the where
     * @param args
     *            the args
     * @param sets
     *            the sets
     * @return the int
     */
    protected final static int insertOrUpdate(String table, String where, Object[] args, V sets, String db) {
        int i = 0;
        if (exists(table, where, args, db)) {
            i = update(table, where, args, sets, db);
            if (i > 0) {
                onChanged(table, IData.OP_UPDATE, where, args);
            }
        } else {
            i = insert(table, sets, db);
            if (i > 0) {
                onChanged(table, IData.OP_CREATE, where, args);
            }
        }

        return i;
    }

    protected static boolean exists(String where, Object[] args, Class<? extends Bean> t) {
        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return false;
        }

        return exists(mapping.table(), where, args, mapping.db());

    }

    /**
     * Exists.
     * 
     * @param table
     *            the table
     * @param where
     *            the where
     * @param args
     *            the args
     * @return true, if successful
     */
    protected static boolean exists(String table, String where, Object[] args, String db) {
        /**
         * create the sql statement
         */
        TimeStamp t = TimeStamp.create();

        StringBuilder sql = new StringBuilder();
        sql.append("select 1 from ").append(table);

        if (where != null) {
            sql.append(" where ").append(where);
        }
        sql.append(" limit 1");

        /**
         * search it in database
         */
        Connection c = null;
        PreparedStatement p = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return false;

            p = c.prepareStatement(sql.toString());

            int order = 1;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object o = args[i];

                    setParameter(p, order++, o);
                }
            }

            r = p.executeQuery();
            return r.next();

        } catch (Exception e) {
            log.error(sql.toString() + toString(args), e);
        } finally {
            close(r, p, c);

            if (t.past() > 2) {
                sqllog.debug("cost:" + t.past() + "ms, sql=[" + sql + "]");
            }
        }
        return false;
    }

    protected static int update(String where, Object[] args, V sets, Class<? extends Bean> t) {
        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return -1;
        }

        if (!X.isEmpty(mapping.table())) {
            return update(mapping.table(), where, args, sets, mapping.db());
        }

        return -1;
    }

    /**
     * Update.
     * 
     * @param table
     *            the table
     * @param where
     *            the where
     * @param whereArgs
     *            the where args
     * @param sets
     *            the sets
     * @return the int
     */
    protected static int update(String table, String where, Object[] whereArgs, V sets, String db) {
        /**
         * create the sql statement
         */
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(table).append(" set ");

        StringBuilder s = new StringBuilder();
        for (V.Entity v : sets.list) {
            if (v.isValid()) {
                if (s.length() > 0)
                    s.append(",");
                s.append(v.name).append("=?");
            }
        }
        sql.append(s);

        if (where != null) {
            sql.append(" where ").append(where);
        }

        /**
         * update it in database
         */
        Connection c = null;
        PreparedStatement p = null;

        int updated = 0;
        try {

            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return -1;

            p = c.prepareStatement(sql.toString());

            int order = 1;
            for (V.Entity v : sets.list) {
                if (v.isValid()) {
                    setParameter(p, order++, v.value);
                }
            }

            if (whereArgs != null) {
                for (int i = 0; i < whereArgs.length; i++) {
                    Object o = whereArgs[i];

                    setParameter(p, order++, o);
                }
            }

            updated = p.executeUpdate();

        } catch (Exception e) {
            log.error(sql.toString() + toString(whereArgs) + sets.toString(), e);
        } finally {
            close(p, c);
        }

        if (updated > 0) {
            onChanged(table, IData.OP_UPDATE, where, whereArgs);
        }

        return updated;

    }

    /**
     * Update.
     * 
     * @deprecated
     * @param sql
     *            the sql
     * @param args
     *            the args
     * @return the int
     */
    protected static int update(String sql, Object[] args, String db) {
        /**
         * /** update it in database
         */
        Connection c = null;
        PreparedStatement p = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return -1;

            p = c.prepareStatement(sql);

            int order = 1;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object o = args[i];

                    setParameter(p, order++, o);
                }
            }

            return p.executeUpdate();

        } catch (Exception e) {
            log.error(sql + toString(args), e);
        } finally {
            close(p, c);
        }

        return 0;

    }

    /**
     * Load.
     * 
     * @param <T>
     *            the generic type
     * @param table
     *            the table
     * @param where
     *            the where
     * @param args
     *            the args
     * @param clazz
     *            the clazz
     * @return the t
     */
    protected static <T extends Bean> T load(String table, String where, Object[] args, Class<T> clazz) {
        return load(table, where, args, null, clazz, null);

    }

    protected static <T extends Bean> T load(String where, Object[] args, Class<T> t) {
        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return null;
        }

        return load(mapping.table(), where, args, null, t, mapping.db());

    }

    protected static boolean load(String where, Object[] args, Bean b) {
        return load(where, args, null, b);
    }

    protected static boolean load(String table, String where, Object[] args, Bean b) {
        return load(table, where, args, null, b, null);
    }

    protected static <T extends Bean> T load(String where, Object[] args, String orderby, Class<T> t) {
        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return null;
        }

        return load(mapping.table(), where, args, orderby, t, mapping.db());
    }

    protected static boolean load(String where, Object[] args, String orderby, Bean b) {
        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) b.getClass().getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + b.getClass() + "] declaretion");
            return false;
        }

        return load(mapping.table(), where, args, orderby, b, mapping.db());
    }

    /**
     * Load.
     * 
     * @param table
     *            the table
     * @param where
     *            the where
     * @param args
     *            the args
     * @param b
     *            the b
     * @return true, if successful
     */
    protected static boolean load(String table, String where, Object[] args, String orderby, Bean b, String db) {
        /**
         * create the sql statement
         */
        TimeStamp t = TimeStamp.create();

        StringBuilder sql = new StringBuilder();
        sql.append("select * from ").append(table);

        if (where != null) {
            sql.append(" where ").append(where);
        }
        if (orderby != null) {
            sql.append(" ").append(orderby).append(" ");
        }
        sql.append(" limit 1");

        /**
         * search it in database
         */
        Connection c = null;
        PreparedStatement p = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return false;

            p = c.prepareStatement(sql.toString());

            int order = 1;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object o = args[i];

                    setParameter(p, order++, o);
                }
            }

            r = p.executeQuery();
            if (r.next()) {
                b.load(r);

                return true;
            }

        } catch (Exception e) {
            log.error(sql + toString(args), e);
        } finally {
            close(r, p, c);

            if (t.past() > 2) {
                sqllog.debug("cost: " + t.past() + "ms, sql=[" + sql + "]");
            }
        }

        return false;
    }

    /**
     * Load.
     * 
     * @param <T>
     *            the generic type
     * @param collection
     *            the collection
     * @param query
     *            the query
     * @param clazz
     *            the clazz
     * @return the t
     */
    protected static <T extends Bean> T load(String collection, DBObject query, Class<T> clazz) {
        try {
            return load(collection, query, clazz.newInstance());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    protected static <T extends Bean> T load(String collection, DBObject query, T b) {
        DBCollection db = Bean.getCollection(collection);
        try {
            DBObject d = db.findOne(query);
            if (d != null) {
                b.load(d);
                return b;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    protected static <T extends Bean> T load(String collection, DBObject query, DBObject order, T b) {
        DBCollection db = Bean.getCollection(collection);
        DBCursor cur = null;
        try {
            cur = db.find(query);
            if (order != null) {
                cur.sort(order);
            }

            if (cur.hasNext()) {
                DBObject d = cur.next();
                if (d != null) {
                    b.load(d);
                    return b;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (cur != null) {
                cur.close();
            }
        }

        return null;
    }

    protected static <T extends Bean> Beans<T> load(DBObject query, DBObject orderby, int offset, int limit, Class<T> clazz) {
        String collection = getCollection(clazz);
        if (collection != null) {
            return load(collection, query, orderby, offset, limit, clazz);
        }
        return null;

    }

    /**
     * get the data from the collection
     * 
     * @param query
     * @param obj
     * @return <T extends Bean>
     */
    protected static <T extends Bean> T load(DBObject query, DBObject order, T obj) {
        String collection = getCollection(obj.getClass());
        if (collection != null) {
            return load(collection, query, order, obj);
        }
        return null;

    }

    /**
     * 
     * @param collection
     * @param query
     * @param orderBy
     * @param offset
     * @param limit
     * @param clazz
     * @return
     */
    protected static <T extends Bean> Beans<T> load(String collection, DBObject query, DBObject orderBy, int offset, int limit, Class<T> clazz) {
        TimeStamp t = TimeStamp.create();
        DBCollection db = Bean.getCollection(collection);
        DBCursor cur = db.find(query);
        try {
            if (orderBy != null) {
                cur.sort(orderBy);
            }

            Beans<T> bs = new Beans<T>();
            bs.total = cur.count();
            cur.skip(offset);
            bs.list = new ArrayList<T>();

            while (cur.hasNext() && limit > 0) {
                DBObject d = cur.next();
                T b = clazz.newInstance();
                b.load(d);
                bs.list.add(b);
                limit--;
            }

            log.debug("load - cost=" + t.past() + "ms, collection=" + collection + ", query=" + query + ", order=" + orderBy + ", result=" + bs);

            if (t.past() > 10000) {
                OpLog.warn("bean", "load", "cost=" + t.past() + "ms", "load - cost=" + t.past() + "ms, collection=" + collection + ", query=" + query + ", order=" + orderBy + ", result=" + bs);
            }
            return bs;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (cur != null)
                cur.close();
        }

        return null;
    }

    protected static <T extends Bean> T load(DBObject query, Class<T> t) {
        String collection = getCollection(t);
        if (collection != null) {
            try {
                T obj = t.newInstance();
                return load(query, null, obj);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    protected static <T extends Bean> T load(DBObject query, DBObject order, Class<T> t) {
        String collection = getCollection(t);
        if (collection != null) {
            try {
                T obj = t.newInstance();
                return load(query, order, obj);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Load.
     * 
     * @param collection
     *            the collection
     * @param query
     *            the query
     * @return the DB object
     */
    protected static DBObject load(String collection, DBObject query) {
        /**
         * create the sql statement
         */
        DBCollection c = Bean.getCollection(collection);
        if (c != null) {
            DBObject d = c.findOne(query);
            return d;
        }
        return null;
    }

    /**
     * Load data by default, get all fields and set in map
     * 
     * @param r
     *            the r
     * @throws SQLException
     *             the SQL exception
     */
    protected void load(ResultSet r) throws SQLException {
        ResultSetMetaData m = r.getMetaData();
        int cols = m.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            Object o = r.getObject(i);
            if (o instanceof java.sql.Date) {
                o = ((java.sql.Date) o).toString();
            } else if (o instanceof java.sql.Time) {
                o = ((java.sql.Time) o).toString();
            } else if (o instanceof java.sql.Timestamp) {
                o = ((java.sql.Timestamp) o).toString();
            } else if (o instanceof java.math.BigDecimal) {
                o = o.toString();
            }
            this.set(m.getColumnName(i), o);
        }
    }

    /**
     * Load by default, get all columns to a map
     * 
     * @param d
     *            the d
     */
    protected void load(DBObject d) {

        for (String name : d.keySet()) {
            this.set(name, d.get(name));
        }

    }

    /**
     * Load.
     * 
     * @param <T>
     *            the generic type
     * @param table
     *            the table
     * @param cols
     *            the cols
     * @param where
     *            the where
     * @param args
     *            the args
     * @param clazz
     *            the clazz
     * @return the list
     */
    protected final static <T extends Bean> List<T> load(String table, String[] cols, String where, Object[] args, Class<T> clazz) {
        return load(table, cols, where, args, null, -1, -1, clazz, null);
    }

    protected final static <T extends Bean> List<T> load(String[] cols, String where, Object[] args, Class<T> clazz) {
        return load(cols, where, args, null, -1, -1, clazz);
    }

    protected final static <T extends Bean> List<T> load(String[] cols, String where, Object[] args, String orderby, int offset, int limit, Class<T> t) {
        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return null;
        }

        return load(mapping.table(), cols, where, args, orderby, offset, limit, t, mapping.db());
    }

    /**
     * Load.
     * 
     * @param <T>
     *            the generic type
     * @param table
     *            the table
     * @param cols
     *            the cols
     * @param where
     *            the where
     * @param args
     *            the args
     * @param orderby
     *            the orderby
     * @param offset
     *            the offset
     * @param limit
     *            the limit
     * @param clazz
     *            the clazz
     * @return the list
     */
    protected static <T extends Bean> List<T> load(String table, String[] cols, String where, Object[] args, String orderby, int offset, int limit, Class<T> clazz, String db) {
        /**
         * create the sql statement
         */
        TimeStamp t = TimeStamp.create();

        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        if (cols != null) {
            for (int i = 0; i < cols.length - 1; i++) {
                sql.append(cols[i]).append(", ");
            }

            sql.append(cols[cols.length - 1]);

        } else {
            sql.append("*");
        }

        sql.append(" from ").append(table);
        if (where != null) {
            sql.append(" where ").append(where);
        }

        if (orderby != null) {
            sql.append(" ").append(orderby);
        }

        // if (com.giisoo.db.DB.isMysql()) {
        // /**
        // * mysql has different turn, shit
        // */
        // if (offset > 0) {
        // sql.append(" offset ").append(offset);
        // }
        //
        // if (limit > 0) {
        // sql.append(" limit ").append(limit);
        // }
        // } else {
        if (limit > 0) {
            sql.append(" limit ").append(limit);
        }

        if (offset > 0) {
            sql.append(" offset ").append(offset);
        }
        // }

        // log.debug("sql:" + sql.toString());

        /**
         * search it in database
         */
        Connection c = null;
        PreparedStatement p = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return null;

            p = c.prepareStatement(sql.toString());

            int order = 1;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object o = args[i];

                    setParameter(p, order++, o);
                }
            }

            r = p.executeQuery();
            List<T> list = new ArrayList<T>();
            while (r.next()) {
                T b = clazz.newInstance();
                b.load(r);
                list.add(b);
            }

            return list;
        } catch (Exception e) {
            log.error(sql.toString() + toString(args), e);
        } finally {
            close(r, p, c);

            if (t.past() > 2) {
                sqllog.debug("cost:" + t.past() + "ms, sql=[" + sql + "]");
            }
        }
        return null;
    }

    protected static <T extends Bean> Beans<T> load(String where, Object[] args, String orderby, int offset, int limit, Class<T> t) {
        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return null;
        }

        return load(mapping.table(), where, args, orderby, offset, limit, t, mapping.db());
    }

    /**
     * Load.
     * 
     * @param <T>
     *            the generic type
     * @param table
     *            the table
     * @param where
     *            the where
     * @param args
     *            the args
     * @param orderby
     *            the orderby
     * @param offset
     *            the offset
     * @param limit
     *            the limit
     * @param clazz
     *            the clazz
     * @return the beans
     */
    protected static <T extends Bean> Beans<T> load(String table, String where, Object[] args, String orderby, int offset, int limit, Class<T> clazz, String db) {
        /**
         * create the sql statement
         */
        TimeStamp t = TimeStamp.create();

        StringBuilder sql = new StringBuilder();
        StringBuilder sum = new StringBuilder();
        sql.append("select * from ").append(table);
        sum.append("select count(*) t from ").append(table);
        if (where != null) {
            sql.append(" where ").append(where);
            sum.append(" where ").append(where);
        }

        if (orderby != null) {
            sql.append(" ").append(orderby);
        }

        if (limit > 0) {
            sql.append(" limit ").append(limit);
        }

        if (offset > 0) {
            sql.append(" offset ").append(offset);
        }

        // log.debug("sql:" + sql.toString());

        /**
         * search it in database
         */
        Connection c = null;
        PreparedStatement p = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return null;

            p = c.prepareStatement(sum.toString());

            int order = 1;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object o = args[i];

                    setParameter(p, order++, o);
                }
            }

            r = p.executeQuery();
            Beans<T> rs = new Beans<T>();
            if (r.next()) {
                rs.total = r.getInt("t");
            }
            r.close();
            r = null;
            p.close();
            p = null;

            if (rs.total > 0) {
                p = c.prepareStatement(sql.toString());

                order = 1;
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        Object o = args[i];

                        setParameter(p, order++, o);
                    }
                }

                r = p.executeQuery();
                rs.list = new ArrayList<T>();
                while (r.next()) {
                    T b = clazz.newInstance();
                    b.load(r);
                    rs.list.add(b);
                }
            }

            log.debug("load - cost=" + t.past() + "ms, collection=" + table + ", sql=" + sql + ", result=" + rs);

            if (t.past() > 10000) {
                OpLog.warn("bean", "load", "cost=" + t.past() + "ms", "load - cost=" + t.past() + "ms, collection=" + table + ", sql=" + sql + ", result=" + rs);
            }

            return rs;
        } catch (Exception e) {

            log.error(sql.toString() + toString(args), e);

        } finally {
            close(r, p, c);

            if (t.past() > 2) {
                sqllog.debug("cost:" + t.past() + "ms, sql=[" + sql + "]; [" + sum + "]");
            }
        }
        return null;
    }

    /**
     * get the data
     * 
     * @param table
     * @param where
     * @param args
     * @param orderby
     * @param offset
     * @param limit
     * @param clazz
     * @param c
     * @return
     */
    protected static <T extends Bean> Beans<T> load(String table, String where, Object[] args, String orderby, int offset, int limit, Class<T> clazz, Connection c) {
        /**
         * create the sql statement
         */
        TimeStamp t = TimeStamp.create();

        StringBuilder sql = new StringBuilder();
        StringBuilder sum = new StringBuilder();
        sql.append("select * from ").append(table);
        sum.append("select count(*) t from ").append(table);
        if (where != null) {
            sql.append(" where ").append(where);
            sum.append(" where ").append(where);
        }

        if (orderby != null) {
            sql.append(" ").append(orderby);
        }

        if (limit > 0) {
            sql.append(" limit ").append(limit);
        }

        if (offset > 0) {
            sql.append(" offset ").append(offset);
        }

        // log.debug("sql:" + sql.toString());

        /**
         * search it in database
         */
        PreparedStatement p = null;
        ResultSet r = null;

        try {
            if (c == null)
                return null;

            p = c.prepareStatement(sum.toString());

            int order = 1;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object o = args[i];

                    setParameter(p, order++, o);
                }
            }

            r = p.executeQuery();
            Beans<T> rs = new Beans<T>();
            if (r.next()) {
                rs.total = r.getInt("t");
            }
            r.close();
            r = null;
            p.close();
            p = null;

            if (rs.total > 0) {
                p = c.prepareStatement(sql.toString());

                order = 1;
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        Object o = args[i];

                        setParameter(p, order++, o);
                    }
                }

                r = p.executeQuery();
                rs.list = new ArrayList<T>();
                while (r.next()) {
                    T b = clazz.newInstance();
                    b.load(r);
                    rs.list.add(b);
                }
            }

            log.debug("load - cost=" + t.past() + "ms, collection=" + table + ", sql=" + sql + ", result=" + rs);

            if (t.past() > 10000) {
                OpLog.warn("bean", "load", "cost=" + t.past() + "ms", "load - cost=" + t.past() + "ms, collection=" + table + ", sql=" + sql + ", result=" + rs);
            }

            return rs;
        } catch (Exception e) {

            log.error(sql.toString() + toString(args), e);

        } finally {
            close(r, p);

            if (t.past() > 2) {
                sqllog.debug("cost:" + t.past() + "ms, sql=[" + sql + "]; [" + sum + "]");
            }
        }
        return null;
    }

    final static protected String getCollection(Class<?> clazz) {
        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) clazz.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + clazz + "] declaretion");
            return null;
        } else {
            return mapping.collection();
        }
    }

    /**
     * according the Mapping(table, collection) declaration to insert in table
     * or collection
     * 
     * @param sets
     * @param t
     * @return int
     */
    final protected static int insert(V sets, Class<?> t) {

        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return -1;
        }

        if (!X.isEmpty(mapping.table())) {
            return insert(mapping.table(), sets, mapping.db());
        } else {
            if (!X.isEmpty(mapping.collection())) {
                return insertCollection(mapping.collection(), sets, mapping.db());
            }
        }
        return -1;
    }

    /**
     * batch insert
     * 
     * @param sets
     * @param t
     * @return int
     */
    final protected static int insert(Collection<V> sets, Class<?> t) {
        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return -1;
        }

        return insert(mapping.table(), sets, mapping.db());

    }

    /**
     * insert to the table according the Map(table) declaration
     * 
     * @param sets
     * @param t
     * @return int
     */
    final protected static int insertTable(V sets, Class<?> t) {
        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return -1;
        }

        if (!X.isEmpty(mapping.table())) {
            return insert(mapping.table(), sets, mapping.db());
        }
        return -1;
    }

    /**
     * insert into the collection according to the Mapping(collection)
     * declaration
     * 
     * @param v
     * @param t
     * @return int
     */
    final protected static int insertCollection(V v, Class<?> t) {
        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return -1;
        }

        if (!X.isEmpty(mapping.collection())) {
            return insertCollection(mapping.collection(), v, mapping.db());
        }
        return -1;
    }

    /**
     * update the collection according the Mapping(collection) declaration
     * 
     * @param id
     * @param v
     * @param t
     * @return int
     */
    final protected static int updateCollection(Object id, V v, Class<?> t) {
        String collection = getCollection(t);
        if (collection != null && !"none".equals(collection)) {
            return updateCollection(collection, id, v);
        }
        return -1;
    }

    /**
     * update the collection by query
     * 
     * @param query
     * @param v
     * @param t
     * @return int of updated
     */
    final protected static int updateCollection(DBObject query, V v, Class<?> t) {
        String collection = getCollection(t);
        if (collection != null && !"none".equals(collection)) {
            return updateCollection(collection, query, v);
        }
        return -1;
    }

    /**
     * insert into the collection
     * 
     * @param collection
     * @param v
     * @return int
     */
    final protected static int insertCollection(String collection, V v, String db) {
        BasicDBObject d = new BasicDBObject();
        int len = v.size();
        for (int i = 0; i < len; i++) {
            d.append(v.name(i), v.value(i));
        }

        Bean.getCollection(X.isEmpty(db) ? "prod" : db, collection).insert(d);

        return 1;
    }

    /**
     * update the data in collection
     * 
     * @param collection
     * @param id
     * @param v
     * @return int
     */
    final protected static int updateCollection(String collection, Object id, V v) {

        BasicDBObject q = new BasicDBObject().append(X._ID, id);
        return updateCollection(collection, q, v);
    }

    /**
     * update the data by query
     * 
     * @param collection
     * @param q
     * @param v
     * @return int of updated
     */
    final protected static int updateCollection(String collection, DBObject q, V v) {
        BasicDBObject d = new BasicDBObject();

        int len = v.size();
        for (int i = 0; i < len; i++) {
            d.append(v.name(i), v.value(i));
        }

        WriteResult r = Bean.getCollection(collection).update(q, new BasicDBObject().append("$set", d), true, true);

        // r.getN();
        // r.getField("nModified");
        return 1;
    }

    /**
     * batch insert
     * 
     * @param table
     * @param list
     * @return int
     */
    public static int insert(String table, Collection<V> list, String db) {
        if (list == null || list.size() == 0)
            return 0;

        /**
         * create the sql statement
         */
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ").append(table).append(" (");
        StringBuilder s = new StringBuilder();
        int total = 0;
        V ss = list.iterator().next();
        for (V.Entity v : ss.list) {
            if (v.isValid()) {
                if (s.length() > 0)
                    s.append(",");
                s.append(v.name);
                total++;
            }
        }
        sql.append(s).append(") values( ");

        for (int i = 0; i < total - 1; i++) {
            sql.append("?, ");
        }
        sql.append("?)");

        /**
         * insert it in database
         */
        Connection c = null;
        PreparedStatement p = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return -1;

            p = c.prepareStatement(sql.toString());

            for (V sets : list) {
                int order = 1;
                for (V.Entity v : sets.list) {
                    if (v.isValid()) {
                        setParameter(p, order++, v.value);
                    }
                }

                p.addBatch();
            }

            int[] ii = p.executeBatch();
            int r = 0;
            for (int i : ii) {
                r += i;
            }
            return r;
        } catch (Exception e) {
            log.error(sql.toString() + list.toString(), e);
        } finally {
            close(p, c);
        }
        return 0;
    }

    /**
     * Insert.
     * 
     * @param table
     *            the table
     * @param sets
     *            the sets
     * @return the int
     */
    final protected static int insert(String table, V sets, String db) {
        /**
         * create the sql statement
         */
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ").append(table).append(" (");
        StringBuilder s = new StringBuilder();
        int total = 0;
        for (V.Entity v : sets.list) {
            if (v.isValid()) {
                if (s.length() > 0)
                    s.append(",");
                s.append(v.name);
                total++;
            }
        }
        sql.append(s).append(") values( ");

        for (int i = 0; i < total - 1; i++) {
            sql.append("?, ");
        }
        sql.append("?)");

        /**
         * insert it in database
         */
        Connection c = null;
        PreparedStatement p = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return -1;

            p = c.prepareStatement(sql.toString());

            int order = 1;
            for (V.Entity v : sets.list) {
                if (v.isValid()) {
                    setParameter(p, order++, v.value);
                }
            }

            return p.executeUpdate();

        } catch (Exception e) {
            log.error(sql.toString() + sets.toString(), e);
        } finally {
            close(p, c);
        }
        return 0;
    }

    /**
     * The Class V. of value
     */
    public static final class V {

        /** The list. */
        private List<Entity> list = new ArrayList<Entity>();

        public V split(String... names) {
            V v = V.create();
            Set<String> set = new HashSet<String>();
            for (String name : names) {
                set.add(name);
            }

            for (int i = list.size() - 1; i >= 0; i--) {
                Entity e = list.get(i);
                if (set.contains(e.name)) {
                    list.remove(i);
                    v.list.add(e);
                }
            }
            return v;
        }

        /**
         * Size.
         * 
         * @return the int
         */
        public int size() {
            return list.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return list.toString();
        }

        /**
         * Instantiates a new v.
         */
        private V() {
        }

        /**
         * Creates the.
         * 
         * @param name
         *            the name
         * @param v
         *            the v
         * @return the v
         */
        public static V create(String name, Object v) {
            if (name != null && v != null) {
                return new V().set(name, v);
            } else {
                return new V();
            }
        }

        /**
         * Sets the value if not exists
         * 
         * @param name
         *            the name
         * @param v
         *            the v
         * @return the v
         */
        public V set(String name, Object v) {
            if (name != null && v != null) {
                for (Entity e : list) {
                    if (name.equals(e.name)) {
                        return this;
                    }
                }
                list.add(new Entity(name, v));
            }
            return this;
        }

        /**
         * set the value
         * 
         * @param name
         * @param v
         * @param force
         * @return V
         */
        public V set(String name, Object v, boolean force) {
            if (name != null && v != null) {
                for (Entity e : list) {
                    if (name.equals(e.name)) {
                        e.value = v;
                        return this;
                    }
                }
                list.add(new Entity(name, v));
            }
            return this;
        }

        /**
         * 
         * @param jo
         * @param names
         */
        public V copy(JSONObject jo, String... names) {
            if (jo == null || names == null)
                return this;

            for (String s : names) {
                if (jo.containsKey(s)) {
                    Object o = jo.get(s);
                    if (X.isEmpty(o)) {
                        set(s, X.EMPTY);
                    } else {
                        set(s, o);
                    }
                }
            }

            return this;
        }

        /**
         * copy the name in jo, the format of name is: ["name",
         * "table field name"]
         * 
         * @param jo
         * @param names
         * @return V
         */
        public V copy(JSONObject jo, String[]... names) {
            if (jo == null || names == null)
                return this;

            for (String s[] : names) {
                if (s.length > 1) {
                    if (jo.containsKey(s[0])) {
                        Object o = jo.get(s[0]);
                        if (o == null || "".equals(o))
                            continue;

                        set(s[1], jo.get(s[0]));
                    }
                } else {
                    if (jo.containsKey(s[0])) {
                        Object o = jo.get(s[0]);
                        if (o == null || "".equals(o))
                            continue;

                        set(s[0], jo.get(s[0]));
                    }
                }
            }

            return this;
        }

        /**
         * copy the checkbox value in V, "on"="on", otherwise="off"
         * 
         * @param jo
         * @param names
         * @return V
         */
        public V copyCheckbox(JSONObject jo, String... names) {
            if (jo == null || names == null || names.length == 0)
                return this;

            for (String s : names) {
                if (jo.containsKey(s)) {
                    Object o = jo.get(s);
                    if (X.isEmpty(o)) {
                        set(s, "off");
                    } else if ("on".equals(o)) {
                        set(s, "on");
                    }
                }
            }

            return this;
        }

        public String name(int i) {
            return list.get(i).name;
        }

        public Object value(int i) {
            return list.get(i).value;
        }

        public Object value(String name) {
            for (Entity e : list) {
                if (name.equals(e.name)) {
                    return e.value;
                }
            }
            return null;
        }

        /**
         * The Class Entity.
         */
        private class Entity {

            /** The name. */
            String name;

            /** The value. */
            Object value;

            /*
             * (non-Javadoc)
             * 
             * @see java.lang.Object#toString()
             */
            @Override
            public String toString() {
                return new StringBuilder("(").append(name).append("=").append(value).append(")").toString();
            }

            /**
             * Instantiates a new entity.
             * 
             * @param name
             *            the name
             * @param value
             *            the value
             */
            Entity(String name, Object value) {
                this.name = name;
                this.value = value;
            }

            /**
             * Checks if is valid.
             * 
             * @return true, if is valid
             */
            boolean isValid() {
                return name != null && value != null;
            }
        }

        /**
         * Creates the.
         * 
         * @return the v
         */
        public static V create() {
            return new V();
        }

        public V set(int index, Object v) {
            list.get(index).value = v;
            return this;
        }

        public V copyInt(JSONObject jo, String... names) {
            if (jo == null || names == null)
                return this;

            for (String s : names) {
                if (jo.containsKey(s)) {
                    set(s, Bean.toInt(jo.get(s)));
                }
            }

            return this;
        }

        /**
         * copy the value in jo, the format of name is: ["name",
         * "table field name"]
         * 
         * @param jo
         * @param names
         * @return V
         */
        public V copyInt(JSONObject jo, String[]... names) {
            if (jo == null || names == null)
                return this;

            for (String s[] : names) {
                if (s.length > 1) {
                    if (jo.containsKey(s[0])) {
                        set(s[1], Bean.toInt(jo.get(s[0])));
                    }
                } else {
                    if (jo.containsKey(s[0])) {
                        set(s[0], Bean.toInt(jo.get(s[0])));
                    }
                }
            }

            return this;
        }

        public V copyLong(JSONObject jo, String... names) {
            if (jo == null || names == null)
                return this;

            for (String s : names) {
                if (jo.containsKey(s)) {
                    set(s, Bean.toLong(jo.get(s)));
                }
            }

            return this;
        }

        /**
         * copy the value in jo, the format of name is: ["name",
         * "table field name"]
         * 
         * @param jo
         * @param names
         * @return V
         */
        public V copyLong(JSONObject jo, String[]... names) {
            if (jo == null || names == null)
                return this;

            for (String s[] : names) {
                if (s.length > 1) {
                    if (jo.containsKey(s[0])) {
                        set(s[1], Bean.toLong(jo.get(s[0])));
                    }
                } else {
                    if (jo.containsKey(s[0])) {
                        set(s[0], Bean.toLong(jo.get(s[0])));
                    }
                }
            }

            return this;
        }

        public V remove(int i) {
            list.remove(i);
            return this;
        }

        public V remove(String name) {
            int len = list.size();
            for (int i = len - 1; i >= 0; i--) {
                Entity e = list.get(i);
                if (name.equals(e.name)) {
                    list.remove(i);
                }
            }

            return this;
        }

    }

    /**
     * The Constant DAYBASE.
     * 
     * @deprecated
     */
    final static long DAYBASE = new Date("2011/01/0").getTime();

    /**
     * Load list.
     * 
     * @param <T>
     *            the generic type
     * @param table
     *            the table
     * @param col
     *            the col
     * @param where
     *            the where
     * @param args
     *            the args
     * @param clazz
     *            the clazz
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> loadList(String table, String col, String where, Object[] args, Class<T> clazz, String db) {
        /**
         * create the sql statement
         */
        TimeStamp t = TimeStamp.create();

        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(col).append(" from ").append(table);

        if (where != null) {
            sql.append(" where ").append(where);
        }

        /**
         * search it in database
         */
        Connection c = null;
        PreparedStatement p = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return null;

            p = c.prepareStatement(sql.toString());

            int order = 1;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object o = args[i];

                    setParameter(p, order++, o);
                }
            }

            List<T> list = new ArrayList<T>();
            r = p.executeQuery();
            while (r.next()) {
                T b = (T) (r.getObject(1));
                list.add(b);
            }
            return list;
        } catch (Exception e) {
            log.error(sql.toString() + toString(args), e);
        } finally {
            close(r, p, c);

            if (t.past() > 2) {
                sqllog.debug("cost:" + t.past() + "ms, sql=[" + sql + "]");
            }
        }
        return null;
    }

    /**
     * get a string value from a col from the table.
     * 
     * @param table
     *            the table
     * @param col
     *            the col
     * @param where
     *            the where
     * @param args
     *            the args
     * @return the string
     */
    public static String getString(String table, String col, String where, Object[] args, String db) {
        /**
         * create the sql statement
         */
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(col).append(" from ").append(table);

        if (where != null) {
            sql.append(" where ").append(where);
        }
        sql.append(" limit 1");

        /**
         * search it in database
         */
        Connection c = null;
        PreparedStatement p = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return null;

            p = c.prepareStatement(sql.toString());

            int order = 1;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object o = args[i];

                    setParameter(p, order++, o);
                }
            }

            r = p.executeQuery();
            if (r.next()) {
                return r.getString(col);
            }

        } catch (Exception e) {

            log.error(sql.toString() + toString(args), e);

        } finally {
            close(r, p, c);
        }

        return null;
    }

    /**
     * get the list of the column
     * 
     * @param col
     * @param where
     * @param args
     * @param orderby
     * @param s
     * @param n
     * @return List<T>
     */
    public static <T> List<T> getList(String col, String where, Object[] args, String orderby, int s, int n, Class<? extends Bean> t) {
        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return null;
        }

        return getList(mapping.table(), col, where, args, orderby, s, n, mapping.db());
    }

    /**
     * get one field
     * 
     * @param col
     * @param where
     * @param args
     * @param orderby
     * @param position
     * @return <T>
     */
    public static <T> T getOne(String col, String where, Object[] args, String orderby, int position, Class<? extends Bean> t) {
        /**
         * get the require annotation onGet
         */
        DBMapping mapping = (DBMapping) t.getAnnotation(DBMapping.class);
        if (mapping == null) {
            log.error("mapping missed in [" + t + "] declaretion");
            return null;
        }

        return getOne(mapping.table(), col, where, args, orderby, position, mapping.db());
    }

    /**
     * 
     * @param table
     * @param col
     * @param where
     * @param args
     * @return T primary type
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOne(String table, String col, String where, Object[] args, String orderby, int position, String db) {

        /**
         * create the sql statement
         */
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(col).append(" from ").append(table);

        if (where != null) {
            sql.append(" where ").append(where);
        }
        if (orderby != null) {
            sql.append(" ").append(orderby);
        }
        sql.append(" limit 1");
        if (position > 0) {
            sql.append(" offset ").append(position);
        }

        /**
         * search it in database
         */
        Connection c = null;
        PreparedStatement p = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return null;

            p = c.prepareStatement(sql.toString());

            int order = 1;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object o = args[i];

                    setParameter(p, order++, o);
                }
            }

            r = p.executeQuery();
            if (r.next()) {
                return (T) r.getObject(1);
            }

        } catch (Exception e) {

            log.error(sql.toString() + toString(args), e);

        } finally {
            close(r, p, c);
        }

        return null;
    }

    /**
     * get the list of the col
     * 
     * @param table
     * @param col
     * @param where
     * @param args
     * @param orderby
     * @param s
     * @param n
     * @return List<T>
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(String table, String col, String where, Object[] args, String orderby, int s, int n, String db) {

        /**
         * create the sql statement
         */
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(col).append(" from ").append(table);

        if (where != null) {
            sql.append(" where ").append(where);
        }
        if (orderby != null) {
            sql.append(" ").append(orderby);
        }
        sql.append(" limit ").append(n);
        if (s > 0) {
            sql.append(" offset ").append(s);
        }

        /**
         * search it in database
         */
        Connection c = null;
        PreparedStatement p = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return null;

            p = c.prepareStatement(sql.toString());

            int order = 1;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object o = args[i];

                    setParameter(p, order++, o);
                }
            }

            r = p.executeQuery();
            List<T> list = new ArrayList<T>();
            while (r.next()) {
                list.add((T) r.getObject(1));
            }
            return list;

        } catch (Exception e) {

            log.error(sql.toString() + toString(args), e);

        } finally {
            close(r, p, c);
        }

        return null;
    }

    /**
     * refill the bean from json
     */
    public boolean fromJSON(JSONObject jo) {
        return false;
    }

    /**
     * serialize the bean to json
     */
    public boolean toJSON(JSONObject jo) {
        if (extra != null && extra.size() > 0 && jo != null) {
            for (String name : extra.keySet()) {
                Object o = extra.get(name);
                if (o == null || name.endsWith("_obj")) {
                    continue;
                }

                jo.put(name, o);
            }

            return true;
        }
        return false;
    }

    /**
     * To int.
     * 
     * @param v
     *            the v
     * @return the int
     */
    public static int toInt(Object v) {
        return toInt(v, 0);
    }

    /**
     * To string.
     * 
     * @param arr
     *            the arr
     * @return the string
     */
    public static String toString(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (arr != null) {
            int len = arr.length;
            for (int i = 0; i < len; i++) {
                if (i > 0) {
                    sb.append(" ");
                }

                sb.append(Integer.toHexString((int) arr[i] & 0xff));
            }
        }

        return sb.append("]").toString();
    }

    /**
     * To string.
     * 
     * @param arr
     *            the arr
     * @return the string
     */
    public static String toString(Object[] arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (arr != null) {
            int len = arr.length;
            for (int i = 0; i < len; i++) {
                if (i > 0) {
                    sb.append(",");
                }

                Object o = arr[i];
                if (o == null) {
                    sb.append("null");
                } else if (o instanceof Integer) {
                    sb.append(o);
                } else if (o instanceof Date) {
                    sb.append("Date(").append(o).append(")");
                } else if (o instanceof Long) {
                    sb.append(o);
                } else if (o instanceof Float) {
                    sb.append(o);
                } else if (o instanceof Double) {
                    sb.append(o);
                } else if (o instanceof Boolean) {
                    sb.append("Bool(").append(o).append(")");
                } else {
                    sb.append("\"").append(o).append("\"");
                }
            }
        }

        return sb.append("]").toString();
    }

    /**
     * convert the v to long data
     * 
     * @param v
     * @return long
     */
    public static long toLong(Object v) {
        return toLong(v, 0);
    }

    /**
     * convert the v to long, if failed using defaultValue
     * 
     * @param v
     * @param defaultValue
     * @return long
     */
    public static long toLong(Object v, long defaultValue) {
        if (v != null) {
            if (v instanceof Long) {
                return (Long) v;
            }
            if (v instanceof Integer) {
                return (Integer) v;
            }

            if (v instanceof Float) {
                return (long) ((Float) v).floatValue();
            }
            if (v instanceof Double) {
                return (long) ((Double) v).doubleValue();
            }
            String s = v.toString();
            if (X.EMPTY.equals(s)) {
                return defaultValue;
            }
            try {
                if (s.indexOf(".") > 0) {
                    return (long) toFloat(s);
                }
                return Long.parseLong(s);
            } catch (Exception e) {
                log.error(e);

                StringBuilder sb = new StringBuilder();
                s = s.trim();
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (c >= '0' && c <= '9') {
                        sb.append(c);
                    } else {
                        break;
                    }
                }
                try {
                    return Long.parseLong(sb.toString());
                } catch (Exception e1) {
                    log.error(e1);
                }

            }
        }
        return defaultValue;
    }

    protected static <T extends Bean> T load(String table, String where, Object[] args, String orderby, Class<T> clazz, String db) {
        try {
            T b = (T) clazz.newInstance();

            if (load(table, where, args, orderby, b, db)) {
                return b;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

//    @Override
//    public boolean load(String where, Object o) {
//        /**
//         * not support
//         */
//        return false;
//    }

//    /**
//     * @deprecated
//     */
//    @Override
//    public String getDisplay() {
//        /**
//         * not support
//         */
//        return null;
//    }

    /**
     * set the extra value
     * 
     * @param name
     * @param value
     */
    public void set(String name, Object value) {
        if (extra == null) {
            extra = new HashMap<String, Object>();
        }

        extra.put(name, value);

    }

    /**
     * get the extra value by name from map <br>
     * the name can be : "name" <br>
     * "name.subname" to get the value in sub-map <br>
     * "name.subname[i]" to get the value in sub-map array <br>
     * 
     * @param name
     * @return Object
     */
    public Object get(Object name) {
        if (extra == null) {
            return null;
        }

        if (extra.containsKey(name)) {
            return extra.get(name);
        }

        // name = name.subname[0]
        String[] ss = name.toString().split("[.]");
        if (ss.length > 1) {

            Object o = extra;
            for (String s : ss) {
                if (o instanceof Map) {
                    // .name
                    Map m = (Map) o;
                    if (m.containsKey(s)) {
                        o = m.get(s);
                        if (o == null) {
                            return null;
                        }
                    } else {
                        // .name[1]
                        String[] ss1 = s.split("[\\[\\]]");
                        if (ss1.length > 1) {
                            o = m.get(ss1[0]);
                            if (o != null && o instanceof List) {
                                List l1 = (List) o;
                                int i = Bean.toInt(ss1[1]);
                                if (i >= 0 && i < l1.size()) {
                                    o = l1.get(i);
                                }
                            }
                        } else {
                            return null;
                        }
                    } // end of "containKey"
                } else {
                    return null;
                } // end of if "map"
            }
            return o;
        }
        return null;
    }

    @Override
    public int size() {
        return extra == null ? 0 : extra.size();
    }

    @Override
    public boolean isEmpty() {
        return extra == null ? true : extra.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return extra == null ? false : extra.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return extra == null ? false : extra.containsValue(value);
    }

    @Override
    public Object put(String key, Object value) {
        set(key, value);
        return value;
    }

    @Override
    public Object remove(Object key) {
        return extra == null ? null : extra.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {

        if (extra == null) {
            extra = new HashMap<String, Object>();
        }
        extra.putAll(m);
    }

    @Override
    public void clear() {
        if (extra != null) {
            extra.clear();
        }

    }

    @Override
    public Set<String> keySet() {
        return extra == null ? null : extra.keySet();
    }

    @Override
    public Collection<Object> values() {
        return extra == null ? null : extra.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return extra == null ? null : extra.entrySet();
    }

    /**
     * by default, get integer from the map
     * 
     * @param name
     * @return int
     */
    public int getInt(String name) {
        return toInt(get(name));
    }

    /**
     * by default, get long from the map
     * 
     * @param name
     * @return long
     */
    public long getLong(String name) {
        return toLong(get(name));
    }

    /**
     * by default, get the string from the map
     * 
     * @param name
     * @return String
     */
    public String getString(String name) {
        Object o = get(name);
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            return (String) o;
        } else {
            return o.toString();
        }
    }

    /**
     * by default, get the float from the map
     * 
     * @param name
     * @return float
     */
    public float getFloat(String name) {
        return toFloat(get(name));
    }

    /**
     * by default, get the double from the map
     * 
     * @param name
     * @return double
     */
    public double getDouble(String name) {
        return toDouble(get(name));
    }

    /**
     * get all extra value
     * 
     * @return Map<String, Object>
     */
    public Map<String, Object> getAll() {
        return extra;
    }

    /**
     * remove all extra value
     */
    public void removeAll() {
        if (extra != null) {
            extra.clear();
        }
    }

    /**
     * remove value by names
     * 
     * @param names
     */
    public void remove(String... names) {
        if (extra != null && names != null) {
            for (String name : names) {
                extra.remove(name);
            }
        }
    }

    private Map<String, Object> extra = null;

    /**
     * used to create SQL "where" conditions
     * 
     * @author joe
     * 
     */
    public final static class W {

        public static final int OP_EQ = 0;
        public static final int OP_GT = 1;
        public static final int OP_GT_EQ = 2;
        public static final int OP_LT = 3;
        public static final int OP_LT_EQ = 4;
        public static final int OP_LIKE = 5;
        public static final int OP_NEQ = 7;
        public static final int OP_NONE = 8;

        private static final int AND = 9;
        private static final int OR = 10;

        List<W> wlist = new ArrayList<W>();

        List<Entity> elist = new ArrayList<Entity>();

        int cond = AND;

        /**
         * clone a new W
         * <p>
         * return a new W
         * 
         * @return W
         */
        public W copy() {
            W w = new W();
            w.cond = cond;

            for (W w1 : wlist) {
                w.wlist.add(w1.copy());
            }

            for (Entity e : elist) {
                w.elist.add(e.copy());
            }

            return w;
        }

        /**
         * size of the W
         * 
         * @return int
         */
        public int size() {
            int size = elist == null ? 0 : elist.size();
            for (W w : wlist) {
                size += w.size();
            }
            return size;
        }

        transient Object[] args;

        /**
         * create args for the SQL "where"
         * <p>
         * return the Object[]
         * 
         * @return Object[]
         */
        public Object[] args() {
            if (args == null && (elist.size() > 0 || wlist.size() > 0)) {
                List<Object> l1 = new ArrayList<Object>();

                args(l1);

                args = l1.toArray(new Object[l1.size()]);
            }

            return args;
        }

        private void args(List<Object> list) {
            for (Entity e : elist) {
                e.args(list);
            }

            for (W w1 : wlist) {
                w1.args(list);
            }
        }

        public String toString() {
            return elist == null ? X.EMPTY : (elist.toString() + "=>{" + where() + ", " + Bean.toString(args()) + "}");
        }

        public List<Entity> getAll() {
            return elist;
        }

        private transient String where;

        /**
         * create the SQL "where"
         * 
         * @return String
         */
        public String where() {
            if (where == null && (elist.size() > 0 || wlist.size() > 0)) {
                StringBuilder sb = new StringBuilder();
                for (Entity e : elist) {
                    if (sb.length() > 0) {
                        if (e.cond == AND) {
                            sb.append(" and ");
                        } else if (e.cond == OR) {
                            sb.append(" or ");
                        }
                    }

                    sb.append(e.where());
                }

                for (W w : wlist) {
                    if (sb.length() > 0) {
                        if (w.cond == AND) {
                            sb.append(" and ");
                        } else if (w.cond == OR) {
                            sb.append(" or ");
                        }
                    }

                    sb.append(" (").append(w.where()).append(") ");
                }

                where = sb.toString();
            }

            return where;
        }

        /**
         * create a empty
         * 
         * @return W
         */
        public static W create() {
            return new W();
        }

        transient String orderby;

        /**
         * set the order by, as "order by xxx desc, xxx"
         * 
         * @param orderby
         * @return W
         */
        public W order(String orderby) {
            this.orderby = orderby;
            return this;
        }

        /**
         * get the order by
         * 
         * @return String
         */
        public String orderby() {
            return orderby;
        }

        /**
         * set the sql and parameter
         * 
         * @param sql
         * @param v
         * @return W
         */
        public W set(String sql, Object v) {
            return and(sql, v, W.OP_NONE);
        }

        /**
         * set the name and parameter with "and" and "EQ" conditions
         * 
         * @param name
         * @param v
         * @return W
         */
        public W and(String name, Object v) {
            return and(name, v, W.OP_EQ);
        }

        /**
         * set and "and (...)" conditions
         * 
         * @param w
         * @return W
         */
        public W and(W w) {
            w.cond = AND;
            wlist.add(w);
            return this;
        }

        /**
         * set a "or (...)" conditions
         * 
         * @param w
         * @return W
         */
        public W or(W w) {
            w.cond = OR;
            wlist.add(w);
            return this;
        }

        /**
         * set the namd and parameter with "op" conditions
         * 
         * @param name
         * @param v
         * @param op
         * @return W
         */
        public W and(String name, Object v, int op) {
            where = null;
            args = null;

            elist.add(new Entity(name, v, op, AND));
            return this;
        }

        /**
         * set name and parameter with "or" and "EQ" conditions
         * 
         * @param name
         * @param v
         * @return W
         */
        public W or(String name, Object v) {
            return or(name, v, W.OP_EQ);
        }

        /**
         * set the name and parameter with "or" and "op" conditions
         * 
         * @param name
         * @param v
         * @param op
         * @return W
         */
        public W or(String name, Object v, int op) {
            where = null;
            args = null;

            elist.add(new Entity(name, v, op, OR));

            return this;
        }

        /**
         * copy the name and parameter from a JSON, with "and" and "op"
         * conditions
         * 
         * @param jo
         * @param op
         * @param names
         * @return W
         */
        public W copy(JSONObject jo, int op, String... names) {
            if (jo != null && names != null && names.length > 0) {
                for (String name : names) {
                    if (jo.has(name)) {
                        String s = jo.getString(name);
                        if (s != null && !"".equals(s)) {
                            and(name, s, op);
                        }
                    }
                }
            }

            return this;
        }

        /**
         * copy the value in jo, the format of name is: ["name",
         * "table field name"]
         * 
         * @param jo
         * @param op
         * @param names
         * @return W
         */
        public W copy(JSONObject jo, int op, String[]... names) {
            if (jo != null && names != null && names.length > 0) {
                for (String name[] : names) {
                    if (name.length > 1) {
                        if (jo.has(name[0])) {
                            String s = jo.getString(name[0]);
                            if (s != null && !"".equals(s)) {
                                and(name[1], s, op);
                            }
                        }
                    } else if (jo.has(name[0])) {
                        String s = jo.getString(name[0]);
                        if (s != null && !"".equals(s)) {
                            and(name[0], s, op);
                        }
                    }
                }
            }

            return this;
        }

        /**
         * copy the name and int parameter from the JSON, with "and" and "op"
         * conditions
         * 
         * @param jo
         * @param op
         * @param names
         * @return W
         */
        public W copyInt(JSONObject jo, int op, String... names) {
            if (jo != null && names != null && names.length > 0) {
                for (String name : names) {
                    if (jo.has(name)) {
                        String s = jo.getString(name);
                        if (s != null && !"".equals(s)) {
                            and(name, toInt(s), op);
                        }
                    }
                }
            }

            return this;
        }

        /**
         * copy the value of jo, the format of name is: ["name",
         * "table field name"]
         * 
         * @param jo
         * @param op
         * @param names
         * @return W
         */
        public W copyInt(JSONObject jo, int op, String[]... names) {
            if (jo != null && names != null && names.length > 0) {
                for (String name[] : names) {
                    if (name.length > 1) {
                        if (jo.has(name[0])) {
                            String s = jo.getString(name[0]);
                            if (s != null && !"".equals(s)) {
                                and(name[1], toInt(s), op);
                            }
                        }
                    } else if (jo.has(name[0])) {
                        String s = jo.getString(name[0]);
                        if (s != null && !"".equals(s)) {
                            and(name[0], toInt(s), op);
                        }
                    }
                }
            }

            return this;
        }

        /**
         * create a new W with name and parameter, "and" and "EQ" conditions
         * 
         * @param name
         * @param v
         * @return W
         */
        public static W create(String name, Object v) {
            W w = new W();
            w.elist.add(new Entity(name, v, OP_EQ, AND));
            return w;
        }

        public static class Entity {
            String name;
            Object value;
            int op;
            int cond;

            private List<Object> args(List<Object> list) {
                if (value != null) {
                    if (value instanceof Object[]) {
                        for (Object o : (Object[]) value) {
                            list.add(o);
                        }
                    } else {
                        list.add(value);
                    }
                }

                return list;
            }

            public Entity copy() {
                return new Entity(name, value, op, cond);
            }

            private String where() {
                StringBuilder sb = new StringBuilder();
                sb.append(name);
                switch (op) {
                case OP_EQ: {
                    sb.append("=?");
                    break;
                }
                case OP_GT: {
                    sb.append(">?");
                    break;
                }
                case OP_GT_EQ: {
                    sb.append(">=?");
                    break;
                }
                case OP_LT: {
                    sb.append("<?");
                    break;
                }
                case OP_LT_EQ: {
                    sb.append("<=?");
                    break;
                }
                case OP_LIKE: {
                    sb.append(" like ?");
                    break;
                }
                case OP_NEQ: {
                    sb.append(" <> ?");
                    break;
                }
                }

                return sb.toString();
            }

            public String getName() {
                return name;
            }

            public Object getValue() {
                return value;
            }

            public int getOp() {
                return op;
            }

            transient String tostring;

            public String toString() {
                if (tostring == null) {
                    StringBuilder s = new StringBuilder(name);
                    switch (op) {
                    case OP_EQ: {
                        s.append("=");
                        break;
                    }
                    case OP_GT: {
                        s.append(">");
                        break;
                    }
                    case OP_GT_EQ: {
                        s.append(">=");
                        break;
                    }
                    case OP_LT: {
                        s.append("<");
                        break;
                    }
                    case OP_LT_EQ: {
                        s.append("<=");
                        break;
                    }
                    case OP_NEQ: {
                        s.append("<>");
                        break;
                    }
                    case OP_LIKE: {
                        s.append(" like ");
                    }
                    }
                    s.append(value);

                    tostring = s.toString();
                }
                return tostring;
            }

            private Entity(String name, Object v, int op, int cond) {
                this.name = name;
                this.op = op;
                this.cond = cond;

                if (op == OP_LIKE) {
                    this.value = "%" + v + "%";
                } else {
                    this.value = v;
                }
            }
        }
    }

    public static boolean exists(DBObject query, Class<? extends Bean> t) {
        String collection = getCollection(t);
        if (collection != null) {
            return Bean.load(collection, query) != null;
        }
        return false;
    }

}
