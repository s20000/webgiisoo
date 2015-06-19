package com.giisoo.core.query;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.core.bean.*;

// TODO: Auto-generated Javadoc
/**
 * The Class Fuzzy.
 */
@SuppressWarnings("serial")
public class Fuzzy extends Bean {

  /** The word. */
  String word;
  
  /** The content. */
  String content;

  /** The log. */
  static Log log = LogFactory.getLog(Fuzzy.class);

  /** The _conf. */
  static Configuration _conf;

  /** The dic. */
  static Map<String, Set<String>> dic;
  
  /** The lastupdated. */
  static long lastupdated = 0;

  /**
   * Gets the word.
   *
   * @return the word
   */
  public String getWord() {
    return word;
  }

  /**
   * Sets the word.
   *
   * @param word the new word
   */
  public void setWord(String word) {
    this.word = word;
  }

  /**
   * Gets the content.
   *
   * @return the content
   */
  public String getContent() {
    return content;
  }

  /**
   * Sets the content.
   *
   * @param content the new content
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Load.
   *
   * @param offset the offset
   * @param limit the limit
   * @return the list
   */
  public static List<Fuzzy> load(int offset, int limit) {
    Connection c = null;
    PreparedStatement stat = null;
    ResultSet r = null;

    try {
      c = Bean.getConnection();
      stat = c.prepareStatement("select * from tblfuzzy order by word offset ? limit ?");
      stat.setInt(1, offset);
      stat.setInt(2, limit);
      r = stat.executeQuery();
      List<Fuzzy> list = new ArrayList<Fuzzy>(limit);
      while (r.next()) {
        Fuzzy f = new Fuzzy();
        f.word = r.getString("word");
        f.content = r.getString("content");
        list.add(f);
      }
      return list;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      Bean.close(r, stat, c);
    }

    return null;
  }

  /**
   * Load.
   *
   * @param word the word
   * @param offset the offset
   * @param limit the limit
   * @return the list
   */
  public static List<Fuzzy> load(String word, int offset, int limit) {
    Connection c = null;
    PreparedStatement stat = null;
    ResultSet r = null;

    try {
      c = Bean.getConnection();
      stat = c.prepareStatement("select * from tblfuzzy where word like ? order by word offset ? limit ?");
      stat.setString(1, "%" + word + "%");
      stat.setInt(2, offset);
      stat.setInt(3, limit);
      r = stat.executeQuery();
      List<Fuzzy> list = new ArrayList<Fuzzy>(limit);
      while (r.next()) {
        Fuzzy f = new Fuzzy();
        f.word = r.getString("word");
        f.content = r.getString("content");
        list.add(f);
      }
      return list;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      Bean.close(r, stat, c);
    }

    return null;
  }

  /**
   * Removes the.
   *
   * @param word the word
   */
  public static void remove(String word) {
    Connection c = null;
    PreparedStatement stat = null;

    try {
      c = Bean.getConnection();
      stat = c.prepareStatement("delete from tblfuzzy where word=?");
      stat.setString(1, word);
      stat.executeUpdate();

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      Bean.close(stat, c);
    }
  }

  /**
   * Update.
   *
   * @param word the word
   * @param content the content
   */
  public static void update(String word, String content) {
    Connection c = null;
    PreparedStatement stat = null;
    ResultSet r = null;

    try {
      c = Bean.getConnection();
      stat = c.prepareStatement("select 1 from tblfuzzy where word=?");
      stat.setString(1, word);
      r = stat.executeQuery();
      boolean created = true;
      if (r.next()) {
        // update
        created = false;
      }
      r.close();
      r = null;
      stat.close();
      if (created) {
        stat = c.prepareStatement("insert into tblfuzzy(content, word) values(?,?)");
      } else {
        stat = c.prepareStatement("update tblfuzzy set content=? where word=?");
      }
      stat.setString(1, content);
      stat.setString(2, word);
      stat.executeUpdate();

      parse(word, content, dic);

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      Bean.close(r, stat, c);
    }
  }

  /**
   * Reload.
   */
  public static void reload() {
    if (System.currentTimeMillis() - lastupdated < 5 * X.AMINUTE)
      return;

    Map<String, Set<String>> temp = new TreeMap<String, Set<String>>();

    // load from file first
    String fuzzyFile = new StringBuilder(_conf.getString("home")).append("/dic/fuzzy.dic").toString();
    File f = new File(fuzzyFile);
    if (f.exists()) {
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
        String line = null;
        while ((line = reader.readLine()) != null) {
          if (!line.startsWith("#")) {
            parse(line, temp);
          }
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }

    // load from database
    Connection c = null;
    PreparedStatement stat = null;
    ResultSet r = null;

    try {
      c = Bean.getConnection();
      stat = c.prepareStatement("select * from tblfuzzy");
      r = stat.executeQuery();

      while (r.next()) {
        String word = r.getString("word");
        String content = r.getString("content");
        parse(word, content, temp);
      }

      lastupdated = System.currentTimeMillis();

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      Bean.close(r, stat, c);
    }

    if (temp.size() > 0) {
      dic = temp;
    }
  }

  /**
   * Inits the.
   *
   * @param conf the conf
   */
  public static void init(Configuration conf) {
    _conf = conf;

    reload();
  }

  /**
   * Parses the.
   *
   * @param line the line
   * @param temp the temp
   */
  private static void parse(String line, Map<String, Set<String>> temp) {
    StringTokenizer st = new StringTokenizer(line, DELIM, false);

    Set<String> list = null;

    while (st.hasMoreTokens()) {
      String s = st.nextToken();
      if (DELIM.contains(s)) {
        continue;
      }

      if (list == null) {
        list = new HashSet<String>();
        list.add(s);
        temp.put(s, list);
      } else {
        list.add(s);
        temp.put(s, list);
      }
    }
  }

  /**
   * Parses the.
   *
   * @param word the word
   * @param line the line
   * @param temp the temp
   */
  private static void parse(String word, String line, Map<String, Set<String>> temp) {
    StringTokenizer st = new StringTokenizer(line, DELIM, false);

    Set<String> list = temp.get(word);
    if (list == null) {
      list = new HashSet<String>();
      temp.put(word, list);
    }
    list.add(word);

    while (st.hasMoreTokens()) {
      String s = st.nextToken();
      if (DELIM.contains(s)) {
        continue;
      }

      if (!list.contains(s)) {
        list.add(s);
      }

      if (temp.containsKey(s)) {
        Set<String> old = temp.get(s);
        list.addAll(old);
        old.addAll(list);
      } else {
        temp.put(s, list);
      }
    }
  }

  /**
   * Gets the.
   *
   * @param s the s
   * @return the sets the
   */
  public static Set<String> getFuzzy(String s) {
    return dic == null ? null : dic.get(s);
  }

  /** The Constant DELIM. */
  final static String DELIM = " =,;，。；";

}
