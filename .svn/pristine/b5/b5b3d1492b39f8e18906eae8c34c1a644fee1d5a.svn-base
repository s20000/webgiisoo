package com.giisoo.core.rpc;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.*;
import java.nio.channels.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;



import org.apache.commons.configuration.Configuration;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.*;
import org.apache.mina.core.session.*;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.giisoo.core.conf.Config;

// TODO: Auto-generated Javadoc
/**
 * The Class ServerStub3.
 *
 * @author yjiang
 */
class ServerStub3 extends Stub implements IoHandler {

	/** The handler. */
	private IRemote handler;
	
	/** The methods. */
	private TreeMap<String, Method> methods = new TreeMap<String, Method>();

	/** The Constant NAME. */
	static final String NAME = "stub-";
	
	/** The Constant counter. */
	static final AtomicInteger counter = new AtomicInteger(0);

	/** The address. */
	private final InetSocketAddress address;
	
	/** The selector. */
	private Selector selector;
	
	/** The server. */
	private ServerSocketChannel server;
	
	/** The process number. */
	private final int PROCESS_NUMBER = 4;
	
	/** The _conf. */
	private Configuration _conf;

	/** The acceptor. */
	private IoAcceptor acceptor;
	
	/** The is running. */
	private boolean isRunning = false;

	/* (non-Javadoc)
	 * @see com.giisoo.rpc.Stub#close()
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

		if (server != null) {
			try {
				server.socket().close();
				server.close();
			} catch (IOException e) {
				log.warn("close socket server fails", e);
			} finally {
				server = null;
			}
		}
	}

	/**
	 * Instantiates a new server stub3.
	 *
	 * @param host the host
	 * @param port the port
	 */
	public ServerStub3(String host, int port) {
		_conf = Config.getConfig();
		address = (host == null) ? new InetSocketAddress(port) : new InetSocketAddress(host, port);

	}

	/**
	 * Instantiates a new server stub3.
	 *
	 * @param port the port
	 */
	public ServerStub3(int port) {
		this(null, port);
	}

	/**
	 * Start.
	 *
	 * @return the stub
	 */
	public synchronized Stub start() {
		if (isRunning) {
			log.info("Error, server is already running on [" + address + "]");
			return this;
		}

		try {
			log.info("starting stub server @port:" + address.getPort());
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
				ssc.setSoLinger(0); // close the socket immediately when invoke the close api
				// ssc.setReadBufferSize(4096);
				// ssc.setSendBufferSize(4096);
			}
			acceptor.bind(address);
			acceptor.setCloseOnDeactivation(true);

			log.info("stub server started");

			isRunning = true;

		} catch (Exception e) {
			log.error("stub server quit. due to exception:", e);
			System.exit(0);
		}

		return this;
	}

	/* (non-Javadoc)
	 * @see com.giisoo.rpc.Stub#stop()
	 */
	public void stop() {
		acceptor.unbind();
	}

	/* (non-Javadoc)
	 * @see com.giisoo.rpc.Stub#bind(com.giisoo.rpc.IRemote)
	 */
	@Override
	public void bind(IRemote remote) throws IOException {
		handler = remote;

		Method[] ms = remote.getClass().getMethods();

		for (Method m : ms) {
			Class<?>[] exs = m.getExceptionTypes();
			for (Class<?> ex : exs) {
				if (ex.equals(RemoteException.class)) {
					if ((m.getModifiers() & Modifier.PUBLIC) > 0) {
						String name = m.getName();
						if (methods.containsKey(name))
							throw new IOException("[" + name + "] duplicated!");
						methods.put(name, m);

						log.debug("binding method: " + name);
					}
					break;
				}
			}
		}
	}

	/**
	 * Service.
	 *
	 * @param o the o
	 * @param session the session
	 */
	void service(IoBuffer o, IoSession session) {
		try {
			// System.out.println(o.remaining() + "/" + o.capacity());

			session.setAttribute("last", System.currentTimeMillis());

			SimpleIoBuffer in = (SimpleIoBuffer) session.getAttribute("buf");
			if (in == null) {
				in = SimpleIoBuffer.create(16384);

				session.setAttribute("buf", in);
			}
			byte[] data = new byte[o.remaining()];
			o.get(data);
			in.append(data);

			while (in.length() > 8) {
				in.mark();
				int flag = in.getInt();
				if (!valid(flag)) {
					log.info("flag is not correct! flag:" + flag + ",from: " + session.getRemoteAddress());
					session.close(true);
					return;
				}

				int len = in.getInt();

				if (len <= 0) {
					log.error("Wrong lendth: " + len + " - " + session.getRemoteAddress());
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

					Stub s = (Stub) session.getAttribute("stub");
					if (s != null) {
						Object obj = s.process(flag, b);
						invoke(obj, s);
						session.setAttribute("last", System.currentTimeMillis());
					}
				}
			}
		} catch (Throwable e) {
			log.error("closing stub: " + session.getRemoteAddress(), e);
			session.close(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#sessionCreated(org.apache.mina.core.session.IoSession)
	 */
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		log.info("stub created:" + session.getRemoteAddress());

		Stub s = new Stub(session);
		session.setAttribute("stub", s);
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#sessionOpened(org.apache.mina.core.session.IoSession)
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {

	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#sessionClosed(org.apache.mina.core.session.IoSession)
	 */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		log.info("closed stub: " + session.getRemoteAddress());
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#sessionIdle(org.apache.mina.core.session.IoSession, org.apache.mina.core.session.IdleStatus)
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		if (IdleStatus.BOTH_IDLE.equals(status)) {
			Long l = (Long) session.getAttribute("last");
			if (l != null && System.currentTimeMillis() - l > 60 * 1000) {
				session.close(true);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#exceptionCaught(org.apache.mina.core.session.IoSession, java.lang.Throwable)
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#messageReceived(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		if (message instanceof IoBuffer) {
			service((IoBuffer) message, session);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#messageSent(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
	}

	/**
	 * Invoke.
	 *
	 * @param o the o
	 * @param s the s
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void invoke(Object o, Stub s) throws IOException {
		// System.out.println("invoke: " + o);

		if (o instanceof Command) {
			Stub.set(s);

			Command cmd = (Command) o;
			String method = cmd.cmd;
			Object[] param = cmd.params;

			try {
				Method m = methods.get(method);
				if (m != null) {

					Object res = m.invoke(handler, param);
					s.write(res);

				} else {
					s.write(Stub.NULL);

					log.warn("method [" + method + "] not found !");
				}
			} catch (Throwable e) {

				log.error("[" + method + "] " + s.getHost(), e);

				s.write(Stub.NULL);

			}
		} else {
			s.write(Stub.NULL);
		}
	}
}
