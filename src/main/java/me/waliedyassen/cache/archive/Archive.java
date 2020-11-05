package me.waliedyassen.cache.archive;

import me.waliedyassen.cache.CacheException;
import me.waliedyassen.cache.fs.FileSystem;

/**
 * A single file system archive, it is the top level in the file system we are targeting. It holds groups and each group
 * can hold as many files as it needs.
 *
 * @author Walied K. Yassen
 */
public final class Archive {

    /**
     * The index table of the archive.
     */
    private final Index index = new Index();

    /**
     * The file ystem of the archive.
     */
    private final FileSystem fileSystem;

    /**
     * Constructs a new {@link Archive} type object instance.
     *
     * @param fileSystem the file system of the archive.
     */
    public Archive(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        initialize();
    }

    /**
     * Initializes the archive.
     */
    private void initialize() {
        byte[] data = fileSystem.loadIndex();
        if (data == null) {
            throw new CacheException("Failed to load the index data from the file system");
        }
        index.decode(data);
    }

    /**
     * Loads all of the data of the archive from the file system.
     */
    private void loadData() {
        for (Group group : index.getGroups()) {
            // NOOP
        }
    }


    /**
     * Returns the version number of the index of this archive.
     *
     * @return the version number of the index of this archive.
     */
    public int getVersion() {
        return index.getVersion();
    }

    /**
     * Returns the CRC-32 checksum of the raw index data of this archive.
     *
     * @return the CRC-32 checksum value.
     */
    public int getCrc() {
        return index.getCrc();
    }

    /**
     * Returns the whirlpool checksum of the raw index data of this archive.
     *
     * @return the whirlpool checksum value.
     */
    public byte[] getWhirlpool() {
        return index.getWhirlpool();
    }
}
