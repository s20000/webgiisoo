/**
 * Copyright (C) 2010 Gifox Networks
 * 
 * @project mms
 * @author jjiang
 * @date 2010-10-23
 */
package com.giisoo.core.bean;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.giisoo.core.cache.DefaultCachable;

// TODO: Auto-generated Javadoc
/**
 * The Class Bean.
 *
 * @param <T> the generic type
 */
public class Beans<T extends Bean> extends DefaultCachable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 2L;

  /** The log. */
  protected static Log log = LogFactory.getLog(Beans.class);

  /** The total. */
  int total;

  /** The list. */
  List<T> list;

  /**
   * Gets the total.
   *
   * @return the total
   */
  public int getTotal() {
    return total;
  }

  /**
   * Sets the total.
   *
   * @param total the new total
   */
  public void setTotal(int total) {
    this.total = total;
  }

  /**
   * Gets the list.
   *
   * @return the list
   */
  public List<T> getList() {
    return list;
  }

  /**
   * Sets the list.
   *
   * @param list the new list
   */
  public void setList(List<T> list) {
    this.list = list;
  }

}
