package com.giisoo.core.index;

import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.TokenGroup;

// TODO: Auto-generated Javadoc
/**
 * The Class BoldFormatter.
 */
public class BoldFormatter implements Formatter {

  /* (non-Javadoc)
   * @see org.apache.lucene.search.highlight.Formatter#highlightTerm(java.lang.String, org.apache.lucene.search.highlight.TokenGroup)
   */
  public String highlightTerm(String originalText, TokenGroup group) {
    if (group.getTotalScore() <= 0) {
      return originalText;
    }

    return new StringBuilder("<b>").append(originalText).append("</b>").toString();
  }
}
