/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.logging.*;

import com.giisoo.core.bean.X;
import com.giisoo.framework.common.Dict;
import com.giisoo.utils.base.Html;

/**
 * language data which located at /modules/[module]/i18n/
 * 
 * @author yjiang
 * 
 */
public class Language {

	final static Log log = LogFactory.getLog(Language.class);

	/**
	 * locale of language
	 */
	private String locale;

	/**
	 * the language mapping data
	 */
	private Map<String, String> data = new HashMap<String, String>();

	/**
	 * cache the language data withe locale
	 */
	private static Map<String, Language> locales = new HashMap<String, Language>();

	public static Language getLanguage() {
		return getLanguage(Module.home.getLanguage());
	}

	/**
	 * Prints the.
	 * 
	 * @param format
	 *            the format
	 * @param args
	 *            the args
	 * @return the string
	 */
	public String print(String format, Object... args) {
		return String.format(format, args);
	}

	/**
	 * Clean.
	 */
	public static void clean() {
		locales.clear();
	}

	public String truncate(String s, int length) {
		if (s != null && s.length() > length) {
			return s.substring(0, length - 3)
					+ "<span class='truncated' title='" + s + "'>...</span>";
		}
		return s;
	}

	/**
	 * Color.
	 * 
	 * @param d
	 *            the d
	 * @param bits
	 *            the bits
	 * @return the string
	 */
	public String color(long d, int bits) {
		String s = Long.toHexString(d);
		StringBuilder sb = new StringBuilder();
		for (int i = s.length() - 1; i >= 0; i--) {
			sb.append(s.charAt(i));
		}
		if (sb.length() < bits) {
			for (int i = sb.length(); i < bits; i++) {
				sb.append("0");
			}
			return "#" + sb.toString();
		} else {
			return "#" + sb.substring(0, bits);
		}
	}

	/**
	 * Now.
	 * 
	 * @param format
	 *            the format
	 * @return the string
	 */
	public String now(String format) {
		return format(System.currentTimeMillis(), format);
	}

	/**
	 * Bitmaps.
	 * 
	 * @param f
	 *            the f
	 * @return the list
	 */
	public List<Integer> bitmaps(int f) {
		List<Integer> list = new ArrayList<Integer>();
		int m = 1;
		for (int i = 0; i < 32; i++) {
			if ((m & f) > 0) {
				list.add(m);
			}
			m <<= 1;
		}
		return list.size() > 0 ? list : null;
	}

	/**
	 * Bits.
	 * 
	 * @param f
	 *            the f
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the int
	 */
	public int bits(int f, int s, int n) {
		f = f >> s;
		return f - (f >> n) << n;
	}

	/**
	 * Gets the language.
	 * 
	 * @param locale
	 *            the locale
	 * @return the language
	 */
	protected static Language getLanguage(String locale) {

		if (locales.containsKey(locale)) {
			return locales.get(locale);
		}

		Language l = new Language(locale);
		locales.put(locale, l);
		return l;

	}

	private Language(String locale) {
		this.locale = locale;
		if (!Module.home.supportLocale(locale)) {
			this.locale = Module.home.getLanguage();
		}

		load();
	}

	/**
	 * Checks for.
	 * 
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public boolean has(String name) {
		if (name == null)
			return false;
		return data.containsKey(name);
	}

	/**
	 * Gets the.
	 * 
	 * @param name
	 *            the name
	 * @return the string
	 */
	public String get(String name) {
		if (name == null) {
			return X.EMPTY;
		}

		if (data.containsKey(name)) {
			return data.get(name);
		} else {
			if (name.startsWith("$,")) {
				return name.substring(2);
			} else if (name.startsWith("dict,")) {
				String[] ss = name.split(",");

				StringBuilder sb = new StringBuilder("root");
				for (int i = 1; i < ss.length; i++) {
					if (i == 1 && "root".equals(ss[i]))
						continue;
					sb.append("|").append(ss[i]);
				}
				Dict d = Dict.loadByPath(sb.toString());
				if (d != null) {
					return d.getDisplay();
				} else {
					return sb.toString();
				}
			}

			if (name.indexOf("$") > -1) {
				return null;
			}

			/**
			 * write to lang file
			 */
			// Module m = Model.currentModule();
			// if (m != null) {
			// m.putLang(locale, name);
			// } else {
			// Module.home.putLang(locale, name);
			// }

			data.put(name, name);
			return name;
		}
	}

	/**
	 * Load.
	 */
	public void load() {
		data = new HashMap<String, String>();
		Module.home.loadLang(data, locale);

		if (data.isEmpty()) {
			// log.error("doesnt support the locale: " + locale);
		}
	}

	/**
	 * @deprecated
	 */
	private SimpleDateFormat sdf = null;

	private static Map<String, SimpleDateFormat> formats = new HashMap<String, SimpleDateFormat>();

	/**
	 * Format.
	 * 
	 * @param t
	 *            the t
	 * @param format
	 *            the format
	 * @return the string
	 */
	public String format(String t, String format) {
		return t;
	}

	/**
	 * Format.
	 * 
	 * @param t
	 *            the t
	 * @param format
	 *            the format
	 * @return the string
	 */
	public String format(long t, String format) {
		if (t == 0)
			return X.EMPTY;

		try {
			SimpleDateFormat sdf = formats.get(format);
			if (sdf == null) {
				sdf = new SimpleDateFormat(format);

				// if (data.containsKey("date.timezone")) {
				// sdf.setTimeZone(TimeZone.getTimeZone(get("date.timezone")));
				// }

				formats.put(format, sdf);
			}
			return sdf.format(new Date(t));
		} catch (Exception e) {
			log.error(t, e);
		}
		return X.EMPTY;
	}

	/**
	 * Convert.
	 * 
	 * @param date
	 *            the date
	 * @param from
	 *            the from
	 * @param format
	 *            the format
	 * @return the string
	 */
	public String convert(int date, String from, String format) {
		if (date == 0)
			return X.EMPTY;

		long t = parse(Integer.toString(date), from);
		if (t == 0)
			return X.EMPTY;

		return format(t, format);
	}

	/**
	 * Convert.
	 * 
	 * @param date
	 *            the date
	 * @param from
	 *            the from
	 * @param format
	 *            the format
	 * @return the string
	 */
	public String convert(String date, String from, String format) {
		if (date == null || date.length() < 8) {
			return date;
		}

		long t = parse(date, from);
		if (t == 0)
			return X.EMPTY;

		return format(t, format);
	}

	/**
	 * Parses the.
	 * 
	 * @param t
	 *            the t
	 * @param format
	 *            the format
	 * @return the long
	 */
	public long parse(String t, String format) {
		if (t == null || "".equals(t))
			return 0;

		try {
			SimpleDateFormat sdf = formats.get(format);
			if (sdf == null) {
				sdf = new SimpleDateFormat(format);
				// sdf.setTimeZone(TimeZone.getTimeZone(get("date.timezone")));
				formats.put(format, sdf);
			}
			return sdf.parse(t).getTime();
		} catch (Exception e) {
			log.error(t, e);
		}

		return 0;
	}

	/**
	 * Format.
	 * 
	 * @param t
	 *            the t
	 * @return the string
	 */
	public String format(long t) {
		try {
			if (sdf == null) {
				sdf = new SimpleDateFormat(get("date.format"));
				// sdf.setTimeZone(TimeZone.getTimeZone(get("date.timezone")));
			}
			return sdf.format(new Date(t));
		} catch (Exception e) {
			log.error("t=" + t + ", format:" + get("date.format"), e);
		}
		return X.EMPTY;
	}

	/**
	 * Size.
	 * 
	 * @param length
	 *            the length
	 * @return the string
	 */
	public String size(long length) {
		String unit = X.EMPTY;
		double d = Math.abs(length);
		if (d < 1024) {
		} else if (d < 1024 * 1024) {
			unit = "K";
			d /= 1024f;
		} else if (d < 1024 * 1024 * 1024) {
			unit = "M";
			d /= 1024f * 1024;
		} else {
			unit = "G";
			d /= 1024f * 1024 * 1024;
		}

		if (length > 0) {
			return ((long) (d * 10)) / 10f + unit;
		} else {
			return -((long) (d * 10)) / 10f + unit;
		}
	}

	/**
	 * Past.
	 * 
	 * @param base
	 *            the base
	 * @return the string
	 */
	public String past(long base) {
		int t = (int) ((System.currentTimeMillis() - base) / 1000);
		if (t < 60) {
			return t + get("past.s");
		}

		t /= 60;
		if (t < 60) {
			return t + get("past.m");
		}

		t /= 60;
		if (t < 24) {
			return t + get("past.h");
		}

		t /= 24;
		return t + get("past.d");
	}

	/**
	 * Parses the.
	 * 
	 * @param body
	 *            the body
	 * @return the html
	 */
	public Html parse(String body) {
		return new Html(body);
	}
}
