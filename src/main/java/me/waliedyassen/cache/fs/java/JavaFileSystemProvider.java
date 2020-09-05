package me.waliedyassen.cache.fs.java;

import me.waliedyassen.cache.fs.FileSystem;
import me.waliedyassen.cache.fs.FileSystemProvider;

import java.nio.file.Path;

/**
 * A {@link FileSystemProvider} implementation that provides {@link JavaFileSystem} objects.
 *
 * @author Walied K. Yasen
 */
public final class JavaFileSystemProvider implements FileSystemProvider {

    /**
     * The path of the directory that contains the file system.
     */
    private final Path directory;

    /**
     * Contruct a new {@link JavaFileSystemProvider} type object intance.
     *
     * @param directory the path of hte directory that contain the file system.
     */
    private JavaFileSystemProvider(Path directory) {
        this.directory = directory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystem provide(int id) {
        return null;
    }

    /**
     * Creates a new {@link JavaFileSystemProvider} type object.
     *
     * @param directory the path of the directory which contains the file system.
     * @return the created {@link JavaFileSystemProvider} object.
     */
    public static JavaFileSystemProvider create(Path directory) {
        return new JavaFileSystemProvider(directory);
    }
}
