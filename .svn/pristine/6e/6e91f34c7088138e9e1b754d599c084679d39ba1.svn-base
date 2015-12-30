package com.giisoo.core.bean;

import java.sql.*;
import java.util.Calendar;
import java.util.UUID;

import com.giisoo.core.conf.SystemConfig;
import com.giisoo.utils.base.*;

/**
 * The Class UID that used to create unique id, or sequence no
 */
public class UID extends Bean {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The seq. */
    long seq;

    public static int thisYear() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        return c.get(Calendar.YEAR);
    }

    /**
     * Next.
     * 
     * @param key
     *            the key
     * @return the long
     */
    public synchronized static long next(String key) {

        long prefix = SystemConfig.l("system.code", 1) * 10000000000000L;

        Connection c = null;
        PreparedStatement stat = null;
        ResultSet r = null;
        try {
            c = Bean.getConnection();

            long v = -1;
            while (v == -1) {
                if (stat != null) {
                    stat.close();
                }
                stat = c.prepareStatement("select l from tblconfig where name=?");
                stat.setString(1, key);
                r = stat.executeQuery();
                if (r.next()) {
                    v = r.getLong("l");
                }
                r.close();
                r = null;
                stat.close();

                if (v == -1) {
                    stat = c.prepareStatement("insert into tblconfig(name, l) values(?, ?)");
                    v = 1;
                    stat.setString(1, key);
                    stat.setLong(2, v + 1);
                } else {
                    stat = c.prepareStatement("update tblconfig set l=l+1 where name=? and l=?");
                    stat.setString(1, key);
                    stat.setLong(2, v);
                }
                if (stat.executeUpdate() > 0) {
                    return prefix + v;
                } else {
                    v = -1;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            Bean.close(r, stat, c);
        }

        return -1;
    }

    /**
     * Gets the.
     * 
     * @param key
     *            the key
     * @return the long
     */
    public static long get(String key, long defaultValue) {
        Connection c = null;
        PreparedStatement stat = null;
        ResultSet r = null;
        try {
            c = Bean.getConnection();
            stat = c.prepareStatement("select l from tblconfig where name=?");
            stat.setString(1, key);
            r = stat.executeQuery();
            if (r.next()) {
                return r.getLong("l");
            }

            return defaultValue;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            Bean.close(r, stat, c);
        }

        return defaultValue;
    }

    /**
     * Sets the.
     * 
     * @param key
     *            the key
     * @param newvalue
     *            the newvalue
     * @return the long
     */
    public static long set(String key, long newvalue) {
        Connection c = null;
        PreparedStatement stat = null;
        ResultSet r = null;
        try {
            c = Bean.getConnection();
            stat = c.prepareStatement("select l from tblconfig where name=?");
            stat.setString(1, key);
            r = stat.executeQuery();
            long v = -1;
            if (r.next()) {
                v = r.getLong("l");
            }

            if (v == -1) {
                stat = c.prepareStatement("insert into tblconfig(l, name) values(?, ?)");
            } else {
                stat = c.prepareStatement("update tblconfig set l=? where name=?");
            }
            stat.setLong(1, newvalue);
            stat.setString(2, key);

            stat.executeUpdate();

            return v;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            Bean.close(r, stat, c);
        }

        return 0;
    }

    /**
     * Random.
     * 
     * @return the string
     */
    public static String random() {
        return UUID.randomUUID().toString();
    }

    /**
     * Id.
     * 
     * @param hash
     *            the hash
     * @return the string
     */
    public static String id(long hash) {
        // System.out.println(hash);
        // System.out.println(Long.toHexString(hash));
        // System.out.println(H64.toString(hash));
        // System.out.println(H32.toString(hash));
        return H32.toString(hash);
        // return Long.toHexString(hash);
    }

    /**
     * Id.
     * 
     * @param ss
     *            the ss
     * @return the string
     */
    public static String id(Object... ss) {
        StringBuilder sb = new StringBuilder();
        for (Object s : ss) {
            if (sb.length() > 0)
                sb.append("/");
            sb.append(s);
        }
        return id(hash(sb.toString()));
    }

    /**
     * global id
     * 
     * @return String
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Hash.
     * 
     * @param s
     *            the s
     * @return the long
     */
    public static long hash(String s) {
        if (s == null) {
            return 0;
        }

        int h = 0;
        int l = 0;
        int len = s.length();
        char[] val = s.toCharArray();
        for (int i = 0; i < len; i++) {
            h = 31 * h + val[i];
            l = 29 * l + val[i];
        }
        return ((long) h << 32) | ((long) l & 0x0ffffffffL);
    }

    /**
     * Random.
     * 
     * @param length
     *            the length
     * @return the string
     */
    public static String random(int length) {
        StringBuilder sb = new StringBuilder();
        while (length > 0) {
            int j = (int) (Math.random() * chars.length);
            sb.append(chars[j]);
            length--;
        }
        return sb.toString();
    }

    /**
     * Digital.
     * 
     * @param length
     *            the length
     * @return the string
     */
    public static String digital(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(digitals[(int) (Math.random() * digitals.length)]);
        }
        return sb.toString();
    }

    static final char[] digitals = "0123456789".toCharArray();
    static final char[] chars = "0123456789abcdefghjiklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

}
