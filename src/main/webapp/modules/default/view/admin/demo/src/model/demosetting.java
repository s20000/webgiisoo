package com.giisoo.demo.web.admin;

import com.giisoo.app.web.admin.setting;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;

public class demosetting extends setting {

    @Override
    public void set() {
        SystemConfig.setConfig("somesetting", this.getString("somesetting"));

        this.set(X.MESSAGE, "修改成功！");

        get();
    }

    @Override
    public void get() {
        this.set("somesetting", SystemConfig.s("somesetting", null));

        this.set("page", "/admin/demo.htm");
    }

}
