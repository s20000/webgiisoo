/**
 * Copyright (C) 2010 Gifox Networks
 * 
 * @project mms
 * @author jjiang
 * @date 2010-10-23
 */
package com.giisoo.core.db;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.X;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.conf.Config;
import com.giisoo.core.worker.WorkerTask;

// TODO: Auto-generated Javadoc
/**
 * The Class DB.
 */
public class DB {

    static Log log = LogFactory.getLog(DB.class);

    private static class BackupTask extends WorkerTask {

        String filename;
        IBackupCallback cb;
        int progress = 0;
        String db;
        int total = 1;
        PrintStream out = null;
        ZipOutputStream zip = null;

        BackupTask(String filename, IBackupCallback cb) {
            this.filename = filename;
            this.cb = cb;
            try {
                zip = new ZipOutputStream(new FileOutputStream(filename));
                zip.putNextEntry(new ZipEntry("db.json"));
                out = new PrintStream(zip);
            } catch (Exception e) {
                log.error(filename, e);
            }
        }

        private int progress() {
            return 100 * progress / total;
        }

        @Override
        public String getName() {
            return "db.backup.task";
        }

        @Override
        public void onExecute() {
            Connection c = null;
            Statement stat = null;
            ResultSet r = null;

            try {
                if (X.isEmpty(db)) {
                    c = getConnection();
                } else {
                    c = getConnection(db);
                }
                if (c == null)
                    return;

                DatabaseMetaData dm = c.getMetaData();
                r = dm.getTables(null, null, "%", new String[] { "TABLE" });
                List<String> tables = new ArrayList<String>();
                while (r.next()) {
                    tables.add(r.getString(3));
                }
                r.close();
                r = null;
                for (String t : tables) {
                    stat = c.createStatement();
                    r = stat.executeQuery("select count(*) t from " + t);
                    r.next();
                    total += r.getInt("t");
                    r.close();
                    r = null;
                    stat.close();
                    stat = null;
                }

                if (cb != null) {
                    cb.onProgress(filename, "backup", progress());
                }

                for (String t : tables) {
                    backup(t, c, out);

                    if (cb != null) {
                        cb.onProgress(filename, "backup", progress());
                    }
                }

                zip.closeEntry();
                out.close();
                zip.close();

                progress = total;
                if (cb != null) {
                    cb.onProgress(filename, "backup", progress());
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                Bean.close(r, stat, c);
            }

        }

        @Override
        public void onFinish() {

        }

    }

    public interface IBackupCallback {
        void onProgress(String filename, String op, int progress);
    }

    public static interface IExportCallback {
        void onJSON(String table, JSONObject jo);
    }

    private static class RecoverTask extends WorkerTask {

        IBackupCallback cb;
        BufferedReader reader;
        ZipInputStream zip;
        String db;
        int total = 1;
        int progress = 0;

        RecoverTask(InputStream in, IBackupCallback cb) {
            this.cb = cb;

            try {
                zip = new ZipInputStream(in);
                zip.getNextEntry();
                reader = new BufferedReader(new InputStreamReader(zip));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        @Override
        public String getName() {
            return "db.recover.task";
        }

        @Override
        public void onExecute() {
            Connection c = null;
            Statement stat = null;
            ResultSet r = null;

            try {
                if (X.isEmpty(db)) {
                    c = getConnection();
                } else {
                    c = getConnection(db);
                }
                if (c == null)
                    return;

                DatabaseMetaData dm = c.getMetaData();
                r = dm.getTables(null, null, "%", new String[] { "TABLE" });
                List<String> tables = new ArrayList<String>();
                while (r.next()) {
                    tables.add(r.getString(3));
                }

                r.close();
                r = null;
                for (String t : tables) {
                    stat = c.createStatement();
                    stat.executeUpdate("delete from " + t);
                    stat.close();
                    stat = null;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                Bean.close(r, stat, c);
            }

            if (cb != null) {
                cb.onProgress(null, "recover", progress());
            }

            try {

                recover(reader);

                zip.closeEntry();
                reader.close();
                zip.close();

                total = progress;
                if (cb != null) {
                    cb.onProgress(null, "recover", progress());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        @Override
        public void onFinish() {

        }

        public int progress() {
            return 100 * progress / total;
        }
    }

    /**
     * delete all tables and recover the data from the ziped input stream
     * <p>
     * -1, exception; 0: error; 1: success
     * 
     * @param in
     *            FileInputSteam
     * @return int
     * 
     */
    public static int recover(InputStream in, String db) {

        Connection c = null;
        Statement stat = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = Bean.getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return 0;

            DatabaseMetaData dm = c.getMetaData();
            r = dm.getTables(null, null, "%", new String[] { "TABLE" });
            List<String> tables = new ArrayList<String>();
            while (r.next()) {
                tables.add(r.getString(3));
            }

            r.close();
            r = null;
            for (String t : tables) {
                stat = c.createStatement();
                stat.executeUpdate("delete from " + t);
                stat.close();
                stat = null;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            return -1;
        } finally {
            Bean.close(r, stat, c);
        }

        try {
            ZipInputStream zip = new ZipInputStream(in);
            zip.getNextEntry();
            BufferedReader reader = new BufferedReader(new InputStreamReader(zip));

            recover(reader);

            zip.closeEntry();
            reader.close();
            zip.close();

            return 1;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return 0;
    }

    private static void recover(BufferedReader reader) throws IOException {

        String line = reader.readLine();
        while (line != null) {
            Bean.insertJSON(line);
            line = reader.readLine();
        }
    }

    public static int dropAll(String db) {
        Connection c = null;
        PreparedStatement stat = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            DatabaseMetaData dm = c.getMetaData();
            r = dm.getTables(null, null, "%", new String[] { "TABLE" });
            List<String> tables = new ArrayList<String>();
            while (r.next()) {
                tables.add(r.getString(3));
            }
            r.close();
            r = null;
            for (String t : tables) {
                stat = c.prepareStatement("drop table " + t);
                stat.executeUpdate();
                stat.close();
                stat = null;
            }

            return tables.size();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

    // protected final static int export(String table, String where,
    // Object[] args, IExportCallback cb, String db) {
    //
    // if (table == null || cb == null)
    // return -1;
    //
    // StringBuilder sql = new StringBuilder();
    // sql.append("select * from ").append(table);
    // if (where != null) {
    // sql.append(" where ").append(where);
    // }
    //
    // Connection c = null;
    // PreparedStatement stat = null;
    // ResultSet r = null;
    //
    // try {
    // if (X.isEmpty(db)) {
    // c = getConnection();
    // } else {
    // c = getConnection(db);
    // }
    // if (c == null)
    // return -1;
    //
    // stat = c.prepareStatement(sql.toString());
    // int order = 1;
    // if (args != null) {
    // for (int i = 0; i < args.length; i++) {
    // Object o = args[i];
    //
    // Bean.setParameter(stat, order++, o);
    // }
    // }
    // r = stat.executeQuery();
    //
    // ResultSetMetaData md = r.getMetaData();
    // int len = md.getColumnCount();
    //
    // int count = 0;
    // while (r.next()) {
    // JSONObject jo = new JSONObject();
    // for (int i = 0; i < len; i++) {
    // jo.put(md.getColumnName(i + 1), r.getObject(i + 1));
    // }
    // cb.onJSON(table, jo);
    //
    // count++;
    // }
    //
    // return count;
    // } catch (Exception e) {
    // log.error(table + where + Bean.toString(args), e);
    // } finally {
    // Bean.close(r, stat, c);
    // }
    // return 0;
    // }

    /**
     * recover the database from the input_stream
     * 
     * @param in
     * @param cb
     * @return float
     */
    public static float recover(InputStream in, IBackupCallback cb) {
        new RecoverTask(in, cb).schedule(10);
        return 0;
    }

    /**
     * backup the database to filename
     * 
     * @deprecated
     * @param filename
     * @param cb
     * @return float
     */
    public static float backup(String filename, IBackupCallback cb) {
        new BackupTask(filename, cb).schedule(10);
        return 0;
    }

    /**
     * backup the tables to filename
     * <p>
     * -1: exception, 0: error, 1: success
     * 
     * @param filename
     * @param tables
     * @return int
     */
    public static int backup(String db, String filename, String... tables) {

        Connection c = null;
        Statement stat = null;
        ResultSet r = null;

        try {
            if (X.isEmpty(db)) {
                c = getConnection();
            } else {
                c = getConnection(db);
            }
            if (c == null)
                return -1;

            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(filename));
            zip.putNextEntry(new ZipEntry("db.json"));
            PrintStream out = new PrintStream(zip);

            for (String t : tables) {
                backup(t, c, out);
            }

            zip.closeEntry();
            out.close();
            zip.close();

            return 1;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            Bean.close(r, stat, c);
        }
        return 0;
    }

    private static void backup(String table, ResultSet r, PrintStream out) {
        try {
            ResultSetMetaData md = r.getMetaData();
            int len = md.getColumnCount();

            while (r.next()) {
                JSONObject jo = new JSONObject();
                jo.put("table", table);
                for (int i = 0; i < len; i++) {
                    jo.put(md.getColumnName(i + 1), r.getObject(i + 1));
                }
                jo.convertStringtoBase64();

                out.println(jo.toString());

            }
        } catch (Exception e) {
            log.error(table, e);
        }

    }

    private static void backup(String table, Connection c, PrintStream out) {
        Statement stat = null;
        ResultSet r = null;

        try {
            stat = c.createStatement();
            r = stat.executeQuery("select * from " + table);
            backup(table, r, out);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            Bean.close(r, stat);
        }
    }

    /** The max active number. */
    private static int MAX_ACTIVE_NUMBER = 10;

    /** The max wait time. */
    private static int MAX_WAIT_TIME = 10 * 1000;

    /** The url. */
    private static String URL = "jdbc:mysql://localhost:3306/lud?user=root&password=123456";

    /** The driver. */
    private static String DRIVER = "com.mysql.jdbc.Driver";

    /** The user. */
    private static String USER;

    /** The passwd. */
    private static String PASSWD;

    /** The validation sql. */
    private static String VALIDATION_SQL = "SELECT 1 FROM DUAL";

    /** The ds. */
    private static BasicDataSource ds;

    /** The dss. */
    private static Map<String, BasicDataSource> dss = new TreeMap<String, BasicDataSource>();

    /** The conf. */
    private static Configuration conf;

    /**
     * Inits the.
     */
    public static void init() {
        conf = Config.getConfig();

        if (ds == null && conf.containsKey("db.url")) {
            if (conf.containsKey("db.driver")) {
                DRIVER = conf.getString("db.driver");
            }

            if (conf.containsKey("db.url")) {
                URL = conf.getString("db.url");
            }

            if (conf.containsKey("db.user")) {
                USER = conf.getString("db.user");
            }

            if (conf.containsKey("db.passwd")) {
                PASSWD = conf.getString("db.passwd");
            }

            if (conf.containsKey("db.number")) {
                MAX_ACTIVE_NUMBER = conf.getInt("db.number");
            }

            if (conf.containsKey("db.validation.sql")) {
                VALIDATION_SQL = conf.getString("db.validation.sql");
            }

            ds = new BasicDataSource();
            ds.setDriverClassName(DRIVER);
            ds.setUrl(URL);

            if (USER != null)
                ds.setUsername(USER);

            if (PASSWD != null)
                ds.setPassword(PASSWD);

            ds.setMaxActive(MAX_ACTIVE_NUMBER);
            ds.setDefaultAutoCommit(true);
            ds.setMaxIdle(MAX_ACTIVE_NUMBER);
            ds.setMaxWait(MAX_WAIT_TIME);
            ds.setDefaultAutoCommit(true);
            ds.setDefaultReadOnly(false);
            ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            ds.setValidationQuery(null);// VALIDATION_SQL);
            ds.setPoolPreparedStatements(true);

        }

    }

    /**
     * Gets the driver.
     * 
     * @return the driver
     */
    public static String getDriver() {
        return shortName(DRIVER);
    }

    /**
     * Gets the driver.
     * 
     * @param name
     *            the name
     * @return the driver
     */
    public static String getDriver(String name) {
        BasicDataSource external = dss.get(name);
        return shortName(external.getDriverClassName());
    }

    /**
     * Short name.
     * 
     * @param name
     *            the name
     * @return the string
     */
    private static String shortName(String name) {
        if (name == null) {
            return X.EMPTY;
        }

        /**
         * get the second string
         */
        int i = name.indexOf(".");
        if (i > 0) {
            name = name.substring(i + 1);
            i = name.indexOf(".");
            if (i > 0) {
                return name.substring(0, i);
            }
        }

        return X.EMPTY;
    }

    /**
     * Inits the.
     * 
     * @param conf
     *            the conf
     */
    public static void init(Properties conf) {
        // if (ds == null) {
        if (conf.containsKey("db.driver")) {
            DRIVER = conf.getProperty("db.driver");
        }

        if (conf.containsKey("db.url")) {
            URL = conf.getProperty("db.url");
        }

        if (conf.containsKey("db.number")) {
            MAX_ACTIVE_NUMBER = Integer.parseInt(conf.getProperty("db.number"));
        }

        if (conf.containsKey("db.validation.sql")) {
            VALIDATION_SQL = conf.getProperty("db.validation.sql");
        }

        ds = new BasicDataSource();
        ds.setDriverClassName(DRIVER);
        ds.setUrl(URL);

        if (USER != null)
            ds.setUsername(USER);

        if (PASSWD != null)
            ds.setPassword(PASSWD);

        ds.setMaxActive(MAX_ACTIVE_NUMBER);
        ds.setMaxIdle(MAX_ACTIVE_NUMBER);
        ds.setMaxWait(MAX_WAIT_TIME);
        ds.setDefaultAutoCommit(true);
        ds.setDefaultAutoCommit(true);
        ds.setDefaultReadOnly(false);
        ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        ds.setValidationQuery(null);// VALIDATION_SQL);
        ds.setPoolPreparedStatements(true);
        // }

    }

    /**
     * Gets the connection.
     * 
     * @return the connection
     * @throws SQLException
     *             the sQL exception
     */
    public static Connection getConnection() throws SQLException {
        if (ds != null) {
            Connection c = ds.getConnection();
            if (c != null) {
                c.setAutoCommit(true);
                return c;
            }
        }

        return null;
    }

    /**
     * Gets the source.
     * 
     * @return the source
     */
    public static DataSource getSource() {
        return ds;
    }

    public static Connection getConnectionByUrl(String url) throws SQLException {
        BasicDataSource external = dss.get(url);
        if (external == null) {

            // String D = conf.getString("db[" + name + "].driver", DRIVER);
            // String EXTERNAL_URL = conf.getString("db[" + name + "].url",
            // URL);
            // int N = conf.getInt("db[" + name + "].conns", MAX_ACTIVE_NUMBER);

            String D = null;
            String[] ss = url.split(":");
            if (ss.length > 2) {
                if (ss[1].equalsIgnoreCase("mysql")) {
                    D = "com.mysql.jdbc.Driver";
                } else if (ss[1].equalsIgnoreCase("postgresql")) {
                    D = "org.postgresql.Driver";
                } else if (ss[1].equalsIgnoreCase("oracle")) {
                    D = "oracle.jdbc.driver.OracleDriver";
                } else if (ss[1].equalsIgnoreCase("sqlserver")) {
                    D = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                }
            }

            if (!X.isEmpty(D)) {
                external = new BasicDataSource();
                external.setDriverClassName(D);

                external.setUrl(url);
                external.setMaxActive(10);
                external.setDefaultAutoCommit(true);
                external.setMaxIdle(10);

                external.setMaxWait(MAX_WAIT_TIME);
                external.setDefaultAutoCommit(true);
                external.setDefaultReadOnly(false);
                external.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                external.setValidationQuery(null);// VALIDATION_SQL);
                external.setPoolPreparedStatements(true);

                dss.put(url, external);
            }
        }

        Connection c = (external == null ? ds.getConnection() : external.getConnection());
        c.setAutoCommit(true);
        return c;
    }

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
        BasicDataSource external = dss.get(name);
        if (external == null) {

            String D = conf.getString("db[" + name + "].driver", DRIVER);
            String EXTERNAL_URL = conf.getString("db[" + name + "].url", URL);
            int N = conf.getInt("db[" + name + "].conns", MAX_ACTIVE_NUMBER);

            external = new BasicDataSource();
            external.setDriverClassName(D);

            external.setUrl(EXTERNAL_URL);
            if (conf.containsKey("db[" + name + "].user")) {
                external.setUsername(conf.getString("db[" + name + "].user"));
            }
            if (conf.containsKey("db[" + name + "].passwd")) {
                external.setUsername(conf.getString("db[" + name + "].passwd"));
            }

            external.setMaxActive(N);
            external.setDefaultAutoCommit(true);
            external.setMaxIdle(N);
            external.setMaxWait(MAX_WAIT_TIME);
            external.setDefaultAutoCommit(true);
            external.setDefaultReadOnly(false);
            external.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            external.setValidationQuery(null);// VALIDATION_SQL);
            external.setPoolPreparedStatements(true);

            dss.put(name, external);
        }

        Connection c = (external == null ? ds.getConnection() : external.getConnection());
        c.setAutoCommit(true);
        return c;
    }

    /** The mysql. */
    private static Boolean mysql = null;

    /**
     * Checks if is mysql.
     * 
     * @return true, if is mysql
     */
    // public static boolean isMysql() {
    // if (mysql == null) {
    // if ("mysql".equals(getDriver())) {
    // mysql = Boolean.TRUE;
    // } else {
    // mysql = Boolean.FALSE;
    // }
    // }
    // return mysql;
    // }
}
