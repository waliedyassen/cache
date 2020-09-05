package me.waliedyassen.cache.io;

import java.nio.ByteBuffer;

/**
 * A wrapper class for {@link ByteBuffer} that holds some extra functions that we will be uing to decode and encode.
 *
 * @author Walied K. Yasen
 */
public final class Packet {

    /**
     * The backing byte buffer of the packet.
     */
    private final ByteBuffer buffer;

    /**
     * Constructs a new {@link Packet} type object instance.
     *
     * @param buffer the backing byte buffer of the packet.
     */
    public Packet(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Writes a single byte value to the buffer at the current position and increment the position by 1.
     *
     * @param value the byte value to write to the buffer.
     */
    public void p1(int value) {
        buffer.put((byte) value);
    }

    /**
     * Writes a 16-bit integer value to the buffer at the current position and increment the position by 2.
     *
     * @param value the 16-bit integer value to write to the buffer.
     */
    public void p2(int value) {
        buffer.put((byte) (value >> 8));
        buffer.put((byte) (value >> 0));
    }

    /**
     * Writes a little endian 16-bit integer value to the buffer at the current position and increment the position by 2.
     *
     * @param value the little endian 16-bit integer value to write to the buffer.
     */
    public void ip2(int value) {
        buffer.put((byte) (value >> 0));
        buffer.put((byte) (value >> 8));
    }

    /**
     * Writes a 24-bit integer value to the buffer at the current position and increment the position by 3.
     *
     * @param value the 24-bit integer value to write to the buffer.
     */
    public void p3(int value) {
        buffer.put((byte) (value >> 16));
        buffer.put((byte) (value >> 8));
        buffer.put((byte) (value >> 0));
    }

    /**
     * Writes a little endian 24-bit integer value to the buffer at the current position and increment the position by 3.
     *
     * @param value the little endian 24-bit integer value to write to the buffer.
     */
    public void ip3(int value) {
        buffer.put((byte) (value >> 0));
        buffer.put((byte) (value >> 8));
        buffer.put((byte) (value >> 16));
    }

    /**
     * Writes a 32-bit integer value to the buffer at the current position and increment the position by 4.
     *
     * @param value the 32-bit integer value to write to the buffer.
     */
    public void p4(int value) {
        buffer.put((byte) (value >> 24));
        buffer.put((byte) (value >> 16));
        buffer.put((byte) (value >> 8));
        buffer.put((byte) (value >> 0));
    }

    /**
     * Writes a little endian 32-bit integer value to the buffer at the current position and increment the position by 4.
     *
     * @param value the little endian 32-bit integer value to write to the buffer.
     */
    public void ip4(int value) {
        buffer.put((byte) (value >> 0));
        buffer.put((byte) (value >> 8));
        buffer.put((byte) (value >> 16));
        buffer.put((byte) (value >> 24));
    }

    /**
     * Writes a 40-bit integer value to the buffer at the current position and increment the position by 4.
     *
     * @param value the 40-bit integer value to write to the buffer.
     */
    public void p5(long value) {
        buffer.put((byte) (int) (value >> 32));
        buffer.put((byte) (int) (value >> 24));
        buffer.put((byte) (int) (value >> 16));
        buffer.put((byte) (int) (value >> 8));
        buffer.put((byte) (int) (value >> 0));
    }

    /**
     * Writes a little endian 40-bit integer value to the buffer at the current position and increment the position by 4.
     *
     * @param value the little endian 40-bit integer value to write to the buffer.
     */
    public void ip5(long value) {
        buffer.put((byte) (int) (value >> 0));
        buffer.put((byte) (int) (value >> 8));
        buffer.put((byte) (int) (value >> 16));
        buffer.put((byte) (int) (value >> 24));
        buffer.put((byte) (int) (value >> 32));
    }

    /**
     * Writes either 8-bit or 16-bit smart value at the current position and increment the position by the corresponding
     * amount of bytes written (either 1 for 8-bit or 2 for 16-bit).
     *
     * @param value the value to write to the buffer.
     * @throws IllegalStateException if the specified value is out of range (Less than 0 or greater than 32768).
     */
    public void pSmart1or2(int value) {
        if (value >= 0 && value < 128) {
            p1(value);
        } else if (value >= 0 && value < 32768) {
            p2(value + 32768);
        } else {
            throw new IllegalArgumentException("pSmart1or2 out of range: " + value);
        }
    }

    /**
     * Writes either 16-bit or 32-bit smart value at the current position and increment the position by the corresponding
     * amount of bytes written (either 2 for 16-bit or 4 for 32-bit).
     *
     * @param value the value to write to the buffer.
     * @throws IllegalStateException if the specified value is out of range (Less than -1).
     */
    public void pSmart2or4(int value) {
        if (value < -1) {
            throw new IllegalArgumentException("pSmart2or4 out of range: " + value);
        } else if (value == -1) {
            p2(Short.MAX_VALUE);
        } else if (value < Short.MAX_VALUE) {
            p2(value);
        } else {
            p4(value | 0x80);
        }
    }

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        Packet packet = new Packet(buffer);
        packet.g1();


    }

    /**
     * Writes the specified byte buffer at the current position and increment the position by the amount of bytes
     * we have written to the buffer.
     *
     * @param buf the byte buffer to write to the packet data.
     */
    public void pArrayBuffer(byte[] buf) {
        pArrayBuffer(buf, 0, buf.length);
    }

    /**
     * Writes the specified byte buffer at the current position and increment the position by the amount of bytes
     * we have written to the buffer.
     *
     * @param buf the byte buffer to write to the packet data.
     * @param off the offset (start pos) to start writing from in the byte buffer.
     * @param len the length (end pos) to stop writing at in the byte buffer.
     */
    public void pArrayBuffer(byte[] buf, int off, int len) {
        while (off < len) {
            buffer.put(buf[off++]);
        }
    }

    /**
     * Writes a string value to the buffer at the current position and increment the position by the string length + 1.
     *
     * @param value the string value to write to the buffer.
     */
    public void pstr(String value) {
        for (int ch : value.toCharArray()) {
            buffer.put((byte) ch);
        }
        buffer.put((byte) 0);
    }


    /**
     * Reads a byte value from the buffer at teh current position and increment the position by 1.
     *
     * @return the byte value that was read.
     */
    public int g1() {
        return buffer.get() & 0xff;
    }

    /**
     * Reads a 16-bit integer value from the buffer at the current position and increment the position by 2.
     *
     * @return the 16-bit integer value that was read.
     */
    public int g2() {
        int value = 0;
        value |= (buffer.get() & 0xff) << 8;
        value |= (buffer.get() & 0xff) << 0;
        return value;
    }

    /**
     * Reads a little endian 16-bit integer value from the buffer at the current position and increment the position by 2.
     *
     * @return the little endian 16-bit integer value that was read.
     */
    public int ig2() {
        int value = 0;
        value |= (buffer.get() & 0xff) << 0;
        value |= (buffer.get() & 0xff) << 8;
        return value;
    }

    /**
     * Reads a 24-bit integer value from the buffer at the current position and increment the position by 3.
     *
     * @return the 24-bit integer value that was read.
     */
    public int g3() {
        int value = 0;
        value |= (buffer.get() & 0xff) << 16;
        value |= (buffer.get() & 0xff) << 8;
        value |= (buffer.get() & 0xff) << 0;
        return value;
    }

    /**
     * Reads a little endian 24-bit integer value from the buffer at the current position and increment the position by 3.
     *
     * @return the little endian 24-bit integer value that was read.
     */
    public int ig3() {
        int value = 0;
        value |= (buffer.get() & 0xff) << 0;
        value |= (buffer.get() & 0xff) << 8;
        value |= (buffer.get() & 0xff) << 16;
        return value;
    }

    /**
     * Reads a 32-bit integer value from the buffer at the current position and increment the position by 4.
     *
     * @return the 32-bit integer value that was read.
     */
    public int g4() {
        int value = 0;
        value |= (buffer.get() & 0xff) << 24;
        value |= (buffer.get() & 0xff) << 16;
        value |= (buffer.get() & 0xff) << 8;
        value |= (buffer.get() & 0xff) << 0;
        return value;
    }

    /**
     * Reads a little endian 32-bit integer value from the buffer at the current position and increment the position by 4.
     *
     * @return the little endian 32-bit integer value that was read.
     */
    public int ig4() {
        int value = 0;
        value |= (buffer.get() & 0xff) << 0;
        value |= (buffer.get() & 0xff) << 8;
        value |= (buffer.get() & 0xff) << 16;
        value |= (buffer.get() & 0xff) << 24;
        return value;
    }

    /**
     * Reads either a 8-bit or a 16-bit smart from the buffer at the current position and increment the position by the
     * corresponding amount of bytes read (Either 1 bytes for 8-bit or 2 bytes for 16-bit).
     *
     * @return the smart value that was read.
     */
    public int gSmart1or2() {
        int value = buffer.get(buffer.position()) & 0xFF;
        if (value < 128) {
            return g1();
        }
        return g2() - 32768;
    }

    /**
     * Reads either a 16-bit or a 32-bit smart from the buffer at the current position and increment the position by the
     * corresponding amount of bytes read (Either 2 bytes for 16-bit or 4 bytes for 32-bit).
     *
     * @return the smart value that was read.
     */
    public int gSmart2or4() {
        if (buffer.get(buffer.position()) < 0) {
            return g4() & 0x7FFFFFFF;
        }
        return g2();
    }

    /**
     * Reads the specified byte buffer from the buffer at the current position and increment the position
     * by the amount of bytes read.
     *
     * @param buf the byte buffer to read the data into.
     */
    public void gArrayBuffer(byte[] buf) {
        gArrayBuffer(buf, 0, buf.length);
    }

    /**
     * Reads the specified byte buffer from the buffer at the current position and increment the position
     * by the amount of bytes read.
     *
     * @param buf the byte buffer to read the data into.
     * @param off the offset to start reading from in the byte buffer.
     * @param len the length to stop reading at in the byte buffer.
     */
    public void gArrayBuffer(byte[] buf, int off, int len) {
        while (off < len) {
            buf[off++] = buffer.get();
        }
    }
}
