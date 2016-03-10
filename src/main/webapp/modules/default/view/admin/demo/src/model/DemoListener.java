/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.giisoo.demo.web;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.app.web.admin.setting;
import com.giisoo.framework.web.LifeListener;
import com.giisoo.framework.web.Module;
import com.giisoo.demo.web.admin.demosetting;

/**
 * lifelistener, it will be called when the module start
 * 
 * @author joe
 *
 */
public class DemoListener implements LifeListener {

    static Log log = LogFactory.getLog(DemoListener.class);

    /**
     * be called when starting
     */
    public void onStart(final Configuration conf, final Module module) {

        log.warn("WEBDEMO is starting");
        setting.register("demosetting", demosetting.class);

    }

    public void onStop() {
    }

    /**
     * test and install the database part
     */
    public void upgrade(Configuration conf, Module module) {
        log.debug("installing");

    }

    public void uninstall(Configuration conf, Module module) {
    }

}
