/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;

@DBMapping(table = "tblstat")
public class Stat extends Bean implements Comparable<Stat> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    protected String id;

    protected String date; // 日期
    protected String module; // 统计模块

    protected String[] f;

    protected long uid; // 访问权限或用户名
    protected float count; // 统计值， 总数或完成时间
    protected long updated; // 统计时间

    transient String fullname;
    transient String name;

    public long getUid() {
        return uid;
    }

    /**
     * Adds the.
     * 
     * @param count
     *            the count
     */
    public void add(float count) {
        this.count += count;
    }

    public void setDate(String date) {
        this.date = date;
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
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    /**
     * Gets the f.
     * 
     * @param i
     *            the i
     * @return the f
     */
    public String getF(int i) {
        if (f != null && i < f.length) {
            return f[i];
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
        if (fullname == null) {
            StringBuilder sb = new StringBuilder();
            for (int i : fs) {
                if (sb.length() > 0)
                    sb.append("_");
                sb.append(f[i]);
            }
            fullname = sb.toString();
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
        if (name == null) {
            StringBuilder sb = new StringBuilder();
            for (int i : fs) {
                if (sb.length() > 0)
                    sb.append("_");
                sb.append(f[i]);
            }
            name = sb.toString();
        }

        if (name == null) {
            if (fs.size() > 0) {
                name = f[fs.get(fs.size() - 1)];
                // if (fs.size() > 1) {
                // String parent = f[fs.get(fs.size() - 2)];
                // String n = Stats.get(module, parent);
                // if (n != null) {
                // name = n + name;
                // }
                // }
            } else {
                name = f[0];
            }

        }
        // return name;
        return this.getDisplayname();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new StringBuilder("Stat(").append(date).append(",").append(Bean.toString(f)).append(",").append(count).append(")").toString();
    }

    public String getDate() {
        return date;
    }

    public float getCount() {
        return count;
    }

    public long getUpdated() {
        return updated;
    }

    /**
     * Load.
     * 
     * @param w
     *            the w
     * @return the list
     */
    public final static List<Stat> load(W w) {
        // log.debug(w.where() + ", " + Bean.toString(w.args()));

        return Bean.load((String[]) null, w.where(), w.args(), w.orderby() == null ? "order by count desc" : w.orderby(), 0, -1, Stat.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Bean#load(java.sql.ResultSet)
     */
    @Override
    protected void load(ResultSet r) throws SQLException {
        id = r.getString("id");
        date = Long.toString(r.getLong("date"));
        module = r.getString("module");
        f = new String[5];
        for (int i = 0; i < f.length; i++) {
            f[i] = r.getString("f" + i);
        }

        uid = r.getLong("uid");
        count = r.getFloat("count");
        updated = r.getLong("updated");

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
        s.module = module;
        s.date = date;
        s.uid = uid;
        s.count = count;
        s.f = f;
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
    public static int insertOrUpdate(String module, long date, long uid, float count, String... f) {
        String id = UID.id(date, module, uid, Bean.toString(f));

        if (!Bean.exists("date=? and id=?", new Object[] { date, id }, Stat.class)) {
            V v = V.create("date", date).set("id", id).set("module", module).set("uid", uid).set("count", count).set("updated", System.currentTimeMillis());

            for (int i = 0; i < 5 && i < f.length; i++) {
                v.set("f" + i, f[i]);
            }

            return Bean.insert(v, Stat.class);

        } else {
            /**
             * only update if count > original
             */
            return Bean.update("date=? and id=? and count<?", new Object[] { date, id, count }, V.create("count", count).set("updated", System.currentTimeMillis()), Stat.class);
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

        int c = date.compareTo(o.date);
        if (c == 0) {
            if (count > o.count) {
                return 1;
            } else if (count < o.count) {
                return -1;
            }
            return 0;
        }

        return c;

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + date.hashCode();
        result = prime * result + Arrays.hashCode(f);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((module == null) ? 0 : module.hashCode());
        result = (int) (prime * result + uid);
        return result;
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
        if (date != other.date)
            return false;
        if (!Arrays.equals(f, other.f))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (module == null) {
            if (other.module != null)
                return false;
        } else if (!module.equals(other.module))
            return false;
        if (uid != other.uid)
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.bean.Bean#toJSON(net.sf.json.JSONObject)
     */
    @Override
    public boolean toJSON(JSONObject jo) {
        jo.put("id", id);
        jo.put("date", date);
        jo.put("module", module);

        jo.put("f.length", f == null ? 0 : f.length);
        if (f != null && f.length > 0) {
            for (int i = 0; i < f.length; i++) {
                jo.put("f" + i, f[i]);
            }
        }

        jo.put("uid", uid);
        jo.put("count", count);
        jo.put("updated", updated);

        jo.put("fullname", fullname);
        jo.put("name", name);

        return true;
    }

    public String getDisplayname() {
        if (convertor == null) {
            return fullname;
        } else {
            return convertor.displayName(f, fullname);
        }
    }

    protected void setConvertor(IConvertor name) {
        convertor = name;
    }

    transient private IConvertor convertor = null;

    public static interface IConvertor {
        public String displayName(String[] f, String name);
    }
}
