package me.waliedyassen.cache.io;

/**
 * Holds functions for computing CRC-32 checksum for blocks of data.
 *
 * @author Walied K. Yassen
 */
public final class CRC {

    /**
     * The polynomial value that will be used for the CRC-32 calculation.
     */
    private static final int CRC32_POLYNOMIAL = 0xEDB88320;

    /**
     * The CRC-32 table.
     */
    private static final int[] CRC32_TABLE;

    static {
        CRC32_TABLE = new int[256];
        for (int i = 0; i < 256; i++) {
            int temp = i;
            for (int j = 0; j < 8; j++) {
                if ((temp & 0x1) == 1) {
                    temp = temp >>> 1 ^ CRC32_POLYNOMIAL;
                } else {
                    temp >>>= 1;
                }
            }
            CRC32_TABLE[i] = temp;
        }
    }

    /**
     * Compute the CRC-32 of a block of byte data.
     *
     * @param data the byte data block that we want to calculate for.
     * @return the CRC-32 value of the data block.
     */
    public static int compute(byte[] data) {
        return compute(data, 0, data.length);
    }

    /**
     * Compute the CRC-32 of a block of byte data.
     *
     * @param data the byte data block that we want to calculate for.
     * @param off  the offset within the byte data block to start from.
     * @param len  the offset within the byte data block to stop at.
     * @return the CRC-32 value of the data block.
     */
    public static int compute(byte[] data, int off, int len) {
        int crc = 0xffffffff;
        for (; off < len; off++) {
            crc = crc >>> 8 ^ CRC32_TABLE[(crc ^ data[off]) & 0xff];
        }
        return ~crc;
    }

    private CRC() {
        // NOOP
    }
}
