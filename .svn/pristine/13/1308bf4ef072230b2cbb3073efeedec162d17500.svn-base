/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc;

import java.util.concurrent.*;

import org.apache.commons.logging.*;
import org.apache.mina.core.session.*;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.*;

/**
 * UDC server
 * 
 * @author yjiang
 * 
 */
class UDCServer extends MDCServer {

	final static Log log = LogFactory.getLog(UDCServer.class);

	/**
	 * Instantiates a new UDC server.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 */
	protected UDCServer(String host, int port) {
		super(host, port);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.framework.mdc.MDCServer#start()
	 */
	public synchronized UDCServer start() {
		if (isRunning) {
			log.info("Error, server is already running on [" + address + "]");
			return this;
		}

		try {
			if (testKey()) {
				log.info("starting stub server udc@port:" + address.getPort());
				int process = PROCESS_NUMBER;
				if (_conf != null) {
					process = _conf.getInt("stub.process", PROCESS_NUMBER);
				}

				acceptor = new NioDatagramAcceptor(
						new ScheduledThreadPoolExecutor(process));
				// acceptor = new NioSocketAcceptor(process);
				acceptor.setHandler(this);
				IoSessionConfig isc = acceptor.getSessionConfig();
				if (isc instanceof SocketSessionConfig) {
					SocketSessionConfig ssc = ((SocketSessionConfig) isc);
					ssc.setReuseAddress(true);
					ssc.setSoLinger(0); // close the socket immediately when
										// invoke the close api
					// ssc.setReadBufferSize(4096);
					// ssc.setSendBufferSize(4096);
				}
				acceptor.bind(address);
				acceptor.setCloseOnDeactivation(true);

				log.info("stub server started");

				isRunning = true;
			} else {
				log.error("bad pubkey and prikey, pubkey=" + TConn.pub_key
						+ ", prikey=" + TConn.pri_key);
			}
		} catch (Exception e) {
			log.error("stub server quit. due to exception:", e);
			System.exit(0);
		}

		return this;
	}

}
