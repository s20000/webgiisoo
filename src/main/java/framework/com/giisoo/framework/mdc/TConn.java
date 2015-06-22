/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc;

import java.sql.*;
import java.util.*;

import net.sf.json.JSONObject;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.giisoo.core.bean.*;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.Stat;
import com.giisoo.framework.common.User;
import com.giisoo.framework.mdc.command.*;
import com.giisoo.framework.web.Language;
import com.giisoo.utils.base.*;
import com.giisoo.utils.base.Base64;

/**
 * MDC device which interact with mdc client in server side
 * 
 * @author yjiang
 * 
 */
@DBMapping(table = "tblconn")
public class TConn extends Bean {

	private static final long serialVersionUID = 1L;

	public static final int STATE_NEW = 0;
	public static final int STATE_ACTIVATED = 1;
	public static final int STATE_HELLOED = 2;
	public static final int STATE_LOGINED = 3;
	public static final int STATE_DISCONNECTED = 4;
	public static final int STATE_ERROR = 5;
	public static final int STATE_PENDING = 6;

	public boolean isConnected() {
		return session != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((session == null) ? 0 : session.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TConn other = (TConn) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (session == null) {
			if (other.session != null)
				return false;
		} else if (!session.equals(other.session))
			return false;
		return true;
	}

	/**
	 * indicate whether debug status
	 */
	public static boolean DEBUG = false;

	/**
	 * the public key of the whole system, store and load from database
	 */
	public static String pub_key;

	/**
	 * the private key of the whole system, store and load from database
	 */
	public static String pri_key;

	private Object id;
	private String clientid;
	String phone;
	String alias;
	String password;
	String address;
	String key;

	int locked;
	int uid;
	long sent;
	long received;

	long created;
	String ip;
	long updated;

	long lastio = 0;

	boolean valid = false;

	transient IoSession session;

	public byte[] deskey;
	private int capability = 0x30; // 0x30 (encode, zip)

	private IResponse resp;

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}

	public int getCapability() {
		return capability;
	}

	public void setCapability(int capability) {
		this.capability = capability;
	}

	public boolean isSupportZip() {
		return (capability & 0x10) != 0;
	}

	public boolean isSupportEncode() {
		return (capability & 0x20) != 0;
	}

	public String getClientid() {
		return clientid;
	}

	public int getLocked() {
		return locked;
	}

	public long getCreated() {
		return created;
	}

	public String getIp() {
		return ip;
	}

	public byte[] getDeskey() {
		return deskey;
	}

	public String getKey() {
		return key;
	}

	public String getPassword() {
		return password;
	}

	public String getClientId() {
		return clientid;
	}

	public String getAddress() {
		return address;
	}

	public String getPhone() {
		return phone;
	}

	public String getAlias() {
		return alias;
	}

	/**
	 * Instantiates a new t conn.
	 * 
	 * @param session
	 *            the session
	 */
	public TConn(IoSession session) {

		this.session = session;
	}

	/**
	 * Instantiates a new t conn.
	 */
	public TConn() {
	}

	/**
	 * Load.
	 * 
	 * @param clientid
	 *            the clientid
	 * @return true, if successful
	 */
	public boolean load(String clientid) {
		return Bean.load("clientid=?", new Object[] { clientid }, this);
	}

	/**
	 * Load.
	 * 
	 * @param w
	 *            the w
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @return the beans
	 */
	public static Beans<TConn> load(W w, int s, int n) {
		if (w == null) {
			w = W.create();
		}
		w.and("uid", 0, W.OP_GT);
		return Bean.load(w.where(), w.args(),
				(w.orderby() == null) ? "order by password" : w.orderby(), s,
				n, TConn.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
	 */
	@Override
	protected void load(ResultSet r) throws SQLException {
		clientid = r.getString("clientid");
		capability = r.getInt("capability");
		phone = r.getString("phone");
		alias = r.getString("alias");
		password = r.getString("password");
		created = r.getLong("created");
		ip = r.getString("ip");
		uid = r.getInt("uid");
		updated = r.getLong("updated");
		address = r.getString("address");
		key = r.getString("pubkey");
		locked = r.getInt("locked");
		sent = r.getLong("sent");
		received = r.getLong("received");

		this.set("key", key);
		this.set("clientid", clientid);
	}

	public void setKey(String key) {
		this.key = key;
		this.set("key", key);
	}

	transient User user;

	public User getUser() {
		if (user == null && uid > 0) {
			user = User.loadById(uid);
		}
		return user;
	}

	public long getSent() {
		return sent;
	}

	public long getReceived() {
		return received;
	}

	/**
	 * Process.
	 * 
	 * @param b
	 *            the b
	 */
	public void process(byte[] b) {
		lastio = System.currentTimeMillis();

		received += b.length;
		this.update(V.create("received", received));

		Command.process(b, this);
	}

	/**
	 * Close.
	 */
	public void close() {

		log.debug("close the client: " + id, new Exception());

		if (session != null) {
			session.removeAttribute("conn");
			session.close(true);
			session = null;
		}

		if (clientid != null) {
			update(clientid, V.create("uid", -1));
			clientid = null;

			TConnCenter.remove(this);

			if (resp != null) {
				resp.onDisconnected();
			}

			/**
			 * call on the waiter
			 */
			for (MyCallback m : callback.values()) {
				if (m != null && m.resp != null) {
					m.resp.onDisconnected();
				}
			}
		}
	}

	/**
	 * Send.
	 * 
	 * @param out
	 *            the out
	 */
	public void send(Response out) {
		send(out.getBytes(), out.isRequiredEncode());
	}

	/**
	 * Send.
	 * 
	 * @param b
	 *            the b
	 * @param requireEncode
	 *            the require encode
	 */
	public void send(byte[] b, boolean requireEncode) {
		// log.debug("send back:" + b.length);
		if (session != null && session.isConnected()) {
			try {
				Response resp = new Response();
				byte head = 0x40;

				if (requireEncode && (deskey != null) && this.isSupportEncode()) {
					b = DES.encode(b, deskey);
					head |= 0x20;
				}

				if ((b.length > 1024) && this.isSupportZip()) {
					b = Zip.zip(b);
					head |= 0x10;
				}

				resp.writeByte(head);
				resp.writeInt(b.length);
				resp.writeBytes(b);
				b = resp.getBytes();

				if (DEBUG) {
					log.debug(Bean.toString(b));
				}

				sent += b.length;
				this.update(V.create("sent", sent));

				IoBuffer buf = IoBuffer.allocate(b.length, false);
				buf.put(b);
				buf.flip();

				synchronized (session) {
					session.write(buf);
				}

				lastio = System.currentTimeMillis();

				session.resumeWrite();
			} catch (Exception e) {
				log.error(e.getMessage(), e);

				close();
			}
		} else {
			close();
		}
	}

	/**
	 * Send.
	 * 
	 * @param cmd
	 *            the cmd
	 * @param jo
	 *            the jo
	 * @param bb
	 *            the bb
	 * @param requireEncode
	 *            the require encode
	 */
	public void send(byte cmd, JSONObject jo, byte[] bb, boolean requireEncode) {
		log.debug("send:" + clientid + ", " + jo + ", bb:"
				+ (bb == null ? 0 : bb.length));

		Response resp = new Response(requireEncode);
		/**
		 * write the command
		 */
		resp.writeByte(cmd);

		/**
		 * write the json as string
		 */
		if (jo != null) {
			resp.writeString(jo.toString());
		}

		if (bb != null) {
			resp.writeInt(bb.length);
			resp.writeBytes(bb);
		}

		this.send(resp);

	}

	/**
	 * Valid.
	 * 
	 * @return true, if successful
	 */
	public boolean valid() {
		return valid;
	}

	public String getRemoteIp() {
		String remote = session.getRemoteAddress().toString();
		// /180.109.27.240:17524
		if (remote.startsWith("/")) {
			remote = remote.substring(1);
		}

		int i = remote.indexOf(":");
		return remote.substring(0, i);
	}

	/**
	 * Validate.
	 * 
	 * @param uid
	 *            the uid
	 * @return true, if successful
	 */
	public boolean validate(String uid) {
		valid = false;

		if (locked == 0 && this.password != null && this.password.equals(uid)) {
			if (Bean.update(
					"clientid=?",
					new Object[] { clientid },
					V.create("login", 1).set("logined",
							System.currentTimeMillis()), TConn.class) > 0) {
				valid = true;

				// TConn d = online.get(clientid);
				// if (d != null && d != this) {
				// log.warn("same [" + clientid + "], resync twice, 1:" + d
				// + ", 2:" + this);
				// d.close();
				// }
				//
				// online.put(clientid, this);
				// startSync();

			}
		}

		return valid;
	}

	/**
	 * Bye.
	 */
	public void bye() {
		send(Command.BYE, null, null, false);
	}

	/**
	 * Update.
	 * 
	 * @param v
	 *            the v
	 * @return true, if successful
	 */
	public boolean update(V v) {
		return Bean.update("clientid=?", new Object[] { clientid }, v,
				TConn.class) > 0;
	}

	// public static TConn online(String clientid) {
	// return online.get(clientid);
	// }

	// private static Map<String, TConn> online = new HashMap<String, TConn>();

	public static String ALLOW_IP = null;

	public static String ALLOW_USER = null;

	public static String ALLOW_UID = null;

	/**
	 * Find.
	 * 
	 * @param name
	 *            the name
	 * @return the beans
	 */
	public static Beans<TConn> find(String name) {
		name = "%" + name + "%";
		return Bean.load("clientid like ? or phone like ? or alias like ?",
				new Object[] { name, name, name }, "order by phone", 0, 100,
				TConn.class);
	}

	public long getUpdated() {
		return updated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder("TConn-[").append(clientid).append(",uid=")
				.append(uid).append(",alias=").append(alias)
				.append(", session=").append(session).append("]").toString();
	}

	/**
	 * Update.
	 * 
	 * @param clientid
	 *            the clientid
	 * @param v
	 *            the v
	 * @return true, if successful
	 */
	public static boolean update(String clientid, V v) {
		return Bean.update("clientid=?", new Object[] { clientid }, v,
				TConn.class) > 0;
	}

	/**
	 * Format phone.
	 * 
	 * @param phone
	 *            the phone
	 * @return the string
	 */
	public static String formatPhone(String phone) {
		if (phone == null)
			return null;

		return phone.replaceAll("[ -]", "").replaceFirst("(\\+86|12593)", "");
	}

	/**
	 * Creates the.
	 * 
	 * @param clientid
	 *            the clientid
	 * @param key
	 *            the key
	 * @param password
	 *            the password
	 */
	public void create(String clientid, String key, String password) {
		/**
		 * if exists save password in tblconn, remove it
		 */
		if (Bean.exists("password=?", new Object[] { password }, TConn.class)) {
			Bean.delete("password=?", new Object[] { password }, TConn.class);
		}

		Bean.insert(
				V.create("clientid", clientid).set("pubkey", key)
						.set("password", password)
						.set("capability", capability), TConn.class);
	}

	/**
	 * Exists.
	 * 
	 * @param clientid
	 *            the clientid
	 * @return true, if successful
	 */
	public static boolean exists(String clientid) {
		return Bean
				.exists("clientid=?", new Object[] { clientid }, TConn.class);
	}

	/**
	 * 
	 * @param host
	 * @param port
	 * @param resp
	 * @return
	 */
	public static TConn connectByTcp(String host, int port, IResponse resp) {
		return connectByTcp(host, port, resp, X.AMINUTE);
	}

	/**
	 * Connect by tcp.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @param resp
	 *            the resp
	 * @return the t conn
	 */
	public static TConn connectByTcp(String host, int port, IResponse resp,
			long timeout) {
		TConn c = MDCConnector.connectByTcp(host, port, timeout);
		if (c != null) {
			c.resp = resp;
			return c;
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
	 * @param resp
	 *            the resp
	 * @return the t conn
	 */
	public static TConn connectByUdp(String host, int port, IResponse resp) {
		TConn c = MDCConnector.connectByUdp(host, port);
		c.resp = resp;
		return c;
	}

	/**
	 * Activate.
	 * 
	 * @param uid
	 *            the uid
	 * @param key
	 *            the key
	 * @param resp
	 *            the resp
	 */
	public void activate(String uid, String key, IResponse resp) {
		this.resp = resp;

		JSONObject jo = new JSONObject();
		jo.put(X.UID, uid);
		jo.put(X.KEY, key);
		jo.put(X.CAPABILITY, capability);
		send(Command.ACTIVATE, jo, null, false);
	}

	/**
	 * Hello.
	 * 
	 * @param clientid
	 *            the clientid
	 * @param password
	 *            the password
	 * @param pubkey
	 *            the pubkey
	 * @param resp
	 *            the resp
	 */
	public void hello(String clientid, String password, String pubkey,
			IResponse resp) {
		this.resp = resp;

		JSONObject jo = new JSONObject();
		jo.put(X.CLIENTID, clientid);
		if (this.isSupportEncode()) {
			password = Base64.encode(RSA.encode(password.getBytes(), pubkey));
		}
		jo.put(X.UID, password);
		jo.put(X.CAPABILITY, capability);

		this.key = pubkey;
		this.clientid = clientid;
		this.password = password;

		send(Command.HELLO, jo, null, false);
	}

	/**
	 * Post.
	 * 
	 * @param uri
	 *            the uri
	 * @param seq
	 *            the seq
	 * @param jo
	 *            the jo
	 */
	public void post(String uri, long seq, JSONObject jo) {
		post(uri, seq, jo, null, null);
	}

	/**
	 * Post.
	 * 
	 * @param uri
	 *            the uri
	 * @param seq
	 *            the seq
	 * @param jo
	 *            the jo
	 * @param bb
	 *            the bb
	 * @param resp
	 *            the resp
	 */
	public void post(String uri, long seq, JSONObject jo, byte[] bb,
			IResponse resp) {
		post(uri, seq, jo, bb, -1, resp);
	}

	/**
	 * Post.
	 * 
	 * @param uri
	 *            the uri
	 * @param seq
	 *            the seq
	 * @param jo
	 *            the jo
	 * @param bb
	 *            the bb
	 * @param timeout
	 *            the timeout
	 * @param resp
	 *            the resp
	 */
	public void post(String uri, long seq, JSONObject jo, byte[] bb,
			long timeout, IResponse resp) {
		if (resp != null) {
			callback.put(seq, new MyCallback(resp, timeout));
		}

		JSONObject j = new JSONObject();
		j.put(X.URI, uri);
		j.put(X.SEQ, seq);
		j.put(X.PARAM, jo);
		send(Command.APP, j, bb, true);
	}

	/**
	 * Release.
	 */
	public static void release() {

	}

	public JSONObject getJSON() {
		JSONObject jo = new JSONObject();
		this.toJSON(jo);
		return jo;
	}

	public void setValid(boolean b) {
		valid = b;
	}

	/**
	 * Send.
	 * 
	 * @param jo
	 *            the jo
	 * @param bb
	 *            the bb
	 */
	public void send(JSONObject jo, byte[] bb) {
		send(jo.toString(), bb);
	}

	/**
	 * Send.
	 * 
	 * @param message
	 *            the message
	 * @param bb
	 *            the bb
	 */
	public void send(String message, byte[] bb) {
		Response resp = new Response();
		resp.writeByte(Command.APP);
		resp.writeString(message);
		if (bb != null) {
			resp.writeInt(bb.length);
			resp.writeBytes(bb);
		}

		this.send(resp);
	}

	private static Map<Long, MyCallback> callback = new HashMap<Long, MyCallback>();

	/**
	 * On response.
	 * 
	 * @param jo
	 *            the jo
	 * @param bb
	 *            the bb
	 */
	public static void onResponse(JSONObject jo, byte[] bb) {
		try {
			long seq = jo.has(X.SEQ) ? Bean.toLong(jo.get(X.SEQ)) : 0L;

			MyCallback r = callback.remove(seq);
			if (r != null && r.resp != null) {
				int state = jo.has(X.STATE) ? jo.getInt(X.STATE) : -1;
				r.resp.onResponse(state, seq,
						jo.has(X.RESULT) ? jo.getJSONObject(X.RESULT) : null,
						bb);
			}
		} catch (Exception e) {
			log.error(jo.toString(), e);
		}
	}

	static class MyCallback {
		long created = System.currentTimeMillis();
		IResponse resp;

		/**
		 * Instantiates a new my callback.
		 * 
		 * @param r
		 *            the r
		 * @param timeout
		 *            the timeout
		 */
		public MyCallback(IResponse r, long timeout) {
			resp = r;
			if (timeout > 0) {
				created = System.currentTimeMillis() - X.AMINUTE + timeout;
			}
		}

		/**
		 * Instantiates a new my callback.
		 * 
		 * @param r
		 *            the r
		 */
		public MyCallback(IResponse r) {
			resp = r;
		}
	}

	static WorkerTask timeroutChecker = new WorkerTask() {

		@Override
		public String getName() {
			return "mdc.timeout.checker";
		}

		@Override
		public void onExecute() {
			Long[] seqs = callback.keySet().toArray(new Long[callback.size()]);
			for (long seq : seqs) {
				MyCallback m = callback.get(seq);
				if (m != null
						&& System.currentTimeMillis() - m.created > X.AMINUTE) {
					callback.remove(seq);
					m.resp.onTimeout(seq);
				}
			}
		}

		@Override
		public void onFinish() {
			if (callback.size() > 0) {
				this.schedule(1000);
			}
		}
	};

	/**
	 * On activate.
	 * 
	 * @param state
	 *            the state
	 * @param clientid
	 *            the clientid
	 * @param pubkey
	 *            the pubkey
	 */
	public void onActivate(int state, String clientid, String pubkey) {

		if (resp != null) {
			resp.onActivate(state, clientid, pubkey);
		}
	}

	/**
	 * On hello.
	 * 
	 * @param state
	 *            the state
	 * @param deskey
	 *            the deskey
	 */
	public void onHello(int state, byte[] deskey) {
		this.deskey = deskey;
		if (resp != null) {
			resp.onHello(state, deskey);
		}
	}

	/**
	 * Load all.
	 * 
	 * @param uid
	 *            the uid
	 * @return the list
	 */
	public static List<TConn> loadAll(int uid) {
		return Bean.load("tblconn", null, "uid=? and uid>0",
				new Object[] { uid }, TConn.class);
	}

	/**
	 * Update.
	 */
	public void update() {
		long t = System.currentTimeMillis();
		if (updated < t - X.AMINUTE * 5) {
			this.update(V.create("updated", t));
		}
		updated = t;
	}

	/**
	 * Stat.
	 */
	public static void stat() {
		long time = Bean.toLong(Language.getLanguage().format(
				System.currentTimeMillis(), "yyyyMMddHHmm00"));

		Connection c = null;
		PreparedStatement stat = null;
		ResultSet r = null;

		try {
			c = Bean.getConnection();
			stat = c.prepareStatement("select count(*) t from tblconn where uid>0 ");

			r = stat.executeQuery();
			if (r.next()) {
				int count = r.getInt("t");
				Stat.insertOrUpdate("mdc", time, -1, count, "total");
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			Bean.close(r, stat, c);
		}
	}

}
