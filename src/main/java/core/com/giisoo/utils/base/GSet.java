package com.giisoo.utils.base;

import java.util.HashSet;

// TODO: Auto-generated Javadoc
/**
 * The Class GSet.
 *
 * @param <E> the element type
 */
public class GSet<E> extends HashSet<E> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Put.
	 *
	 * @param e the e
	 * @return the g set
	 */
	public GSet<E> put(E... e) {
		for (E e1 : e) {
			add(e1);
		}
		return this;
	}

	/**
	 * Put.
	 *
	 * @param e the e
	 * @return the g set
	 */
	public GSet<E> put(E e) {
		add(e);
		return this;
	}
}
