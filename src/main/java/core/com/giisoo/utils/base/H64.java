package com.giisoo.utils.base;

/**
 * The {@code H64} Class used to generate "Base64" string.
 * 
 * @author joe
 *
 */
public class H64 {

    /**
     * To string.
     *
     * @param l
     *            the l
     * @return the string
     */
    public static String toString(long l) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            int t = (int) (l & 0x3f);
            l = l >> 6;
            sb.append(chars[t]);
        }
        return sb.reverse().toString();
    }

    /** The Constant chars. */
    static final char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".toCharArray();

}
