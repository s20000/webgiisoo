package com.giisoo.app.web.admin;

import com.giisoo.framework.web.Model;

public class cluster extends Model {

    @Override
    public void onGet() {
        this.show("/admin/cluster.index.html");
    }

}
