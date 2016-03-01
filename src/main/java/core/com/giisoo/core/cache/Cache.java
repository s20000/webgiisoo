package com.giisoo.core.cache;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.*;

import com.danga.MemCached.*;

/**
 * The {@code Cache} Class Cache used for cache object, the cache was grouped by
 * cluster
 * <p>
 * configuration in giisoo.properties
 * 
 * <pre>
 * cache.url=memcached://host:port
 * cache.group=demo
 * </pre>
 * 
 * @author joe
 *
 */
public class Cache {

    /** The log. */
    private static Log log = LogFactory.getLog(Cache.class);

    final static private String MEMCACHED = "memcached://";
    private static String GROUP = "g://";

    private static MemCachedClient memCachedClient;

    private static Configuration _conf;

    /**
     * initialize the cache with configuration
     * 
     * @param conf
     *            the configuration that includes cache configure ("cache.url")
     */
    public static synchronized void init(Configuration conf) {
        if (_conf != null)
            return;

        _conf = conf;

        if (conf.containsKey("cache.url")) {
            String server = conf.getString("cache.url");
            if (server.startsWith(MEMCACHED)) {
                SockIOPool pool = SockIOPool.getInstance();
                pool.setServers(new String[] { server.substring(MEMCACHED.length()) });
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
            }
        } else {
            FileCache.init(conf);
        }

        if (conf.containsKey("cache.group")) {
            GROUP = conf.getString("cache.group", "demo") + "://";
        }
    }

    /**
     * Gets the object by id, if the object was expired, null return
     * 
     * @param id
     *            the id of object in cache system
     * @return cachable if the object not presented or expired, will return null
     */
    public static Cachable get(String id) {

        try {

            // /**
            // * must using my class loader, otherwise
            // */
            // Thread thread = Thread.currentThread();
            // if (Module.classLoader != null
            // && thread.getContextClassLoader() != Module.classLoader) {
            // thread.setContextClassLoader(Module.classLoader);
            // }

            // log.debug("contextclassloader.cache="
            // + Thread.currentThread().getContextClassLoader());

            id = GROUP + id;

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

                    return null;
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
     * Removes the cached object by id
     * 
     * @param id
     *            the object id in cache
     * @return true, if successful
     */
    public static boolean remove(String id) {
        id = GROUP + id;
        if (memCachedClient != null) {
            return memCachedClient.delete(id);
        } else {
            return FileCache.delete(id);
        }
    }

    /**
     * cache the object with the id, if exists, then update it, otherwise create
     * new in cache
     * 
     * @param id
     *            the id of the object
     * @param data
     *            the object
     * @return true, if successful
     */
    public static boolean set(String id, Cachable data) {

        id = GROUP + id;

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
