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
	 * set the expired time by second, -1 never expired
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
