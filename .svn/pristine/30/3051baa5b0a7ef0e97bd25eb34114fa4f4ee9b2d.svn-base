package com.giisoo.core.rpc;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleIoBuffer.
 */
public class SimpleIoBuffer {

  /** The buf. */
  byte[] buf = null;// new byte[16*1024];
  
  /** The s. */
  int s;
  
  /** The e. */
  int e;

  /** The mark. */
  int mark;

  /**
   * Capacity.
   *
   * @return the int
   */
  public int capacity() {
    return buf == null ? 0 : buf.length;
  }

  /**
   * Creates the.
   *
   * @param size the size
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
  public void reset() {
    if (mark > -1) {
      s = mark;
    }
  }

  /**
   * Gets the int.
   *
   * @return the int
   */
  public int getInt() {
    byte[] b = new byte[4];
    read(b);
    return ((int) b[0] & 0xff) << 24 | ((int) b[1] & 0xff) << 16 | ((int) b[2] & 0xff) << 8 | ((int) b[3] & 0xff);
  }

  /**
   * Mark.
   */
  public void mark() {
    mark = s;
  }

  /**
   * Length.
   *
   * @return the int
   */
  public int length() {
    return e - s;
  }

  /**
   * Append.
   *
   * @param b the b
   */
  public synchronized void append(byte[] b) {
    mark = -1;

    // System.out.println("append:" + b.length);
    if (b == null || b.length == 0)
      return;

    if (buf == null) {
      buf = b;
      s = 0;
      e = b.length;
    } else if (buf.length - e >= b.length) {
      System.arraycopy(b, 0, buf, e, b.length);
      e += b.length;
    } else if (s + buf.length - e >= b.length) {
      System.arraycopy(buf, s, buf, 0, e - s);
      e -= s;
      s = 0;
      System.arraycopy(b, 0, buf, e, b.length);
      e += b.length;
    } else {
      /**
       * expand with extra 1KB
       */
      byte[] tmp = new byte[buf.length + b.length + 1024];
      System.arraycopy(buf, s, tmp, 0, e - s);
      e -= s;
      s = 0;
      System.arraycopy(b, 0, tmp, e, b.length);
      e += b.length;
      buf = tmp;
    }
  }

  /**
   * Read.
   *
   * @param b the b
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
