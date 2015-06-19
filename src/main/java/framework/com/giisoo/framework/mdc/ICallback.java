/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc;

public interface ICallback {

	/**
	 * web request command
	 */
	public static final int REQUEST = 1;

	/**
	 * response back to connection
	 */
	public static final int RESPONSE = 2;

	/**
	 * close a connection
	 */
	public static final int CLOSE = 3;

	/**
	 * Run.
	 * 
	 * @param command
	 *            the command
	 * @param o
	 *            the o
	 */
	public void run(int command, Object... o);

}
