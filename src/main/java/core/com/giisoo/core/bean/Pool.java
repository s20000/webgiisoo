package com.giisoo.core.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Pool<E> {
	static Log log = LogFactory.getLog(Pool.class);

	private List<E> list = new ArrayList<E>();
	private int initial = 10;
	private int max = 10;
	private int created = 0;
	private ICreator creator;

	public static <T> Pool<T> create(int initial, int max, ICreator creator) {
		Pool<T> p = new Pool<T>();
		p.initial = initial;
		p.max = max;
		p.creator = creator;
		p.init();
		return p;
	}

	private synchronized void init() {
		for (int i = 0; i < initial; i++) {
			E t = (E)creator.create();
			if (t != null) {
				list.add(t);
			}
		}
		created = list.size();
	}

	public synchronized void release(E t) {
		if (t == null) {
			created--;
		} else {
			creator.cleanup(t);
			list.add(t);
		}
	}

	public synchronized E get(long timeout) {
		try {
			if (list.size() == 0) {
				if (created < max) {
					E t = (E)creator.create();
					if (t != null) {
						created++;
						return t;
					}
				}
				this.wait(timeout);
			}

			if (list.size() > 0) {
				return list.remove(0);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	public interface ICreator<T> {
		public T create();

		public void cleanup(T t);

	}
}
