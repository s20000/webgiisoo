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
	 * Path.
	 * 
	 * @return the string
	 */
	String path() default X.NONE;

	/**
	 * Method.
	 * 
	 * @return the int
	 */
	int method() default Model.METHOD_GET | Model.METHOD_POST
			| Model.METHOD_MDC;

	/**
	 * Login.
	 * 
	 * @return true, if successful
	 */
	boolean login() default false;

	/**
	 * Access.
	 * 
	 * @return the string
	 */
	String access() default X.NONE;

	/**
	 * Log.
	 * 
	 * @return the int
	 */
	int log() default 0;

	/**
	 * Config.
	 * 
	 * @return the string[]
	 */
	String[] config() default {};
}
