package com.giisoo.core.message;

import java.util.HashMap;
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

import com.giisoo.core.bean.X;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.mdc.Request;
import com.giisoo.framework.mdc.Response;

public final class MQ {

    private static String group = X.EMPTY;

    private static Log log = LogFactory.getLog(MQ.class);

    enum Mode {
        TOPIC, QUEUE
    };

    public static final int REQUEST = 1;
    public static final int RESPONSE = 2;

    private static Connection connection;
    private static Session session;
    private static boolean enabled = false;
    private static String url;
    private static String user;
    private static String password;
    private static ActiveMQConnectionFactory factory;

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

                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return enabled && session != null;
    }

    private MQ() {
    }

    public static boolean init(Configuration conf) {
        if (session != null)
            return true;

        enabled = "true".equals(conf.getString("mq.enabled", "false"));

        if (enabled) {
            url = conf.getString("mq.url", ActiveMQConnection.DEFAULT_BROKER_URL);
            user = conf.getString("mq.user", ActiveMQConnection.DEFAULT_USER);
            password = conf.getString("mq.password", ActiveMQConnection.DEFAULT_PASSWORD);

            group = conf.getString("mq.group", X.EMPTY);
            if (!X.EMPTY.equals(group) && !group.endsWith(".")) {
                group += ".";
            }
        }

        return check();

    }

    /**
     * listen on the name
     * 
     * @param name
     * @param stub
     */
    public static WorkerTask listen(String name, IStub stub, Mode mode) {
        if (mode == Mode.TOPIC) {
            if (enabled) {
                TopicTask r = new TopicTask(name, stub);
                r.schedule(10);
                return r;
            }
        } else if (mode == Mode.QUEUE) {
            QueueTask r = new QueueTask(name, stub);
            r.schedule(10);
            return r;
        }

        return null;
    }

    public static void listen(String name, IStub stub) {
        listen(name, stub, Mode.QUEUE);
    }

    /**
     * QueueTask
     * 
     * @author joe
     * 
     */
    private static class QueueTask extends WorkerTask {
        String name;
        IStub cb;
        MessageConsumer consumer;
        int interval = 0;

        public QueueTask(String name, IStub cb) {
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

    private static void process(Request req, IStub cb) {
        int command = req.readInt();
        String to = req.readString();
        String from = req.readString();
        String src = req.readString();
        byte flag = req.readByte();
        String header = req.readString();
        String message = req.readString();
        int len = req.readInt();
        byte[] bb = req.readBytes(len);

        log.debug("got a message:" + src + ", " + message);

        if (command == MQ.REQUEST) {
            cb.onRequest(to, from, src, flag, JSONObject.fromObject(header), JSONObject.fromObject(message), bb);
        } else {
            cb.onResponse(to, from, src, flag, JSONObject.fromObject(header), JSONObject.fromObject(message), bb);
        }

    }

    private static class TopicTask extends WorkerTask {
        String name;
        IStub cb;
        MessageConsumer consumer;
        int interval = 0;

        public TopicTask(String name, IStub cb) {
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

    public static int broadcast(String dest, String to, String message, byte[] bb, String src, String from, String header) {
        if (message == null)
            return -1;

        if (!check()) {
            return -1;
        }

        try {

            /**
             * get the message producer by destination name
             */
            MessageProducer p = getTopic(dest);
            if (p != null) {
                BytesMessage m = session.createBytesMessage();

                Response resp = new Response();
                resp.writeInt(MQ.REQUEST);
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

    public static int call(String dest, String to, String message, byte[] bb, String src, String from, String header) {
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
                resp.writeInt(MQ.REQUEST);
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

    public static int response(String dest, String to, String message, byte[] bb, String src, String from, String header) {
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
                resp.writeInt(MQ.RESPONSE);
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
     * queue producer cache
     */
    private static Map<String, MessageProducer> queues = new HashMap<String, MessageProducer>();

    /**
     * @deprecated topic producer cache
     */
    private static Map<String, MessageProducer> topics = new HashMap<String, MessageProducer>();

}
