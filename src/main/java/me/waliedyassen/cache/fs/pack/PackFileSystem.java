package me.waliedyassen.cache.fs.pack;

import me.waliedyassen.cache.CacheException;
import me.waliedyassen.cache.archive.Group;
import me.waliedyassen.cache.archive.Index;
import me.waliedyassen.cache.fs.FileSystem;
import me.waliedyassen.cache.io.Packet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * Represents a packed file system. The packed file system stores everything sequentially, the header contains the
 * index data (compressed using Js5 format) then it is followed by  all of the group data (compressed using the Js5
 * format).
 *
 * @author Walied K. Yassen
 */
public final class PackFileSystem implements FileSystem {

    /**
     * The path which leads to the file system.
     */
    private final Path path;

    /**
     * The raw data of all the groups in the pack.
     */
    private byte[][] groupData;

    /**
     * The raw data of the index table.
     */
    private byte[] indexData;

    /**
     * Constructs a new {@link PackFileSystem} type object instance.
     *
     * @param path the path which leads to the file system.
     */
    public PackFileSystem(Path path) {
        this.path = path;
        try (InputStream stream = Files.newInputStream(path, StandardOpenOption.READ)) {
            read(new DataInputStream(stream));
        } catch (IOException e) {
            throw new CacheException("Failed to read .js5 pack content from the input stream", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() {
        try (OutputStream stream = Files.newOutputStream(path, StandardOpenOption.WRITE)) {
            write(new DataOutputStream(stream));
        } catch (IOException e) {
            throw new CacheException("Failed to write .js5 pack content to the output stream", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] loadGroup(int id) {
        if (id < 0 || id >= groupData.length) {
            return null;
        }
        return groupData[id];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeGroup(int id, byte[] data) {
        if (id >= groupData.length) {
            groupData = Arrays.copyOf(groupData, id + 1);
        }
        groupData[id] = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] loadIndex() {
        return indexData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeIndex(byte[] data) {
        indexData = data;
    }

    /**
     * Reads the content of the file system from the specified {@link DataInputStream stream}.
     *
     * @param stream the stream to read the content of the file system from.
     * @throws IOException if anything occurs while reading the content of the input stream.
     */
    private void read(DataInputStream stream) throws IOException {
        // Read the raw data of the index
        indexData = readPackedChunk(stream);
        // Decode an index table object from the data
        Index index = new Index();
        index.decode(indexData);
        // Read teh raw data of the groups
        final Group[] groups = index.getGroups();
        for (int groupId = 0; groupId < groups.length; groupId++) {
            Group group = groups[groupId];
            if (group == null) {
                continue;
            }
            groupData[groupId] = readPackedChunk(stream);
        }
    }

    /**
     * Reads a single packed group raw data from the specified {@link DataInputStream stream}.
     *
     * @param stream the stream to read the packed data from.
     * @return the read packed data as a {@code byte[]} object..
     * @throws IOException if anything occurs while reading the packed group from the stream.
     */
    private byte[] readPackedChunk(DataInputStream stream) throws IOException {
        int ctype = stream.read();
        int clen = stream.readInt();
        int len = clen;
        if (ctype != 0) {
            clen += 4;
        }
        Packet packet = new Packet(clen + 5);
        packet.p1(ctype);
        packet.p4(len);
        for (; clen > 0x2000000; clen -= 0x2000000) {
            stream.readFully(packet.data(), packet.pos(), 0x2000000);
            packet.pos(packet.pos() + 0x2000000);
        }
        stream.readFully(packet.data(), packet.pos(), clen);
        return packet.data();
    }

    /**
     * Writes the content of the file system to the specified {@link DataOutputStream stream}.
     *
     * @param stream the output stream to write the content of the file system to.
     * @throws IOException if anything occurs while writing the content to the stream.
     */
    private void write(DataOutputStream stream) throws IOException {
        // TODO: Find a better way to access the index, potentially share the
        // index object between the file system and the archive.
        Index index = new Index();
        index.decode(indexData);
        // Write the index raw data
        stream.write(indexData);
        // Write all of the groups raw data
        final Group[] groups = index.getGroups();
        for (int groupId = 0; groupId < groups.length; groupId++) {
            Group group = groups[groupId];
            if (group == null) {
                continue;
            }
            stream.write(groupData[groupId]);
        }
    }
}
