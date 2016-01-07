package com.giisoo.core.mq;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;

import com.giisoo.core.bean.TimeStamp;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.Config;
import com.giisoo.core.mq.MQ.Receiver;
import com.giisoo.core.worker.WorkerTask;

public class MQTest1 implements IStub {

    public static void main(String[] args) {
        String home = "/home/joe/d/www/s1";

        System.out.println("home=" + home);

        System.setProperty("home", home);

        try {
            Config.init("home", "giisoo");

            Configuration conf = Config.getConfig();

            WorkerTask.init(conf.getInt("thread.number", 20), conf);

            MQ.init(conf);

            MQTest1 t = new MQTest1();

            Receiver w = MQ.bind("tt1", t);// , MQ.Mode.TOPIC);
            System.out.println("w=" + w);

            int n = 100000000;
            JSONObject msg = new JSONObject();
            msg.put("ttt", "t");

            TimeStamp t1 = TimeStamp.create();
            for (int i = 0; i < n; i++) {
                msg.put("i", i);
                msg.put("time", System.currentTimeMillis());
                MQ.send(i, "tt2", "to", msg, null, "ddd", "from", null);
                if (i % 10000 == 0) {
                    System.out.println("send 10000, cost: " + t1.past() + "ms");
                    Thread.sleep(1000);
                    t1.reset();
                }
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
            System.out.println("REQUEST: seq=" + seq + ", to=" + to + ", from=" + from + ", msg=" + msg);
        }
    }

    @Override
    public void onResponse(long seq, String to, String from, String src, JSONObject header, JSONObject msg, byte[] attachment) {
        System.out.println("RESPONSE: seq=" + seq + ", to=" + to + ", from=" + from + ", msg=" + msg);
    }

}
