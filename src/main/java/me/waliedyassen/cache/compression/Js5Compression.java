package me.waliedyassen.cache.compression;

import java.io.IOException;

/**
 * Holds utilities to decompress or compress using the Js5 compression format.
 *
 * @author Walied K. Yassen
 */
public final class Js5Compression {

    /**
     * Decompresses the specified {@code data} using Js5 compression format.
     *
     * @param data the data that we want to decompress.
     * @return the decompressed data.
     */
    public static byte[] decompress(byte[] data) {
        if (data.length < 5) {
            throw new IllegalArgumentException("The specified data is not properly compressed");
        }
        int ctype = data[0] & 0xff;
        Compression compression = Compression.forId(ctype);
        if (compression == null) {
            throw new IllegalArgumentException("Unrecognized compression method: " + ctype);
        }
        int clen = (data[1] & 0xff) << 24 | (data[2] & 0xff) << 16 | (data[3] & 0xff) << 8 | (data[4] & 0xff);
        byte[] output;
        if (compression == Compression.NONE) {
            output = new byte[clen];
        } else {
            int dlen = (data[5] & 0xff) << 24 | (data[6] & 0xff) << 16 | (data[7] & 0xff) << 8 | (data[8] & 0xff);
            output = new byte[dlen];
        }
        try {
            compression.decompress(data, compression == Compression.NONE ? 5 : 9, output);
        } catch (IOException e) {
            throw new IllegalArgumentException("The specified data is not properly compressed", e);
        }
        return output;
    }

    private Js5Compression() {
        // NOOP
    }
}
