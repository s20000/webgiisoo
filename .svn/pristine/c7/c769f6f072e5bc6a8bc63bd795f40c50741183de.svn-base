package com.giisoo.utils.base;

import java.io.*;

/**
 * efficient IO stream utility.
 *
 * @author yjiang
 */
public class IoStream {

	/**
	 * Read string.
	 *
	 * @param in the in
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String readString(ObjectInput in) throws IOException {
		int len = in.readInt();
		if (len > 0) {
			byte[] b = new byte[len];
			in.readFully(b);
			return new String(b, 0, len);
		}

		return null;
	}

	/**
	 * Write string.
	 *
	 * @param out the out
	 * @param s the s
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeString(ObjectOutput out, String s) throws IOException {
		byte[] b = s == null ? null : s.getBytes();
		int len = b == null ? 0 : b.length;

		out.writeInt(len);
		if (len > 0) {
			out.write(b);
		}
	}
}
