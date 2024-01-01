package me.modmuss50.optifabric.patcher;

import me.modmuss50.optifabric.IOUtils;
import me.modmuss50.optifabric.mod.Optifabric;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.jar.JarFile;

// pulls out the patched classes and saves into a classCache, and also creates an optifine jar without these classes
public class PatchSplitter {
    public static ClassCache generateClassCache(Path inputFile, Path classCacheOutput, byte[] inputHash) throws IOException {
        boolean extractClasses = Boolean.parseBoolean(System.getProperty("optifabric.extract", "false"));
        Path classesDir = classCacheOutput.getParent().resolve("classes");
        if (extractClasses) {
            Files.createDirectories(classesDir);
        }
        ClassCache classCache = new ClassCache(inputHash);
        try (JarFile jarFile = new JarFile(inputFile.toFile())) {
            jarFile.stream().forEach(entry -> {
                if ((entry.getName().startsWith("net/minecraft/") || entry.getName().startsWith("com/mojang/")) && entry.getName().endsWith(".class")) {
                    // if the class does not exist we cannot replace it, so we don't add it to the classCache and just let it get added to the classpath
                    if (Optifabric.class.getClassLoader().getResource(entry.getName()) == null) {
                        return;
                    }
                    try (InputStream inputStream = jarFile.getInputStream(entry)) {
                        String name = entry.getName();
                        byte[] bytes = IOUtils.toByteArray(inputStream);
                        classCache.addClass(name, bytes);
                        if (extractClasses) {
                            Path classFile = classesDir.resolve(entry.getName());
                            IOUtils.writeByteArrayToFile(classFile, bytes);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        // remove all the classes that are going to be patched in with replaceClass, we don't want these on the classpath
        try (FileSystem fs = FileSystems.newFileSystem(inputFile, null)) {
            classCache.getClasses().forEach(clazz -> {
                try {
                    Files.deleteIfExists(fs.getPath(clazz));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        System.out.println("found " + classCache.getClasses().size() + " patched classes");
        classCache.save(classCacheOutput);
        return classCache;
    }
}
