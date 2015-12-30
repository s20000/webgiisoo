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
import com.mongodb.BasicDBObject;

@DBMapping(collection = "gi_app")
public class App extends Bean {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    // String appid;
    // String key;
    // String memo;
    // String company;
    // String contact;
    // String phone;
    // String email;
    // String logout;
    //
    // String setrule;
    // String getrule;
    //
    // int locked;
    // long lastlogin;
    // long created;

    public String getLogout() {
        return getString("logout");
    }

    public String getAppid() {
        return getString("appid");
    }

    public String getKey() {
        return getString("key");
    }

    // public String getMemo() {
    // return memo;
    // }
    //
    // public String getCompany() {
    // return company;
    // }
    //
    // public String getContact() {
    // return contact;
    // }
    //
    // public String getPhone() {
    // return phone;
    // }
    //
    // public String getEmail() {
    // return email;
    // }
    //
    // public int getLocked() {
    // return locked;
    // }
    //
    // public long getCreated() {
    // return created;
    // }
    //
    // public String getSetrule() {
    // return setrule;
    // }
    //
    // public String getGetrule() {
    // return getrule;
    // }

    /**
     * Creates the.
     * 
     * @param appid
     *            the appid
     * @param v
     *            the v
     * @return the int
     */
    public static int create(String appid, V v) {
        if (!Bean.exists(new BasicDBObject().append(X.ID, appid), App.class)) {
            return Bean.insertCollection(v.set(X._ID, appid).set("appid", appid).set("created", System.currentTimeMillis()), App.class);
        }
        return 0;
    }

    /**
     * Load.
     * 
     * @param appid
     *            the appid
     * @return the app
     */
    public static App load(String appid) {
        return Bean.load(new BasicDBObject().append(X._ID, appid), App.class);
    }

    public boolean isLocked() {
        return getInt("locked") > 0;
    }

    public long getLastlogin() {
        return getLong("lastlogin");
    }

    /**
     * Update.
     * 
     * @param appid
     *            the appid
     * @param v
     *            the v
     * @return the int
     */
    public static int update(String appid, V v) {
        return Bean.updateCollection(appid, v, App.class);
    }

    // /*
    // * (non-Javadoc)
    // *
    // * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
    // */
    // @Override
    // protected void load(ResultSet r) throws SQLException {
    // appid = r.getString("appid");
    // key = r.getString("_key");
    //
    // setrule = r.getString("setrule");
    // getrule = r.getString("getrule");
    //
    // memo = r.getString("memo");
    // company = r.getString("company");
    // contact = r.getString("contact");
    // phone = r.getString("phone");
    // email = r.getString("email");
    // locked = r.getInt("locked");
    // lastlogin = r.getLong("lastlogin");
    // created = r.getLong("created");
    // logout = r.getString("logout");
    // }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Bean#toJSON(net.sf.json.JSONObject)
     */
    // @Override
    // public boolean toJSON(JSONObject jo) {
    // jo.put("appid", appid);
    // jo.put("key", key);
    // jo.put("memo", memo);
    // jo.put("company", company);
    // jo.put("contact", contact);
    // jo.put("phone", phone);
    // jo.put("email", email);
    // jo.put("locked", locked);
    // jo.put("lastlogin", lastlogin);
    // jo.put("created", created);
    // jo.put("logout", logout);
    // jo.put("setrule", setrule);
    // jo.put("getrule", getrule);
    //
    // return true;
    // }

    /**
     * 
     * @param q
     * @param order
     * @param s
     * @param n
     * @return Beans
     */
    public static Beans<App> load(BasicDBObject q, BasicDBObject order, int s, int n) {
        return Bean.load(q, order, s, n, App.class);
    }

}
