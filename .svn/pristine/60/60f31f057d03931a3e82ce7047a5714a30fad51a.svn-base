package com.giisoo.core.index;

import java.util.*;

import org.apache.commons.logging.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;

import com.giisoo.core.bean.*;
import com.giisoo.core.cache.DefaultCachable;
import com.giisoo.core.index.Searchable.SearchableField;

// TODO: Auto-generated Javadoc
/**
 * The Class SearchResults.
 *
 * @param <T> the generic type
 */
public class SearchResults<T extends Searchable> extends DefaultCachable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The log. */
  static Log log = LogFactory.getLog(SearchResults.class);

  /** The docs. */
  transient ScoreDoc[] docs;
  
  /** The total. */
  int total;

  /** The scores. */
  Map<Integer, Float> scores;
  
  /** The list. */
  List<Searchable> list;
  
  /** The stats. */
  List<Stat> stats;

  /**
   * Gets the stats.
   *
   * @return the stats
   */
  public List<Stat> getStats() {
    return stats;
  }

  /**
   * Sets the stats.
   *
   * @param stats the new stats
   */
  public void setStats(List<Stat> stats) {
    this.stats = stats;
  }

  /**
   * Gets the list.
   *
   * @return the list
   */
  public List<T> getList() {
    return (List<T>) list;
  }

  /**
   * Instantiates a new search results.
   *
   * @param docs the docs
   */
  public SearchResults(TopDocs docs) {
    if (docs != null) {
      this.docs = docs.scoreDocs;
      total = docs.totalHits;
    }
  }

  /**
   * Length.
   *
   * @return the int
   */
  public int length() {
    return docs.length;
  }

  /**
   * Total.
   *
   * @return the int
   */
  public int total() {
    return total;
  }

  /**
   * Highlight.
   *
   * @param searchable the searchable
   * @param q the q
   * @param reader the reader
   * @param analyzer the analyzer
   * @param distances the distances
   * @param start the start
   * @param number the number
   * @param highlighter the highlighter
   * @return the search results
   */
  public SearchResults<T> highlight(Class<T> searchable, Query q, IndexReader reader, Analyzer analyzer, Map<Integer, Double> distances, int start, int number, Highlighter highlighter) {
    // log.info("highlighting ...");
    if (docs != null) {
      int end = start + number;
      end = Math.min(end, docs.length);

      SearchableField[] flds = null;

      if (end >= start) {
        // log.info("end: " + end);

        list = new ArrayList<Searchable>(end - start);

        for (int i = start; i < end; i++) {
          // log.info("i:" + i + ", end: " + end);

          int doc = docs[i].doc;
          try {

            Document d = reader.document(doc);
            String id = d.get(X.ID);

            // load from cache first
            T p = Bean.cache(id, searchable);
            if (p == null || p.elderThan(X.AMINUTE)) {
              // else load from db
              p = searchable.newInstance();
              if (p.load(id) && p.isValid()) {
                p.cache(id);
              } else {
                p = null;
              }
            }

            if (p != null) {
              if (scores != null && scores.containsKey(doc)) {
                p.setScore(scores.get(doc));
              }

              String date = d.get(X.DATE);
              if (date != null) {
                p.setDate(Long.parseLong(date));
              }

              if (distances != null) {
                p.setDistance(X.mile2Meter(distances.get(doc)));
              }

              int pos = list.indexOf(p);
              if (pos > -1) {
                Searchable l0 = list.get(pos);
                if (p.betterThan(l0)) {
                  Searchable p0 = list.remove(pos);
                  list.add(p);
                  // p0.remove();
                  PendIndex.create(searchable, p0.getId());
                } else {
                  // p.remove();
                  PendIndex.create(searchable, p.getId());
                }

                end = Math.min(end + 1, docs.length);
                total--;

              } else {
                if (flds == null) {
                  flds = p.getSearchableFields();
                }
                if (flds != null) {
                  for (SearchableField f : flds) {
                    try {
                      if (f == null)
                        continue;

                      if (reader != null) {
                        // TermPositionVector tpv = (TermPositionVector) reader.getTermFreqVector(doc, f.field);
                        // if (tpv != null) {
                        String s = p.getValue(f);
                        if (s != null) {
                          s = highlighter.getBestFragment(analyzer, f.field, s);
                          if (s != null) {
                            p.setValue(f, s);
                          }
                        }
                      }
                      // } else {
                      // log.error("the indexer reader is null, highlight failed!");
                      // }
                    } catch (Exception e1) {
                      log.error(e1.getMessage(), e1);
                    }
                  }
                }

                list.add(p);
              }
            } else {
              log.info("load object failed, id:" + id);

              PendIndex.create(searchable, id);
              end = Math.min(end + 1, docs.length);
              total--;

              // addStat(-1);
            }
          } catch (Exception e) {
            log.error(e.getMessage(), e);
          }
        }
      } else {
        log.info("start:" + start + ", end:" + end);
      }
    } else {
      log.info("not found anything!");
    }

    // to test
    if (list == null) {
      list = new ArrayList<Searchable>();
    }

    setStat(total);

    return this;
  }

  /**
   * Sets the stat.
   *
   * @param i the new stat
   */
  private void setStat(int i) {
    if (stats != null && stats.size() > 0) {
      if (i == 0) {
        stats.clear();
      } else
        for (Stat s : stats) {
          if (s != null && s.stat != null && s.stat.size() > 0) {
            for (String name : s.stat.keySet()) {
              int ii = s.stat.get(name);
              if (ii > i)
                ii = i;

              s.stat.put(name, ii);
            }
          }
        }
    }
  }

  /**
   * Sets the scores.
   *
   * @param scores the scores
   */
  public void setScores(Map<Integer, Float> scores) {
    this.scores = scores;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "SearchResults [total=" + total + "]";
  }

}
