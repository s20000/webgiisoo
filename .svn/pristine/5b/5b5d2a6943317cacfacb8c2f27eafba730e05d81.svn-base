/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.core.bean.X;
import com.giisoo.framework.web.Language;
import com.giisoo.framework.web.Module;

// TODO: Auto-generated Javadoc
/**
 * The Class Shell.
 */
public class Shell {

    /** The log. */
    static Log log = LogFactory.getLog(Shell.class);

    public static enum Logger {
        error("ERROR"), warn("WARN"), info("INFO");

        String level;

        Logger(String s) {
            this.level = s;
        }

    };

    /**
     * Run.
     * 
     * @param command
     *            the command
     * @return the string
     * @throws Exception
     *             the exception
     */
    public static String run(String command) throws Exception {
        return run(command, null, null);
    }

    public static String run(String command, String passwd, IPrint print) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader input = null;

        try {
            Process p = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", command });

            input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = input.readLine();
            while (line != null) {
                if (line.toLowerCase().indexOf("password") > 0 && !X.isEmpty(passwd)) {
                    p.getOutputStream().write((passwd + "\n").getBytes());
                }
                if (print != null) {
                    print.print(line);
                } else {
                    sb.append(line).append("\r\n");
                }
                line = input.readLine();
            }
            input.close();
            input = null;
            if (sb.length() > 0)
                return sb.toString();

            input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            line = input.readLine();
            while (line != null) {
                if (print != null) {
                    print.print("<r>" + line + "</r>");
                } else {
                    sb.append(line).append("\r\n");
                }
                line = input.readLine();
            }
            p.destroy();

            return sb.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    // 192.168.1.1#系统名称#2014-10-31#ERROR#日志消息#程序名称
    public static void log(String ip, Logger level, String module, String message) {
        String deli = Module.home.get("log_deli", "#");
        StringBuilder sb = new StringBuilder();
        sb.append(ip).append(deli);
        sb.append("support").append(deli);
        sb.append(Language.getLanguage().format(System.currentTimeMillis(), "yyyy-MM-dd hh:mm:ss"));
        sb.append(deli).append(level.name()).append(deli).append(message).append(deli).append(module);

        try {
            Shell.run("logger " + level.level + deli + sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface IPrint {
        void print(String line);
    }
}
