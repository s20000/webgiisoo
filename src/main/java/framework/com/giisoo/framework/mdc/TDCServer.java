/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc;

import org.apache.commons.logging.*;
import org.apache.mina.core.session.*;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 * TDC server
 * 
 * @author yjiang
 * 
 */
class TDCServer extends MDCServer {

	final static Log log = LogFactory.getLog(TDCServer.class);

	/**
	 * Instantiates a new TDC server.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 */
	protected TDCServer(String host, int port) {
		super(host, port);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.framework.mdc.MDCServer#start()
	 */
	public synchronized TDCServer start() {
		if (isRunning) {
			log.info("Error, server is already running on [" + address + "]");
			return this;
		}

		try {

			if (testKey()) {
				log.info("starting stub server mdc@port:" + address.getPort());
				int process = PROCESS_NUMBER;
				if (_conf != null) {
					process = _conf.getInt("stub.process", PROCESS_NUMBER);
				}

				acceptor = new NioSocketAcceptor(process);
				acceptor.setHandler(this);
				IoSessionConfig isc = acceptor.getSessionConfig();
				if (isc instanceof SocketSessionConfig) {
					SocketSessionConfig ssc = ((SocketSessionConfig) isc);
					ssc.setReuseAddress(true);
					ssc.setSoLinger(0);
					ssc.setIdleTime(IdleStatus.BOTH_IDLE, 300); // 300seconds
					// close the socket immediately when invoke the close api
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
