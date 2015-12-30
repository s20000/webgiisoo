package com.giisoo.utils.base;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GTokenizer.
 */
public class GTokenizer {

	/**
	 * Parses the.
	 *
	 * @param s the s
	 * @param ignore the ignore
	 * @param delim the delim
	 * @return the list
	 */
	public static List<String> parse(String s, String ignore, char delim) {
		List<String> list = new ArrayList<String>();

		if (s == null || s.length() == 0)
			return list;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c == delim) {
				if (sb.length() > 0) {
					list.add(sb.toString());
					sb = new StringBuilder();
				}
			} else if (ignore != null && ignore.indexOf(c) > -1) {
				/**
				 * skip to next [delim]
				 */
				sb = new StringBuilder();
				while (++i < s.length()) {
					c = s.charAt(i);
					if (c == delim)
						break;
				}
			} else if ((c != ' ') && (c != 160)) {
//				System.out.println((int)c);
				sb.append(c);
			}
		}

		if (sb.length() > 0) {
			list.add(sb.toString());
		}

		return list;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		String s = "首页 > 大家电、生活电器 > 影音设备 > 家庭影院 > Sansui 山水 SA2601IMM";
		
		System.out.println(parse(s, "、", '>'));
		
	}
}
