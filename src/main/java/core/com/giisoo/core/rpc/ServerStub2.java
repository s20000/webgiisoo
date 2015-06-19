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
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.giisoo.core.conf.Config;

// TODO: Auto-generated Javadoc
/**
 * The Class ServerStub2.
 *
 * @author yjiang
 */
class ServerStub2 extends Stub implements IoHandler {

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

	/** The Constant key_oout. */
	public static final Object key_oout = new Object();
	
	/** The Constant key_zout. */
	public static final Object key_zout = new Object();
	
	/** The Constant key_bout. */
	public static final Object key_bout = new Object();

	/** The acceptor. */
	private IoAcceptor acceptor;

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
	 * Instantiates a new server stub2.
	 *
	 * @param host the host
	 * @param port the port
	 */
	public ServerStub2(String host, int port) {
		int process = PROCESS_NUMBER;
		Configuration conf = Config.getConfig();
		if (conf != null) {
			process = conf.getInt("stub.process", PROCESS_NUMBER);
		}
		acceptor = new NioSocketAcceptor(process);
		address = (host == null) ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
	}

	/**
	 * Instantiates a new server stub2.
	 *
	 * @param port the port
	 */
	public ServerStub2(int port) {
		this(null, port);
	}

	/**
	 * Start.
	 *
	 * @return the stub
	 */
	public Stub start() {
		log.info("initializing iobuffer for stub server");
		IoBufferPool.init();
		log.info("starting stub server @port:" + address.getPort());
		try {
			acceptor.setHandler(this);
			acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new StubCodecFactory()));
			acceptor.bind(address);

			log.info("stub server started");
		} catch (Exception e) {
			log.warn("stub server quit. due to exception:", e);
			close();
		}

		return this;
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
	void service(Object o, IoSession session) {
		try {
			if (o instanceof Command) {
				Command cmd = (Command) o;
				String method = cmd.cmd;
				Object[] param = cmd.params;

				try {
					Method m = methods.get(method);
					if (m != null) {

						Object res = m.invoke(handler, param);

						session.write(res);
					} else {
						session.write(Stub.NULL);

						log.warn("method [" + method + "] not found !");
					}
				} catch (Throwable e) {

					log.error("[" + method + "] " + session.getRemoteAddress(), e);

					session.write(Stub.NULL);

				}
			} else {
				session.write(Stub.NULL);
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
		GifoxByteArrayOuputStream bout = new GifoxByteArrayOuputStream(10240);
		session.setAttribute(key_bout, bout);

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
		returnbuffer(session);
		log.error("closed stub: " + session.getRemoteAddress());
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#sessionIdle(org.apache.mina.core.session.IoSession, org.apache.mina.core.session.IdleStatus)
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#exceptionCaught(org.apache.mina.core.session.IoSession, java.lang.Throwable)
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		returnbuffer(session);
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#messageReceived(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// log.info("rcv msg "+message +" from "+session.getRemoteAddress());
		if (message instanceof IoBuffer) {
			service(message, session);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#messageSent(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		returnbuffer(session);
	}

	/**
	 * Returnbuffer.
	 *
	 * @param session the session
	 */
	private void returnbuffer(IoSession session) {
		try {
			IoBuffer buffer = (IoBuffer) session.removeAttribute(IoBufferPool.key);
			if (buffer != null) {
				buffer.clear();
				IoBufferPool.getInstance().returnBuffer(buffer);
			}
		} catch (Exception e) {
			log.error("return buffer used by stub: " + session.getRemoteAddress() + " failed", e);
		}
		try {
			GifoxByteArrayOuputStream bos = (GifoxByteArrayOuputStream) session.getAttribute(ServerStub2.key_bout);
			if (bos != null)
				bos.close();
		} catch (Exception e) {
			log.error("close zip stream used by stub: " + session.getRemoteAddress() + " failed", e);
		}

	}

}
