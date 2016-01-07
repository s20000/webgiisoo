package com.giisoo.utils.base;

// TODO: Auto-generated Javadoc
/**
 * The Class Trim.
 */
public class Trim {
	
	/** The Constant sPrefix. */
	static final String[] sPrefix = { "商品介绍", "内容介绍", "产品名称", "产品描述", "商品名称", "商品描述", "商品详情", "商品说明", "null", "首页", "您的位置", "现在的位置", "您" };
	
	/** The Constant cPrefix. */
	static final char[] cPrefix = { 160, ' ', '　', ',', '.', '，', '。', '、', ':', '：', '【', '】', '[', ']', '>' };

	/**
	 * Trim.
	 *
	 * @param d the d
	 * @return the string
	 */
	public static String trim(String d) {
		if (d == null)
			return null;

		return d.replaceAll("(^null|[ 　 \t\r\n])", "");

		// boolean updated = true;
		// while (updated) {
		// updated = false;
		//
		// if (d == null || d.length() == 0)
		// return d;
		//
		// for (String s : sPrefix) {
		// if (d.startsWith(s)) {
		// d = d.substring(s.length());
		// updated = true;
		// }
		// }
		//
		// for (char c : cPrefix) {
		// if (d.length() == 0)
		// return d;
		//
		// if (d.charAt(0) == c) {
		// d = d.substring(1);
		// updated = true;
		// }
		// }
		// }
		//
		// return d;
	}

}
