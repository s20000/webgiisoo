/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import java.io.File;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.*;

import com.giisoo.core.bean.*;
import com.giisoo.core.cache.Cache;
import com.giisoo.core.conf.*;
import com.giisoo.core.db.DB;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.Repo;
import com.giisoo.framework.common.Temp;
import com.giisoo.framework.web.Model.HTTPMethod;

/**
 * 
 * @author yjiang
 * 
 */
public class GiisooServlet extends HttpServlet {

    /**
   * 
   */
    private static final long serialVersionUID = 1L;

    static Log log = LogFactory.getLog(GiisooServlet.class);

    protected static ServletConfig config;

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Controller.dispatchWithContextPath(req.getRequestURI(), req, resp, new HTTPMethod(Model.METHOD_GET));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Controller.dispatchWithContextPath(req.getRequestURI(), req, resp, new HTTPMethod(Model.METHOD_POST));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        GiisooServlet.config = config;

        super.init(config);

    }

    /**
     * Shutdown.
     */
    public static void shutdown() {
        log.warn("shutdown the app");
        System.exit(0);
    }

    /**
     * Restart.
     */
    public static void restart() {
        log.warn("restart the app");

        /**
         * clean cache
         */
        Module.clean();
        TemplateLoader.clean();
        Model.clean();

        /**
         * re-initialize
         */
        init(home, Controller.PATH);
    }

    private static String home;

    /**
     * Inits the.
     * 
     * @param home
     *            the home
     * @param contextPath
     *            the context path
     */
    public static void init(String home, String contextPath) {
        try {
            GiisooServlet.home = home;

            Model.GIISOO_HOME = System.getenv("GIISOO_HOME");

            if (X.isEmpty(Model.GIISOO_HOME)) {
                Model.GIISOO_HOME = System.getenv("CATALINA_HOME");
            }

            if (X.isEmpty(Model.GIISOO_HOME)) {
                System.out.println("ERROR, did not set GIISOO_HOME, please set GIISOO_HOME=[path of web container]");
            }

            System.out.println("tomcat.home=" + Model.GIISOO_HOME);
            System.out.println("giisoo.home=" + Model.GIISOO_HOME);
            System.out.println("webgiisoo.home=" + Model.HOME);

            log.info("webgiisoo is starting ...");
            log.info("tomcat.home=" + Model.GIISOO_HOME);
            log.info("giisoo.home=" + Model.GIISOO_HOME);
            log.info("webgiisoo.home=" + Model.HOME);

            System.setProperty("home", home);

            /**
             * initialize the configuration
             */
            Config.init("home", "giisoo");

            Configuration conf = Config.getConfig();

            /**
             * initialize the DB connections pool
             */
            DB.init();

            /**
             * initialize the cache
             */
            Cache.init(conf);

            Bean.init(conf);

            WorkerTask.init(conf.getInt("thread.number", 20), conf);

            /**
             * initialize the controller, this MUST place in the end !:-)
             */
            Controller.init(conf, contextPath);

            /**
             * initialize the repo
             */
            Repo.init(conf);

            /**
             * initialize the temp
             */
            Temp.init(conf);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
