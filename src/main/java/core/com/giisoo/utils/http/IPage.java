package com.giisoo.utils.http;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.StatusLine;

import com.giisoo.utils.url.Url;

// TODO: Auto-generated Javadoc
/**
 * The Interface IPage.
 */
public interface IPage {

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	Url getUrl();

	/**
	 * Seek.
	 *
	 * @param skip the skip
	 * @return the int
	 */
	int seek(int skip);

	/**
	 * Back to.
	 *
	 * @param s the s
	 * @return true, if successful
	 */
	boolean backTo(char s);

	/**
	 * Seek.
	 *
	 * @param s the s
	 * @return true, if successful
	 */
	boolean seek(String s);

	/**
	 * Seek.
	 *
	 * @param s the s
	 * @param inlen the inlen
	 * @return true, if successful
	 */
	boolean seek(String s, int inlen);

	/**
	 * Gets the attribute.
	 *
	 * @param name the name
	 * @return the attribute
	 */
	String getAttribute(String name);

	/**
	 * Mark.
	 */
	void mark();

	/**
	 * Reset.
	 */
	void reset();

	/**
	 * Clear mark.
	 */
	void clearMark();

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	String getText();

	/**
	 * Gets the word in.
	 *
	 * @param schar the schar
	 * @param echar the echar
	 * @return the word in
	 */
	String getWordIn(String schar, String echar);

	/**
	 * Gets the text.
	 *
	 * @param name the name
	 * @return the text
	 */
	String getText(String name);

	/**
	 * Sets the cached.
	 *
	 * @param b the new cached
	 */
	void setCached(boolean b);

	/**
	 * Gets the request.
	 *
	 * @return the request
	 */
	Url getRequest();

	/**
	 * Gets the status line.
	 *
	 * @return the status line
	 */
	StatusLine getStatusLine();

	/**
	 * Gets the headers.
	 *
	 * @return the headers
	 */
	Header[] getHeaders();

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	String getContext();

	/**
	 * Gets the urls.
	 *
	 * @return the urls
	 */
	List<Url> getUrls();

	/**
	 * Size.
	 *
	 * @return the int
	 */
	int size();

	/**
	 * Release.
	 */
	void release();

	/**
	 * Gets the.
	 *
	 * @param url the url
	 * @return the string
	 */
	String get(String url);

	/**
	 * Format.
	 *
	 * @param href the href
	 * @return the url
	 */
	Url format(String href);

}
