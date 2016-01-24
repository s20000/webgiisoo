package com.giisoo.utils.base;

import com.giisoo.core.bean.X;

// TODO: Auto-generated Javadoc
/**
 * The Class ID.
 *
 * @author yjiang
 * @deprecated 
 */
public class ID {

  /**
   * Id.
   *
   * @param hash the hash
   * @return the string
   */
  public static String id(long hash) {
    return Long.toHexString(hash);
  }

  /**
   * Hash of id.
   *
   * @param id the id
   * @return the long
   */
  public static long hashOfId(String id) {
    int l = 0;
    byte[] b = id.getBytes();
    for (int i = 0; i < b.length; i++) {
      l <<= 4;
      if (b[i] >= 'a') {
        l += b[i] - 'a' + 10;
      } else {
        l += b[i] - '0';
      }
    }

    return l;
  }

  /**
   * Id.
   *
   * @param s the s
   * @return the string
   */
  public static String id(String s) {
    if (s == null)
      return X.EMPTY;

    return Long.toHexString(hash(s));
  }

  /**
   * Hash.
   *
   * @param s the s
   * @return the long
   */
  public static long hash(String s) {
    int h = 0;
    int l = 0;
    int len = s.length();
    char[] val = s.toCharArray();
    for (int i = 0; i < len; i++) {
      h = 31 * h + val[i];
      l = 29 * l + val[i];
    }
    return ((long) h << 32) | ((long) l & 0x0ffffffffL);
  }
}
