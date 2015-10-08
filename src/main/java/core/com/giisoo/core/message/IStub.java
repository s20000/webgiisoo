package com.giisoo.core.message;

import net.sf.json.JSONObject;

public interface IStub {
    public void onRequest(String to, String from, String src, byte flag, JSONObject header, JSONObject msg, byte[] attachment);

    public void onResponse(String to, String from, String src, byte flag, JSONObject header, JSONObject msg, byte[] attachment);
}
