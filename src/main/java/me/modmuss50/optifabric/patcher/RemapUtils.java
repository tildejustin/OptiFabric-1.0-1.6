package me.modmuss50.optifabric.patcher;

import net.fabricmc.loader.impl.lib.tinyremapper.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class RemapUtils {
    public static void mapJar(Path output, Path input, IMappingProvider mappings, List<Path> libraries) throws IOException {
        Files.deleteIfExists(output);
        TinyRemapper remapper = TinyRemapper.newRemapper().withMappings(mappings).renameInvalidLocals(true).rebuildSourceFilenames(true).build();
        try {
            OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(output).build();
            outputConsumer.addNonClassFiles(input, NonClassCopyMode.UNCHANGED, remapper);
            remapper.readInputsAsync(null, input);
            for (Path path : libraries) remapper.readClassPathAsync(path);
            remapper.apply(outputConsumer);
            outputConsumer.close();
            remapper.finish();
        } catch (Exception e) {
            remapper.finish();
            throw new RuntimeException("failed to remap jar", e);
        }
    }
}
