package com.giisoo.core.mq;

import net.sf.json.JSONObject;

/**
 * the message stub
 * 
 * @author joe
 *
 */
public interface IStub {

    public void onRequest(long seq, String to, String from, String src,  JSONObject header, JSONObject msg, byte[] attachment);

    public void onResponse(long seq, String to, String from, String src,JSONObject header, JSONObject msg, byte[] attachment);

}
