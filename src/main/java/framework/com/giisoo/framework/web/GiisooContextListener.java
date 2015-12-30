/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

import javax.servlet.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author yjiang
 * 
 */
public class GiisooContextListener implements ServletContextListener {

  static Log log = LogFactory.getLog(GiisooContextListener.class);

  /* (non-Javadoc)
   * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
   */
  public void contextDestroyed(ServletContextEvent arg) {
    
  }

  /* (non-Javadoc)
   * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
   */
  public void contextInitialized(ServletContextEvent event) {
    String home = event.getServletContext().getRealPath("/");

    GiisooServlet.init(home, event.getServletContext().getContextPath());
    
  }

}
