/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc.command;

import javax.servlet.http.*;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.framework.mdc.*;
import com.giisoo.framework.web.*;
import com.giisoo.framework.web.Model.HTTPMethod;

/**
 * App that handle the MDC income packet for app, and dispatch the packet to
 * IAPP which "APP"=packet.app
 * 
 * @author yjiang
 * 
 */
public class App extends Command {

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	public final byte COMMAND = APP;

	/* (non-Javadoc)
	 * @see com.giisoo.framework.mdc.command.Command#onRequest(com.giisoo.framework.mdc.Request, com.giisoo.framework.mdc.Response, com.giisoo.framework.mdc.TConn)
	 */
	@Override
	public boolean onRequest(Request in, Response out, TConn d) {

		String jsonstr = in.readString();
		try {
			JSONObject jo = JSONObject.fromObject(jsonstr);
			int len = in.readInt();
			byte[] bb = in.readBytes(len);

			/**
			 * send the request to web by MQ <br/>
			 * if fail, invoke the web component directly <br/>
			 * create mock http request and mock httpresponse, dispatch the
			 * request to http controller
			 */
			if (jo.has(X.URI) && jo.has(X.SEQ)) {
				String uri = jo.getString(X.URI);
				/**
				 * if the data is correct
				 */
				if (d.valid()) {
					String mquri = SystemConfig.s("mq.mdc.uri", X.EMPTY);

					if (mquri == null
							|| !uri.matches(mquri)
							|| MQ.send("web", null, ICallback.REQUEST, jsonstr,
									bb, TConnCenter.getQueue(), d.getId()
											.toString(), d.getJSON().toString()) <= 0) {

						/**
						 * the connection is invalid and send failured call hte
						 * http service in local create mock http request
						 */
						MDCHttpRequest req = MDCHttpRequest.create(jo, bb, d);

						JSONObject r = new JSONObject();

						MDCHttpResponse resp = MDCHttpResponse.create(r, d);

						/**
						 * send http request to controller
						 */
						Controller.dispatch(jo.getString(X.URI), req, resp,
								new HTTPMethod(Model.METHOD_MDC));

						/**
						 * write back to client
						 */
						JSONObject r1 = new JSONObject();
						r1.put(X.SEQ, jo.getLong(X.SEQ));
						r1.put(X.RESULT, r);
						if (r.has(X.STATE)) {
							r1.put(X.STATE, r.get(X.STATE));
						} else {
							r1.put(X.STATE, X.OK_200);
						}

						out.writeString(r1.toString());
						bb = resp.getBytes();
						if (bb != null) {
							out.writeInt(bb.length);
							out.writeBytes(bb);
						}
						log.debug("response: " + r1.toString() + ", bytes:"
								+ (bb == null ? 0 : bb.length));
					}
				} else {
					JSONObject r1 = new JSONObject();
					r1.put(X.STATE, X.FAIL);
					r1.put(X.MESSAGE, "connection not ready, required HELLO");
					out.writeString(r1.toString());
				}
			} else {
				/**
				 * the uri or seq missed
				 */
				JSONObject r1 = new JSONObject();
				r1.put(X.STATE, X.FAIL);
				r1.put(X.MESSAGE, "uri, seq are required, param={} is optional");
				out.writeString(r1.toString());
			}
		} catch (Exception e) {
			log.error(jsonstr, e);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.giisoo.framework.mdc.command.Command#onResponse(com.giisoo.framework.mdc.Request, com.giisoo.framework.mdc.TConn)
	 */
	@Override
	public boolean onResponse(Request in, TConn d) throws Exception {
		String jsonstr = in.readString();
		int len = in.readInt();
		byte[] bb = in.readBytes(len);

		log.debug("got:" + jsonstr + ", bb:" + (bb == null ? 0 : bb.length)
				+ ", " + d.getRemoteIp());

		JSONObject jo = JSONObject.fromObject(jsonstr);

		TConn.onResponse(jo, bb);

		return true;
	}

	/**
	 * Instantiates a new app.
	 */
	public App() {
	}

	/**
	 * Hello.
	 * 
	 * @param d
	 *            the d
	 */
	public static void hello(TConn d) {

		JSONObject in = new JSONObject();
		in.put(X.URI, "/hello");
		JSONObject out = new JSONObject();
		HttpServletRequest req = MDCHttpRequest.create(in, null, d);
		HttpServletResponse resp = MDCHttpResponse.create(out, d);
		Controller.dispatch(in.getString(X.URI), req, resp, new HTTPMethod(
				Model.METHOD_MDC));

	}

	/**
	 * Bye.
	 * 
	 * @param d
	 *            the d
	 */
	public static void bye(TConn d) {

		JSONObject in = new JSONObject();
		in.put(X.URI, "/bye");
		JSONObject out = new JSONObject();
		HttpServletRequest req = MDCHttpRequest.create(in, null, d);
		HttpServletResponse resp = MDCHttpResponse.create(out, d);
		Controller.dispatch(in.getString(X.URI), req, resp, new HTTPMethod(
				Model.METHOD_MDC));

	}

}
