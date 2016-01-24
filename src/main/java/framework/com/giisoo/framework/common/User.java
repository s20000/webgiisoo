/*

 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.*;
import java.util.*;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.*;
import com.giisoo.core.cache.Cache;
import com.giisoo.core.conf.SystemConfig;
import com.giisoo.framework.common.Company.Department;
import com.giisoo.framework.web.Module;
import com.mongodb.BasicDBObject;

/**
 * User
 * 
 * @author yjiang
 * 
 */
@DBMapping(table = "tbluser")
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

    public Company getCompany_obj() {
        if (!this.containsKey("company_obj")) {
            Company c = Company.load(this.getInt("company"));
            this.set("company_obj", c);
        }
        return (Company) this.get("company_obj");
    }

    public Department getDepartment_obj() {
        if (!this.containsKey("department_obj")) {
            Department c = Department.load(this.getLong("department"));
            this.set("department_obj", c);
        }
        return (Department) this.get("department_obj");
    }

    /**
     * Checks if is role.
     * 
     * @param r
     *            the r
     * @return true, if is role
     */
    public boolean isRole(Role r) {
        getRoles();

        return roles.contains(r.getId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new StringBuilder("User:[").append(getId()).append(",").append(get("name")).append("]").toString();
    }

    /**
     * Creates the.
     * 
     * @param v
     *            the v
     * @return the int
     */
    public static long create(V v) {

        for (int i = 0; i < v.size(); i++) {
            if ("password".equals(v.name(i))) {
                String password = (String) v.value(i);
                v.set(i, encrypt(password));
            }
        }

        long id = UID.next("user.id");

        while (Bean.exists("id=?", new Object[] { id }, User.class)) {
            id = UID.next("user.id");
        }

        if (Bean.insert(v.set("id", id).set("created", System.currentTimeMillis()).set("updated", System.currentTimeMillis()), User.class) > 0) {

            return id;
        }

        return -1;
    }

    /**
     * create a user from the jo
     * 
     * @deprecated
     * @param jo
     * @return int
     */
    public static int copy(JSONObject jo) {

        Connection c = null;
        PreparedStatement stat = null;
        ResultSet r = null;
        try {
            c = Bean.getConnection();

            stat = c.prepareStatement("select * from tbluser limit 1");

            r = stat.executeQuery();
            ResultSetMetaData m = r.getMetaData();

            V v = V.create();
            for (int i = 0; i < m.getColumnCount(); i++) {
                String name = m.getColumnName(i + 1);
                if (jo.containsKey(name)) {
                    v.set(name, jo.get(name));
                }
            }

            return Bean.insert(v, User.class);

        } catch (Exception e) {
            log.error(jo.toString(), e);
        } finally {
            Bean.close(r, stat, c);
        }

        return 0;
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

        if (Bean.exists("name=? and locked=0 and remote=0", new String[] { name }, User.class)) {
            /**
             * exists, create failded
             */
            throw new giException(-1, "the name exists");
        }

        long id = UID.next("user.id");
        while (Bean.exists("id=?", new Object[] { id }, User.class)) {
            id = UID.next("user.id");
        }

        password = encrypt(password);
        V v = V.create("id", id).set("name", name).set("password", password, true).set("created", System.currentTimeMillis()).set("updated", System.currentTimeMillis());
        v.copy(jo, "company", "title", "department", "address", "email", "nickname", "description", "special", "certid").copyLong(jo, "total");

        if (Bean.insert(v, User.class) > 0) {

            return id;
        }

        throw new giException(-1, "unknown error");
    }

    /**
     * Load.
     * 
     * @param name
     *            the name
     * @param password
     *            the password
     * @return the user
     */
    public static User load(String name, String password) {

        password = encrypt(password);

        log.debug("name=" + name + ", passwd=" + password);
        // System.out.println("name=" + name + ", passwd=" + password);

        return Bean.load("tbluser", "name=? and password=? and deleted=0 and remote=0", new String[] { name, password }, User.class);

    }

    public boolean isDeleted() {
        return getInt("deleted") == 1;
    }

    public long getId() {
        return this.getLong("id");
    }

    /**
     * Load.
     * 
     * @param name
     *            the name
     * @return the user
     */
    public static User load(String name) {
        String uid = "user://name/" + name;
        User u1 = (User) Cache.get(uid);
        if (u1 != null) {
            return u1;
        }

        List<User> list = Bean.load("tbluser", null, "name=?", new String[] { name }, User.class);

        if (list != null && list.size() > 0) {
            for (User u : list) {

                /**
                 * if the user has been locked, then not allow to login
                 */
                if (u.isLocked() || u.isDeleted())
                    continue;

                /**
                 * id == 0: admin; across.login ==0, not allow across login
                 */
                if (SystemConfig.i("across.login", 0) == 0 && u.getId() > 0) {
                    int prefix = Bean.toInt(Module.home.get("user_prefix"));
                    if ((u.getId() & prefix) != prefix) {
                        /**
                         * the prefix is different, do not allow to login
                         */
                        continue;
                    }
                }

                u.setExpired(60);
                Cache.set(uid, u);
                return u;
            }
        }

        return null;
    }

    /**
     * Load by id.
     * 
     * @param id
     *            the id
     * @return the user
     */
    public static User loadById(long id) {
        String uid = "user://id/" + id;
        User u = (User) Cache.get(uid);
        if (u != null && !u.expired()) {
            return u;
        }

        u = Bean.load("id=?", new Object[] { id }, User.class);
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

    private void cleanup() {
        String uid = "user://id/" + getId();
        Cache.remove(uid);
    }

    /**
     * Load by access.
     * 
     * @param access
     *            the access
     * @return the list
     */
    public static List<User> loadByAccess(String access) {
        return Bean.load("tbluser", null, "id in (select uid from tbluserrole where rid in (select rid from tblroleaccess where name=?)) and deleted=0 and locked=0", new Object[] { access },
                User.class);
    }

    public static List<User> loadByAccess(String access, W w) {
        if (w == null) {
            w = W.create();
        }
        w.set("id in (select uid from tbluserrole where rid in (select rid from tblroleaccess where name=?)) and deleted=0 and locked=0", access);
        return Bean.load("tbluser", null, w.where(), w.args(), User.class);
    }

    /**
     * Validate.
     * 
     * @param password
     *            the password
     * @return true, if successful
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
     * Checks for access.
     * 
     * @param name
     *            the name
     * @return true, if successful
     */
    public boolean hasAccess(String... name) {
        if (this.getId() == 0) {
            return true;
        }

        if (role == null) {
            getRoles();
            role = new Roles(roles);
        }

        return role.hasAccess(name);
    }

    transient Roles role = null;

    public Roles getRole() {
        if (role == null) {
            getRoles();
            role = new Roles(roles);
        }
        return role;
    }

    transient List<Integer> roles = null;

    public List<Integer> getRoles() {
        if (roles == null) {
            roles = Bean.loadList("tbluserrole", "rid", "uid=?", new Long[] { getId() }, Integer.class, null);

            if (roles == null) {
                roles = new ArrayList<Integer>();
            }
        }

        return roles;
    }

    /**
     * set a role to a user with role id
     * 
     * @param rid
     */
    public void setRole(int rid) {
        getRoles();

        if (!roles.contains(rid)) {
            // add
            Bean.insert("tbluserrole", V.create("uid", getId()).set("rid", rid), null);
            roles.add(rid);

            role = null;

            Bean.update("id=?", new Object[] { getId() }, V.create("updated", System.currentTimeMillis()), User.class);
        }
    }

    /**
     * Removes the role.
     * 
     * @param rid
     *            the rid
     */
    public void removeRole(String rid) {
        getRoles();

        if (roles.contains(rid)) {
            // remove it
            Bean.delete("tbluserrole", "uid=? and rid=?", new Object[] { getId(), rid }, null);
            roles.remove(rid);

            Bean.update("id=?", new Object[] { getId() }, V.create("updated", System.currentTimeMillis()), User.class);
        }
    }

    /**
     * Removes the all roles.
     */
    public void removeAllRoles() {
        getRoles();
        roles.clear();
        Bean.delete("tbluserrole", "uid=?", new Object[] { getId() }, null);

        Bean.update("id=?", new Object[] { getId() }, V.create("updated", System.currentTimeMillis()), User.class);
    }

    public void setSid(String sid) {
        set("sid", sid);

        Bean.update("id=?", new Object[] { getId() }, V.create("sid", sid).set("updated", System.currentTimeMillis()), User.class);
    }

    public void setIp(String ip) {
        set("ip", ip);

        Bean.update("id=?", new Object[] { getId() }, V.create("ip", ip).set("updated", System.currentTimeMillis()), User.class);
    }

    private static String encrypt(String passwd) {
        if (X.isEmpty(passwd)) {
            return X.EMPTY;
        }
        return UID.id(passwd);
    }

    /**
     * Load by id.
     * 
     * @param certid
     *            the certid
     * @return the user
     */
    public static User loadById(String certid) {
        return Bean.load("certid=?", new Object[] { certid }, User.class);
    }

    /**
     * Load by refer.
     * 
     * @param refer
     *            the refer
     * @return true, if successful
     */
    public boolean loadByRefer(JSONObject refer) {
        return Bean.load("tbluser", "id=?", new Object[] { getId() }, this);
    }

    /**
     * To refer.
     * 
     * @param refer
     *            the refer
     * @return true, if successful
     */
    public boolean toRefer(JSONObject refer) {
        if (getId() > 0) {
            refer.put("id", getId());

            return true;
        }
        return false;
    }

    /**
     * Load.
     * 
     * @param w
     *            the w
     * @param rank
     *            the rank
     * @param offset
     *            the offset
     * @param limit
     *            the limit
     * @return the beans
     */
    public static Beans<User> load(W w, int rank, int offset, int limit) {
        if (w == null) {
            w = W.create();
        }

        if (rank >= 0) {
            w.and("id", 0, W.OP_GT).and("rank", rank);
            return Bean.load(w.where(), w.args(), w == null || X.isEmpty(w.orderby()) ? "order by created desc" : w.orderby(), offset, limit, User.class);
        } else {
            w.and("id", 0, W.OP_GT);
            return Bean.load(w.where(), w.args(), X.isEmpty(w.orderby()) ? "order by name" : w.orderby(), offset, limit, User.class);
        }
    }

    /**
     * Exists.
     * 
     * @param w
     *            the w
     * @return true, if successful
     */
    public static boolean exists(W w) {
        return Bean.exists(w.where(), w.args(), User.class);
    }

    /**
     * Load.
     * 
     * @param w
     *            the w
     * @param offset
     *            the offset
     * @param limit
     *            the limit
     * @return the beans
     */
    public static Beans<User> load(W w, int offset, int limit) {
        if (w == null) {
            w = W.create();
        }

        w.and("id", 0, W.OP_GT);

        return Bean.load(w.where(), w.args(), X.isEmpty(w.orderby()) ? "order by name" : w.orderby(), offset, limit, User.class);
    }

    /**
     * Update.
     * 
     * @param v
     *            the v
     * @return the int
     */
    public int update(V v) {
        int len = v.size();
        for (int i = 0; i < len; i++) {
            String name = v.name(i);
            if ("password".equals(name)) {
                String passwd = (String) v.value(i);
                if (!"".equals(passwd)) {
                    passwd = encrypt(passwd);
                    v.set("password", passwd, true);
                } else {
                    v.remove(i);
                }
                break;
            }
        }
        int i = Bean.update("id=?", new Object[] { getId() }, v.set("updated", System.currentTimeMillis()), User.class);
        if (i > 0) {
            cleanup();
        }
        return i;
    }

    public static int update(long id, V v) {
        int len = v.size();
        for (int i = 0; i < len; i++) {
            String name = v.name(i);
            if ("password".equals(name)) {
                String passwd = (String) v.value(i);
                if (!"".equals(passwd)) {
                    passwd = encrypt(passwd);
                    v.set("password", passwd, true);
                } else {
                    v.remove(i);
                }
                break;
            }
        }
        return Bean.update("id=?", new Object[] { id }, v.set("updated", System.currentTimeMillis()), User.class);
    }

    public void setRoles(String[] roles) {
        /**
         * remove all
         */
        if (roles != null) {
            Bean.delete("tbluserrole", "uid=?", new Object[] { getId() }, null);

            for (String r : roles) {
                int rid = Bean.toInt(r, -1);
                if (rid >= 0) {
                    Bean.insert("tbluserrole", V.create("uid", getId()).set("rid", rid), null);
                }
            }

            Bean.update("id=?", new Object[] { getId() }, V.create("updated", System.currentTimeMillis()), User.class);
        }
    }

    /**
     * Check free.
     * 
     * @param uid
     *            the uid
     * @return the long
     */
    public static long checkFree(long uid) {

        Connection c = null;
        PreparedStatement stat = null;
        ResultSet r = null;
        try {
            c = Bean.getConnection();
            stat = c.prepareStatement("select total from tbluser where id=?");
            stat.setLong(1, uid);
            r = stat.executeQuery();
            long total = -1;
            if (r.next()) {
                total = r.getLong("total");
            }
            if (total < 0)
                return 0;
            r.close();
            stat.close();

            stat = c.prepareStatement("select sum(total) total from tblrepo where uid=?");
            stat.setLong(1, uid);
            r = stat.executeQuery();
            if (r.next()) {
                long t = r.getLong("total");
                return total - t;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            Bean.close(r, stat, c);
        }

        return 0;
    }

    /**
     * Failed.
     * 
     * @param ip
     *            the ip
     * @return the int
     */
    public int failed(String ip) {
        set("failtimes", getInt("failtimes") + 1);

        Cache.remove("user://name/" + this.getString("name"));
        Cache.remove("user://id/" + this.getId());

        return Bean.update("id=?", new Object[] { getId() }, V.create("lastfailtime", System.currentTimeMillis()).set("lastfailip", ip).set("failtimes", getInt("failtimes")).set("updated",
                System.currentTimeMillis()), User.class);
    }

    public int failed(String ip, String sid, String useragent) {
        set("failtimes", getInt("failtimes") + 1);

        return Lock.locked(getId(), sid, ip, useragent);
    }

    /**
     * Logout.
     * 
     * @return the int
     */
    public int logout() {
        return Bean.update("id=?", new Object[] { getId() }, V.create("sid", X.EMPTY).set("updated", System.currentTimeMillis()), User.class);
    }

    /**
     * Logined.
     * 
     * @param sid
     *            the sid
     * @param ip
     *            the ip
     * @return the int
     */
    public int logined(String sid, String ip) {

        // update
        set("logintimes", getInt("logintimes") + 1);

        Lock.removed(getId(), sid);

        /**
         * cleanup the old sid for the old logined user
         */
        Bean.update("sid=?", new Object[] { sid }, V.create("sid", X.EMPTY), User.class);

        return Bean.update("id=?", new Object[] { getId() }, V.create("lastlogintime", System.currentTimeMillis()).set("logintimes", getInt("logintimes")).set("ip", ip).set("failtimes", 0).set(
                "locked", 0).set("lockexpired", 0).set("sid", sid).set("updated", System.currentTimeMillis()), User.class);

    }

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

        // long uid;
        // long created;
        // String sid;
        // String host;
        // String useragent;

    }

    /**
     * List company.
     * 
     * @return the list
     */
    public static List<String> listCompany() {
        return Bean.loadList("tbluser", "distinct company", "id>0", null, String.class, null);
    }

    /**
     * Updated.
     * 
     * @return the long
     */
    public static long updated() {
        return Bean.getOne("max(updated)", null, null, null, 0, User.class);
    }

    /**
     * Delete.
     * 
     * @param id
     *            the id
     * @return the int
     */
    public static int delete(long id) {
        return Bean.delete("id=?", new Object[] { id }, User.class);
    }

}
