package com.giisoo.utils.base;

import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * 3DES encrypt/decrypt utility.
 *
 * @author yjiang
 */
public class DES {

  /** The iv. */
  private static byte[] iv = { 1, 2, 3, 4, 5, 6, 7, 8 };

  /**
   * encode the data with the key using 3DES, the key MUST 24 bytes key.
   *
   * @param data the data
   * @param key the key
   * @return the byte[]
   * @throws Exception the exception
   */
  public static byte[] encode(byte[] data, byte[] key) throws Exception {

    /**
     * get the 3DES key
     */
    SecretKey securekey = new SecretKeySpec(key, "DESede");

    IvParameterSpec zeroIv = new IvParameterSpec(iv);

    /**
     * setting the full mode, this is useful for different OS
     */
    Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");

    cipher.init(Cipher.ENCRYPT_MODE, securekey, zeroIv);

    return cipher.doFinal(data);
  }

  /**
   * decode the data with key using 3DES, the key MUST 24 bytes.
   *
   * @param data the data
   * @param key the key
   * @return the byte[]
   * @throws Exception the exception
   */
  public static byte[] decode(byte[] data, byte[] key) throws Exception {

    /**
     * get the 3DES key
     */
    SecretKey securekey = new SecretKeySpec(key, "DESede");

    /**
     * setting the full mode, this is useful for different OS, different OS or JDK has different default, so...
     */
    Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");

    IvParameterSpec zeroIv = new IvParameterSpec(iv);

    cipher.init(Cipher.DECRYPT_MODE, securekey, zeroIv);

    return cipher.doFinal(data);
  }

}
