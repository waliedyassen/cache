package me.waliedyassen.cache.archive;

import lombok.Getter;
import me.waliedyassen.cache.compression.Js5Compression;
import me.waliedyassen.cache.io.Packet;

/**
 * Index table for a single {@link Archive}, it holds useful information about each entry of the archive (groups and files)
 * as well as information about the archive itself such a the compression type that is used and the protocol number.
 *
 * @author Walied K. Yassen
 */
public final class Index {

    /**
     * When flagged, groups and files will have DJB2 32 name stored.
     */
    private static final int FLAG_NAME32 = bit(0);

    /**
     * When flagged, groups will have whirlpool checksum stored.
     */
    private static final int FLAG_WHIRLPOOL = bit(1);

    /**
     * When flagged, groups will have compressed size stored.
     */
    private static final int FLAG_COMPRESSED_SIZE = bit(2);

    /**
     * When flagged, groups will have decompressed size stored.
     */
    private static final int FLAG_DECOMPRESSED_CRC = bit(3);

    /**
     * The protocol number of the index table which determines the byte structure of the index table.
     */
    @Getter
    private int protocolNumber;

    /**
     * The version number of index table which indicates how many revisions or updates did it have.
     */
    @Getter
    private int version;

    /**
     * Whether or not the debug 32-bit name hash is enabled.
     */
    @Getter
    private boolean optionName32;

    /**
     * Whether or not the whirlpool checksum is enabled.
     */
    @Getter
    private boolean optionWhirlpool;

    /**
     * Whether or not the compression size is enabled.
     */
    @Getter
    private boolean optionCompressedSize;

    /**
     * Whether or not decompressed size is enabled.
     */
    @Getter
    private boolean optionDecompressedCrc;

    /**
     * The groups that are within this index table.
     */
    @Getter
    private Group[] groups;

    /**
     * Decodes the index table content from the specified array of {@code byte} data.
     *
     * @param data the array of byte data to decode the content of the index from.
     */
    public void decode(byte[] data) {
        Packet packet = new Packet(Js5Compression.decompress(data));
        protocolNumber = packet.g1();
        if (protocolNumber < 5 || protocolNumber > 7) {
            throw new IllegalStateException("Incorrect JS5 protocol number: " + protocolNumber);
        }
        if (protocolNumber >= 6) {
            version = packet.g4();
        } else {
            version = 0;
        }
        int settings = packet.g1();
        optionName32 = (settings & FLAG_NAME32) == FLAG_NAME32;
        optionWhirlpool = (settings & FLAG_WHIRLPOOL) == FLAG_WHIRLPOOL;
        optionCompressedSize = (settings & FLAG_COMPRESSED_SIZE) == FLAG_COMPRESSED_SIZE;
        optionDecompressedCrc = (settings & FLAG_DECOMPRESSED_CRC) == FLAG_DECOMPRESSED_CRC;
        int validGroupsCount = readSize(packet);
        int baseGroupId = 0;
        int highestGroupId = -1;
        int[] groupIds = new int[validGroupsCount];
        for (int groupIndex = 0; groupIndex < validGroupsCount; groupIndex++) {
            groupIds[groupIndex] = baseGroupId += readSize(packet);
            if (groupIds[groupIndex] > highestGroupId) {
                highestGroupId = groupIds[groupIndex];
            }
        }
        groups = new Group[highestGroupId + 1];
        for (int groupIndex = 0; groupIndex < validGroupsCount; groupIndex++) {
            int groupId = groupIds[groupIndex];
            groups[groupId] = new Group(groupId);
        }
        if (optionName32) {
            for (int groupIndex = 0; groupIndex < validGroupsCount; groupIndex++) {
                groups[groupIds[groupIndex]].setName32(packet.g4());
            }
        }
        for (int groupIndex = 0; groupIndex < validGroupsCount; groupIndex++) {
            groups[groupIds[groupIndex]].setCompressedCrc(packet.g4());
        }
        if (optionDecompressedCrc) {
            for (int groupIndex = 0; groupIndex < validGroupsCount; groupIndex++) {
                groups[groupIds[groupIndex]].setDecompressedCrc(packet.g4());
            }
        }
        if (optionWhirlpool) {
            for (int groupIndex = 0; groupIndex < validGroupsCount; groupIndex++) {
                byte[] whirlpool = new byte[64];
                packet.gArrayBuffer(whirlpool, 0, 64);
                groups[groupIds[groupIndex]].setWhirlpool(whirlpool);
            }
        }
        if (optionCompressedSize) {
            for (int groupIndex = 0; groupIndex < validGroupsCount; groupIndex++) {
                groups[groupIds[groupIndex]].setCompressedSize(packet.g4());
                groups[groupIds[groupIndex]].setDecompressedSize(packet.g4());
            }
        }
        for (int groupIndex = 0; groupIndex < validGroupsCount; groupIndex++) {
            groups[groupIds[groupIndex]].setVersion(packet.g4());
        }
        int[] validFilesCountPerGroup = new int[groups.length];
        for (int groupIndex = 0; groupIndex < validGroupsCount; groupIndex++) {
            validFilesCountPerGroup[groupIds[groupIndex]] = readSize(packet);
        }
        int[][] fileIdsPerGroup = new int[groups.length][];
        for (int groupIndex = 0; groupIndex < validGroupsCount; groupIndex++) {
            Group group = groups[groupIds[groupIndex]];
            int validFileIdsCount = validFilesCountPerGroup[group.getId()];
            int baseFileId = 0;
            int highestFileId = -1;
            fileIdsPerGroup[group.getId()] = new int[validFileIdsCount];
            for (int fileIndex = 0; fileIndex < validFileIdsCount; fileIndex++) {
                int fileId = fileIdsPerGroup[group.getId()][fileIndex] = baseFileId += readSize(packet);
                if (fileId > highestFileId) {
                    highestFileId = fileId;
                }
            }
            group.initFiles(highestFileId + 1, optionName32);
        }
        if (optionName32) {
            for (int groupIndex = 0; groupIndex < validGroupsCount; groupIndex++) {
                Group group = groups[groupIds[groupIndex]];
                int validFilesCount = validFilesCountPerGroup[group.getId()];
                for (int fileIndex = 0; fileIndex < validFilesCount; fileIndex++) {
                    int fileId;
                    if (fileIdsPerGroup[group.getId()] != null) {
                        fileId = fileIdsPerGroup[group.getId()][fileIndex];
                    } else {
                        fileId = fileIndex;
                    }
                    group.getFileName32()[fileId] = packet.g4();
                }
            }
        }
    }

    /**
     * Writes the specified size type {@code value} to the specified {@link Packet packet}.
     *
     * @param packet the packet to write the size type to.
     * @param value  the value that we want to write.
     */
    private void writeSize(Packet packet, int value) {
        if (protocolNumber >= 7) {
            packet.pSmart2or4(value);
        } else {
            packet.p2(value);
        }
    }

    /**
     * Reads a size type from the specified {@link Packet packet}.
     *
     * @param packet the packet to read the size type from.
     * @return the read size value.
     */
    private int readSize(Packet packet) {
        if (protocolNumber >= 7) {
            return packet.gSmart2or4();
        } else {
            return packet.g2();
        }
    }

    /**
     * Returns the 32-bit integer representation of the specified {@code bit}.
     *
     * @param bit the bit which we want the representation for.
     * @return the 32-bit integer value of the bit.
     */
    private static int bit(int bit) {
        return 1 << bit;
    }
}
