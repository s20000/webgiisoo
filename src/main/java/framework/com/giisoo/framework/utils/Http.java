package com.giisoo.framework.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * http utils
 * 
 * @author joe
 * 
 */
public class Http {

	static Log log = LogFactory.getLog(Http.class);
	static int TIMEOUT = 10 * 1000;

	/**
	 * GET response from a url
	 * 
	 * @param url
	 * @return Response
	 */
	public static Response get(String url) {
		return get(url, null);
	}

	/**
	 * POST response from a url
	 * 
	 * @param url
	 * @param body
	 * @return Response
	 */
	public static Response post(String url, String[][] params) {
		return post(url, "application/x-javascript; charset=UTF8", null, params);
	}

	/**
	 * GET method
	 * 
	 * @param url
	 * @param headers
	 * @return Response
	 */
	public static Response get(String url, String[][] headers) {

		Response r = new Response();
		DefaultHttpClient client = getClient(url);

		if (client != null) {
			HttpGet get = new HttpGet(url);

			try {

				if (headers != null && headers.length > 0) {
					for (String[] s : headers) {
						if (s != null && s.length > 1) {
							get.addHeader(s[0], s[1]);
						}
					}
				}

				HttpResponse resp = client.execute(get);
				r.status = resp.getStatusLine().getStatusCode();
				r.body = getContext(resp);

			} catch (Exception e) {
				log.error(url, e);
			} finally {
				get.abort();
			}
		}

		return r;
	}

	/**
	 * POST method
	 * 
	 * @param url
	 * @param contenttype
	 * @param headers
	 * @param body
	 * @return Response
	 */

	public static Response post(String url, String contenttype,
			String[][] headers, String[][] params) {
		return post(url, contenttype, headers, params, null);
	}

	/**
	 * POST
	 * 
	 * @param url
	 * @param contenttype
	 * @param headers
	 * @param body
	 * @param attachments
	 * @return Response
	 */
	public static Response post(String url, String contenttype,
			String[][] headers, String[][] body, Object[][] attachments) {

		Response r = new Response();

		DefaultHttpClient client = getClient(url);

		if (client != null) {
			HttpPost post = new HttpPost();
			try {

				if (headers != null && headers.length > 0) {
					for (String[] s : headers) {
						if (s != null && s.length > 1) {
							post.addHeader(s[0], s[1]);
						}
					}
				}
				if (attachments == null || attachments.length == 0) {
					if (body != null && body.length > 0) {
						List<NameValuePair> paramList = new ArrayList<NameValuePair>();

						for (String[] s : body) {
							if (s != null && s.length > 1) {
								BasicNameValuePair param = new BasicNameValuePair(
										s[0], s[1]);
								paramList.add(param);
							}
						}
						post.setEntity(new UrlEncodedFormEntity(paramList,
								HTTP.UTF_8));
					}
				} else {
					MultipartEntity entity = new MultipartEntity();
					for (Object[] f : attachments) {
						if (f != null && f.length > 1 && f[1] instanceof File) {
							FileBody fileBody = new FileBody((File) f[1]);
							entity.addPart((String) f[0], fileBody);
						}
					}

					if (body != null && body.length > 0) {
						for (String[] s : body) {
							if (s != null && s.length > 1) {
								StringBody stringBody = new StringBody(s[1]);
								entity.addPart(s[0], stringBody);
							}
						}
					}
					post.setEntity(entity);
				}

				HttpResponse resp = client.execute(post);
				r.status = resp.getStatusLine().getStatusCode();
				r.body = getContext(resp);

			} catch (Exception e) {
				log.error(url, e);
			} finally {
				post.abort();
			}

		}

		return r;
	}

	private static DefaultHttpClient getClient(String url) {

		DefaultHttpClient client = new DefaultHttpClient();

		if (url.toLowerCase().startsWith("https://")) {
			try {
				SSLContext ctx = SSLContext.getInstance("TLS");
				X509TrustManager tm = new X509TrustManager() {

					public void checkClientTrusted(X509Certificate[] arg0,
							String arg1) throws CertificateException {
						// TODO Auto-generated method stub

					}

					public void checkServerTrusted(X509Certificate[] arg0,
							String arg1) throws CertificateException {
						// TODO Auto-generated method stub

					}

					public X509Certificate[] getAcceptedIssuers() {
						// TODO Auto-generated method stub
						return null;
					}
				};
				ctx.init(null, new TrustManager[] { tm }, null);
				SSLSocketFactory ssf = new SSLSocketFactory(ctx);

				ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				ClientConnectionManager ccm = client.getConnectionManager();
				SchemeRegistry sr = ccm.getSchemeRegistry();
				sr.register(new Scheme("https", ssf, 443));
				HttpParams params = client.getParams();
				HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
				HttpConnectionParams.setSoTimeout(params, TIMEOUT);

				client = new DefaultHttpClient(ccm, params);

			} catch (Exception e) {
				log.error(url, e);
			}
		}

		return client;
	}

	private static String getContext(HttpResponse response) {
		String context = null;

		if (response.getEntity() != null) {
			try {
				HttpEntity entity = response.getEntity();
				String ccs = EntityUtils.getContentCharSet(entity);

				/**
				 * fix the bug of http.util of apache
				 */
				String encoding = null;
				if (entity.getContentEncoding() != null) {
					encoding = entity.getContentEncoding().getValue();
				}

				if (ccs == null || ccs.indexOf("gb2312") > -1) {
					ccs = "GBK";
				}

				if (encoding != null && encoding.indexOf("gzip") > -1) {

					BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(
							entity);

					entity = bufferedEntity;

					StringBuilder sb = new StringBuilder();

					try {
						GZIPInputStream in = new GZIPInputStream(
								bufferedEntity.getContent());

						Reader reader = new InputStreamReader(in, ccs);

						// String s = reader.readLine();
						char[] buf = new char[2048];
						int len = reader.read(buf);
						while (len > 0) {
							sb.append(buf, 0, len);
							// sb.append(s).append("\r\n");
							len = reader.read(buf);
							// s = reader.readLine();
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}

					if (sb.length() > 0) {
						context = sb.toString();
					}
				}

				if (context == null || context.length() == 0) {
					context = _getContext(entity, ccs);
				}

				// log.debug(context);

			} catch (Exception e) {
				log.error(e.getMessage());// , e);
			}
		}
		return context;
	}

	private static String _getContext(HttpEntity entity, String charset) {
		StringBuilder sb = new StringBuilder();

		InputStreamReader reader = null;

		try {
			if (charset == null) {
				reader = new InputStreamReader(entity.getContent());
			} else {
				reader = new InputStreamReader(entity.getContent(), charset);
			}

			char[] buf = new char[1024];
			int len = reader.read(buf);
			while (len > 0) {
				sb.append(buf, 0, len);
				len = reader.read(buf);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		return sb.toString();

	}

	public static class Response {
		public int status;
		public String body;

	}
}
