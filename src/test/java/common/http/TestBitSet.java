/**
 * 专注互联网,分享创造价值
 *  maoxiang@gmail.com
 *  2010-3-30下午11:13:04
 */
package common.http;

public class TestBitSet {

	public static void main(String[] args) throws Exception {
		byte[] bits = createBit(20);
		System.out.println(dumpBytes(bits));
		setBit(bits, 6, true); // 0000 0100 0000 0000 0000 0000
		setBit(bits, 12, true); // 0000 0100 0000 1000 0100 0000
		setBit(bits, 18, true); // 0000 0100 0000 0000 0100 0000
		System.out.println(dumpBytes(bits));
		setBit(bits, 13, false);
		System.out.println(dumpBytes(bits));
		System.out.println(getBit(bits, 18));
		System.out.println(getBit(bits, 12));
	}

	public static String dumpBytes(byte[] ints) throws Exception {
		StringBuffer sb = new StringBuffer();
		int loop = 0;
		for (byte b : ints) {
			if (loop++ == 16) {
				sb.append("\n");
				loop = 1;
			}
			sb.append(String.format("%x ", b));
		}
		return sb.toString();
	}

	public static byte[] createBit(int len) {
		int size = len / Byte.SIZE;
		if (len % Byte.SIZE > 0) {
			size++;
		}
		return new byte[size];
	}

	/** 取出某位，是0 还是1 */
	public static boolean getBit(byte[] bits, int pos) {
		int i = pos / Byte.SIZE;
		int b = bits[i];
		int j = pos % Byte.SIZE;
		byte c = (byte) (0x80 >>> (j - 1));
		return b == c;
	}

	/** 设置某位，是0 还是1 */
	public static void setBit(byte[] bits, int pos, boolean flag) {
		int i = pos / Byte.SIZE;
		byte b = bits[i];
		int j = pos % Byte.SIZE;
		byte c = (byte) (0x80 >>> (j - 1));
		if (flag) {
			bits[i] = (byte) (b | c);
		} else {
			c = (byte) (0xFF ^ c);
			bits[i] = (byte) (b & c);
		}
	}
}
