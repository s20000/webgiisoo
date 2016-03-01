package com.giisoo.utils.url;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.apache.commons.configuration.Configuration;

import com.giisoo.core.bean.*;
import com.giisoo.utils.base.*;

/**
 * The Class Url.
 * 
 * @author yjiang
 */
public class Url extends Bean implements Serializable, Comparable<Url> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The table name. */
    private String tableName;

    /** The url. */
    String url;

    /** The referer. */
    String referer;

    /** The cookie. */
    String cookie;

    /** The domain. */
    private String domain;

    /** The known. */
    boolean known;

    /** The block. */
    boolean block;

    /** The url_date. */
    long url_date;

    /** The web_date. */
    long web_date;

    /** The numofknown. */
    long numofknown;

    /** The numofparent. */
    long numofparent;

    /** The depth. */
    int depth;

    /** The charset. */
    String charset;

    /** The type. */
    String type;

    /** The tag. */
    String tag;

    /** The tolowcase. */
    boolean tolowcase = false;

    /** The attempt. */
    int attempt = 0;

    /** The is product. */
    boolean isProduct = false;

    /** The attachments. */
    Map<String, Object> attachments = new TreeMap<String, Object>();

    /** The headers. */
    Map<String, String> headers = new TreeMap<String, String>();

    /** The _lock. */
    static HashMap<String, Object> _lock = new HashMap<String, Object>();

    /** The pre domain. */
    static List<Integer> preDomain = new ArrayList<Integer>(10);

    /** The host. */
    transient String host;

    /** The port. */
    transient int port;

    /** The path. */
    transient String path;

    /** The request. */
    transient String request;

    /** The query. */
    transient String query;

    /**
     * get attempt.
     * 
     * @return the int
     */
    public int attempt() {
        return attempt;
    }

    /**
     * setting attempt.
     * 
     * @param i
     *            the i
     * @return the url
     */
    public Url attempt(int i) {
        attempt = i;
        return this;
    }

    /**
     * setting an attachment.
     * 
     * @param key
     *            the key
     * @param obj
     *            the obj
     * @return the url
     */
    public Url attachment(String key, Object obj) {
        attachments.put(key, obj);
        return this;
    }

    /**
     * get an attachment by key.
     * 
     * @param key
     *            the key
     * @return the object
     */
    public Object attachment(String key) {
        return attachments.get(key);
    }

    /**
     * Id.
     * 
     * @param url
     *            the url
     * @return the string
     * @deprecated return a unique id for a url
     */
    public static String id(String url) {
        if (url != null) {
            // return ID.id(url);
            int len = url.length();
            if (len > 512) {
                StringBuilder sb = new StringBuilder().append(url.hashCode());
                int i = 0;
                while (i < len) {
                    sb.append(".").append(url.substring(i, Math.min(len, i + 512)).hashCode());
                    i += 512;
                }

                return sb.toString();
            } else {
                return url;
            }
        }

        return url;
    }

    /**
     * is a product url.
     * 
     * @return true, if is product
     */
    public boolean isProduct() {
        return isProduct;
    }

    /**
     * setting the url is a product url.
     * 
     * @param is
     *            the is
     */
    public void isProduct(boolean is) {
        isProduct = is;
    }

    /**
     * Lock.
     * 
     * @param domain
     *            the domain
     * @return the object
     */
    private static Object lock(String domain) {
        synchronized (_lock) {
            Object o = _lock.get(domain);
            if (o == null) {
                o = new Object();
                _lock.put(domain, o);
            }

            return o;
        }
    }

    /**
     * Instantiates a new url.
     */
    public Url() {
    }

    /** The Constant unWantedUrl. */
    public final static Url unWantedUrl = new Url("http://w.w");

    /**
     * get the type of the url.
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * set the url type.
     * 
     * @param type
     *            the new type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the tag.
     * 
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Sets the tag.
     * 
     * @param tag
     *            the new tag
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Gets the cookie.
     * 
     * @return the cookie
     */
    public String getCookie() {
        return cookie;
    }

    /**
     * Sets the cookie.
     * 
     * @param cookie
     *            the new cookie
     */
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    /**
     * Gets the referer.
     * 
     * @return the referer
     */
    public String getReferer() {
        return referer;
    }

    /**
     * Sets the referer.
     * 
     * @param referer
     *            the new referer
     */
    public void setReferer(String referer) {
        this.referer = referer;
    }

    /**
     * get the query string in the url.
     * 
     * @return the query
     */
    public String getQuery() {
        if (query == null) {
            int len = url.length();
            // skip http://../, assume at least 10 char
            for (int i = 10; i < len; i++) {
                char c = url.charAt(i);
                if (c == '?' || c == '&' || c == '#') {
                    query = url.substring(i);
                    break;
                }
            }
        }

        return query;
    }

    /**
     * get the value in the query string by name.
     * 
     * @param name
     *            the name
     * @return the value
     */
    public String getValue(String name) {
        getQuery();

        if (query != null) {
            String q = query.toLowerCase();
            // log.debug(q + "==>" + name );
            int i = q.indexOf(name + "=");
            if (i > -1) {
                int j = q.indexOf("&", i);
                if (j > -1) {
                    return query.substring(i + name.length() + 1, j);
                } else {
                    return query.substring(i + name.length() + 1);
                }
            }
        }

        return null;

    }

    /**
     * Gets the numofparent.
     * 
     * @return the numofparent
     */
    public long getNumofparent() {
        return numofparent;
    }

    /**
     * Sets the numofparent.
     * 
     * @param numofparent
     *            the new numofparent
     */
    public void setNumofparent(long numofparent) {
        this.numofparent = numofparent;
    }

    /**
     * Gets the depth.
     * 
     * @return the depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Sets the depth.
     * 
     * @param depth
     *            the new depth
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Sets the domain.
     * 
     * @param domain
     *            the new domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
        this.tableName = table(domain);
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
        return obj instanceof Url && url.equals(((Url) obj).url);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return url.hashCode();
    }

    /**
     * get the request path string.
     * 
     * @return the request
     */
    public String getRequest() {
        if (request == null) {
            StringBuilder sb = new StringBuilder("http://");
            sb.append(getHost());
            if (getPort() != 80) {
                sb.append(":").append(port);
            }

            sb.append(getPath());

            request = sb.toString();
        }

        return request;

    }

    /**
     * Gets the url_date.
     * 
     * @return the url_date
     */
    public long getUrl_date() {
        return url_date;
    }

    /**
     * Sets the url_date.
     * 
     * @param urlDate
     *            the new url_date
     */
    public void setUrl_date(long urlDate) {
        url_date = urlDate;
    }

    /**
     * Checks if is known.
     * 
     * @return true, if is known
     */
    public boolean isKnown() {
        return known;
    }

    /**
     * Sets the known.
     * 
     * @param known
     *            the new known
     */
    public void setKnown(boolean known) {
        this.known = known;
    }

    /**
     * Checks if is block.
     * 
     * @return true, if is block
     * @deprecated
     */
    public boolean isBlock() {
        return block;
    }

    /**
     * Sets the block.
     * 
     * @param block
     *            the new block
     * @deprecated
     */
    public void setBlock(boolean block) {
        this.block = block;
    }

    /**
     * Instantiates a new url.
     * 
     * @param url
     *            the url
     */
    public Url(String url) {
        this.url = url;
        // getURL();
        domain = getDomain();
        tableName = table(domain);
    }

    /**
     * format the url as a better.
     */
    public void format() {
        String q = getQuery();
        if (q != null) {
            q = q.substring(1);
            String[] qs = q.split("&");
            ArrayList<String> list = new ArrayList<String>();
            for (String s : qs) {
                if (!list.contains(s)) {
                    list.add(s);
                }
            }

            Collections.sort(list);
            StringBuilder sb = new StringBuilder();
            for (String s : list) {
                if (sb.length() > 0)
                    sb.append("&");
                sb.append(s);
            }

            q = sb.toString();
        }

        getPath();
        int len = path.length();

        Stack<String> st = new Stack<String>();
        int s = 0;
        for (int i = 0; i < len; i++) {
            char c = path.charAt(i);
            if (c == '/') {
                if (s != i) {
                    st.add(path.substring(s, i));
                    s = i;
                }
            }
        }

        if (s != len) {
            st.add(path.substring(s, len));
        }

        Stack<String> f = new Stack<String>();
        int c = 0;
        while (!st.isEmpty()) {
            String s1 = st.pop();
            if (s1.equals("/.")) {
                continue;
            } else if (s1.equals("/..")) {
                c++;
                continue;
            } else if (s1.equals("/")) {
                if (!f.isEmpty()) {
                    continue;
                }
            }

            if (c <= 0) {
                f.add(s1);
            } else {
                c--;
            }
        }

        StringBuilder sb = new StringBuilder();
        while (!f.isEmpty()) {
            String s1 = f.pop();
            sb.append(s1);
        }

        path = sb.toString();

        sb = new StringBuilder("http://").append(getHost());
        if (getPort() != 80) {
            sb.append(":").append(port);
        }

        sb.append(path);
        if (query != null && query.length() > 0) {
            sb.append(query.charAt(0));
            if (q != null) {
                sb.append(q);
            }
        }

        url = sb.toString();

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return url;
    }

    /**
     * Gets the path.
     * 
     * @return the path
     */
    public String getPath() {
        if (path == null) {
            int len = url.length();
            StringBuilder sb = new StringBuilder();
            int start = 0;
            for (int i = 10; i < len; i++) {
                char c = url.charAt(i);
                if (c == '/' && start == 0) {
                    start = 1;
                    sb.append("/");
                } else if (c == '#' || c == '&' || c == '?') {
                    break;
                } else if (start == 1) {
                    sb.append(c);
                }
            }

            path = sb.toString();

        }

        return path;

    }

    /**
     * get the host name of the url.
     * 
     * @param url
     *            the url
     * @return the string
     */
    public static String host(String url) {
        if (url == null) {
            return null;
        }

        int len = url.length();
        StringBuilder sb = new StringBuilder();
        int start = 0;
        for (int i = 2; i < len; i++) {
            char c = url.charAt(i);
            if (c == ':' && start == 0) {
                i += 2;
                start = 1;
                continue;
            } else if (c == ':' || c == '/' || c == '&' || c == '?' || c == '#') {
                break;
            } else if (start == 1) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * get the domain name of the url.
     * 
     * @param url
     *            the url
     * @return the string
     */
    public static String domain(String url) {

        String host = host(url);
        if (host == null) {
            return null;
        }

        String[] ss = host.split("\\.");
        int i = 0;
        for (i = ss.length - 1; i >= 0; i--) {
            if (ss[i] != null && preDomain.contains(ss[i].hashCode())) {
                continue;
            } else {
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (; i < ss.length; i++) {
            if (sb.length() > 0) {
                sb.append(".");
            }

            sb.append(ss[i]);
        }

        return sb.toString();
    }

    /**
     * get the domain name of the url.
     * 
     * @return the domain
     */
    public String getDomain() {
        if (domain == null) {
            domain = domain(url);
        }

        return domain;

    }

    /**
     * Gets the url.
     * 
     * @return the url
     */
    public URL getURL() {
        try {
            return new URL(url);
        } catch (Exception e) {
            log.error("url=" + url, e);
        }

        return null;
    }

    /**
     * Gets the url.
     * 
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url.
     * 
     * @param url
     *            the new url
     */
    public void setUrl(String url) {
        this.url = url;
        host = null;
        request = null;
    }

    /**
     * Checks if is mongo.
     * 
     * @return true, if is mongo
     */
    public static boolean isMongo() {
        return Bean.hasDB("temp");
    }

    /**
     * Update.
     */
    public void update() {
        V v = V.create();

        // if (domain != null)
        // v.put("domain", domain);

        if (known)
            v.set("known", known);
        if (block)
            v.set("block", block);

        // v.put("depth", depth);

        if (url_date > 0)
            v.set("url_date", url_date);
        if (web_date > 0)
            v.set("web_date", web_date);

        if (numofknown > 0)
            v.set("numofknown", numofknown);

        if (v.size() > 0) {
            update(tableName, "hashcode=? and url=?", new Object[] { url.hashCode(), url }, v, null);
        }
    }

    /**
     * Table.
     * 
     * @param domain
     *            the domain
     * @return the string
     */
    private static String table(String domain) {
        if (domain == null)
            return null;
        return "tblurl_" + domain.replace('.', '_').replace('-', '_');
    }

    /**
     * Inits the.
     * 
     * @param conf
     *            the conf
     */
    public static void init(Configuration conf) {
        if (preDomain.size() == 0) {
            preDomain.add("com".hashCode());
            preDomain.add("cn".hashCode());
            preDomain.add("cc".hashCode());
            preDomain.add("org".hashCode());
            preDomain.add("net".hashCode());
            preDomain.add("tv".hashCode());
            preDomain.add("mobi".hashCode());
            preDomain.add("jp".hashCode());
            preDomain.add("me".hashCode());
        }
    }

    /**
     * Gets the host.
     * 
     * @return the host
     */
    public String getHost() {
        if (host == null) {
            host = host(url);
        }

        return host;
    }

    /**
     * Gets the port.
     * 
     * @return the port
     */
    public int getPort() {
        if (port < 10) {
            int len = url.length();
            StringBuilder sb = new StringBuilder();
            int start = 0;
            for (int i = 10; i < len; i++) {
                char c = url.charAt(i);
                if (c == '/' || c == '#' || c == '?' || c == '&') {
                    break;
                } else if (c == ':') {
                    start = 1;
                } else if (start == 1) {
                    sb.append(c);
                }
            }

            if (start == 0) {
                port = 80;
            } else {
                try {
                    port = Integer.parseInt(sb.toString());
                } catch (Exception e) {
                    port = 80;
                }
            }
        }

        return port;

    }

    /**
     * Checks if is java script.
     * 
     * @return true, if is java script
     */
    public boolean isJavaScript() {
        return url.endsWith(".js");
    }

    /**
     * Valid.
     * 
     * @return true, if successful
     */
    public boolean valid() {
        if (url == null || url.length() == 0)
            return false;
        if (!url.startsWith("http://"))
            return false;
        if (url.length() >= 4095)
            return false;

        if (url.endsWith(".jpg") || url.endsWith(".JPG") || url.endsWith(".png") || url.endsWith(".PNG") || url.endsWith(".gif") || url.endsWith(".GIF")) {
            return false;
        }

        return true;
    }

    /**
     * Sets the charset.
     * 
     * @param charset
     *            the charset
     * @return the url
     */
    public Url setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Gets the charset.
     * 
     * @return the charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Encode url.
     * 
     * @param url
     *            the url
     * @return the string
     */
    public static String encodeUrl(String url) {
        return new String(Hex.encode(url.getBytes()));
    }

    /**
     * Decode url.
     * 
     * @param url
     *            the url
     * @return the string
     */
    public static String decodeUrl(String url) {
        return new String(Hex.decode(url));
    }

    /**
     * Sets the header.
     * 
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    /**
     * Gets the headers.
     * 
     * @return the headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Url o) {
        if (o == this)
            return 0;
        return url.compareTo(o.url);
    }

    /**
     * To lowcase.
     * 
     * @return true, if successful
     */
    public boolean toLowcase() {
        return tolowcase;
    }

    /**
     * Support parallel.
     * 
     * @return true, if successful
     */
    public boolean supportParallel() {
        return false;
    }

    /**
     * Checks if is gzip.
     * 
     * @return true, if is gzip
     */
    public boolean isGzip() {
        // TODO Auto-generated method stub
        return false;
    }
}
