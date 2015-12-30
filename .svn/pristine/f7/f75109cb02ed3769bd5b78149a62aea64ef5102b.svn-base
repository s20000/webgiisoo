package com.giisoo.framework.mdc;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.TimeStamp;
import com.giisoo.core.conf.Config;
import com.giisoo.core.db.DB;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.mdc.command.IResponse;

public class MDCConnectorTest implements IResponse {

    public static void main(String[] args) {
        String home = "/home/joe/d/www/s1";

        System.out.println("home=" + home);

        System.setProperty("home", home);

        try {
            Config.init("home", "giisoo");

            Configuration conf = Config.getConfig();

            DB.init();
            Bean.init(conf);

            WorkerTask.init(conf.getInt("thread.number", 20), conf);
            MDCConnector.init();
            String host = "0.0.0.0";
            int port = 1099;

            con = MDCConnector.connectByTcp(host, port);
            con.activate(uid, TConn.pub_key, new MDCConnectorTest());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String clientid;
    static String key = "bbb";
    static byte[] deskey = null;
    static TConn con = null;
    static String pubkey = null;
    static String uid = "111111";

    @Override
    public void onActivate(int state, String clientid, String pubkey) {
        MDCConnectorTest.clientid = clientid;
        MDCConnectorTest.pubkey = pubkey;
        con.hello(clientid, key, pubkey, this);

    }

    @Override
    public void onHello(int state, byte[] key) {
        deskey = key;
        new WorkerTask() {

            @Override
            public void onExecute() {
                JSONObject jo = new JSONObject();

                int n = 10001;
                TimeStamp t = TimeStamp.create();
                for (int i = 1; i < n; i++) {
                    con.send(jo, null);
                    // System.out.println("send 1, cost: " + t.reset() + "ms");
                    if (i % 10000 == 0) {
                        System.out.println("send 10000, cost: " + t.reset() + "ms");
                    }
                }
                System.out.println("over");
            }

        }.schedule(0);
    }

    @Override
    public void onLogin(int state, JSONObject in) {

    }

    @Override
    public void onConnected() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDisconnected() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResponse(int state, long seq, JSONObject in, byte[] bb) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTimeout(long seq) {
        // TODO Auto-generated method stub

    }

}
