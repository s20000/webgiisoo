package com.giisoo.utils.base;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GDate.
 */
public class GDate {
	
	/** The Constant BREAK. */
	static final private String BREAK = "[/-_年月日时分秒点整:hms]";

	/**
	 * Parses the.
	 *
	 * @param datetime the datetime
	 * @return the long
	 */
	public static long parse(String datetime) {

		if (datetime == null || datetime.length() == 0) {
			return System.currentTimeMillis();
		}

		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(System.currentTimeMillis());

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0);

		StringTokenizer dt = new StringTokenizer(datetime, "[ 日整]", false);
		if (dt.hasMoreTokens()) {
			String s = dt.nextToken();
			StringTokenizer st = new StringTokenizer(s, BREAK, false);
			if (st.countTokens() > 2) {
				// 年月日
				int y = toInt(st.nextToken(), 0);
				if (y > 1000) {
					c.set(Calendar.YEAR, y);
				} else if (y % 100 <= today.get(Calendar.YEAR) % 100) {
					// this year
					c.set(Calendar.YEAR, today.get(Calendar.YEAR) / 100 * 100 + y);
				} else {
					// last year
					c.set(Calendar.YEAR, y + (today.get(Calendar.YEAR) / 100 - 1) * 100);
				}
				int m = toInt(st.nextToken(), 1) - 1;
				c.set(Calendar.MONTH, m);
				int d = toInt(st.nextToken(), 1);
				c.set(Calendar.DAY_OF_MONTH, d);
			} else if (st.countTokens() > 1) {
				// 月日
				int m = toInt(st.nextToken(), 1) - 1;
				int d = toInt(st.nextToken(), 1);

				if (m > today.get(Calendar.MONTH)) {
					// last year
					c.set(Calendar.YEAR, today.get(Calendar.YEAR) - 1);
				} else {
					// this year
					c.set(Calendar.YEAR, today.get(Calendar.YEAR));
				}

				c.set(Calendar.MONTH, m);
				c.set(Calendar.DAY_OF_MONTH, d);
			} else if (st.countTokens() == 1) {
				// this year
				long d = toLong(st.nextToken(), 1);
				if (d > 10000) {
					// it's millis seconds
					c.setTimeInMillis(d);
				} else {
					if (d > today.get(Calendar.DAY_OF_MONTH)) {
						// last month
						c.set(Calendar.MONTH, today.get(Calendar.MONTH) - 1);
					} else {
						c.set(Calendar.MONTH, today.get(Calendar.MONTH));
					}
					c.set(Calendar.DAY_OF_MONTH, (int) d);
					c.set(Calendar.YEAR, today.get(Calendar.YEAR));
				}
			}
		}

		// System.out.println(datetime + "/" + c.getTimeInMillis());
		if (dt.hasMoreTokens()) {
			String t = dt.nextToken();
			// System.out.println(t);

			StringTokenizer st1 = new StringTokenizer(t, BREAK, false);
			if (st1.hasMoreTokens()) {
				c.set(Calendar.HOUR, toInt(st1.nextToken(), 0));
			}
			if (st1.hasMoreTokens()) {
				c.set(Calendar.MINUTE, toInt(st1.nextToken(), 0));
			}
			if (st1.hasMoreTokens()) {
				c.set(Calendar.SECOND, toInt(st1.nextToken(), 0));
			}
		}
		// System.out.println(datetime + "/" + c.getTimeInMillis());

		return c.getTimeInMillis();
	}

	/**
	 * To int.
	 *
	 * @param s the s
	 * @param d the d
	 * @return the int
	 */
	private static int toInt(String s, int d) {
		try {
			// System.out.println(s);
			return Integer.parseInt(s);
		} catch (Throwable e) {
			return d;
		}
	}

	/**
	 * To long.
	 *
	 * @param s the s
	 * @param d the d
	 * @return the long
	 */
	private static long toLong(String s, long d) {
		try {
			return Long.parseLong(s);
		} catch (Throwable e) {
			return d;
		}
	}
}
