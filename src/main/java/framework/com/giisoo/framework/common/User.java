/*

 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.util.*;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.*;
import com.giisoo.core.cache.Cache;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.framework.web.Module;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * The {@code User} Class is base user class, all the login/access controlled in
 * webgiisoo was depended on the user, it contains all the user-info, and is
 * expandable.
 * <p>
 * MOST important field
 * 
 * <pre>
 * id: long, global unique,
 * name: login name, global unique
 * password: string of hashed
 * nickname: string of nickname
 * title: title of the user
 * roles: the roles of the user, user can has multiple roles
 * hasAccess: test whether has the access token for the user
 * </pre>
 * 
 * @author yjiang
 * 
 */
@DBMapping(collection = "gi_user")
public class User extends Bean {

    /**
   * 
   */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new user.
     */
    public User() {

    }

    /**
     * Checks if is role.
     * 
     * @param r
     *            the r
     * @return true, if is role
     */
    @SuppressWarnings("unchecked")
    public boolean isRole(Role r) {
        List<Long> roles = (List<Long>) this.get("roles");
        return roles != null && roles.contains(r.getId());
    }

    /**
     * Creates a user with the values
     * 
     * @param v
     *            the values
     * @return long of the user id, if failed, return -1
     */
    public static long create(V v) {

        String s = (String) v.value("password");
        if (s != null) {
            v.set("password", encrypt(s), true);
        }

        Long id = (Long) v.value("id");
        if (id == null) {
            id = UID.next("user.id");
            while (Bean.exists(new BasicDBObject(X._ID, id), User.class)) {
                id = UID.next("user.id");
            }
        }
        if (log.isDebugEnabled())
            log.debug("v=" + v);

        Bean.insertCollection(v.set(X._ID, id).set("created", System.currentTimeMillis()).set("updated", System.currentTimeMillis()), User.class);

        return id;
    }

    /**
     * create a user from the jo
     * 
     * @deprecated
     * @param jo
     * @return int
     */
    public static long copy(JSONObject jo) {

        V v = V.create();
        for (Object name : jo.keySet()) {
            v.set(name.toString(), jo.get(name));
        }

        return User.create(v);

    }

    /**
     * Creates a user with the jo <br>
     * using create(V) instead <br>
     * 
     * @deprecated
     * @param name
     *            the name
     * @param password
     *            the password
     * @param jo
     *            the jo
     * @return the int
     * @throws giException
     *             the gi exception
     */
    public static long create(String name, String password, JSONObject jo) throws giException {

        String allow = conf.getString("user.name", "^[a-zA-Z0-9]{4,16}$");

        if (X.isEmpty(name) || !name.matches(allow)) {
            /**
             * the format of name is not correct
             */
            throw new giException(-2, "the name format is not correct, or password is none");
        }

        if (Bean.exists(new BasicDBObject("name", name).append("locked", new BasicDBObject("$ne", 1)).append("remote", new BasicDBObject("$ne", 1)), User.class)) {
            /**
             * exists, create failed
             */
            throw new giException(-1, "the name exists");
        }

        V v = V.create();
        for (Object n : jo.keySet()) {
            v.set(n.toString(), jo.get(n));
        }

        v.set("name", name).set("password", password);
        return User.create(v);

    }

    /**
     * Load user by name and password
     * 
     * @param name
     *            the name of the user
     * @param password
     *            the password
     * @return User, if not match anyoone, return null
     */
    public static User load(String name, String password) {

        password = encrypt(password);

        log.debug("name=" + name + ", passwd=" + password);
        // System.out.println("name=" + name + ", passwd=" + password);

        return Bean.load(new BasicDBObject("name", name).append("password", password).append("deleted", new BasicDBObject("$ne", 1)).append("remote", new BasicDBObject("$ne", 1)), User.class);

    }

    public boolean isDeleted() {
        return getInt("deleted") == 1;
    }

    public long getId() {
        return this.getLong(X._ID);
    }

    /**
     * Load user by name
     * 
     * @param name
     *            the name of the name
     * @return User
     */
    public static User load(String name) {
        String uid = "user://name/" + name;
        User u1 = (User) Cache.get(uid);
        if (u1 != null) {
            return u1;
        }

        Beans<User> list = Bean.load(new BasicDBObject("name", name), new BasicDBObject("name", 1), 0, 100, User.class);

        if (list != null && list.getList() != null && list.getList().size() > 0) {
            for (User u : list.getList()) {

                /**
                 * if the user has been locked, then not allow to login
                 */
                if (u.isLocked() || u.isDeleted())
                    continue;

                u.setExpired(60);
                Cache.set(uid, u);
                return u;
            }
        }

        return null;
    }

    /**
     * Load user by id.
     * 
     * @param id
     *            the user id
     * @return User
     */
    public static User loadById(long id) {
        String uid = "user://id/" + id;
        User u = (User) Cache.get(uid);
        if (u != null && !u.expired()) {
            return u;
        }

        u = Bean.load(new BasicDBObject(X._ID, id), User.class);
        if (u != null) {
            u.setExpired(60);
            u.recache();
        }

        return u;
    }

    private void recache() {
        String uid = "user://id/" + getId();
        Cache.set(uid, this);
    }

    /**
     * Load users by access token name
     * 
     * @param access
     *            the access token name
     * @return list of user who has the access token
     */
    public static List<User> loadByAccess(String access) {

        Beans<Role> bs = Role.loadByAccess(access, 0, 1000);
        BasicDBObject q = new BasicDBObject();
        if (bs != null && bs.getList() != null) {
            if (bs.getList().size() > 1) {
                BasicDBList list = new BasicDBList();
                for (Role a : bs.getList()) {
                    list.add(new BasicDBObject("role", a.getId()));
                }
                q.append("$or", list);
            } else if (bs.getList().size() == 1) {
                q.append("role", bs.getList().get(0).getId());
            }
        }

        q.append("deleted", new BasicDBObject("$ne", 1));

        Beans<User> us = Bean.load(q, new BasicDBObject("name", 1), 0, Integer.MAX_VALUE, User.class);
        return us == null ? null : us.getList();

    }

    /**
     * Validate the user with the password
     * 
     * @param password
     *            the password
     * @return true, if the password was match
     */
    public boolean validate(String password) {

        /**
         * if the user has been locked, then not allow to login
         */
        if (this.isLocked())
            return false;

        /**
         * id == 0: admin; across.login ==0, not allow across login
         */
        if (SystemConfig.i("across.login", 0) == 0 && getId() > 0) {
            int prefix = Bean.toInt(Module.home.get("user_prefix"));
            if ((getId() & prefix) != prefix) {
                /**
                 * the prefix is different, do not allow to login
                 */
                return false;
            }
        }

        password = encrypt(password);
        return get("password") != null && get("password").equals(password);
    }

    /**
     * whether the user has been locked
     * 
     * @return boolean
     */
    public boolean isLocked() {
        return getInt("locked") > 0;
    }

    /**
     * Checks whether has the access token.
     * 
     * @param name
     *            the name of the access token
     * @return true, if has anyone
     */
    public boolean hasAccess(String... name) {
        if (this.getId() == 0L) {
            return true;
        }

        if (role == null) {
            getRole();
        }

        return role.hasAccess(name);
    }

    transient Roles role = null;

    /**
     * get the roles for the user
     * 
     * @return Roles
     */
    @SuppressWarnings("unchecked")
    public Roles getRole() {
        if (role == null) {
            List<Long> roles = (List<Long>) this.get("roles");
            role = new Roles(roles);
        }
        return role;
    }

    /**
     * set a role to a user with role id
     * 
     * @param rid
     */
    @SuppressWarnings("unchecked")
    public void setRole(long rid) {
        List<Long> roles = (List<Long>) this.get("roles");

        if (!roles.contains(rid)) {
            // add
            roles.add(rid);

            role = null;

            Bean.updateCollection(getId(), V.create("roles", roles).set("updated", System.currentTimeMillis()), User.class);
        }
    }

    /**
     * Removes the role.
     * 
     * @param rid
     *            the rid
     */
    @SuppressWarnings("unchecked")
    public void removeRole(String rid) {
        List<Long> roles = (List<Long>) this.get("roles");

        if (roles.contains(rid)) {
            // remove it
            roles.remove(rid);
            role = null;
            Bean.updateCollection(getId(), V.create("roles", roles).set("updated", System.currentTimeMillis()), User.class);
        }
    }

    /**
     * Removes the all roles.
     */
    public void removeAllRoles() {
        List<Long> roles = (List<Long>) this.get("roles");
        roles.clear();

        Bean.updateCollection(getId(), V.create("roles", roles).set("updated", System.currentTimeMillis()), User.class);
    }

    public void setSid(String sid) {
        set("sid", sid);

        Bean.updateCollection(getId(), V.create("sid", sid).set("updated", System.currentTimeMillis()), User.class);
    }

    public void setIp(String ip) {
        set("ip", ip);

        Bean.updateCollection(getId(), V.create("ip", ip).set("updated", System.currentTimeMillis()), User.class);
    }

    private static String encrypt(String passwd) {
        if (X.isEmpty(passwd)) {
            return X.EMPTY;
        }
        return UID.id(passwd);
    }

    /**
     * Load the users by the query
     * 
     * @param q
     *            the query of the condition
     * @param offset
     *            the start number
     * @param limit
     *            the number
     * @return Beans<User>
     */
    public static Beans<User> load(BasicDBObject q, int offset, int limit) {
        return Bean.load(q.append(X._ID, new BasicDBObject("$gt", 0)), new BasicDBObject("name", 1), offset, limit, User.class);
    }

    /**
     * Update the user with the V
     * 
     * @param v
     *            the values
     * @return int
     */
    public int update(V v) {
        return update(this.getId(), v);
    }

    /**
     * update the user by the values
     * 
     * @param id
     *            the user id
     * @param v
     *            the values
     * @return int, 0 no user updated
     */
    public static int update(long id, V v) {

        String passwd = (String) v.value("password");
        if (!X.isEmpty(passwd)) {
            passwd = encrypt(passwd);
            v.set("password", passwd, true);
        } else {
            v.remove("password");
        }
        return Bean.updateCollection(id, v.set("updated", System.currentTimeMillis()), User.class);
    }

    /***
     * replace all the roles for the user
     * 
     * @param roles
     */
    public void setRoles(List<Long> roles) {
        Bean.updateCollection(getId(), V.create("roles", roles).set("updated", System.currentTimeMillis()), User.class);
    }

    /**
     * record the login failure in database
     * 
     * @param ip
     *            the ip that the user come from
     * @return int the impacted of the data
     */
    public int failed(String ip) {
        set("failtimes", getInt("failtimes") + 1);

        Cache.remove("user://name/" + this.getString("name"));
        Cache.remove("user://id/" + this.getId());

        return Bean.update("id=?", new Object[] { getId() }, V.create("lastfailtime", System.currentTimeMillis()).set("lastfailip", ip).set("failtimes", getInt("failtimes")).set("updated",
                System.currentTimeMillis()), User.class);
    }

    /**
     * record the login failure, and record the user lock info
     * 
     * @param ip
     *            the ip that login come from
     * @param sid
     *            the session id
     * @param useragent
     *            the browser agent
     * @return int of the locked times
     */
    public int failed(String ip, String sid, String useragent) {
        set("failtimes", getInt("failtimes") + 1);

        return Lock.locked(getId(), sid, ip, useragent);
    }

    /**
     * record the logout info in database for the user
     * 
     * @return the int
     */
    public int logout() {
        return Bean.updateCollection(getId(), V.create("sid", X.EMPTY).set("updated", System.currentTimeMillis()), User.class);
    }

    /**
     * record login info in database for the user
     * 
     * @param sid
     *            the session id
     * @param ip
     *            the ip that the user come fram
     * @return the int
     */
    public int logined(String sid, String ip) {

        // update
        set("logintimes", getInt("logintimes") + 1);

        Lock.removed(getId(), sid);

        /**
         * cleanup the old sid for the old logined user
         */
        Bean.updateCollection(new BasicDBObject("sid", sid), V.create("sid", X.EMPTY), User.class);

        return Bean.updateCollection(getId(), V.create("lastlogintime", System.currentTimeMillis()).set("logintimes", getInt("logintimes")).set("ip", ip).set("failtimes", 0).set("locked", 0).set(
                "lockexpired", 0).set("sid", sid).set("updated", System.currentTimeMillis()), User.class);

    }

    /**
     * The {@code Lock} Class used to record login failure log, was used by
     * webgiisoo framework
     * 
     * @author joe
     *
     */
    @DBMapping(collection = "gi_userlock")
    public static class Lock extends Bean {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        public static int locked(long uid, String sid, String host, String useragent) {
            return Bean.insertCollection(V.create("uid", uid).set("sid", sid).set("host", host).set("useragent", useragent).set("created", System.currentTimeMillis()), Lock.class);
        }

        public static int removed(long uid) {
            return Bean.delete(new BasicDBObject("uid", uid), Lock.class);
        }

        public static int removed(long uid, String sid) {
            return Bean.delete(new BasicDBObject("uid", uid).append("sid", sid), Lock.class);
        }

        public static List<Lock> load(long uid, long time) {
            Beans<Lock> bs = Bean.load(new BasicDBObject("uid", uid).append("created", new BasicDBObject("$gt", time)), new BasicDBObject("created", 1), 0, Integer.MAX_VALUE, Lock.class);
            return bs == null ? null : bs.getList();
        }

        public static List<Lock> loadBySid(long uid, long time, String sid) {
            Beans<Lock> bs = Bean.load(new BasicDBObject("uid", uid).append("created", new BasicDBObject("$gt", time)).append("sid", sid), new BasicDBObject("created", 1), 0, Integer.MAX_VALUE,
                    Lock.class);
            return bs == null ? null : bs.getList();
        }

        public static List<Lock> loadByHost(long uid, long time, String host) {
            Beans<Lock> bs = Bean.load(new BasicDBObject("uid", uid).append("created", new BasicDBObject("$gt", time)).append("host", host), new BasicDBObject("created", 1), 0, Integer.MAX_VALUE,
                    Lock.class);
            return bs == null ? null : bs.getList();
        }

        public long getUid() {
            return getLong("uid");
        }

        public long getCreated() {
            return getLong("created");
        }

        public String getSid() {
            return getString("sid");
        }

        public String getHost() {
            return getString("host");
        }

        public String getUseragent() {
            return getString("useragent");
        }

    }

    /**
     * Delete the user by ID
     * 
     * @param id
     *            the id of the user
     * @return int how many was deleted
     */
    public static int delete(long id) {
        return Bean.delete(new BasicDBObject(X._ID, id), User.class);
    }

    /**
     * check the database, if there is no "config.admin" user, then create the
     * "admin" user, with "admin" as password
     */
    public static void checkAndInit() {
        if (Bean.isConfigured()) {
            List<User> list = User.loadByAccess("access.config.admin");
            if (list == null || list.size() == 0) {
                User.create(V.create("id", 0L).set("name", "admin").set("password", "admin").set("title", "Admin"));
            }
        }
    }

    /**
     * test the user exists for the query
     * 
     * @param q
     *            the query
     * @return boolean
     */
    public static boolean exists(BasicDBObject q) {
        return Bean.exists(q, User.class);
    }

    /**
     * test the user exists for the id
     * 
     * @param id
     * @return boolean
     */
    public static boolean exists(long id) {
        return Bean.exists(new BasicDBObject(X._ID, id), User.class);
    }

}
