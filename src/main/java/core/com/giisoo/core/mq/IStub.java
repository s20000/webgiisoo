package com.giisoo.core.mq;

import net.sf.json.JSONObject;

/**
 * The {@code IStub} Interface is used to handle the request message come in or
 * a response come in in Distributed System
 * 
 * @author joe
 *
 */
public interface IStub {

    /**
     * a request come in
     * 
     * @param seq
     * @param to
     * @param from
     * @param src
     * @param header
     * @param msg
     * @param attachment
     */
    public void onRequest(long seq, String to, String from, String src, JSONObject header, JSONObject msg, byte[] attachment);

    /**
     * a response come in
     * 
     * @param seq
     * @param to
     * @param from
     * @param src
     * @param header
     * @param msg
     * @param attachment
     */
    public void onResponse(long seq, String to, String from, String src, JSONObject header, JSONObject msg, byte[] attachment);

}
