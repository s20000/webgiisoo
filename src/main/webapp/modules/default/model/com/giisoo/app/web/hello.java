/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import com.giisoo.framework.web.*;

/**
 * web api：/hello
 * <p>
 * it will be accessed when mdc connected
 * 
 * @author joe
 *
 */
public class hello extends Model {

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.framework.web.Model#onMDC()
     */
    @Override
    @Require(login = false)
    public void onMDC() {

    }

    /**
     * Test.
     * 
     * @param id
     *            the id
     * @param param
     *            the param
     */
    @Path(path = "test/(.*)/(.*)", method = Model.METHOD_GET, login = true)
    public void test(String id, String param) {
        this.println("id=" + id);
        this.println("params=" + param);
    }

}
