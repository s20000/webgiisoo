/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import org.apache.commons.configuration.Configuration;

import com.giisoo.core.conf.Config;
import com.giisoo.framework.web.*;

/**
 * web api: /admin/dashboard
 * <p>
 * used to show dashboard
 * 
 * @author yjiang
 * 
 */
public class dashboard extends Model {

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.Model#onGet()
     */
    @Override
    @Path(login = true, access = "access.config.admin")
    public void onGet() {

        Configuration conf = Config.getConfig();

        this.set("me", this.getUser());
        this.set("uptime", lang.format(Model.UPTIME, "yy-MM-dd"));
        this.set("past", lang.past(Model.UPTIME));
        this.set("node", conf.getString("node", ""));
        this.set("release", Module.load("default").getVersion());
        this.set("build", Module.load("default").getBuild());

        show("admin/dashboard.html");
    }

}
