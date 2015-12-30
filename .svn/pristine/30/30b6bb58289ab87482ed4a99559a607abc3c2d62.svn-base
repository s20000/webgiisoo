/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc;

import org.apache.commons.logging.*;
import org.apache.mina.core.session.*;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.*;

/**
 * TCP connector
 * 
 * @author yjiang
 * 
 */
class TDCConnector extends MDCConnector {

	final static Log log = LogFactory.getLog(TDCConnector.class);

	/**
	 * Instantiates a new TDC connector.
	 */
	protected TDCConnector() {

		connector = new NioSocketConnector();
		connector.setHandler(this);

		IoSessionConfig isc = connector.getSessionConfig();
		if (isc instanceof SocketSessionConfig) {
			SocketSessionConfig ssc = ((SocketSessionConfig) isc);
			ssc.setReuseAddress(true);
			ssc.setSoLinger(0); // close the socket immediately when invoke the
								// close api
			// ssc.setReadBufferSize(4096);
			// ssc.setSendBufferSize(4096);
		}

		log.info("stub server started");

	}

}
