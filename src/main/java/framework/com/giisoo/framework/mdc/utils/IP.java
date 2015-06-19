/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc.utils;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.utils.url.Url;
import com.giisoo.utils.http.GHttpClient;
import com.giisoo.utils.http.IPage;

public class IP {

	static Log log = LogFactory.getLog(IP.class);

	/**
	 * Inits the.
	 * 
	 * @param conf
	 *            the conf
	 */
	public static void init(Configuration conf) {
		try {
			GHttpClient.start();
		} catch (IOException e) {
		}
	}

	/**
	 * Gets the place.
	 * 
	 * @param ip
	 *            the ip
	 * @return the place
	 */
	public static String getPlace(String ip) {

		if (ip == null || "".equals(ip))
			return null;

		try {
			/**
			 * http://ip138.com/ips138.asp?action=2&ip=%1$s
			 */
			String url = SystemConfig.s("ip.query", null);
			if (!X.isEmpty(url)) {
				url = String.format(url, ip);
				Url u = new Url(url);
				IPage p = GHttpClient.get(u);
				if (p != null) {
					if (p.seek("class=\"ul1\"")) {
						String a = p.getWordIn("本站主数据：", "<");
						if (a != null) {
							return a;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(ip, e);
		}

		return null;
	}
}
