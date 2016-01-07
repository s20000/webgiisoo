/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc.command;

import java.io.*;
import java.security.Principal;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.fileupload.*;
import org.apache.commons.logging.*;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.X;
import com.giisoo.framework.mdc.*;
import com.giisoo.framework.web.Controller;

/**
 * 
 * @author yjiang
 * 
 */
public class MDCHttpRequest implements HttpServletRequest {

	static Log log = LogFactory.getLog(MDCHttpRequest.class);

	private Map<String, Object> d;

	private JSONObject in;
	private JSONObject param;
	private byte[] raw;

	/**
	 * Creates the.
	 * 
	 * @param in
	 *            the in
	 * @param raw
	 *            the raw
	 * @param d
	 *            the d
	 * @return the MDC http request
	 */
	public static MDCHttpRequest create(JSONObject in, byte[] raw,
			Map<String, Object> d) {
		return new MDCHttpRequest(in, raw, d);
	}

	// public TConn getConnection() {
	// return d;
	// }

	private MDCHttpRequest(JSONObject in, byte[] raw, Map<String, Object> d) {

		this.in = in;
		this.d = d;
		this.raw = raw;

		if (in.containsKey(X.PARAM)) {
			param = in.getJSONObject(X.PARAM);
			
			/**
			 * merge the params in conn
			 */
		}

		try {
			log.debug("request:" + in + ", binary: "
					+ getInputStream().available());
		} catch (Exception e) {

		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		return d.get(name);
	}

	public Enumeration<?> getAttributeNames() {
		return null;
	}

	public String getCharacterEncoding() {
		return "UTF-8";
	}

	public int getContentLength() {
		return 0;
	}

	public String getContentType() {
		return "multipart/mixed";
	}

	transient ServletInputStream instream = null;

	public ServletInputStream getInputStream() throws IOException {
		if (instream == null) {
			instream = new ServletInputStream() {
				int pos = 0;

				@Override
				public synchronized void mark(int readlimit) {
				}

				@Override
				public synchronized void reset() throws IOException {
					pos = 0;
				}

				@Override
				public int read() throws IOException {
					if (raw == null || pos >= raw.length)
						return 0;

					return raw[pos++];
				}

				@Override
				public int read(byte[] b) throws IOException {
					if (raw == null || pos >= raw.length)
						return 0;
					int len = Math.min(b.length, raw.length - pos);
					System.arraycopy(raw, pos, b, 0, len);

					pos += len;
					return len;
				}

				@Override
				public int read(byte[] b, int off, int len) throws IOException {
					if (raw == null || pos >= raw.length)
						return 0;

					len = Math.min(len, raw.length - pos);
					System.arraycopy(raw, pos, b, off, len);

					pos += len;
					return len;
				}

				@Override
				public int available() throws IOException {
					return raw == null ? 0 : raw.length - pos;
				}

			};

		}

		return instream;
	}

	public String getLocalAddr() {
		return null;
	}

	public String getLocalName() {
		return null;
	}

	public int getLocalPort() {
		return 0;
	}

	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration<?> getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		if (param != null && param.containsKey(name)) {
			return param.getString(name);
		}

		return null;
	}

	public Map<?, ?> getParameterMap() {
		return param;
	}

	public Enumeration<?> getParameterNames() {
		return param == null ? null : new Enumeration<String>() {

			Iterator<String> it;

			@SuppressWarnings("unchecked")
			public boolean hasMoreElements() {
				if (it == null) {
					it = param.keys();
				}
				return it.hasNext();
			}

			@SuppressWarnings("unchecked")
			public String nextElement() {
				if (it == null) {
					it = param.keys();
				}
				return it.next();
			}

		};
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String name) {
		if (param != null && param.containsKey(name)) {
			return new String[] { param.getString(name) };
		} else {
			return null;
		}
	}

	public String getProtocol() {
		return "mdc";
	}

	public BufferedReader getReader() throws IOException {
		throw new IOException(
				"not support, please using getInputStream instead!");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRemoteHost() {
		// return d.getRemoteIp();
		return null;
	}

	public int getRemotePort() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object o) {
		d.put(name, o);

	}

	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {

	}

	public String getAuthType() {
		return null;
	}

	public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String name) {
		if (name == null)
			return null;

		return (String) d.get(name.toLowerCase());
		// String s = (String) d.get(name);
		// log.debug("getHeader:" + name + "=" + s);
		// return s;
	}

	public Enumeration<?> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	public Enumeration<?> getHeaders(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getMethod() {
		return "mdc";
	}

	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getQueryString() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRequestURI() {
		return Controller.PATH + in.getString(X.URI);
	}

	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpSession getSession() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Gets the file.
	 * 
	 * @param filename
	 *            the filename
	 * @return the file
	 */
	public FileItem getFile(String filename) {
		return new MDCFileItem(filename);
	}

	public JSONObject getJSON() {
		return param;
	}

	class MDCFileItem implements FileItem {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		String filename;
		InputStream in;

		/**
		 * Instantiates a new MDC file item.
		 * 
		 * @param name
		 *            the name
		 */
		MDCFileItem(String name) {
			filename = param.has(name) ? param.getString(name) : null;
		}

		public FileItemHeaders getHeaders() {
			return null;
		}

		public void setHeaders(FileItemHeaders arg0) {

		}

		/* (non-Javadoc)
		 * @see org.apache.commons.fileupload.FileItem#delete()
		 */
		public void delete() {

		}

		/* (non-Javadoc)
		 * @see org.apache.commons.fileupload.FileItem#get()
		 */
		public byte[] get() {
			return MDCHttpRequest.this.raw;
		}

		public String getContentType() {
			return null;
		}

		public String getFieldName() {
			return null;
		}

		public InputStream getInputStream() throws IOException {
			if (in == null) {
				in = MDCHttpRequest.this.getInputStream();
			}
			return in;
		}

		public String getName() {
			return filename;
		}

		public OutputStream getOutputStream() throws IOException {
			return null;
		}

		public long getSize() {
			try {
				if (in == null) {
					in = MDCHttpRequest.this.getInputStream();
				}
				return raw == null ? 0 : raw.length;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			return -1;
		}

		public String getString() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.apache.commons.fileupload.FileItem#getString(java.lang.String)
		 */
		public String getString(String arg0)
				throws UnsupportedEncodingException {
			return null;
		}

		public boolean isFormField() {
			return false;
		}

		public boolean isInMemory() {
			// TODO Auto-generated method stub
			return false;
		}

		public void setFieldName(String arg0) {
			// TODO Auto-generated method stub

		}

		public void setFormField(boolean arg0) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see org.apache.commons.fileupload.FileItem#write(java.io.File)
		 */
		public void write(File arg0) throws Exception {
			// TODO Auto-generated method stub

		}

	}
}
