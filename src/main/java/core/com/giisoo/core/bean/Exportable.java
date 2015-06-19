package com.giisoo.core.bean;

import java.io.IOException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @author joe
 * 
 */
public interface Exportable {

	/**
	 * output the data to the zip stream, and return back to a json
	 * 
	 * @param where
	 * @param args
	 * @param out
	 * @return JSONObject
	 * @throws IOException
	 */
	JSONObject output(String where, Object[] args, ZipOutputStream out)
			throws IOException;

	/**
	 * input the data from the jsonarray and zip
	 * 
	 * @param list
	 * @param zip
	 * @return int
	 * @throws IOException
	 */
	int input(JSONArray list, ZipFile zip) throws IOException;

	/**
	 * load the data by parameters
	 * 
	 * @param where
	 * @param args
	 * @param s
	 * @param n
	 * @return Beans<T>
	 */
	<T extends Bean> Beans<T> load(String where, Object[] args, int s, int n);

	/**
	 * get the exportable id
	 * 
	 * @return String
	 */
	String getExportableId();

	/**
	 * get the exportable name
	 * 
	 * @return String
	 */
	String getExportableName();

	/**
	 * get the updated of the data
	 * 
	 * @return long
	 */
	long getExportableUpdated();

	/**
	 * check the data is exportable
	 * 
	 * @return boolean
	 */
	boolean isExportable();
}
