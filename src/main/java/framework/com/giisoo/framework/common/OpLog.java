/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.giisoo.core.bean.*;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.framework.utils.Shell;
import com.giisoo.framework.web.Language;
import com.mongodb.BasicDBObject;

/**
 * Operation Log
 * 
 * @author yjiang
 * 
 */
@DBMapping(collection = "gi_oplog")
public class OpLog extends Bean {

    private static final long serialVersionUID = 1L;

    public static final int TYPE_INFO = 0;
    public static final int TYPE_WARN = 1;
    public static final int TYPE_ERROR = 2;

    /**
     * Removes the.
     * 
     * @return the int
     */
    public static int remove() {
        return Bean.delete(null, null, OpLog.class);
    }

    /**
     * Cleanup.
     * 
     * @param max
     *            the max
     * @param min
     *            the min
     * @return the int
     */
    public static int cleanup(int max, int min) {
        Beans<OpLog> bs = load((BasicDBObject) null, 0, 1);
        int total = bs.getTotal();
        if (total >= max) {

            // TODO
            Bean.delete(new BasicDBObject().append("created", new BasicDBObject().append("$lt", System.currentTimeMillis() - 5 * X.ADAY)), OpLog.class);

            // long created = Bean.getOne("created", null, null,
            // "order by created desc", min, OpLog.class);
            // if (created > 0) {
            // int i = Bean.delete("created <?", new Object[] { created },
            // OpLog.class);
            //
            // if (i > 0) {
            // OpLog.log("cleanup", "cleanup log: " + i, null);
            // }
            // return i;
            // }
        }

        return 0;

    }

    /**
     * Load.
     * 
     * @param w
     *            the w
     * @param offset
     *            the offset
     * @param limit
     *            the limit
     * @return the beans
     */
    public static Beans<OpLog> load(BasicDBObject query, BasicDBObject order, int offset, int limit) {
        return Bean.load(query, order, offset, limit, OpLog.class);
    }

    public static Beans<OpLog> load(BasicDBObject query, int offset, int limit) {
        return load(query, new BasicDBObject().append("created", -1), offset, limit);
    }

    /**
     * Log.
     * 
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @return the int
     */
    public static int log(String op, String brief, String message) {
        return log(op, brief, message, -1, null);
    }

    /**
     * Log.
     * 
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int log(String op, String brief, String message, long uid, String ip) {
        return log(X.EMPTY, op, brief, message, uid, ip);
    }

    /**
     * Log.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @return the int
     */
    public static int log(String module, String op, String brief, String message) {
        return log(module, op, brief, message, -1, null);
    }

    /**
     * @deprecated
     * @param op
     * @param message
     * @return
     */
    public static int log(String op, String message) {
        return info("default", op, message, -1, null);
    }

    /**
     * Log.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param message
     *            the message
     * @return the int
     */
    public static int log(Class<?> module, String op, String message) {
        return info(module.getName(), op, message, -1, null);
    }

    /**
     * Log.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int log(String module, String op, String brief, String message, long uid, String ip) {
        return info(SystemConfig.s("node", X.EMPTY), module, op, brief, message, uid, ip);
    }

    /**
     * Log.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int log(Class<?> module, String op, String brief, String message, long uid, String ip) {
        return info(SystemConfig.s("node", X.EMPTY), module.getName(), op, brief, message, uid, ip);
    }

    /**
     * Log.
     * 
     * @param system
     *            the system
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int log(String system, Class<?> module, String op, String brief, String message, long uid, String ip) {
        return info(system, module.getName(), op, brief, message, uid, ip);
    }

    /**
     * Log.
     * 
     * @param system
     *            the system
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int log(String system, String module, String op, String brief, String message, long uid, String ip) {
        return info(system, module, op, brief, message, uid, ip);
    }

    public String getId() {
        return this.getString(X._ID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Exportable#output(java.lang.String,
     * java.lang.Object[], java.util.zip.ZipOutputStream)
     */
    public JSONObject output(String where, Object[] args, ZipOutputStream out) {
        int s = 0;
        Beans<OpLog> bs = Bean.load(where, args, null, s, 10, OpLog.class);

        JSONObject jo = new JSONObject();
        JSONArray arr = new JSONArray();
        int count = 0;

        while (bs != null && bs.getList() != null && bs.getList().size() > 0) {
            for (OpLog d : bs.getList()) {
                JSONObject j = new JSONObject();
                d.toJSON(j);

                j.convertStringtoBase64();

                arr.add(j);

                count++;
            }
            s += bs.getList().size();
            bs = Bean.load(where, args, null, s, 10, OpLog.class);

        }

        jo.put("list", arr);
        jo.put("total", count);

        return jo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Exportable#input(net.sf.json.JSONArray,
     * java.util.zip.ZipFile)
     */
    public int input(JSONArray list, ZipFile in) {
        int count = 0;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            JSONObject jo = list.getJSONObject(i);
            jo.convertBase64toString();

            if (jo.has("id")) {
                String id = jo.getString("id");
                long created = jo.getLong("created");
                count += Bean.insertOrUpdate("tbloplog", "id=? and created=?", new Object[] { id, created }, V.create().copy(jo, "id", "system", "module", "op", "ip", "brief", "message").copyInt(jo,
                        "type", "uid").copyLong(jo, "created"), null);
            }
        }
        return count;
    }

    public User getUser() {
        if (!this.containsKey("user_obj")) {
            this.set("user_obj", User.loadById(this.getLong("uid")));
        }
        return (User) this.get("user_obj");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Exportable#load(java.lang.String,
     * java.lang.Object[], int, int)
     */
    public Beans<OpLog> load(String where, Object[] args, int s, int n) {
        return Bean.load(where, args, "order by created", s, n, OpLog.class);
    }

    public String getExportableId() {
        return getId();
    }

    public String getExportableName() {
        return this.getString("message");
    }

    public long getExportableUpdated() {
        return this.getLong("created");
    }

    /**
     * Info.
     * 
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @return the int
     */
    public static int info(String op, String brief, String message) {
        return info(op, brief, message, -1, null);
    }

    /**
     * Info.
     * 
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int info(String op, String brief, String message, long uid, String ip) {
        return info(X.EMPTY, op, brief, message, uid, ip);
    }

    /**
     * Info.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @return the int
     */
    public static int info(String module, String op, String brief, String message) {
        return info(module, op, brief, message, -1, null);
    }

    /**
     * Info.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @return the int
     */
    public static int info(Class<?> module, String op, String brief, String message) {
        return info(module.getName(), op, brief, message, -1, null);
    }

    /**
     * Info.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int info(String module, String op, String brief, String message, long uid, String ip) {
        return info(SystemConfig.s("node", X.EMPTY), module, op, brief, message, uid, ip);
    }

    /**
     * Info.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int info(Class<?> module, String op, String brief, String message, long uid, String ip) {
        return info(SystemConfig.s("node", X.EMPTY), module.getName(), op, brief, message, uid, ip);
    }

    /**
     * Info.
     * 
     * @param system
     *            the system
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int info(String system, Class<?> module, String op, String brief, String message, long uid, String ip) {
        return info(system, module.getName(), op, brief, message, uid, ip);
    }

    /**
     * Info.
     * 
     * @param system
     *            the system
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int info(String system, String module, String op, String brief, String message, long uid, String ip) {
        return _log(OpLog.TYPE_INFO, system, module, op, brief, message, uid, ip);
    }

    private static int _log(int type, String system, String module, String op, String brief, String message, long uid, String ip) {

        // brief = Language.getLanguage().truncate(brief, 1024);
        // message = Language.getLanguage().truncate(message, 8192);

        long t = System.currentTimeMillis();
        String id = UID.id(t, op, message);
        int i = Bean.insertCollection(V.create("id", id).set("created", t).set("system", system).set("module", module).set("op", op).set("brief", brief).set("message", message).set("uid", uid).set(
                "ip", ip).set("type", type), OpLog.class);

        if (i > 0) {
//            Category.update(system, module, op);

            /**
             * 记录系统日志
             */
            if (SystemConfig.i("logger.rsyslog", 0) == 1) {
                Language lang = Language.getLanguage();
                // 192.168.1.1#系统名称#2014-10-31#ERROR#日志消息#程序名称
                if (type == OpLog.TYPE_INFO) {
                    Shell.log(ip, Shell.Logger.info, lang.get("log.module_" + module), lang.get("log.opt_" + op) + "//" + brief + ", uid=" + uid);
                } else if (type == OpLog.TYPE_ERROR) {
                    Shell.log(ip, Shell.Logger.error, lang.get("log.module_" + module), lang.get("log.opt_" + op) + "//" + brief + ", uid=" + uid);
                } else {
                    Shell.log(ip, Shell.Logger.warn, lang.get("log.module_" + module), lang.get("log.opt_" + op) + "//" + brief + ", uid=" + uid);
                }
            }

//            onChanged("tbloplog", IData.OP_CREATE, "created=? and id=?", new Object[] { t, id });
        }

        return i;
    }

    /**
     * Warn.
     * 
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @return the int
     */
    /**
     * 
     * @param op
     * @param message
     */
    public static int warn(String op, String brief, String message) {
        return warn(op, brief, message, -1, null);
    }

    /**
     * Warn.
     * 
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int warn(String op, String brief, String message, long uid, String ip) {
        return warn(X.EMPTY, op, brief, message, uid, ip);
    }

    /**
     * Warn.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @return the int
     */
    public static int warn(String module, String op, String brief, String message) {
        return warn(module, op, brief, message, -1, null);
    }

    /**
     * Warn.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @return the int
     */
    public static int warn(Class<?> module, String op, String brief, String message) {
        return warn(module.getName(), op, brief, message, -1, null);
    }

    /**
     * Warn.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int warn(String module, String op, String brief, String message, long uid, String ip) {
        return warn(SystemConfig.s("node", X.EMPTY), module, op, brief, message, uid, ip);
    }

    /**
     * Warn.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int warn(Class<?> module, String op, String brief, String message, long uid, String ip) {
        return warn(SystemConfig.s("node", X.EMPTY), module.getName(), op, brief, message, uid, ip);
    }

    /**
     * Warn.
     * 
     * @param system
     *            the system
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int warn(String system, Class<?> module, String op, String brief, String message, long uid, String ip) {
        return warn(system, module.getName(), op, brief, message, uid, ip);
    }

    /**
     * Warn.
     * 
     * @param system
     *            the system
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int warn(String system, String module, String op, String brief, String message, long uid, String ip) {
        return _log(OpLog.TYPE_WARN, system, module, op, brief, message, uid, ip);
    }

    /**
     * Error.
     * 
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @return the int
     */
    /**
     * 
     * @param op
     * @param message
     */
    public static int error(String op, String brief, String message) {
        return error(op, brief, message, -1, null);
    }

    /**
     * Error.
     * 
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int error(String op, String brief, String message, long uid, String ip) {
        return error(X.EMPTY, op, brief, message, uid, ip);
    }

    /**
     * Error.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @return the int
     */
    public static int error(String module, String op, String brief, String message) {
        return error(module, op, brief, message, -1, null);
    }

    /**
     * Error.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @return the int
     */
    public static int error(Class<?> module, String op, String brief, String message) {
        return error(module.getName(), op, brief, message, -1, null);
    }

    /**
     * Error.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int error(String module, String op, String brief, String message, long uid, String ip) {
        return error(SystemConfig.s("node", X.EMPTY), module, op, brief, message, uid, ip);
    }

    /**
     * Error.
     * 
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int error(Class<?> module, String op, String brief, String message, long uid, String ip) {
        return error(SystemConfig.s("node", X.EMPTY), module.getName(), op, brief, message, uid, ip);
    }

    /**
     * Error.
     * 
     * @param system
     *            the system
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int error(String system, Class<?> module, String op, String brief, String message, long uid, String ip) {
        return error(system, module.getName(), op, brief, message, uid, ip);
    }

    /**
     * Error.
     * 
     * @param system
     *            the system
     * @param module
     *            the module
     * @param op
     *            the op
     * @param brief
     *            the brief
     * @param message
     *            the message
     * @param uid
     *            the uid
     * @param ip
     *            the ip
     * @return the int
     */
    public static int error(String system, String module, String op, String brief, String message, long uid, String ip) {
        return _log(OpLog.TYPE_ERROR, system, module, op, brief, message, uid, ip);
    }

    public String getSystem() {
        return this.getString("system");
    }

    public String getModule() {
        return this.getString("module");
    }

    public String getOp() {
        return this.getString("op");
    }

    public String getMessage() {
        return this.getString("message");
    }

}
