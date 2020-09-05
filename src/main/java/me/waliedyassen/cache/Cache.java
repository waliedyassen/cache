package me.waliedyassen.cache;

import me.waliedyassen.cache.archive.Archive;
import me.waliedyassen.cache.fs.FileSystem;
import me.waliedyassen.cache.fs.FileSystemProvider;
import me.waliedyassen.cache.fs.java.JavaFileSystemProvider;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * The main class for the cache system, it is responsible for managing all of the file
 * systems that are used by this cache and handling resources.
 *
 * @author Walied K. Yassen
 */
public final class Cache {

    /**
     * A map which holds all of the currently opened archives. This map is lazily populated in most of the
     * scenarios.
     */
    private final Map<Integer, Archive> archives = new HashMap<>();

    /**
     * The file system provider which is used for feeding the archives with the {@link FileSystem} objects it
     * provides.
     */
    private final FileSystemProvider fileSystemProvider;

    /**
     * Constructs a new {@link Cache} type object instance.
     *
     * @param fileSystemProvider the file system provider of the cache.
     */
    private Cache(FileSystemProvider fileSystemProvider) {
        this.fileSystemProvider = fileSystemProvider;
    }

    /**
     * Returns the {@link Archive} object with the specified {@code id}. It will try to retrieve the {@link Archive} object
     * from the cached map, if the object is not present in the cache, it will attempt to load the object using
     * the {@link #load(int)} function and cache it if the load was successful.
     *
     * @param id the id of the archive that we are trying to retrieve.
     * @return the created or retrieved {@link Archive} object if it was available otherwiswe {@code null}.
     */
    public Archive get(int id) {
        Archive archive = archives.get(id);
        if (archive != null) {
            return archive;
        }
        return load(id);
    }

    /**
     * Attempts to load the {@link Archive} object with the specified {@code id} from the file system.
     *
     * @param id the id of the archive that we are trying to load.
     * @return the {@link Archive} object if it was available otherwise {@code null}.
     */
    private Archive load(int id) {
        FileSystem fileSystem = fileSystemProvider.provide(id);
        if (fileSystem == null) {
            return null;
        }
        Archive archive = new Archive(fileSystem);
        archives.put(id, archive);
        return archive;
    }

    /**
     * Opens a {@link Cache cache} using the specified {@link FileSystemProvider file system provider}.
     *
     * @param fileSystemProvider the file system provider which we will use for the cache.
     * @return the created {@link Cache} object.
     */
    public static Cache open(FileSystemProvider fileSystemProvider) {
        return new Cache(fileSystemProvider);
    }

    /**
     * Opens a {@link Cache cache} using the RuneScape Java Client File System (also known as Java Client).
     *
     * @param directory the path of the directory that contain the file system.
     * @return the created {@link Cache} object.
     */
    public static Cache openJava(Path directory) {
        return open(JavaFileSystemProvider.create(directory));
    }
}
