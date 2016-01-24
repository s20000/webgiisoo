package com.giisoo.app.web.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.framework.common.Cluster;
import com.giisoo.framework.mdc.utils.IP;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;
import com.mongodb.BasicDBObject;

public class cluster extends Model {

    @Path(login = true, access = "access.config.admin")
    @Override
    public void onGet() {

        BasicDBObject q = new BasicDBObject();

        int s = this.getInt("s");
        int n = this.getInt("n", 10, "number.per.page");

        Beans<Cluster> bs = Cluster.load(q, new BasicDBObject().append("node", 1), s, n);

        this.set(bs, s, n);

        this.show("/admin/cluster.index.html");
    }

    @Path(login = true, path = "delete", access = "access.config.admin")
    public void delete() {

        String id = this.getString("id");
        Cluster.remove(id);
        this.print("ok");

    }

    @Path(login = true, path = "traffic", access = "access.config.admin")
    public void traffic() {

    }

    @Path(login = true, path = "add", access = "access.config.admin")
    public void add() {

        if (method.isPost()) {
            if (copy()) {
                onGet();
            }
        }

        String host = this.getString("host");
        String user = this.getString("user");
        String password = this.getString("password");
        int port = this.getInt("port", 22);
        this.set("port", port);
        this.set("host", host);
        this.set("user", user);
        this.set("password", password);
        this.show("/admin/cluster.add.html");

    }

    private boolean copy() {
        String host = this.getString("host");
        int port = this.getInt("port");
        String user = this.getString("user");
        String password = this.getString("password");

        try {

            // Connection conn = new Connection(host, port);

            /* Now connect */

            // conn.connect();

            /*
             * Authenticate. If you get an IOException saying something like
             * "Authentication method password not supported by the server at this stage."
             * then please check the FAQ.
             */

            // boolean isAuthenticated = conn.authenticateWithPassword(user,
            // password);

            // if (isAuthenticated == false)
            // throw new IOException("Authentication failed.");

            /* Create a session */

            // Session sess = conn.openSession();

            // sess.execCommand("uname -a && date && uptime && who");

            System.out.println("Here is some information about the remote host:");

            /*
             * This basic example does not handle stderr, which is sometimes
             * dangerous (please read the FAQ).
             */

            // InputStream stdout = new StreamGobbler(sess.getStdout());

            // BufferedReader br = new BufferedReader(new
            // InputStreamReader(stdout));

            // StringBuilder sb = new StringBuilder();
            // while (true) {
            // String line = br.readLine();
            // if (line == null) {
            // br.close();
            // break;
            // }
            // sb.append(line);
            // }

            /* Show exit status, if available (otherwise "null") */

            // System.out.println("ExitCode: " + sess.getExitStatus());

            String id = Cluster.add(Model.HOME, V.create().set("status", "installing").set("updated", System.currentTimeMillis()).set("ip", host).set("port", port).set("user", user).set("password",
                    password));

            // this.set(X.MESSAGE, sb.toString());
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            this.set(X.ERROR, e.getMessage());

            return false;
        }

    }
}
