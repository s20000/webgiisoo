package com.giisoo.core.rpc;

import java.io.IOException;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * parallel stub pool.
 *
 * @author yjiang
 */
public class ParallelStubPool extends StubPool {

	/** The pools. */
	private List<StubPool> pools;

	/**
	 * create stub pool for urls, [size] of stub in each pool.
	 *
	 * @param urls the urls
	 * @param size the size
	 * @return the stub pool
	 * @deprecated 
	 */
	public static StubPool create(List<String> urls, int size) {
		return create(urls, size, 60);
	}

	/**
	 * Creates the.
	 *
	 * @param urls the urls
	 * @param size the size
	 * @param timeout          seconds
	 * @return the stub pool
	 */
	public static StubPool create(List<String> urls, int size, int timeout) {
		ParallelStubPool p = new ParallelStubPool();
		p.pools = new ArrayList<StubPool>(urls.size());
		for (String url : urls) {
			try {
				p.pools.add(StubPool.create(url, size, timeout));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		return p;
	}

	/* (non-Javadoc)
	 * @see com.giisoo.rpc.StubPool#close()
	 */
	@Override
	public void close() {
		for (StubPool p : pools) {
			p.close();
		}

		pools.clear();
	}

	/* (non-Javadoc)
	 * @see com.giisoo.rpc.StubPool#get()
	 */
	@Override
	public Stub get() throws IOException {
		Stub p = null;
		int attempt = 0;
		while (p == null) {
			int max = Integer.MIN_VALUE;
			StubPool pool = null;
			synchronized (pools) {
				for (int i = pools.size() - 1; i >= 0; i--) {
					StubPool p1 = pools.get(i);
					int avaliable = p1.available();
					if (avaliable > max) {
						max = avaliable;
						pool = p1;
					}
				}
			}

			if (pool == null)
				return null;

			p = pool.get();
			if (p == null) {
				if (attempt >= 3) {
					return null;
				}

				attempt++;
				synchronized (pool) {
					try {
						pool.wait((int) (3000 * Math.random()));
					} catch (InterruptedException e) {
						log.error(e);
					}
				}
			} else {
				p.attachment("pool", pool);
			}
		}

		return p;
	}

	/* (non-Javadoc)
	 * @see com.giisoo.rpc.StubPool#release(com.giisoo.rpc.Stub)
	 */
	@Override
	protected void release(Stub s) {
		if (s == null)
			return;

		Object o = s.attachment("pool");
		if (o != null && o instanceof StubPool) {
			((StubPool) o).release(s);
		}
	}

}
