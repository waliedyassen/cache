package me.waliedyassen.cache.fs;

/**
 * A file system is responsible for managing, loading, and storing file within a single archive. This
 * interface holds all of the common functionality between different kind of file system implementations.s
 *
 * @author Walied K. Yassen
 */
public interface FileSystem {

    /**
     * Commit all of the changes of this file system.
     */
    void commit();

    /**
     * Loads the raw data of the group with the specified {@code id} from the file system.
     *
     * @param id the id of teh group that we want to load the data for.
     * @return the raw data of the group or if present otherwise {@code null}.
     */
    byte[] loadGroup(int id);

    /**
     * Stores the raw data of the group with the specified {@code id} in the file system.
     *
     * @param id   the id of the group the raw data is for.
     * @param data the raw data of the group that we want to store.
     */
    void storeGroup(int id, byte[] data);

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
