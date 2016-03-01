/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import java.lang.annotation.*;

import com.giisoo.core.bean.X;

/**
 * the {@code Path} annotation interface used to define a Web api, for each
 * annotated method, the framework will auto generate the web api mapping for
 * the method,
 * 
 * <p>
 * the whole web api uri should be=
 * <tt>http://[host]/[classname]/[method path]</tt>, method including:
 * <p>
 * 
 * <pre>
 * path=X.NONE (no path defined)
 * method=Model.METHOD_GET|MOdel.METHOD_POST|Model.METHOD_MDC (handle all request method)
 * login=false (no required login)
 * access=X.NONE (not required access token)
 * accesslog=true (record the accesslog)
 * </pre>
 * 
 * @author joe
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

    /**
     * the URI path, default is "none"
     * 
     * @return String
     */
    String path() default X.NONE;

    /**
     * the method of request, default for all
     * 
     * @return int
     */
    int method() default Model.METHOD_GET | Model.METHOD_POST | Model.METHOD_MDC;

    /**
     * login required, default is "false"
     * 
     * @return boolean
     */
    boolean login() default false;

    /**
     * the access key that required, default is "none"
     * 
     * @return String
     */
    String access() default X.NONE;

    /**
     * Log the data of request and response, default is "none"
     * 
     * @deprecated
     * @return int
     */
    int log() default 0;

    /**
     * the support device, default is for all
     * 
     * @deprecated
     * @return String
     */
    String device() default X.NONE;

    /**
     * log the access of client info, default is true
     * 
     * @return boolean
     */
    boolean accesslog() default true;

}
