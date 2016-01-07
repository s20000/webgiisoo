/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import java.lang.annotation.*;

import com.giisoo.core.bean.X;

/**
 * used to define a Web api, for each commented api, the framework will pass
 * "login", "method" Object as "default" parameters
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
     * @return int
     */
    int log() default 0;

    /**
     * the support device, default is for all
     * 
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
