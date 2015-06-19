package org.apache.lucene.search;

import java.io.IOException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.similarities.Similarity;

import com.giisoo.core.index.Stat;

// TODO: Auto-generated Javadoc
/**
 * The Class GroupScorer.
 */
public class GroupScorer extends Scorer {
  
  /** The log. */
  static Log log = LogFactory.getLog(GroupScorer.class);

  /** The reader. */
  IndexReader reader;
  
  /** The query. */
  GroupQuery query;
  
  /** The stats. */
  List<Stat> stats;
  
  /** The scores. */
  Map<Integer, Float> scores;

  /** The doc. */
  int doc = -1;

  /** The all scorer. */
  List<Scorer> allScorer = new ArrayList<Scorer>();

  /**
   * Instantiates a new group scorer.
   *
   * @param query the query
   * @param reader the reader
   * @param similarity the similarity
   * @param minNrShouldMatch the min nr should match
   * @param required the required
   * @param prohibited the prohibited
   * @param optional the optional
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public GroupScorer(GroupQuery query, IndexReader reader, Similarity similarity, int minNrShouldMatch, List<Scorer> required, List<Scorer> prohibited, List<Scorer> optional) throws IOException {

    super(null);

    this.reader = reader;
    this.query = query;
    this.stats = query.getStats();
    this.scores = query.getScores();

    for (Scorer s : required) {
      if (!allScorer.contains(s)) {
        allScorer.add(s);
      }
    }

    for (Scorer s : optional) {
      if (!allScorer.contains(s)) {
        allScorer.add(s);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.apache.lucene.search.DocIdSetIterator#advance(int)
   */
  @Override
  public int advance(int target) throws IOException {
    doc = NO_MORE_DOCS;
    for (Scorer s : allScorer) {

      int d1 = s.docID();
      if (d1 < target) {
        d1 = s.advance(target);
      }

      if (d1 < doc) {
        doc = d1;
      }
    }

    // log.info("advance: t:" + target + ", d:" + doc);
    return doc;
  }

  /* (non-Javadoc)
   * @see org.apache.lucene.search.DocIdSetIterator#docID()
   */
  @Override
  public int docID() {
    return doc;
  }

  /* (non-Javadoc)
   * @see org.apache.lucene.search.DocIdSetIterator#nextDoc()
   */
  @Override
  public int nextDoc() throws IOException {
    if (doc < NO_MORE_DOCS) {
      int tmp = NO_MORE_DOCS;
      for (Scorer s : allScorer) {
        int d = s.docID();
        if (d <= doc) {
          d = s.nextDoc();
        }

        if (d < tmp)
          tmp = d;
      }

      doc = tmp;
    }

    // log.info("nextDoc: " + doc);
    return doc;
  }

  /* (non-Javadoc)
   * @see org.apache.lucene.search.Scorer#score()
   */
  @Override
  public float score() throws IOException {

    if (doc == NO_MORE_DOCS)
      return 0;

    float f = 0;
    float score = 0;

    Document d = reader.document(doc);
    if (d != null) {
      for (Scorer s : allScorer) {
        int d1 = s.docID();
        if (d1 < doc) {
          d1 = s.advance(doc);
        }
        if (d1 == doc) {
          float t = s.score();
          if (f < t) {
            f = t;
          }
          if (s instanceof GroupScorer) {
            GroupScorer gs = (GroupScorer) s;
            if (gs.scores != null) {
              score += t;
            }
          }
        }
      }

      // get stat
      if (stats != null) {
        // System.out.println(stats);

        for (Stat s : stats) {
          Object o = d.get(s.field);
          if (o != null) {
            s.add(o.toString(), 1);
          }
        }
      }

      if (scores != null) {
        scores.put(doc, score);
      }
    }
    return f;
  }

  /* (non-Javadoc)
   * @see org.apache.lucene.index.DocsEnum#freq()
   */
  @Override
  public int freq() throws IOException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.apache.lucene.search.DocIdSetIterator#cost()
   */
  @Override
  public long cost() {
    // TODO Auto-generated method stub
    return 0;
  }
}
