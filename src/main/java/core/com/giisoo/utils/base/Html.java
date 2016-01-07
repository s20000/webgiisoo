package com.giisoo.utils.base;

import java.util.*;

import javax.swing.text.html.parser.ParserDelegator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

// TODO: Auto-generated Javadoc
/**
 * The Class Html.
 */
public class Html {

	/** The delegator. */
	ParserDelegator delegator = new ParserDelegator();

	/** The log. */
	static Log log = LogFactory.getLog(Html.class);

	/** The d. */
	Document d = null;

	/**
	 * Instantiates a new html.
	 * 
	 * @param html
	 *            the html
	 */
	public Html(String html) {
		if (html != null) {
			d = Jsoup.parse(html);
		}
	}

	/**
	 * Removes the tag.
	 * 
	 * @param html
	 *            the html
	 * @param tag
	 *            the tag
	 * @return the string
	 */
	public static String removeTag(String html, String tag) {
		if (html == null)
			return null;

		StringBuilder sb = new StringBuilder();
		int p = 0;
		int len = html.length();
		while (p < len) {
			int i = html.indexOf("<" + tag, p);
			if (i > -1) {
				sb.append(html.substring(p, i));
				i = html.indexOf("</" + tag + ">", i);
				if (i > -1) {
					p = i + tag.length() + 3;
				} else {
					break;
				}
			} else {
				sb.append(html.substring(p));
				break;
			}
		}

		return sb.toString();
	}

	/**
	 * Title.
	 * 
	 * @return the string
	 */
	public String title() {
		if (d != null) {
			return d.title();
		}

		return null;
	}

	transient String body;

	/**
	 * Body.
	 * 
	 * @return the string
	 */
	public String body() {
		if (body == null && d != null) {
			d.select("iframe").remove();
			body = d.html();
		}

		return body;
	}

	public String body(int len) {
		body();

		if (body != null && body.length() > len) {
			body = body.substring(0, len - 3) + "...";
		}

		return body;
	}

	transient String text;

	/**
	 * Text.
	 * 
	 * @return the string
	 */
	public String text() {
		if (text == null && d != null) {
			String s = d.text();
			text = s.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		}

		return text;
	}

	public String text(int len) {
		text();

		if (text != null && text.getBytes().length > len) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < text.length()
					&& sb.toString().getBytes().length < len - 5; i++) {
				sb.append(text.charAt(i));
			}
			text = sb.append("...").toString();
		}

		return text;
	}

	transient List<String> images;

	/**
	 * Images.
	 * 
	 * @return the list
	 */
	public List<String> images() {
		if (images == null && d != null) {
			Elements es = d.getElementsByTag("img");
			images = new ArrayList<String>();
			for (int i = 0; i < es.size(); i++) {
				Element e = es.get(i);
				String src = e.attr("src");
				if (src != null) {
					images.add(src);
				}
			}
		}

		return images;
	}

	transient String firstimage;

	public String getFirstimage() {
		if (firstimage == null) {
			images();

			if (images != null && images.size() > 0) {
				firstimage = images.get(0);
			}
		}
		return firstimage;
	}

	/**
	 * Removes the.
	 * 
	 * @param q
	 *            the q
	 * @return the html
	 */
	public Html remove(String q) {
		if (d != null) {
			d.select(q).remove();
		}
		return this;
	}

}
