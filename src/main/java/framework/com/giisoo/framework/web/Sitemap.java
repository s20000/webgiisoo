/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import java.io.*;

import org.apache.commons.logging.*;

/**
 * create sitemap utility
 * 
 * @author yjiang
 * 
 */
public class Sitemap {

  static Log log = LogFactory.getLog(Sitemap.class);

  /**
	 * Adds the.
	 * 
	 * @param uri
	 *            the uri
	 */
  public static void add(String uri) {

    /**
     * find the sitemap.txt file
     */
    File f = Module.home.loadResource("/sitemap.txt");

    BufferedReader in = null;
    PrintStream out = null;
    try {
      /**
       * test has same uri ?, if so, ignore
       */
      in = new BufferedReader(new FileReader(f));
      String line = in.readLine();
      while (line != null) {
        if (line.equals(uri)) {
          return;
        }
      }

      /**
       * append the uri to the sitemap.txt
       */
      out = new PrintStream(new FileOutputStream(f, true));
      out.println(uri);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          log.error(e);
        }
      }
      if (out != null) {
        out.close();
      }
    }
  }

}
