package com.giisoo.utils.base;

/**
 * The Class Char.
 */
public class Char {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		String s = "�ʼǱ�����$%666好的号码";

		System.out.println(s + ": chinese? " + isChinese(s));
	}

	/**
	 * Checks if is pure chinese.
	 *
	 * @param c the c
	 * @return true, if is pure chinese
	 */
	public static boolean isPureChinese(char c) {
		return ((c >= 0x3400 && c <= 0x4db5) || (c >= 0x4E00 && c <= 0x9FA5) || (c >= 0x9FA6 && c <= 0x9FBB) || (c >= 0xF900 && c <= 0xFA2D)
				|| (c >= 0xFA30 && c <= 0xFA6A) || (c >= 0xFA70 && c <= 0xFAD9) || (c >= 0x20000 && c <= 0x2A6D6) || (c >= 0x2F800 && c <= 0x2FA1D));

	}

	/**
	 * Checks if is chinese.
	 *
	 * @param s the s
	 * @return true, if is chinese
	 */
	public static boolean isChinese(String s) {
		if (s == null)
			return true;

		int len = s.length();

		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);

			if ((c > 0 && c < 0xff) || (c >= 0x3400 && c <= 0x4db5) || (c >= 0x4E00 && c <= 0x9FA5) || (c >= 0x9FA6 && c <= 0x9FBB) || (c >= 0xF900 && c <= 0xFA2D)
					|| (c >= 0xFA30 && c <= 0xFA6A) || (c >= 0xFA70 && c <= 0xFAD9) || (c >= 0x20000 && c <= 0x2A6D6) || (c >= 0x2F800 && c <= 0x2FA1D)) {

				continue;
			} else {
				System.out.println(c);

				return false;
			}

		}

		return true;
	}

}
