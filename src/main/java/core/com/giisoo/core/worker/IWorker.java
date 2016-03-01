package com.giisoo.core.worker;

import org.apache.commons.configuration.Configuration;

/**
 * The Interface IWorker.
 */
public interface IWorker {

	/**
	 * Inits the.
	 *
	 * @param conf the conf
	 * @return true, if successful
	 */
	public boolean init(Configuration conf);

	/**
	 * Start.
	 *
	 * @return true, if successful
	 */
	public boolean start();

}
