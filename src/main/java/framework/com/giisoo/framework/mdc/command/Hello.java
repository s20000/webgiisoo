/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc.command;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.framework.common.Counter;
import com.giisoo.framework.mdc.*;
import com.giisoo.utils.base.*;

/**
 * 
 * @author yjiang
 * 
 */
public class Hello extends Command {

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	public final byte COMMAND = HELLO;

	/* (non-Javadoc)
	 * @see com.giisoo.framework.mdc.command.Command#onRequest(com.giisoo.framework.mdc.Request, com.giisoo.framework.mdc.Response, com.giisoo.framework.mdc.TConn)
	 */
	@Override
	public boolean onRequest(Request in, Response out, TConn d) {

		Counter.add("mdc.hello", 1);

		String jsonstr = in.readString();
		try {
			JSONObject jo = JSONObject.fromObject(jsonstr);

			String clientid = jo.getString(X.CLIENTID);
			String uid = jo.getString(X.UID);
			if (jo.containsKey(X.CAPABILITY)) {
				d.setCapability(jo.getInt(X.CAPABILITY));
			}

			if (d.isSupportEncode()) {
				/**
				 * is support encode, then using my private key to decode the
				 * data
				 */
				uid = new String(RSA.decode(Base64.decode(uid), TConn.pri_key));
			}

			JSONObject r = new JSONObject();

			if (d.load(clientid)) {

				log.debug("clientid:" + clientid + ", uid:" + uid + "-"
						+ d.getRemoteIp());

				if (d.validate(uid)) {

					log.debug("[" + uid + "] say hello, obj:" + d);

					r.put(X.STATE, X.OK_200);

					String code = UID.random(24);
					log.info("uid:" + uid + ", code:" + code + ", pubkey:"
							+ d.getKey());

					if (d.isSupportEncode()) {
						d.deskey = code.getBytes();
						byte[] b = RSA.encode(d.deskey, d.getKey());
						r.put(X.CODE, Base64.encode(b));
					}

					/**
					 * set default data for tconn;
					 */
					d.set("user-agent", "mdc");
					d.set("sid", "mdc_" + d.getClientId());
					d.set("clientid", clientid);
					d.setId(clientid);

					d.update(V.create("capability", d.getCapability()).set(
							"ip", d.getRemoteIp()));

					/**
					 * cache it in TConnCenter
					 */
					TConnCenter.add(d);

					App.hello(d);
					// d.startSync();

					// log.debug("login:ok:" + uid + "/" + imei);
				} else {
					r.put(X.STATE, X.FAIL);
					r.put(X.MESSAGE, "uid is wrong");
				}
			} else {
				r.put(X.STATE, X.FAIL);
				r.put(X.MESSAGE, "clientid is wrong");
			}

			r.put(X.VERSION, SystemConfig.s(X.VERSION, null));

			out.writeString(r.toString());
			out.setRequiredEncode(false);

			// d.onHello(IResponse.STATE_OK, d.deskey);
		} catch (Exception e) {
			log.error(jsonstr, e);

			JSONObject r = new JSONObject();
			r.put(X.STATE, X.FAIL);
			r.put(X.MESSAGE, e.getMessage());

			out.writeString(r.toString());
			out.setRequiredEncode(false);

		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.giisoo.framework.mdc.command.Command#onResponse(com.giisoo.framework.mdc.Request, com.giisoo.framework.mdc.TConn)
	 */
	@Override
	public boolean onResponse(Request in, TConn d) throws Exception {
		String jsonstr = in.readString();
		JSONObject jo = JSONObject.fromObject(jsonstr);
		log.debug("hello.resp:" + jo + "-" + d.getRemoteIp());
		String key = jo.has(X.CODE) ? jo.getString(X.CODE) : null;

		if (Command.STATE_OK == jo.getInt(X.STATE) && key != null) {
			log.debug("set valiid! " + d);
			d.setValid(true);

			byte[] deskey = Base64.decode(key);
			deskey = RSA.decode(deskey, TConn.pri_key);

			d.onHello(IResponse.STATE_OK, deskey);

		} else {
			d.onHello(IResponse.STATE_FAIL, null);
		}

		return true;
	}

}
