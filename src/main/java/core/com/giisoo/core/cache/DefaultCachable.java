package com.giisoo.core.cache;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class DefaultCachable.
 */
public class DefaultCachable implements Cachable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The _age. */
	private long _age = System.currentTimeMillis();

	private long _expired = -1;

	/** The attachments. */
	protected Map<String, Object> attachments = new TreeMap<String, Object>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.cache.Cachable#elderThan(long)
	 */
	public boolean elderThan(long age) {
		return System.currentTimeMillis() - _age > age;
	}

	/**
	 * Younger than.
	 * 
	 * @param age
	 *            the age
	 * @return true, if successful
	 */
	public boolean youngerThan(long age) {
		return System.currentTimeMillis() - _age < age;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.cache.Cachable#attach(java.lang.String, java.lang.Object)
	 */
	@Override
	public void attach(String key, Object o) {
		attachments.put(key, o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.cache.Cachable#attachment(java.lang.String)
	 */
	@Override
	public Object attachment(String key) {
		return attachments.get(key);
	}

	/**
	 * Cache.
	 * 
	 * @param id
	 *            the id
	 * @throws Exception
	 *             the exception
	 */
	public void cache(String id) throws Exception {
		try {
			Cache.set(new StringBuilder(this.getClass().getName()).append(":")
					.append(id).toString(), this);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Cache.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param id
	 *            the id
	 * @param clazz
	 *            the clazz
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Cachable> T cache(String id, Class<T> clazz) {
		Cachable o = Cache.get(new StringBuilder(clazz.getName()).append(":")
				.append(id).toString());
		if (o != null && clazz.equals(o.getClass())) {
			return (T) o;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.cache.Cachable#age()
	 */
	@Override
	public long age() {
		return System.currentTimeMillis() - _age;
	}

	@Override
	public void setExpired(int t) {
		_expired = t * 1000;
	}

	public boolean expired() {
		return _expired > 0 && (System.currentTimeMillis() - _age > _expired);
	}
}