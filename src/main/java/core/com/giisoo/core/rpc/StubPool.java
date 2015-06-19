package com.giisoo.core.rpc;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class StubPool.
 *
 * @author yjiang
 */
public class StubPool {
	
	/** The log. */
	static Log log = LogFactory.getLog(StubPool.class);

	/** The Constant INTERVAL. */
	final static long INTERVAL = 20 * 1000;
	
	/** The Constant ATTEMPT_TIMES. */
	final static int ATTEMPT_TIMES = 20;

	/** linkedlist is better than arraylist due to frequently remove/add elements from/in it. */
	private TreeMap<Long, Stub> stubMap = new TreeMap<Long, Stub>();
	
	/** The url. */
	private String url;
	
	/** The semaphore. */
	private Semaphore semaphore;
	
	/** The timeout. */
	private int timeout = 60;
	
	/** The pool size. */
	private int poolSize = 20;

	/**
	 * create a stub pool to the [url] with [size] of stub
	 * 
	 * please refers to create(url, poolSize, timeout).
	 *
	 * @param url the url
	 * @param poolSize the pool size
	 * @return the stub pool
	 * @throws Exception the exception
	 * @deprecated 
	 */
	public static StubPool create(String url, int poolSize) throws Exception {
		return create(url, poolSize, 60);
	}

	/**
	 * Creates the.
	 *
	 * @param url the url
	 * @param poolSize the pool size
	 * @param timeout the timeout
	 * @return the stub pool
	 * @throws Exception the exception
	 */
	public static StubPool create(String url, int poolSize, int timeout) throws Exception {
		StubPool pool = new StubPool();
		pool.url = url;
		pool.timeout = timeout;
		pool.semaphore = new Semaphore(poolSize);
		pool.poolSize = poolSize;

		for (int i = 0; i < poolSize; i++) {
			Stub astub = Stub.connect(url, timeout);
			astub.attachment("seq", pool.seq);
			astub.attachment("pool", pool);
			pool.stubMap.put(System.currentTimeMillis(), astub);
		}

		return pool;
	}

	/**
	 * get a stub from pool, return an available stub or null until trying attempt_times;.
	 *
	 * @return the stub
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Stub get() throws IOException {
		Stub s = null;

		try {
			for (int i = 0; i < ATTEMPT_TIMES; i++) {

				semaphore.acquire();

				/**
				 * get an available one and return
				 */
				synchronized (stubMap) {
					while (stubMap.size() > 0) {
						/**
						 * get last one as used in recently
						 */

						Long k = stubMap.lastKey();
						s = stubMap.remove(k);
						if (s.available() && s.test()) {
							return s;
						} else {
							s.close();
						}
					}
				}

				/**
				 * otherwise create a new one
				 */
				s = Stub.connect(url, timeout);
				if (s != null) {
					s.attachment("seq", seq);
					s.attachment("pool", this);
					return s;
				} else {

					/**
					 * release the semaphore to others and sleep a while;
					 */
					semaphore.release();
					Thread.sleep(1000);// sleep 1 seconds.
				}
			}
		} catch (InterruptedException e) {
			log.info(Thread.currentThread().getName() + " is interrupted while waiting for stub pool");
		} finally {
			if (s == null) {
				semaphore.release();
			}
		}

		return s;
	}

	/**
	 * trying to get one, and return immediately, null will be return if not available.
	 *
	 * @return the stub
	 */
	protected Stub tryGet() {
		Stub s = null;
		try {
			if (semaphore.tryAcquire()) {

				/**
				 * get an available one and return
				 */
				synchronized (stubMap) {
					while (stubMap.size() > 0) {
						s = stubMap.remove(0);
						if (s.available() && s.test()) {
							return s;
						}
					}
				}

				s = null;
				/**
				 * otherwise create a new one
				 */
				s = Stub.connect(url);
				if (s != null) {
					s.attachment("seq", seq);
					s.attachment("pool", this);
				}
			}
		} catch (Throwable e) {
			log.info(Thread.currentThread().getName() + " is interrupted while waiting for stub pool");
		} finally {
			if (s == null) {
				semaphore.release();
			}
		}

		return s;
	}

	/**
	 * release a stub to pool.
	 *
	 * @param s the s
	 */
	protected void release(Stub s) {
		if (s == null)
			return;

		Integer seq = (Integer) s.attachment("seq");
		if (seq == null || seq == this.seq) {
			/**
			 * check whether the stub is avaliable
			 */
			semaphore.release();

			if (s.available()) {
				synchronized (stubMap) {
					stubMap.put(System.currentTimeMillis(), s);
				}
			}
		}
	}

	/**
	 * there is a bug there. in case of: a stub is in use and it doesn't present in the list
	 */
	public void close() {
		synchronized (stubMap) {
			if (stubMap.size() > 0) {
				for (Stub aStub : stubMap.values()) {
					aStub.close();
				}

				stubMap.clear();
			}
		}
	}

	/**
	 * return how many available stub.
	 *
	 * @return the int
	 */
	public int available() {
		if (badExpire > System.currentTimeMillis()) {
			return 0;
		} else {
			return semaphore.availablePermits();
		}
	}

	/**
	 * set the pool is bad.
	 */
	public void bad() {
		badExpire = System.currentTimeMillis() + 60 * 1000; // 1 minute
	}

	/**
	 * Checks if is bad.
	 *
	 * @return true, if is bad
	 */
	public boolean isBad() {
		return badExpire > System.currentTimeMillis();
	}

	/**
	 * Restart.
	 */
	public void restart() {
		seq++;
		semaphore = new Semaphore(poolSize);
		synchronized (stubMap) {
			stubMap.clear();
			for (int i = 0; i < poolSize; i++) {
				try {
					Stub astub = Stub.connect(url, timeout);
					astub.attachment("seq", seq);
					astub.attachment("pool", this);
					stubMap.put(System.currentTimeMillis(), astub);
				} catch (IOException e) {
					log.error(e);
				}
			}
		}
	}

	/** The seq. */
	private int seq = 1;
	
	/** The bad expire. */
	private long badExpire = 0;

}
