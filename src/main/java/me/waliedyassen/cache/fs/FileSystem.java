package me.waliedyassen.cache.fs;

/**
 * A file system is responsible for managing, loading, and storing file within a single archive. This
 * interface holds all of the common functionality between different kind of file system implementations.s
 *
 * @author Walied K. Yassen
 */
public interface FileSystem {

    /**
     * Loads the file system index table data.
     *
     * @return the file system index table data.
     */
    byte[] loadIndex();
}
