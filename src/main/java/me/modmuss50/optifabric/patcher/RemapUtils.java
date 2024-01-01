package me.modmuss50.optifabric.patcher;


import me.modmuss50.optifabric.mod.TinyRemapperPreload;
import net.fabricmc.tinyremapper.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class RemapUtils {
    public static void mapJar(Path output, Path input, IMappingProvider mappings, List<Path> libraries) throws IOException {
        TinyRemapperPreload.load();
        Files.deleteIfExists(output);
        TinyRemapper remapper = TinyRemapper
                .newRemapper()
                .withMappings(mappings)
                .renameInvalidLocals(true)
                .rebuildSourceFilenames(true)
                .fixPackageAccess(true)
                .build();
        try {
            OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(output).build();
            outputConsumer.addNonClassFiles(input, NonClassCopyMode.UNCHANGED, remapper);
            remapper.readInputs(input);
            remapper.readClassPath(libraries.toArray(new Path[0]));
            libraries.forEach(remapper::readClassPath);
            remapper.apply(outputConsumer);
            outputConsumer.close();
            remapper.finish();
        } catch (Exception e) {
            remapper.finish();
            throw new RuntimeException("failed to remap jar", e);
        }
    }
}
