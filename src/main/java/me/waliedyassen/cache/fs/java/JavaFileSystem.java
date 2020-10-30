package me.waliedyassen.cache.fs.java;

import lombok.RequiredArgsConstructor;
import me.waliedyassen.cache.fs.FileSystem;

/**
 * The RuneScape Java Client file system implementation.
 *
 * @author Walied K. Yassen
 */
@RequiredArgsConstructor
public final class JavaFileSystem implements FileSystem {

    /**
     * The id of the file store.
     */
    private final int id;

    /**
     * The data file store.
     */
    private final JavaFileStore dataFs;

    /**
     * The master file store.
     */
    private final JavaFileStore masterFs;

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() {
        // NOOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] loadGroup(int id) {
        return dataFs.load(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeGroup(int id, byte[] data) {
        dataFs.store(id, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] loadIndex() {
        return masterFs.load(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeIndex(byte[] data) {
        masterFs.store(id, data);
    }
}
