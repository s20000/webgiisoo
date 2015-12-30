/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.web;

/**
 * 
 * @author yjiang
 * 
 */
public class PageLabel implements Comparable<PageLabel> {
  String label;
  int pn;
  int s;
  int n;
  boolean curr;
  int seq;

  public int getPn() {
    return pn;
  }

  public boolean getCurr() {
    return curr;
  }

  public String getLabel() {
    return label;
  }

  public int getS() {
    return s;
  }

  public int getN() {
    return n;
  }

  /**
	 * Sets the pn.
	 * 
	 * @param pn
	 *            the pn
	 * @return the page label
	 */
  public PageLabel setPn(int pn) {
    this.pn = pn;
    return this;
  }

  /**
	 * Instantiates a new page label.
	 * 
	 * @param label
	 *            the label
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @param seq
	 *            the seq
	 * @param iscur
	 *            the iscur
	 */
  public PageLabel(String label, int s, int n, int seq, boolean iscur) {
    this.label = label;
    this.s = s;
    this.n = n;
    this.curr = iscur;
    this.seq = seq;
  }

  /**
	 * Instantiates a new page label.
	 * 
	 * @param label
	 *            the label
	 * @param s
	 *            the s
	 * @param n
	 *            the n
	 * @param seq
	 *            the seq
	 */
  public PageLabel(String label, int s, int n, int seq) {
    this.label = label;
    this.s = s;
    this.n = n;
    this.seq = seq;
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(PageLabel o) {
    if (seq < o.seq) {
      return -1;
    } else {
      return 1;
    }
  }
}