/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import org.apache.commons.configuration.Configuration;

/**
 * 
 * @author yjiang
 * 
 */
public interface LifeListener {

  /**
	 * Upgrade.
	 * 
	 * @param conf
	 *            the conf
	 * @param module
	 *            the module
	 */
  void upgrade(Configuration conf, Module module);

  /**
	 * Uninstall.
	 * 
	 * @param conf
	 *            the conf
	 * @param module
	 *            the module
	 */
  void uninstall(Configuration conf, Module module);

  /**
	 * On start.
	 * 
	 * @param conf
	 *            the conf
	 * @param module
	 *            the module
	 */
  void onStart(Configuration conf, Module module);

  /**
	 * On stop.
	 */
  void onStop();
  
}
