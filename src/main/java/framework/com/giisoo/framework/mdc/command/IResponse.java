/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc.command;

import com.giisoo.core.bean.X;

import net.sf.json.JSONObject;

public interface IResponse {

	final static public int STATE_OK = X.OK_200;
	final static public int STATE_FAIL = X.FAIL;

	/**
	 * On activate.
	 * 
	 * @param state
	 *            the state
	 * @param clientid
	 *            the clientid
	 * @param pubkey
	 *            the pubkey
	 */
	public void onActivate(int state, String clientid, String pubkey);

	/**
	 * On hello.
	 * 
	 * @param state
	 *            the state
	 * @param key
	 *            the key
	 */
	public void onHello(int state, byte[] key);

	/**
	 * On login.
	 * 
	 * @param state
	 *            the state
	 * @param in
	 *            the in
	 */
	public void onLogin(int state, JSONObject in);

	/**
	 * On connected.
	 */
	public void onConnected();

	/**
	 * On disconnected.
	 */
	public void onDisconnected();

	/**
	 * On response.
	 * 
	 * @param state
	 *            the state
	 * @param seq
	 *            the seq
	 * @param in
	 *            the in
	 * @param bb
	 *            the bb
	 */
	public void onResponse(int state, long seq, JSONObject in, byte[] bb);

	/**
	 * On timeout.
	 * 
	 * @param seq
	 *            the seq
	 */
	public void onTimeout(long seq);

}
