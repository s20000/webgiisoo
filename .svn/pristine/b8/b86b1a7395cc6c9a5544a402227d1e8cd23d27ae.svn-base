package com.giisoo.core.mq;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;

import com.giisoo.core.bean.X;
import com.giisoo.core.conf.Config;
import com.giisoo.core.mq.MQ.Receiver;
import com.giisoo.core.worker.WorkerTask;

public class MQTest2 implements IStub {

    public static void main(String[] args) {
        String home = "/home/joe/d/www/s1";

        System.out.println("home=" + home);

        System.setProperty("home", home);

        try {
            Config.init("home", "giisoo");

            Configuration conf = Config.getConfig();

            WorkerTask.init(conf.getInt("thread.number", 2000), conf);

            MQ.init(conf);

            MQTest2 t = new MQTest2();
            MQTest2 t1 = new MQTest2();
            MQTest2 t2 = new MQTest2();

            Receiver w = MQ.bind("tt2", t);// , MQ.Mode.TOPIC);
            System.out.println("w=" + w);
            w = MQ.bind("tt2", t1);// , MQ.Mode.TOPIC);
            w = MQ.bind("tt2", t2);// , MQ.Mode.TOPIC);

            int n = 10;
            JSONObject msg = new JSONObject();
            msg.put("ttt", "t");

            for (int i = 0; i < n; i++) {

                msg.put("i", i);
                MQ.send(i, "tt1", "to", msg, null, "ddd", "from", null);
            }

            Thread.sleep(X.AMINUTE);
            System.out.println("over");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int r = 0;

    @Override
    public void onRequest(long seq, String to, String from, String src, JSONObject header, JSONObject msg, byte[] attachment) {
        r++;
        if (r % 1000 == 0) {
            System.out.println("REQUEST: seq=" + seq + ", to=" + to + ", from=" + from + ", msg=" + msg + ", delay=" + (System.currentTimeMillis() - msg.getLong("time")) + " ms");
        }
    }

    @Override
    public void onResponse(long seq, String to, String from, String src,JSONObject header, JSONObject msg, byte[] attachment) {
        System.out.println("RESPONSE: seq=" + seq + ", to=" + to + ", from=" + from + ", msg=" + msg);
    }

}
