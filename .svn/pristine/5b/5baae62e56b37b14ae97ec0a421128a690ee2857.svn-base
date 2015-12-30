package com.giisoo.core.rpc;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.*;
import org.apache.mina.transport.socket.SocketSessionConfig;

// TODO: Auto-generated Javadoc
/**
 * @deprecated The Class Stub.
 */
public class Stub {

    /** The log. */
    static Log log = LogFactory.getLog(Stub.class);

    /** The Constant NULL. */
    public static final Byte NULL = 0;

    /** The in. */
    protected DataInputStream in;

    /** The out. */
    protected DataOutputStream out;

    /** The expired. */
    protected static long EXPIRED = 60 * 1000;

    /** The Constant MAX_SIZE. */
    protected static final int MAX_SIZE = 1024 * 1024 * 10;

    /** The zip. */
    public static boolean zip = true;

    /** The socket. */
    protected Socket socket;

    /** The last. */
    private long last = 0;

    /** The available. */
    private boolean available = false;

    /** The attachments. */
    private TreeMap<String, Object> attachments = new TreeMap<String, Object>();

    /** The _test integer. */
    private static Integer _testInteger = new Integer(1);

    /** The local. */
    private static ThreadLocal<WeakReference<Stub>> local = new ThreadLocal<WeakReference<Stub>>();

    /** The session. */
    private IoSession session = null;

    /** The incoming. */
    private List<Object> incoming = null;

    /**
     * get a object which attached from the stub.
     *
     * @param key
     *            the key
     * @return the object
     */
    public Object attachment(String key) {
        return attachments.get(key);
    }

    /**
     * attach a object for [key] on the stub.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public void attachment(String key, Object value) {
        attachments.put(key, value);
    }

    /**
     * get the remote host address.
     *
     * @return the host
     */
    public String getHost() {
        if (socket != null) {
            return socket.getInetAddress().getHostAddress();
        } else if (session != null) {
            SocketAddress saddr = session.getRemoteAddress();
            if (saddr instanceof InetSocketAddress) {
                InetSocketAddress isa = (InetSocketAddress) saddr;
                return isa.getAddress().getHostAddress();
            }
            return session.getRemoteAddress().toString();
        }

        return null;
    }

    /**
     * Sets the.
     *
     * @param stub
     *            the stub
     */
    public static void set(Stub stub) {
        local.set(new WeakReference<Stub>(stub));
    }

    /**
     * Gets the.
     *
     * @return the stub
     */
    public static Stub get() {
        WeakReference<Stub> w = local.get();
        if (w != null) {
            return w.get();
        }

        return null;
    }

    /**
     * Test.
     *
     * @return true, if successful
     */
    protected boolean test() {
        try {
            if (last < System.currentTimeMillis() - EXPIRED) {
                Object o = this.call("echo", _testInteger);
                return _testInteger.equals(o);
            } else {
                return true;
            }
        } catch (Throwable e) {
            available = false;
        }

        return false;
    }

    /**
     * Read.
     *
     * @return the object
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected Object read() throws IOException {
        if (in != null) {
            try {
                int flag = in.readInt();
                int len = in.readInt();
                if (len > MAX_SIZE) {
                    throw new IOException("error package size, exceed max, size=" + len);
                }
                byte[] b = new byte[len];
                // log.info("read:"+len+","+flag+" from "+socket.getLocalSocketAddress());
                in.readFully(b);

                Object o = null;
                if ((flag & 0x01) == 1) {
                    /**
                     * the data is compressed
                     */
                    ByteArrayInputStream bis = new ByteArrayInputStream(b);
                    GZIPInputStream zis = new GZIPInputStream(bis);
                    ObjectInputStream ois = new ObjectInputStream(zis);
                    o = ois.readObject();// readUnshared();

                    ois.close();
                    bis.close();
                } else {
                    /**
                     * the data is not compressed
                     */
                    ByteArrayInputStream bis = new ByteArrayInputStream(b);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    o = ois.readObject();// readUnshared();

                    ois.close();
                    bis.close();
                }

                last = System.currentTimeMillis();
                if (o instanceof Byte && NULL.equals(o)) {
                    return null;
                }
                return o;

            } catch (Exception e) {
                available = false;
                log.error("read from stub fail.", e);
                throw new IOException(e);
            }
        } else if (incoming != null) {
            synchronized (incoming) {
                try {
                    while (incoming.size() == 0) {
                        incoming.wait();
                    }
                    return incoming.remove(0);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    ;
                }
            }
        }

        throw new IOException("no avail input stream");
    }

    /**
     * Write.
     *
     * @param o
     *            the o
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void write(Object o) throws IOException {
        if (o == null) {
            o = NULL;
        }

        try {
            int flag = 0x01 << 28;
            byte[] b = null;
            if (zip) {
                flag |= 0x01;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                GZIPOutputStream zos = new GZIPOutputStream(bos);
                ObjectOutputStream oos = new ObjectOutputStream(zos);
                oos.writeObject(o);// .writeUnshared(o);// writeObject(o);
                oos.flush();
                oos.close();
                bos.close();
                b = bos.toByteArray();
            } else {
                // flag &= 0xFFFFFFFE;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(o);
                oos.flush();
                oos.close();
                bos.close();
                b = bos.toByteArray();
            }

            if (session != null) {
                IoBuffer buf = IoBuffer.allocate(b.length + 8, false);
                buf.putInt(flag);
                buf.putInt(b.length);
                buf.put(b);
                buf.flip();
                session.write(buf);

            } else if (out != null) {
                out.writeInt(flag);
                out.writeInt(b.length);
                out.write(b);
                out.flush();
            }

            last = System.currentTimeMillis();
        } catch (IOException e) {
            available = false;
            throw e;
        }
    }

    /**
     * Instantiates a new stub.
     */
    protected Stub() {
    }

    /**
     * Instantiates a new stub.
     *
     * @param session
     *            the session
     */
    protected Stub(IoSession session) {
        this.session = session;
        IoSessionConfig isc = session.getService().getSessionConfig();
        if (isc instanceof SocketSessionConfig) {
            SocketSessionConfig ssc = ((SocketSessionConfig) isc);
            ssc.setReuseAddress(true);
            ssc.setSoLinger(0);
        }
        incoming = new ArrayList<Object>();
    }

    /**
     * Instantiates a new stub.
     *
     * @param sock
     *            the sock
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected Stub(Socket sock) throws IOException {
        socket = sock;
        socket.setReuseAddress(true);
        socket.setSoTimeout(0);

        out = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
        in = new DataInputStream(sock.getInputStream());

        available = true;
        last = System.currentTimeMillis();
    }

    /**
     * create a stub on [host]:[port].
     *
     * @param host
     *            the host
     * @param port
     *            the port
     * @return the stub
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static Stub create(String host, int port) throws IOException {
        if (host == null) {
            return create(port);
        } else {
            return new ServerStub3(host, port).start();
        }
    }

    /**
     * create a stub on [port].
     *
     * @param port
     *            the port
     * @return the stub
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static Stub create(int port) throws IOException {
        // ServerSocket ss = new ServerSocket(port);
        // return new ServerStub(ss).start();

        return new ServerStub3(port).start();
    }

    /**
     * Check.
     *
     * @return true, if successful
     */
    public boolean check() {
        try {
            Object o = call("echo", _testInteger);
            return _testInteger.equals(o);
        } catch (Throwable e) {
            available = false;
            log.error(e.getMessage(), e);
        }

        return false;
    }

    /**
     * Available.
     *
     * @param a
     *            the a
     */
    public void available(boolean a) {
        available = a;
    }

    /**
     * is available ?.
     *
     * @return true, if successful
     */
    public boolean available() {
        return available;
    }

    /**
     * url: protocol://host:port, please refer connect(url, timeout).
     *
     * @param url
     *            the url
     * @return the stub
     * @throws Exception
     *             the exception
     * @deprecated
     */
    public static Stub connect(String url) throws Exception {
        return connect(url, 60);
    }

    /**
     * Connect.
     *
     * @param url
     *            the url
     * @param timeout
     *            the timeout
     * @return the stub
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static Stub connect(String url, int timeout) throws IOException {
        if (url == null)
            throw new IOException("the url is null!");

        // try {
        String[] ss = url.split(":");

        if (ss.length > 2) {
            String host = ss[1];
            if (host.startsWith("//")) {
                host = host.substring(2);
            }
            int port = Integer.parseInt(ss[2]);
            InetSocketAddress sadd = new InetSocketAddress(host, port);
            log.info("connectting to " + url);
            Socket sock = new Socket();
            sock.connect(sadd, 10000);

            // SocketChannel socketChannel = SocketChannel.open();
            // // socketChannel.configureBlocking(false);
            // socketChannel.connect(sadd);
            // int trys = 0;
            // while (!socketChannel.finishConnect() && trys<10) {
            // Thread.sleep(1000L);
            // trys++;
            // }
            // Stub stub = new Stub(socketChannel.socket());
            Stub stub = new Stub(sock);

            return stub;
        } else {
            // } catch (Throwable e) {
            // log.error("can't connect to " + url, e);
            // }
            throw new IOException("the url is wrong: [" + url + "]");
        }
    }

    /**
     * Release.
     */
    public void release() {
        Object o = this.attachment("pool");
        if (o != null && o instanceof StubPool) {
            ((StubPool) o).release(this);

            if (!available) {
                close();
            }
        } else {
            close();
        }
    }

    /** The is closed. */
    private boolean isClosed = false;

    /**
     * close the stub.
     */
    protected void close() {
        if (isClosed) {
            return;
        }
        isClosed = true;
        available = false;

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                log.error("close inputstream error", e);
            }
            in = null;
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                log.error("close outputstream error", e);
            }
            out = null;
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("close socket errot", e);
            }

            socket = null;
        }
    }

    /**
     * clean up the stub.
     */
    public void cleanup() {
        try {
            if (session != null) {
                incoming.clear();
            } else {
                int len = in.available();
                if (len > 0) {
                    byte b[] = new byte[len];
                    in.read(b);
                }
            }
        } catch (Throwable e) {
            available = false;
        }
    }

    /**
     * remote call.
     *
     * @param method
     *            the method
     * @param param
     *            the param
     * @return the object
     * @throws Exception
     *             the exception
     */
    public Object call(String method, Object... param) throws Exception {
        try {
            Command c = new Command();
            c.cmd = method;
            c.params = param;

            cleanup();
            write(c);
            return read();

        } catch (Throwable e) {
            available = false;
            log.error("call to server error, method:" + method, e);
            close();
            throw new Exception(e);
        }
    }

    /**
     * bind a remote interface in the stub.
     *
     * @param caller
     *            the caller
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void bind(IRemote caller) throws IOException {
        throw new IOException("@not support");
    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     * @throws Exception
     *             the exception
     */
    public static void main(String args[]) throws Exception {
        connect("cloud://127.0.0.1:1122", 100000);
    }

    /**
     * Process.
     *
     * @param flag
     *            the flag
     * @param b
     *            the b
     * @return the object
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected Object process(int flag, byte[] b) throws IOException {
        // log.debug(flag + "," + b);

        Object o = null;
        try {
            if ((flag & 0x01) == 1) {
                /**
                 * the data is compressed
                 */
                ByteArrayInputStream bis = new ByteArrayInputStream(b);
                GZIPInputStream zis = new GZIPInputStream(bis);
                ObjectInputStream ois = new ObjectInputStream(zis);
                o = ois.readObject();

                ois.close();
                bis.close();
            } else {
                /**
                 * the data is not compressed
                 */
                ByteArrayInputStream bis = new ByteArrayInputStream(b);
                ObjectInputStream ois = new ObjectInputStream(bis);
                o = ois.readObject();// readUnshared();

                ois.close();
                bis.close();
            }
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            available = false;
        }

        last = System.currentTimeMillis();
        if (o instanceof Byte && NULL.equals(o)) {
            return null;
        }
        return o;
    }

    /**
     * Stop.
     */
    public void stop() {

    }

    /**
     * Valid.
     *
     * @param flag
     *            the flag
     * @return true, if successful
     */
    protected boolean valid(int flag) {
        int f = (flag >> 28) & 0x0F;
        if (f == 0x01) {
            return true;
        }

        return false;
    }
}
