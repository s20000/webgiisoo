package com.giisoo.utils.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.*;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.ContentEncodingHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.impl.nio.conn.PoolingClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.concurrent.FutureCallback;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.params.*;

import com.giisoo.core.bean.TimeStamp;
import com.giisoo.core.conf.Config;
import com.giisoo.core.worker.WorkerManager;
import com.giisoo.utils.url.Url;

// TODO: Auto-generated Javadoc
/**
 * The Class GHttpClient.
 */
public class GHttpClient {

	/** The log. */
	static Log log = LogFactory.getLog(GHttpClient.class);

	/** The user agent. */
	static String USER_AGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0) ";

	/** The asynclient. */
	static DefaultHttpAsyncClient asynclient;

	/** The synclient. */
	static HttpClient synclient;

	/** The params. */
	static HttpParams params;

	/** The executor. */
	static ScheduledThreadPoolExecutor executor;

	/** The pcm. */
	static PoolingClientConnectionManager pcm;

	/** The requesting. */
	static AtomicInteger requesting = new AtomicInteger(0);

	/** The uptime. */
	static TimeStamp uptime;

	/** The inprocessing. */
	static Set<String> inprocessing = new TreeSet<String>();

	/**
	 * Uptime.
	 * 
	 * @return the long
	 */
	public static long uptime() {
		return uptime.past();
	}

	/**
	 * Requesting.
	 * 
	 * @return the int
	 */
	public static int requesting() {
		return requesting.get();
	}

	/**
	 * Inits the.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static void init() throws IOException {

		uptime = new TimeStamp();

		Configuration conf = Config.getConfig();

		HttpProxy.init(conf);

		executor = new ScheduledThreadPoolExecutor(conf.getInt(
				"http.thread.count", 50));

		USER_AGENT = conf.getString("user.agent", USER_AGENT);

		params = new SyncBasicHttpParams();
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 20000)
				.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000)
				.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
						8 * 1024)
				.setBooleanParameter(
						CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
				.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
				.setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT)
				.setParameter(CoreConnectionPNames.SO_REUSEADDR, true)
				.setParameter(CoreConnectionPNames.SO_LINGER, 0);

		int worker = conf.getInt("ioreactor.worker.count", 2);

		DefaultConnectingIOReactor ioreactor = new DefaultConnectingIOReactor(
				worker, params);
		ioreactor.setExceptionHandler(new IOReactorExceptionHandler() {

			@Override
			public boolean handle(IOException ex) {
				log.error("IOReactor throw IOExcetpion", ex);
				return true;
			}

			@Override
			public boolean handle(RuntimeException ex) {
				log.error("IOReactor throw RunTimeExcetpion", ex);
				return true;
			}

		});
		pcm = new PoolingClientConnectionManager(ioreactor);
		pcm.setDefaultMaxPerHost(conf.getInt("max.connections.per.host", 100));
		pcm.setTotalMax(conf.getInt("max.total.connections", 1000));
		pcm.closeIdleConnections(10, TimeUnit.SECONDS);

		asynclient = new DefaultHttpAsyncClient(pcm, params);

		ThreadSafeClientConnManager tscm = new ThreadSafeClientConnManager();
		synclient = new ContentEncodingHttpClient(tscm, params);
	}

	/**
	 * Start.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void start() throws IOException {

		init();

		asynclient.start();
	}

	/**
	 * Format url.
	 * 
	 * @param uri
	 *            the uri
	 * @param charset
	 *            the charset
	 * @return the string
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	private static String formatUrl(String uri, String charset)
			throws UnsupportedEncodingException {

		int i = uri.indexOf("#");
		if (i > -1) {
			uri = uri.substring(0, i);
		}

		uri = charset != null ? URLEncoder.encode(uri, charset) : URLEncoder
				.encode(uri, "utf-8");

		uri = uri.replaceAll("%2F", "/");
		uri = uri.replaceAll("%3F", "?");
		uri = uri.replaceAll("%3D", "=");
		uri = uri.replaceAll("%26", "&");
		uri = uri.replaceAll("%3A", ":");
		uri = uri.replaceAll("%25", "%");

		return uri;

	}

	/**
	 * _set header.
	 * 
	 * @param request
	 *            the request
	 * @param url
	 *            the url
	 */
	static void _setHeader(HttpRequestBase request, Url url) {
		String referer = url.getReferer();
		String cookie = url.getCookie();

		if (referer != null && referer.length() > 0) {
			request.addHeader("Referer", referer);
		} else {
			request.addHeader("Referer", "http://" + url.getHost());
		}

		if (cookie != null && cookie.length() > 0) {
			request.addHeader("Cookie", cookie);
		}

		request.addHeader("Cache-Control", "max-age=0");
		request.addHeader(
				"Accept",
				"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		request.addHeader("Accept-Encoding", "gzip");
		request.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
		request.addHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");

		Map<String, String> headers = url.getHeaders();
		if (headers.size() > 0) {
			for (String name : headers.keySet()) {
				String value = headers.get(name);
				request.addHeader(name, value);
			}
		}
	}

	/** The rset. */
	static Set<String> rset = new TreeSet<String>();

	/**
	 * Sget.
	 * 
	 * @param url
	 *            the url
	 * @return the i page
	 * @throws Exception
	 *             the exception
	 */
	public static IPage sget(Url url) throws Exception {
		String r = url.getRequest();
		try {
			synchronized (rset) {
				while (rset.contains(r)) {
					rset.wait();
				}

				rset.add(r);
			}
			return get(url);
		} finally {
			rset.remove(r);
			synchronized (rset) {
				rset.notifyAll();
			}
		}
	}

	/**
	 * Gets the.
	 * 
	 * @param url
	 *            the url
	 * @param proxy
	 *            the proxy
	 * @return the i page
	 * @throws Exception
	 *             the exception
	 */
	public static IPage get(Url url, HttpHost proxy) throws Exception {
		if (proxy == null) {
			synclient.getParams()
					.removeParameter(ConnRoutePNames.DEFAULT_PROXY);
			asynclient.getParams().removeParameter(
					ConnRoutePNames.DEFAULT_PROXY);
		} else {
			synclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);
			asynclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);
		}

		return get(url);
	}

	/**
	 * Gets the.
	 * 
	 * @param url
	 *            the url
	 * @return the i page
	 * @throws Exception
	 *             the exception
	 */
	public static IPage get(Url url) throws Exception {

		// TimeStat ts = new TimeStat(url.getUrl());
		// ts.StartCounter(TimeStat.HttpRequest);
		String c = url.getCharset();

		pcm.closeExpiredConnections();
		TimeStamp t = new TimeStamp();
		HttpGet get = null;

		try {
			get = new HttpGet(formatUrl(url.getUrl(), url.getCharset()));

			_setHeader(get, url);

			// log.info(get.getURI());

			requesting.incrementAndGet();

			HttpResponse resp = null;

			boolean parallel = url.supportParallel();
			if (!parallel) {
				synchronized (inprocessing) {
					while (inprocessing.contains(url.getHost())) {
						inprocessing.wait();
					}
					inprocessing.add(url.getHost());
				}
			}
			try {
				resp = synclient.execute(get);
			} catch (Exception e) {
				if (e.getMessage() != null
						&& e.getMessage().indexOf("shut down") > 0) {
					WorkerManager.stop(true);
				}
				throw e;
			} finally {
				if (!parallel) {
					synchronized (inprocessing) {
						inprocessing.remove(url.getHost());
						inprocessing.notifyAll();
					}
				}
			}

			requesting.decrementAndGet();

			if (resp != null) {
				int statusCode = resp.getStatusLine().getStatusCode();
				if (statusCode == 301 || statusCode == 302) {
					Header location = resp.getLastHeader("Location");
					if (location != null) {
						String uri = location.getValue();

						Url u;

						if (uri.startsWith("/")) {
							u = new Url("http://" + url.getHost() + uri);
						} else if (uri.startsWith("http://")) {
							u = new Url(uri);
						} else {
							String r = url.getRequest();
							int i = r.lastIndexOf("/");
							if (i > -1) {
								r = r.substring(0, i);
							}

							u = new Url(r + "/" + uri);
						}

						u.setCharset(url.getCharset());
						u.setReferer(url.getReferer());
						u.setCookie(url.getCookie());

						log.debug(url.getUrl() + " ->redirecting-> "
								+ u.getUrl());
						return get(u);

					}
				}

				if (statusCode == 200) {
					url.setUrl(get.getURI().toString());
					return parse(url, resp);
				}
			}

		} catch (IllegalArgumentException e) {
			// ignore this
		} catch (IllegalStateException e) {
			log.error(e.getMessage(), e);

			WorkerManager.stop(true);
		} catch (Exception e) {
			throw new Exception(url.toString(), e);
		} finally {

			if (get != null) {
				get.abort();
			}
		}

		return null;
	}

	/**
	 * Post.
	 * 
	 * @param url
	 *            the url
	 * @return the i page
	 * @throws Exception
	 *             the exception
	 */
	public static IPage post(Url url) throws Exception {

		// TimeStat ts = new TimeStat(url.getUrl());
		// ts.StartCounter(TimeStat.HttpRequest);
		String c = url.getCharset();

		pcm.closeExpiredConnections();
		TimeStamp t = new TimeStamp();
		HttpPost get = null;

		try {
			get = new HttpPost(formatUrl(url.getUrl(), url.getCharset()));

			_setHeader(get, url);

			// log.info(get.getURI());

			requesting.incrementAndGet();

			HttpResponse resp = null;

			boolean parallel = url.supportParallel();
			if (!parallel) {
				synchronized (inprocessing) {
					while (inprocessing.contains(url.getHost())) {
						inprocessing.wait();
					}
					inprocessing.add(url.getHost());
				}
			}
			try {
				resp = synclient.execute(get);
			} catch (Exception e) {
				if (e.getMessage() != null
						&& e.getMessage().indexOf("shut down") > 0) {
					WorkerManager.stop(true);
				}
				throw e;
			} finally {
				if (!parallel) {
					synchronized (inprocessing) {
						inprocessing.remove(url.getHost());
						inprocessing.notifyAll();
					}
				}
			}

			requesting.decrementAndGet();

			if (resp != null) {
				int statusCode = resp.getStatusLine().getStatusCode();
				if (statusCode == 301 || statusCode == 302) {
					Header location = resp.getLastHeader("Location");
					if (location != null) {
						String uri = location.getValue();

						Url u;

						if (uri.startsWith("/")) {
							u = new Url("http://" + url.getHost() + uri);
						} else if (uri.startsWith("http://")) {
							u = new Url(uri);
						} else {
							String r = url.getRequest();
							int i = r.lastIndexOf("/");
							if (i > -1) {
								r = r.substring(0, i);
							}

							u = new Url(r + "/" + uri);
						}

						u.setCharset(url.getCharset());
						u.setReferer(url.getReferer());
						u.setCookie(url.getCookie());

						log.debug(url.getUrl() + " ->redirecting-> "
								+ u.getUrl());
						return get(u);

					}
				}

				if (statusCode == 200) {
					url.setUrl(get.getURI().toString());
					return parse(url, resp);
				}
			}

		} catch (IllegalArgumentException e) {
			// ignore this
		} catch (IllegalStateException e) {
			log.error(e.getMessage(), e);

			WorkerManager.stop(true);
		} catch (Exception e) {
			throw new Exception(url.toString(), e);
		} finally {

			if (get != null) {
				get.abort();
			}
		}

		return null;
	}

	/**
	 * Parses the.
	 * 
	 * @param url
	 *            the url
	 * @param resp
	 *            the resp
	 * @return the i page
	 */
	private static IPage parse(Url url, HttpResponse resp) {
		IPage page = null;
		String type = url.getType();

		try {

			if (type == null || "html".equalsIgnoreCase(type)) {
				page = new HtmlPage(url, resp);
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
		return page;
	}

	/**
	 * Gets the.
	 * 
	 * @param url
	 *            the url
	 * @param respcb
	 *            the respcb
	 */
	public static void get(Url url, IResponse respcb) {

		try {

			// final TimeStat ts = new TimeStat(url.getUrl());
			// ts.StartCounter(TimeStat.HttpRequest);
			pcm.closeExpiredConnections();

			HttpGet get = new HttpGet(formatUrl(url.getUrl(), url.getCharset()));
			_setHeader(get, url);

			// log.info(get.getURI());

			requesting.incrementAndGet();
			if (url.isGzip()) {
				executor.execute(new GetTask(url, respcb, get));
			} else {
				boolean parallel = url.supportParallel();
				if (!parallel) {
					synchronized (inprocessing) {
						while (inprocessing.contains(url.getHost())) {
							inprocessing.wait();
						}
						inprocessing.add(url.getHost());
					}
				}
				try {
					asynclient.execute(get, new Callback(url, respcb, get));
				} catch (Throwable e) {
					if (e.getMessage() != null
							&& e.getMessage().indexOf("shut down") > 0) {
						WorkerManager.stop(true);
					}

					if (!parallel) {
						synchronized (inprocessing) {
							inprocessing.remove(url.getHost());
							inprocessing.notifyAll();
						}
					}
				}
			}
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);

			respcb.onException(url, e);
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);

			respcb.onException(url, e);
		} catch (IllegalStateException e) {
			log.error(e.getMessage(), e);

			WorkerManager.stop(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);

			respcb.onException(url, e);

		}

	}

	/**
	 * Post.
	 * 
	 * @param url
	 *            the url
	 * @param encodingType
	 *            the encoding type
	 * @param body
	 *            the body
	 * @return the i page
	 * @throws Exception
	 *             the exception
	 */
	public static IPage post(Url url, String encodingType, String body)
			throws Exception {
		// TimeStat ts = new TimeStat(url.getUrl());
		// ts.StartCounter(TimeStat.HttpRequest);
		pcm.closeExpiredConnections();

		TimeStamp t = new TimeStamp();

		HttpPost post = new HttpPost(formatUrl(url.getUrl(), url.getCharset()));

		try {
			_setHeader(post, url);

			BasicHttpEntity entity = new BasicHttpEntity();

			entity.setContentType(encodingType);
			entity.setContent(new ByteArrayInputStream(body.getBytes()));
			entity.setContentLength(body.length());
			post.setEntity(entity);

			requesting.incrementAndGet();
			HttpResponse resp = null;

			boolean parallel = url.supportParallel();
			if (!parallel) {
				synchronized (inprocessing) {
					while (inprocessing.contains(url.getHost())) {
						inprocessing.wait();
					}
					inprocessing.add(url.getHost());
				}
			}
			try {
				resp = synclient.execute(post);
			} catch (Exception e) {
				if (e.getMessage() != null
						&& e.getMessage().indexOf("shut down") > 0) {
					WorkerManager.stop(true);
				}

				throw e;
			} finally {
				if (!parallel) {
					synchronized (inprocessing) {
						inprocessing.remove(url.getHost());
						inprocessing.notifyAll();
					}
				}
			}

			requesting.decrementAndGet();

			int statusCode = resp.getStatusLine().getStatusCode();
			if (statusCode == 301 || statusCode == 302) {
				Header location = resp.getLastHeader("Location");
				if (location != null) {
					String uri = location.getValue();

					Url u;

					if (uri.startsWith("/")) {
						u = new Url("http://" + url.getHost() + uri);
					} else if (uri.startsWith("http://")) {
						u = new Url(uri);
					} else {
						String r = url.getRequest();
						int i = r.lastIndexOf("/");
						if (i > -1) {
							r = r.substring(0, i);
						}

						u = new Url(r + "/" + uri);
					}

					u.setCharset(url.getCharset());
					u.setReferer(url.getReferer());
					u.setCookie(url.getCookie());

					log.debug(url.getUrl() + " ->redirecting-> " + u.getUrl());
					return post(u, encodingType, body);

				}
			}

			if (statusCode == 200) {
				url.setUrl(post.getURI().toString());
				return parse(url, resp);
			}
		} catch (IllegalStateException e) {
			log.error(e.getMessage(), e);

			WorkerManager.stop(true);
		} finally {
			post.abort();
		}
		return null;
	}

	/**
	 * The Class GetTask.
	 */
	static class GetTask implements Runnable {

		/** The url. */
		Url url;

		/** The respcb. */
		IResponse respcb;

		/** The get. */
		HttpGet get;

		/**
		 * Instantiates a new gets the task.
		 * 
		 * @param u
		 *            the u
		 * @param resp
		 *            the resp
		 * @param get
		 *            the get
		 */
		GetTask(Url u, IResponse resp, HttpGet get) {
			this.url = u;
			this.respcb = resp;
			this.get = get;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				HttpResponse resp = null;
				boolean parallel = url.supportParallel();
				if (!parallel) {
					synchronized (inprocessing) {
						while (inprocessing.contains(url.getHost())) {
							inprocessing.wait();
						}
						inprocessing.add(url.getHost());
					}
				}
				try {
					resp = synclient.execute(get);
				} finally {
					if (!parallel) {
						synchronized (inprocessing) {
							inprocessing.remove(url.getHost());
							inprocessing.notifyAll();
						}
					}
				}

				// HttpResponse resp = synclient.execute(get);
				if (resp != null) {
					int statusCode = resp.getStatusLine().getStatusCode();
					if (statusCode == 301 || statusCode == 302) {

						Header location = resp.getLastHeader("Location");
						if (location != null && !location.equals(url.getUrl())) {
							String uri = location.getValue();

							if (uri.startsWith("/")) {
								url.setUrl("http://" + url.getHost() + uri);
							} else if (uri.startsWith("http://")) {
								url.setUrl(uri);
							} else {
								String r = url.getRequest();
								int i = r.lastIndexOf("/");
								if (i > -1) {
									r = r.substring(0, i);
								}

								url.setUrl(r + "/" + uri);
							}

							log.debug("redirecting: " + url.getUrl());
							get(url, respcb);

						}
					} else if (statusCode == 200) {
						// G.timeConsume(url.getDomain(), G.PAGE_GET, t.past());
						url.setUrl(get.getURI().toString());
						IPage page = parse(url, resp);
						respcb.onComplete(page.getUrl(), page);
					} else {
						respcb.onError(url, null, resp.getStatusLine());
					}
				} else {
					respcb.onError(url, null, null);
				}

			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				respcb.onException(url, new Exception(e));
			} finally {
				get.abort();
			}

			requesting.decrementAndGet();

		}
	}

	/**
	 * The Class Callback.
	 */
	static class Callback implements FutureCallback<HttpResponse> {

		/** The url. */
		Url url;

		/** The respcb. */
		IResponse respcb;

		/** The get. */
		HttpGet get;

		/** The parallel. */
		boolean parallel;

		/**
		 * Instantiates a new callback.
		 * 
		 * @param u
		 *            the u
		 * @param resp
		 *            the resp
		 * @param get
		 *            the get
		 */
		Callback(Url u, IResponse resp, HttpGet get) {
			this.url = u;
			this.respcb = resp;
			this.get = get;
			this.parallel = url.supportParallel();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.http.nio.concurrent.FutureCallback#cancelled()
		 */
		@Override
		public void cancelled() {
			if (!parallel) {
				synchronized (inprocessing) {
					inprocessing.remove(url.getHost());
					inprocessing.notifyAll();
				}
			}

			// ts.StopCounter(TimeStat.HttpRequest,
			// TimeStat.Status.Cancelled);
			// ts.finishCounter();
			requesting.decrementAndGet();
			respcb.onException(url, null);
			get.abort();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.http.nio.concurrent.FutureCallback#completed(java.lang
		 * .Object)
		 */
		@Override
		public void completed(final HttpResponse resp) {
			if (!parallel) {
				synchronized (inprocessing) {
					inprocessing.remove(url.getHost());
					inprocessing.notifyAll();
				}
			}

			// ts.StopCounter(TimeStat.HttpRequest,
			// TimeStat.Status.Success);
			// ts.finishCounter();
			requesting.decrementAndGet();

			final int statusCode = resp.getStatusLine().getStatusCode();
			if (statusCode == 301 || statusCode == 302) {

				Header location = resp.getLastHeader("Location");
				if (location != null && !location.equals(url.getUrl())) {
					String uri = location.getValue();

					if (uri.startsWith("/")) {
						url.setUrl("http://" + url.getHost() + uri);
					} else if (uri.startsWith("http://")) {
						url.setUrl(uri);
					} else {
						String r = url.getRequest();
						int i = r.lastIndexOf("/");
						if (i > -1) {
							r = r.substring(0, i);
						}

						url.setUrl(r + "/" + uri);
					}

					log.debug("redirecting: " + url.getUrl());
					get(url, respcb);
				}
			}

			executor.execute(new Runnable() {
				@Override
				public void run() {
					if (statusCode == 200) {
						// G.timeConsume(url.getDomain(), G.PAGE_GET, t.past());
						url.setUrl(get.getURI().toString());
						IPage page = parse(url, resp);
						respcb.onComplete(page.getUrl(), page);
					} else {
						respcb.onError(url, null, resp.getStatusLine());
					}
					get.abort();
				}

			});

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.http.nio.concurrent.FutureCallback#failed(java.lang.Exception
		 * )
		 */
		@Override
		public void failed(Exception e) {
			if (e.getMessage() != null
					&& e.getMessage().indexOf("shut down") > 0) {
				WorkerManager.stop(true);
			}

			if (!parallel) {
				synchronized (inprocessing) {
					inprocessing.remove(url.getHost());
					inprocessing.notifyAll();
				}
			}

			// ts.StopCounter(TimeStat.HttpRequest,
			// TimeStat.Status.Fail);
			// ts.finishCounter();
			requesting.decrementAndGet();

			respcb.onException(url, e);
			get.abort();
		}

	}
}
