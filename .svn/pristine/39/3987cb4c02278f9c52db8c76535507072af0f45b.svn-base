package com.giisoo.core.index;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.logging.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;

import com.giisoo.core.bean.X;
import com.giisoo.core.worker.WorkerTask;

/**
 * The Class RTIndex.
 */
class RTIndex {

	/** The log. */
	static Log log = LogFactory.getLog(RTIndex.class);

	/** The path. */
	private String path;

	/** The searcher. */
	IndexSearcher searcher;

	/** The reader. */
	IndexReader ramreader;
	IndexReader offlinereader;

	MultiReader reader;

	/** The indexer. */
	IndexWriter ramindexer;
	IndexWriter offlineindexer;

	/** The _analyzer. */
	Analyzer _analyzer;

	/** The backup. */
	String backup;

	/** The backupoint. */
	int backupoint;

	/** The backupopen. */
	boolean backupopen = true;

	/** The d. */
	Directory ram;
	Directory offline;

	/** The changed. */
	private int changed = 0;

	/** The closed. */
	private boolean closed = false;

	/** The delay commit. */
	private DelayCommitTask delayCommit = new DelayCommitTask();

	/** The delaycommitinterval. */
	private long DELAYCOMMITINTERVAL = X.AMINUTE;

	/** The lastcommittime. */
	private long lastcommittime = 0;

	/** The max result. */
	public static int MAX_RESULT = 1000;

	/** The buffer size. */
	static int BUFFER_SIZE = 100;

	/** The closerequired. */
	boolean closerequired = false;

	/** The indexerlock. */
	Object indexerlock = new Object();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new StringBuilder("@Gindex(").append(path).append(")")
				.toString();
	}

	/**
	 * Close.
	 */
	public void close() {
		closed = true;
		synchronized (indexerlock) {
			try {
				/**
				 * merge all ram to offline first
				 */
				offlineindexer.addIndexes(ram);

				reader.close();
				ramreader.close();
				// offlinereader.close();

				ramindexer.close(true);
				// offlineindexer.close(true);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			indexerlock.notifyAll();
		}
	}

	/**
	 * Gets the reader.
	 * 
	 * @return the reader
	 */
	public IndexReader getReader() {
		synchronized (indexerlock) {
			return reader;
		}
	}

	/**
	 * Instantiates a new RT index.
	 * 
	 * @param p
	 *            the p
	 * @param a
	 *            the a
	 * @param backup
	 *            the backup
	 * @param backupoint
	 *            the backupoint
	 */
	private RTIndex(String p, Analyzer a, String backup, int backupoint) {
		path = p;
		_analyzer = a;

		this.backup = backup;
		this.backupoint = backupoint;
	}

	/**
	 * _init.
	 * 
	 * @return true, if successful
	 */
	@SuppressWarnings("deprecation")
	private boolean _init() {
		try {
			File f = new File(path + "/write.lock");
			if (f.exists()) {
				f.delete();
			}

			backup();

			open();
			return true;
		} catch (Exception e) {
			log.error(e.getMessage() + ", path:" + path, e);
		}

		return false;
	}

	/**
	 * Open.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("deprecation")
	private void open() throws IOException {

		IndexWriterConfig wconf = new IndexWriterConfig(Version.LUCENE_45,
				_analyzer);
		wconf.setMaxBufferedDocs(10);
		wconf.setOpenMode(OpenMode.CREATE_OR_APPEND);

		ram = new RAMDirectory();
		ramindexer = new IndexWriter(ram, wconf);
		if (DirectoryReader.indexExists(ram)) {
			ramreader = IndexReader.open(ram);
		}

		if (offline == null) {
			offline = FSDirectory.open(new File(path));

			wconf = new IndexWriterConfig(Version.LUCENE_45, _analyzer);
			wconf.setMaxBufferedDocs(10);
			wconf.setOpenMode(OpenMode.CREATE_OR_APPEND);
			offlineindexer = new IndexWriter(offline, wconf);

			if (DirectoryReader.indexExists(offline)) {
				offlinereader = IndexReader.open(offline);

				int maxDoc = offlinereader.maxDoc();
				log.info(this + ", maxDoc: " + maxDoc);

			}
		}

		if (ramreader != null && offlinereader != null) {
			reader = new MultiReader(ramreader, offlinereader);
		} else if (ramreader != null) {
			reader = new MultiReader(ramreader);
		} else if (offlinereader != null) {
			reader = new MultiReader(offlinereader);
		}
		if (reader != null) {
			searcher = new IndexSearcher(reader);
		}

		delayCommit.schedule(X.AMINUTE);

	}

	/**
	 * Gets the index.
	 * 
	 * @param path
	 *            the path
	 * @param a
	 *            the a
	 * @param backup
	 *            the backup
	 * @param backupoint
	 *            the backupoint
	 * @param closerequired
	 *            the closerequired
	 * @return the index
	 */
	public static RTIndex getIndex(String path, Analyzer a, String backup,
			int backupoint, boolean closerequired) {
		RTIndex i = new RTIndex(path, a, backup, backupoint);
		i.closerequired = closerequired;
		if (i._init()) {
			return i;
		} else {
			return null;
		}
	}

	/**
	 * Update.
	 * 
	 * @param d
	 *            the d
	 */
	public void update(Document d) {
		try {
			String id = d.get(X.ID);

			synchronized (indexerlock) {
				Term t = new Term(X.ID, id);
				offlineindexer.deleteDocuments(t);

				ramindexer.deleteDocuments(t);
				ramindexer.addDocument(d);

				changed++;
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		_optimize();
	}

	/**
	 * Removes the.
	 * 
	 * @param id
	 *            the id
	 */
	public void remove(String id) {
		if (id != null) {
			try {
				synchronized (indexerlock) {

					Term t = new Term(X.ID, id);
					ramindexer.deleteDocuments(t);
					offlineindexer.deleteDocuments(t);

					changed++;
				}
			} catch (Exception e) {
				log.error("error remove:" + id);
			}
			_optimize();
		}
	}

	/**
	 * _optimize.
	 */
	protected void _optimize() {
		if (changed > BUFFER_SIZE) {
			commit();
		}
	}

	/**
	 * Search.
	 * 
	 * @param q
	 *            the q
	 * @param sort
	 *            the sort
	 * @return the top docs
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("deprecation")
	public TopDocs search(Query q, Sort sort) throws IOException {
		if (searcher == null)
			return null;

		if (sort == null) {
			return searcher.search(q, MAX_RESULT);
		} else {
			return searcher.search(q, null, MAX_RESULT, sort);
		}
	}

	/**
	 * Search.
	 * 
	 * @param query
	 *            the query
	 * @param filter
	 *            the filter
	 * @param maxValue
	 *            the max value
	 * @param sort
	 *            the sort
	 * @return the top docs
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("deprecation")
	public TopDocs search(Query query, Filter filter, int maxValue, Sort sort)
			throws IOException {
		if (searcher == null)
			return null;
		return searcher.search(query, filter, maxValue, sort);
	}

	/**
	 * Doc.
	 * 
	 * @param docID
	 *            the doc id
	 * @return the document
	 * @throws CorruptIndexException
	 *             the corrupt index exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Document doc(int docID) throws CorruptIndexException, IOException {
		return reader.document(docID);
	}

	/**
	 * Backup.
	 */
	public void backup() {
		if (backup != null) {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String sub = sdf.format(new Date(System.currentTimeMillis()));

			File f = new File(path);
			File b = new File(backup + "/" + sub);

			if (f.exists() && f.isDirectory()) {
				/**
				 * empty the backup folder
				 */
				if (b.exists()) {
					File[] list = b.listFiles();
					for (File t : list) {
						if (t.isFile()) {
							t.delete();
						}
					}
				} else {
					b.mkdirs();
				}

				/**
				 * copying files
				 */
				byte[] buf = new byte[1024 * 1024];
				File s[] = f.listFiles();
				for (File t : s) {
					if (t.isFile()) {
						OutputStream out = null;
						InputStream in = null;

						try {
							File d = new File(b.getAbsolutePath() + "/"
									+ t.getName());
							out = new FileOutputStream(d);
							in = new FileInputStream(t);

							int len = in.read(buf);
							while (len > 0) {
								out.write(buf, 0, len);
								len = in.read(buf);
							}
						} catch (Exception e) {
							log.error(path, e);
						} finally {
							if (out != null) {
								try {
									out.close();
								} catch (IOException e) {
									log.warn(path, e);
								}
							}
							if (in != null) {
								try {
									in.close();
								} catch (IOException e) {
									log.warn(path, e);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Commit.
	 */
	@SuppressWarnings("deprecation")
	public void commit() {

		lastcommittime = System.currentTimeMillis();

		try {
			if (changed > 0) {
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(System.currentTimeMillis());
				int now = c.get(Calendar.HOUR_OF_DAY);

				synchronized (indexerlock) {
					// indexer.optimize();
					// offlineindexer.commit();
					ramindexer.commit();
					offlineindexer.addIndexes(ram);
					offlineindexer.commit();

					changed = 0;

					if (backup != null && now == backupoint && backupopen) {
						backupopen = false;
						if (closerequired) {
							close();
						}
						backup();
						if (closerequired) {
							open();
						}
					} else if (now != backupoint) {
						backupopen = true;
					}
				}

				// if (!closed) {
				// // if (searcher != null) {
				// // // searcher.close();
				// // }
				// // if (reader != null) {
				// // if (reader.getRefCount() > 0) {
				// // final IndexReader r = reader;
				// // new WorkerTask() {
				// // int attempt = 0;
				// //
				// // @Override
				// // public String getName() {
				// // return "index.reader.close";
				// // }
				// //
				// // @Override
				// // public void onExecute() {
				// // attempt++;
				// // if (r.getRefCount() == 0 || attempt >= 3) {
				// // try {
				// // r.close();
				// // } catch (IOException e) {
				// // log.error(e.getMessage(), e);
				// // }
				// // }
				// // }
				// //
				// // @Override
				// // public void onFinish() {
				// // if (r.getRefCount() > 0 && attempt < 3) {
				// // this.schedule(1000);
				// // }
				// // }
				// //
				// // }.schedule(1000);
				// // } else {
				// // reader.close();
				// // }
				// // }
				//
				// reader = new MultiReader(ramreader, offlinereader);//
				// IndexReader.open(indexer.getDirectory());
				// searcher = new IndexSearcher(reader);
				// int maxDoc = reader.maxDoc();
				//
				// log.info(this + ", maxDoc: " + maxDoc);
				//
				// }
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			lastcommittime = System.currentTimeMillis();
		}
	}

	/**
	 * The Class DelayCommitTask.
	 */
	class DelayCommitTask extends WorkerTask {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.worker.WorkerTask#getName()
		 */
		@Override
		public String getName() {
			return "index.delaycommit";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.worker.WorkerTask#onExecute()
		 */
		@Override
		public void onExecute() {
			try {
				log.info(RTIndex.this + " delay committed starting");
				if (System.currentTimeMillis() - lastcommittime > DELAYCOMMITINTERVAL) {
					commit();
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			} finally {
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.giisoo.worker.WorkerTask#onFinish()
		 */
		@Override
		public void onFinish() {
			if (!closed) {
				this.schedule(X.AMINUTE);
			}
		}
	}

	/**
	 * Avalid.
	 * 
	 * @return true, if successful
	 */
	public boolean avalid() {
		return searcher != null && reader != null;
	}

}
