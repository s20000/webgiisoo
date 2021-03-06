/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.X;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.User;
import com.giisoo.framework.utils.Shell;
import com.giisoo.framework.web.*;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.mongodb.DB;

/**
 * web api: /admin/system
 * <p>
 * used to control the "system"
 * 
 * @author joe
 *
 */
public class system extends Model {

    /**
     * Restart.
     */
    @Path(path = "init", login = true, access = "access.config.admin", log = Model.METHOD_POST)
    public void init() {
        JSONObject jo = new JSONObject();
        User me = this.getUser();
        String pwd = this.getString("pwd");

        if (me.validate(pwd)) {
            jo.put("state", "ok");

            new WorkerTask() {

                @Override
                public String getName() {
                    return "init";
                }

                @Override
                public void onExecute() {

                    // drop all tables
                    java.sql.Connection c = null;
                    Statement stat = null;
                    ResultSet r = null;

                    try {
                        c = Bean.getConnection();
                        DatabaseMetaData d = c.getMetaData();
                        r = d.getTables(null, null, null, new String[] { "TABLE" });
                        while (r.next()) {
                            String name = r.getString("table_name");

                            ResultSetMetaData rm = r.getMetaData();
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < rm.getColumnCount(); i++) {
                                sb.append(rm.getColumnName(i + 1) + "=" + r.getString(i + 1)).append(",");
                            }
                            log.warn("table=" + sb.toString());
                            stat = c.createStatement();
                            stat.execute("drop table " + name);
                            stat.close();
                            stat = null;
                        }

                        // drop all collections
                        DB d1 = Bean.getDB();
                        d1.dropDatabase();

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        Bean.close(r, stat, c);
                    }

                    // drop all collection

                    System.exit(0);
                }

                @Override
                public void onFinish() {

                }

            }.schedule(1000);
        } else {
            jo.put("state", "fail");
            jo.put("message", lang.get("invalid.passwd"));
        }

        this.response(jo);
    }

    /**
     * Restart.
     */
    @Path(path = "restart", login = true, access = "access.config.admin", log = Model.METHOD_POST)
    public void restart() {

        JSONObject jo = new JSONObject();
        User me = this.getUser();
        String pwd = this.getString("pwd");

        if (me.validate(pwd)) {
            jo.put("state", "ok");

            new WorkerTask() {

                @Override
                public String getName() {
                    return "restart";
                }

                @Override
                public void onExecute() {
                    System.exit(0);
                }

                @Override
                public void onFinish() {

                }

            }.schedule(1000);
        } else {
            jo.put("state", "fail");
            jo.put("message", lang.get("invalid.passwd"));
        }

        this.response(jo);
    }

    /**
     * clone a new system as me
     */
    @Path(path = "clone", login = true, access = "access.config.admin", log = Model.METHOD_POST)
    public void clone0() {
        if (method.isPost()) {
            String host = this.getString("host");
            int port = this.getInt("port");
            String user = this.getString("user");
            String passwd = this.getString("password");

            JSONObject jo = new JSONObject();

            if (task == null) {
                task = new CloneTask(host, port, user, passwd);
                task.schedule(0);
            }

            List<String> list = task.get();
            if (list != null) {
                StringBuilder sb = new StringBuilder();
                for (String s : list) {
                    sb.append("<p>").append(s).append("</p>");
                }
                jo.put(X.MESSAGE, sb.toString());
            }

            if (task.done) {
                jo.put("done", 1);
                task = null;
            }
            jo.put(X.STATE, 200);
            this.response(jo);
        } else {
            this.show("/admin/system.clone.html");
        }
    }

    static CloneTask task = null;

    public static class CloneTask extends WorkerTask implements Shell.IPrint {

        String host;
        int port;
        String user;
        String passwd;
        boolean done = false;
        String os = "centos";

        Session session = null;

        List<String> lines = new ArrayList<String>();

        public synchronized List<String> get() {
            if (lines.size() == 0 && !done) {
                try {
                    this.wait(10000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }

            if (lines.size() > 0) {
                List<String> l1 = lines;
                lines = new ArrayList<String>();
                return l1;
            }
            return null;
        }

        public CloneTask(String host, int port, String user, String passwd) {
            this.host = host;
            this.port = port;
            this.user = user;
            this.passwd = passwd;
        }

        public synchronized void print(String line) {
            lines.add(line);
            this.notifyAll();
        }

        @Override
        public void onExecute() {

            try {

                JSch jsch = new JSch();

                session = jsch.getSession(user, host, port);
                session.setPassword(passwd);

                UserInfo ui = new UserInfo() {
                    public void showMessage(String message) {
                        print(message);
                    }

                    public boolean promptYesNo(String message) {
                        print(message + ", Yes/No?");
                        return true;
                    }

                    @Override
                    public String getPassphrase() {
                        return null;
                    }

                    @Override
                    public String getPassword() {
                        return null;
                    }

                    @Override
                    public boolean promptPassphrase(String arg0) {
                        return false;
                    }

                    @Override
                    public boolean promptPassword(String arg0) {
                        return false;
                    }

                };

                session.setUserInfo(ui);
                session.connect(30000); // making a connection with timeout.

                String line = run("uname -a");

                if (line == null) {
                    throw new Exception("unknown OS");
                }

                if (line.indexOf("Ubuntu") > 0) {
                    os = "ubuntu";
                }

                delete(new File("/opt/tmp/webgiisoo/"));

                /**
                 * copy the file to /tmp, and replace the $marco
                 */
                print("Preparing jdk ...");
                String jdk = Shell.run("echo $JAVA_HOME").trim();
                if (X.isEmpty(jdk)) {
                    throw new Exception("no set JAVA_HOME, please set it in /etc/profile");
                }
                copy(new File(jdk), "/opt/tmp/webgiisoo/", false);

                /**
                 * copy tomcat
                 */
                print("Preparing tomcat ...");
                String tomcat = Shell.run("echo $TOMCAT_HOME").trim();
                if (X.isEmpty(tomcat)) {
                    throw new Exception("no set TOMCAT_HOME, please set it in /etc/profile");
                }
                copy(new File(tomcat), "/opt/tmp/webgiisoo/", false, new String[] { "temp", "logs", "work" });

                /**
                 * copy webgiisoo
                 */
                print("Preparing webgiisoo ...");
                copy(new File(Model.HOME), "/opt/tmp/webgiisoo/", false);

                /**
                 * copy dbinit.sql
                 */
                File s1 = Module.home.getFile("/admin/clone/dbinit.sql");
                copy(s1, "/opt/tmp/webgiisoo/dbinit.sql");

                /**
                 * copy scp.sh
                 */
                s1 = Module.home.getFile("/admin/clone/scp.shell");
                copy(s1, "/opt/tmp/webgiisoo/scp.sh", new String[] { "\\$HOST", host }, new String[] { "\\$PORT", Integer.toString(port) }, new String[] { "\\$USER", user });

                Shell.run("chmod ugo+x /opt/tmp/webgiisoo/scp.sh");
                Shell.run("/opt/tmp/webgiisoo/scp.sh", null, this);

                /**
                 * run install shell on remote
                 */
                File f = Module.home.getFile("/admin/clone/install." + os);
                if (f == null) {
                    throw new Exception("can not find the file [install." + os + "] in /admin/clone/");
                }
                BufferedReader cmd = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

                try {
                    String c = cmd.readLine();

                    while (c != null) {
                        c = c.trim();
                        if (c.startsWith("#")) {
                            print("<label><g>" + c + "</g></label>");
                        } else if (!X.isEmpty(c)) {
                            run(c);
                        }
                        c = cmd.readLine();
                    }

                } finally {

                    if (cmd != null) {
                        cmd.close();
                    }

                }

            } catch (Exception e) {

                log.error(e.getMessage(), e);
                print("<r>" + e.getMessage() + "</r>");

            } finally {

                if (session != null) {
                    session.disconnect();
                }

            }
        }

        private void copy(File f, String target, String[]... replacements) {
            BufferedReader in = null;
            PrintStream out = null;

            try {
                new File(target).getParentFile().mkdirs();
                out = new PrintStream(new FileOutputStream(target));
                in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

                String line = in.readLine();
                while (line != null) {
                    if (replacements != null) {
                        for (String[] ss : replacements) {
                            if (ss.length == 2) {
                                line = line.replaceAll(ss[0], ss[1]);
                            }
                        }
                    }
                    out.println(line);
                    line = in.readLine();
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }

                if (out != null) {
                    out.close();
                }
            }

        }

        private void copy(File f, String target, boolean debug) {
            copy(f, target, debug, null);
        }

        private void copy(File f, String target, boolean debug, String[] excepts) {

            if (target.endsWith("/")) {
                target += f.getName();
            } else {
                target += "/" + f.getName();
            }

            if (f.isDirectory()) {
                File t = new File(target);
                if (t.exists()) {
                    delete(t);
                }
                t.mkdirs();

                if (excepts != null) {
                    for (String s : excepts) {
                        if (s.equals(f.getName())) {
                            return;
                        }
                    }
                }

                File[] list = f.listFiles();
                if (list != null && list.length > 0) {
                    for (File f1 : list) {
                        copy(f1, target, debug, excepts);
                    }
                }
            } else {
                // copy
                File t = new File(target);
                t.getParentFile().mkdirs();

                if (debug) {
                    log.debug("copying " + f.getName());
                }

                try {
                    OutputStream out = new FileOutputStream(t);
                    InputStream in = new FileInputStream(f);

                    Model.copy(in, out, true);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

        }

        private void delete(File f) {
            if (f.isDirectory()) {
                for (File f1 : f.listFiles()) {
                    delete(f1);
                }
            }
            f.delete();
        }

        private String run(String cmd) throws Exception {
            OutputStream stdin = null;
            BufferedReader stdout = null;
            BufferedReader stderr = null;
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            try {
                stdin = channel.getOutputStream();

                if (cmd.startsWith("sudo ")) {
                    channel.setCommand(cmd);
                } else {
                    channel.setCommand("sudo -S " + cmd);
                }
                channel.connect(3000);
                stdin.write((passwd + "\n").getBytes());
                stdin.flush();

                stdout = new BufferedReader(new InputStreamReader(channel.getInputStream()));
                String line = stdout.readLine();
                String last = null;
                while (line != null) {
                    print(line);
                    last = line;
                    line = stdout.readLine();
                }

                return last;
            } finally {
                if (stdin != null) {
                    stdin.close();
                }
                if (stdout != null) {
                    stdout.close();
                }
                if (stderr != null) {
                    stderr.close();
                }
                if (channel != null) {
                    channel.disconnect();
                }
            }
        }

        @Override
        public void onFinish() {
            done = true;
        }

    }

}
