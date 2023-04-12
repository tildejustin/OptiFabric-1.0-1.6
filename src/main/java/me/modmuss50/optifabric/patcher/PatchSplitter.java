package me.modmuss50.optifabric.patcher;

import me.modmuss50.optifabric.mod.Optifabric;
import me.modmuss50.optifabric.mod.OptifineSetup;
import me.modmuss50.optifabric.mod.OptifineVersion;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//Pulls out the patched classes and saves into a classCache, and also creates an optifine jar without these classes
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
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if ((entry.getName().startsWith("net/minecraft/") || entry.getName().startsWith("com/mojang/")) && entry.getName().endsWith(".class")) {
                    try (InputStream inputStream = jarFile.getInputStream(entry)) {
                        String name = entry.getName();
                        byte[] bytes = IOUtils.toByteArray(inputStream);
                        classCache.addClass(name, bytes);
                        if (extractClasses) {
                            File classFile = new File(classesDir, entry.getName());
                            FileUtils.writeByteArrayToFile(classFile, bytes);
                        }
                    }
                }
            }
        }


        //Remove all the classes that are going to be patched in, we don't want theses on the classpath
        ZipUtil.removeEntries(inputFile, classCache.getClasses().toArray(new String[0]));

        System.out.println("Found " + classCache.getClasses().size() + " patched classes");
        classCache.save(classCacheOutput);
        return classCache;
    }

}
