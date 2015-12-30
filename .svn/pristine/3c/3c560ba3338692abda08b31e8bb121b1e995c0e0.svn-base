package com.giisoo.core.query;

import java.util.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;

import com.giisoo.core.bean.X;
import com.giisoo.core.index.*;
import com.giisoo.core.index.Searchable.SearchableField;
import com.giisoo.utils.base.Html;

// TODO: Auto-generated Javadoc
/**
 * The Class GTerm.
 */
public class GTerm {

	/** The log. */
	static Log log = LogFactory.getLog(GTerm.class);

	/** The terms. */
	List<String> terms;

	/** The q. */
	String q;

	/** The loc. */
	String loc;

	/** The type. */
	String type;

	/** The flds. */
	SearchableField[] flds;

	/** The stats. */
	List<Stat> stats;

	/** The scores. */
	Map<Integer, Float> scores;

	boolean must;

	/** The _conf. */
	static Configuration _conf;

	public GTerm(SearchableField[] fields, String q, String type,
			List<Stat> stats, Map<Integer, Float> scores) {
		this(fields, q, type, stats, scores, true);
	}

	/**
	 * Instantiates a new g term.
	 * 
	 * @param fields
	 *            the fields
	 * @param q
	 *            the q
	 * @param type
	 *            the type
	 * @param stats
	 *            the stats
	 * @param scores
	 *            the scores
	 */
	public GTerm(SearchableField[] fields, String q, String type,
			List<Stat> stats, Map<Integer, Float> scores, boolean must) {
		this.q = q;
		this.type = type;
		this.flds = fields;
		this.scores = scores;
		this.must = must;

		if (q != null) {
			q = q.toLowerCase();
			Html h = new Html(q);
			q = h.text();

			List<String> words = new ArrayList<String>();
			// split by: space, /, [, ], (, ), {, }, \
			Tokenizer t = new Tokenizer(q);
			String s1;
			while ((s1 = t.next()) != null) {
				if (!words.contains(s1)) {
					words.add(s1);
				}
			}

			terms = words;
		}

		this.stats = stats;
	}

	/**
	 * Creates the query.
	 * 
	 * @return the query
	 */
	public Query createQuery() {
		final List<String> words = new ArrayList<String>();

		List<String> removal = new ArrayList<String>();
		List<GroupQuery> queryList = new ArrayList<GroupQuery>();
		BooleanQuery query = new BooleanQuery();

		// log.debug("terms:" + terms);

		if (terms != null) {
			for (String s : terms) {
				if (s.length() == 0)
					continue;// exception thrown for
								// StringIndexOutOfBoundsException
				char firstChar = s.charAt(0);
				if (firstChar == '-') {
					/**
					 * the word is for removal
					 */
					s = s.substring(1);
					removal.add(s);
					continue;
				} else if (firstChar == '+') {
					/**
					 * do not load default filter for this word
					 */
					s = s.substring(1);
				} else {
					/**
					 * will load default filter for this word
					 */
					words.add(s);
				}

				/**
				 * create query for word except "-"
				 */
				GroupQuery q = _createQuery(s, false, must);
				if (q != null) {
					// only set one
					// stats = null;

					query.add(q, Occur.MUST);

					queryList.add(q);

					// log.debug("query=" + query);
				}

			}
		}

		/**
		 * add the type
		 */
		if (type != null) {
			GroupQuery q = new GroupQuery();
			q.add(new TermQuery(new Term(X._TYPE, type)), Occur.MUST);
			q.setStats(stats);

			query.add(q, Occur.MUST);

			log.debug("query=" + query);

		}

		return query;
	}

	/**
	 * _create query.
	 * 
	 * @param s
	 *            the s
	 * @param oneword
	 *            the oneword
	 * @return the group query
	 */
	protected GroupQuery _createQuery(String s, boolean oneword, boolean must) {
		try {
			if (s != null && s.length() > 0) {
				if (s.matches("^[\"'].*[\"']$")) {
					s = s.substring(1, s.length() - 1);
					return GMultiQuery.parse3(flds, s, true, must);
				}

				int i = s.indexOf(":");
				if (i > 0) {
					String f = s.substring(0, i);
					if (X._LIKE.equals(f)) {
						s = s.substring(i + 1);
						if (s.matches("^[\"'].*[\"']$")) {
							s = s.substring(1, s.length() - 1);
						}

						List<SearchableField> list = new ArrayList<SearchableField>();
						for (SearchableField f1 : flds) {
							if (f1 != null && f1.simliarfactor > 0) {
								list.add(f1);
							}
						}
						GroupQuery q = GMultiQuery.simliarparse(
								list.toArray(new SearchableField[list.size()]),
								s);
						q.setScores(scores);
						return q;
					} else {
						List<SearchableField> list = null;
						for (SearchableField f1 : flds) {
							if (f1 == null)
								continue;

							if (f1.field.equalsIgnoreCase(f)
									|| (f1.display != null && f1.display
											.equalsIgnoreCase(f))) {
								if (list == null) {
									list = new ArrayList<SearchableField>();
								}

								list.add(f1);
							}
						}

						if (list != null) {
							/**
							 * make sure find the whole word in the field
							 */
							s = s.substring(i + 1);
							return GMultiQuery.parse3(list
									.toArray(new SearchableField[list.size()]),
									s, oneword, must);
						}
					}
				}

				/**
				 * can not found mode
				 */
				s = s.replaceAll(":", " ");
				return GMultiQuery.parse3(flds, s, oneword, must);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * Find.
	 * 
	 * @param q
	 *            the q
	 * @return the list
	 */
	public static List<GroupQuery> find(Query q) {
		List<GroupQuery> list = new ArrayList<GroupQuery>();

		return _find(list, q);
	}

	/**
	 * _find.
	 * 
	 * @param list
	 *            the list
	 * @param q
	 *            the q
	 * @return the list
	 */
	private static List<GroupQuery> _find(List<GroupQuery> list, Query q) {
		if (q instanceof GroupQuery) {
			list.add((GroupQuery) q);
			return list;
		} else if (q instanceof BooleanQuery) {
			BooleanQuery b = (BooleanQuery) q;
			List<BooleanClause> list1 = b.clauses();

			for (BooleanClause c : list1) {
				_find(list, c.getQuery());
			}
		} else if (q instanceof FilteredQuery) {
			FilteredQuery fq = (FilteredQuery) q;
			_find(list, fq.getQuery());
		}

		return list;
	}

	/**
	 * The Class Tokenizer.
	 */
	static class Tokenizer {

		/** The Constant DELI. */
		private static final String DELI = " /\\[,]()";

		/** The s. */
		String s;

		/** The len. */
		int len;

		/** The pos. */
		int pos;

		/**
		 * Instantiates a new tokenizer.
		 * 
		 * @param s
		 *            the s
		 */
		public Tokenizer(String s) {
			this.s = s;
			pos = 0;
			len = s.length();
		}

		/**
		 * Next.
		 * 
		 * @return the string
		 */
		public String next() {
			int start = pos;
			while (pos < len) {
				char c = nextchar();
				if (DELI.indexOf(c) > -1) {
					// stop
					String ss = s.substring(start, pos - 1);
					if (X.EMPTY.equals(ss)) {
						return next();
					}
					return ss;
				} else if (c == '\'' || c == '"') {
					tochar(c);
				}
			}
			if (pos > start) {
				return s.substring(start, pos);
			}

			return null;
		}

		/**
		 * Nextchar.
		 * 
		 * @return the char
		 */
		private char nextchar() {
			return s.charAt(pos++);
		}

		/**
		 * Tochar.
		 * 
		 * @param c
		 *            the c
		 */
		private void tochar(char c) {
			while ((pos < len) && (s.charAt(pos) != c)) {
				pos++;
			}
			pos++;
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		String s = "asdas _like:asdsada sddds[asdasd]";
		System.out.println(s);

		Tokenizer t = new Tokenizer(s);
		String s1;
		while ((s1 = t.next()) != null) {
			System.out.println(s1);
		}
	}
}
