/**
 * Copyright (C) 2010 Gifox Networks
 * 
 * @project mms
 * @author jjiang
 * @date 2010-10-23
 */
package com.giisoo.core.bean;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class Values.
 *
 * @deprecated The Class Values.
 */
public class Values {

  /** The _k. */
  private ArrayList<String> _k = new ArrayList<String>();
  
  /** The _v. */
  private ArrayList<Object> _v = new ArrayList<Object>();

  /**
   * Checks if is empty.
   *
   * @return true, if is empty
   */
  public boolean isEmpty() {
    return _k.isEmpty();
  }

  /**
   * Put.
   *
   * @param key          the key
   * @param value          the value
   * @return the values
   */
  public Values put(String key, Object value) {
    if (key == null || value == null)
      return this;

    if (!_k.contains(key)) {
      _k.add(key);
      _v.add(value);
    }

    return this;
  }

  /**
   * Sets the.
   *
   * @param key the key
   * @param value the value
   * @return the values
   */
  public Values set(String key, Object value) {
    return put(key, value);
  }

  /**
   * Key set.
   * 
   * @return the array list
   */
  public ArrayList<String> keySet() {
    return _k;
  }

  /**
   * Values.
   * 
   * @return the array list
   */
  public ArrayList<Object> values() {
    return _v;
  }

  /**
   * Clear.
   */
  public void clear() {
    _k.clear();
    _v.clear();
  }
}
