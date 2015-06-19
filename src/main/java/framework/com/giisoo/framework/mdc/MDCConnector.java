/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.*;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.*;
import org.apache.mina.core.session.*;

import com.giisoo.core.bean.*;
import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.TimeStamp;
import com.giisoo.core.conf.*;
import com.giisoo.framework.mdc.command.Command;
import com.giisoo.utils.base.*;
import com.giisoo.utils.base.RSA.Key;

/**
 * MDC Connector
 * 
 * @author yjiang
 * 
 */
public abstract class MDCConnector extends IoHandlerAdapter {

	final static Log log = LogFactory.getLog(MDCConnector.class);

	/**
	 * the the max size of a packet, 32KB
	 */
	static int MAX_SIZE = MDCServer.MAX_SIZE;

	protected Selector selector;
	protected IoConnector connector;
	protected static Configuration _conf;

	protected static boolean inited = false;

	/**
	 * Close.
	 */
	public void close() {
		if (selector != null) {
			selector.wakeup();
			try {
				selector.close();
			} catch (IOException e1) {
				log.warn("close selector fails", e1);
			} finally {
				selector = null;
			}
		}

		if (connector != null) {
			connector.dispose();
			connector = null;
		}
	}

	/**
	 * Instantiates a new MDC connector.
	 */
	protected MDCConnector() {
	}

	/**
	 * Inits the.
	 */
	public synchronized static void init() {
		if (inited) {
			return;
		}

		_conf = Config.getConfig();

		/**
		 * initialize app command
		 */
		Command.init();

		/**
		 * initialize the RSA key, hardcode 2048 bits
		 */
		TConn.pub_key = SystemConfig.s("pub_key", null);
		if (TConn.pub_key == null) {
			Key k = RSA.generate(2048);
			TConn.pri_key = k.pri_key;
			TConn.pub_key = k.pub_key;

			/**
			 * set back in database
			 */
			SystemConfig.setConfig("pri_key", TConn.pri_key);
			SystemConfig.setConfig("pub_key", TConn.pub_key);
		} else {

			/**
			 * get from the database
			 */
			TConn.pri_key = SystemConfig.s("pri_key", null);
		}

		inited = true;

	}

	/**
	 * Service.
	 * 
	 * @param o
	 *            the o
	 * @param session
	 *            the session
	 */
	void service(IoBuffer o, IoSession session) {
		try {
			// System.out.println(o.remaining() + "/" + o.capacity());

			session.setAttribute("last", System.currentTimeMillis());

			SimpleIoBuffer in = (SimpleIoBuffer) session.getAttribute("buf");
			if (in == null) {
				in = SimpleIoBuffer.create(4096);
				session.setAttribute("buf", in);
			}
			byte[] data = new byte[o.remaining()];
			o.get(data);
			in.append(data);

			// log.debug("recv: " + data.length + ", " +
			// session.getRemoteAddress());

			while (in.length() > 5) {
				in.mark();
				/**
				 * Byte 1: head of the package<br/>
				 * bit 7-6: "01", indicator of MDC<br/>
				 * bit 5: encrypt indicator, "0": no; "1": encrypted<br/>
				 * bit 4: zip indicator, "0": no, "1": ziped<br/>
				 * bit 0-3: reserved<br/>
				 * Byte 2-5: length of data<br/>
				 * Byte[…]: data array<br/>
				 * 
				 */

				byte head = in.read();
				/**
				 * test the head indicator, if not correct close it
				 */
				if ((head & 0xC0) != 0x40) {
					log.info("flag is not correct! flag:" + head + ",from: "
							+ session.getRemoteAddress());

					session.close(true);
					return;
				}

				int len = in.getInt();

				if (len <= 0 || len > MAX_SIZE) {
					log.error("mdcconnector.Wrong lendth: " + len + "/"
							+ MAX_SIZE + " - " + session.getRemoteAddress());
					session.close(true);
					break;
				}

				if (in.length() < len) {
					in.reset();
					break;
				} else {
					// do it
					// log.info("stub.package.size: " + len);

					byte[] b = new byte[len];
					in.read(b);

					if (TConn.DEBUG) {
						log.debug("recv: " + Bean.toString(b));
					}

					/**
					 * test the zip flag
					 */
					if ((head & 0x10) > 0) {
						b = Zip.unzip(b);
					}

					TConn d = (TConn) session.getAttribute("conn");
					if (d != null) {
						/**
						 * test the encrypted flag
						 */
						if ((head & 0x20) > 0) {
							b = DES.decode(b, d.deskey);
						}

						/**
						 * test if the packet is for mdc or app
						 */
						d.process(b);

						session.setAttribute("last", System.currentTimeMillis());
					}
				}
			}
		} catch (Throwable e) {
			log.error("closing stub: " + session.getRemoteAddress(), e);
			session.close(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.core.service.IoHandlerAdapter#sessionCreated(org.apache
	 * .mina.core.session.IoSession)
	 */
	public void sessionCreated(IoSession session) throws Exception {
		String remote = session.getRemoteAddress().toString();
		log.info("stub created:" + remote);

		/**
		 * check the allow ip
		 */
		if (TConn.ALLOW_IP == null || "*".equals(TConn.ALLOW_IP)
				|| remote.matches(TConn.ALLOW_IP)) {
			TConn d = new TConn(session);
			session.setAttribute("conn", d);
		} else {
			log.warn("deny the connection:" + remote + ", allow ip:"
					+ TConn.ALLOW_IP);
			session.close(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.core.service.IoHandlerAdapter#sessionClosed(org.apache
	 * .mina.core.session.IoSession)
	 */
	public void sessionClosed(IoSession session) throws Exception {
		log.debug("closed stub: " + session.getRemoteAddress());
		TConn d = (TConn) session.getAttribute("conn");
		if (d != null) {
			d.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.core.service.IoHandlerAdapter#sessionIdle(org.apache.
	 * mina.core.session.IoSession, org.apache.mina.core.session.IdleStatus)
	 */
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		if (IdleStatus.BOTH_IDLE.equals(status)) {
			Long l = (Long) session.getAttribute("last");
			if (l != null && System.currentTimeMillis() - l > 60 * 1000) {
				session.close(true);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.core.service.IoHandlerAdapter#messageReceived(org.apache
	 * .mina.core.session.IoSession, java.lang.Object)
	 */
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		// System.out.println(message);
		if (message instanceof IoBuffer) {
			service((IoBuffer) message, session);
		}
	}

	private static MDCConnector tcpconnector;
	private static MDCConnector udpconnector;

	/**
	 * 
	 * @param host
	 * @param port
	 * @return TConn
	 */
	public synchronized static TConn connectByTcp(String host, int port) {
		return connectByTcp(host, port, X.AMINUTE);
	}

	/**
	 * Connect by tcp.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @return the t conn
	 */
	public synchronized static TConn connectByTcp(String host, int port,
			long timeout) {

		TimeStamp t = TimeStamp.create();

		try {
			if (tcpconnector == null) {
				tcpconnector = new TDCConnector();
			}

			tcpconnector.connector.setConnectTimeoutMillis(timeout);
			
			ConnectFuture connFuture = tcpconnector.connector
					.connect(new InetSocketAddress(host, port));

			connFuture.awaitUninterruptibly(timeout);
			IoSession session = connFuture.getSession();

			TConn c = new TConn(session);

			session.setAttribute("conn", c);
			return c;
		} catch (Exception e) {
			log.error("error, [" + host + ":" + port + "], cost: " + t.past()
					+ "ms, timeout=" + timeout, e);
		}

		return null;
	}

	/**
	 * Connect by udp.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @return the t conn
	 */
	public synchronized static TConn connectByUdp(String host, int port) {
		try {
			if (udpconnector == null) {
				udpconnector = new UDCConnector();
			}

			ConnectFuture connFuture = udpconnector.connector
					.connect(new InetSocketAddress(host, port));
			connFuture.awaitUninterruptibly();
			IoSession session = connFuture.getSession();

			TConn c = new TConn(session);

			session.setAttribute("conn", c);
			return c;
		} catch (Exception e) {
			log.error("[" + host + ":" + port + "]", e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.core.service.IoHandlerAdapter#exceptionCaught(org.apache
	 * .mina.core.session.IoSession, java.lang.Throwable)
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		log.error(cause.getMessage(), cause);
	}

}
