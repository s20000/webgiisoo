/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.X;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.*;
import com.giisoo.framework.common.Repo.Entity;
import com.giisoo.framework.web.*;

public class module extends Model {

    private static String ROOT = "/tmp/modules/";

    /**
     * Adds the.
     */
    @Path(path = "add", login = true, access = "access.config.admin", log = Model.METHOD_POST | Model.METHOD_GET)
    public void add() {

        String url = this.getString("url");
        Entity e = Repo.loadByUri(url);

        JSONObject jo = new JSONObject();
        if (e != null) {
            try {
                ZipInputStream in = new ZipInputStream(e.getInputStream());

                /**
                 * store all entry in temp file
                 */
                String temp = Integer.toString(Bean.millis2Date(System.currentTimeMillis()));
                String root = Model.HOME + "/modules/" + temp + "/";

                ZipEntry z = in.getNextEntry();
                byte[] bb = new byte[4 * 1024];
                while (z != null) {
                    File f = new File(root + z.getName());

                    // log.info("name:" + z.getName() + ", " +
                    // f.getAbsolutePath());
                    if (z.isDirectory()) {
                        f.mkdirs();
                    } else {
                        if (!f.exists()) {
                            f.getParentFile().mkdirs();
                        }

                        FileOutputStream out = new FileOutputStream(f);
                        int len = in.read(bb);
                        while (len > 0) {
                            out.write(bb, 0, len);
                            len = in.read(bb);
                        }

                        out.close();
                    }

                    z = in.getNextEntry();
                }

                Module m = Module.load(temp);
                File f = new File(root);
                File dest = new File(Model.HOME + File.separator + "modules" + File.separator + m.getName());
                if (dest.exists()) {
                    delete(dest);
                }

                /**
                 * merge WEB-INF and depends lib
                 * 
                 */
                boolean restart = m.merge();

                /**
                 * move the temp to target dest
                 */
                f.renameTo(dest);

                Module.init(m);

                jo.put("result", "ok");

                if (restart) {
                    jo.put("message", lang.get("auto_restarting"));
                } else {
                    jo.put("message", lang.get("required_restart"));
                }

                /**
                 * delete the old file in repo
                 */
                e.delete();

                if (restart) {
                    new WorkerTask() {

                        @Override
                        public void onExecute() {
                            log.info("WEB-INF has been merged, need to restart");
                            System.exit(0);
                        }

                    }.schedule(5000);
                }
            } catch (Exception e1) {
                log.error(e.toString(), e1);

                /**
                 * the file is bad, delete it from the repo
                 */
                e.delete();

                jo.put("result", "fail");
                jo.put("message", "invalid module package");
            } finally {
                e.close();
            }
        } else {
            jo.put("result", "fail");
            jo.put("message", "entity not found in repo for [" + url + "]");
        }
        this.response(jo);

    }

    /**
     * Index.
     */
    @Path(login = true, access = "access.config.admin")
    public void index() {

        List<Module> actives = new ArrayList<Module>();
        Module m = Module.home;
        while (m != null) {
            actives.add(m);
            m = m.floor();
        }

        this.set("actives", actives);

        this.set("list", Module.getAll());

        this.show("/admin/module.index.html");

    }

    /**
     * Download.
     */
    @Path(path = "download", login = true, access = "access.config.admin")
    public void download() {
        String name = this.getString("name");

        /**
         * zip module
         */
        Module m = Module.load(name);
        String file = ROOT + name + ".zip";
        File f = m.zipTo(Model.HOME + file);
        if (f != null && f.exists()) {

            this.set("f", f);
            this.set("link", file);

            this.show("/admin/module.download.html");
            return;
        } else {
            this.set(X.MESSAGE, lang.get("message.fail"));
            index();
        }
    }

    /**
     * Disable.
     */
    @Path(path = "disable", login = true, access = "access.config.admin", log = Model.METHOD_POST | Model.METHOD_GET)
    public void disable() {
        String name = this.getString("name");

        Module m = Module.load(name);
        m.setEnabled(false);

        index();
    }

    /**
     * Enable.
     */
    @Path(path = "enable", login = true, access = "access.config.admin", log = Model.METHOD_POST | Model.METHOD_GET)
    public void enable() {
        String name = this.getString("name");

        Module m = Module.load(name);
        m.setEnabled(true);

        index();
    }

    /**
     * Delete.
     */
    @Path(path = "delete", login = true, access = "access.config.admin", log = Model.METHOD_POST | Model.METHOD_GET)
    public void delete() {
        String name = this.getString("name");
        Module m = Module.load(name);

        m.delete();

        index();
    }

    @SuppressWarnings("unused")
    private boolean validate(FileItem file) {
        return false;
    }
}