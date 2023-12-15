package me.modmuss50.optifabric.util;

import net.fabricmc.loader.impl.lib.mappingio.MappingReader;
import net.fabricmc.loader.impl.lib.mappingio.format.tiny.Tiny1FileReader;
import net.fabricmc.loader.impl.lib.mappingio.tree.MemoryMappingTree;
import net.fabricmc.loader.impl.lib.tinyremapper.IMappingProvider;
import net.fabricmc.loader.impl.lib.tinyremapper.NonClassCopyMode;
import net.fabricmc.loader.impl.lib.tinyremapper.OutputConsumerPath;
import net.fabricmc.loader.impl.lib.tinyremapper.TinyRemapper;
import net.fabricmc.loader.impl.util.mappings.TinyRemapperMappingsHelper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RemapUtils {

	public static IMappingProvider getTinyRemapper(File mappings, String from, String to) {
        MemoryMappingTree tree = new MemoryMappingTree();
        try {
            // assumes mapping file is tiny v1, safe assumption for 1.14-1.15 but may change
            Tiny1FileReader.read(new FileReader(mappings), tree);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return TinyRemapperMappingsHelper.create(tree, from, to);
	}

	public static void mapJar(Path output, Path input, File mappings, List<Path> libraries, String from, String to) throws IOException {
		mapJar(output, input, getTinyRemapper(mappings, from, to), libraries);
	}

	public static void mapJar(Path output, Path input, IMappingProvider mappings, List<Path> libraries) throws IOException {
		Files.deleteIfExists(output);

		TinyRemapper remapper = TinyRemapper.newRemapper().withMappings(mappings).renameInvalidLocals(true).rebuildSourceFilenames(true).build();

		try {
			OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(output).build();
			outputConsumer.addNonClassFiles(input, NonClassCopyMode.UNCHANGED, remapper);
			remapper.readInputsAsync(null, input);

			for (Path path : libraries) {
				remapper.readClassPathAsync(path);
			}

			remapper.apply(outputConsumer);
			outputConsumer.close();
			remapper.finish();
		} catch (Exception e) {
			remapper.finish();
			throw new RuntimeException("Failed to remap jar", e);
		}
	}

}
