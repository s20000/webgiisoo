/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import java.io.*;
import java.util.Map;
import java.util.regex.*;

import javax.jms.JMSException;
import javax.servlet.http.*;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.*;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.TimeStamp;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.X;
import com.giisoo.core.mq.IStub;
import com.giisoo.core.mq.MQ;
import com.giisoo.framework.common.AccessLog;
import com.giisoo.framework.common.User;
import com.giisoo.framework.common.Cluster.Counter;
import com.giisoo.framework.mdc.command.MDCHttpRequest;
import com.giisoo.framework.mdc.command.MDCHttpResponse;
import com.giisoo.framework.web.Model.HTTPMethod;

/**
 * load module, default module
 * 
 * @author yjiang
 * 
 */
public class Controller implements IStub {

    static Log log = LogFactory.getLog(Controller.class);

    static private Controller owner = new Controller();

    /**
     * the configured context path
     */
    public static String PATH;

    /**
     * os info
     */
    public static String OS;

    /**
     * uptime of the app
     */
    public final static long UPTIME = System.currentTimeMillis();

    /**
     * the length of context path, to make the cut faster :-)
     */
    private static int prelen = 0;

    /**
     * resource uri pattern
     */
    private static Pattern transparenturls;

    /**
     * Inits the.
     * 
     * @param conf
     *            the conf
     * @param path
     *            the path
     */
    public static void init(Configuration conf, String path) {
        Controller.PATH = path;
        Controller.prelen = path == null ? 0 : path.length();

        OS = System.getProperty("os.name").toLowerCase() + "_" + System.getProperty("os.version") + "_" + System.getProperty("os.arch");

        Model.HOME = conf.getString("home");

        /**
         * initialize the module
         */
        Module.init(conf);

        if (MQ.init(conf)) {
            /**
             * mq enabled, create a web queue
             */
            if ("true".equals(conf.getString("mq.web.enabled", "true"))) {
                try {
                    MQ.bind("web", owner);
                } catch (JMSException e) {
                    log.error(e.getMessage(), e);
                }
            }

            if ("true".equals(conf.getString("mq.webadmin.enabled", "true"))) {
                /**
                 * create web topic also, to collect load report
                 */
                try {
                    MQ.bind("webadmin", owner, MQ.Mode.TOPIC);
                } catch (JMSException e) {
                    log.error(e.getMessage(), e);
                }
            }

        }
    }

    @SuppressWarnings("serial")
    @Override
    public void onRequest(long seq, String to, String from, String src, final JSONObject header, JSONObject msg, byte[] bb) {
        // TODO Auto-generated method stub

        try {
            // String to = o.length > 0 ? (String) o[0] : null;
            // log.debug(Bean.toString(o));

            /**
             * create mock http request
             */
            Map<String, Object> h1 = new Bean() {

                @SuppressWarnings("unused")
                public Object get(String name) {
                    return header == null ? null : header.get(name);
                }

            };
            MDCHttpRequest req = MDCHttpRequest.create(msg, bb, h1);

            JSONObject r = new JSONObject();

            final JSONObject header2 = new JSONObject();
            Map<String, Object> h2 = new Bean() {

                public void set(String name, Object o) {
                    header2.put(name, o);
                }

            };

            MDCHttpResponse resp = MDCHttpResponse.create(r, h2);

            /**
             * send http request to controller
             */
            Controller.dispatch(msg.getString(X.URI), req, resp, new HTTPMethod(Model.METHOD_MDC));

            /**
             * write back to client
             */
            if (r.size() > 0) {
                JSONObject r1 = new JSONObject();
                r1.put(X.SEQ, msg.getLong(X.SEQ));
                r1.put(X.RESULT, r);
                if (r.has(X.STATE)) {
                    r1.put(X.STATE, r.get(X.STATE));
                } else {
                    r1.put(X.STATE, X.OK);
                }

                MQ.response(seq, src, from, r1, bb, null, null, header2);
            }
        } catch (Exception e) {
            log.error(msg.toString(), e);
        }

    }

    @Override
    public void onResponse(long seq, String to, String from, String src, JSONObject header, JSONObject msg, byte[] attachment) {
        // TODO Auto-generated method stub

    }

    /**
     * Gets the model.
     * 
     * @param uri
     *            the uri
     * @return the model
     */
    public static Model getModel(String uri) {
        return Module.home.loadModel(uri);
    }

    /**
     * Dispatch with context path.
     * 
     * @param uri
     *            the uri
     * @param req
     *            the req
     * @param resp
     *            the resp
     * @param method
     *            the method
     */
    public static void dispatchWithContextPath(String uri, HttpServletRequest req, HttpServletResponse resp, HTTPMethod method) {

        /**
         * check the model in the cache, if exists then do it first to repad
         * response
         */
        // log.debug(uri);

        if (prelen > 0) {
            uri = uri.substring(prelen);
        }

        dispatch(uri, req, resp, method);
    }

    static private String getRemoteHost(HttpServletRequest req) {
        String remote = req.getHeader("X-Forwarded-For");
        if (remote == null) {
            remote = req.getHeader("X-Real-IP");

            if (remote == null) {
                remote = req.getRemoteAddr();
            }
        }

        return remote;
    }

    /**
     * Dispatch.
     * 
     * @param uri
     *            the uri
     * @param req
     *            the req
     * @param resp
     *            the resp
     * @param method
     *            the method
     */
    public static void dispatch(String uri, HttpServletRequest req, HttpServletResponse resp, Model.HTTPMethod method) {

        Counter.add("web", "request", 1);

        TimeStamp t = TimeStamp.create();

        uri = uri.replaceAll("//", "/");

        /**
         * test and load from cache first
         */
        Model mo = Module.home.loadModelFromCache(uri);
        if (mo != null) {

            mo.dispatch(uri, req, resp, method);

            log.info(method + " " + uri + " - " + t.past() + "ms -" + mo.getRemoteHost() + " " + mo);
            V v = V.create("method", method.toString()).set("cost", t.past()).set("sid", mo.sid());
            User u1 = mo.getUser();
            if (u1 != null) {
                v.set("uid", u1.getId()).set("username", u1.get("name"));
            }
            AccessLog.create(mo.getRemoteHost(), uri, v);

            // Counter.max("web.request.max", t.past(), uri);
            return;
        }

        /**
         * test if the file is resource files
         */
        if (transparenturls == null) {
            String s = Module.home.getTransparent();

            log.debug("transparenturls:" + s);

            if (X.isEmpty(s)) {
                transparenturls = Pattern.compile("^/(css|js|images)/.*$", Pattern.CASE_INSENSITIVE);
            } else {
                transparenturls = Pattern.compile(s);
            }
        }

        if (transparenturls != null) {
            Matcher m = transparenturls.matcher(uri);
            if (m.matches()) {
                /**
                 * load file directly, here do not consider resource cache,
                 * let's do it in proxy layer
                 */
                File f = Module.home.loadResource(uri);
                if (f == null) {
                    /**
                     * load the file from the home
                     */
                    String home = Model.HOME;

                    try {
                        File f1 = new File(home + uri);
                        if (f1.exists() && f1.getCanonicalPath().startsWith(home)) {
                            /**
                             * make sure the file is still under the home folder
                             */
                            f = f1;
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }

                if (f != null) {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        // log.debug("file=" + f.getCanonicalPath());

                        Language lang = Language.getLanguage();
                        String range = req.getHeader("RANGE");

                        String date2 = lang.format(f.lastModified(), "yyyy-MM-dd HH:mm:ss z");

                        if (X.isEmpty(range)) {
                            String date = req.getHeader("If-Modified-Since");
                            if (date != null && date.equals(date2)) {
                                resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

                                log.info(method + " " + uri + " - " + t.past() + "ms " + getRemoteHost(req));

                                return;
                            }
                        }

//                        resp.addHeader("Last-Modified", date2);

                        in = new FileInputStream(f);
                        out = resp.getOutputStream();

                        String mimetype = Model.getMimeType(uri);

                        resp.setContentType(mimetype);
                        resp.setHeader("Last-Modified", date2);
                        resp.setHeader("Content-Length", Long.toString(f.length()));
                        resp.setHeader("Accept-Ranges", "bytes");

                        // RANGE: bytes=2000070-
                        long start = 0;
                        long end = f.length();
                        if (range != null) {
                            String[] ss = range.split("=| |-");
                            if (ss.length > 1) {
                                start = Bean.toLong(ss[1]);
                            }
                            if (ss.length > 2) {
                                end = Bean.toLong(ss[2]);
                            }
                            // Content-Range=bytes 2000070-106786027/106786028
                            resp.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + f.length());

                        }

                        Model.copy(in, out, start, end, false);

                        out.flush();
                    } catch (Exception e) {
                        log.error(uri, e);
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                log.error(e);
                            }
                        }
                    }
                } else {
                    log.warn("not found the file = " + uri);
                }
                // TODO
                log.info(method + " " + uri + " - " + t.past() + "ms " + getRemoteHost(req));

                return;
            } // end of matches
        } // end of transparent

        if ("/".equals(uri) || uri.endsWith("/")) {
            uri += "index";
        }

        /**
         * looking for the model
         */
        try {
            /**
             * load model from the modules
             */
            mo = getModel(uri);
            if (mo != null) {
                mo.dispatch(uri, req, resp, method);

                log.info(method + " " + uri + " - " + t.past() + "ms -" + mo.getRemoteHost() + " " + mo);
                V v = V.create("method", method.toString()).set("cost", t.past()).set("sid", mo.sid());
                User u1 = mo.getUser();
                if (u1 != null) {
                    v.set("uid", u1.getId()).set("username", u1.get("name"));
                }
                AccessLog.create(mo.getRemoteHost(), uri, v);

                // Counter.max("web.request.max", t.past(), uri);
                return;
            } else {
                /**
                 * looking for sub path
                 */
                mo = getModel(uri + "/index");
                if (mo != null) {
                    mo.dispatch(uri, req, resp, method);

                    log.info(method + " " + uri + " - " + t.past() + "ms -" + mo.getRemoteHost() + " " + mo);
                    V v = V.create("method", method.toString()).set("cost", t.past()).set("sid", mo.sid());
                    User u1 = mo.getUser();
                    if (u1 != null) {
                        v.set("uid", u1.getId()).set("username", u1.get("name"));
                    }
                    AccessLog.create(mo.getRemoteHost(), uri, v);

                    // Counter.max("web.request.max", t.past(), uri);
                    return;
                } else {
                    /**
                     * get back of the uri, and set the path to the model if
                     * found, and the path instead
                     */
                    int i = uri.lastIndexOf("/");
                    while (i > 0) {
                        String path = uri.substring(i + 1);
                        String u = uri.substring(0, i);
                        mo = getModel(u);
                        if (mo != null) {
                            mo.setPath(path);
                            mo.dispatch(u, req, resp, method);

                            log.info(method + " " + uri + " - " + t.past() + "ms -" + mo.getRemoteHost() + " " + mo);
                            V v = V.create("method", method.toString()).set("cost", t.past()).set("sid", mo.sid());
                            User u1 = mo.getUser();
                            if (u1 != null) {
                                v.set("uid", u1.getId()).set("username", u1.get("name"));
                            }
                            AccessLog.create(mo.getRemoteHost(), uri, v);

                            // Counter.max("web.request.max", t.past(), uri);
                            return;
                        }
                        i = uri.lastIndexOf("/", i - 1);
                    }

                    /**
                     * not found, then using dummymodel instead, and cache it
                     */
                    mo = new DummyModel();
                    mo.module = Module.home;

                    /**
                     * do not put in model cache
                     */
                    // Module.home.modelMap.put(uri, (Class<Model>)
                    // mo.getClass());
                    mo.dispatch(uri, req, resp, method);

                    log.info(method + " " + uri + " - " + t.past() + "ms -" + mo.getRemoteHost() + " " + mo);
                    V v = V.create("method", method.toString()).set("cost", t.past()).set("sid", mo.sid());
                    User u1 = mo.getUser();
                    if (u1 != null) {
                        v.set("uid", u1.getId()).set("username", u1.get("name"));
                    }
                    AccessLog.create(mo.getRemoteHost(), uri, v);

                    // Counter.max("web.request.max", t.past(), uri);
                }
            }

        } catch (Exception e) {
            log.error(uri, e);
        }
    }
}
