/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import net.sf.json.JSONObject;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.giisoo.core.bean.Bean.W;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.User;
import com.giisoo.framework.mdc.command.Command;

/**
 * 分布式消息队列工具类，创建一个消息队列的监听器，或向消息队列返一条消息
 * <p>
 * 用于MDC与Web之间的分布式通信
 * 
 * @author joe
 * @since 1.1
 * 
 */
public final class MQ {

	private static Log log = LogFactory.getLog(MQ.class);

	private static boolean enabled = false;
	private static String url;
	private static String user;
	private static String password;
	private static Connection connection;
	private static Session session;
	private static ActiveMQConnectionFactory factory;

	private static String group = X.EMPTY;

	/**
	 * Inits the.
	 * 
	 * @param conf
	 *            the conf
	 * @return true, if successful
	 */
	public synchronized static boolean init(Configuration conf) {
		if (session != null)
			return true;

		enabled = "true".equals(conf.getString("mq.enabled", "false"));

		if (enabled) {
			url = conf.getString("mq.url",
					ActiveMQConnection.DEFAULT_BROKER_URL);
			user = conf.getString("mq.user", ActiveMQConnection.DEFAULT_USER);
			password = conf.getString("mq.password",
					ActiveMQConnection.DEFAULT_PASSWORD);

			group = conf.getString("mq.group", X.EMPTY);
			if (!X.EMPTY.equals(group) && !group.endsWith(".")) {
				group += ".";
			}
		}

		return check();
	}

	/**
	 * queue producer cache
	 */
	private static Map<String, MessageProducer> queues = new HashMap<String, MessageProducer>();

	/**
	 * @deprecated topic producer cache
	 */
	private static Map<String, MessageProducer> topics = new HashMap<String, MessageProducer>();

	/**
	 * check if the connection is enabled and created the connection
	 * 
	 * @return boolean, true if success, otherwise false
	 */
	private static boolean check() {
		if (enabled && (session == null)) {
			try {
				if (factory == null) {
					factory = new ActiveMQConnectionFactory(user, password, url);
				}

				if (connection == null) {
					connection = factory.createConnection();
					connection.start();
				}

				session = connection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		return enabled && session != null;
	}

	private MQ() {
	}

	/**
	 * Checks if is online.
	 * 
	 * @param uid
	 *            the uid
	 * @return true, if is online
	 */
	public static boolean isOnline(long uid) {
		List<TConn> list = TConn.loadAll(uid);
		return list != null && list.size() > 0;
	}

	/**
	 * Send.
	 * 
	 * @param uid
	 *            the uid
	 * @param message
	 *            the message
	 * @param bb
	 *            the bb
	 * @return the int
	 */
	public static int send(long uid, String message, byte[] bb) {
		List<TConn> list = TConn.loadAll(uid);

		int count = 0;
		/**
		 * check connected the message queue ?
		 */

		boolean connected = check();

		if (list != null && list.size() > 0) {
			for (TConn c : list) {
				TConn c1 = TConnCenter.get(c.getClientId());
				if (c1 != null) {
					/**
					 * send directly
					 */
					Response resp = new Response();
					resp.writeByte((byte) (Command.APP));
					resp.writeString(message);
					if (bb != null) {
						resp.writeInt(bb.length);
						resp.writeBytes(bb);
					}

					c1.send(resp);

					log.debug("send directly : " + c.getClientId() + "=>"
							+ message + ", binary: "
							+ (bb == null ? 0 : bb.length));

					count++;
				} else if (connected) {
					/**
					 * send by MQ
					 */
					count += MQ.send(c.getAddress(), c.getClientId(),
							ICallback.RESPONSE, message, bb, null, null, null);

					log.debug("send MQ : " + c.getClientId() + "=>" + message
							+ ", binary: " + (bb == null ? 0 : bb.length));
				} else {
					log.warn("no mdc or MQ avaliable for uid= " + uid
							+ ", message=" + message);
				}
			}
		} else {
			log.warn("no connection for uid= " + uid + ", message=" + message);
		}
		return count;
	}

	public static int sendByClientid(String clientid, String message, byte[] bb) {

		int count = 0;
		/**
		 * check connected the message queue ?
		 */

		if (!check()) {
			/**
			 * not, so send directly
			 */
			TConn c = TConnCenter.get(clientid);
			if (c != null) {
				Response resp = new Response();
				resp.writeByte((byte) (Command.APP));
				resp.writeString(message);
				if (bb != null) {
					resp.writeInt(bb.length);
					resp.writeBytes(bb);
				}

				c.send(resp);

				log.debug("send: " + c.getClientId() + "=>" + message
						+ ", binary: " + (bb == null ? 0 : bb.length));

				count++;
			}
		} else {
			/**
			 * connected message queue, send via message queue
			 */
			Beans<TConn> bs = TConn.load(W.create("clientid", clientid), 0, 1);
			if (bs != null && bs.getList() != null && bs.getList().size() > 0) {
				TConn c = bs.getList().get(0);
				count += MQ.send(c.getAddress(), c.getClientId(),
						ICallback.RESPONSE, message, bb, null, null, null);
			}
		}
		return count;
	}

	/**
	 * Response.
	 * 
	 * @param uid
	 *            the uid
	 * @param message
	 *            the message
	 * @param bb
	 *            the bb
	 * @return the int
	 */
	public static int response(long uid, String message, byte[] bb) {
		List<TConn> list = TConn.loadAll(uid);

		int count = 0;
		/**
		 * check connected the message queue ?
		 */

		if (!check()) {
			/**
			 * not, so send directly
			 */
			if (list != null && list.size() > 0) {
				for (TConn c : list) {
					c = TConnCenter.get(c.getClientId());
					if (c != null) {
						Response resp = new Response();
						resp.writeByte((byte) (Command.APP | 0x80));
						resp.writeString(message);
						if (bb != null) {
							resp.writeInt(bb.length);
							resp.writeBytes(bb);
						}

						c.send(resp);

						log.debug("send: " + c.getClientId() + "=>" + message
								+ ", binary: " + (bb == null ? 0 : bb.length));

						count++;
					}
				}
			}
		} else {
			/**
			 * connected message queue, send via message queue
			 */
			if (list != null && list.size() > 0) {
				for (TConn c : list) {
					count += MQ.response(c.getAddress(), c.getClientId(),
							ICallback.RESPONSE, message, bb, null, null, null);
				}
			}
		}
		return count;
	}

	/**
	 * Send.
	 * 
	 * @param user
	 *            the user
	 * @param message
	 *            the message
	 * @param bb
	 *            the bb
	 * @return the int
	 */
	public static int send(String user, String message, byte[] bb) {
		User u = User.load(user);
		return send(u == null ? -1 : u.getId(), message, bb);
	}

	/**
	 * Response.
	 * 
	 * @param user
	 *            the user
	 * @param message
	 *            the message
	 * @param bb
	 *            the bb
	 * @return the int
	 */
	public static int response(String user, String message, byte[] bb) {
		User u = User.load(user);
		return response(u == null ? -1 : u.getId(), message, bb);
	}

	/**
	 * Send.
	 * 
	 * @param dest
	 *            the dest
	 * @param to
	 *            the to
	 * @param command
	 *            the command
	 * @param message
	 *            the message
	 * @param bb
	 *            the bb
	 * @param src
	 *            the src
	 * @param from
	 *            the from
	 * @param header
	 *            the header
	 * @return the int
	 */
	public static int send(String dest, String to, int command, String message,
			byte[] bb, String src, String from, String header) {
		if (message == null)
			return -1;

		if (!check()) {
			return -1;
		}

		try {

			/**
			 * get the message producer by destination name
			 */
			MessageProducer p = getQueue(dest);
			if (p != null) {
				BytesMessage m = session.createBytesMessage();

				Response resp = new Response();
				resp.writeInt(command);
				resp.writeString(to);
				resp.writeString(from);
				resp.writeString(src);
				resp.writeByte((byte) 0); // send
				resp.writeString(header);

				resp.writeString(message);
				resp.writeInt(bb == null ? 0 : bb.length);
				resp.writeBytes(bb);

				bb = resp.getBytes();
				m.writeInt(bb.length);
				m.writeBytes(bb);

				p.send(m);

				log.debug("AMQ:" + dest + ", " + message);

				return 1;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);

			connection = null;
			session = null;
		}
		return 0;
	}

	/**
	 * Response.
	 * 
	 * @param dest
	 *            the dest
	 * @param to
	 *            the to
	 * @param command
	 *            the command
	 * @param message
	 *            the message
	 * @param bb
	 *            the bb
	 * @param src
	 *            the src
	 * @param from
	 *            the from
	 * @param header
	 *            the header
	 * @return the int
	 */
	public static int response(String dest, String to, int command,
			String message, byte[] bb, String src, String from, String header) {
		if (message == null)
			return -1;

		if (!check()) {
			return -1;
		}

		try {

			/**
			 * get the message producer by destination name
			 */
			MessageProducer p = getQueue(dest);
			if (p != null) {
				BytesMessage m = session.createBytesMessage();

				Response resp = new Response();
				resp.writeInt(command);
				resp.writeString(to);
				resp.writeString(from);
				resp.writeString(src);
				resp.writeByte((byte) 1); // response
				resp.writeString(header);

				resp.writeString(message);
				resp.writeInt(bb == null ? 0 : bb.length);
				resp.writeBytes(bb);

				bb = resp.getBytes();
				m.writeInt(bb.length);
				m.writeBytes(bb);

				p.send(m);

				log.debug("response:" + dest + ", " + message);

				return 1;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);

			connection = null;
			session = null;
		}
		return 0;
	}

	/**
	 * 获取消息队列的发送庄
	 * 
	 * @param name
	 *            消息队列名称
	 * @return messageproducer
	 */
	private static MessageProducer getQueue(String name) {
		synchronized (queues) {
			if (check()) {
				if (queues.containsKey(name)) {
					return queues.get(name);
				}

				try {
					Destination dest = new ActiveMQQueue(group + name);
					MessageProducer producer = session.createProducer(dest);
					producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
					queues.put(name, producer);

					return producer;
				} catch (Exception e) {
					log.error(name, e);
				}
			}
		}

		return null;
	}

	private static MessageProducer getTopic(String name) {
		synchronized (topics) {
			if (check()) {
				if (topics.containsKey(name)) {
					return topics.get(name);
				}

				try {
					Destination dest = new ActiveMQTopic(group + name);
					MessageProducer producer = session.createProducer(dest);
					producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
					topics.put(name, producer);

					return producer;
				} catch (Exception e) {
					log.error(name, e);
				}
			}
		}
		return null;
	}

	/**
	 * Creates the queue.
	 * 
	 * @param name
	 *            the name
	 * @param cb
	 *            the cb
	 * @param mode
	 *            the mode
	 * @return the worker task
	 */
	public static WorkerTask createQueue(final String name, final ICallback cb,
			final int mode) {

		if (enabled) {
			QueueTask r = new QueueTask(name, cb);
			r.schedule(10);
			return r;
		}

		return null;

	}

	/**
	 * Creates the topic.
	 * 
	 * @param name
	 *            the name
	 * @param cb
	 *            the cb
	 * @return the worker task
	 */
	public static WorkerTask createTopic(final String name, final ICallback cb) {

		if (enabled) {
			QueueTask r = new QueueTask(name, cb);
			r.schedule(10);
			return r;
		}

		return null;

	}

	private static void process(Request req, ICallback cb) {
		int command = req.readInt();
		switch (command) {
		case ICallback.REQUEST:
		case ICallback.RESPONSE: {
			String to = req.readString();
			String from = req.readString();
			String src = req.readString();
			byte flag = req.readByte();
			String header = req.readString();
			String message = req.readString();
			int len = req.readInt();
			byte[] bb = req.readBytes(len);

			log.debug("got a message:" + src + ", " + message);

			cb.run(command, to, from, src, flag, JSONObject.fromObject(header),
					JSONObject.fromObject(message), bb);
			break;
		}
		}
	}

	/**
	 * QueueTask
	 * 
	 * @author joe
	 * 
	 */
	private static class QueueTask extends WorkerTask {
		String name;
		ICallback cb;
		MessageConsumer consumer;
		int interval = 0;

		public QueueTask(String name, ICallback cb) {
			this.name = name;
			this.cb = cb;

			connect();
		}

		private void connect() {
			try {
				if (check()) {
					Destination dest = new ActiveMQQueue(group + name);

					consumer = session.createConsumer(dest);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		transient String _name;

		@Override
		public String getName() {
			if (_name == null) {
				_name = "mq." + name;
			}
			return _name;
		}

		@Override
		public void onExecute() {
			try {
				if (consumer == null) {
					connect();
				}
				if (consumer != null) {

					log.debug("waiting for message...");

					Message m = consumer.receive();
					try {
						if (m instanceof BytesMessage) {
							BytesMessage m1 = (BytesMessage) m;
							int len = m1.readInt();
							byte[] bb = new byte[len];
							m1.readBytes(bb);

							Request req = new Request(bb, 0);

							process(req, cb);
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				interval = -1;
			}
		}

		@Override
		public void onFinish() {
			if (interval >= 0) {
				this.schedule(interval);
			}
		}

	}

	/**
	 * @deprecated
	 * @author joe
	 * 
	 */
	private static class TopicTask extends WorkerTask {
		String name;
		ICallback cb;
		MessageConsumer consumer;
		int interval = 0;

		public TopicTask(String name, ICallback cb) {
			this.name = name;
			this.cb = cb;

			connect();
		}

		private void connect() {
			try {
				if (check()) {
					Destination dest = new ActiveMQTopic(group + name);

					consumer = session.createConsumer(dest);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		transient String _name;

		@Override
		public String getName() {
			if (_name == null) {
				_name = "mq." + name;
			}
			return _name;
		}

		@Override
		public void onExecute() {
			try {
				if (consumer == null) {
					connect();
				}
				if (consumer != null) {

					log.debug("waiting for message...");

					Message m = consumer.receive();
					try {
						if (m instanceof BytesMessage) {
							BytesMessage m1 = (BytesMessage) m;
							int len = m1.readInt();
							byte[] bb = new byte[len];
							m1.readBytes(bb);

							Request req = new Request(bb, 0);
							process(req, cb);

						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				interval = -1;
			}
		}

		@Override
		public void onFinish() {
			if (interval >= 0) {
				this.schedule(interval);
			}
		}

	}

	/**
	 * Close.
	 * 
	 * @param clientid
	 *            the clientid
	 */
	public static void close(String clientid) {
		if (!check()) {
			/**
			 * not, so close it directly
			 */
			TConn c = TConnCenter.get(clientid);
			if (c != null) {
				c.close();
			}
		} else {
			/**
			 * connected message queue, send via message queue
			 */
		}
	}

}
