package com.giisoo.core.index;

import java.util.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.*;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.query.*;
import org.apache.lucene.spatial.vector.PointVectorStrategy;

import com.giisoo.core.bean.*;
import com.giisoo.core.cache.Cache;
import com.giisoo.core.index.Searchable.SearchableField;
import com.giisoo.core.query.*;
import com.giisoo.core.worker.WorkerTask;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;

// TODO: Auto-generated Javadoc
/**
 * The Class SpatialIndex.
 */
public class SpatialIndex {

	/** The log. */
	static Log log = LogFactory.getLog(SpatialIndex.class);

	/** The searchable. */
	static List<Class<? extends Searchable>> searchable;

	/** The type. */
	static Map<Class<? extends Searchable>, String> type;

	/** The searchfields. */
	static Map<Class<? extends Searchable>, SearchableField[]> searchfields;

	/** The index. */
	static RTIndex index;

	/** The formatter. */
	static Formatter formatter;

	/** The forcetype. */
	static boolean forcetype = true;

	// Spatial
	/** The ctx. */
	static private SpatialContext ctx;
	// "ctx" is the conventional variable name
	/** The strategy. */
	static private SpatialStrategy strategy;

	/** The cache_timeout. */
	static long cache_timeout = 0;

	/**
	 * Close.
	 */
	public static void close() {
		if (index != null) {
			index.close();
			index.commit();
			index = null;
		}
	}

	/**
	 * Inits the.
	 * 
	 * @param conf
	 *            the conf
	 * @param path
	 *            the path
	 * @param clazz
	 *            the clazz
	 */
	static void init(Configuration conf, String path,
			List<Class<? extends Searchable>> clazz) {
		// Typical geospatial context
		// These can also be constructed from SpatialContextFactory
		ctx = SpatialContext.GEO;

		// int maxLevels = 11;// results in sub-meter precision for geohash
		// TODO demo lookup by detail distance
		// This can also be constructed from SpatialPrefixTreeFactory
		// SpatialPrefixTree grid = new GeohashPrefixTree(ctx, maxLevels);

		strategy = new PointVectorStrategy(ctx, "myGeoField");

		Fuzzy.init(conf);

		GAnalyzer.init(conf);

		GMultiQuery.init();

		Analyzer a = GAnalyzer.getAnalyzer();
		// new SmartChineseAnalyzer(Version.LUCENE_45);
		// new IKAnalyzer(false);

		cache_timeout = conf.getInt("cache.timeout", 60) * 1000;

		String backup = conf.getString("backup.index.path", null);
		int backupoint = conf.getInt("backup.index.point", 1);
		boolean closerequired = "yes".equals(conf.getString(
				"backup.index.close", "no"));
		forcetype = "yes".equals(conf.getString("se.type.force", "yes"));

		RTIndex.MAX_RESULT = conf.getInt("search.max.result",
				RTIndex.MAX_RESULT);

		index = RTIndex.getIndex(path, a, backup, backupoint, closerequired);
		try {
			searchable = clazz;
			searchfields = new HashMap<Class<? extends Searchable>, SearchableField[]>();
			type = new HashMap<Class<? extends Searchable>, String>();

			for (Class<? extends Searchable> s : clazz) {
				SearchableField[] flds = s.newInstance().getSearchableFields();
				String name = s.getSimpleName().toLowerCase();
				searchfields.put(s, flds);
				type.put(s, name);
			}
		} catch (InstantiationException e) {
			log.error(e);
		} catch (IllegalAccessException e) {
			log.error(e);
		}

		if (index == null) {
			log.error("initialized spatial searching failed!!!");
			return;
		}

		formatter = new BoldFormatter();

		new IndexTask().schedule(0);
	}

	/**
	 * Builds the query.
	 * 
	 * @param clazz
	 *            the clazz
	 * @param wd
	 *            the wd
	 * @param stats
	 *            the stats
	 * @param scores
	 *            the scores
	 * @return the query
	 * @throws Exception
	 *             the exception
	 */
	private static Query buildQuery(Class<? extends Searchable> clazz,
			String wd, List<Stat> stats, Map<Integer, Float> scores,
			boolean must) throws Exception {
		String t = null;
		if (forcetype) {
			t = type.get(clazz);
		}

		SearchableField[] flds = searchfields.get(clazz);
		Query query = new GTerm(flds, wd, t, stats, scores, must).createQuery();

		// log.info(query);

		return query;
	}

	public static <T extends Searchable> SearchResults<T> search(
			Class<? extends Searchable> clazz, String q, double lat,
			double lng, double range, int start, int number) {
		return search(clazz, q, lat, lng, range, start, number, true);
	}

	/**
	 * Search.
	 * 
	 * @param clazz
	 *            the clazz
	 * @param q
	 *            the q
	 * @param lat
	 *            the lat
	 * @param lng
	 *            the lng
	 * @param range
	 *            the range
	 * @param start
	 *            the start
	 * @param number
	 *            the number
	 * @return the search results
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends Searchable> SearchResults<T> search(
			Class<? extends Searchable> clazz, String q, double lat,
			double lng, double range, int start, int number, boolean must) {
		try {
			log.info("searching ...: clazz=" + clazz + ",lat=" + lat + ", lng="
					+ lng + ", range:" + range);
			if (!index.avalid())
				return null;

			Query query = buildQuery(clazz, q, null, null, must);

			// log.info("query:" + query);

			SortField s0 = new SortField(X.GROUP, Type.INT);
			Point p = ctx.makePoint(lat, lng);
			ValueSource valueSource = strategy.makeDistanceValueSource(p);
			Sort sort = new Sort(s0, valueSource.getSortField(false))
					.rewrite(index.searcher);

			SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,
					ctx.makeCircle(lat, lng, DistanceUtils.dist2Degrees(range,
							DistanceUtils.EARTH_MEAN_RADIUS_KM)));
			String doc1Str = ctx.toString(p);
			String cachestr = clazz
					+ ":"
					+ UID.id(query.toString() + ":" + doc1Str + "," + start
							+ "," + number);
			Object o = Cache.get(cachestr);
			if (o != null && o instanceof SearchResults) {
				SearchResults sr = (SearchResults) o;
				if (sr.youngerThan(cache_timeout)) {

					log.info("get from cache: " + sr);

					return sr;
				}
			}

			Filter filter = strategy.makeFilter(args);
			TopDocs hits = index
					.search(query, filter, RTIndex.MAX_RESULT, sort);

			// 获得各条结果相对应的距离
			Map<Integer, Double> distances = new HashMap<Integer, Double>();
			for (int i = 0; i < hits.scoreDocs.length; i++) {
				Document doc1 = index.doc(hits.scoreDocs[i].doc);

				doc1Str = doc1.getField(strategy.getFieldName()).stringValue();
				Point doc1Point = (Point) ctx.readShape(doc1Str);
				double doc1DistDEG = ctx.getDistCalc().distance(
						args.getShape().getCenter(), doc1Point);

				distances.put(hits.scoreDocs[i].doc, DistanceUtils
						.degrees2Dist(doc1DistDEG,
								DistanceUtils.EARTH_MEAN_RADIUS_KM));
			}

			log.info("result:" + hits.scoreDocs.length);

			SearchResults ps = new SearchResults(hits);
			try {
				Highlighter highlighter = new Highlighter(formatter,
						new QueryScorer(query));
				ps.highlight(clazz, query, index.getReader(), index._analyzer,
						distances, start, number, highlighter);
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			}

			Cache.set(cachestr, ps);

			return ps;

		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public static <T extends Searchable> SearchResults<T> search(
			Class<? extends Searchable> clazz, String q, int start, int number,
			List<Stat> stats) {
		return search(clazz, q, start, number, stats, true);
	}

	/**
	 * Search.
	 * 
	 * @param clazz
	 *            the clazz
	 * @param q
	 *            the q
	 * @param start
	 *            the start
	 * @param number
	 *            the number
	 * @param stats
	 *            the stats
	 * @return the search results
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends Searchable> SearchResults<T> search(
			Class<? extends Searchable> clazz, String q, int start, int number,
			List<Stat> stats, boolean must) {
		try {
			if (!index.avalid())
				return null;

			TimeStamp t = TimeStamp.create();

			Map<Integer, Float> scores = new HashMap<Integer, Float>();
			Query query = buildQuery(clazz, q, stats, scores, must);

			// Query query = new QueryParser(Version.LUCENE_45, "name",
			// GAnalyzer.getAnalyzer()).parse(q);

			// log.info(query);

			String cachestr = clazz + ":"
					+ UID.id(query.toString() + "," + start + "," + number);
			Object o = Cache.get(cachestr);

			if (o != null && o instanceof SearchResults) {
				SearchResults sr = (SearchResults) o;
				if (sr.youngerThan(cache_timeout)) {

					log.info("get from cache: " + sr);
					return sr;
				}
			}

			Sort s = null;
			if (q == null || q.isEmpty()) {
				s = new Sort(new SortField(X.DATE, Type.LONG, true));
			}

			TopDocs hits = index.search(query, s);

			log.info("got: " + hits.totalHits + ", cost: " + t.past() + "ms");

			SearchResults ps = new SearchResults(hits);

			IndexReader reader = index.getReader();
			try {
				ps.setStats(stats);
				ps.setScores(scores);

				reader.incRef();
				Highlighter highlighter = new Highlighter(formatter,
						new QueryScorer(query));
				ps.highlight(clazz, query, reader, index._analyzer, null,
						start, number, highlighter);

				// log.info("after highlight, got: " + ps.total + ", list:" +
				// ps.getList());

				// must set back the stat as the original can not be transfer
				// back
				Cache.set(cachestr, ps);

				return ps;

			} finally {
				reader.decRef();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Location.
	 * 
	 * @param document
	 *            the document
	 * @param p
	 *            the p
	 */
	private static void location(Document document, Searchable p) {
		try {
			Location l = p.getLocation();
			if (l != null) {
				Point p1 = ctx.makePoint(l.getLng(), l.getLat());
				for (IndexableField f : strategy.createIndexableFields(p1)) {
					document.add(f);
				}

				document.add(new StoredField(strategy.getFieldName(), ctx
						.toString(p1)));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Index.
	 * 
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	public static boolean index(Searchable p) {
		if (index != null) {
			Document d = toDocument(p);

			// log.debug("doc=" + d);

			index.update(d);
			return true;
		}
		return false;
	}

	/**
	 * Flush.
	 */
	public static void flush() {
		if (index != null) {
			index.commit();
		}
	}

	/**
	 * Removes the.
	 * 
	 * @param id
	 *            the id
	 * @return true, if successful
	 */
	public static boolean remove(String id) {
		if (index != null) {
			index.remove(id);
			return true;
		}
		return false;
	}

	/**
	 * To document.
	 * 
	 * @param p
	 *            the p
	 * @return the document
	 */
	private static Document toDocument(Searchable p) {
		Document doc = new Document();

		// System.out.println(p.getClass().getSimpleName());
		doc.add(new Field(X._TYPE, p.getClass().getSimpleName().toLowerCase(),
				Field.Store.NO, Field.Index.ANALYZED));
		doc.add(new Field(X.ID, p.getId(), Field.Store.YES,
				Field.Index.ANALYZED));

		LongField f1 = new LongField(X.DATE, System.currentTimeMillis(),
				Field.Store.YES);
		doc.add(f1);

		p.toDoc(doc);

		location(doc, p);
		return doc;
	}

	/**
	 * The Class IndexTask.
	 */
	static class IndexTask extends WorkerTask {

		/** The interval. */
		long interval = X.AMINUTE;

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.worker.WorkerTask#getName()
		 */
		@Override
		public String getName() {
			return "rt.indexer";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.worker.WorkerTask#onExecute()
		 */
		@Override
		public void onExecute() {
			interval = X.AMINUTE;
			try {
				for (Class<? extends Searchable> s : searchable) {

					log.debug("searchable: " + s);

					List<String> list = PendIndex.eldest(s, 100);
					if (list != null && list.size() > 0) {
						for (String id : list) {
							Searchable p = s.newInstance();
							if (p.load(id) && p.isValid()) {
								boolean r = false;
								r = SpatialIndex.index(p);

								if (!r) {
									PendIndex.create(s, id);
								}
							} else {
								SpatialIndex.remove(id);
							}
						}
						interval = 0;
					}
				}
				Fuzzy.reload();

			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.worker.WorkerTask#onFinish()
		 */
		@Override
		public void onFinish() {
			if (index != null) {
				schedule(interval);
			}
		}
	}
}
