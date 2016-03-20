/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;
import com.mongodb.BasicDBObject;

@DBMapping(collection = "gi_stat")
public class Stat extends Bean implements Comparable<Stat> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    // protected String id;
    //
    // protected String date; // 日期
    // protected String module; // 统计模块
    //
    // protected String[] f;
    //
    // protected long uid; // 访问权限或用户名
    // protected float count; // 统计值， 总数或完成时间

    // protected long updated; // 统计时间
    //
    // transient String fullname;
    // transient String name;

    public long getUid() {
        return this.getLong("uid");
    }

    /**
     * Adds the.
     * 
     * @param count
     *            the count
     */
    public void add(float count) {
        this.set("count", count + this.getInt("count"));
    }

    public void setDate(String date) {
        this.set("date", date);
    }

    /**
     * Sets the.
     * 
     * @param name
     *            the name
     * @param count
     *            the count
     */
    public void set(String name, float count) {
        this.set("name", name);
        this.set("count", count);
    }

    public String getName() {
        return this.getString("name");
    }

    public List<String> getF() {
        return (List<String>) this.get("f");
    }

    /**
     * Gets the f.
     * 
     * @param i
     *            the i
     * @return the f
     */
    public String getF(int i) {
        List<String> f = (List<String>) this.get("f");
        if (f != null && i < f.size()) {
            return f.get(i);
        }
        return X.EMPTY;
    }

    /**
     * Gets the fullname.
     * 
     * @param fs
     *            the fs
     * @return the fullname
     */
    public String getFullname(List<Integer> fs) {
        String fullname = this.getString("fullname");
        if (fullname == null) {
            StringBuilder sb = new StringBuilder();
            for (int i : fs) {
                if (sb.length() > 0)
                    sb.append("_");
                sb.append(getF(i));
            }
            fullname = sb.toString();
            this.set("fullname", fullname);
        }
        return fullname;
    }

    /**
     * Gets the name.
     * 
     * @param fs
     *            the fs
     * @return the name
     */
    public String getName(List<Integer> fs) {
        String name = this.getString("name");
        if (name == null) {
            StringBuilder sb = new StringBuilder();
            for (int i : fs) {
                if (sb.length() > 0)
                    sb.append("_");
                sb.append(getF(i));
            }
            name = sb.toString();
        }

        if (name == null) {
            if (fs.size() > 0) {
                name = getF(fs.get(fs.size() - 1));
                // if (fs.size() > 1) {
                // String parent = f[fs.get(fs.size() - 2)];
                // String n = Stats.get(module, parent);
                // if (n != null) {
                // name = n + name;
                // }
                // }
            } else {
                name = getF(0);
            }

        }
        // return name;
        return this.getDisplayname();
    }

    public String getDate() {
        return this.getString("date");
    }

    public float getCount() {
        return this.getFloat("count");
    }

    public long getUpdated() {
        return this.getLong("updated");
    }

    /**
     * create a stat item in memory
     * 
     * @param module
     * @param date
     * @param uid
     * @param count
     * @param f
     * @return Stat
     */
    public static Stat create(String module, String date, long uid, float count, String... f) {
        Stat s = new Stat();
        s.set("module", module);
        s.set("date", date);
        s.set("uid", uid);
        s.set("count", count);

        List<String> list = new ArrayList<String>();
        Collections.addAll(list, f);
        s.set("f", list);

        return s;
    }

    /**
     * Insert or update.
     * 
     * @param module
     *            the module
     * @param date
     *            the date
     * @param uid
     *            the uid
     * @param count
     *            the count
     * @param f
     *            the f
     * @return the int
     */
    public static int insertOrUpdate(String module, String date, long uid, float count, String... f) {
        String id = UID.id(date, module, uid, Bean.toString(f));

        if (!Bean.exists(new BasicDBObject("date", date).append("id", id), Stat.class)) {
            V v = V.create("date", date).set(X._ID, id).set("id", id).set("module", module).set("uid", uid).set("count", count).set("updated", System.currentTimeMillis());

            List<String> list = new ArrayList<String>();
            Collections.addAll(list, f);
            v.set("f", list);
            return Bean.insertCollection(v, Stat.class);

        } else {
            /**
             * only update if count > original
             */
            return Bean.updateCollection(new BasicDBObject("date", date).append("id", id).append("count", new BasicDBObject("$lt", count)), V.create("count", count).set("updated",
                    System.currentTimeMillis()), Stat.class);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Stat o) {
        if (this == o)
            return 0;

        int c = getDate().compareTo(o.getDate());
        if (c == 0) {
            if (getCount() > o.getCount()) {
                return 1;
            } else if (getCount() < o.getCount()) {
                return -1;
            }
            return 0;
        }
        return c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Stat other = (Stat) obj;
        if (getDate() != other.getDate())
            return false;
        if (!Arrays.equals(getF().toArray(), other.getF().toArray()))
            return false;
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId()))
            return false;
        if (getModule() == null) {
            if (other.getModule() != null)
                return false;
        } else if (!getModule().equals(other.getModule()))
            return false;
        if (this.getUid() != other.getUid())
            return false;
        return true;
    }

    public String getId() {
        return this.getString("id");
    }

    private Object getModule() {
        return this.getString("module");
    }

    public String getDisplayname() {
        if (convertor == null) {
            return this.getString("fullname");
        } else {
            return convertor.displayName(this.getF().toArray(new String[this.getF().size()]), this.getString("fullname"));
        }
    }

    protected void setConvertor(IConvertor name) {
        convertor = name;
    }

    transient private IConvertor convertor = null;

    public static interface IConvertor {
        public String displayName(String[] f, String name);
    }

    /**
     * 
     * @param count
     */
    public void setCount(float count) {
        this.set("count", count);
    }

    /**
     * 
     * @param q
     * @param order
     * @param s
     * @param n
     * @return Beans<Stat>
     */
    public static Beans<Stat> load(BasicDBObject q, BasicDBObject order, int s, int n) {
        return Bean.load(q, order, s, n, Stat.class);
    }

    /**
     * load the stat
     * 
     * @param q
     * @return Stat
     */
    public static Stat load(BasicDBObject q) {
        return Bean.load(q, Stat.class);
    }

    /**
     * 
     * @param module
     * @param date
     * @param uid
     * @return Stat
     */
    public static Stat load(String module, String date, long uid) {
        return load(new BasicDBObject("module", module).append("date", date).append("uid", uid));
    }

}
