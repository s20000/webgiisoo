package com.giisoo.core.query;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.Version;

import com.giisoo.core.index.Searchable.SearchableField;

// TODO: Auto-generated Javadoc
/**
 * The Class GMultiQuery.
 */
public class GMultiQuery {

	/** The log. */
	static Log log = LogFactory.getLog(GMultiQuery.class);

	/**
	 * Inits the.
	 */
	public synchronized static void init() {
		// GroupQuery.setMaxClauseCount(4096);
	}

	/**
	 * Parse3.
	 * 
	 * @param flds
	 *            the flds
	 * @param word1
	 *            the word1
	 * @param oneword
	 *            the oneword
	 * @return the group query
	 */
	public static GroupQuery parse3(SearchableField[] flds, String word1,
			boolean oneword, boolean must) {
		List<String> list = new ArrayList<String>();

		if (!oneword) {
			StringTokenizer st = new StringTokenizer(word1, "|", false);
			while (st.hasMoreTokens()) {
				String word = st.nextToken();
				if (word == null || word.length() == 0)
					continue;

				list.add(word);
				Set<String> l1 = Fuzzy.getFuzzy(word);
				if (l1 != null) {
					for (String s : l1) {
						if (!list.contains(s)) {
							list.add(s);
						}
					}
				}
			}
		} else {
			list.add(word1);
		}

		GroupQuery q = new GroupQuery();
		log.debug("list:" + list);

		// System.out.println(stats);

		try {
			// Collections.sort(list);

			for (String s : list) {
				BooleanQuery q1 = new BooleanQuery();
				int count = 0;
				for (SearchableField fld : flds) {
					if (fld == null)
						continue;

					Query q2 = new TermQuery(new Term(fld.field, s));
					// Query q2 = new QueryParser(Version.LUCENE_45, fld.field,
					// GAnalyzer.analyzer).parse(s);
					q2.setBoost(fld.boost);

					// log.debug("phase1:" + q2);
					q1.add(q2, Occur.SHOULD);
					// q1.add(q2, Occur.MUST);
					count++;
				}
				if (count > 0) {
					// q.add(q1, Occur.MUST);
					q.add(q1, Occur.SHOULD);
				}
				// log.debug("q=" + q);

				if (!oneword) {
					List<List<String>> splits = GAnalyzer.parse3(s);
					// log.debug("parse3:" + splits);

					// q1 = new BooleanQuery();
					count = 0;
					for (List<String> split : splits) {
						if (split != null) {
							split.remove(s);

							if (split.size() > 0) {
								FuzzyDescart descart = new FuzzyDescart(split);
								for (SearchableField fld : flds) {

									for (List<String> l1 = descart.first(); l1 != null; l1 = descart
											.next()) {
										// for (List<String> l1 : qlist) {
										// PhraseQuery q2 = new PhraseQuery();
										// q2.setSlop(5);
										// for (String s1 : l1) {
										// q2.add(new Term(fld.field, s1));
										// }
										// q2.setSlop(5);
										BooleanQuery q2 = new BooleanQuery();
										int count2 = 0;
										for (String s1 : l1) {

											q2.add(new TermQuery(new Term(
													fld.field, s1)),
													must ? Occur.MUST
															: Occur.SHOULD);

											count2++;
										}

										// log.debug("phase2:" + q2);

										if (count2 > 0) {
											q2.setBoost(fld.boost);
											q.add(q2, Occur.SHOULD);
											count++;
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Throwable e) {
			log.error(word1, e);
		}

		return q;
	}

	/**
	 * Simliarparse.
	 * 
	 * @param flds
	 *            the flds
	 * @param word1
	 *            the word1
	 * @return the group query
	 */
	public static GroupQuery simliarparse(SearchableField[] flds, String word1) {
		GroupQuery q = new GroupQuery();
		// log.debug("list:" + list);

		try {
			List<String> list = new ArrayList<String>();
			GTerm.Tokenizer st = new GTerm.Tokenizer(word1);
			String s2;
			while ((s2 = st.next()) != null) {
				if (!list.contains(s2)) {
					list.add(s2);

					Set<String> l1 = Fuzzy.getFuzzy(s2);
					if (l1 != null) {
						for (String s3 : l1) {
							if (!list.contains(s3)) {
								list.add(s3);
							}
						}
					}
				}
			}

			String[] fields = new String[flds.length];
			for (int i = 0; i < fields.length; i++) {
				fields[i] = flds[i].field;
			}

			StringBuilder sb = new StringBuilder();
			for (String s : list) {
				sb.append(s).append(" ");
			}
			MultiFieldQueryParser p1 = new MultiFieldQueryParser(
					Version.LUCENE_45, fields, GAnalyzer.analyzer);
			Query q1 = p1.parse(sb.toString());
			q.add(q1, Occur.MUST);

		} catch (Throwable e) {
			log.error(word1, e);
		}

		return q;
	}

	/**
	 * Parses the.
	 * 
	 * @param flds
	 *            the flds
	 * @param word
	 *            the word
	 * @param oneword
	 *            the oneword
	 * @return the query
	 */
	public static Query parse(SearchableField[] flds, String word,
			boolean oneword) {
		List<String> list = new ArrayList<String>();
		list.add(word);

		if (!oneword) {
			Set<String> l1 = Fuzzy.getFuzzy(word);
			if (l1 != null) {
				for (String s : l1) {
					if (!list.contains(s)) {
						list.add(s);
					}
				}
			}

		}

		GroupQuery q = new GroupQuery();
		for (String s : list) {
			for (SearchableField fld : flds) {
				Query q2 = new TermQuery(new Term(fld.field, s));
				q2.setBoost(fld.boost);
				q.add(q2, Occur.SHOULD);
			}

			if (!oneword) {
				List<String> split = GAnalyzer.parse2(s);
				if (split != null) {
					split.remove(s);

					if (split.size() > 0) {
						FuzzyDescart descart = new FuzzyDescart(split);

						for (SearchableField fld : flds) {

							for (List<String> l1 = descart.first(); l1 != null; l1 = descart
									.next()) {
								// for (List<String> l1 : qlist) {
								// PhraseQuery q2 = new PhraseQuery();
								// q2.setSlop(10);
								// for (String s1 : l1) {
								// q2.add(new Term(fld.field, s1));
								// }
								BooleanQuery q2 = new BooleanQuery();
								// q2.setSlop(5);
								for (String s1 : l1) {
									q2.add(new TermQuery(
											new Term(fld.field, s1)),
											Occur.MUST);
								}

								q2.setBoost(fld.boost);
								q.add(q2, Occur.SHOULD);
							}
						}
					}
				}
			}
		}

		return q;
	}
}
