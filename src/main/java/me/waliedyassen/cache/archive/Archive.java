package me.waliedyassen.cache.archive;

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
    }
}
