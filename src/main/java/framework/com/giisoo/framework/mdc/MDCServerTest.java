package com.giisoo.framework.mdc;

import org.apache.commons.configuration.Configuration;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.conf.Config;
import com.giisoo.core.db.DB;
import com.giisoo.core.worker.WorkerTask;

public class MDCServerTest {

    public static void main(String[] args) {
        String home = "/home/joe/d/www/s1";

        System.out.println("home=" + home);

        System.setProperty("home", home);

        try {
            Config.init("home", "giisoo");

            Configuration conf = Config.getConfig();

            DB.init();
            Bean.init(conf);
            
            WorkerTask.init(conf.getInt("thread.number", 1000), conf);

            String host = "0.0.0.0";
            int port = 1099;
            MDCServer mdc = MDCServer.createTcpServer(host, port);
            mdc.start();

            MDCServer udc = MDCServer.createUdpServer(host, port);
            udc.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
