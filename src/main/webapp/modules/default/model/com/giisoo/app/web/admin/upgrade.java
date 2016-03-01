/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.configuration.Configuration;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.Config;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.common.Temp;
import com.giisoo.framework.mdc.utils.IP;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Module;
import com.giisoo.framework.web.Path;

/**
 * web api: /admin/upgrade
 * <p>
 * used to manage auto-upgrade
 * 
 * @author joe
 *
 */
public class upgrade extends Model {

    /**
     * Edits the.
     */
    @Path(path = "edit", login = true, access = "access.config.admin")
    public void edit() {
        Configuration conf = Config.getConfig();

        log.debug("upgrade.edit....");

        if (method.isPost()) {

            SystemConfig.setConfig(conf.getString("node") + ".upgrade.framework.enabled", "on".equals(this.getString("upgrade_enabled")) ? "true" : "false");

            SystemConfig.setConfig(conf.getString("node") + ".upgrade.framework.url", this.getString("upgrade_url"));

            SystemConfig.setConfig(conf.getString("node") + ".upgrade.framework.modules", this.getString("upgrade_modules"));

            this.set(X.MESSAGE, lang.get("save_success"));

        }

        this.set("node", conf.getString("node"));
        this.set("upgrade_enabled", SystemConfig.s(conf.getString("node") + ".upgrade.framework.enabled", "false"));
        this.set("upgrade_url", SystemConfig.s(conf.getString("node") + ".upgrade.framework.url", ""));
        this.set("upgrade_modules", SystemConfig.s(conf.getString("node") + ".upgrade.framework.modules", ""));

        this.show("/admin/upgrade.edit.html");

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.Model#onGet()
     */
    @Override
    public void onGet() {
        ver();
    }

    /**
     * Ver.
     */
    @Path(path = "ver")
    public void ver() {

        String modules = this.getString("modules");

        JSONObject jo = new JSONObject();
        jo.put("release", Module.load("default").getVersion());
        jo.put("build", Module.load("default").getBuild());

        if (modules != null) {
            String[] ss = modules.split(",");
            for (String s : ss) {
                if (!X.isEmpty(s)) {
                    Module m = Module.load(s);
                    if (m != null) {
                        JSONObject j = new JSONObject();
                        j.put("version", m.getVersion());
                        j.put("build", m.getBuild());
                        jo.put(s, j);
                    }
                }
            }
        }

        log.debug("request: " + modules + ", response:" + jo.toString());

        this.response(jo);

    }

    /**
     * Gets the.
     */
    @Path(path = "get")
    public void get() {
        Configuration conf = Config.getConfig();

        String name = new StringBuilder("webgiisoo_").append(Module.load("default").getVersion()).append("_").append(Module.load("default").getBuild()).append(".zip").toString();
        String modules = this.getString("modules");

        log.debug("modules=" + modules);
        StringBuilder sb = new StringBuilder(name);
        if (modules != null) {
            String[] ss = modules.split(",");
            for (String s : ss) {
                if (!X.isEmpty(s)) {
                    Module m = Module.load(s);
                    if (m != null) {
                        sb.append("_").append(s).append(".").append(m.getVersion()).append(".").append(m.getBuild());
                    }
                }
            }
        }

        File file = Temp.get(UID.id("upgrade", sb.toString()), name);
        try {
            StringBuilder getmodules = new StringBuilder("frameword;default");

            if (!file.exists()) {
                file.getParentFile().mkdirs();

                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));

                zip(out, new File(Model.HOME + "/modules/default"), "modules/");

                zip(out, new File(Model.HOME + "/WEB-INF/lib"), "WEB-INF/");

                if (!X.isEmpty(modules)) {
                    String[] ss = modules.split(",");
                    for (String s : ss) {
                        Module m = Module.load(s);
                        if (m != null) {
                            getmodules.append(";").append(s).append(":").append(m.getVersion()).append(".").append(m.getBuild());

                            File f = new File(Model.HOME + "/modules/" + s);
                            zip(out, f, "modules/");
                        }
                    }
                }

                out.close();
            }

            InputStream in = new FileInputStream(file);
            this.setContentType(Model.getMimeType(".zip"));
            Model.copy(in, this.getOutputStream(), true);

            String ip = this.getRemoteHost();
            String remote = IP.getPlace(ip);

            OpLog.info("upgrade", "download", "modules=" + getmodules.toString() + ", host=" + ip + ", remote=" + remote, null);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private void zip(ZipOutputStream out, File f, String path) {
        try {
            if (f.isDirectory()) {
                ZipEntry e = new ZipEntry(path + f.getName() + "/");
                out.putNextEntry(e);
                out.closeEntry();
                File[] list = f.listFiles();
                if (list != null && list.length > 0) {
                    for (File f1 : list) {
                        zip(out, f1, path + f.getName() + "/");
                    }
                }
            } else {
                ZipEntry e = new ZipEntry(path + f.getName());
                out.putNextEntry(e);
                InputStream in = new FileInputStream(f);
                Model.copy(in, out, false);
                in.close();
                out.closeEntry();
            }
        } catch (Exception e) {
            log.error(f.getAbsolutePath(), e);
        }
    }

}
