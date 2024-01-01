package me.modmuss50.optifabric;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.Comparator;
import java.util.stream.Stream;

public class IOUtils {
    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }

    public static void writeByteArrayToFile(Path file, final byte[] data) throws IOException {
        Files.createDirectories(file.getParent());
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        try (OutputStream out = Files.newOutputStream(file)) {
            out.write(data, 0, data.length);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteDirectory(Path optifineClasses) throws IOException {
        try (Stream<Path> walk = Files.walk(optifineClasses)) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        Files.deleteIfExists(optifineClasses);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static byte[] fileHash(Path input) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(input); DigestInputStream dis = new DigestInputStream(is, md)) {
            while (dis.read() != -1) {
            }
        }
        return md.digest();
    }
}
