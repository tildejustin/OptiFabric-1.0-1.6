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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void writeByteArrayToFile(final File file, final byte[] data) throws IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            out.write(data, 0, data.length);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteDirectory(File optifineClasses) throws IOException {
        try (Stream<Path> walk = Files.walk(optifineClasses.toPath())) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        optifineClasses.delete();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static byte[] fileHash(File input) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(input.toPath()); DigestInputStream dis = new DigestInputStream(is, md)) {
            while (dis.read() != -1) ;
        }
        return md.digest();
    }
}
