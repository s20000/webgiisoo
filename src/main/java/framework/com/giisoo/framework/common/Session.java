/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.*;

import com.giisoo.core.bean.X;
import com.giisoo.core.cache.*;
import com.giisoo.framework.web.Module;

/**
 * Session of http request
 * 
 * @author yjiang
 * 
 */
public class Session extends DefaultCachable implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Log log = LogFactory.getLog(Session.class);

	String sid;

	Map<String, Object> a = new TreeMap<String, Object>();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new StringBuilder(sid).append(":").append(a).toString();
	}

	/**
	 * Exists.
	 * 
	 * @param sid
	 *            the sid
	 * @return true, if successful
	 */
	public static boolean exists(String sid) {
		Session o = null;
		try {
			o = (Session) Cache.get("session-" + sid);
		} catch (Exception e) {
		}
		return o != null;
	}

	/**
	 * Delete.
	 * 
	 * @param sid
	 *            the sid
	 */
	public static void delete(String sid) {
		Cache.remove("session-" + sid);
	}

	/**
	 * Load.
	 * 
	 * @param sid
	 *            the sid
	 * @return the session
	 */
	public static Session load(String sid) {
		Session o = null;
		try {
			o = (Session) Cache.get("session-" + sid);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		if (o == null) {
			o = new Session();

			/**
			 * set the session expired time
			 */
			o.setExpired(Module._conf.getInt("session.expired",
					(int) (X.AYEAR / 1000)));

			o.sid = sid;

		}

		return o;
	}

	/**
	 * Checks for.
	 * 
	 * @param key
	 *            the key
	 * @return true, if successful
	 */
	public boolean has(String key) {
		return a.containsKey(key);
	}

	/**
	 * Removes the.
	 * 
	 * @param key
	 *            the key
	 * @return the session
	 */
	public Session remove(String key) {
		a.remove(key);
		return this;
	}

	/**
	 * Store.
	 * 
	 * @return the session
	 */
	public Session store() {
		if (!Cache.set("session-" + sid, this)) {
			log.error("set session failed !", new Exception(
					"store session failed"));
		}

		return this;
	}

	/**
	 * Sets the.
	 * 
	 * @param key
	 *            the key
	 * @param o
	 *            the o
	 * @return the session
	 */
	public Session set(String key, Object o) {
		a.put(key, o);
		return this;
	}

	/**
	 * Sid.
	 * 
	 * @return the string
	 */
	public String sid() {
		return sid;
	}

	/**
	 * Gets the.
	 * 
	 * @param key
	 *            the key
	 * @return the object
	 */
	public Object get(String key) {
		return (Object) a.get(key);
	}

	/**
	 * Gets the int.
	 * 
	 * @param key
	 *            the key
	 * @return the int
	 */
	public int getInt(String key) {
		Integer i = (Integer) a.get(key);
		if (i != null) {
			return i;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		sid = (String) in.readObject();
		a = (Map<String, Object>) in.readObject();
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(sid);
		out.writeObject(a);
	}

}
