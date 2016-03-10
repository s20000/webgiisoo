package com.giisoo.demo.web.admin;

import com.giisoo.app.web.admin.setting;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;

/**
 * some setting of the module
 * 
 * @author joe
 *
 */
public class demosetting extends setting {

    /**
     * setting
     */
    @Override
    public void set() {
        SystemConfig.setConfig("somesetting", this.getString("somesetting"));

        this.set(X.MESSAGE, "修改成功！");

        get();
    }

    /**
     * view the setting
     */
    @Override
    public void get() {
        this.set("somesetting", SystemConfig.s("somesetting", null));

        /**
         * show the widget
         */
        this.set("page", "/admin/demo.html");
    }

}
