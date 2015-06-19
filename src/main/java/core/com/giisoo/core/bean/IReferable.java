package com.giisoo.core.bean;

import net.sf.json.JSONObject;

// TODO: Auto-generated Javadoc
/**
 * 
 * The Interface IReferable.
 * 
 * @deprecated
 */
public interface IReferable {

	/**
	 * output to JSON Object
	 * 
	 * @param jo
	 * @return boolean
	 */
	boolean toJSON(JSONObject jo);

	/**
	 * input from JSON Object
	 * 
	 * @param jo
	 * @return boolean
	 */
	boolean fromJSON(JSONObject jo);

	/**
	 * load it from database by ...
	 * 
	 * @param where
	 * @param o
	 * @return boolean
	 */
	boolean load(String where, Object o);

	/**
	 * get the display name, for the object, and which should be include a <a>
	 * link to access it
	 * 
	 * @return String
	 */
	String getDisplay();

}
