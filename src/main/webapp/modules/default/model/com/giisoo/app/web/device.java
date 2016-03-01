package com.giisoo.app.web;

import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;

/**
 * web api: /device
 * <p>
 * used to test the device user agent
 * 
 * @author joe
 *
 */
public class device extends Model {

    @Path()
    public void onGet() {
        // AccessLog.create(this.getRemoteHost(), this.path, V.create("agent",
        // this.browser()).set("status", 200));

        this.set("ip", this.getRemoteHost());
        this.set("headers", this.getHeaders());
        this.show("/device.html");
    }
}
