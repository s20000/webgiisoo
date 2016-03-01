/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.core.bean.*;
import com.giisoo.core.bean.Bean.V;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.core.mq.IStub;
import com.giisoo.core.mq.MQ;
import com.giisoo.core.mq.MQ.Receiver;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.Load;
import com.giisoo.framework.mdc.command.Command;

/**
 * 
 * The {@code TConnCenter} Class used to caches all the connection from the MDC
 * <p>
 * it handle the message from the MQ, and dispatch the message to remote
 * 
 * @author joe
 * 
 */
public class TConnCenter implements IStub {

    static Log log = LogFactory.getLog(TConnCenter.class);

    private static TConnCenter owner = new TConnCenter();

    private static String name = null;

    private static Map<Object, TConn> conns = new HashMap<Object, TConn>();

    static private Receiver receiver;

    /**
     * initialize the the tconncenter
     * 
     * @param conf
     *            the conf
     * @param port
     *            the port
     */
    public static void init(Configuration conf, int port) {
        MQ.init(conf);

        if (name == null) {
            try {
                name = SystemConfig.s("node", "default");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        if (receiver == null) {
            try {
                receiver = MQ.bind(name, owner);
            } catch (JMSException e) {
                log.error(e.getMessage(), e);
            }
        }

        new NOPTask().schedule(1000);

    }

    @Override
    public void onRequest(long seq, String to, String from, String src, JSONObject header, JSONObject msg, byte[] bb) {

        // String from = o.length > 1 ? (String) o[1] : null;
        // String src = o.length > 2 ? (String) o[2] : null;

        TConn c = conns.get(to);
        if (c != null) {
            /**
             * set the head in server side
             */
            if (header != null && header.size() > 0) {
                for (Object name : header.keySet()) {
                    c.set((String) name, header.get(name));
                }
            }

            /**
             * respond the data to remote
             */
            Response out = new Response();
            out.writeByte((byte) (Command.APP));
            out.writeString(msg.toString());
            if (bb != null) {
                out.writeInt(bb.length);
                out.writeBytes(bb);
            }
            c.send(out);

            log.debug("response: " + msg);

        }

    }

    @Override
    public void onResponse(long seq, String to, String from, String src, JSONObject header, JSONObject msg, byte[] bb) {

        // String from = o.length > 1 ? (String) o[1] : null;
        // String src = o.length > 2 ? (String) o[2] : null;

        TConn c = conns.get(to);
        if (c != null) {
            /**
             * set the head in server side
             */
            if (header != null && header.size() > 0) {
                for (Object name : header.keySet()) {
                    c.set((String) name, header.get(name));
                }
            }

            /**
             * respond the data to remote
             */
            Response out = new Response();
            out.writeByte((byte) (Command.APP | 0x80));
            out.writeString(msg.toString());
            if (bb != null) {
                out.writeInt(bb.length);
                out.writeBytes(bb);
            }
            c.send(out);

            log.debug("response: " + msg);

        }
    }

    public void run(int command, Object... o) {
        if (o != null && o.length > 0) {
            String to = o.length > 0 ? (String) o[0] : null;
            // String from = o.length > 1 ? (String) o[1] : null;
            // String src = o.length > 2 ? (String) o[2] : null;
            byte flag = o.length > 3 ? (Byte) (o[3]) : 0;
            JSONObject header = o.length > 4 ? (JSONObject) o[4] : null;
            JSONObject msg = o.length > 5 ? (JSONObject) o[5] : null;
            byte[] bb = o.length > 6 ? (byte[]) o[6] : null;

            TConn c = conns.get(to);
            if (c != null) {
                /**
                 * set the head in server side
                 */
                if (header != null && header.size() > 0) {
                    for (Object name : header.keySet()) {
                        c.set((String) name, header.get(name));
                    }
                }

                /**
                 * respond the data to remote
                 */
                Response out = new Response();
                if (flag > 0) {
                    out.writeByte((byte) (Command.APP | 0x80));
                } else {
                    out.writeByte((byte) (Command.APP));
                }
                out.writeString(msg.toString());
                if (bb != null) {
                    out.writeInt(bb.length);
                    out.writeBytes(bb);
                }
                c.send(out);

                log.debug("response: " + msg);

            }
        }
    }

    /**
     * Removes the connection by the object id
     * 
     * @param o
     *            the object
     * @return the t conn
     */
    public static TConn remove(Object o) {
        if (o instanceof TConn) {
            Object id = ((TConn) o).getId();
            TConn c = conns.get(id);
            if (c == o) {
                conns.remove(id);
            }

            return null;
        }

        return conns.remove(o);
    }

    /**
     * Gets the connection by the object id
     * 
     * @param id
     *            the id
     * @return the t conn
     */
    public static TConn get(Object id) {
        return conns.get(id);
    }

    /**
     * Adds the connection to center
     * 
     * @param c
     *            the c
     */
    public static void add(TConn c) {
        Object id = c.getId();
        if (id != null) {
            TConn m = conns.get(id);
            if (m != null && !m.equals(c)) {
                log.warn("same [" + id + "] hello twice, 1:" + m + ", 2:" + c);
                m.close();
            }

            c.update(V.create("address", getQueue()));
            conns.put(id, c);
        }
    }

    /**
     * get the message queue name
     * 
     * @return String
     */
    public static String getQueue() {
        return name;
    }

    /**
     * send NOP to remote to keep the connection alive
     * 
     * @author joe
     *
     */
    private static class NOPTask extends WorkerTask {

        private final static int INTERVAL = 10 * 1000;

        @Override
        public String getName() {
            return "mdc.nop";
        }

        @Override
        public void onExecute() {
            /**
             * update the load in database
             */
            Load.update("mdc", name, conns.size());

            /**
             * check the connections and send NOP
             */
            if (conns.size() > 0) {
                TConn[] cc = conns.values().toArray(new TConn[conns.size()]);

                log.info("nop.size=" + conns.size() + ", conns.key=" + conns.keySet() + ", connection.size=" + cc.length);
                for (TConn c : cc) {
                    if (System.currentTimeMillis() - c.lastio > X.AMINUTE * 3) {
                        c.send(Command.NOP, null, null, false);
                    }
                }
            }
        }

        @Override
        public void onFinish() {
            /**
             * schedule the task later
             */
            this.schedule(INTERVAL);
        }

    }
}
