package com.giisoo.core.rpc;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: Auto-generated Javadoc
/**
 * The Class ServerStub.
 *
 * @author yjiang
 * @deprecated 
 */
class ServerStub extends Stub implements Runnable {

	/** The handler. */
	private IRemote handler;
	
	/** The server. */
	private ServerSocket server;
	
	/** The methods. */
	private TreeMap<String, Method> methods = new TreeMap<String, Method>();

	/** The Constant NAME. */
	static final String NAME = "stub-";
	
	/** The Constant counter. */
	static final AtomicInteger counter = new AtomicInteger(0);

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (server != null) {
			try {
				Socket sock = server.accept();

				Stub stub = new Stub(sock);

				new Service(stub).start();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.giisoo.rpc.Stub#close()
	 */
	public void close() {
		if (server != null) {
			try {
				server.close();
				server = null;
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Instantiates a new server stub.
	 *
	 * @param server the server
	 */
	public ServerStub(ServerSocket server) {
		this.server = server;
	}

	/**
	 * Start.
	 *
	 * @return the stub
	 */
	public Stub start() {
		new Thread(this).start();

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
	 * The Class Service.
	 *
	 * @author yjiang
	 */
	class Service implements Runnable {

		/** The stub. */
		private Stub stub;

		/**
		 * Instantiates a new service.
		 *
		 * @param stub the stub
		 */
		public Service(Stub stub) {
			this.stub = stub;
		}

		/**
		 * Start.
		 */
		public void start() {
			log.info("new stub: " + stub.getHost());

			Thread t = new Thread(this);
			t.setName(NAME + counter.incrementAndGet());
			t.start();
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				// int count = 0;
				// long time = 0;
				// long time2 = 0;

				while (true) {
					Object o = stub.read();
					// count++;
					// TimeStamp t = TimeStamp.create();

					if (o instanceof Command) {
						Command cmd = (Command) o;
						String method = cmd.cmd;
						Object[] param = cmd.params;

						try {
							Method m = methods.get(method);
							if (m != null) {

								set(stub);

								Object res = m.invoke(handler, param);

								stub.write(res);
							} else {
								stub.write(null);

								log.warn("method [" + method + "] not found !");
							}
						} catch (Throwable e) {

							stub.write(null);

							log.error("[" + method + "] " + e.getMessage(), e);

						}
					} else {
						stub.write(null);
					}
				}
			} catch (Throwable e) {
				log.error("close stub: " + stub.getHost() + ", " + e.getMessage());

				// if(e instanceof )
				stub.close();
			}
		}
	}
}
