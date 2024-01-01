package me.modmuss50.optifabric.patcher;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class ClassCache {
    private final Map<String, byte[]> classes = new HashMap<>();
    private byte[] hash;

    public ClassCache(byte[] hash) {
        this.hash = hash;
    }

    private ClassCache() {
    }

    public static ClassCache read(File input) throws IOException {
        FileInputStream fis = new FileInputStream(input);
        GZIPInputStream gis = new GZIPInputStream(fis);
        DataInputStream dis = new DataInputStream(gis);

        ClassCache classCache = new ClassCache();

        // read the hash
        int hashSize = dis.readInt();
        byte[] hash = new byte[hashSize];
        dis.readFully(hash);
        classCache.hash = hash;

        int count = dis.readInt();
        for (int i = 0; i < count; i++) {
            int nameByteCount = dis.readInt();
            byte[] nameBytes = new byte[nameByteCount];
            dis.readFully(nameBytes);
            String name = new String(nameBytes, StandardCharsets.UTF_8);

            int byteCount = dis.readInt();
            byte[] bytes = new byte[byteCount];
            dis.readFully(bytes);
            classCache.classes.put(name, bytes);
        }

        dis.close();
        gis.close();
        fis.close();
        return classCache;
    }

    public void addClass(String name, byte[] bytes) {
        if (this.classes.containsKey(name)) {
            throw new UnsupportedOperationException(name + " is already in the classcache");
        }
        Objects.requireNonNull(bytes, "bytes cannot be null");
        this.classes.put(name, bytes);
    }

    public byte[] getClass(String name) {
        return this.classes.get(name);
    }

    public byte[] getAndRemove(String name) {
        byte[] bytes = this.getClass(name);
        this.classes.remove(name);
        return bytes;
    }

    public byte[] getHash() {
        return this.hash;
    }

    public Set<String> getClasses() {
        return this.classes.keySet();
    }

    public void save(Path output) throws IOException {
        Files.deleteIfExists(output);

        FileOutputStream fos = new FileOutputStream(output.toFile());
        GZIPOutputStream gos = new GZIPOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(gos);

        // write the hash
        dos.writeInt(this.hash.length);
        dos.write(this.hash);

        // write the number of classes
        dos.writeInt(this.classes.size());
        for (Map.Entry<String, byte[]> clazz : this.classes.entrySet()) {
            String name = clazz.getKey();
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            byte[] bytes = clazz.getValue();

            // write the name
            dos.writeInt(nameBytes.length);
            dos.write(nameBytes);

            // write the actual bytes
            dos.writeInt(bytes.length);
            dos.write(bytes);
        }
        dos.flush();
        dos.close();
        gos.flush();
        gos.close();
        fos.flush();
        fos.close();
    }
}
