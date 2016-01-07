/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.X;
import com.giisoo.framework.mdc.TConn;
import com.giisoo.framework.web.Model;
import com.giisoo.framework.web.Path;

public class mdc extends Model {

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.Model#onGet()
     */
    @Override
    @Path(login = true, access = "access.config.admin")
    public void onGet() {
        int s = this.getInt("s");
        int n = this.getInt("n", 10, "default.list.number");
        Beans<TConn> bs = TConn.load(null, s, n);
        this.set(bs, s, n);

        this.show("/admin/mdc.index.html");

    }

    /**
     * Close.
     */
    @Path(path = "close", login = true, access = "access.config.admin")
    public void close() {
        String clientids = this.getString("clientid");
        int updated = 0;
        if (clientids != null) {
            String[] ss = clientids.split(",");
            for (String clientid : ss) {
                // MQ.close(clientid);
                updated++;
            }
        }

        if (updated > 0) {
            this.set(X.MESSAGE, lang.get("close.success"));
        } else {
            this.set(X.MESSAGE, lang.get("select.required"));
        }

        onGet();
    }
}
