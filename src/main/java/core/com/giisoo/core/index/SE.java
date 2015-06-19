package com.giisoo.core.index;

import java.util.*;

import org.apache.commons.logging.*;

import com.giisoo.core.rpc.*;

// TODO: Auto-generated Javadoc
/**
 * The Class SE.
 * 
 * @param <T>
 *            the generic type
 */
public class SE<T extends Searchable> {

	/** The Constant log. */
	static final Log log = LogFactory.getLog(SE.class);

	/** The Constant ATTEMPT. */
	static final int ATTEMPT = 3;

	/** The pool. */
	StubPool pool;

	/** The clazz. */
	Class<? extends Searchable> clazz;

	/**
	 * Connect.
	 * 
	 * @param url
	 *            the url
	 * @param conns
	 *            the conns
	 * @return the stub pool
	 */
	public static StubPool connect(String url, int conns) {
		try {
			return StubPool.create(url, conns, 60);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * url: se://host:port.
	 * 
	 * @param connection
	 *            the connection
	 * @param clazz
	 *            the clazz
	 */
	public SE(StubPool connection, Class<? extends Searchable> clazz) {
		try {
			pool = connection;
			this.clazz = clazz;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		log.info("PLSE initialied, url:" + pool.toString());
	}

	/**
	 * Load.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @param clazz
	 *            the clazz
	 * @return the map
	 */
	@SuppressWarnings("hiding")
	public <T extends Searchable> Map<String, T> load(Set<String> ids,
			Class<T> clazz) {

		if (pool == null) {
			return null;
		}

		Stub stub = null;

		/**
		 * attempt to call the server in n times to get the result
		 */
		for (int i = 0; i < ATTEMPT; i++) {
			stub = null;
			try {
				stub = pool.get();
				if (stub != null) {
					Object o = stub.call("load", ids, clazz);

					if (o instanceof Map) {
						return (Map<String, T>) o;
					} else {
						stub.available(false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if (stub != null) {
					stub.release();
				}
			}
		}
		return null;
	}

	/**
	 * Search by wd.
	 * 
	 * @param q
	 *            the q
	 * @param start
	 *            the start
	 * @param number
	 *            the number
	 * @return the search results
	 */
	@SuppressWarnings("unchecked")
	public SearchResults<T> searchByWd(String q, int start, int number) {
		// System.out.println(ctype);

		if (pool == null) {
			return null;
		}

		Stub stub = null;

		/**
		 * attempt to call the server in n times to get the result
		 */
		for (int i = 0; i < ATTEMPT; i++) {
			stub = null;
			try {
				stub = pool.get();
				if (stub != null) {
					Object o = stub.call("searchByWd", clazz, q, start, number);

					if (o instanceof SearchResults) {
						return (SearchResults<T>) o;
					} else {
						stub.available(false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if (stub != null) {
					stub.release();
				}
			}
		}
		return null;
	}

	public SearchResults<T> searchByWd(String q, int start, int number,
			boolean must) {
		// System.out.println(ctype);

		if (pool == null) {
			return null;
		}

		Stub stub = null;

		/**
		 * attempt to call the server in n times to get the result
		 */
		for (int i = 0; i < ATTEMPT; i++) {
			stub = null;
			try {
				stub = pool.get();
				if (stub != null) {
					Object o = stub.call("searchByWdWithMust", clazz, q, start,
							number, must);

					if (o instanceof SearchResults) {
						return (SearchResults<T>) o;
					} else {
						stub.available(false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if (stub != null) {
					stub.release();
				}
			}
		}
		return null;
	}

	public SearchResults<T> searchByWd(String q, int start, int number,
			List<Stat> stats, boolean must) {

		// System.out.println(ctype);

		if (pool == null) {
			return null;
		}

		Stub stub = null;

		/**
		 * attempt to call the server in n times to get the result
		 */
		for (int i = 0; i < ATTEMPT; i++) {
			stub = null;
			try {
				stub = pool.get();
				if (stub != null) {
					Object o = stub.call("searchByWdWidthStatMust", clazz, q,
							start, number, stats, must);

					if (o instanceof SearchResults) {
						return (SearchResults<T>) o;
					} else {
						stub.available(false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if (stub != null) {
					stub.release();
				}
			}
		}
		return null;

	}

	/**
	 * Search by wd.
	 * 
	 * @param q
	 *            the q
	 * @param start
	 *            the start
	 * @param number
	 *            the number
	 * @param stats
	 *            the stats
	 * @return the search results
	 */
	@SuppressWarnings("unchecked")
	public SearchResults<T> searchByWd(String q, int start, int number,
			List<Stat> stats) {
		// System.out.println(ctype);

		if (pool == null) {
			return null;
		}

		Stub stub = null;

		/**
		 * attempt to call the server in n times to get the result
		 */
		for (int i = 0; i < ATTEMPT; i++) {
			stub = null;
			try {
				stub = pool.get();
				if (stub != null) {
					Object o = stub.call("searchByWdWidthStat", clazz, q,
							start, number, stats);

					if (o instanceof SearchResults) {
						return (SearchResults<T>) o;
					} else {
						stub.available(false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if (stub != null) {
					stub.release();
				}
			}
		}
		return null;
	}

	/**
	 * Search by loc.
	 * 
	 * @param q
	 *            the q
	 * @param lng
	 *            the lng
	 * @param lat
	 *            the lat
	 * @param range
	 *            the range
	 * @param start
	 *            the start
	 * @param number
	 *            the number
	 * @return the search results
	 */
	@SuppressWarnings("unchecked")
	public SearchResults<T> searchByLoc(String q, double lng, double lat,
			double range, int start, int number) {
		if (pool == null) {
			return null;
		}

		Stub stub = null;

		/**
		 * attempt to call the server in n times to get the result
		 */
		for (int i = 0; i < ATTEMPT; i++) {
			stub = null;
			try {
				stub = pool.get();
				if (stub != null) {
					log.info("calling seachByLoc ...., " + i);
					Object o = stub.call("searchByLoc", clazz, q, lat, lng,
							range, start, number);

					if (o instanceof SearchResults) {
						return (SearchResults<T>) o;
					} else {
						stub.available(false);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if (stub != null) {
					stub.release();
				}
			}
		}
		return null;
	}
}
