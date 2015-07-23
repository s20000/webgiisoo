package com.giisoo.core.bean;

import java.util.TimeZone;

// TODO: Auto-generated Javadoc
/**
 * The Class X.
 */
public class X {

	/** The Constant AMINUTE. */
	public static final long AMINUTE = 1000 * 60;

	/** The Constant AHOUR. */
	public static final long AHOUR = AMINUTE * 60;

	/** The Constant ADAY. */
	public static final long ADAY = 24 * AHOUR;

	/** The Constant AWEEK. */
	final static public long AWEEK = 7 * ADAY;

	/** The Constant AMONTH. */
	final static public long AMONTH = 30 * ADAY;

	/** The Constant AYEAR. */
	final static public long AYEAR = 365 * ADAY;

	/** The Constant TITLE. */
	public static final String TITLE = "title";

	/** The Constant TYPE. */
	public static final String TYPE = "type";

	/** The Constant ID. */
	public static final String ID = "id";

	/** The Constant _TYPE. */
	public static final String _TYPE = "_type";

	/** The Constant _LIKE. */
	public static final String _LIKE = "_like";

	/** The Constant _ID. */
	public static final String _ID = "_id";

	/** The Constant KEYWORD. */
	public static final String KEYWORD = "keyword";

	/** The Constant STATE. */
	public static final String STATE = "state";

	/** The Constant NAME. */
	public static final String NAME = "name";

	/** The Constant BRAND. */
	public static final String BRAND = "brand";

	/** The Constant AUDIT. */
	public static final String AUDIT = "audit";

	/** The Constant EMPTY. */
	public static final String EMPTY = "";

	/** The Constant ALL. */
	public static final String ALL = "all";

	/** The Constant TAGS. */
	public static final String TAGS = "tags";

	/** The Constant DATE. */
	public static final String DATE = "_date";

	/** The Constant GROUP. */
	public static final String GROUP = "_group";

	/** The Constant query. */
	public static final String query = "query";

	/** The Constant wd. */
	public static final String wd = "wd";

	/** The Constant key. */
	public static final String key = "key";

	/** The Constant output. */
	public static final String output = "output";

	/** The Constant RESULTS. */
	public static final String RESULTS = "results";

	/** The Constant PN. */
	public static final String PN = "pn";

	/** The Constant STATUS. */
	public static final String STATUS = "status";

	/** The Constant OK. */
	public static final int OK = 200;

	/** The Constant UTF8. */
	public static final String UTF8 = "UTF-8";

	/** The Constant RATE_MILE_TO_KM. */
	public static final double RATE_MILE_TO_KM = 1.609344; // 英里和公里的比率

	public static final String CAPABILITY = "capability";
	public static final String UID = "uid";
	public static final String CLIENTID = "clientid";
	public static final String KEY = "key";
	public static final String NONE = "none";

	public static final int OK_200 = 200;
	public static final int FAIL = 201;
	public static final int FAIL201 = 201;
	public static final int FAIL301 = 301;
	public static final int FAIL401 = 401;

	public static final String URI = "uri";
	public static final String CODE = "code";

	public static final String MESSAGE = "message";

	public static final String PARAM = "param";

	public static final String CONTENTTYPE = "contenttype";

	public static final String ERROR = "error";
	public static final String DATA = "data";

	public static final String FILE = "file";
	public static final String LENGTH = "length";
	public static final String TOTAL = "total";
	public static final String POSITION = "position";
	public static final String DONE = "done";

	public static final String LIST = "list";
	public static final String S = "s";
	public static final String E = "e";
	public static final String VERSION = "version";
	public static final String SEQ = "seq";
	public static final String RESULT = "result";

	public static boolean isEmpty(Object s) {
		return s == null || X.EMPTY.equals(s);
	}

	public static boolean isEmpty(String s) {
		return s == null || X.EMPTY.equals(s);
	}

	/**
	 * Mile2 meter.
	 * 
	 * @param miles
	 *            the miles
	 * @return the int
	 */
	public static int mile2Meter(double miles) {
		double dMeter = miles * RATE_MILE_TO_KM * 1000;
		return (int) dMeter;
	}

	/**
	 * Meter2 mile.
	 * 
	 * @param meter
	 *            the meter
	 * @return the double
	 */
	public static double meter2Mile(double meter) {
		return meter / RATE_MILE_TO_KM / 1000;
	}

	/** The Constant UNIT. */
	public static final String[] UNIT = { "", "0", "00", "000", "万", "0万",
			"00万", "000万", "亿", "0亿" };

	/** The Constant tz. */
	public static final TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");

}
