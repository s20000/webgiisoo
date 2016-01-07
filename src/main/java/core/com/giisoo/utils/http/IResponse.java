package com.giisoo.utils.http;

import org.apache.http.StatusLine;

import com.giisoo.utils.url.Url;

// TODO: Auto-generated Javadoc
/**
 * The Class IResponse.
 */
public abstract class IResponse {

	/**
	 * On error.
	 *
	 * @param url the url
	 * @param e the e
	 * @param status the status
	 */
	public abstract void onError(Url url, Exception e, StatusLine status);

	/**
	 * On complete.
	 *
	 * @param url the url
	 * @param page the page
	 */
	public abstract void onComplete(Url url, IPage page);

	/**
	 * On exception.
	 *
	 * @param url the url
	 * @param e the e
	 */
	public void onException(Url url, Exception e) {
		onError(url, e, null);
	};

	/**
	 * On cancel.
	 *
	 * @param url the url
	 */
	public void onCancel(Url url) {
		onComplete(url, null);
	};

}