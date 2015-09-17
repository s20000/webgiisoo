/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.app.web.admin.accesslog;
import com.giisoo.core.bean.X;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.core.worker.WorkerTask;
import com.giisoo.framework.common.AccessLog;
import com.giisoo.framework.common.OpLog;
import com.giisoo.framework.common.Temp;

/**
 * clean up the oplog, temp file in Temp
 * 
 * @author joe
 * 
 */
class CleanupTask extends WorkerTask {

    static Log log = LogFactory.getLog(CleanupTask.class);

    String home;

    /**
     * Instantiates a new cleanup task.
     * 
     * @param conf
     *            the conf
     */
    public CleanupTask(Configuration conf) {
        home = conf.getString("home");
    }

    @Override
    public String getName() {
        return "cleanup.task";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.worker.WorkerTask#onExecute()
     */
    @Override
    public void onExecute() {
        try {
            /**
             * clean up the local temp files
             */
            int count = 0;
            for (String f : folders) {
                String path = home + f;
                count += cleanup(path);
            }

            /**
             * clean files in Temp
             */
            if (Temp.ROOT != null) {
                count += cleanup(Temp.ROOT);
            }

            log.info("cleanup temp files: " + count);

            OpLog.cleanup();

            AccessLog.cleanup();

        } catch (Exception e) {
            // eat the exception
        }
    }

    private int cleanup(String path) {
        int count = 0;
        try {
            File f = new File(path);

            /**
             * test the file last modified exceed the cache time
             */
            if (f.isFile() && f.lastModified() + SystemConfig.l("cache.time", X.ADAY) < System.currentTimeMillis()) {
                f.delete();
                count++;
            } else if (f.isDirectory()) {
                File[] list = f.listFiles();
                if (list == null || list.length == 0) {
                    /**
                     * delete the empty folder
                     */
                    f.delete();
                    count++;
                } else {
                    /**
                     * cleanup the sub folder
                     */
                    for (File f1 : list) {
                        count += cleanup(f1.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {

        }

        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.worker.WorkerTask#priority()
     */
    @Override
    public int priority() {
        return Thread.MIN_PRIORITY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.worker.WorkerTask#onFinish()
     */
    @Override
    public void onFinish() {
        this.schedule(X.AMINUTE * 10);
    }

    static String[] folders = { "/tmp/_cache", "/tmp/_raw" };
}
