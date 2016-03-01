/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import java.io.File;
import java.io.FileInputStream;

import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Module;
import com.giisoo.framework.web.Path;

/**
 * web api: /repository
 * <p>
 * used to get jar library in auto-upgrade component
 * 
 * @author joe
 * 
 */
public class repository extends Model {

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.Model#onGet()
     */
    @Override
    public void onGet() {
        String e = Module.home.get("repository.enabled");
        if ("true".equals(e)) {
            this.notfound();
            return;
        }

        try {
            String libs = new File(Model.HOME + "/WEB-INF/lib/").getCanonicalPath();
            File f = new File(Model.HOME + "/WEB-INF/lib/" + this.path);

            if (f.exists()) {

                /**
                 * copy file
                 */
                if (f.getCanonicalPath().startsWith(libs)) {
                    this.setContentType(Model.getMimeType(this.path));
                    try {
                        copy(new FileInputStream(f), resp.getOutputStream());
                        return;
                    } catch (Exception e1) {
                        log.error(f.getAbsolutePath(), e1);
                    }
                }
            } else {
                Module m = Module.home;
                while (m != null) {
                    f = new File(m.getPath() + "/model/" + this.path);

                    /**
                     * copy file
                     */
                    if (f.exists() && f.getCanonicalPath().startsWith(m.getPath())) {

                        this.setContentType(Model.getMimeType(this.path));
                        try {
                            copy(new FileInputStream(f), resp.getOutputStream());
                            return;
                        } catch (Exception e1) {
                            log.error(f.getAbsolutePath(), e1);
                        }
                    }

                    m = m.floor();
                }
            }
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
        }

        this.notfound();
    }

    /**
     * List.
     */
    @Path(path = "list")
    public void list() {
        String e = Module.home.get("repository.enabled");
        if ("true".equals(e)) {
            this.notfound();
            return;
        }

        StringBuilder sb = new StringBuilder();
        File libs = new File(Model.HOME + "/WEB-INF/lib/");
        for (File f : libs.listFiles()) {
            if (f.getName().endsWith(".jar")) {
                if (sb.length() > 0)
                    sb.append(";");
                sb.append(f.getName());
            }
        }

        String modules = this.getString("module");

        if (modules != null) {
            String[] ss = modules.split("[;,]");
            for (String s : ss) {
                libs = new File(Model.HOME + "/modules/" + s + "/model/");
                for (File f : libs.listFiles()) {
                    if (f.getName().endsWith(".jar")) {
                        if (sb.length() > 0)
                            sb.append(";");
                        sb.append(f.getName());
                    }
                }
            }
        }
        this.print(sb.toString());
    }

}
