package me.waliedyassen.cache.fs;

/**
 * The file system provider is responsible for providing {@link FileSystem} objects for a given archive {@code key}. The
 * implementation may or may not cache the {@link FileSystem} objects. This should be called once per archive per
 * cache.
 *
 * @author Walied K. Yassen
 */
@FunctionalInterface
public interface FileSystemProvider {

    /**
     * Provides a {@link  FileSystem} object for the specified archive {@cvode key}.
     *
     * @param id the key of the archive which the file system is for.
     * @return the {@link FileSystem} object or {@code null} if the file system is not available.
     */
    FileSystem provide(int id);
}
