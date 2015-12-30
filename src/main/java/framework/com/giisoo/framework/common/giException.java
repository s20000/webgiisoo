/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

public class giException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int state;

	/**
	 * Instantiates a new gi exception.
	 * 
	 * @param state
	 *            the state
	 * @param message
	 *            the message
	 */
	public giException(int state, String message) {
		super(message);
		this.state = state;
	}

	public int getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
        String s = getClass().getName() + ":" + state;
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
	}
	
	
}
