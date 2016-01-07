/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import java.lang.annotation.*;

import com.giisoo.core.bean.X;

/**
 * please refer @Path
 * 
 * @deprecated
 * @author joe
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Require {

	/**
	 * Access.
	 * 
	 * @return the string
	 */
	String access() default X.NONE;

	/**
	 * Login.
	 * 
	 * @return true, if successful
	 */
	boolean login() default false;

	/**
	 * Contenttype.
	 * 
	 * @return the string
	 */
	String contenttype() default X.NONE;

	/**
	 * Hello.
	 * 
	 * @return true, if successful
	 */
	boolean hello() default false;

	/**
	 * Attr.
	 * 
	 * @return the string[]
	 */
	String[] attr() default {};

}
