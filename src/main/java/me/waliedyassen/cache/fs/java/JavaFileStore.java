package me.waliedyassen.cache.fs.java;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The RuneScape Client file store implementation. This is based on the implementation RuneScape have in the client.
 *
 * @author Walied K. Yassen
 */
@RequiredArgsConstructor
public final class JavaFileStore {

    /**
     * The data file block size.
     */
    private static final int DATA_BLOCK_SIZE = 520;

    /**
     * The index file block size.
     */
    private static final int INDEX_BLOCK_SIZE = 6;

    /**
     * The size of the small header of the data block.
     */
    public static final int SMALL_DATA_BLOCK_HEADER_SIZE = 8;

    /**
     * The size of the large header of the data block.
     */
    public static final int LARGE_DATA_BLOCK_HEADER_SIZE = 10;

    /**
     * The size of the small content block of the data block.
     */
    public static final int SMALL_DATA_BLOCK_CONTENT_SIZE = DATA_BLOCK_SIZE - SMALL_DATA_BLOCK_HEADER_SIZE;

    /**
     * The size of the large content block of the data block.
     */
    public static final int LARGE_DATA_BLOCK_CONTENT_SIZE = DATA_BLOCK_SIZE - LARGE_DATA_BLOCK_HEADER_SIZE;

    /**
     * A temporary buffer used for buffering the I/O.
     */
    private final byte[] BUFFER = new byte[DATA_BLOCK_SIZE];

    /**
     * The archive id this file store is for.
     */
    private final int archiveId;

    /**
     * The file object of the data file on the disk.
     */
    @Getter
    private final RandomAccessFile dataFile;

    /**
     * The file object of the index file on the disk.
     */
    @Getter
    private final RandomAccessFile indexFile;

    /**
     * {@inheritDoc}
     */
    public byte[] load(int groupId) {
        synchronized (dataFile) {
            try {
                if (indexFile.length() < groupId * INDEX_BLOCK_SIZE + INDEX_BLOCK_SIZE) {
                    return null;
                }
                indexFile.seek(groupId * INDEX_BLOCK_SIZE);
                indexFile.read(BUFFER, 0, INDEX_BLOCK_SIZE);
                int size = g3(0);
                int block = g3(3);
                if (size < 0) {
                    return null;
                }
                if (block <= 0 || block > dataFile.length() / DATA_BLOCK_SIZE) {
                    return null;
                }
                byte[] data = new byte[size];
                int offset = 0;
                int chunk = 0;
                while (offset < size) {
                    if (block == 0) {
                        return null;
                    }
                    dataFile.seek(block * DATA_BLOCK_SIZE);
                    int count = size - offset;
                    int myHeaderLength;
                    int myGroup;
                    int myChunk;
                    int myNextBlock;
                    int myIndex;
                    if (groupId > 65535) {
                        if (count > LARGE_DATA_BLOCK_CONTENT_SIZE) {
                            count = LARGE_DATA_BLOCK_CONTENT_SIZE;
                        }
                        myHeaderLength = LARGE_DATA_BLOCK_HEADER_SIZE;
                        dataFile.read(BUFFER, 0, count + myHeaderLength);
                        myGroup = g4(0);
                        myChunk = g2(4);
                        myNextBlock = g3(6);
                        myIndex = g1(9);
                    } else {
                        if (count > SMALL_DATA_BLOCK_CONTENT_SIZE) {
                            count = SMALL_DATA_BLOCK_CONTENT_SIZE;
                        }
                        myHeaderLength = SMALL_DATA_BLOCK_HEADER_SIZE;
                        dataFile.read(BUFFER, 0, count + myHeaderLength);
                        myGroup = g2(0);
                        myChunk = g2(2);
                        myNextBlock = g3(4);
                        myIndex = g1(7);
                    }
                    if (myGroup != groupId || myChunk != chunk || myIndex != archiveId) {
                        return null;
                    }
                    if (myNextBlock < 0 || myNextBlock > dataFile.length() / DATA_BLOCK_SIZE) {
                        return null;
                    }
                    int numBytes = count + myHeaderLength;
                    for (int index = myHeaderLength; index < numBytes; index++) {
                        data[offset++] = BUFFER[index];
                    }
                    block = myNextBlock;
                    chunk++;
                }
                return data;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void store(int groupId, byte[] data) {
        if (!store(groupId, data, true)) {
            store(groupId, data, false);
        }
    }

    private boolean store(int groupId, byte[] data, boolean exists) {
        synchronized (this.dataFile) {
            try {
                int block;
                if (exists) {
                    if (indexFile.length() < groupId * INDEX_BLOCK_SIZE + INDEX_BLOCK_SIZE) {
                        return false;
                    }
                    indexFile.seek(groupId * INDEX_BLOCK_SIZE);
                    indexFile.read(BUFFER, 0, INDEX_BLOCK_SIZE);
                    block = g3(3);
                    if (block <= 0 || block > dataFile.length() / DATA_BLOCK_SIZE) {
                        return false;
                    }
                } else {
                    block = (int) ((dataFile.length() + (DATA_BLOCK_SIZE - 1)) / DATA_BLOCK_SIZE);
                    if (block == 0) {
                        block = 1;
                    }
                }
                p3(0, data.length);
                p3(3, block);
                indexFile.seek(groupId * INDEX_BLOCK_SIZE);
                indexFile.write(BUFFER, 0, INDEX_BLOCK_SIZE);
                int offset = 0;
                int chunk = 0;
                while (offset < data.length) {
                    int myNextBlock = 0;
                    if (exists) {
                        dataFile.seek(block * DATA_BLOCK_SIZE);
                        int myGroup;
                        int myChunk;
                        int myIndex;
                        if (groupId > 65535) {
                            try {
                                dataFile.read(BUFFER, 0, LARGE_DATA_BLOCK_HEADER_SIZE);
                            } catch (EOFException eof) {
                                break;
                            }
                            myGroup = g4(0);
                            myChunk = g2(4);
                            myNextBlock = g3(6);
                            myIndex = g1(9);
                        } else {
                            try {
                                dataFile.read(BUFFER, 0, SMALL_DATA_BLOCK_HEADER_SIZE);
                            } catch (EOFException eofexception) {
                                break;
                            }
                            myGroup = g2(0);
                            myChunk = g2(2);
                            myNextBlock = g3(4);
                            myIndex = g1(7);
                        }
                        if (myGroup != groupId || myChunk != chunk || myIndex != archiveId) {
                            return false;
                        }
                        if (myNextBlock < 0 || myNextBlock > dataFile.length() / DATA_BLOCK_SIZE) {
                            return false;
                        }
                    }
                    if (myNextBlock == 0) {
                        exists = false;
                        myNextBlock = (int) ((dataFile.length() + DATA_BLOCK_SIZE - 1) / DATA_BLOCK_SIZE);
                        if (myNextBlock == 0) {
                            myNextBlock++;
                        }
                        if (myNextBlock == block) {
                            myNextBlock++;
                        }
                    }
                    if (data.length - offset <= SMALL_DATA_BLOCK_CONTENT_SIZE) {
                        myNextBlock = 0;
                    }
                    if (groupId > 65535) {
                        p4(0, groupId);
                        p2(4, chunk);
                        p3(6, myNextBlock);
                        p1(9, archiveId);
                        dataFile.seek(block * DATA_BLOCK_SIZE);
                        dataFile.write(BUFFER, 0, LARGE_DATA_BLOCK_HEADER_SIZE);
                        int numBytes = data.length - offset;
                        if (numBytes > LARGE_DATA_BLOCK_CONTENT_SIZE) {
                            numBytes = LARGE_DATA_BLOCK_CONTENT_SIZE;
                        }
                        dataFile.write(data, offset, numBytes);
                        offset += numBytes;
                    } else {
                        p2(0, groupId);
                        p2(2, chunk);
                        p3(4, myNextBlock);
                        p1(7, archiveId);
                        dataFile.seek(block * DATA_BLOCK_SIZE);
                        dataFile.write(BUFFER, 0, SMALL_DATA_BLOCK_HEADER_SIZE);
                        int numBytes = data.length - offset;
                        if (numBytes > SMALL_DATA_BLOCK_CONTENT_SIZE) {
                            numBytes = SMALL_DATA_BLOCK_CONTENT_SIZE;
                        }
                        dataFile.write(data, offset, numBytes);
                        offset += numBytes;
                    }
                    block = myNextBlock;
                    chunk++;
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Returns the amount of groups within this file store.
     *
     * @return the amount of groups within this file store.
     */
    public int getGroupCount() {
        try {
            return (int) (indexFile.length() / INDEX_BLOCK_SIZE);
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Writes a 8-bit integer value to the specified {@code position} in the buffer.
     *
     * @param pos the position to write the 8-bit integer value to.
     */
    private void p1(int pos, int value) {
        BUFFER[pos] = (byte) value;
    }

    /**
     * Writes a 16-bit integer value to the specified {@code position} in the buffer.
     *
     * @param pos the position to write the 16-bit integer value to.
     */
    private void p2(int pos, int value) {
        p1(pos++, value >> 8);
        p1(pos, value);
    }

    /**
     * Writes a 24-bit integer value to the specified {@code position} in the buffer.
     *
     * @param pos the position to write the 24-bit integer value to.
     */
    private void p3(int pos, int value) {
        p1(pos++, value >> 16);
        p1(pos++, value >> 8);
        p1(pos, value);
    }

    /**
     * Writes a 32-bit integer value to the specified {@code position} in the buffer.
     *
     * @param pos the position to write the 32-bit integer value to.
     */
    private void p4(int pos, int value) {
        p1(pos++, value >> 24);
        p1(pos++, value >> 16);
        p1(pos++, value >> 8);
        p1(pos, value);
    }

    /**
     * Reads and returns a 8-bit integer value from the specified {@code position} in the buffer.
     *
     * @param pos the position to read the 8-bit integer value from.
     * @return the value that was read from the buffer.
     */
    private int g1(int pos) {
        return BUFFER[pos] & 0xff;
    }

    /**
     * Reads and returns a 16-bit integer value from the specified {@code position} in the buffer.
     *
     * @param pos the position to read the 16-bit integer value from.
     * @return the value that was read from the buffer.
     */
    private int g2(int pos) {
        return (g1(pos++) << 8) | g1(pos);
    }

    /**
     * Reads and returns a 24-bit integer value from the specified {@code position} in the buffer.
     *
     * @param pos the position to read the 24-bit integer value from.
     * @return the value that was read from the buffer.
     */
    private int g3(int pos) {
        return (g1(pos++) << 16) | (g1(pos++) << 8) | g1(pos);
    }

    /**
     * Reads and returns a 32-bit integer value from the specified {@code position} in the buffer.
     *
     * @param pos the position to read the 32-bit integer value from.
     * @return the value that was read from the buffer.
     */
    private int g4(int pos) {
        return (g1(pos++) << 24) | (g1(pos++) << 16) | (g1(pos++) << 8) | g1(pos);
    }
}
