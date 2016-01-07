package com.giisoo.core.worker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class WorkerManager.
 */
public class WorkerManager {

	/** The log. */
	static Log log = LogFactory.getLog(WorkerManager.class);

	/** The list. */
	static ArrayList<IWorker> list = new ArrayList<IWorker>();

	
	/**
	 * Register.
	 *
	 * @param p the p
	 */
	public static void register(IWorker p) {
		list.add(p);
	}

	/**
	 * Start.
	 *
	 * @return true, if successful
	 */
	public synchronized static boolean start() {

		boolean r = true;
		for (IWorker p : list) {
			if (!p.start()) {
				r = false;
			}
		}

		return r;
	}

	/**
	 * Dump thread.
	 *
	 * @return the string
	 */
	public static String dumpThread() {
		StringBuilder sb = new StringBuilder();
		sb.append("====================begin of thread dump=============================\r\n");
		Map<Thread, StackTraceElement[]> m = Thread.getAllStackTraces();
		for (Iterator<Thread> it = m.keySet().iterator(); it.hasNext();) {
			Thread t = it.next();
			StackTraceElement[] st = m.get(t);

			sb.append(t.getName()).append(" - ").append(t.getState()).append(" - ").append(t.toString()).append("\r\n");
			for (StackTraceElement e : st) {
				sb.append("\t").append(e.getClassName()).append(".").append(e.getMethodName()).append("(").append(e.getLineNumber()).append(")").append("\r\n");
			}
		}
		sb.append("====================end of thread dump=============================");

		return sb.toString();
	}

	/**
	 * Stop.
	 *
	 * @param fast the fast
	 * @return true, if successful
	 */
	public static boolean stop(boolean fast) {
		boolean r = true;

		String s = dumpThread();
		log.info(s);
		// if (!fast) {
		// String s = dumpThread();
		// log.info(s);
		// }

		WorkerTask.stopAll(fast);

		return r;
	}

	/**
	 * Inits the.
	 *
	 * @param conf the conf
	 */
	public static void init(Configuration conf) {
		for (IWorker p : list) {
			p.init(conf);
		}
	}
}
