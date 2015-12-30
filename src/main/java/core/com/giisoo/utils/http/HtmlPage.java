package com.giisoo.utils.http;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import com.giisoo.utils.url.*;

// TODO: Auto-generated Javadoc
/**
 * The Class HtmlPage.
 */
public class HtmlPage implements IPage {

  /** The log. */
  static Log log = LogFactory.getLog(HtmlPage.class);

  /** The response. */
  HttpResponse response;
  
  /** The context. */
  String context;
  
  /** The request. */
  Url request;
  
  /** The request filename. */
  String requestFilename; // only have the file name
  
  /** The absoluate path. */
  String absoluatePath; // start with /, only stripped the host and
  // http://
  /** The headers. */
  Header[] headers;
  
  /** The cached. */
  boolean cached;
  
  /** The pos. */
  int pos;
  
  /** The size. */
  int size;
  
  /** The cookie. */
  Cookie cookie = new Cookie();

  /**
   * To page.
   *
   * @param context the context
   * @param charset the charset
   * @return the i page
   */
  public static IPage toPage(String context, String charset) {
    HtmlPage p = new HtmlPage();
    p.context = context;
    p.pos = 0;
    if (context != null) {
      p.size = context.length();
    }
    return p;
  }

  /**
   * To page.
   *
   * @param context the context
   * @param u the u
   * @return the i page
   */
  public static IPage toPage(String context, Url u) {
    HtmlPage p = new HtmlPage();
    p.pos = 0;
    p.request = u;
    if (context != null) {
      p.size = context.length();
    }
    if (context != null && u.toLowcase()) {
      p.context = context.toLowerCase();
    } else {
      p.context = context;
    }

    return p;
  }

  /**
   * To page.
   *
   * @param context the context
   * @return the i page
   */
  public static IPage toPage(String context) {
    return toPage(context, "utf-8");
  }

  /**
   * Instantiates a new html page.
   */
  private HtmlPage() {
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#release()
   */
  @Override
  public void release() {
    cookie = null;
    response = null;
    context = null;
    request = null;
    requestFilename = null;
    absoluatePath = null;
    headers = null;

  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#size()
   */
  @Override
  public int size() {
    return size;
  }

  /**
   * Gets the cookie.
   *
   * @return the cookie
   */
  public String getCookie() {
    if (cookie != null)
      return cookie.toString();
    return null;
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#getText(java.lang.String)
   */
  public String getText(String tag) {
    if (!seek("<" + tag))
      return null;

    int s = pos;
    pos++;

    Stack<Tag> tags = new Stack<Tag>();
    // ArrayList<Tag> tags = new ArrayList<Tag>();
    tags.add(new Tag(tag, false));

    while (tags.size() > 0) {
      Tag t = nextTag();
      // log.debug(tags + ", tag=" + t);

      if (t == null)
        break;
      if (t.reverse) {
        if (t.isScript() || t.isStyle()) {
          continue;
        }

        // pop the pair
        while (!tags.isEmpty()) {
          if (t.equals(tags.pop()))
            break;
        }

        // for (int i = tags.size() - 1; i > -1; i--) {
        // Tag t0 = tags.get(i);
        // if (t.equals(t0)) {
        // while (tags.size() > i) {
        // tags.remove(tags.size() - 1);
        // }
        // break;
        // }
        // }

      } else if (t.isScript() || t.isStyle()) {
        seek("</" + t.name);
      } else {
        tags.add(t);
      }
    }

    int e = pos > size - 2 ? size - 1 : pos + 1;

    return context.substring(s, e);
  }

  /**
   * Skip.
   *
   * @param c the c
   */
  private void skip(char c) {
    for (; pos < size; pos++) {
      char n = context.charAt(pos);
      if (n == c) {
        break;
      } else if (n == '\\') {
        pos++;
      } else if (n == '\'' || n == '"') {
        pos++;
        skip(n);
      } else if (n == '>' || n == '<') {
        pos--;
        break;
      }
    }
  }

  /**
   * Next is.
   *
   * @param s the s
   * @return true, if successful
   */
  private boolean nextIs(String s) {
    char[] c = s.toCharArray();

    for (int i = 0; i < c.length; i++) {
      if (c[i] != context.charAt(pos + i)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Next tag2.
   *
   * @return the tag
   */
  private Tag nextTag2() {
    for (; pos < size; pos++) {
      char c = context.charAt(pos);
      if (c == '<') {
        Tag t = new Tag();
        // get the word
        pos++;
        char n = context.charAt(pos);

        if (n == '/' || Character.isLetterOrDigit(n)) {
          StringBuilder s = new StringBuilder();
          if (n == '/') {
            t.end();
          } else {
            t.start();
          }

          for (; pos < size; pos++) {
            n = context.charAt(pos);
            if (n == ' ' || n == '>') {
              if (n == ' ') {
                skip('>');
                if (context.charAt(pos - 1) == '/')
                  return nextTag2();
              }

              return t.name(s.toString());
            } else {
              s.append(n);
            }
          }
        } else if (nextIs("!--")) {
          seek("-->");
        }

      } else if (c == '\'' || c == '"') {
        // find the next quota
        pos++;
        skip(c);
      }
    }

    return null;
  }

  /**
   * Next tag.
   *
   * @return the tag
   * @deprecated 
   */
  private Tag nextTag() {
    for (; pos < size; pos++) {
      char c = context.charAt(pos);
      if (c == '<') {
        // get the word
        pos++;
        char n = context.charAt(pos);

        if (n == '/' || Character.isLetterOrDigit(n)) {
          StringBuilder s = new StringBuilder();
          boolean reverse = false;

          for (; pos < size; pos++) {
            n = context.charAt(pos);
            if (n == ' ' || n == '>') {
              if (n == ' ') {
                skip('>');
                if (context.charAt(pos - 1) == '/')
                  return nextTag();
              }

              return new Tag(s.toString(), reverse);
            } else if (n == '/') {
              if (s.length() == 0) {
                reverse = true;
              }
            } else {
              s.append(n);
            }
          }
        } else if (nextIs("!--")) {
          seek("-->");
        }

      } else if (c == '\'' || c == '"') {
        // find the next quota
        pos++;
        skip(c);
      }
    }

    return null;
  }

  /**
   * Checks if is cached.
   *
   * @return true, if is cached
   * @deprecated 
   */
  public boolean isCached() {
    return cached;
  }

  /**
   * Sets the cached.
   *
   * @param cached the new cached
   * @deprecated 
   */
  public void setCached(boolean cached) {
    this.cached = cached;
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#getUrl()
   */
  public Url getUrl() {
    return request;
  }

  /**
   * Instantiates a new html page.
   *
   * @param request the request
   * @param res the res
   */
  public HtmlPage(Url request, HttpResponse res) {

    seq.incrementAndGet();
    this.request = request;
    String urlStr = request.getUrl();
    response = res;

    headers = response.getAllHeaders();
    int index = urlStr.lastIndexOf('/');
    requestFilename = index < 8 ? null : urlStr.substring(index + 1); // https://
    // <--
    // at
    // least
    // index=7
    // or
    // 6
    index = urlStr.indexOf("//");
    if (index == -1)
      log.error("invalid url, without //" + urlStr);
    index = urlStr.indexOf('/', index + 3);
    if (index == -1) {
      absoluatePath = null;
    } else {
      absoluatePath = urlStr.substring(index);
    }

    getContext();

    if (context != null && request.toLowcase()) {
      context = context.toLowerCase();
    }
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#getHeaders()
   */
  public Header[] getHeaders() {
    return headers;
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#getUrls()
   */
  public List<Url> getUrls() {

    Set<Url> list = new HashSet<Url>();

    try {
      clearMark();
      reset();
      seek("<body");

      String co = cookie.toString();
      Url u = null;
      while ((u = getNextUrl()) != null) {
        if (u == Url.unWantedUrl)
          continue;

        // log.debug(u.getDomain() + ", " + u.getUrl());
        if (u.valid()) {
          u.format();
          if (!list.contains(u)) {
            // log.debug("url=" + u.getUrl() + ", cookie=" +
            // co);
            u.setCookie(co);
            // String url = Processor.rewrite(u);
            // u.setUrl(url);
            list.add(u);
          }
        }
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    if (list.isEmpty()) {
      log.debug("getList=empty, url=" + request.getUrl());
      clearMark();
      reset();
      // log.debug(getText());
    } else {
      // log.debug(request.getUrl() + "\r\nlink.size=" + list.size());

    }
    return new ArrayList<Url>(list);
  }

  /** The seq. */
  public static AtomicInteger seq = new AtomicInteger(0);

  // @Override
  // protected void finalize() throws Throwable {
  // super.finalize();
  // seq.decrementAndGet();
  // }

  /**
   * _get context.
   *
   * @param entity the entity
   * @param charset the charset
   * @return the string
   */
  private String _getContext(HttpEntity entity, String charset) {
    StringBuilder sb = new StringBuilder();

    InputStreamReader reader = null;

    try {
      if (charset == null) {
        reader = new InputStreamReader(entity.getContent());
      } else {
        reader = new InputStreamReader(entity.getContent(), charset);
      }

      char[] buf = new char[1024];
      int len = reader.read(buf);
      while (len > 0) {
        sb.append(buf, 0, len);
        len = reader.read(buf);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          log.error(e.getMessage(), e);
          ;
        }
      }
    }

    return sb.toString();

  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#getContext()
   */
  public String getContext() {
    if (context == null && response.getEntity() != null) {
      try {
        HttpEntity entity = response.getEntity();
        String ccs = EntityUtils.getContentCharSet(entity);
        // boolean defaultchar = false;

        if (ccs == null) {
          ccs = request.getCharset();
          // defaultchar = true;
        }

        /**
         * fix the bug of http.util of apache
         */
        String encoding = null;
        if (entity.getContentEncoding() != null) {
          encoding = entity.getContentEncoding().getValue();
        }

        if (ccs == null || ccs.indexOf("gb2312") > -1) {
          ccs = "GBK";
        }
        log.debug("charset:" + ccs + ", " + encoding);

        // InputStream iin = entity.getContent();
        // byte[] b = new byte[1024];
        // int len1 = iin.read(b);
        // while (len1 > 0) {
        // System.out.print(new String(b, 0, len1));
        // len1 = iin.read(b);
        // }

        if (encoding != null && encoding.indexOf("gzip") > -1) {

          BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);

          entity = bufferedEntity;

          StringBuilder sb = new StringBuilder();

          try {
            GZIPInputStream in = new GZIPInputStream(bufferedEntity.getContent());

            Reader reader = new InputStreamReader(in, ccs);

            // String s = reader.readLine();
            char[] buf = new char[2048];
            int len = reader.read(buf);
            while (len > 0) {
              sb.append(buf, 0, len);
              // sb.append(s).append("\r\n");
              len = reader.read(buf);
              // s = reader.readLine();
            }
          } catch (Exception e) {
            log.error(e.getMessage(), e);
          }

          if (sb.length() > 0) {
            context = sb.toString();
          }
        }

        if (context == null || context.length() == 0) {
          context = _getContext(entity, ccs);
        }

        // log.debug(context);

        size = context.length();
        pos = 0;

        // if (defaultchar && seek("charset")) {
        // mark();
        // String c = getWordIn("=", "\"");
        // if (c == null || "".equals(c)) {
        // reset();
        // c = getWordIn("\"", "\"");
        // }
        //
        // if (c != null
        // && (ccs == null || !ccs.equals(c.toLowerCase()))) {
        // try {
        // if (Charset.isSupported(c)) {
        // ccs = c.toLowerCase();
        // context = new String(context.getBytes(ccs));
        // }
        // } catch (Exception e) {
        // log.error("charset=" + c + ", url:"
        // + request.getUrl(), e);
        // }
        // size = context.length();
        // pos = 0;
        // }
        // } else {
        // pos = 0;
        // }

        // set cookie;
        cookie.add(request.getCookie());
        String c = this.getHeaderField("Set-Cookie");
        if (c != null) {
          cookie.add(c);
        }

      } catch (Exception e) {
        log.error(e.getMessage());// , e);
      }
    }
    return context;
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#getStatusLine()
   */
  public StatusLine getStatusLine() {
    return response.getStatusLine();
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#seek(java.lang.String)
   */
  public boolean seek(String pattern) {
    return seek(pattern, size - pos);
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#seek(java.lang.String, int)
   */
  public boolean seek(String pattern, int inlen) {
    // System.out.println("size=" + size + ": pos=" + pos + ", " + pattern);

    if (pos == size || pos == -1)
      return false;

    int i = context.indexOf(pattern, pos);

    if (i != -1 && (i - pos < inlen)) {
      pos = i;
      return true;
    }

    // if not found, seek to the end of doc
    // pos = size;

    // log.debug(context.substring(pos));
    return false;
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#getText()
   */
  public String getText() {
    // System.out.println("pos=" + pos);
    if (pos == 0) {
      return context;
    } else {

      if (!backTo('<'))
        return null;

      int s = pos;
      Tag t = nextTag();
      pos = s;
      if (t != null) {
        return getText(t.name);
      }

      return null;
    }
  }

  /**
   * Gets the tag name.
   *
   * @return the tag name
   */
  public String getTagName() {

    char c;
    int s;
    do {
      s = pos++;
      c = context.charAt(s);
    } while ((c <= 'a' || c >= 'z') && s < size);

    int e;
    do {
      e = pos++;
      c = context.charAt(e);
    } while (c >= 'a' && c <= 'z' && e < size);

    if (s < size && e < size) {
      return context.substring(s, e);
    }

    return null;
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#backTo(char)
   */
  public boolean backTo(char c) {
    while (pos >= 0) {
      if (context.charAt(pos) == c) {
        return true;
      }
      pos--;
    }

    // log.debug("backTo:" + c + ", " + pos);
    return false;
  }

  /** The mark. */
  Stack<Integer> mark = new Stack<Integer>();

  // private TimeStat timeStat;

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#mark()
   */
  public void mark() {
    mark.push(pos);
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#reset()
   */
  public void reset() {
    if (mark.isEmpty()) {
      pos = 0;
    } else {
      pos = mark.pop();
    }
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#clearMark()
   */
  public void clearMark() {
    mark.clear();
  }

  /**
   * Gets the header field.
   *
   * @param fieldName the field name
   * @return the header field
   */
  public String getHeaderField(String fieldName) {
    Header h = response.getFirstHeader(fieldName);
    if (h != null) {
      String value = h.getValue();
      if (value != null && "Content-Encoding".equals(fieldName) && value.indexOf("gzip") > -1) {
        return null;
      }
      return value;
    }

    return null;
  }

  /**
   * Gets the header field names.
   *
   * @return the header field names
   */
  public String[] getHeaderFieldNames() {
    String[] names = new String[headers.length];

    for (int i = 0; i < headers.length; i++) {
      names[i] = headers[i].getName();
    }
    return names;
  }

  /**
   * Gets the response code.
   *
   * @return the response code
   */
  public int getResponseCode() {
    return response.getStatusLine().getStatusCode();
  }

  /**
   * Gets the response message.
   *
   * @return the response message
   */
  public String getResponseMessage() {
    return response.getStatusLine().getReasonPhrase();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return context;
  }

  /**
   * Gets the header fields.
   *
   * @param fieldName the field name
   * @return the header fields
   */
  public String[] getHeaderFields(String fieldName) {
    Header[] h = response.getHeaders(fieldName);
    String[] names = new String[h.length];
    for (int i = 0; i < h.length; i++) {
      names[i] = h[i].getName();
    }
    return names;
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#getAttribute(java.lang.String)
   */
  public String getAttribute(String name) {
    if (!backTo('<'))
      return null;
    if (!seek(" " + name))
      return null;

    String equal = "=";
    if (!seek(equal)) {
      return null;
    }

    pos++;
    char c = context.charAt(pos);
    while (c == ' ') {
      c = context.charAt(++pos);
    }
    pos++;
    int s = (c == '"' || c == '\'') ? pos : pos - 1;
    char b = context.charAt(pos);
    while (true) {

      if (b == '\\')
        pos++;
      if ((b == ' ' || b == '>') && c != '"' && c != '\'')
        break;
      if (b == c && (c == '"' || c == '\''))
        break;

      if (pos == size)
        break;
      b = context.charAt(++pos);
    }

    int e = pos;
    return context.substring(s, e);
  }

  /** The regex. */
  String regex = null;

  /** The Constant A. */
  static final String A = "<a";
  
  /** The Constant IFRAME. */
  static final String IFRAME = "<iframe";
  
  /** The Constant HREF. */
  static final String HREF = "location.href";
  
  /** The Constant LOCATION. */
  static final String LOCATION = "window.location";

  /** The regex. */
  static String REGEX[] = { A, IFRAME, HREF, LOCATION };

  /**
   * Next link.
   *
   * @return the string
   */
  private String nextLink() {
    if (regex == null) {
      regex = REGEX[0];
    } else {
      for (int i = 0; i < REGEX.length; i++) {
        if (regex == REGEX[i]) {
          if (i == REGEX.length - 1) {
            return null;
          }
          regex = REGEX[i + 1];
          break;
        }
      }
    }
    clearMark();
    reset();

    return regex;
  }

  /**
   * Href.
   *
   * @return the string
   */
  private String href() {
    if (regex == null) {
      regex = REGEX[0];
    }

    if (!seek(regex)) {
      if (nextLink() == null)
        return null;

      return href();
    }
    String href = null;

    if (regex == A) {
      pos++;
      href = getAttribute("href");
    } else if (regex == HREF) {
      pos += regex.length();
      char c = context.charAt(pos);
      while (pos < size && c != '"' && c != '\'') {
        if (c == '>' || c == '<')
          return null;

        pos++;
        c = context.charAt(pos);
      }

      pos++;
      int s = pos;
      while (pos < size && context.charAt(pos) != c) {
        pos++;
      }

      href = context.substring(s, pos);
    } else if (regex == IFRAME) {
      pos++;
      href = getAttribute("src");
    } else if (regex == LOCATION) {
      pos += regex.length();
      char c = context.charAt(pos);
      while (pos < size && c != '"' && c != '\'') {
        if (c == '>' || c == '<')
          return null;

        pos++;
        c = context.charAt(pos);
      }

      pos++;
      int s = pos;
      while (pos < size && context.charAt(pos) != c) {
        pos++;
      }

      href = context.substring(s, pos);
    }

    if (href == null) {
      if (nextLink() == null)
        return null;

      return href();
    }

    return href;
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#format(java.lang.String)
   */
  public Url format(String href) {
    if (href == null)
      return null;// Url.unWantedUrl;

    // String u = href;// .toLowerCase();
    href = href.trim();
    pos += href.length();
    if (href == null || href.startsWith("javascript") || href.startsWith("mailto") || href.indexOf("void(0)") > 0) {
      return Url.unWantedUrl;
    } else {
      int index = href.indexOf('#');
      if (index != -1)
        href = href.substring(0, index);
      if (href.startsWith("http://") || href.startsWith("https://")) { // TODO
        // need
        // to
        // compare
        // the
        // current
        // url
        return new Url(href);
      } else {
        int p = -1;
        if (href.startsWith("//")) { // an absolute path inside the web
          return new Url("http:" + href);
        } else if (href.startsWith("/")) { // an absolute path inside the web
          // site
          if (!href.equals(absoluatePath))
            href = "http://" + request.getHost() + href;
          else
            return Url.unWantedUrl;
        } else { // an relative path to current request
          if (href.equals(requestFilename))
            return Url.unWantedUrl;
          String path = request.getPath();
          p = path.lastIndexOf("/");
          if (p != -1) {
            href = "http://" + request.getHost() + path.substring(0, p + 1) + href;
          } else if (path.length() == 0) {
            href = "http://" + request.getHost() + "/" + href;
          }
        }
      }
      return new Url(href);
    }
  }

  /**
   * Gets the next url.
   *
   * @return the next url
   */
  private Url getNextUrl() {
    String href = href();
    return format(href);
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#getWordIn(java.lang.String, java.lang.String)
   */
  public String getWordIn(String startChar, String endChar) {

    if (!seek(startChar))
      return null;

    pos += startChar.length();
    int s = pos;

    if (!seek(endChar))
      return null;
    int e = pos;

    String word = context.substring(s, e);

    // log.debug("word=" + word);

    return word;
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#seek(int)
   */
  public int seek(int i) {
    pos += i;
    return pos;
  }

  /**
   * The Class Tag.
   */
  class Tag {
    
    /** The name. */
    String name;
    
    /** The start. */
    boolean start = false;
    
    /** The end. */
    boolean end = false;
    
    /** The reverse. */
    boolean reverse = false;
    
    /** The isclosed. */
    boolean isclosed = false;

    /**
     * Start.
     *
     * @return the tag
     */
    public Tag start() {
      start = true;
      return this;
    }

    /**
     * End.
     *
     * @return the tag
     */
    public Tag end() {
      end = true;
      return this;
    }

    /**
     * Name.
     *
     * @param name the name
     * @return the tag
     */
    public Tag name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Instantiates a new tag.
     */
    public Tag() {
    }

    /**
     * Instantiates a new tag.
     *
     * @param s the s
     * @param r the r
     * @deprecated 
     */
    public Tag(String s, boolean r) {
      name = s.toLowerCase().replaceAll("[ 　\r\n\t]", "");
      reverse = r;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Tag) {
        return name.equals(((Tag) obj).name);
      }

      return false;
    }

    /**
     * Checks if is script.
     *
     * @return true, if is script
     */
    public boolean isScript() {
      return "script".equals(name);
    }

    /**
     * Checks if is style.
     *
     * @return true, if is style
     */
    public boolean isStyle() {
      return "style".equals(name);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
      if (reverse) {
        return new StringBuilder("</").append(name).append(">").toString();
      } else {
        return new StringBuilder("<").append(name).append(">").toString();
      }
    }
  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#getRequest()
   */
  @Override
  public Url getRequest() {
    return request;
  }

  /**
   * The main method.
   *
   * @param ar the arguments
   */
  public static void main(String ar[]) {

  }

  /* (non-Javadoc)
   * @see com.giisoo.http.IPage#get(java.lang.String)
   */
  @Override
  public String get(String url) {
    return null;
  }

}
