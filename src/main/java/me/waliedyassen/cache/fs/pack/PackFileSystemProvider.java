package me.waliedyassen.cache.fs.pack;

import lombok.RequiredArgsConstructor;
import me.waliedyassen.cache.fs.FileSystem;
import me.waliedyassen.cache.fs.FileSystemProvider;
import me.waliedyassen.cache.fs.java.JavaFileSystemProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link FileSystemProvider} implementation that provides {@link PackFileSystem} objects.
 *
 * @author Walied K. Yasen
 */
@RequiredArgsConstructor
public final class PackFileSystemProvider implements FileSystemProvider {

    /**
     * A map of all the cached file store.
     */
    private final Map<Integer, PackFileSystem> cached = new HashMap<>();

    /**
     * Holds the ids of all the failed to load file systems, so we don't keep doing IO checks.
     */
    private final Set<Integer> erroneous = new HashSet<>();

    /**
     * The path of the directory that contains the file system.
     */
    private final Path directory;

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystem provide(int id) {
        PackFileSystem fs = cached.get(id);
        if (fs != null) {
            return fs;
        }
        if (erroneous.contains(id)) {
            return null;
        }
        Path file = directory.resolve(id + ".js5");
        if (!Files.exists(file)) {
            erroneous.add(id);
            return null;
        }
        fs = new PackFileSystem(file);
        cached.put(id, fs);
        return fs;
    }

    /**
     * Creates a new {@link PackFileSystemProvider} type object.
     *
     * @param directory the path of the directory which contains the file system.
     * @return the created {@link PackFileSystemProvider} object.
     */
    public static PackFileSystemProvider create(Path directory) {
        return new PackFileSystemProvider(directory);
    }
}
