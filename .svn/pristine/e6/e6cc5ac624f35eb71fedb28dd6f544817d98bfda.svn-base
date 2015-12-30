package com.giisoo.core.index;

import java.io.Serializable;
import java.util.*;

import com.giisoo.core.bean.X;

// TODO: Auto-generated Javadoc
/**
 * The Class Stat.
 */
public class Stat implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant METHOD_COUNT. */
  public static final int METHOD_COUNT = 1;
  
  /** The Constant METHOD_SUMMARY. */
  public static final int METHOD_SUMMARY = 2;

  /** The field. */
  public String field;
  
  /** The method. */
  int method;

  /** The stat. */
  Map<String, Integer> stat;

  /**
   * Instantiates a new stat.
   *
   * @param name the name
   * @param method the method
   */
  public Stat(String name, int method) {
    this.field = name;
    this.method = method;
  }

  /**
   * Creates the.
   *
   * @param name the name
   * @return the stat
   */
  public static Stat create(String name) {
    return new Stat(name);
  }

  /**
   * Instantiates a new stat.
   *
   * @param name the name
   */
  public Stat(String name) {
    this(name, METHOD_COUNT);
  }

  /**
   * Gets the field.
   *
   * @return the field
   */
  public String getField() {
    return field;
  }

  /**
   * Sets the field.
   *
   * @param field the new field
   */
  public void setField(String field) {
    this.field = field;
  }

  /**
   * Gets the.
   *
   * @return the map
   */
  public Map<String, Integer> get() {
    return stat;
  }

  /**
   * Adds the.
   *
   * @param value the value
   * @param amount the amount
   */
  public void add(String value, int amount) {
    if (X.EMPTY.equals(value)) {
      return;
    }

    if (stat == null) {
      stat = new TreeMap<String, Integer>();
    }
    if (stat.containsKey(value)) {
      int i = stat.get(value);
      amount += i;
    }

    stat.put(value, amount);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "[" + field + ":" + stat + "]";
  }

}
