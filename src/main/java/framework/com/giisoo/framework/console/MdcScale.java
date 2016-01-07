/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.console;

import java.io.*;

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.conf.*;
import com.giisoo.core.db.DB;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.mdc.*;
import com.giisoo.framework.mdc.command.*;
import com.giisoo.utils.base.*;
import com.giisoo.utils.base.RSA.Key;

public class MdcScale {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		try {

			System.getProperties().setProperty("MMS_HOME",
					"/opt/d/joe/www/webs1");

			Config.init("MMS_HOME", "giisoo");

			Configuration conf = Config.getConfig();

			DB.init();
			Bean.init(conf);

			WorkerTask.init(conf.getInt("thread.number", 200), conf);

			Command.init();

			MDCConnector.init();

			TConn.DEBUG = true;

			MdcScale mdc = new MdcScale();
			mdc.host = conf.getString("mdc.server", mdc.host);

			mdc.start(conf);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start.
	 * 
	 * @param conf
	 *            the conf
	 */
	protected void start(Configuration conf) {
		String cmd = null;

		while (!"q".equals(cmd)) {
			System.out.println("Command:");
			System.out
					.println(" s: setting host, port, tcp/udp, uid, clientid, zip, encode");
			System.out.println(" c: connect to [" + host + ":" + port
					+ "] by [protocol=" + via + ", zip=" + zip + ", encode="
					+ encode + "]");
			System.out.println(" a: activate");
			System.out.println(" h: hello");
			System.out.println(" app: send app string");
			System.out.println(" t: test whole mdc");
			System.out.println(" q: quit the console");

			cmd = getCmd();
			if ("c".equals(cmd)) {
				connect();
			} else if ("s".equals(cmd)) {
				setting();
			} else if ("a".equals(cmd)) {
				activate();
			} else if ("h".equals(cmd)) {
				hello();
			} else if ("app".equals(cmd)) {
				app();
			} else if ("t".equals(cmd)) {
				testMdc();
			}
		}

		if (reader != null) {
			try {
				reader.close();
			} catch (Exception e) {

			}
		}

		System.exit(0);
	}

	TConn con = null;
	static String pubkey = null;

	private void connect() {

		if (con != null) {
			con.close();
		}

		if ("tcp".equals(via)) {
			con = TConn.connectByTcp(host, port, r);
		} else {
			con = TConn.connectByUdp(host, port, r);
		}
	}

	private void port() {
		System.out.println("port? [" + (port > 0 ? port : "") + "]");
		String cmd = getCmd();
		port = Bean.toInt(cmd);
	}

	private void host() {
		System.out.println("Host name or IP? [" + host + "]");
		String cmd = getCmd();
		if (cmd.length() > 0) {
			host = cmd;
		}
	}

	String via = "tcp";
	String host = "mdc.giisoo.com";
	boolean zip = false;
	boolean encode = false;
	int port = 1099;

	private void app() {
		String cmd = null;

		while (!"r".equals(cmd)) {
			System.out.println("Command:");
			System.out.println(" s: send a json");
			System.out.println(" r: return");

			cmd = getCmd();
			if ("s".equals(cmd)) {
				send();
			}
		}
	}

	private void send() {
		try {
			System.out.println("json?");
			String cmd = getCmd();
			if (cmd.length() > 0) {
				JSONObject jo = JSONObject.fromObject(cmd);
				byte[] bb = new byte[4096];
				con.send(jo, bb);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void hello() {
		con.hello(con.getClientId(), uid, pubkey, r);
	}

	private void activate() {
		if (con == null) {
			System.out.println("[Error] not connected yet");
			return;
		}

		con.activate(uid, pub_key, r);

	}

	private void setting() {
		String cmd = null;

		while (!"r".equals(cmd)) {
			System.out.println("Command:");
			System.out.println(" h: input host"
					+ (host != null ? "[" + host + "]" : "[]"));
			System.out.println(" p: input port"
					+ (port > 0 ? "[" + port + "]" : "[]"));
			System.out.println(" u: by UDP"
					+ ("udp".equals(via) ? "[set]" : "[]"));
			System.out.println(" t: by TCP"
					+ ("tcp".equals(via) ? "[set]" : "[]"));
			System.out.println(" z: zip? " + (zip ? "[yes]" : "[]"));
			System.out.println(" e: encode? " + (encode ? "[yes]" : "[]"));
			System.out.println(" uid: set uid "
					+ (uid != null ? "[" + uid + "]" : "[]"));
			System.out.println(" r: return");

			cmd = getCmd();
			if ("h".equals(cmd)) {
				host();
			} else if ("p".equals(cmd)) {
				port();
			} else if ("u".equals(cmd)) {
				via = "udp";
			} else if ("t".equals(cmd)) {
				via = "tcp";
			} else if ("z".equals(cmd)) {
				zip = !zip;
			} else if ("e".equals(cmd)) {
				encode = !encode;
			} else if ("uid".equals(cmd)) {
				uid();
			}
		}
	}

	private void uid() {
		System.out.println("uid? [" + uid + "]");
		String cmd = getCmd();
		if (cmd.length() > 0) {
			uid = cmd;
		}
	}

	BufferedReader reader = null;

	String getCmd() {
		try {
			if (reader == null) {
				reader = new BufferedReader(new InputStreamReader(System.in));
			}

			System.out.print("$>");
			return reader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Inits the mdc.
	 */
	static void initMdc() {
		Key key = RSA.generate(2048);
		pri_key = key.pri_key;
		pub_key = key.pub_key;

		uid = "111111";
	}

	static String uid = "111111";
	static String pri_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCQv7flgL7Y9GJlEr07XyUvRpHufN0Abx2Kb3T7oceTxdB9iOoZCc7/jvjmmL41G3x1UJfGWsy4sIWy4aqaDZlbqJxgnaZpO3sclUyjXDH6McqjGVQZRnKgvuRV+RT5yxDmUq+4ddxLZjuaH7YoUC/NiW6ZOK9fiVm5RwV+1Z6N6xYWtstc2eKutxRVXpSynPflJjthe3BzlYg7fEx9jYGgzEAks5VKCcOEHcs/TxWCxkjnIOG8nQ4cu8a9vBJbMERbuvhTkEL2rwRLrTHuZE4O+3+D2PDoDB+zvGXn9jRkFElF/HtYDo41YaWsppG4PVrkBWjXf3kP/vERJnPvSuz7AgMBAAECggEAKigcF/RSIP+z9HnrsPH583XbJJkrCGhUMzBY4rxwUsJnAVixzU8FAXsTVJ2hr/fsXIJ3YaaqPxPyLHH67NkPFBncSWtAvx86sM1uv5knWRXNTYkf7CiPU++kssFTcZjJuDrWwKNa4H7K1w1rUPxhn7vqASf4M5veDM75c8IlZnFhf0l2V4+oqPSfZeIU8vvcT2xmQkTwz3O778LoDV/PbZEVL+dZvS9oqIAzkBNunouCdm+nYYopM7O/3sNX7XRtIMe20mCnn2dshU6BCDne+2+MRC62s+VG4ehZWN+gjP+7MP+P5Evvt2wNluawowm/1LxhqyYzKvHhd6+LRIVIqQKBgQDu/hzqeBrLy6Fe4198bZ+Uj7DQiqdGRDF9kK/V8EPJ7jqygvpivqSZBlsaBfekOaCO00R2BlNKLq9DCxMEyWRid9QB0xGdLT2furIPC56SfvWlfvsLR5w4WsxsfuTh539oNabteEqv+IVUD5wTWMqHYzdSfHe7CSlb2bTzt3NkrwKBgQCbDLR3cipYKAUjzt64RukJpGTNxcQD7ZU4IM1MEkyeBXnzccfgELYOtzUH8aqduIxG6Dq2IlfyEeaJYLjRCur6RV8MXNoXq3BmviPIyStvY+OuYQyxpV+EUng2cllrvzktShPLmWgZ53RR5U10MlMRU8OGxSZfJXef2EvrmIzndQKBgQCZJlRHWJHrWOFfGhudc+5YWEA5UzDgUWDZkzdqxF3ccAJDVLW5Q2vc9/q3UW2hWqBuBiqP63vCROpg7x7P6XKfuyQryyM1csFTs2LZS1vhiuRFmnqrfsc4Qc9QE3z+2seFcyQ2duh9nadq99nPeA3I9qaEAGkFfVh4mKlhqISU1wKBgAE/LehT+7EvyTLLhBD5SQBx2PmqoHPjojKMWPpGn2UZ6Cwj+xZ7K+ucX4nL0LJp/H7JeUdjqU9iA3eRVosfylnu2t/Pk1VjZIj++Gyx6W6A4CDT2PVIvibjPuI5MyzhocUSdmxJtIOYz9+kt2W6zd4yzTy1MyTKuVaIt7uSQ3fNAoGACYp1lXB1MZi7UyKXSgbOmld1QH+blzA0xLgMraPNsiOqbL2VSqteA9xbO3vaRTdMoZuPbCMfAQMrh0nBHcNhqdF9IYBkHcQMNn9D89QKgYCu4sdb2EWh/9R9DqDEOd7YH+QsASCaWt96+YuGICK7/fIsgkbZcHNRJO2H19aUsLw=";
	static String pub_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkL+35YC+2PRiZRK9O18lL0aR7nzdAG8dim90+6HHk8XQfYjqGQnO/4745pi+NRt8dVCXxlrMuLCFsuGqmg2ZW6icYJ2maTt7HJVMo1wx+jHKoxlUGUZyoL7kVfkU+csQ5lKvuHXcS2Y7mh+2KFAvzYlumTivX4lZuUcFftWejesWFrbLXNnirrcUVV6Uspz35SY7YXtwc5WIO3xMfY2BoMxAJLOVSgnDhB3LP08VgsZI5yDhvJ0OHLvGvbwSWzBEW7r4U5BC9q8ES60x7mRODvt/g9jw6Awfs7xl5/Y0ZBRJRfx7WA6ONWGlrKaRuD1a5AVo1395D/7xESZz70rs+wIDAQAB";

	static RR r = new RR();

	/**
	 * Test mdc.
	 */
	void testMdc() {

		try {
			initMdc();

			System.out.println("pri_key=" + pri_key);
			System.out.println("pub_key=" + pub_key);
			System.out.println("uid=" + uid);

			final TConn c = TConn.connectByTcp(host, port, r);
			r.setC(c);

			c.activate(uid, pub_key, r);
			// c.hello(clientid, uid);

			JSONObject jo = new JSONObject();

			jo.put("name", "11113");
			jo.put("pwd", "2");
			jo.put("phone", "22222");

			c.post("/user/register", System.currentTimeMillis(), jo);

			jo.clear();
			jo.put("phone", "1");
			jo.put("uid", uid);
			c.post("/user/login", System.currentTimeMillis(), jo);

			jo.clear();
			jo.put("_id", 3007);
			jo.put("created", System.currentTimeMillis());
			/**
			 * "_id":3007,"created":1415167304450,"data":{"chat_id":2198,
			 * "content":"发送测试消息 － 502","diagnosis_id":0,"from_uid":16777237,
			 * "from_user_type"
			 * :1,"to_uid":16777224,"to_user_type":0,"type":"text"
			 * },"datatype":"chat","fk":"2198","mimetype":"notification"
			 */
			jo.put("to", "1,2,3");
			jo.put("subject", "subject");
			jo.put("body", "asdasdsad");
			c.post("/message/relay", System.currentTimeMillis(), jo,
					"aaaaaaa".getBytes(), new RR(c));

			jo.clear();
			jo.put("name", "1,2,3");
			c.post("/user/friend", System.currentTimeMillis(), jo);

			jo.clear();
			jo.put("name", "1,2,3");
			c.post("/subject/create", System.currentTimeMillis(), jo);
			c.post("/subject/get", System.currentTimeMillis(), jo);

			jo.clear();
			jo.put("id", 1);
			jo.put("comment", "asdasdasdaaaaa");
			c.post("/subject/comment", System.currentTimeMillis(), jo);
			c.post("/subject/follow", System.currentTimeMillis(), jo);
			c.post("/subject/search", System.currentTimeMillis(), jo);

			// c.post("/images/logo.png", System.currentTimeMillis(), jo);

			// c.bye();

			// while (!c.valid()) {
			// Log.info("c:" + c);
			// Thread.sleep(1000);
			// }
			//
			// Log.info("valid!!!!");
			//
			// // long total = c.send("d:/pic/31953.jpg");
			// // long total = c.send("d:/pic/31953.jpg");
			//
			// RR r = new RR(c);
			// String file = "d:/pic/31953.jpg";
			// long total = c.send(file, r);
			// c.resume(file, total / 2, r);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// c.close();
	}

	static class RR extends DefaultResponseHandler {
		TConn c = null;

		/**
		 * Instantiates a new rr.
		 * 
		 * @param c
		 *            the c
		 */
		RR(TConn c) {
			this.c = c;
		}

		/**
		 * Instantiates a new rr.
		 */
		RR() {

		}

		void setC(TConn c) {
			this.c = c;
		}

		/* (non-Javadoc)
		 * @see com.giisoo.framework.mdc.command.DefaultResponseHandler#onResponse(int, long, net.sf.json.JSONObject, byte[])
		 */
		public void onResponse(int state, long seq, JSONObject in, byte[] bb) {
			// try {
			// Raw.resume(in, c, this);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
		}

		/* (non-Javadoc)
		 * @see com.giisoo.framework.mdc.command.DefaultResponseHandler#onTimeout(long)
		 */
		public void onTimeout(long seq) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see com.giisoo.framework.mdc.command.DefaultResponseHandler#onActivate(int, java.lang.String, java.lang.String)
		 */
		public void onActivate(int state, String clientid, String pubkey) {
			// TODO Auto-generated method stub
			MdcScale.pubkey = pubkey;
		}

		/* (non-Javadoc)
		 * @see com.giisoo.framework.mdc.command.DefaultResponseHandler#onHello(int, byte[])
		 */
		public void onHello(int state, byte[] key) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see com.giisoo.framework.mdc.command.DefaultResponseHandler#onConnected()
		 */
		public void onConnected() {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see com.giisoo.framework.mdc.command.DefaultResponseHandler#onDisconnected()
		 */
		public void onDisconnected() {
			// TODO Auto-generated method stub

		}
	}

}
