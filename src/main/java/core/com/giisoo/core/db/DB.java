/**
 * Copyright (C) 2010 Gifox Networks
 * 
 * @project mms
 * @author jjiang
 * @date 2010-10-23
 */
package com.giisoo.core.db;

import java.sql.*;
import java.util.*;

import javax.sql.DataSource;


import org.apache.commons.configuration.Configuration;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.core.bean.X;
import com.giisoo.core.conf.Config;

// TODO: Auto-generated Javadoc
/**
 * The Class DB.
 */
public class DB {

    static Log log = LogFactory.getLog(DB.class);

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

            String username = null;
            String password = null;
            String D = null;
            String[] ss = url.split(":");
            if (ss.length > 2) {
                if (ss[1].equalsIgnoreCase("mysql")) {
                    D = "com.mysql.jdbc.Driver";
                } else if (ss[1].equalsIgnoreCase("postgresql")) {
                    D = "org.postgresql.Driver";
                } else if (ss[1].equalsIgnoreCase("oracle")) {
                    D = "oracle.jdbc.driver.OracleDriver";
                } else if (ss[1].equalsIgnoreCase("sqlserver") || ss[1].equalsIgnoreCase("microsoft")) {
                    D = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

                    // TODO, user, password
                    int i = url.indexOf("user=");
                    if (i > 0) {
                        int j = url.indexOf("&", i + 1);
                        if (j < 0) {
                            j = url.length();
                        }
                        String[] ss1 = url.substring(i, j).split("=");
                        if (ss1.length == 2) {
                            username = ss1[1];
                        }

                        String url1 = url.substring(0, i - 1);
                        if (j < url.length()) {
                            url = url1 + url.substring(j);
                        } else {
                            url = url1;
                        }
                    }

                    i = url.indexOf("password=");
                    if (i > 0) {
                        int j = url.indexOf("&", i + 1);
                        if (j < 0) {
                            j = url.length();
                        }
                        String[] ss1 = url.substring(i, j).split("=");
                        if (ss1.length == 2) {
                            password = ss1[1];
                        }
                        String url1 = url.substring(0, i - 1);
                        if (j < url.length()) {
                            url = url1 + url.substring(j + 1);
                        } else {
                            url = url1;
                        }
                    }

                }
            }

            log.debug("driver=" + D + ", url=" + url + ", user=" + username + ", password=" + password);

            if (!X.isEmpty(D)) {
                external = new BasicDataSource();
                external.setDriverClassName(D);

                external.setUrl(url);

                if (!X.isEmpty(username)) {
                    external.setUsername(username);
                }
                if (!X.isEmpty(password)) {
                    external.setPassword(password);
                }

                external.setMaxActive(10);
                // external.setDefaultAutoCommit(true);
                external.setMaxIdle(10);

                external.setMaxWait(MAX_WAIT_TIME);
                // external.setDefaultAutoCommit(true);
                // external.setDefaultReadOnly(false);
                // external.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                external.setValidationQuery(null);// VALIDATION_SQL);
                external.setPoolPreparedStatements(true);

                dss.put(url, external);
            }
        }

        Connection c = (external == null ? ds.getConnection() : external.getConnection());
        // c.setAutoCommit(true);
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

            String username = null;
            String password = null;
            String[] ss = EXTERNAL_URL.split(":");
            if (ss.length > 2) {
                if (ss[1].equalsIgnoreCase("mysql")) {
                    D = X.isEmpty(D) ? "com.mysql.jdbc.Driver" : D;
                } else if (ss[1].equalsIgnoreCase("postgresql")) {
                    D = X.isEmpty(D) ? "org.postgresql.Driver" : D;
                } else if (ss[1].equalsIgnoreCase("oracle")) {
                    D = X.isEmpty(D) ? "oracle.jdbc.driver.OracleDriver" : D;
                } else if (ss[1].equalsIgnoreCase("sqlserver") || ss[1].equalsIgnoreCase("microsoft")) {
                    D = X.isEmpty(D) ? "com.microsoft.sqlserver.jdbc.SQLServerDriver" : D;

                    // TODO, user, password
                    int i = EXTERNAL_URL.indexOf("user=");
                    if (i > 0) {
                        int j = EXTERNAL_URL.indexOf("&", i + 1);
                        if (j < 0) {
                            j = EXTERNAL_URL.length();
                        }
                        String[] ss1 = EXTERNAL_URL.substring(i, j).split("=");
                        if (ss1.length == 2) {
                            username = ss1[1];
                        }

                        String url1 = EXTERNAL_URL.substring(0, i - 1);
                        if (j < EXTERNAL_URL.length()) {
                            EXTERNAL_URL = url1 + EXTERNAL_URL.substring(j);
                        } else {
                            EXTERNAL_URL = url1;
                        }
                    }

                    i = EXTERNAL_URL.indexOf("password=");
                    if (i > 0) {
                        int j = EXTERNAL_URL.indexOf("&", i + 1);
                        if (j < 0) {
                            j = EXTERNAL_URL.length();
                        }
                        String[] ss1 = EXTERNAL_URL.substring(i, j).split("=");
                        if (ss1.length == 2) {
                            password = ss1[1];
                        }
                        String url1 = EXTERNAL_URL.substring(0, i - 1);
                        if (j < EXTERNAL_URL.length()) {
                            EXTERNAL_URL = url1 + EXTERNAL_URL.substring(j + 1);
                        } else {
                            EXTERNAL_URL = url1;
                        }
                    }

                }
            }

            int N = conf.getInt("db[" + name + "].conns", MAX_ACTIVE_NUMBER);

            external = new BasicDataSource();
            external.setDriverClassName(D);

            if (!X.isEmpty(username)) {
                external.setUsername(username);
            }
            if (!X.isEmpty(password)) {
                external.setPassword(password);
            }

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
