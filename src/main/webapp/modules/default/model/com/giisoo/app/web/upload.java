/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import net.sf.json.*;

import org.apache.commons.fileupload.FileItem;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;
import com.giisoo.framework.common.*;
import com.giisoo.framework.web.*;

/**
 * web api：/upload
 * <p>
 * used to upload file and return the file id in file repository, it support
 * "resume“ file upload, the "Content-Range: bytes 0-1024/2048"
 * 
 * @author joe
 * 
 */
public class upload extends Model {

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.Model#onMDC()
     */
    @Override
    public void onMDC() {
        User me = this.getUser();

        if (me != null) {
            String access = Module.home.get("upload.require.access");
            if (access == null || "".equals(access) || me.hasAccess(access)) {
                FileItem file = this.getFile("file");
                store(me.getId(), file, null);
            } else {
                this.set(X.ERROR, X.FAIL201);
                this.put(X.MESSAGE, lang.get("no_access") + ":" + access);
            }
        } else {
            this.set(X.ERROR, X.FAIL201);
            this.put(X.MESSAGE, lang.get("login.required"));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.Model#onPost()
     */
    @Override
    public void onPost() {

        JSONObject jo = new JSONObject();
        User me = this.getUser();

        if (me != null) {
            // String access = Module.home.get("upload.require.access");

            FileItem file = this.getFile("file");
            store(me.getId(), file, jo);

        } else {
            this.set(X.ERROR, X.FAIL201);
            jo.put(X.MESSAGE, lang.get("login.required"));
        }

        // /**
        // * test
        // */
        // jo.put("error", "error");
        this.response(jo);

    }

    private boolean store(long me, FileItem file, JSONObject jo) {
        String tag = this.getString("tag");

        try {
            String range = this.getHeader("Content-Range");
            if (range == null) {
                range = this.getString("Content-Range");
            }
            long position = 0;
            long total = 0;
            String lastModified = this.getHeader("lastModified");
            if (X.isEmpty(lastModified)) {
                lastModified = this.getString("lastModified");
            }
            if (X.isEmpty(lastModified)) {
                lastModified = this.getString("lastModifiedDate");
            }

            if (range != null) {

                // bytes 0-9999/22775650
                String[] ss = range.split(" ");
                if (ss.length > 1) {
                    range = ss[1];
                }
                ss = range.split("-|/");
                if (ss.length == 3) {
                    position = Bean.toLong(ss[0]);
                    total = Bean.toLong(ss[2]);
                }

                // log.debug(range + ", " + position + "/" + total);
            }

            String id = UID.id(me, tag, file.getName(), total, lastModified);

            log.debug("storing, id=" + id + ", name=" + file.getName() + ", tag=" + tag + ", total=" + total + ", last=" + lastModified);

            String share = this.getString("share");
            String folder = this.getString("folder");

            long pos = Repo.store(folder, id, file.getName(), tag, position, total, file.getInputStream(), -1, !"no".equals(share), me);
            if (pos >= 0) {
                if (jo == null) {
                    this.put("url", "/repo/" + id + "/" + file.getName());
                    this.put(X.ERROR, 0);
                    this.put("repo", id);
                    if (total > 0) {
                        this.put("name", file.getName());
                        this.put("pos", pos);
                        this.put("size", total);
                    }
                } else {
                    jo.put("url", "/repo/" + id + "/" + file.getName());
                    jo.put("repo", id);
                    jo.put(X.ERROR, 0);
                    if (total > 0) {
                        jo.put("name", file.getName());
                        jo.put("pos", pos);
                        jo.put("size", total);
                    }
                }

                // Session.load(sid()).set("access.repo." + id, 1).store();
            } else {
                if (jo == null) {
                    this.set(X.ERROR, X.FAIL201);
                    this.put(X.MESSAGE, lang.get("repo.locked"));
                } else {
                    jo.put(X.ERROR, X.FAIL201);
                    jo.put(X.MESSAGE, lang.get("repo.locked"));
                }
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (jo == null) {
                this.set(X.ERROR, X.FAIL401);
                this.put(X.MESSAGE, lang.get(e.getMessage()));
            } else {
                jo.put(X.ERROR, X.FAIL401);
                jo.put(X.MESSAGE, lang.get(e.getMessage()));
            }
        }

        return false;
    }
}
