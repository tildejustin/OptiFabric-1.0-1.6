package me.modmuss50.optifabric.patcher;

import me.modmuss50.optifabric.IOUtils;
import me.modmuss50.optifabric.mod.Optifabric;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.util.jar.JarFile;

// pulls out the patched classes and saves into a classCache, and also creates an optifine jar without these classes
public class PatchSplitter {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static ClassCache generateClassCache(File inputFile, File classCacheOutput, byte[] inputHash) throws IOException {
        boolean extractClasses = Boolean.parseBoolean(System.getProperty("optifabric.extract", "false"));
        File classesDir = new File(classCacheOutput.getParent(), "classes");
        if (extractClasses) {
            classesDir.mkdir();
        }
        ClassCache classCache = new ClassCache(inputHash);
        try (JarFile jarFile = new JarFile(inputFile)) {
            jarFile.stream().forEach(entry -> {
                if ((entry.getName().startsWith("net/minecraft/") || entry.getName().startsWith("com/mojang/")) && entry.getName().endsWith(".class")) {
                    // if the class does not exist we cannot replace it, so we don't add it to the classCache and just let it get added to the classpath
                    if (Optifabric.class.getClassLoader().getResource(entry.getName()) == null) return;
                    try (InputStream inputStream = jarFile.getInputStream(entry)) {
                        String name = entry.getName();
                        byte[] bytes = IOUtils.toByteArray(inputStream);
                        classCache.addClass(name, bytes);
                        if (extractClasses) {
                            File classFile = new File(classesDir, entry.getName());
                            IOUtils.writeByteArrayToFile(classFile, bytes);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        // remove all the classes that are going to be patched in with replaceClass, we don't want these on the classpath
        ZipUtil.removeEntries(inputFile, classCache.getClasses().toArray(new String[0]));

        System.out.println("found " + classCache.getClasses().size() + " patched classes");
        classCache.save(classCacheOutput);
        return classCache;
    }
}
