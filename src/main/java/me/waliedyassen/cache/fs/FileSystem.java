package me.waliedyassen.cache.fs;

/**
 * A file system is responsible for managing, loading, and storing file within a single archive. This
 * interface holds all of the common functionality between different kind of file system implementations.s
 *
 * @author Walied K. Yassen
 */
public interface FileSystem {

    /**
     * Loads the raw data of the index table of this file system.
     *
     * @return the raw data of the index table.
     */
    byte[] loadIndex();

    /**
     * Stores the raw data of the index table of this file system.
     *
     * @param data the raw data of the file system index table.
     */
    void storeIndex(byte[] data);
}
