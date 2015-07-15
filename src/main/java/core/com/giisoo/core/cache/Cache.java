package com.giisoo.core.cache;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.*;

import com.danga.MemCached.*;
import com.giisoo.framework.web.Module;

// TODO: Auto-generated Javadoc
/**
 * The Class Cache.
 */
public class Cache {

	/** The log. */
	private static Log log = LogFactory.getLog(Cache.class);

	/** The mem cached client. */
	private static MemCachedClient memCachedClient;

	/** The _conf. */
	private static Configuration _conf;

	/**
	 * Inits the.
	 * 
	 * @param conf
	 *            the conf
	 */
	public static synchronized void init(Configuration conf) {
		if (_conf != null)
			return;

		_conf = conf;

		if (conf.containsKey("memcached.host")) {
			String[] servers = conf.getStringArray("memcached.host");
			SockIOPool pool = SockIOPool.getInstance();
			pool.setServers(servers);
			pool.setFailover(true);
			pool.setInitConn(10);
			pool.setMinConn(5);
			pool.setMaxConn(100);
			pool.setMaintSleep(30);
			pool.setNagle(false);
			pool.setSocketTO(3000);
			pool.setAliveCheck(true);
			pool.initialize();

			memCachedClient = new MemCachedClient();
		} else {
			FileCache.init(conf);
		}
	}

	/**
	 * Gets the.
	 * 
	 * @param id
	 *            the id
	 * @return the cachable
	 */
	public static Cachable get(String id) {
		try {

			/**
			 * must using my class loader, otherwise
			 */
			Thread thread = Thread.currentThread();
			if (Module.classLoader != null
					&& thread.getContextClassLoader() != Module.classLoader) {
				thread.setContextClassLoader(Module.classLoader);
			}

			// log.debug("contextclassloader.cache="
			// + Thread.currentThread().getContextClassLoader());

			Cachable r = null;
			if (memCachedClient != null) {
				r = (Cachable) memCachedClient.get(id);
			} else {
				r = (Cachable) FileCache.get(id);
			}
			if (r != null) {
				if (r.expired()) {
					if (memCachedClient != null) {
						memCachedClient.delete(id);
					} else {
						FileCache.delete(id);
					}
				}
			}

			return r;
		} catch (Throwable e) {
			if (memCachedClient != null) {
				memCachedClient.delete(id);
			} else {
				FileCache.delete(id);
			}
			log.warn("nothing get from memcache by " + id + ", remove it!");
		}
		return null;
	}

	/**
	 * Removes the.
	 * 
	 * @param id
	 *            the id
	 * @return true, if successful
	 */
	public static boolean remove(String id) {
		if (memCachedClient != null) {
			return memCachedClient.delete(id);
		} else {
			return FileCache.delete(id);
		}
	}

	/**
	 * Sets the.
	 * 
	 * @param id
	 *            the id
	 * @param data
	 *            the data
	 * @return true, if successful
	 */
	public static boolean set(String id, Cachable data) {
		if (memCachedClient != null) {
			if (data == null) {
				return memCachedClient.delete(id);
			} else {
				return memCachedClient.set(id, data);
			}
		} else {
			return FileCache.set(id, data);
		}
	}

}
