package com.giisoo.core.index;

import java.rmi.RemoteException;
import java.util.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.*;

import com.giisoo.core.cache.Cache;
import com.giisoo.core.rpc.*;
import com.giisoo.core.worker.WorkerManager;

// TODO: Auto-generated Javadoc
/**
 * The Class ProxyEngine.
 */
public class ProxyEngine {

  /** The Constant log. */
  static final Log log = LogFactory.getLog(ProxyEngine.class);

  /** The conf. */
  static Configuration conf;
  
  /** The host. */
  static String host = "127.0.0.1";
  
  /** The port. */
  static int port = 20090;
  
  /** The plse. */
  static IRemote plse;

  /**
   * Inits the.
   *
   * @param conf the conf
   * @param clazz the clazz
   * @return true, if successful
   */
  public static boolean init(Configuration conf, List<Class<? extends Searchable>> clazz) {
    if (ProxyEngine.conf == null) {
      ProxyEngine.conf = conf;

      try {
        if (conf.containsKey("proxy.host")) {
          String url = conf.getString("proxy.host");
          String[] ss = url.split(":");
          if (ss.length == 2) {
            host = ss[0];
            port = Integer.parseInt(ss[1]);
          }

          Stub s = Stub.create(host, port);
          plse = new ProxyStub();
          s.bind(plse);

          Cache.init(conf);

          return true;

        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }

    return false;
  }

  /**
   * Start.
   */
  public static void start() {

  }

  /**
   * Shutdown.
   */
  public static void shutdown() {
    log.warn("Proxy is downing...");
    WorkerManager.stop(true);
    SpatialIndex.close();
  }

  /**
   * The Class ProxyStub.
   */
  public static class ProxyStub extends IRemote {

    /**
     * Load.
     *
     * @param <T> the generic type
     * @param ids the ids
     * @param clazz the clazz
     * @return the map
     * @throws RemoteException the remote exception
     */
    public <T extends Searchable> Map<String, T> load(Set<String> ids, Class<T> clazz) throws RemoteException {
      Map<String, T> maps = new HashMap<String, T>();
      for (String id : ids) {
        try {
          T t = clazz.newInstance();
          if (t.load(id) && t.isValid()) {
            maps.put(id, t);
          }
        } catch (Exception e) {
          log.error(e.getMessage(), e);
        }
      }
      return maps;
    }

    /**
     * Search by wd.
     *
     * @param clazz the clazz
     * @param q the q
     * @param start the start
     * @param number the number
     * @return the search results
     * @throws RemoteException the remote exception
     */
    @SuppressWarnings("rawtypes")
    public SearchResults searchByWd(Class<? extends Searchable> clazz, String q, int start, int number) throws RemoteException {
      return SpatialIndex.search(clazz, q, start, number, null);
    }

    /**
     * Search by wd width stat.
     *
     * @param clazz the clazz
     * @param q the q
     * @param start the start
     * @param number the number
     * @param stats the stats
     * @return the search results
     * @throws RemoteException the remote exception
     */
    @SuppressWarnings("rawtypes")
    public SearchResults searchByWdWidthStat(Class<? extends Searchable> clazz, String q, int start, int number, List<Stat> stats) throws RemoteException {
      return SpatialIndex.search(clazz, q, start, number, stats);
    }

    /**
     * Search by loc.
     *
     * @param clazz the clazz
     * @param q the q
     * @param lat the lat
     * @param lng the lng
     * @param range the range
     * @param start the start
     * @param number the number
     * @return the search results
     * @throws RemoteException the remote exception
     */
    @SuppressWarnings("rawtypes")
    public SearchResults searchByLoc(Class<? extends Searchable> clazz, String q, double lat, double lng, double range, int start, int number) throws RemoteException {
      return SpatialIndex.search(clazz, q, lat, lng, range / 1000, start, number);
    }
  }

}
