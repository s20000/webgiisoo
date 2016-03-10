/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.giisoo.app.web.admin.setting;
import com.giisoo.app.web.admin.sync;
import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.Config;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.core.db.DB;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.AccessLog;
import com.giisoo.framework.common.Cluster;
import com.giisoo.framework.common.Host;
import com.giisoo.framework.common.Load;
import com.giisoo.framework.common.Menu;
import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.common.Repo;
import com.giisoo.framework.common.Stat;
import com.giisoo.framework.common.Temp;
import com.giisoo.framework.common.User;
import com.giisoo.framework.mdc.MDCServer;
import com.giisoo.framework.mdc.TConn;
import com.giisoo.framework.mdc.utils.IP;
import com.giisoo.framework.utils.FileUtil;
import com.giisoo.framework.utils.Http;
import com.giisoo.framework.utils.Http.Response;
import com.giisoo.framework.utils.Shell;
import com.giisoo.framework.web.Language;
import com.giisoo.framework.web.LifeListener;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Module;
import com.mongodb.BasicDBObject;

/**
 * startup life listener
 * 
 * @author joe
 * 
 */
public class DefaultListener implements LifeListener {

    public static final DefaultListener owner = new DefaultListener();

    MDCServer mdc = null;
    MDCServer udc = null;

    /**
     * backup
     * 
     * @author joe
     *
     */
    public static class BackupTask extends WorkerTask {

        public static String path() {
            return SystemConfig.s("backup.path", "/opt/backup");
        }

        @Override
        public String getName() {
            return "backup.task";
        }

        @Override
        public void onExecute() {
            Module m = Module.home;
            try {

                String out = path() + "/" + Language.getLanguage().format(System.currentTimeMillis(), "yyyyMMddHHmm");
                new File(out).mkdirs();

                File f = m.getFile("/admin/clone/backup_db.sh");
                Shell.run("chmod ugo+x " + f.getParent() + "/*.sh");

                /**
                 * 1, backup postgresql
                 */
                String url = conf.getString("db.url", null);
                if (!X.isEmpty(url)) {
                    int i = url.indexOf("?");
                    if (i > 0) {
                        url = url.substring(0, i);
                    }
                    i = url.lastIndexOf("/");
                    if (i > 0) {
                        url = url.substring(i + 1);
                    }

                    String r = Shell.run(f.getCanonicalPath() + " " + url + " " + out + "/" + url + ".dmp");
                    log.debug("result: " + r);
                }

                /**
                 * 2, backup mongo
                 */
                f = m.getFile("/admin/clone/backup_mongo.sh");
                url = conf.getString("mongo[prod].url", null);
                if (!X.isEmpty(url)) {
                    String db = conf.getString("mongo[prod].db", "demo");

                    url = url.split(";")[0];
                    int i = url.indexOf(":");
                    if (i > 0) {
                        String host = url.substring(0, i);
                        String port = url.substring(i + 1);

                        Shell.run(f.getCanonicalPath() + " " + host + " " + port + " " + db + " " + out);
                    }
                }

                /**
                 * 3, backup repo
                 */
                f = m.getFile("/admin/clone/backup_tar.sh");
                url = conf.getString("repo.path", null);
                if (!X.isEmpty(url)) {
                    Shell.run(f.getCanonicalPath() + " " + out + "/repo.tar.gz " + url);
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

    };

    private static class NtpTask extends WorkerTask {

        static NtpTask owner = new NtpTask();

        private NtpTask() {
        }

        @Override
        public void onExecute() {
            String ntp = SystemConfig.s("ntp.server", null);
            if (!X.isEmpty((Object) ntp)) {
                try {
                    String r = Shell.run("ntpdate -u " + ntp);
                    OpLog.info("ntp", X.EMPTY, "时钟同步： " + r);
                } catch (Exception e) {
                    OpLog.error("ntp", X.EMPTY, "时钟同步： " + e.getMessage());
                }
            }
        }

        @Override
        public void onFinish() {
            this.schedule(X.AHOUR);
        }
    }

    static Log log = LogFactory.getLog(DefaultListener.class);

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.LifeListener#onStart(org.apache.commons.
     * configuration.Configuration, com.giisoo.framework.web.Module)
     */
    public void onStart(Configuration conf, Module module) {

        /**
         * clean up the old version's jar
         */
        if (cleanup(new File(Model.HOME), new HashMap<String, FileUtil>())) {
            System.exit(0);
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("upgrade.enabled=" + SystemConfig.s(conf.getString("node") + ".upgrade.framework.enabled", "false"));
        }

        if ("true".equals(SystemConfig.s(conf.getString("node") + ".upgrade.framework.enabled", "false"))) {
            UpgradeTask.owner.schedule(X.AMINUTE + (long) (2 * X.AMINUTE * Math.random()));
        }

        // cleanup
        File f = new File(Model.HOME + "/WEB-INF/lib/mina-core-2.0.0-M4.jar");
        if (f.exists()) {
            f.delete();
            System.exit(0);
        }

        IP.init(conf);

        NtpTask.owner.schedule(X.AMINUTE);

        setting.register("system", setting.system.class);
        setting.register("sync", sync.class);
        setting.register("smtp", setting.mail.class);

        V v = V.create().set("status", "running").set("updated", System.currentTimeMillis()).set("ip", IP.myip().toString()).set("started", System.currentTimeMillis()).set("master",
                conf.containsKey("master") && "yes".equals(conf.getString("master")) ? 1 : 0);
        String id = Cluster.update(conf.getString("node", X.EMPTY), Model.HOME, v);

        Cluster.self = Cluster.load(id);

        HeartbeatTask.owner.schedule(X.AMINUTE);
        new CleanupTask(conf).schedule(X.AMINUTE);
        new StatTask().schedule(X.AMINUTE);
        new SitemapTask().schedule(X.AMINUTE);
        new RestartTask().schedule(X.AMINUTE);
        new VersionCheckTask().schedule(X.AMINUTE);

        /**
         * initialize the MDCServer
         */
        String e = SystemConfig.s("mdc.tcp.enabled", "false");
        if ("true".equals(e)) {
            String mdchost = SystemConfig.s("mdc.tcp.host", "0.0.0.0");
            int mdcport = Bean.toInt(SystemConfig.s("mdc.tcp.port", "-1"), -1);

            if (mdcport > 0) {
                if (log.isWarnEnabled()) {
                    log.warn("mdc is starting on [" + mdchost + ":" + mdcport + "]");
                }

                mdc = MDCServer.createTcpServer(mdchost, mdcport);
                mdc.start();

                log.warn("mdc is starting on [" + mdchost + ":" + mdcport + "]");

                udc = MDCServer.createUdpServer(mdchost, mdcport);
                udc.start();

                Host.update(conf.getString("node", X.EMPTY), SystemConfig.s(Model.node() + ".mdc.tcp.domain", null), mdcport);

            } else {
                if (log.isWarnEnabled()) {
                    log.warn("mdc is enabled, but the port is wrong: [" + mdchost + ":" + mdcport + "]");
                }
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("mdc.tcp is diabled!");
            }
        }

        /**
         * check and initialize
         */
        User.checkAndInit();

        new WorkerTask() {

            @Override
            public void onExecute() {
                Bean.repair();
            }

        }.schedule(10);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.LifeListener#onStop()
     */
    public void onStop() {
    }

    public static void runDBScript(File f) throws IOException, SQLException {
        BufferedReader in = null;
        Connection c = null;
        Statement s = null;
        try {
            c = Bean.getConnection();
            if (c != null) {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
                StringBuilder sb = new StringBuilder();
                try {
                    String line = in.readLine();
                    while (line != null) {
                        line = line.trim();
                        if (!"".equals(line) && !line.startsWith("#")) {

                            sb.append(line).append("\r\n");

                            if (line.endsWith(";")) {
                                String sql = sb.toString().trim();

                                try {
                                    s = c.createStatement();
                                    s.executeUpdate(sql);
                                    s.close();
                                } catch (Exception e) {
                                    log.error(sb.toString(), e);
                                }
                                s = null;
                                sb = new StringBuilder();
                            }
                        }
                        line = in.readLine();
                    }

                    String sql = sb.toString().trim();
                    if (!"".equals(sql)) {
                        s = c.createStatement();
                        s.executeUpdate(sql);
                    }
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error(sb.toString(), e);
                    }
                }
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("database not configured !");
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
            Bean.close(s, c);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.LifeListener#upgrade(org.apache.commons.
     * configuration.Configuration, com.giisoo.framework.web.Module)
     */
    public void upgrade(Configuration conf, Module module) {
        if (log.isDebugEnabled()) {
            log.debug(module + " upgrading...");
        }

        /**
         * test database connection has configured?
         */
        try {
            /**
             * test the database has been installed?
             */
            String dbname = DB.getDriver();

            if (!X.isEmpty(dbname) && DB.isConfigured()) {
                /**
                 * initial the database
                 */
                File f = module.loadResource("/install/" + dbname + "/initial.sql", false);
                if (f != null && f.exists()) {
                    String key = module.getName() + ".db.initial." + dbname + "." + f.lastModified();
                    int b = SystemConfig.i(key, 0);
                    if (b == 0) {
                        if (log.isWarnEnabled()) {
                            log.warn("db[" + key + "] has not been initialized! initializing...");
                        }

                        try {
                            runDBScript(f);
                            SystemConfig.setConfig(key, (int) 1);
                            if (log.isWarnEnabled()) {
                                log.warn("db[" + key + "] has been initialized! ");
                            }
                        } catch (Exception e) {
                            if (log.isErrorEnabled()) {
                                log.error(f.getAbsolutePath(), e);
                            }
                        }

                    }
                } else {
                    if (log.isWarnEnabled()) {
                        log.warn("db[" + module.getName() + "." + dbname + "] not exists ! ");
                    }
                }

                f = module.loadResource("/install/" + dbname + "/upgrade.sql", false);
                if (f != null && f.exists()) {
                    String key = module.getName() + ".db.upgrade." + dbname + "." + f.lastModified();
                    int b = SystemConfig.i(key, 0);

                    if (b == 0) {

                        try {
                            runDBScript(f);

                            SystemConfig.setConfig(key, (int) 1);

                            if (log.isWarnEnabled()) {
                                log.warn("db[" + key + "] has been upgraded! ");
                            }
                        } catch (Exception e) {
                            if (log.isErrorEnabled()) {
                                log.error(f.getAbsolutePath(), e);
                            }
                        } finally {
                        }

                    }
                }
            } else {
                if (log.isErrorEnabled()) {
                    log.error("DB was miss configured, please congiure it in [" + Model.GIISOO_HOME + "/webgiisoo/giisoo.properties]");
                }
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("database is not configured!", e);
            }
            return;
        }

        if (Bean.isConfigured()) {
            /**
             * check the menus
             * 
             */
            File f = module.getFile("/install/menu.json");
            if (f != null && f.exists()) {
                BufferedReader reader = null;
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("initialize [" + f.getCanonicalPath() + "]");
                    }

                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        sb.append(line).append("\r\n");
                        line = reader.readLine();
                    }

                    /**
                     * convert the string to json array
                     */
                    JSONArray arr = JSONArray.fromObject(sb.toString());
                    Menu.insertOrUpdate(arr, module.getName());

                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getMessage(), e);
                    }
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            if (log.isErrorEnabled()) {
                                log.error(e);
                            }
                        }
                    }
                }
            }
        } else {
            if (log.isErrorEnabled()) {
                log.error("Mongo was miss configured, please congiure it in [" + Model.GIISOO_HOME + "/webgiisoo/giisoo.properties]");
            }
            return;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.LifeListener#uninstall(org.apache.commons.
     * configuration.Configuration, com.giisoo.framework.web.Module)
     */
    public void uninstall(Configuration conf, Module module) {
        Menu.remove(module.getName());
    }

    /**
     * 
     * @param f
     * @param map
     * @return
     */
    private boolean cleanup(File f, Map<String, FileUtil> map) {
        /**
         * list and compare all jar files
         */
        boolean changed = false;

        if (f.isDirectory()) {
            for (File f1 : f.listFiles()) {
                if (cleanup(f1, map)) {
                    changed = true;
                }
            }
        } else if (f.isFile() && f.getName().endsWith(".jar")) {
            FileUtil f1 = new FileUtil(f);
            String name = f1.getName();

            FileUtil f2 = map.get(name);
            if (f2 == null) {
                map.put(f1.getName(), f1);
            } else {
                FileUtil.R r = f1.compareTo(f2);
                if (r == FileUtil.R.HIGH || r == FileUtil.R.SAME) {
                    // remove f2
                    if (log.isWarnEnabled()) {
                        log.warn("delete duplicated jar file, but low version:" + f2.getFile().getAbsolutePath() + ", keep: " + f2.getFile().getAbsolutePath());
                    }
                    f2.getFile().delete();
                    map.put(name, f1);
                } else if (r == FileUtil.R.LOW) {
                    // remove f1;
                    if (log.isWarnEnabled()) {
                        log.warn("delete duplicated jar file, but low version:" + f1.getFile().getAbsolutePath() + ", keep: " + f1.getFile().getAbsolutePath());
                    }
                    f1.getFile().delete();
                }
            }
        }

        return changed;
    }

    /**
     * @deprecated
     * @param args
     */
    public static void main(String[] args) {
        DefaultListener d = new DefaultListener();
        File f = new File("/home/joe/d/workspace/");
        Map<String, FileUtil> map = new HashMap<String, FileUtil>();
        d.cleanup(f, map);
        System.out.println(map);

    }

    /**
     * @author joe
     *
     */
    private static class HeartbeatTask extends WorkerTask {

        private static HeartbeatTask owner = new HeartbeatTask();

        @Override
        public void onExecute() {
            Cluster.update(Cluster.self.getId(), V.create("updated", System.currentTimeMillis()));

            Cluster.update(new BasicDBObject().append("updated", new BasicDBObject().append("$lt", System.currentTimeMillis() - 2 * X.AMINUTE)), V.create("status", "lost"));

        }

        @Override
        public void onFinish() {
            this.schedule(X.AMINUTE);
        }

    }

    /**
     * check the new version
     * 
     * @author joe
     *
     */
    private static class VersionCheckTask extends WorkerTask {

        @Override
        public String getName() {
            return "versioncheck.task";
        }

        @Override
        public void onExecute() {
            Response res = Http.post("http://www.giisoo.com/version", new String[][] { { "version", Module.load("default").getVersion() }, { "build", Module.load("default").getBuild() } });
            if (res.status == 200 && !X.isEmpty(res.body)) {
                JSONObject jo = JSONObject.fromObject(res.body);
                String ver = jo.getString("version");
                String build = jo.getString("build");
                if (!X.isSame(ver, Module.load("default").getVersion()) || !X.isSame(build, Module.load("default").getBuild())) {
                    SystemConfig.setConfig("notification.enabled", 1);
                    SystemConfig.setConfig("notification.message", Language.getLanguage().get("notification.newbuild") + ": <a href='http://www.giisoo.com' target='_blank'>" + ver + "." + build
                            + "</a>");
                }
            }
        }

    }

    private static class RestartTask extends WorkerTask {

        @Override
        public String getName() {
            return "restart.task";
        }

        @Override
        public void onExecute() {
            if (System.currentTimeMillis() - Model.UPTIME > X.AHOUR) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                int h = c.get(Calendar.HOUR_OF_DAY);
                int point = Bean.toInt(Module.home.get("restart.point"), 2);
                if (h == point) {
                    if (log.isWarnEnabled()) {
                        log.warn("restarting by restart.task");
                    }
                    System.exit(0);
                }
            }
        }

    }

    /**
     * 自动升级类，根據節點配置，支持并行中的单一节点、多模块升级。
     * 
     * @author joe
     * 
     */
    private static class UpgradeTask extends WorkerTask {

        private static UpgradeTask owner = new UpgradeTask();
        private static String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36";

        long interval = X.AMINUTE;

        @Override
        public String getName() {
            return "upgrade.task";
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.giisoo.worker.WorkerTask#onExecute()
         */
        @Override
        public void onExecute() {

            interval = X.AMINUTE;

            Configuration conf = Config.getConfig();

            String url = SystemConfig.s(conf.getString("node") + ".upgrade.framework.url", null);

            if (X.isEmpty(url)) {
                OpLog.log("autoupgrade", "upgrade.framework.url missed", null);
                interval = X.AHOUR;
                return;
            }

            try {
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }

                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(url + "/admin/upgrade/ver?modules=" + getModules());
                get.addHeader("User-Agent", USER_AGENT);

                HttpResponse resp = client.execute(get);
                HttpEntity e = resp.getEntity();
                if (e == null) {
                    // OpLog.log("autoupgrade",
                    // "can not get the ver info from remote");
                    interval = X.AMINUTE;
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(e.getContent(), "utf8"));

                StringBuilder sb = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    sb.append(line).append("\r\n");
                    line = reader.readLine();
                }
                reader.close();
                try {
                    JSONObject jo = JSONObject.fromObject(sb.toString());

                    String release = jo.has("release") ? jo.getString("release") : null;
                    String build = jo.has("build") ? jo.getString("build") : null;
                    if (release == null || build == null) {
                        // OpLog.log("autoupgrade", "response error: " +
                        // sb.toString());
                        interval = X.AMINUTE;
                        return;
                    }

                    if (!checkBuild(jo)) {
                        /**
                         * server has different version or build
                         */

                        get = new HttpGet(url + "/admin/upgrade/get?modules=" + getModules());
                        get.addHeader("User-Agent", USER_AGENT);

                        resp = client.execute(get);
                        e = resp.getEntity();
                        if (e == null) {
                            // OpLog.log("autoupgrade",
                            // "can not get the ver info from remote");
                            interval = X.AMINUTE;
                            return;
                        }

                        try {
                            // TODO, enhancement, during unzip, the system was
                            // shutdown ?!
                            StringBuilder getmodules = new StringBuilder("framework;default");
                            String modules = getModules();
                            if (modules != null) {
                                String[] ss = modules.split(",");
                                for (String s : ss) {
                                    if (!X.isEmpty(s)) {
                                        if (jo.has(s)) {
                                            JSONObject j = jo.getJSONObject(s);
                                            getmodules.append(";").append(s).append(":").append(j.getString("version")).append(".").append(j.getString("build"));
                                        }
                                    }
                                }
                            }

                            OpLog.info("upgrade", "upgrade", "url=" + url + ", modules=" + getmodules.toString() + ", release=" + release + ", build=" + build, null);

                            /**
                             * catch all error avoid "jvm" hot-plug-in issue
                             */
                            ZipInputStream in = new ZipInputStream(e.getContent());
                            ZipEntry e1 = in.getNextEntry();
                            while (e1 != null) {
                                File f = new File(Model.HOME + e1.getName());
                                if (e1.isDirectory()) {
                                    f.mkdirs();
                                } else {
                                    f.getParentFile().mkdirs();
                                    OutputStream out = new FileOutputStream(f);
                                    Model.copy(in, out, false);
                                    out.close();
                                }

                                e1 = in.getNextEntry();
                            }

                            // SystemConfig.setConfig(conf.getString("node")
                            // + ".build", build);
                            // SystemConfig.setConfig(conf.getString("node")
                            // + ".release", release);

                            OpLog.log("autoupgrade", "upgrade success to " + release + "_" + build, null);
                        } catch (Throwable e2) {
                            /**
                             * because of the libary changed, this method may
                             * inaccessiable
                             */
                            log.error(e2.getMessage(), e2);
                        }

                        /**
                         * upgrade success, shutdown the application and let's
                         * appdog to restart it
                         */
                        System.exit(0);
                    } else {
                        interval = X.AHOUR;
                    }
                } catch (Exception e1) {
                    interval = X.AMINUTE;
                    log.error(sb.toString(), e1);
                }
            } catch (Exception e) {
                // OpLog.log("autoupgrade", "upgrade failed");

                interval = X.AMINUTE;
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        private String getModules() {
            return SystemConfig.s(conf.getString("node") + ".upgrade.framework.modules", "");
        }

        private boolean checkBuild(JSONObject jo) {
            String release = jo.getString("release");
            String build = jo.getString("build");

            /**
             * 比较框架的版本和build
             */
            if (!release.equals(Module.load("default").getVersion())) {
                return false;
            }

            if (!build.equals(Module.load("default").getBuild())) {
                return false;
            }

            /**
             * check each modules
             */
            String modules = getModules();
            if (modules != null) {
                String[] ss = modules.split(",");
                for (String s : ss) {
                    if (!X.isEmpty(s)) {
                        if (jo.has(s)) {
                            JSONObject j = jo.getJSONObject(s);
                            Module m = Module.load(s);
                            JSONObject j1 = new JSONObject();
                            j1.put("version", m.getVersion());
                            j1.put("build", m.getBuild());
                            if (!j.equals(j1)) {
                                return false;
                            }
                        }
                    }
                }
            }

            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.giisoo.worker.WorkerTask#onFinish()
         */
        @Override
        public void onFinish() {
            this.schedule(interval);
        }
    }

    private static class StatTask extends WorkerTask {

        @Override
        public String getName() {
            return "stat.task";
        }

        @Override
        public void onExecute() {

            /**
             * stat all user
             */
            Language lang = Language.getLanguage();

            String date = lang.format(System.currentTimeMillis(), "yyyyMMdd");
            float count = User.count(new BasicDBObject("deleted", new BasicDBObject("$ne", 1)).append(X._ID, new BasicDBObject("$gt", 0L)), User.class);
            Stat.insertOrUpdate("user", date, 0L, count);

            /**
             * stat all mdc
             */
            date = lang.format(System.currentTimeMillis(), "yyyyMMddHH");
            count = TConn.count(new BasicDBObject("uid", new BasicDBObject("$gt", 0)).append("updated", new BasicDBObject("$gt", System.currentTimeMillis() - X.AMINUTE * 5)), TConn.class);
            Stat s = Stat.load("mdc", date, 0L);
            if (s == null || s.getCount() < count) {
                Stat.insertOrUpdate("mdc", date, 0L, count);
            }

            /**
             * record "load"
             */
            count = Load.count(new BasicDBObject("name", "mdc"), Load.class);
            s = Stat.load("load", date, 0L);
            if (s == null || s.getCount() < count) {
                Stat.insertOrUpdate("load", date, 0L, count);
            }

        }

        @Override
        public void onFinish() {
            this.schedule(X.AMINUTE);
        }

    }

    @SuppressWarnings("unused")
    private static class SitemapTask extends WorkerTask {

        @Override
        public void onExecute() {

            // get all url
            List<Object> list = AccessLog.distinct();

            if (list != null && list.size() > 0) {
                String name = "sitemap.txt";

                File f = Module.load("default").getFile(name);
                if (f.exists()) {
                    f.delete();
                }

                PrintStream out = null;

                try {
                    out = new PrintStream(new FileOutputStream(f));
                    out.println("/");
                    for (Object u : list) {
                        out.println(u);
                    }
                    out.println();

                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getMessage(), e);
                    }
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            }
        }

        @Override
        public void onFinish() {
            this.schedule(X.ADAY);
        }

    }

    /**
     * clean up the oplog, temp file in Temp
     * 
     * @author joe
     * 
     */
    private static class CleanupTask extends WorkerTask {

        static Log log = LogFactory.getLog(CleanupTask.class);

        String home;

        /**
         * Instantiates a new cleanup task.
         * 
         * @param conf
         *            the conf
         */
        public CleanupTask(Configuration conf) {
            home = conf.getString("home");
        }

        @Override
        public String getName() {
            return "cleanup.task";
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.giisoo.worker.WorkerTask#onExecute()
         */
        @Override
        public void onExecute() {
            try {
                /**
                 * clean up the local temp files
                 */
                int count = 0;
                for (String f : folders) {
                    String path = home + f;
                    count += cleanup(path, X.ADAY);
                }

                /**
                 * clean files in Temp
                 */
                if (!X.isEmpty(Temp.ROOT)) {
                    count += cleanup(Temp.ROOT, X.ADAY);
                }

                /**
                 * clean temp files in tomcat
                 */
                if (!X.isEmpty(Model.GIISOO_HOME)) {
                    // do it
                    count += cleanup(Model.GIISOO_HOME + "/work", X.ADAY);
                    count += cleanup(Model.GIISOO_HOME + "/logs", X.ADAY * 3);
                }
                if (log.isInfoEnabled()) {
                    log.info("cleanup temp files: " + count);
                }

                OpLog.cleanup();

                AccessLog.cleanup();

                /**
                 * cleanup repo
                 */
                Repo.cleanup();

            } catch (Exception e) {
                // eat the exception
            }
        }

        private int cleanup(String path, long expired) {
            int count = 0;
            try {
                File f = new File(path);

                /**
                 * test the file last modified exceed the cache time
                 */
                if (f.isFile() && System.currentTimeMillis() - f.lastModified() > expired) {
                    f.delete();
                    if (log.isInfoEnabled()) {
                        log.info("delete file: " + f.getCanonicalPath());
                    }
                    count++;
                } else if (f.isDirectory()) {
                    File[] list = f.listFiles();
                    if (list != null) {
                        /**
                         * cleanup the sub folder
                         */
                        for (File f1 : list) {
                            count += cleanup(f1.getAbsolutePath(), expired);
                        }
                    }
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
            }

            return count;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.giisoo.worker.WorkerTask#priority()
         */
        @Override
        public int priority() {
            return Thread.MIN_PRIORITY;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.giisoo.worker.WorkerTask#onFinish()
         */
        @Override
        public void onFinish() {
            this.schedule(X.AHOUR);
        }

        static String[] folders = { "/tmp/_cache", "/tmp/_raw" };
    }

}
