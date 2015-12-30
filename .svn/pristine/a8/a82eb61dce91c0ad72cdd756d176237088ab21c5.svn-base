package com.giisoo.core.cache;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Interface Cachable.
 */
public interface Cachable extends Serializable {

	/**
	 * Age.
	 * 
	 * @return the long
	 */
	public long age();

	/**
	 * Elder than.
	 * 
	 * @param age
	 *            the age
	 * @return true, if successful
	 */
	public boolean elderThan(long age);

	/**
	 * Younger than.
	 * 
	 * @param age
	 *            the age
	 * @return true, if successful
	 */
	public boolean youngerThan(long age);

	/**
	 * Attach.
	 * 
	 * @param key
	 *            the key
	 * @param o
	 *            the o
	 */
	public void attach(String key, Object o);

	/**
	 * Attachment.
	 * 
	 * @param key
	 *            the key
	 * @return the object
	 */
	public Object attachment(String key);

	/**
	 * set the expired time by second, <=0 never expired
	 * 
	 * @param t
	 */
	public void setExpired(int t);

	/**
	 * check whether expired
	 * 
	 * @return boolean
	 */
	public boolean expired();

}
