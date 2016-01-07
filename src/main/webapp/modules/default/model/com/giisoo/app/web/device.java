package com.giisoo.app.web;

import com.giisoo.core.bean.Bean.V;
import com.giisoo.framework.common.AccessLog;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;

public class device extends Model {

    @Path()
    public void onGet() {
        AccessLog.create(this.getRemoteHost(), this.path, V.create("agent", this.browser()));

        this.set("ip", this.getRemoteHost());
        this.set("agent", this.browser());
        this.show("/device.html");
    }

}
