package me.waliedyassen.cache.fs.java;

import me.waliedyassen.cache.CacheException;
import me.waliedyassen.cache.fs.FileSystem;
import me.waliedyassen.cache.fs.FileSystemProvider;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link FileSystemProvider} implementation that provides {@link JavaFileSystem} objects.
 *
 * @author Walied K. Yasen
 */
public final class JavaFileSystemProvider implements FileSystemProvider {

    /**
     * A map of all the cached file store.
     */
    private final Map<Integer, JavaFileSystem> cached = new HashMap<>();

    /**
     * Holds the ids of all the failed to load file systems, so we don't keep doing IO checks.
     */
    private final Set<Integer> erroneous = new HashSet<>();

    /**
     * The path of the directory that contains the file system.
     */
    private final Path directory;

    /**
     * A random access for the data file.
     */
    private RandomAccessFile dataFile;

    /**
     * The file store of the master index.
     */
    private JavaFileStore masterFs;

    /**
     * Constructs a new {@link JavaFileSystemProvider} type object intance.
     *
     * @param directory the path of hte directory that contain the file system.
     */
    private JavaFileSystemProvider(Path directory) {
        this.directory = directory;
        initialize();
    }

    /**
     * Initializes the file system provider.
     */
    private void initialize() {
        if (!Files.exists(directory)) {
            throw new CacheException("The specified directory for the file system does not exist.");
        }
        if (!Files.isDirectory(directory)) {
            throw CacheException.fileSystemInvalidDirectory();
        }
        Path dataFilePath = directory.resolve("main_file_cache.dat2");
        if (!Files.exists(dataFilePath)) {
            throw CacheException.fileSystemInvalidDirectory();
        }
        Path masterFilePath = directory.resolve("main_file_cache.idx255");
        if (!Files.exists(masterFilePath)) {
            throw CacheException.fileSystemInvalidDirectory();
        }
        try {
            dataFile = new RandomAccessFile(dataFilePath.toAbsolutePath().toString(), "rw");
        } catch (FileNotFoundException e) {
            throw new CacheException("Data file not found", e);
        }
        masterFs = loadStore(255);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystem provide(int id) {
        JavaFileSystem fs = cached.get(id);
        if (fs != null) {
            return fs;
        }
        if (erroneous.contains(id)) {
            return null;
        }
        JavaFileStore dataFs = loadStore(id);
        if (dataFs == null) {
            erroneous.add(id);
            return null;
        }
        fs = new JavaFileSystem(id, dataFs, masterFs);
        cached.put(id, fs);
        return fs;
    }

    /**
     * Loads the {@link JavaFileStore} for the archive with the specified {@code id}.
     *
     * @param id the id of archive that we want to load the file store for.
     * @return the loaded {@link JavaFileStore} if it exists othewrise {@code null}.
     */
    private JavaFileStore loadStore(int id) {
        Path idxFile = directory.resolve("main_file_cache.idx" + id);
        if (!Files.exists(idxFile)) {
            return null;
        }
        try {
            RandomAccessFile indexFile = new RandomAccessFile(idxFile.toString(), "rw");
            return new JavaFileStore(id, dataFile, indexFile);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
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
