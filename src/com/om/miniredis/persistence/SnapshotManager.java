package com.om.miniredis.persistence;

import com.om.miniredis.store.DataStore;
import com.om.miniredis.store.SnapshotEntry;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class SnapshotManager {
    private final Path file;

    public SnapshotManager(Path file) {
        this.file = file;
    }

    public synchronized void save(DataStore store) throws IOException {
        List<SnapshotEntry> entries = store.snapshot(); // lock held only for this copy
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");

        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            out.writeInt(entries.size());
            for (SnapshotEntry e : entries) {
                out.writeUTF(e.key());
                out.writeUTF(e.value());
                out.writeLong(e.expiryMillis());
            }
        }
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public int load(DataStore store) throws IOException {
        if (!Files.exists(file)) return 0;

        List<SnapshotEntry> entries = new ArrayList<>();
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                entries.add(new SnapshotEntry(in.readUTF(), in.readUTF(), in.readLong()));
            }
        }
        store.restore(entries);
        return entries.size();
    }
}