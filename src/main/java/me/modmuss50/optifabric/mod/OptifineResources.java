package me.modmuss50.optifabric.mod;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;

public enum OptifineResources {
    INSTANCE;

    private OptifineResources() {
        try {
            zip = new ZipFile(OptifabricSetup.optifineRuntimeJar);
        } catch (ZipException e) {
            //Would've thought the classloader would have caught this sooner but whatever
            throw new RuntimeException("Error opening Optifine jar, probably corrupt?", e);
        } catch (IOException e) {
            throw new UncheckedIOException("Error opening Optifine jar", e);
        }
    }

    private ZipEntry getEntry(String path) {
        return StringUtils.isNotBlank(path) ? zip.getEntry(path.charAt(0) == '/' ? path.substring(1) : path) : null;
    }

    public boolean hasResource(String path) {
        return getEntry(path) != null;
    }

    public InputStream getResource(String path) throws IOException {
        ZipEntry entry = getEntry(path);
        return entry != null ? zip.getInputStream(entry) : null;
    }

    private final ZipFile zip;
}