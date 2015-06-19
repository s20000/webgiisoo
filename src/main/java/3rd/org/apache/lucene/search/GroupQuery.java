package org.apache.lucene.search;

import java.io.IOException;
import java.util.*;

import org.apache.lucene.index.*;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;

import com.giisoo.core.index.Stat;

// TODO: Auto-generated Javadoc
/**
 * The Class GroupQuery.
 */
@SuppressWarnings("serial")
public class GroupQuery extends BooleanQuery {

  /** The stats. */
  List<Stat> stats;
  
  /** The scores. */
  Map<Integer, Float> scores;

  /**
   * Gets the scores.
   *
   * @return the scores
   */
  public Map<Integer, Float> getScores() {
    return scores;
  }

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

  /** The tostring. */
  transient String tostring = null;

  /* (non-Javadoc)
   * @see org.apache.lucene.search.BooleanQuery#toString(java.lang.String)
   */
  @Override
  public String toString(String arg0) {
    if (tostring == null) {
      tostring = super.toString(arg0);
      if (stats != null) {
        tostring = tostring + stats;
      }
    }

    return tostring;
  }

  /* (non-Javadoc)
   * @see org.apache.lucene.search.BooleanQuery#rewrite(org.apache.lucene.index.IndexReader)
   */
  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    GroupQuery clone = null; // recursively rewrite
    for (int i = 0; i < clauses().size(); i++) {
      BooleanClause c = clauses().get(i);
      Query query = c.getQuery().rewrite(reader);
      if (query != c.getQuery()) { // clause rewrote: must clone
        if (clone == null)
          clone = (GroupQuery) this.clone();
        clone.clauses().set(i, new BooleanClause(query, c.getOccur()));
      }
    }
    if (clone != null) {
      return clone; // some clauses rewrote
    } else
      return this; // no clauses rewrote
  }

  /**
   * Instantiates a new group query.
   */
  public GroupQuery() {
  }

  /* (non-Javadoc)
   * @see org.apache.lucene.search.BooleanQuery#createWeight(org.apache.lucene.search.IndexSearcher)
   */
  @Override
  public Weight createWeight(IndexSearcher searcher) throws IOException {
    return new GroupWeight(searcher);
  }

  /**
   * Sets the scores.
   *
   * @param scores the scores
   */
  public void setScores(Map<Integer, Float> scores) {
    this.scores = scores;
  }

  /**
   * The Class GroupWeight.
   */
  class GroupWeight extends BooleanWeight {
    
    /**
     * Instantiates a new group weight.
     *
     * @param searcher the searcher
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public GroupWeight(IndexSearcher searcher) throws IOException {
      super(searcher, false);
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.BooleanQuery.BooleanWeight#scorer(org.apache.lucene.index.AtomicReaderContext, boolean, boolean, org.apache.lucene.util.Bits)
     */
    @Override
    public Scorer scorer(AtomicReaderContext reader, boolean arg1, boolean arg2, Bits bits) throws IOException {
      List<Scorer> required = new ArrayList<Scorer>();
      List<Scorer> prohibited = new ArrayList<Scorer>();
      List<Scorer> optional = new ArrayList<Scorer>();
      Iterator<BooleanClause> cIter = clauses().iterator();
      for (Weight w : weights) {
        BooleanClause c = cIter.next();
        Scorer subScorer = w.scorer(reader, true, false, bits);
        if (subScorer == null) {
          if (c.isRequired()) {
            return null;
          }
        } else if (c.isRequired()) {
          required.add(subScorer);
        } else if (c.isProhibited()) {
          prohibited.add(subScorer);
        } else {
          optional.add(subScorer);
        }
      }
      return new GroupScorer(GroupQuery.this, reader.reader(), similarity, minNrShouldMatch, required, prohibited, optional);

    }

  }

}
