/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.*;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.*;
import org.apache.mina.core.session.*;

import com.giisoo.core.bean.UID;
import com.giisoo.core.conf.*;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.Cluster.Counter;
import com.giisoo.framework.mdc.command.*;
import com.giisoo.utils.base.*;
import com.giisoo.utils.base.RSA.Key;

/**
 * MDC server
 * 
 * @author yjiang
 * 
 */
public abstract class MDCServer extends IoHandlerAdapter {

    final static Log log = LogFactory.getLog(MDCServer.class);

    static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * the the max size of a packet, 32KB
     */
    static int MAX_SIZE = 10240 * 1024; // test

    protected InetSocketAddress address;
    protected Selector selector;
    protected ServerSocketChannel server;
    protected final int PROCESS_NUMBER = 4;
    protected static Configuration _conf;

    protected IoAcceptor acceptor;
    protected boolean isRunning = false;

    protected boolean testKey() {
        String data = UID.random(24);
        byte[] bb = RSA.encode(data.getBytes(), TConn.pub_key);
        if (bb != null) {
            bb = RSA.decode(bb, TConn.pri_key);
            if (bb != null && data.equals(new String(bb))) {
                return true;
            }
        }

        return false;
    }

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
     * Instantiates a new MDC server.
     * 
     * @param host
     *            the host
     * @param port
     *            the port
     */
    protected MDCServer(String host, int port) {
        _conf = Config.getConfig();

        address = (host == null) ? new InetSocketAddress(port) : new InetSocketAddress(host, port);

        /**
         * initialize app command
         */
        Command.init();

        /**
         * initialize the connection center
         */
        TConnCenter.init(_conf, port);

        synchronized (_conf) {
            /**
             * load public key from database
             */
            TConn.pub_key = SystemConfig.s("pub_key", null);
            TConn.pri_key = SystemConfig.s("pri_key", null);

            /**
             * initialize the RSA key, hardcode 2048 bits
             */
            if (TConn.pub_key == null || TConn.pri_key == null || "".equals(TConn.pub_key) || "".equals(TConn.pri_key)) {
                /**
                 * print out the old state
                 */
                log.warn("the pub_key or pri_key missed, the old state are pub_key:[" + TConn.pub_key + "], pri_key:[" + TConn.pri_key + "]");

                Key k = RSA.generate(2048);
                TConn.pri_key = k.pri_key;
                TConn.pub_key = k.pub_key;

                /**
                 * print out the new public key
                 */
                log.warn("create new RSA key pair, pub_key:[" + TConn.pub_key + ']');

                /**
                 * set back in database
                 */
                SystemConfig.setConfig("pri_key", TConn.pri_key);
                SystemConfig.setConfig("pub_key", TConn.pub_key);

            }

            MAX_SIZE = SystemConfig.i("mdc.max_size", MAX_SIZE);
        }
    }

    /**
     * Start.
     * 
     * @return the MDC server
     */
    public abstract MDCServer start();

    /**
     * Stop.
     */
    public void stop() {
        acceptor.unbind();
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
                    log.info("flag is not correct! flag:" + head + ",from: " + session.getRemoteAddress());

                    session.write("error.head");
                    session.close(true);
                    return;
                }

                int len = in.getInt();

                if (len <= 0 || len > MAX_SIZE) {
                    log.error("mdcserver.Wrong lendth: " + len + "/" + MAX_SIZE + " - " + session.getRemoteAddress());
                    session.write("error.packet.size");
                    session.close(true);
                    break;
                }

                // log.info("packet.len:" + len + ", len in buffer:" +
                // in.length());
                if (in.length() < len) {
                    in.reset();
                    break;
                } else {
                    // do it

                    byte[] b = new byte[len];
                    in.read(b);

                    // log.info("stub.package.size: " + len + ", head:" + head +
                    // ", cmd:" + Bean.toString(b));
                    // log.info("stub.package.size: " + len + ", head:" + head);

                    /**
                     * test the zip flag
                     */
                    if ((head & 0x10) != 0) {
                        b = Zip.unzip(b);
                    }

                    final TConn d = (TConn) session.getAttribute("conn");
                    if (d != null) {
                        /**
                         * test the encrypted flag
                         */
                        if ((head & 0x20) != 0) {
                            b = DES.decode(b, d.deskey);
                        }

                        final byte[] bb = b;

                        /**
                         * test if the packet is for mdc or app
                         */
                        new WorkerTask() {

                            @Override
                            public void onExecute() {
                                d.process(bb);
                            }

                        }.schedule(0);

                        session.setAttribute("last", System.currentTimeMillis());
                    } else {
                        session.write("error.getconnection");

                        log.error("error to get connection: " + session.getRemoteAddress());
                        session.close(true);
                    }
                }
            }
        } catch (Throwable e) {
            log.error("closing stub: " + session.getRemoteAddress(), e);
            session.write("exception." + e.getMessage());
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
        log.info("stub created:" + session.getRemoteAddress());

        Counter.add("mdc", "connection", 1);

        TConn d = new TConn(session);
        d.set("x-forwarded-for", session.getRemoteAddress().toString());

        session.setAttribute("conn", d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.mina.core.service.IoHandlerAdapter#sessionClosed(org.apache
     * .mina.core.session.IoSession)
     */
    public void sessionClosed(IoSession session) throws Exception {
        log.info("closed stub: " + session.getRemoteAddress());
        TConn d = (TConn) session.getAttribute("conn");
        if (d != null) {
            d.close();

            session.removeAttribute("conn");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.mina.core.service.IoHandlerAdapter#sessionIdle(org.apache.
     * mina.core.session.IoSession, org.apache.mina.core.session.IdleStatus)
     */
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
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
    public void messageReceived(IoSession session, Object message) throws Exception {
        // System.out.println(message);
        if (message instanceof IoBuffer) {
            service((IoBuffer) message, session);
        }

    }

    /**
     * Creates the tcp server.
     * 
     * @param host
     *            the host
     * @param port
     *            the port
     * @return the MDC server
     */
    public synchronized static MDCServer createTcpServer(String host, int port) {
        return new TDCServer(host, port);
    }

    /**
     * Creates the udp server.
     * 
     * @param host
     *            the host
     * @param port
     *            the port
     * @return the MDC server
     */
    public synchronized static MDCServer createUdpServer(String host, int port) {
        return new UDCServer(host, port);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.mina.core.service.IoHandlerAdapter#exceptionCaught(org.apache
     * .mina.core.session.IoSession, java.lang.Throwable)
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        TConn d = (TConn) session.getAttribute("conn");
        if (d != null && d.valid()) {
            App.bye(d);
        }

    }

}
