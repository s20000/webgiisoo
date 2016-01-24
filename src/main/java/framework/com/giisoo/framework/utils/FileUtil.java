package com.giisoo.framework.utils;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.core.bean.Bean;

public class FileUtil {

    static Log log = LogFactory.getLog(FileUtil.class);

    public static enum R {
        SAME, DIFF, HIGH, LOW
    }

    String name;
    Version ver;
    File f;

    public String getName() {
        return name;
    }

    public FileUtil(File f) {
        this.f = f;
        name = f.getName();
        String[] ss = name.split("[-_]");
        if (ss.length > 1) {
            String ver = ss[ss.length - 1];
            name = name.substring(0, name.length() - ver.length() - 1);
            ver = ver.substring(0, ver.length() - 4); // cut off ".jar"
            this.ver = new Version(ver);
        }
    }

    public File getFile() {
        return f;
    }

    public R compareTo(File f1) {
        return compareTo(new FileUtil(f1));
    }

    public static class Version {
        String ver;
        String[] ss;

        Version(String s) {
            ver = s;
            ss = s.split("\\.");
        }

        R compareTo(Version v1) {
            try {
                for (int i = 0; i < ss.length; i++) {
                    int i1 = Bean.toInt(ss[i]);
                    int i2 = v1.ss.length>i ? Bean.toInt(v1.ss[i]):0;

                    if (i1 > i2) {
                        return R.HIGH;
                    } else if (i1 < i2) {
                        return R.LOW;
                    } else if (v1.ss.length == i) {
                        return R.HIGH;
                    }
                }
                return R.LOW;
            } catch (Exception e) {
                log.error("this=" + this + ", v1=" + v1, e);
            }

            return R.DIFF;
        }

        transient String _string;

        @Override
        public String toString() {
            if (_string == null) {
                _string = new StringBuilder("(ver=").append(ver).append(Bean.toString(ss)).append(")").toString();
            }

            return _string;
        }

    }

    public R compareTo(FileUtil f1) {
        if (!this.name.equalsIgnoreCase(f1.name)) {
            return R.DIFF;
        }

        if (ver != null) {
            if (f1.ver == null) {
                return R.HIGH;
            } else {
                return ver.compareTo(f1.ver);
            }
        } else if (f1.ver == null) {
            return R.SAME;
        } else {
            return R.LOW;
        }
    }

}
