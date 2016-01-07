/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc;

/**
 * utility of io
 * 
 * @author yjiang
 * 
 */
public class SimpleIoBuffer {

  private byte[] buf = null;// new byte[16*1024];
  private int s;
  private int e;

  private int mark;

  /**
	 * Capacity.
	 * 
	 * @return the int
	 */
  public synchronized int capacity() {
    return buf == null ? 0 : buf.length;
  }

  /**
	 * Creates the.
	 * 
	 * @param size
	 *            the size
	 * @return the simple io buffer
	 */
  public static SimpleIoBuffer create(int size) {
    SimpleIoBuffer sib = new SimpleIoBuffer();
    sib.buf = new byte[size];

    return sib;
  }

  /**
	 * Reset.
	 */
  public synchronized void reset() {
    if (mark > -1) {
      s = mark;
    }
  }

  public synchronized int getInt() {
    byte[] b = new byte[4];
    read(b);

    // System.out.println("int:" + Bean.toString(b));
    return ((int) b[0] & 0xff) << 24 | ((int) b[1] & 0xff) << 16 | ((int) b[2] & 0xff) << 8 | ((int) b[3] & 0xff);
  }

  /**
	 * Mark.
	 */
  public synchronized void mark() {
    mark = s;
  }

  /**
	 * Length.
	 * 
	 * @return the int
	 */
  public synchronized int length() {
    return e - s;
  }

  /**
	 * Append.
	 * 
	 * @param b
	 *            the b
	 */
  public synchronized void append(byte[] b) {
    append(b, 0, b.length);
  }

  /**
	 * Append.
	 * 
	 * @param b
	 *            the b
	 * @param offset
	 *            the offset
	 * @param len
	 *            the len
	 */
  public synchronized void append(byte[] b, int offset, int len) {
    mark = -1;

    // System.out.println("append:" + b.length);
    if (b == null || len == 0)
      return;

    if (buf == null) {
      buf = b;
      s = 0;
      e = len;
    } else if (buf.length - e >= len) {
      System.arraycopy(b, 0, buf, e, len);
      e += len;
    } else if (s + buf.length - e >= len) {
      System.arraycopy(buf, s, buf, 0, e - s);
      e -= s;
      s = 0;
      System.arraycopy(b, 0, buf, e, len);
      e += len;
    } else {
      /**
       * expand with extra 1KB
       */
      byte[] tmp = new byte[buf.length + len + 1024];
      System.arraycopy(buf, s, tmp, 0, e - s);
      e -= s;
      s = 0;
      System.arraycopy(b, 0, tmp, e, len);
      e += len;
      buf = tmp;
    }
  }

  /**
	 * Read.
	 * 
	 * @return the byte
	 */
  public synchronized byte read() {
    if (buf == null)
      return 0;

    return buf[s++];

  }

  /**
	 * Read.
	 * 
	 * @param b
	 *            the b
	 * @return the int
	 */
  public synchronized int read(byte[] b) {
    if (buf == null)
      return 0;
    int l = Math.min(b.length, e - s);
    System.arraycopy(buf, s, b, 0, l);

    s += l;

    return l;
  }

}
