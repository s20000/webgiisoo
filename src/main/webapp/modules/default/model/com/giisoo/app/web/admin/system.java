/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Set;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.User;
import com.giisoo.framework.web.*;
import com.mongodb.DB;

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
                    Connection c = null;
                    Statement stat = null;
                    ResultSet r = null;

                    try {
                        c = Bean.getConnection();
                        DatabaseMetaData d = c.getMetaData();
                        r = d.getTables(null, null, null, new String[] { "TABLE" });
                        while (r.next()) {
                            String name = r.getString(1);

                            ResultSetMetaData rm = r.getMetaData();
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < rm.getColumnCount(); i++) {
                                sb.append(rm.getColumnName(i + 1) + "=" + r.getString(i + 1));
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

}
