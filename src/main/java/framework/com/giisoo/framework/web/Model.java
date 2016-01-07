/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.logging.*;
import org.apache.velocity.*;

import com.giisoo.framework.common.*;
import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.TimeStamp;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.framework.mdc.command.*;
import com.giisoo.utils.base.Html;

/**
 * all web api base class
 * 
 * @author yjiang
 * 
 */
public class Model {
    protected static Log log = LogFactory.getLog(Model.class);

    public static String ENCODING = "UTF-8";

    /**
     * request
     */
    protected HttpServletRequest req;

    /**
     * response
     */
    protected HttpServletResponse resp;

    /**
     * language map
     */
    protected Language lang;

    protected HTTPMethod method;

    protected VelocityContext context;

    /**
     * the absolute home path of the application
     */
    public static String HOME;

    /**
     * the home of the tomcat
     */
    public static String TOMCAT_HOME;

    /**
     * session is
     */
    private String sid;

    /**
     * locale of user
     */
    protected String locale;

    /**
     * the uri of request
     */
    protected String uri;

    /**
     * the module of this model
     */
    protected Module module;

    /**
     * the query string of the request
     */
    protected QueryString query;

    /**
     * cache the template by viwename, the template will be override in child
     * module
     */
    private static Map<String, Template> cache = new HashMap<String, Template>();

    public static long UPTIME = System.currentTimeMillis();

    /**
     * contentType will return
     */
    private String contentType;

    protected User login = null;

    // protected long created = System.currentTimeMillis();

    private static final ThreadLocal<Module> _currentmodule = new ThreadLocal<Module>();

    // final protected boolean expired() {
    // long expired = Module.home.getInt("request.expired");
    // if (expired > 0) {
    // return System.currentTimeMillis() - created > expired * 1000;
    // }
    //
    // return false;
    // }

    /**
     * Clean.
     */
    public static void clean() {
        cache.clear();
    }

    /**
     * 
     * @return InputStream
     * @throws IOException
     */
    final public InputStream getInputStream() throws IOException {
        return req.getInputStream();
    }

    /**
     * 
     * @return OutputStream
     * @throws IOException
     */
    final public OutputStream getOutputStream() throws IOException {
        return resp.getOutputStream();
    }

    /**
     * get the locale
     * 
     * @return String
     */
    final protected String getLocale() {
        if (locale == null) {
            locale = this.getString("lang");
            if (locale == null) {
                /**
                 * get the language from the cookie or default setting;
                 */
                locale = getCookie("lang");
                if (locale == null) {
                    // locale = Module._conf.getString("default.locale",
                    // "en_us");
                    //
                    // /**
                    // * get from the default, then set back to the cookie
                    // */
                    // this.addCookie("lang", locale, (int) (X.AYEAR / 1000));
                    locale = Module.home.getLanguage();
                }
            } else {
                /**
                 * get the language from the query, then set back in the cookie;
                 */
                this.addCookie("lang", locale, (int) (X.AYEAR / 1000));
            }
        }

        // System.out.println("lang:" + locale);
        return locale;
    }

    /**
     * Current module.
     * 
     * @return the module
     */
    public static Module currentModule() {
        return _currentmodule.get();
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
    final protected Path dispatch(String uri, HttpServletRequest req, HttpServletResponse resp, HTTPMethod method) {

        // created = System.currentTimeMillis();

        // construct var
        // init
        try {
            _currentmodule.set(module);

            this.req = req;
            this.resp = resp;
            this.method = method;
            this.uri = uri;

            this._multipart = ServletFileUpload.isMultipartContent(req);

            req.setCharacterEncoding(ENCODING);

            /**
             * set default data in model
             */
            this.lang = Language.getLanguage(getLocale());

            // if path exists, then using pathmapping instead
            // log.debug("pathmapping=" + pathmapping);

            if (pathmapping != null) {

                String path = this.path;
                if (X.isEmpty(this.path)) {
                    path = X.NONE;
                }

                Map<String, PathMapping> methods = pathmapping.get(this.method.method);

                // log.debug(this.method + "=>" + methods);

                if (methods != null) {
                    for (String s : methods.keySet()) {

                        /**
                         * catch the exception avoid break the whole block
                         */
                        try {
                            /**
                             * match test in outside first
                             */
                            // log.debug(s + "=>" + this.path);

                            if (path.matches(s)) {

                                /**
                                 * create the pattern
                                 */
                                PathMapping oo = methods.get(s);
                                if (oo != null) {
                                    Pattern p = oo.pattern;
                                    Matcher m1 = p.matcher(path);

                                    /**
                                     * find
                                     */
                                    Object[] params = null;
                                    if (m1.find()) {
                                        /**
                                         * get all the params
                                         */
                                        params = new Object[m1.groupCount()];
                                        for (int i = 0; i < params.length; i++) {
                                            params[i] = m1.group(i + 1);
                                        }
                                    }

                                    Path pp = oo.path;
                                    /**
                                     * check the access and login status
                                     */
                                    if (pp.login()) {
                                        login = this.getUser();
                                        if (login == null) {
                                            /**
                                             * login require
                                             */
                                            if (method.isMdc()) {
                                                this.set(X.STATE, 201);
                                                this.set(X.MESSAGE, "login required");

                                                log.debug("login required: " + pp + ", session: " + this.sid());

                                            } else {
                                                gotoLogin();
                                            }
                                            return pp;
                                        }

                                        if (!X.NONE.equals(pp.access()) && !login.hasAccess(pp.access().split("\\|"))) {
                                            /**
                                             * no access
                                             */
                                            if (method.isMdc()) {
                                                this.put(X.STATE, 201);
                                                this.put(X.MESSAGE, "access deny");
                                            } else {
                                                this.put("lang", lang);
                                                this.deny();
                                            }

                                            OpLog.warn("deny", uri, "requred: " + lang.get(pp.access()), login.getId(), this.getRemoteHost());
                                            return pp;
                                        }
                                    }

                                    /**
                                     * set the "global" attribute for the model
                                     */
                                    switch (this.method.method) {
                                    case METHOD_POST:
                                    case METHOD_GET:
                                        this.put("lang", lang);
                                        this.put(X.URI, uri);
                                        this.put("module", Module.home);
                                        this.put("path", this.path); // set
                                                                     // original
                                                                     // path
                                        this.put("request", req);
                                        this.put("response", resp);
                                        this.set("me", login);
                                        this.set("session", this.getSession());
                                        this.set("system", SystemConfig.getInstance());

                                        createQuery();

                                        if (!validbrowser()) {
                                            notsupport();
                                            return pp;
                                        }

                                        break;
                                    case METHOD_MDC:
                                        break;
                                    }

                                    /**
                                     * invoke the method
                                     */
                                    Method m = oo.method;
                                    // log.debug("invoking: " + m.getName());

                                    try {
                                        m.invoke(this, params);

                                        if ((pp.log() & method.method) > 0) {

                                            boolean error = false;

                                            StringBuilder sb = new StringBuilder();
                                            JSONObject jo = this.getJSON();
                                            jo.remove("password");
                                            jo.remove("pwd");
                                            jo.remove("passwd");

                                            sb.append("<b>IN</b>=").append(jo.toString());
                                            String message = null;
                                            sb.append("; <b>OUT</b>=");
                                            if (context != null) {
                                                if (context.containsKey("jsonstr")) {
                                                    sb.append(context.get("jsonstr"));
                                                } else {
                                                    jo = new JSONObject();
                                                    if (context.containsKey(X.MESSAGE)) {
                                                        jo.put(X.MESSAGE, context.get(X.MESSAGE));
                                                        message = jo.getString(X.MESSAGE);
                                                    }
                                                    if (context.containsKey(X.ERROR)) {
                                                        jo.put(X.ERROR, context.get(X.ERROR));
                                                        message = jo.getString(X.ERROR);
                                                        error = true;
                                                    }
                                                    sb.append(jo.toString());
                                                }
                                            }

                                            if (error) {
                                                OpLog.warn(this.getClass().getName(), path == null ? uri : uri + "/" + path, message, sb.toString(), getUser() == null ? -1 : getUser().getId(), this
                                                        .getRemoteHost());
                                            } else {
                                                OpLog.info(this.getClass().getName(), path == null ? uri : uri + "/" + path, message, sb.toString(), getUser() == null ? -1 : getUser().getId(), this
                                                        .getRemoteHost());
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.error(e.getMessage(), e);

                                        StringBuilder sb = new StringBuilder();
                                        JSONObject jo = this.getJSON();
                                        jo.remove("password");
                                        jo.remove("pwd");
                                        jo.remove("passwd");

                                        sb.append("IN=").append(jo.toString());

                                        sb.append("; OUT=");

                                        StringWriter sw = new StringWriter();
                                        PrintWriter out = new PrintWriter(sw);
                                        e.printStackTrace(out);
                                        sb.append(sw.toString());

                                        OpLog.error(this.getClass().getName(), path == null ? uri : uri + "/" + path, lang.get("oplog.exception"), sb.toString().replaceAll("\r\n", "<br>"),
                                                getUser() == null ? -1 : getUser().getId(), this.getRemoteHost());
                                    }

                                    return pp;
                                }
                            }
                        } catch (Exception e) {
                            log.error(s, e);
                        }
                    }
                }
            } // end of "pathmapping is not null

            /**
             * default handler
             */
            switch (method.method) {
            case METHOD_GET: {
                this.put("lang", lang);
                this.put("uri", uri);
                this.put("module", Module.home);
                this.put("path", path);
                this.put("request", req);
                this.put("response", resp);
                this.set("session", this.getSession());
                this.set("system", SystemConfig.getInstance());
                this.createQuery();

                if (!validbrowser()) {
                    notsupport();
                    return null;
                }

                /**
                 * get the require annotation onGet
                 */
                Method m = this.getClass().getMethod("onGet");
                @SuppressWarnings("deprecation")
                Require require = m.getAnnotation(Require.class);

                /**
                 * test the require login or access ?
                 */
                User me = this.getUser();
                this.set("me", me);
                // log.info(require);
                if (require == null || !require.login() || (me != null && (require.access() == null || X.NONE.equals(require.access()) || me.hasAccess(require.access())))) {

                    onGet();
                } else if (me == null) {
                    /**
                     * the user not logined, the redirect to login
                     */
                    gotoLogin();
                } else {
                    /**
                     * the user logined, but no access, show deny
                     */
                    OpLog.warn("deny", uri, "requred: " + lang.get(require.access()), login == null ? -1 : login.getId(), this.getRemoteHost());

                    deny();
                }
                break;
            }
            case METHOD_POST: {
                this.put("lang", lang);
                this.put("uri", uri);
                this.put("module", Module.home);
                this.put("path", path);
                this.put("request", req);
                this.put("response", resp);
                this.set("session", this.getSession());
                this.set("system", SystemConfig.getInstance());
                this.createQuery();

                if (!validbrowser()) {
                    notsupport();
                    return null;
                }

                /**
                 * get the require annotation define onPost;
                 */
                Method m = this.getClass().getMethod("onPost");
                Require require = m.getAnnotation(Require.class);

                /**
                 * test require login or access ?
                 */
                User me = this.getUser();
                this.set("me", me);
                if (require == null || !require.login() || (me != null && (require.access() == null || X.NONE.equals(require.access()) || me.hasAccess(require.access())))) {
                    onPost();
                } else if (me == null) {
                    /**
                     * the user not login
                     */
                    this.responseJson("{state:'require login'}");
                } else {
                    /**
                     * access deny, show json object
                     */
                    this.responseJson("{state:'access deny'}");
                }
                break;
            }
            case METHOD_MDC: {
                /**
                 * get the require annotation define onPost;
                 */
                // Method m = this.getClass().getMethod("onMDC");
                //
                // /**
                // * test require login or access ?
                // */
                // User me = this.getUser();
                onMDC();
                //
                // } else if (me == null) {
                // /**
                // * the user not login
                // */
                // this.put(X.STATE, X.FAIL);
                // this.put(X.MESSAGE, "login required");
                // } else {
                // /**
                // * access deny,
                // */
                // this.put(X.STATE, X.FAIL);
                // this.put(X.MESSAGE, "access deny");
                // }
                break;
            }
            } // end default handler

        } catch (Exception e) {
            onError(e);
        } finally {
            _currentmodule.remove();
        }
        return null;
    }

    private void createQuery() {
        String url = uri;
        if (this.path != null) {
            url += "/" + path;
        }

        query = new QueryString(url).copy(this);
        this.set("query", query);

    }

    /**
     * Goto login.
     */
    final protected void gotoLogin() {
        if (this.uri != null && !(this.uri.indexOf("/usr/") > 0)) {
            if (query == null) {
                createQuery();
            }

            Session.load(sid()).set("uri", this.query == null ? this.uri : this.query.path(this.uri).toString()).store();
        }

        String request = this.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(request)) {
            JSONObject jo = new JSONObject();
            jo.put(X.STATE, 202);
            jo.put(X.MESSAGE, "请重现登录！");
            jo.put(X.ERROR, "没有登录信息！");
            // this.redirect("/user/login/popup");
            this.response(jo);

        } else {
            this.redirect("/user/login");
        }
    }

    /**
     * Sid.
     * 
     * @return the string
     */
    final public String sid() {
        return sid(true);
    }

    /**
     * Sid.
     * 
     * @param newSession
     *            the new session
     * @return the string
     */
    final public String sid(boolean newSession) {
        if (sid == null) {
            sid = this.getString("sid");
            if (sid == null) {
                sid = this.getString("token");
                if (sid == null) {
                    sid = this.getHeader("sid");
                    if (sid == null) {
                        sid = this.getCookie("sid");
                        if (sid == null && newSession) {
                            do {
                                sid = H64.toString((int) (Math.random() * Integer.MAX_VALUE)) + H64.toString(System.currentTimeMillis());
                            } while (Session.exists(sid));

                        }
                    }
                }
            }
        }

        String sid2 = this.getCookie("sid");
        if (sid2 == null || !sid2.equals(sid)) {
            /**
             * get session.expired in seconds
             */
            addCookie("sid", sid, Module._conf.getInt("session.expired", (int) (X.AYEAR / 1000)));
        }
        return sid;
    }

    /**
     * On error.
     * 
     * @param e
     *            the e
     */
    final protected void onError(Throwable e) {
        log.error(e.getMessage(), e);

        if (method.isMdc()) {
            this.put(X.STATE, 202);
            this.put(X.MESSAGE, "server error");
            this.put(X.ERROR, e.getMessage());
        } else {
            if (resp != null) {
                this.put("e", e);
                show("error.html");
            }
        }
    }

    /**
     * Redirect.
     * 
     * @param url
     *            the url
     */
    final protected void redirect(String url) {
        resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        resp.setHeader("Location", url);
    }

    /**
     * Forward.
     * 
     * @param model
     *            the model
     */
    final protected void forward(String model) {
        Controller.dispatch(model, req, resp, method);
    }

    protected JSONObject mockMdc = null;

    /**
     * Mock mdc.
     */
    final protected void mockMdc() {
        mockMdc = new JSONObject();
    }

    /**
     * Put.
     * 
     * @param name
     *            the name
     * @param o
     *            the o
     */
    final protected void put(String name, Object o) {
        // System.out.println("put:" + name + "=>" + o);

        /**
         * if the request is from mdc, the set the result directly to the
         * response
         */
        if (resp instanceof MDCHttpResponse) {
            ((MDCHttpResponse) resp).set(name, o);
            return;
        }

        if (mockMdc != null) {
            if (o != null) {
                mockMdc.put(name, o);
            } else {
                mockMdc.remove(name);
            }
        }

        if (context == null) {
            context = new VelocityContext();
        }
        if (name == null) {
            return;
        }

        if (o == null) {
            /**
             * clear
             */
            context.remove(name);
        } else {
            context.put(name, o);
        }
    }

    final protected void remove(String name) {
        if (context != null) {
            context.remove(name);
        }
    }

    /**
     * Sets the.
     * 
     * @param name
     *            the name
     * @param o
     *            the o
     */
    final protected void set(String name, Object o) {
        put(name, o);
    }

    /**
     * get the value from the context
     * 
     * @param name
     * @return Object
     */
    final protected Object get(String name, Object defaultValue) {
        if (context != null) {
            return context.get(name);
        }
        return defaultValue;
    }

    /**
     * Sets the header.
     * 
     * @param name
     *            the name
     * @param value
     *            the value
     */
    final protected void setHeader(String name, String value) {
        resp.setHeader(name, value);
    }

    /**
     * Sets the.
     * 
     * @param bs
     *            the bs
     * @param s
     *            the s
     * @param n
     *            the n
     */
    final protected void set(Beans<? extends Bean> bs, int s, int n) {
        if (bs != null) {
            this.set("list", bs.getList());
            this.set("total", bs.getTotal());
            if (n > 0) {
                int t = bs.getTotal() / n;
                if (bs.getTotal() % n > 0)
                    t++;
                this.set("totalpage", t);
            }
            this.set("pages", Paging.create(bs.getTotal(), s, n));
        }
    }

    /**
     * Sets the.
     * 
     * @param jo
     *            the jo
     * @param names
     *            the names
     */
    final protected void set(Map<Object, Object> jo, String... names) {
        if (jo == null) {
            return;
        }

        if (names == null || names.length == 0) {
            for (Object name : jo.keySet()) {
                put(name.toString(), jo.get(name));
            }
        } else {
            for (String name : names) {
                if (jo.containsKey(name)) {
                    put(name, jo.get(name));
                }
            }
        }
    }

    /**
     * Checks for.
     * 
     * @param name
     *            the name
     * @return true, if successful
     */
    final protected boolean has(String name) {
        return context != null && context.containsKey(name);
    }

    /**
     * Gets the header.
     * 
     * @param tag
     *            the tag
     * @return the header
     */
    final protected String getHeader(String tag) {
        try {
            return req.getHeader(tag);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Adds the header.
     * 
     * @param tag
     *            the tag
     * @param v
     *            the v
     */
    final protected void addHeader(String tag, String v) {
        try {
            resp.addHeader(tag, v);
        } catch (Exception e) {
        }
    }

    /**
     * Gets the int.
     * 
     * @param tag
     *            the tag
     * @return the int
     */
    final protected int getInt(String tag) {
        return getInt(tag, 0);
    }

    /**
     * Gets the int.
     * 
     * @param jo
     *            the jo
     * @param tag
     *            the tag
     * @return the int
     */
    final protected int getInt(JSONObject jo, String tag) {
        if (jo.has(tag)) {
            return Bean.toInt(jo.get(tag));
        }

        return this.getInt(tag);
    }

    /**
     * Gets the int.
     * 
     * @param jo
     *            the jo
     * @param tag
     *            the tag
     * @param minValue
     *            the min value
     * @param tagInSession
     *            the tag in session
     * @return the int
     */
    final protected int getInt(JSONObject jo, String tag, int minValue, String tagInSession) {
        if (jo.has(tag)) {
            int i = Bean.toInt(jo.get(tag));
            if (i >= minValue) {
                return i;
            }
        }

        return this.getInt(tag, minValue, tagInSession);
    }

    /**
     * Gets the int.
     * 
     * @param tag
     *            the tag
     * @param minValue
     *            the min value
     * @param tagInSession
     *            the tag in session
     * @return the int
     */
    final protected int getInt(String tag, int minValue, String tagInSession) {
        int r = getInt(tag);
        if (r < minValue) {
            Session s = this.getSession();
            r = s.getInt(tagInSession);
            if (r < minValue) {
                r = SystemConfig.i(tagInSession, minValue);
            }
        } else {
            Session s = this.getSession();
            s.set(tagInSession, r).store();
        }

        if (r > 500) {
            r = 500;
            log.error("the page number exceed max[500]: " + r);
        }
        return r;
    }

    /**
     * get the parameter from the request, if not presented, then get from
     * session
     * 
     * @param tag
     * @param tagInSession
     * @param defaultValue
     * @return String of the value
     */
    final protected String getString(String tag, String tagInSession, String defaultValue) {
        String r = getString(tag);
        if (X.isEmpty(r)) {
            Session s = this.getSession();
            r = (String) s.get(tagInSession);
            if (X.isEmpty(r)) {
                r = defaultValue;
                s.set(tagInSession, r).store();
            }
        } else {
            Session s = this.getSession();
            s.set(tagInSession, r).store();
        }

        return r;
    }

    /**
     * Gets the int.
     * 
     * @param tag
     *            the tag
     * @param defaultValue
     *            the default value
     * @return the int
     */
    final protected int getInt(String tag, int defaultValue) {
        String v = this.getString(tag);
        return Bean.toInt(v, defaultValue);
    }

    /**
     * Gets the long.
     * 
     * @param tag
     *            the tag
     * @return the long
     */
    final protected long getLong(String tag, long defaultvalue) {
        String v = this.getString(tag);
        return Bean.toLong(v, defaultvalue);
    }

    final protected long getLong(String tag) {
        return getLong(tag, 0);
    }

    /**
     * get all cookies
     * 
     * @return Cookie[]
     */
    final protected Cookie[] getCookie() {
        return req.getCookies();
    }

    /**
     * Gets the cookie.
     * 
     * @param name
     *            the name
     * @return the cookie
     */
    final protected String getCookie(String name) {
        Cookie[] cc = getCookie();
        if (cc != null) {
            for (int i = cc.length - 1; i >= 0; i--) {
                Cookie c = cc[i];
                if (c.getName().equals(name)) {
                    return c.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 
     * @return HttpServletRequest
     */
    final protected HttpServletRequest getRequest() {
        return req;
    }

    /**
     * 
     * @return HttpServletResponse
     */
    final protected HttpServletResponse getResponse() {
        return resp;
    }

    /**
     * Browser.
     * 
     * @return the string
     */
    final protected String browser() {
        return this.getHeader("user-agent");
    }

    /**
     * 
     * @return boolean
     */
    final protected boolean isMDC() {
        return "mdc".equals(browser());
    }

    /**
     * Adds the cookie.
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     * @param expireseconds
     *            the expireseconds
     */
    // final protected TConn getConnection() {
    // if (req instanceof MDCHttpRequest) {
    // return ((MDCHttpRequest) req).getConnection();
    // }
    //
    // return null;
    // }

    /**
     * set the cookie back to the response
     * 
     * @param key
     * @param value
     * @param expireseconds
     */
    final protected void addCookie(String key, String value, int expireseconds) {
        if (key == null) {
            return;
        }

        Cookie c = new Cookie(key, value);
        if (value == null) {
            c.setMaxAge(0);
        } else if (expireseconds > 0) {
            c.setMaxAge(expireseconds);
        }
        c.setPath("/");

        /**
         * set back to the domain
         */
        // c.setDomain(SystemConfig.s("domain", this.getHeader("Host")));
        String domain = Module._conf.getString("domain", null);
        if (!X.isEmpty(domain)) {
            c.setDomain(domain);
        }

        addCookie(c);
    }

    /**
     * Adds the cookie.
     * 
     * @param c
     *            the c
     */
    final protected void addCookie(Cookie c) {
        if (c != null) {
            this.getResponse().addCookie(c);
        }
    }

    /**
     * the the request uri
     * 
     * @return String
     */
    final protected String getURI() {
        return req.getRequestURI();
    }

    /**
     * get the sub.path of the uri
     * 
     * @return String
     */
    final protected String getPath() {
        return path;
    }

    /**
     * trying to get the client ip
     * 
     * @return String
     */
    final protected String getRemoteHost() {
        String remote = this.getHeader("X-Forwarded-For");
        if (remote == null) {
            remote = getHeader("X-Real-IP");

            if (remote == null) {
                remote = req.getRemoteAddr();
            }
        }

        return remote;
    }

    /**
     * get all param names
     * 
     * @return Enumeration
     */
    @SuppressWarnings("unchecked")
    final protected Enumeration<String> getParameterNames() {
        try {
            return req.getParameterNames();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * get the request as JSON
     * 
     * @return JSONObject
     */
    final protected JSONObject getJSON() {
        if (req instanceof MDCHttpRequest) {
            return ((MDCHttpRequest) req).getJSON();
        } else {
            JSONObject jo = new JSONObject();
            for (String name : this.getNames()) {
                String s = this.getString(name);
                // if (!X.isEmpty(s)) {
                jo.put(name, s);
                // }
            }
            return jo;
        }
    }

    /**
     * Gets the string.
     * 
     * @param name
     *            the name
     * @return the string
     */
    final protected String getString(String name) {
        try {
            if (this._multipart) {
                getFiles();

                FileItem i = this.getFile(name);

                if (i != null && i.isFormField()) {
                    InputStream in = i.getInputStream();
                    byte[] bb = new byte[in.available()];
                    in.read(bb);
                    in.close();
                    return new String(bb, "UTF8").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                }

            } else {
                String[] ss = req.getParameterValues(name);
                if (ss != null && ss.length > 0) {
                    String s = ss[ss.length - 1];
                    return s.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                }
            }

            return null;
        } catch (Exception e) {
            log.error("get request parameter " + name + " get exception.", e);
            return null;
        }
    }

    /**
     * Gets the string.
     * 
     * @param name
     *            the name
     * @param maxlength
     *            the maxlength
     * @return the string
     */
    final protected String getString(String name, int maxlength) {
        String s = getString(name);
        if (!X.isEmpty(s)) {
            if (s.getBytes().length > maxlength) {
                s = new Html(s).text(maxlength);
            }
        }

        return s;
    }

    /**
     * Gets the string.
     * 
     * @param name
     *            the name
     * @param maxlength
     *            the maxlength
     * @param defaultvalue
     *            the defaultvalue
     * @return the string
     */
    final protected String getString(String name, int maxlength, String defaultvalue) {
        String s = getString(name);
        if (!X.isEmpty(s)) {
            if (s.getBytes().length > maxlength) {
                s = new Html(s).text(maxlength);
            }
        } else {
            s = defaultvalue;
        }

        return s;
    }

    final protected String getHtml(String name) {
        return getHtml(name, false);
    }

    /**
     * Gets the html.
     * 
     * @param name
     *            the name
     * @return the html
     */
    final protected String getHtml(String name, boolean all) {
        try {
            // String contenttype = this.getRequest() != null ?
            // this.getRequest()
            // .getContentType() : null;
            // if (contenttype != null
            // && contenttype.indexOf("multipart/form-data") > -1) {
            // String ss = req.getParameter(name);
            // log.debug("name=" + ss);
            // if (ss != null) {
            // return Html.removeTag(ss, "script");
            // }
            // }
            String[] ss = req.getParameterValues(name);
            if (ss != null && ss.length > 0) {
                String s = ss[ss.length - 1];
                if (all) {
                    return s;
                } else {
                    return Html.removeTag(s, "script");
                }
            }

            return null;
        } catch (Exception e) {
            log.error("get request parameter " + name + " get exception.", e);
            return null;
        }
    }

    /**
     * Gets the html.
     * 
     * @param name
     *            the name
     * @param maxlength
     *            the maxlength
     * @return the html
     */
    final protected String getHtml(String name, int maxlength) {
        String html = getHtml(name);
        if (!X.isEmpty(html)) {
            if (html.getBytes().length >= maxlength) {
                html = new Html(html).text(maxlength);
            }
        }
        return html;
    }

    /**
     * Gets the html.
     * 
     * @param name
     *            the name
     * @param maxlength
     *            the maxlength
     * @param defaultvalue
     *            the defaultvalue
     * @return the html
     */
    final protected String getHtml(String name, int maxlength, String defaultvalue) {
        String html = getHtml(name);
        if (!X.isEmpty(html)) {
            if (html.getBytes().length >= maxlength) {
                html = new Html(html).text(maxlength);
            }
        } else {
            html = defaultvalue;
        }
        return html;
    }

    /**
     * Gets the strings.
     * 
     * @param name
     *            the name
     * @return the strings
     */
    final protected String[] getStrings(String name) {
        try {
            if (this._multipart) {
                getFiles();

                Object o = uploads.get(name);
                if (o instanceof FileItem) {
                    return new String[] { getString(name) };
                } else if (o instanceof List) {
                    List<FileItem> list = (List<FileItem>) o;
                    String[] ss = new String[list.size()];
                    for (int i = 0; i < ss.length; i++) {
                        FileItem ii = list.get(i);
                        if (ii.isFormField()) {
                            InputStream in = ii.getInputStream();
                            byte[] bb = new byte[in.available()];
                            in.read(bb);
                            in.close();
                            ss[i] = new String(bb, "UTF8").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                        }
                    }
                    return ss;
                }
            } else {
                String[] ss = req.getParameterValues(name);
                if (ss != null && ss.length > 0) {
                    for (int i = 0; i < ss.length; i++) {
                        ss[i] = ss[i].replaceAll("<", "&lt").replaceAll(">", "&gt");
                    }
                }
                return ss;
            }
        } catch (Exception e) {
            log.error(name, e);
        }
        return null;
    }

    /**
     * get the parameters names
     * 
     * @return List
     */
    final protected List<String> getNames() {
        if (this._multipart) {
            getFiles();
            return new ArrayList<String>(uploads.keySet());
        } else {
            Enumeration<?> e = req.getParameterNames();
            List<String> list = new ArrayList<String>();

            while (e.hasMoreElements()) {
                list.add(e.nextElement().toString());
            }
            return list;
        }

    }

    /**
     * get the session, if not presented, then create a new, "user" should store
     * the session invoking session.store()
     * 
     * @return Session
     */
    final protected Session getSession() {
        return Session.load(sid());
    }

    /**
     * Gets the http session.
     * 
     * @param bfCreate
     *            the bf create
     * @return the http session
     */
    final protected HttpSession getHttpSession(boolean bfCreate) {

        return req.getSession(bfCreate);
    }

    transient boolean _multipart = false;

    /**
     * is multipart request
     * 
     * @return boolean
     */
    final protected boolean isMultipart() {
        return _multipart;
    }

    /**
     * get the user associated with the session
     * 
     * @return User
     */
    final protected User getUser() {
        if (login == null) {
            Session s = getSession();
            login = (User) s.get("user");

            // log.debug("sid=" + s.sid() + ", user=" + login + ", session=" +
            // s);

            if (login == null && "true".equals(SystemConfig.s("cross.context", "false"))) {
                HttpSession s1 = this.getHttpSession(true);
                if (s1 != null) {
                    String ss = (String) s1.getAttribute(SystemConfig.s("session.key", "user"));
                    if (ss != null) {
                        JSONObject jo = JSONObject.fromObject(ss);
                        if (jo.has("id")) {
                            int id = jo.getInt("id");
                            login = User.loadById(id);
                        }
                    }
                }

            }
        }
        return login;
    }

    /**
     * set the user associated with the session
     * 
     * @param u
     */
    final protected void setUser(User u) {
        Session s = getSession();
        if (u == null) {
            s.remove("user");

            // Exception e = new Exception();
            // log.debug("remove user", e);
        } else {
            s.set("user", u);
        }
        s.store();

        // log.debug("store session: " + s.sid() + ", user=" + u + ", session="
        // + getSession());

        login = u;

        if ("true".equals(SystemConfig.s("cross.context", "false"))) {
            HttpSession s1 = this.getHttpSession(true);
            if (s1 != null) {
                if (u == null) {
                    s1.removeAttribute(SystemConfig.s("session.key", "user"));
                } else {
                    JSONObject jo = new JSONObject();
                    u.toJSON(jo);
                    s1.setAttribute(SystemConfig.s("session.key", "user"), jo.toString());
                }
            }
        }
    }

    final protected Map<String, Object> getFiles() {
        if (uploads == null) {
            uploads = new HashMap<String, Object>();
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Configure a repository (to ensure a secure temp location is used)
            ServletContext servletContext = GiisooServlet.config.getServletContext();
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            factory.setRepository(repository);

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);

            // Parse the request
            try {
                List<FileItem> items = upload.parseRequest(req);
                if (items != null) {
                    for (FileItem f : items) {
                        if (uploads.containsKey(f.getFieldName())) {
                            Object o = uploads.get(f.getFieldName());
                            if (o instanceof FileItem) {
                                List<FileItem> list = new ArrayList<FileItem>();
                                list.add((FileItem) o);
                                list.add(f);
                                uploads.put(f.getFieldName(), list);
                            } else if (o instanceof List) {
                                ((List<FileItem>) o).add(f);
                            }
                        } else {
                            uploads.put(f.getFieldName(), f);
                        }
                    }
                }
            } catch (FileUploadException e) {
                log.error(e);
            }
        }

        return uploads;
    }

    /**
     * Gets the file.
     * 
     * @param name
     *            the name
     * @return the file
     */
    final protected FileItem getFile(String name) {
        if (isMDC()) {
            return ((MDCHttpRequest) req).getFile(name);
        }

        getFiles();

        Object o = uploads.get(name);
        if (o instanceof FileItem) {
            return (FileItem) o;
        } else if (o instanceof List) {
            List<FileItem> list = (List<FileItem>) o;
            return list.get(list.size() - 1);
        }
        return null;
    }

    /**
     * uploaded file
     */
    private Map<String, Object> uploads = null;

    protected String path;

    /**
     * 
     * @param contentType
     */
    final protected void setContentType(String contentType) {
        this.contentType = contentType;
        resp.setContentType(contentType);
    }

    /**
     * return the contentType
     * 
     * @return String
     */
    final protected String getContentType() {
        if (contentType == null) {
            return MIME_HTML;
        } else {
            return contentType;
        }
    }

    /**
     * Gets the template.
     * 
     * @param viewname
     * @param allowEmpty
     * @return Template
     */
    final protected Template getTemplate(String viewname, boolean allowEmpty) {
        Template template = cache.get(viewname);

        if (template == null || template.isSourceModified()) {
            /**
             * get the template from the top
             */
            template = Module.home.getTemplate(viewname, allowEmpty);

            cache.put(viewname, template);
        }

        return template;
    }

    /**
     * render and output the html page
     * 
     * @param viewname
     *            the viewname
     */
    final protected boolean show(String viewname) {
        this.set("path", this.path);
        this.set("query", this.query);

        return show(viewname, false);
    }

    /**
     * output the json as "application/json"
     * 
     * @param jo
     *            the jo
     */
    final protected void response(JSONObject jo) {
        if (jo == null) {
            responseJson("{}");
        } else {
            responseJson(jo.toString());
        }
    }

    /**
     * Response.
     * 
     * @param arr
     *            the arr
     */
    final protected void response(JSONArray arr) {
        if (arr == null) {
            responseJson("[]");
        } else {
            responseJson(arr.toString());
        }
    }

    /**
     * Response json.
     * 
     * @deprecated
     * @param jsonstr
     *            the jsonstr
     */
    final protected void responseJson(String jsonstr) {
        this.setContentType(Model.MIME_JSON);
        this.print(jsonstr);
    }

    /**
     * Render.
     * 
     * @param viewname
     *            the viewname
     * @return the string
     */
    final protected String render(String viewname) {
        StringBuilderWriter sb = null;
        try {

            Template template = getTemplate(viewname, true);

            // System.out.println(viewname + "=>" + template);
            if (template != null) {
                sb = new StringBuilderWriter();

                template.merge(context, sb);

                return sb.toString();
            }

        } catch (Exception e) {
            log.error(viewname, e);
        } finally {
            if (sb != null) {
                sb.close();
            }
        }

        return null;
    }

    /**
     * Show.
     * 
     * @param viewname
     *            the viewname
     * @param allowOverride
     *            the allow override
     */
    final protected boolean show(String viewname, boolean allowOverride) {
        // if (expired()) {
        // log.warn("the request was expired");
        // return false;
        // }

        Writer writer = null;
        try {

            resp.setContentType(this.getContentType());

            TimeStamp t1 = TimeStamp.create();
            Template template = getTemplate(viewname, allowOverride);
            log.debug("finding template = " + viewname + ", cost: " + t1.past() + "ms");

            // System.out.println(viewname + "=>" + template);
            if (template != null) {
                writer = new BufferedWriter(resp.getWriter());

                TimeStamp t = TimeStamp.create();
                template.merge(context, writer);
                writer.flush();
                log.debug("merge [" + viewname + "] cost: " + t.past() + "ms");

                return true;
            }

        } catch (Exception e) {
            log.error(viewname, e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }

        return false;
    }

    final protected String parse(String viewname, Object[]... params) {

        StringWriter writer = null;
        try {

            resp.setContentType(this.getContentType());

            TimeStamp t1 = TimeStamp.create();
            Template template = getTemplate(viewname, true);
            log.debug("finding template = " + viewname + ", cost: " + t1.past() + "ms");

            // System.out.println(viewname + "=>" + template);
            if (template != null) {
                writer = new StringWriter();

                TimeStamp t = TimeStamp.create();

                VelocityContext context = new VelocityContext();
                if (params != null) {
                    for (Object[] p : params) {
                        if (p.length == 2) {
                            context.put(p[0].toString(), p[1]);
                        }
                    }
                }
                template.merge(context, writer);
                writer.flush();
                log.debug("merge [" + viewname + "] cost: " + t.past() + "ms");

                return writer.toString();
            }

        } catch (Exception e) {
            log.error(viewname, e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }

        return X.EMPTY;
    }

    /**
     * On mdc.
     */
    public void onMDC() {
        if (module != null) {
            Module t = module.floor();
            if (t != null) {
                Model m = t.loadModel(uri);
                if (m != null) {
                    m.dispatch(uri, req, resp, new HTTPMethod(Model.METHOD_MDC));
                    return;
                }
            }
        }

        this.put(X.MESSAGE, "not support, not found the uri:" + uri);
    }

    /**
     * On get.
     */
    public void onGet() {
        onPost();
    }

    /**
     * On post.
     */
    public void onPost() {
        if (module != null) {
            Module t = module.floor();
            if (t != null) {
                Model m = t.loadModel(uri);
                if (m != null) {
                    m.dispatch(uri, req, resp, method);
                    return;
                }
            }
        }
        log.warn("nosupport the POST" + this.path);
        show("/nosupport.html");
    }

    /**
     * @param path
     */
    final protected void setPath(String path) {
        this.path = path;
    }

    /**
     * Merge.
     * 
     * @param uri
     *            the uri
     * @return the string
     */
    final protected String merge(String uri) {
        Template template = getTemplate(uri, true);

        // System.out.println(viewname + "=>" + template);
        StringWriter writer = null;
        try {
            if (template != null) {
                writer = new StringWriter();

                template.merge(context, writer);
                writer.flush();
                return writer.toString();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                log.error(e);
            }
        }
        return null;
    }

    /**
     * Gets the mime type.
     * 
     * @param uri
     *            the uri
     * @return the mime type
     */
    final static protected String getMimeType(String uri) {
        return GiisooServlet.config.getServletContext().getMimeType(uri);
    }

    /**
     * Error.
     * 
     * @param e
     *            the e
     */
    final protected void error(Throwable e) {
        this.set("me", this.getUser());

        this.set("e", e);

        this.show("/error.html", true);
    }

    /**
     * Notfound.
     */
    final protected void notfound() {
        this.set("me", this.getUser());
        this.show("/notfound.html", true);
    }

    /**
     * Deny.
     */
    final protected void deny() {
        deny(null);
    }

    /**
     * Deny.
     * 
     * @param error
     *            the error
     */
    final protected void deny(String error) {
        log.warn("deny ... " + error);
        String request = this.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(request)) {
            JSONObject jo = new JSONObject();
            jo.put(X.STATE, 202);
            jo.put(X.MESSAGE, "你没有权限访问！");
            jo.put(X.ERROR, error);
            // this.redirect("/user/login/popup");
            this.response(jo);
        } else {
            this.set("me", this.getUser());
            this.set(X.ERROR, error);
            this.show("/deny.html", true);
        }

    }

    /**
     * Delete.
     * 
     * @param f
     *            the f
     */
    final protected void delete(File f) {
        if (!f.exists()) {
            return;
        }
        if (f.isFile()) {
            f.delete();
        }

        if (f.isDirectory()) {
            File[] list = f.listFiles();
            if (list != null && list.length > 0) {
                for (File f1 : list) {
                    delete(f1);
                }
            }
            f.delete();
        }
    }

    /**
     * 
     * @return int
     */
    final protected int getMethod() {
        return method.method;
    }

    final public static int METHOD_GET = 1;
    final public static int METHOD_POST = 2;
    final public static int METHOD_MDC = 4;

    final public static String MIME_JSON = "application/json;charset=" + ENCODING;
    final protected static String MIME_HTML = "text/html;charset=" + ENCODING;

    /**
     * Copy.
     * 
     * @param in
     *            the in
     * @param out
     *            the out
     * @return the int
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        return copy(in, out, true);
    }

    /**
     * Copy.
     * 
     * @param in
     *            the in
     * @param out
     *            the out
     * @param start
     *            the start
     * @param end
     *            the end
     * @param closeAfterDone
     *            the close after done
     * @return the int
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static int copy(InputStream in, OutputStream out, long start, long end, boolean closeAfterDone) throws IOException {
        try {
            if (in == null || out == null)
                return 0;

            byte[] bb = new byte[1024 * 4];
            int total = 0;
            in.skip(start);
            int ii = Math.min((int) (end - start), bb.length);
            int len = in.read(bb, 0, ii);
            while (len > 0) {
                out.write(bb, 0, len);
                total += len;
                ii = Math.min((int) (end - start - total), bb.length);
                len = in.read(bb, 0, ii);
                out.flush();
            }
            return total;
        } finally {
            if (closeAfterDone) {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    /**
     * Copy.
     * 
     * @param in
     *            the in
     * @param out
     *            the out
     * @param closeAfterDone
     *            the close after done
     * @return the int
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static int copy(InputStream in, OutputStream out, boolean closeAfterDone) throws IOException {
        try {
            if (in == null || out == null)
                return 0;

            byte[] bb = new byte[1024 * 4];
            int total = 0;
            int len = in.read(bb);
            while (len > 0) {
                out.write(bb, 0, len);
                total += len;
                len = in.read(bb);
                out.flush();
            }
            return total;
        } finally {
            if (closeAfterDone) {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    /**
     * Copy.
     * 
     * @param m
     *            the m
     */
    final public void copy(Model m) {
        this.req = m.req;
        this.resp = m.resp;
        this.contentType = m.contentType;
        this.context = m.context;
        this.lang = m.lang;
        this.locale = m.locale;
        this.login = m.login;
        this.method = m.method;
        this.path = m.path;
        this.sid = m.sid;
        this.uri = m.uri;
    }

    /**
     * pathmapping structure: {"method", {"path", Path|Method}}
     */
    protected Map<Integer, Map<String, PathMapping>> pathmapping;

    /**
     * Println.
     * 
     * @param o
     *            the o
     */
    final protected void println(Object o) {
        print(o);
    }

    /**
     * Prints the.
     * 
     * @param o
     *            the o
     */
    final protected void print(Object o) {
        try {
            BufferedWriter writer = new BufferedWriter(resp.getWriter());

            writer.write(o.toString());
            writer.flush();
        } catch (Exception e) {
            log.error(o, e);
        }
    }

    /**
     * Copy.
     * 
     * @param v
     *            the v
     * @param names
     *            the names
     * @return the int
     */
    final protected int copy(V v, String... names) {
        if (v == null || names == null || names.length == 0)
            return 0;

        int count = 0;
        for (String name : names) {
            String s = this.getString(name);
            v.set(name, s);
            count++;
        }
        return count;
    }

    /**
     * Copy int.
     * 
     * @param v
     *            the v
     * @param names
     *            the names
     * @return the int
     */
    final protected int copyInt(V v, String... names) {
        if (v == null || names == null || names.length == 0)
            return 0;

        int count = 0;
        for (String name : names) {
            int s = Bean.toInt(this.getString(name));
            v.set(name, s);
            count++;
        }
        return count;
    }

    /**
     * Copy long.
     * 
     * @param v
     *            the v
     * @param names
     *            the names
     * @return the int
     */
    final protected int copyLong(V v, String... names) {
        if (v == null || names == null || names.length == 0)
            return 0;

        int count = 0;
        for (String name : names) {
            long s = Bean.toLong(this.getString(name));
            v.set(name, s);
            count++;
        }
        return count;
    }

    /**
     * Copy date.
     * 
     * @param v
     *            the v
     * @param format
     *            the format
     * @param names
     *            the names
     * @return the int
     */
    final protected int copyDate(V v, String format, String... names) {
        if (v == null || names == null || names.length == 0)
            return 0;

        int count = 0;
        for (String name : names) {
            long s = lang.parse(this.getString(name), format);
            if (s > 0) {
                v.set(name, s);
                count++;
            }
        }
        return count;
    }

    /**
     * Rebound.
     * 
     * @param names
     *            the names
     * @return the int
     */
    final protected int rebound(String... names) {
        int count = 0;
        if (names != null && names.length > 0) {
            for (String name : names) {
                set(name, this.getString(name));
                count++;
            }
        } else {
            for (String name : this.getNames()) {
                set(name, this.getString(name));
                count++;
            }
        }
        return count;
    }

    public static class PathMapping {
        Pattern pattern;
        Method method;
        Path path;

        /**
         * Creates the.
         * 
         * @param pattern
         *            the pattern
         * @param path
         *            the path
         * @param method
         *            the method
         * @return the path mapping
         */
        public static PathMapping create(Pattern pattern, Path path, Method method) {
            PathMapping e = new PathMapping();
            e.pattern = pattern;
            e.path = path;
            e.method = method;
            return e;
        }

    }

    public static class HTTPMethod {
        int method = Model.METHOD_GET;

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            switch (method) {
            case Model.METHOD_GET: {
                return "GET";
            }
            case Model.METHOD_MDC: {
                return "MDC";
            }
            case Model.METHOD_POST: {
                return "POST";
            }
            }
            return "Unknown";
        }

        /**
         * Instantiates a new HTTP method.
         * 
         * @param m
         *            the m
         */
        public HTTPMethod(int m) {
            this.method = m;
        }

        public boolean isGet() {
            return method == Model.METHOD_GET;
        }

        public boolean isPost() {
            return method == Model.METHOD_POST;
        }

        public boolean isMdc() {
            return method == Model.METHOD_MDC;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return method;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            HTTPMethod other = (HTTPMethod) obj;
            if (method != other.method)
                return false;
            return true;
        }

    }

    transient String tostring;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (tostring == null) {
            tostring = new StringBuilder(this.getClass().getName()).append("[").append(this.uri).append(", path=").append(this.path).append("]").toString();
        }
        return tostring;
    }

    /**
     * Random.
     * 
     * @param <T>
     *            the generic type
     * @param list
     *            the list
     * @return the list
     */
    public static <T> List<T> random(List<T> list) {
        if (list == null || list.size() == 0)
            return list;

        int len = list.size();
        for (int i = 0; i < len; i++) {
            int j = (int) Math.random() * len;
            if (j == i)
                continue;

            T o = list.get(i);
            list.set(i, list.get(j));
            list.set(j, o);
        }

        return list;
    }

    private boolean validbrowser() {
        /**
         * ignore DummyModel
         */
        if (this instanceof DummyModel) {
            return true;
        }

        String agent = this.browser();
        String support = Module.home.get("browser.support");
        // log.info("support=" + support + ", agent=" + agent);

        if (!X.isEmpty(support) && (agent != null && agent.matches(support))) {
            return true;
        }

        String notsupport = Module.home.get("browser.notsupport");
        // log.info("not.support=" + notsupport);
        if (!X.isEmpty(notsupport) && (agent == null || agent.matches(notsupport))) {
            return false;
        }

        return true;
    }

    final public void notsupport() {
        log.warn("nosupport, support.ua=" + module.get("browser.support") + ", actual.ua=" + this.browser() + ", params=" + this.getJSON());

        this.show("/notsupport.html");
    }

    @SuppressWarnings("unchecked")
    final public NameValue[] getQueries() {
        Enumeration<String> e = req.getParameterNames();
        if (e != null) {
            List<NameValue> list = new ArrayList<NameValue>();
            while (e.hasMoreElements()) {
                String n = e.nextElement();
                list.add(NameValue.create(n, Bean.toString(req.getParameterValues(n))));
            }

            return list.toArray(new NameValue[list.size()]);
        }
        return null;
    }

    /**
     * Node.
     * 
     * @return the string
     */
    public static String node() {
        return Module._conf.getString("node", null);
    }

    @SuppressWarnings("unchecked")
    final public NameValue[] getHeaders() {
        Enumeration<String> e = req.getHeaderNames();
        if (e != null) {
            List<NameValue> list = new ArrayList<NameValue>();
            while (e.hasMoreElements()) {
                String n = e.nextElement();
                list.add(NameValue.create(n, req.getHeader(n)));
            }

            return list.toArray(new NameValue[list.size()]);
        }

        return null;
    }

    public static class NameValue {
        String name;
        String value;

        /**
         * Creates the.
         * 
         * @param name
         *            the name
         * @param value
         *            the value
         * @return the name value
         */
        public static NameValue create(String name, String value) {
            NameValue h = new NameValue();
            h.name = name;
            h.value = value;
            return h;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return new StringBuilder(name).append("=").append(value).toString();
        }
    }

    public static void setCurrentModule(Module e) {
        _currentmodule.set(e);
    }

    public final boolean isMobile() {
        String useragent = module.get("mobile", ".*");
        return Pattern.matches(useragent, this.browser());
    }

}
