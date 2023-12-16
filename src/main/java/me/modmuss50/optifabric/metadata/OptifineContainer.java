package me.modmuss50.optifabric.metadata;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.impl.util.FileSystemUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class OptifineContainer implements ModContainer {
    private final Path ofPath;
    private final List<Path> rootPaths;
    private final ModMetadata ofMetadata;

    public OptifineContainer(Path ofPath, Version version) {
        try {
            this.ofPath = FileSystemUtil.getJarFileSystem(ofPath, true).get().getRootDirectories().iterator().next();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        rootPaths = new ArrayList<>();
        rootPaths.add(this.ofPath);
        this.ofMetadata = new OptifineMetadata(version);
    }

    @Override
    public ModMetadata getMetadata() {
        return ofMetadata;
    }

    @Override
    public List<Path> getRootPaths() {
        return Collections.unmodifiableList(rootPaths);
    }

    @Override
    public OptifineOrigin getOrigin() {
        return this::getRootPaths;
    }

    @Override
    public Optional<ModContainer> getContainingMod() {
        return FabricLoader.getInstance().getModContainer("optifabric");
    }

    @Override
    public Collection<ModContainer> getContainedMods() {
        return new HashSet<>();
    }

    @Override
    public Path getRootPath() {
        return ofPath;
    }

    @Override
    public Path getPath(String file) {
        return findPath(file).orElse(null);
    }
}
