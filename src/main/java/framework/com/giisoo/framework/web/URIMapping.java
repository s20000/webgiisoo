/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import java.lang.annotation.*;

import com.giisoo.core.bean.X;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface URIMapping {

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
	 * Config.
	 * 
	 * @return the string[]
	 */
	String[] config() default {};
}
