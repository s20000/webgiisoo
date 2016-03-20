/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.X;
import com.giisoo.utils.base.RSA;
import com.giisoo.utils.base.RSA.Key;
import com.mongodb.BasicDBObject;

/**
 * RSA key pair
 * 
 * @author joe
 *
 */
@DBMapping(collection = "gi_keypair")
public class Keypair extends Bean {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    // long created;
    // String memo;
    // int length;
    //
    // String pubkey;
    // String prikey;

    /**
     * Creates the.
     * 
     * @param length
     *            the length
     * @param memo
     *            the memo
     * @return the long
     */
    public static long create(int length, String memo) {

        Key k = RSA.generate(length);
        if (k != null) {
            long created = System.currentTimeMillis();
            if (Bean.insertCollection(V.create(X._ID, created).set("created", created).set("length", length).set("memo", memo).set("pubkey", k.pub_key).set("prikey", k.pri_key), Keypair.class) > 0) {
                return created;
            }
        }

        return 0;
    }

    /**
     * Load.
     * 
     * @param s
     *            the s
     * @param n
     *            the n
     * @return the beans
     */
    public static Beans<Keypair> load(int s, int n) {
        return Bean.load(new BasicDBObject(), new BasicDBObject(X._ID, -1), s, n, Keypair.class);
    }

    /**
     * Update.
     * 
     * @param created
     *            the created
     * @param v
     *            the v
     * @return the int
     */
    public static int update(long created, V v) {
        return Bean.updateCollection(created, v, Keypair.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
     */
    // @Override
    // protected void load(ResultSet r) throws SQLException {
    // created = r.getLong("created");
    // memo = r.getString("memo");
    // length = r.getInt("length");
    //
    // pubkey = r.getString("pubkey");
    // prikey = r.getString("prikey");
    // }

    public long getCreated() {
        return getLong("created");
    }

    public String getMemo() {
        return getString("memo");
    }

    public int getLength() {
        return getInt("length");
    }

    public String getPubkey() {
        return getString("pubkey");
    }

    public String getPrikey() {
        return getString("prikey");
    }

    /**
     * Load.
     * 
     * @param created
     *            the created
     * @return the keypair
     */
    public static Keypair load(long created) {
        return Bean.load(new BasicDBObject(X._ID, created), Keypair.class);
    }

    public static void delete(long created) {
        Bean.delete(new BasicDBObject(X._ID, created), Keypair.class);
    }

}
