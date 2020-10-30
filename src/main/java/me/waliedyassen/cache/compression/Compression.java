package me.waliedyassen.cache.compression;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.zip.GZIPInputStream;

/**
 * Contains all of the possible methods of compression that the cache library uses.
 *
 * @author Walied K. Yassen
 */
public enum Compression {
    NONE {
        /**
         * {@inheritDoc}
         */
        @Override
        public void decompress(byte[] input, int ioff, byte[] output) throws IOException {
            System.arraycopy(input, ioff, output, 0, output.length);

        }
    },
    BZIP2 {
        /**
         * The BZip2 header, this is stripped from Js5 BZ2 decompression, so we have to manually
         * add it when passing to a compressor streams.
         */
        private final byte[] BZ2_HEADER = new byte[]{'B', 'Z', 'h', '1'};

        /**
         * {@inheritDoc}
         */
        @Override
        public void decompress(byte[] input, int ioff, byte[] output) throws IOException {
            ByteArrayInputStream his = new ByteArrayInputStream(BZ2_HEADER);
            ByteArrayInputStream dis = new ByteArrayInputStream(input, ioff, input.length);
            try (BZip2CompressorInputStream bzip2 = new BZip2CompressorInputStream(new SequenceInputStream(his, dis))) {
                int cursor = 0;
                do {
                    cursor += bzip2.read(output, cursor, output.length - cursor);
                } while (cursor < output.length);
            }
        }
    },
    GZIP {
        /**
         * {@inheritDoc}
         */
        @Override
        public void decompress(byte[] input, int ioff, byte[] output) throws IOException {
            ByteArrayInputStream dis = new ByteArrayInputStream(input, ioff, input.length);
            try (GZIPInputStream gzip = new GZIPInputStream(dis)) {
                int cursor = 0;
                do {
                    cursor += gzip.read(output, cursor, output.length - cursor);
                } while (cursor < output.length);
            }
        }
    },
    LZMA {
        /**
         * {@inheritDoc}
         */
        @Override
        public void decompress(byte[] input, int ioff, byte[] output) throws IOException {
            throw new UnsupportedOperationException();
        }
    };

    /**
     * Decompresses the specified {@code input} data and write the decompressed data
     * to the {@code output} buffer.
     *
     * @param input  the input buffer which contains the compressed data.
     * @param ioff   the input buffer offset to start reading data at.
     * @param output the output buffer which the decompressed data will be placed in.
     * @throws IOException if anything occurs while decompressing the input buffer or writing to the output buffer.
     */
    public abstract void decompress(byte[] input, int ioff, byte[] output) throws IOException;

    /**
     * Looks-up for the {@link Compression} with the specified {@code ctype}.
     *
     * @param ctype the type of the compression that we are looking for.
     * @return the {@link Compression} if found otherwise {@code null}.
     */
    public static Compression forId(int ctype) {
        switch (ctype) {
            case 0:
                return NONE;
            case 1:
                return BZIP2;
            case 2:
                return GZIP;
            case 3:
                return LZMA;
            default:
                return null;
        }
    }
}
