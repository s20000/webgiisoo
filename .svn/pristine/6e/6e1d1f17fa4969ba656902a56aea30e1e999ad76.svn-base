package com.giisoo.utils.http;

import java.util.*;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpHost;

import com.giisoo.core.bean.X;

// TODO: Auto-generated Javadoc
/**
 * The Class HttpProxy.
 */
public class HttpProxy {

	/** The _conf. */
	static Configuration _conf;

	/** The list. */
	static List<HttpHost> list = new ArrayList<HttpHost>();

	/**
	 * Inits the.
	 *
	 * @param conf the conf
	 */
	public static void init(Configuration conf) {
		_conf = conf;
	}

	/**
	 * Gets the.
	 *
	 * @return the http host
	 */
	public static HttpHost get() {
		if (_conf == null)
			return null;

		HttpHost h = null;
		synchronized (list) {
			if (list.isEmpty()) {
				_get();
			}

			int len = list.size();
			if (len > 0) {
				h = list.remove(len - 1);
			}
		}

		return h;
	}

	/**
	 * _get.
	 */
	private static void _get() {
		String s = _conf.getString("http.proxy", X.EMPTY);

		if (!X.EMPTY.equals(s)) {
			String[] ss = s.split("\\|");
			for (String s1 : ss) {
				String[] pp = s1.split(":");
				if (pp.length > 1) {
					list.add(new HttpHost(pp[0], Integer.parseInt(pp[1])));
				}
			}
		}
	}
}
