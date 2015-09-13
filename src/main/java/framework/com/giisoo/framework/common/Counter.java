/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

import com.giisoo.core.bean.Bean;

public class Counter extends MBeanInfo {

	public Counter(String className, String description,
			MBeanAttributeInfo[] attributes,
			MBeanConstructorInfo[] constructors,
			MBeanOperationInfo[] operations,
			MBeanNotificationInfo[] notifications)
			throws IllegalArgumentException {
		
		super(className, description, attributes, constructors, operations,
				notifications);
		// TODO Auto-generated constructor stub

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static long currenttime = 0;
	
	private static Map<String, Integer> counters = new HashMap<String, Integer>();
	private static Map<String, Object[]> maxs = new HashMap<String, Object[]>();

	public static Map<String, Integer> getCounters() {
		return counters;
	}

	/**
	 * To string.
	 * 
	 * @param sb
	 *            the sb
	 */
	public static void toString(StringBuilder sb) {
		sb.append("---------------------------------\r\n");
		for (String name : counters.keySet()) {
			sb.append(name).append("=").append(counters.get(name))
					.append("\r\n");
		}

		sb.append("---------------------------------\r\n");
		for (String name : maxs.keySet()) {
			sb.append(name).append("=").append(Bean.toString(maxs.get(name)))
					.append("\r\n");
		}
	}

	/**
	 * Reset.
	 * 
	 * @param name
	 *            the name
	 */
	public static void reset(String name) {
		counters.remove(name);
	}

	/**
	 * Gets the.
	 * 
	 * @param name
	 *            the name
	 * @return the int
	 */
	public static int get(String name) {
		if (counters.containsKey(name)) {
			return counters.get(name);
		}

		return -1;
	}

	/**
	 * Adds the.
	 * 
	 * @param name
	 *            the name
	 * @param count
	 *            the count
	 * @return the int
	 */
	public synchronized static int add(String name, int count) {
		if (counters.containsKey(name)) {
			count = counters.get(name) + count;
		}

		counters.put(name, count);
		return count;
	}

	/**
	 * Max.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @param memo
	 *            the memo
	 * @return the long
	 */
	public static long max(String name, long value, String memo) {
		Object[] o = maxs.get(name);
		if (o == null) {
			o = new Object[2];
		} else {
			long i = (Long) o[0];
			if (i > value)
				return i;
		}
		o[0] = value;
		o[1] = memo;
		maxs.put(name, o);

		return value;
	}

	/**
	 * Max.
	 * 
	 * @param name
	 *            the name
	 * @return the string
	 */
	public static String max(String name) {
		Object[] o = maxs.get(name);
		StringBuilder sb = new StringBuilder();
		if (o != null) {
			sb.append("max=").append(o[0]).append(",memo=").append(o[1]);
		} else {
			sb.append("max=,memo=");
		}
		return sb.toString();
	}
}
