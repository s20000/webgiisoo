package com.giisoo.core.bean;

import net.sf.json.JSONObject;

public interface IData {

	public static byte OP_CREATE = 0;

	public static byte OP_UPDATE = 1;

	public static byte OP_DELETE = 2;

	/**
	 * @param table
	 * @param op
	 * @param refer
	 */
	public void onChanged(String table, byte op, JSONObject refer);

}
