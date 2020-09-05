package me.waliedyassen.cache.archive;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * The second top level object of the file system, it can only exit in archives and it can only hold file
 * and information about itself
 *
 * @author Walied K. Yassen
 */
@RequiredArgsConstructor
public final class Group {

    /**
     * The id of the group.
     */
    @Getter
    private final int id;

    /**
     * The version number of the group.
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int version;

    /**
     * The 32-bit integer hashed name of the group (using DJB2).
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int name32;

    /**
     * The CRC-32 checksum of the decompressed data of the group.
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int decompressedCrc;

    /**
     * The CRC-32 checksum of the compressed data of the group.
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int compressedCrc;

    /**
     * The whirlpool checksum of the group.
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private byte[] whirlpool;

    /**
     * The compressed size of the group.
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int compressedSize;

    /**
     * The decompressed size of the group.
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int decompressedSize;

    /**
     * An array of all the file names in the group.
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int[] fileName32;

    /**
     * An array of all the file data in the group.
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private byte[][] fileData;

    /**
     * Initializes the file array of the group.
     *
     * @param count  the amount of files within this group.
     * @param name32 whether or not to initialize the names.
     */
    void initFiles(int count, boolean name32) {
        fileData = new byte[count][];
        if (name32) {
            fileName32 = new int[count];
        }
    }

    /**
     * Returns the {@code byte[]} data of the file with the specified {@code id}. The returned array
     * should never be modified by the user, it will potentially cause issues in the cache library.
     *
     * @param id the id of hte file the want it's data.
     * @return the file data as a {@codeb byte[]} object if it was present otherwise {@code null}.
     */
    public byte[] getFile(int id) {
        if (!contains(id)) {
            return null;
        }
        return fileData[id];
    }

    /**
     * Returns the 32-bit integer name of the file with the specified {@code id}.
     *
     * @param id the id of the file that we want the name32 for.
     * @return the 32-bit integer name of the file or {@code -1} if it was not present.
     */
    public int getName32(int id) {
        if (fileName32 == null || !contains(id)) {
            return -1;
        }
        return fileName32[id];
    }

    /**
     * Checks whether or not the  group contains a file with the specified {@code id}.
     *
     * @param id the id of the file that we are checking.
     * @return <code>true</code> if it does otherwise <code>false</code>.
     */
    public boolean contains(int id) {
        return id >= 0 && id <= fileData.length && fileData[id] != null;
    }
}
