/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.io.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.*;

import com.giisoo.core.bean.*;
import com.giisoo.framework.mdc.*;
import com.mongodb.BasicDBObject;

/**
 * repository of file system
 * 
 * @author yjiang
 * 
 */
@DBMapping(collection = "gi_repo")
public class Repo extends Bean {

    /**
   * 
   */
    private static final long serialVersionUID = 1L;

    static Log log = LogFactory.getLog(Repo.class);

    public static String ROOT;

    /**
     * Inits the.
     * 
     * @param conf
     *            the conf
     */
    public static void init(Configuration conf) {
        ROOT = conf.getString("repo.path", "/opt/repo/f1");
    }

    /**
     * List.
     * 
     * @param uid
     *            the uid
     * @param offset
     *            the offset
     * @param limit
     *            the limit
     * @return the beans
     */
    public static Beans<Entity> list(long uid, int offset, int limit) {
        return Bean.load(new BasicDBObject("uid", uid), new BasicDBObject("created", -1), offset, limit, Entity.class);
    }

    /**
     * List.
     * 
     * @param tag
     *            the tag
     * @param offset
     *            the offset
     * @param limit
     *            the limit
     * @return the beans
     */
    public static Beans<Entity> list(String tag, int offset, int limit) {
        return Bean.load(new BasicDBObject("tag", tag), new BasicDBObject("created", -1), offset, limit, Entity.class);
    }

    /**
     * store the inputstream data in repo
     * 
     * @param id
     * @param name
     * @param in
     * @return long
     * @throws IOException
     */
    public static long store(String id, String name, InputStream in) throws IOException {
        return store(X.EMPTY, id, name, X.EMPTY, 0, in.available(), in, -1, true, -1);
    }

    /**
     * Store.
     * 
     * @param folder
     *            the folder
     * @param id
     *            the id
     * @param name
     *            the name
     * @param tag
     *            the tag
     * @param position
     *            the position
     * @param total
     *            the total
     * @param in
     *            the in
     * @param expired
     *            the expired
     * @param share
     *            the share
     * @param uid
     *            the uid
     * @return the long
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long store(String folder, String id, String name, String tag, long position, long total, InputStream in, long expired, boolean share, long uid) throws IOException {
        Entity e = new Entity();
        e.set("folder", folder);
        e.set("name", name);
        e.set(X._ID, id);
        e.set("total", total);
        e.set("expired", expired);
        e.set("uid", uid);

        return e.store(tag, position, in, total, name, (byte) (share ? 0x01 : 0));
    }

    /**
     * Gets the id.
     * 
     * @param uri
     *            the uri
     * @return the id
     */
    public static String getId(String uri) {
        if (X.isEmpty(uri))
            return null;

        String id = uri;
        int i = id.indexOf("/");
        while (i >= 0) {
            if (i > 0) {
                String s = id.substring(0, i);
                if (s.equals("repo") || s.equals("download")) {
                    id = id.substring(i + 1);
                    i = id.indexOf("/");
                    if (i > 0) {
                        id = id.substring(0, i);
                    }
                } else {
                    id = s;
                    break;
                }
            } else {
                id = id.substring(1);
            }

            i = id.indexOf("/");
        }

        log.info("loadbyuri: uri=" + uri + ", id=" + id);
        return id;
    }

    /**
     * Load by uri.
     * 
     * @param uri
     *            the uri
     * @return the entity
     */
    public static Entity loadByUri(String uri) {
        String id = getId(uri);
        if (!X.isEmpty(id)) {
            return load(id);
        }
        return null;
    }

    public static Entity load(String folder, String id, File f) {
        if (f.exists()) {
            Entity e = null;
            if (!X.isEmpty(id)) {
                if (folder != null) {
                    e = Bean.load(new BasicDBObject("folder", folder).append(X._ID, id), Entity.class);
                } else {
                    e = Bean.load(new BasicDBObject(X._ID, id), Entity.class);
                }
            }

            if (e == null) {
                try {
                    InputStream in = new FileInputStream(f);

                    /**
                     * will not close the inputstream
                     */
                    return Entity.create(in);

                } catch (Exception e1) {
                    log.error("load: id=" + id, e1);
                }
            }

            return e;
        } else {
            try {
                log.warn("not find the file: " + f.getCanonicalPath() + ", id=" + id);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Load.
     * 
     * @param id
     *            the id
     * @return the entity
     */
    public static Entity load(String id) {
        return load(null, id);
    }

    public static void delete(String folder, String id) {
        File f = new File(path(folder, id));

        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * Load.
     * 
     * @param folder
     *            the folder
     * @param id
     *            the id
     * @return the entity
     */
    public static Entity load(String folder, String id) {
        String path = path(folder, id);
        return load(folder, id, new File(path));
    }

    /**
     * Delete.
     * 
     * @param id
     *            the id
     * @return the int
     */
    public static int delete(String id) {
        /**
         * delete the file in the repo
         */
        Repo.delete(null, id);

        /**
         * delete the info in table
         */
        Bean.delete(new BasicDBObject(X._ID, id), Entity.class);

        return 1;
    }

    /**
     * entity of repo
     * 
     * @author yjiang
     * 
     */
    @DBMapping(collection = "gi_repo")
    public static class Entity extends Bean {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        // private byte version = 1;
        //
        // public long pos;
        // public int flag;
        // public long expired;
        // public long total;
        // public long uid;
        // public String id;
        // public String name;
        // public long created;
        // public String folder;
        // String memo;

        private transient InputStream in;
        private transient int headsize;

        public String getMemo() {
            return getString("memo");
        }

        public String getUrl() {
            return "/repo/" + getId() + "/" + getName();
        }

        public byte getVersion() {
            return (byte) getInt("version");
        }

        public long getPos() {
            return getLong("pos");
        }

        public int getFlag() {
            return getInt("flag");
        }

        public long getExpired() {
            return getLong("expired");
        }

        public long getTotal() {
            return getLong("total");
        }

        // public long getUid() {
        // return getLong("uid");
        // }
        //
        public String getId() {
            return this.getString(X._ID);
        }

        public String getFiletype() {
            String name = this.getName();
            if (name != null) {
                int i = name.lastIndexOf(".");
                if (i > 0) {
                    return name.substring(i + 1);
                }
            }
            return X.EMPTY;
        }

        public String getName() {
            return getString("name");
        }

        public long getCreated() {
            return getLong("created");
        }

        transient User user;

        public User getUser() {
            if (user == null) {
                user = User.loadById(this.getLong("uid"));
            }
            return user;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return new StringBuilder("Repo.Entity[").append(getId()).append(", name=").append(getName()).append(", pos:").append(getPos()).append(", total:").append(getTotal()).append("]").toString();
        }

        /**
         * Delete.
         */
        public void delete() {
            Repo.delete(getId());
        }

        private long store(String tag, long position, InputStream in, long total, String name, int flag) throws IOException {
            File f = new File(path(getFolder(), getId()));

            if (f.exists()) {
                InputStream tmp = null;
                try {
                    tmp = new FileInputStream(f);
                    if (!load(tmp)) {// && (total != this.getTotal() ||
                                     // !name.equals(this.getName()))) {

                        log.error("file: " + f.getCanonicalPath());

                        /**
                         * this file is not original file
                         */
                        throw new IOException("same filename[" + getId() + "/" + this.getName() + "], but different size, old.total=" + this.getTotal() + ", new.total=" + total + ", old.name="
                                + this.getName() + ", new.name=" + name + ", ?" + (total != this.getTotal() || !name.equals(this.getName())));
                    }
                } finally {
                    close();
                }
            } else {
                f.getParentFile().mkdirs();
            }

            if (!f.exists() || total != this.getTotal()) {
                /**
                 * initialize the storage, otherwise append
                 */
                OutputStream out = null;
                try {
                    out = new FileOutputStream(f);
                    set("pos", in.available());

                    Response resp = new Response();
                    resp.writeLong(getPos());
                    resp.writeInt(flag);
                    resp.writeLong(getExpired());
                    resp.writeLong(total);
                    resp.writeInt((int) 0);
                    resp.writeString(getId());
                    resp.writeString(name);
                    byte[] bb = resp.getBytes();
                    resp = new Response();

                    resp.writeByte(getVersion());
                    resp.writeInt(bb.length);

                    resp.writeBytes(bb);
                    bb = resp.getBytes();
                    out.write(bb);
                    long pos = 0;
                    bb = new byte[4 * 1024];

                    int len = in.read(bb);
                    while (len > 0) {
                        out.write(bb, 0, len);
                        pos += len;
                        len = in.read(bb);
                    }

                    long pp = pos;
                    if (total > 0) {
                        while (pp < total) {
                            len = (int) Math.min(total - pp, bb.length);
                            out.write(bb, 0, len);
                            pp += len;
                        }
                    }

                    if (Bean.exists(new BasicDBObject(X._ID, getId()), Entity.class)) {
                        Bean.updateCollection(getId(), V.create("total", pp).set("tag", tag).set("expired", getExpired()), Entity.class);
                    } else {
                        Bean.insertCollection(V.create(X._ID, getId()).set("uid", 0).set("total", pp).set("tag", tag).set("expired", getExpired()).set("created", System.currentTimeMillis()).set(
                                "flag", flag).set("name", name), Entity.class);
                    }

                    /**
                     * check the free of the user
                     */
                    // long free = User.checkFree(getUid());
                    // if (free < 0) {
                    // throw new IOException("repo.no.space");
                    // }

                    log.debug("stored, id=" + this.getId() + ", pos=" + pos);
                    return pos;
                } catch (IOException e) {
                    Repo.delete(getId());

                    throw e;
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            log.error(e);
                        }
                    }

                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error(e);
                    }
                }

            } else {
                /**
                 * append
                 */
                RandomAccessFile raf = null;
                /**
                 * load head, and skip
                 */
                try {
                    raf = new RandomAccessFile(f, "rws");
                    byte[] bb = new byte[17]; // version(1) + head.length(4) +
                    // pos(8) + flag(4)
                    raf.read(bb);
                    Request req = new Request(bb, 0);

                    set("version", req.readByte());
                    int head = req.readInt();
                    set("pos", req.readLong());

                    if (getPos() >= position) {
                        raf.seek(head + 5 + position);

                        bb = new byte[4 * 1024];
                        int len = in.read(bb);
                        while (len > 0) {
                            raf.write(bb, 0, len);
                            position += len;
                            len = in.read(bb);
                        }

                        if (position > getPos()) {
                            Response resp = new Response();
                            resp.writeLong(position);
                            raf.seek(5);
                            raf.write(resp.getBytes());
                            set("pos", position);
                        }
                    }

                    return getPos();
                } finally {
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e) {
                            log.error(e);
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            log.error(e);
                        }
                    }
                }
            }

        }

        /**
         * get the inputstream of the repo Entity
         * 
         * @return InputStream
         * @throws IOException
         */
        public InputStream getInputStream() throws IOException {
            if (in == null) {
                File f = new File(path(getFolder(), getId()));

                if (f.exists()) {
                    try {
                        in = new FileInputStream(f);
                        load(in);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }

            return in;
        }

        private String getFolder() {
            return getString("folder");
        }

        /**
         * Close.
         */
        public synchronized void close() {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#finalize()
         */
        @Override
        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }

        private boolean load(InputStream in) {
            try {
                byte[] bb = new byte[1];
                in.read(bb);

                set("version", bb[0]);
                bb = new byte[4];
                in.read(bb);
                Request req = new Request(bb, 0);
                headsize = req.readInt();
                bb = new byte[headsize];
                in.read(bb);
                req = new Request(bb, 0);

                set("pos", req.readLong());
                set("flag", req.readInt());
                set("expired", req.readLong());
                set("total", req.readLong());
                set("uid", req.readInt());
                set("id", req.readString());
                set("name", req.readString());

                this.in = in;

                return true;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return false;
        }

        public boolean isShared() {
            return (getFlag() & 0x01) != 0;
        }

        private static Entity create(InputStream in) throws IOException {
            Entity e = new Entity();

            e.load(in);
            return e;
        }

        /**
         * Update.
         * 
         * @param v
         *            the v
         * @return the int
         */
        public int update(V v) {
            return Bean.updateCollection(getId(), v, Entity.class);
        }

        /**
         * Move to.
         * 
         * @param folder
         *            the folder
         */
        public void moveTo(String folder) {

            File f1 = new File(path(this.getFolder(), getId()));
            File f2 = new File(path(folder, getId()));
            if (f2.exists()) {
                f2.delete();
            } else {
                f2.getParentFile().mkdirs();
            }
            f1.renameTo(f2);

            Bean.updateCollection(getId(), V.create("folder", folder), Entity.class);

        }

        public void reset() {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            in = null;
        }
    }

    static private String path(String folder, String path) {
        long id = Math.abs(UID.hash(path));
        char p1 = (char) (id % 23 + 'a');
        char p2 = (char) (id % 19 + 'A');
        char p3 = (char) (id % 17 + 'a');
        char p4 = (char) (id % 13 + 'A');

        StringBuilder sb = new StringBuilder(ROOT);

        if (folder != null && "".equals(folder)) {
            sb.append("/").append(folder);
        }

        sb.append("/").append(p1).append("/").append(p2).append("/").append(p3).append("/").append(p4).append("/").append(id);
        return sb.toString();
    }

    public static void cleanup() {
        File f = new File(ROOT);

        File[] fs = f.listFiles();
        if (fs != null) {
            for (File f1 : fs) {
                delete(f1);
            }
        }

    }

    private static void delete(File f) {
        if (f.isFile()) {
            if (System.currentTimeMillis() - f.lastModified() > X.ADAY) {
                // check the file is fine?
                Entity e = Repo.load(null, null, f);
                if (e.getTotal() > e.getPos()) {
                    e.delete();
                }
            }
        } else if (f.isDirectory()) {
            File[] fs = f.listFiles();
            if (fs != null) {
                for (File f1 : fs) {
                    delete(f1);
                }
            }

            /**
             * delete the empty directory
             */
            fs = f.listFiles();
            if (fs == null || fs.length == 0) {
                f.delete();
            }

        }
    }

    public static Beans<Entity> load(BasicDBObject q, BasicDBObject order, int s, int n) {
        return Bean.load(q, order, s, n, Entity.class);
    }
}
