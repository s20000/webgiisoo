package com.giisoo.core.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.wltea.analyzer.core.*;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.giisoo.core.conf.Config;

// TODO: Auto-generated Javadoc
/**
 * The Class GAnalyzer.
 */
public class GAnalyzer {

  /** The analyzer. */
  static Analyzer analyzer;
  
  /** The log. */
  static Log log = LogFactory.getLog(GAnalyzer.class);
  
  /** The Constant INTERVAL. */
  static final long INTERVAL = 10 * 60 * 1000;

  /** The _instance. */
  static GAnalyzer _instance = new GAnalyzer();

  /** The initialied. */
  static boolean initialied = false;

  /**
   * Inits the.
   *
   * @param conf the conf
   */
  public synchronized static void init(Configuration conf) {
    if (initialied)
      return;

    try {
      Fuzzy.init(conf);

      // Dictionary.getSingleton(). .loadExtendStopWords(loadStop());

      loadExternal();

      initialied = true;

    } catch (Throwable e) {
      log.error(e.getMessage(), e);
    }

  }

  /**
   * Load external words.
   *
   * @param list the list
   */
  public static void loadExternalWords(Set<String> list) {
    if (list != null) {
      list.remove("");
      list.remove(" ");
      Dictionary.getSingleton().addWords(list);
    }
  }

  /**
   * Gets the analyzer.
   *
   * @return the analyzer
   */
  public static synchronized Analyzer getAnalyzer() {
    if (analyzer == null) {
      analyzer = new IKAnalyzer(false);
    }

    return analyzer;
  }

  /**
   * return the similar for s2 with the list.
   *
   * @param l1 the l1
   * @param s2 the s2
   * @return the float
   */
  public static float similar(List<String> l1, String s2) {
    if (l1 == null || s2 == null)
      return 0;
    float f = 0;
    for (String s1 : l1) {
      float f1 = similar(s1, s2);
      if (f1 > f) {
        f = f1;
      }
    }

    return f;
  }

  /**
   * Similar.
   *
   * @param s1 the s1
   * @param s2 the s2
   * @return the float
   */
  public static float similar(String s1, String s2) {
    List<String> stop = new ArrayList<String>();

    if (s1 == null || s2 == null)
      return 0;

    List<String> l1 = parse2(s1);
    List<String> l2 = parse2(s2);

    if (l1 == null || l2 == null)
      return 0;

    int len = 0;
    for (int i = l1.size() - 1; i >= 0; i--) {
      String s = l1.get(i);
      if (l2.remove(s)) {
        l1.remove(i);
      } else {
        if (!stop.contains(s)) {
          len += s.length();
        }
      }
    }

    for (String s : l2) {
      if (!stop.contains(s)) {
        len += s.length();
      }
    }

    return 1 - len / (float) (s1.length() + s2.length());
  }

  /**
   * Parse3.
   *
   * @param s the s
   * @return the list
   */
  public static List<List<String>> parse3(String s) {
    return _ik_parse3(s);
  }

  /**
   * Parse2.
   *
   * @param s the s
   * @return the list
   */
  public static List<String> parse2(String s) {
    return _ik_parse(s);
  }

  /**
   * Parses the.
   *
   * @param s the s
   * @return the list
   * @deprecated <br>
   *             split string to words and remove duplicate word
   */
  public static List<String> parse(String s) {
    return _ik_parse2(s);
  }

  /**
   * _ik_parse3.
   *
   * @param t the t
   * @return the list
   */
  static List<List<String>> _ik_parse3(String t) {
    if (t == null || t.length() == 0)
      return null;

    IKSegmenter s = new IKSegmenter(new StringReader(t), false);
    KTerms ts = new KTerms();

    try {
      Lexeme l = null;
      Lexeme prev = null;

      int end = 0;
      while ((l = s.next()) != null) {
        if (prev != null) {
          int e1 = l.getBeginPosition();
          for (int i = prev.getEndPosition(); i < e1; i++) {
            char c = t.charAt(i);
            String s1 = Character.toString(c);
            if (s1.length() > 0 && !" ".equals(s1) && !Dictionary.getSingleton().isStopWord(s1.toCharArray(), 0, 1)) {
              ts.add(KTerm.create(s1, i, i + 1));
            }
          }
        }

        prev = l;
        String s1 = l.getLexemeText().trim();
        ts.add(KTerm.create(s1, l.getBeginPosition(), l.getEndPosition()));
        if (l.getEndPosition() > end) {
          end = l.getEndPosition();
        }
      }

      if (prev != null) {
        int e = t.length();
        for (int i = prev.getEndPosition(); i < e; i++) {

          char c = t.charAt(i);
          String s1 = Character.toString(c);
          if (s1.length() > 0 && !" ".equals(s1) && !Dictionary.getSingleton().isStopWord(s1.toCharArray(), 0, 1)) {
            ts.add(KTerm.create(s1, i, i + 1));
          }

        }
      }

      int len = ts.size();
      List<KTerms> list = new ArrayList<KTerms>();
      int min = -1;
      for (int i = 0; i < len; i++) {
        KTerm k = ts.get(i);
        if (min == -1) {
          min = k.s;
        }
        if (k.s == min) {
          list.add(new KTerms().add(k));
        } else {
          break;
        }
      }

      boolean changed = true;
      while (changed) {
        changed = false;
        int s1 = list.size();
        for (int i1 = 0; i1 < s1; i1++) {
          KTerms k = list.get(i1);

          List<KTerm> l1 = ts.next(k.last());
          if (l1.size() == 1) {
            k.add(l1.get(0));
            changed = true;
          } else if (l1.size() > 1) {
            for (int i = 0; i < l1.size() - 1; i++) {
              KTerms k1 = k.copy();
              k1.add(l1.get(i));
              list.add(k1);
            }
            k.add(l1.get(l1.size() - 1));
            changed = true;
          }
        }
      }

      List<List<String>> r = new ArrayList<List<String>>();
      for (KTerms k : list) {
        if (k.length() == end) {
          r.add(k.get());
        }
      }

      return r;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * The Class KTerms.
   */
  static class KTerms {
    
    /** The list. */
    List<KTerm> list = new ArrayList<KTerm>();

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return list.toString();
    }

    /**
     * Length.
     *
     * @return the int
     */
    public int length() {
      return last().e;
    }

    /**
     * Copy.
     *
     * @return the k terms
     */
    public KTerms copy() {
      KTerms k = new KTerms();
      k.list.addAll(list);
      return k;
    }

    /**
     * Last.
     *
     * @return the k term
     */
    public KTerm last() {
      if (list.size() > 0) {
        return list.get(list.size() - 1);
      }

      return null;
    }

    /**
     * Gets the.
     *
     * @return the list
     */
    public List<String> get() {
      List<String> l = new ArrayList<String>(list.size());

      for (KTerm s : list) {
        if (!Dictionary.getSingleton().isStopWord(s.m.toCharArray(), 0, s.m.length())) {
          l.add(s.m);
        }
      }

      return l;
    }

    /**
     * Size.
     *
     * @return the int
     */
    public int size() {
      return list.size();
    }

    /**
     * Gets the.
     *
     * @param i the i
     * @return the k term
     */
    public KTerm get(int i) {
      return list.get(i);
    }

    /**
     * Adds the.
     *
     * @param e the e
     * @return the k terms
     */
    public KTerms add(KTerm e) {
      list.add(e);
      return this;
    }

    /**
     * Next.
     *
     * @param e the e
     * @return the list
     */
    public List<KTerm> next(KTerm e) {
      List<KTerm> r = new ArrayList<KTerm>();
      int min = -1;
      for (KTerm t : list) {
        if ((t.s > e.s) && (t.s < e.e + 1) && t.e > e.e) {
          r.add(t);
        } else if (t.s > e.e) {
          min = t.s;
          break;
        }
      }

      /**
       * empty and there must some special char
       */
      if (r.size() == 0 && min != -1) {
        for (KTerm t : list) {
          if (t.e > e.e && t.s < min) {
            break;
          } else if (t.s == min) {
            r.add(t);
          }
        }
      }

      return r;
    }
  }

  /**
   * The Class KTerm.
   */
  static class KTerm {
    
    /** The m. */
    String m;
    
    /** The s. */
    int s;
    
    /** The e. */
    int e;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return new StringBuilder(m).append(":").append(s).append("/").append(e).toString();
    }

    /**
     * Creates the.
     *
     * @param m the m
     * @param s the s
     * @param e the e
     * @return the k term
     */
    public static KTerm create(String m, int s, int e) {
      KTerm k = new KTerm();
      k.m = m;
      k.s = s;
      k.e = e;
      return k;
    }

  }

  /**
   * _ik_parse2.
   *
   * @param t the t
   * @return the list
   */
  static List<String> _ik_parse2(String t) {
    if (t == null || t.length() == 0)
      return null;

    IKSegmenter s = new IKSegmenter(new StringReader(t), false);
    ArrayList<String> list = new ArrayList<String>();

    try {
      Lexeme l = null;
      Lexeme prev = null;
      while ((l = s.next()) != null) {
        if (prev != null) {
          int end = l.getBeginPosition();
          for (int i = prev.getEndPosition(); i < end; i++) {
            char c = t.charAt(i);
            String s1 = Character.toString(c);
            if (!Dictionary.getSingleton().isStopWord(s1.toCharArray(), 0, 1) && !list.contains(s1)) {
              list.add(s1);
            }
          }
        }

        prev = l;
        String s1 = l.getLexemeText().trim();

        /**
         * the first one not full include the second one
         */
        if (!Dictionary.getSingleton().isStopWord(s1.toCharArray(), 0, 1) && !list.contains(s1)) {
          list.add(s1);
        }
      }

      if (prev != null) {
        int end = t.length();
        for (int i = prev.getEndPosition(); i < end; i++) {
          char c = t.charAt(i);
          String s1 = Character.toString(c);
          if (!Dictionary.getSingleton().isStopWord(s1.toCharArray(), 0, 1) && !list.contains(s1)) {
            list.add(s1);
          }
        }
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    // for (int i = list.size() - 1; i >= 0; i--) {
    // String s1 = list.get(i);
    //
    // if (s1 == null || s1.trim().length() == 0) {
    // list.remove(i);
    //
    // log.info("removed: " + i);
    // }
    // }
    //
    // log.info(list);

    list.remove("");
    list.remove(" ");
    return list;
  }

  /**
   * _ik_parse.
   *
   * @param t the t
   * @return the list
   */
  static List<String> _ik_parse(String t) {
    if (t == null || t.length() == 0)
      return null;

    IKSegmenter s = new IKSegmenter(new StringReader(t), false);
    ArrayList<String> list = new ArrayList<String>();

    try {
      Lexeme l = null;
      Lexeme prev = null;
      Lexeme temp = null;

      while ((l = s.next()) != null) {
        if (prev != null && prev.getEndPosition() > l.getBegin()) {
          String s1 = l.getLexemeText().trim();
          if (!Dictionary.getSingleton().isStopWord(s1.toCharArray(), 0, s1.length())) {
            temp = l;
          }
          continue;
        }

        if (prev != null && prev.getEndPosition() != l.getBegin()) {
          String s1 = null;
          if (temp != null) {
            s1 = temp.getLexemeText().trim();
          }

          if (s1 != null && s1.length() > 0 && !list.contains(s1) && !Dictionary.getSingleton().isStopWord(s1.toCharArray(), 0, s1.length())) {
            list.add(s1);
          }
        }
        temp = null;
        prev = l;

        String s1 = l.getLexemeText().trim();

        /**
         * the first one not full include the second one
         */
        if (s1.length() > 0 && !list.contains(s1) && !Dictionary.getSingleton().isStopWord(s1.toCharArray(), 0, s1.length())) {
          list.add(s1);
        }
      }

      if (temp != null && prev != null && temp.getEndPosition() > prev.getEndPosition()) {
        String s1 = temp.getLexemeText().trim();
        if (s1.length() > 0 && !list.contains(s1) && !Dictionary.getSingleton().isStopWord(s1.toCharArray(), 0, s1.length())) {
          list.add(s1);
        }
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    list.remove(" ");
    list.remove("");
    return list;
  }

  /**
   * Load external.
   *
   * @return the list
   */
  private static List<String> loadExternal() {
    Configuration conf = Config.getConfig();

    File f = new File(conf.getString("home") + "/dic/external.dic");

    List<String> dic = new ArrayList<String>();

    try {
      if (f.exists()) {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
        String s = null;
        do {
          s = r.readLine();
          if (s != null && !s.startsWith("#")) {
            String[] ss = s.split(",");

            for (String s1 : ss) {
              s1 = s1.trim().toLowerCase();
              if (s1.length() > 0 && !dic.contains(s1)) {
                dic.add(s1);
              }
            }
          }
        } while (s != null);
      }
    } catch (Exception e) {

    }

    return dic;
  }

  /**
   * Load stop.
   *
   * @return the list
   */
  private static List<String> loadStop() {
    Configuration conf = Config.getConfig();

    File f = new File(conf.getString("home") + "/dic/stop.dic");

    List<String> dic = new ArrayList<String>();

    try {
      if (f.exists()) {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
        String s = null;
        do {
          s = r.readLine();
          if (s != null && !s.startsWith("#")) {
            String[] ss = s.split(",");

            for (String s1 : ss) {
              s1 = s1.trim().toLowerCase();
              if (s1.length() > 0 && !dic.contains(s1)) {
                dic.add(s1);
              }
            }
          }
        } while (s != null);
      }
    } catch (Exception e) {

    }

    dic.remove("");

    return dic;
  }

}
