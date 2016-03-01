/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc;

import java.util.*;

import net.sf.json.JSONObject;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.giisoo.core.bean.*;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.User;
import com.giisoo.framework.mdc.command.*;
import com.giisoo.utils.base.*;
import com.giisoo.utils.base.Base64;
import com.mongodb.BasicDBObject;

/**
 * MDC device which interact with mdc client in server side
 * 
 * @author yjiang
 * 
 */
@DBMapping(collection = "gi_conn")
public class TConn extends Bean {

    private static final long serialVersionUID = 1L;

    public static final int STATE_NEW = 0;
    public static final int STATE_ACTIVATED = 1;
    public static final int STATE_HELLOED = 2;
    public static final int STATE_LOGINED = 3;
    public static final int STATE_DISCONNECTED = 4;
    public static final int STATE_ERROR = 5;
    public static final int STATE_PENDING = 6;

    /**
     * test is connected
     * 
     * @return boolean, true if connected
     */
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
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId()))
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

    //
    // private Object id;
    // private String clientid;
    // String phone;
    // String alias;
    // String password;
    // String address;
    // String key;
    //
    // int locked;
    // long uid;
    // long sent;
    // long received;
    //
    // long created;
    // String ip;
    // long updated;
    //
    transient long lastio = 0;

    boolean valid = false;

    transient IoSession session;
    public byte[] deskey;

    transient private int capability = 0x30; // 0x30 (encode, zip)

    //
    private IResponse resp;

    /**
     * get the "object id", if not logined, the id=clientid, if logined, the
     * id=userid
     * 
     * @return
     */
    public Object getId() {
        return this.get("id");
    }

    /**
     * set the object id
     * 
     * @param id
     */
    public void setId(Object id) {
        this.set("id", id);
    }

    /**
     * set the client id
     * 
     * @param clientid
     */
    public void setClientid(String clientid) {
        this.set("clientid", clientid);
    }

    /**
     * get the capability
     * 
     * @return int
     */
    public int getCapability() {
        return capability;// getInt("capability");
    }

    /**
     * set the capability for the connection
     * 
     * @param capability
     */
    public void setCapability(int capability) {
        this.capability = capability;
        this.set("capability", capability);
    }

    /**
     * test is support zip
     * 
     * @return boolean
     */
    public boolean isSupportZip() {
        return (capability & 0x10) != 0;
    }

    /**
     * test is support encode, "RSA/3DES"
     * 
     * @return boolean
     */
    public boolean isSupportEncode() {
        return (capability & 0x20) != 0;
    }

    /**
     * get the clientid
     * 
     * @return String
     */
    public String getClientid() {
        return getString("clientid");
    }

    /**
     * is locked
     * 
     * @return int, if locked, return 1
     */
    public int getLocked() {
        return getInt("locked");
    }

    /**
     * get the created time
     * 
     * @return long
     */
    public long getCreated() {
        return getLong("created");
    }

    /**
     * get the ip of last time connected
     * 
     * @return String
     */
    public String getIp() {
        return getString("ip");
    }

    /**
     * get the 3DES key
     * 
     * @return byte[]
     */
    public byte[] getDeskey() {
        return deskey;
    }

    public String getKey() {
        return getString("key");
    }

    /**
     * get the "password", or "uuid"
     * 
     * @return String
     */
    public String getPassword() {
        return getString("password");
    }

    /**
     * get the clientid
     * 
     * @return String
     */
    public String getClientId() {
        return getString("clientid");
    }

    /**
     * get the remote address
     * 
     * @return String
     */
    public String getAddress() {
        return getString("address");
    }

    /**
     * get the phone
     * 
     * @return String
     */
    public String getPhone() {
        return getString("phone");
    }

    /**
     * get the alias
     * 
     * @return String
     */
    public String getAlias() {
        return getString("alias");
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
     * Load the Conn by the query
     * 
     * @param q
     *            the query of the condition
     * @param s
     *            the start number
     * @param n
     *            the number
     * @return Beans<TConn>
     */
    public static Beans<TConn> load(BasicDBObject q, int s, int n) {
        return Bean.load(q, new BasicDBObject("clientid", 1), s, n, TConn.class);
    }

    /**
     * set the RSA public key for the client
     * 
     * @param key
     */
    public void setKey(String key) {
        this.set("key", key);
    }

    transient User user;

    /**
     * get the associated user with the connection
     * 
     * @return User
     */
    public User getUser() {
        if (user == null && this.getLong("uid") > 0) {
            user = User.loadById(this.getLong("uid"));
        }
        return user;
    }

    /**
     * get the "sent" bytes
     * 
     * @return long
     */
    public long getSent() {
        return this.getLong("sent");
    }

    /**
     * get the "received" bytes
     * 
     * @return long
     */
    public long getReceived() {
        return this.getLong("received");
    }

    /**
     * Process the received data
     * 
     * @param b
     *            the received data
     */
    public synchronized void process(byte[] b) {
        lastio = System.currentTimeMillis();

        long received = this.getReceived() + b.length;
        this.set("received", received);

        this.update(V.create("received", received));

        Command.process(b, this);
    }

    /**
     * Close the connection
     */
    public void close() {

        log.debug("close the client: " + getId(), new Exception());

        if (session != null) {
            session.removeAttribute("conn");
            session.close(true);
            session = null;
        }

        if (getClientid() != null) {
            update(getClientid(), V.create("uid", -1));
            set("clientid", null);

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
     * Send the response data to remote
     * 
     * @param out
     *            the response data
     */
    public void send(Response out) {
        send(out.getBytes(), out.isRequiredEncode());
    }

    /**
     * Send the bytes to remote
     * 
     * @param bb
     * @param requireEncode
     *            the require encode
     */
    public void send(final byte[] bb, final boolean requireEncode) {
        // log.debug("send back:" + b.length);
        if (session != null && session.isConnected()) {
            new WorkerTask() {

                @Override
                public void onExecute() {
                    try {
                        Response resp = new Response();
                        byte head = 0x40;
                        byte[] b = bb;

                        if (requireEncode && (deskey != null) && isSupportEncode()) {
                            b = DES.encode(b, deskey);
                            head |= 0x20;
                        }

                        if ((b.length > 1024) && isSupportZip()) {
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

                        long sent = getSent() + b.length;
                        update(V.create("sent", sent));

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
                }

            }.schedule(0);
        } else {
            close();
        }
    }

    /**
     * Send the command, json, bytes to remote
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
        log.debug("send:" + getClientid() + ", " + jo + ", bb:" + (bb == null ? 0 : bb.length));

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
     * test is valid connection
     * 
     * @return true, if valid
     */
    public boolean valid() {
        return valid;
    }

    /**
     * get the remote ip address
     * 
     * @return String
     */
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
        int locked = this.getInt("locked");
        String password = this.getPassword();

        if (locked == 0 && password != null && password.equals(uid)) {
            if (Bean.updateCollection(new BasicDBObject("clientid", this.getClientid()), V.create("login", 1).set("logined", System.currentTimeMillis()), TConn.class) > 0) {
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
     * send Bye to remote
     */
    public void bye() {
        send(Command.BYE, null, null, false);
    }

    /**
     * Update the connection info in database
     * 
     * @param v
     *            the values
     * @return true, if successful
     */
    public boolean update(V v) {
        return Bean.updateCollection(new BasicDBObject("clientid", this.getClientid()), v, TConn.class) > 0;
    }

    public static String ALLOW_IP = null;

    public static String ALLOW_USER = null;

    public static String ALLOW_UID = null;

    /**
     * get the updated time
     * 
     * @return long
     */
    public long getUpdated() {
        return this.getLong("updated");
    }

    /**
     * Update the connection by the clientid, and values
     * 
     * @param clientid
     *            the clientid
     * @param v
     *            the values
     * @return true, if successful
     */
    public static boolean update(String clientid, V v) {
        return Bean.updateCollection(new BasicDBObject("clientid", clientid), v, TConn.class) > 0;
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

        Bean.insertCollection(V.create(X._ID, clientid).set("clientid", clientid).set("pubkey", key).set("password", password).set("capability", getCapability()), TConn.class);
    }

    /**
     * test is exists for the clientid
     * 
     * @param clientid
     *            the clientid
     * @return true, if successful
     */
    public static boolean exists(String clientid) {
        return Bean.exists(new BasicDBObject("clientid", clientid), TConn.class);
    }

    /**
     * connect to the remote
     * 
     * @param host
     * @param port
     * @param resp
     * @return TConn
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
    public static TConn connectByTcp(String host, int port, IResponse resp, long timeout) {
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
    public void hello(String clientid, String password, String pubkey, IResponse resp) {
        this.resp = resp;

        JSONObject jo = new JSONObject();
        jo.put(X.CLIENTID, clientid);
        if (this.isSupportEncode()) {
            password = Base64.encode(RSA.encode(password.getBytes(), pubkey));
        }
        jo.put(X.UID, password);
        jo.put(X.CAPABILITY, capability);

        this.set("key", pubkey);
        this.set("clientid", clientid);
        this.set("password", password);

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
    public void post(String uri, long seq, JSONObject jo, byte[] bb, IResponse resp) {
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
    public void post(String uri, long seq, JSONObject jo, byte[] bb, long timeout, IResponse resp) {
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

    public void setValid(boolean b) {
        valid = b;
    }

    /**
     * Send the json and bytes to remote
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
     * Send the string message and bytes to remote
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
                r.resp.onResponse(state, seq, jo.has(X.RESULT) ? jo.getJSONObject(X.RESULT) : null, bb);
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
                if (m != null && System.currentTimeMillis() - m.created > X.AMINUTE) {
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
     * @return List of connection
     */
    public static List<TConn> loadAll(long uid) {
        return Bean.load("tblconn", null, "uid=? and uid>0", new Object[] { uid }, TConn.class);
    }

    /**
     * Update.
     */
    public void update() {
        long t = System.currentTimeMillis();
        if (this.getLong("updated") < t - X.AMINUTE * 5) {
            this.update(V.create("updated", t));
        }
        set("updated", t);
    }

}
