package me.modmuss50.optifabric.mod;

import me.modmuss50.optifabric.patcher.ASMUtils;
import net.fabricmc.loader.api.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.jar.*;
import java.util.zip.ZipFile;

public class OptifineVersion {
    public static String version;
    public static String minecraftVersion;
    public static JarType jarType;

    public static File findOptifineJar() throws IOException {
        File modsDir = FabricLoader.getInstance().getGameDir().resolve("mods").toFile();
        File[] mods = modsDir.listFiles();

        File optifineJar = null;

        if (mods != null) {
            for (File file : mods) {
                if (file.isDirectory()) {
                    continue;
                }
                if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) {
                    JarType type = getJarType(file);
                    if (type.error) {
                        if (!type.equals(JarType.INCOMPATIBLE)) {
                            throw new RuntimeException("an error occurred when trying to find the optifine jar: " + type.name());
                        } else {
                            continue;
                        }
                    }
                    if (type == JarType.OPTIFINE_MOD || type == JarType.OPTIFINE_INSTALLER) {
                        if (optifineJar != null) {
                            Optifabric.error = "found 2 or more optifine jars, please ensure you only have 1 copy of optifine in the mods folder!";
                            throw new FileNotFoundException("multiple optifine jars");
                        }
                        jarType = type;
                        optifineJar = file;
                    }
                }
            }
        }

        if (optifineJar != null) {
            return optifineJar;
        }

        Optifabric.error = "optifabric could not find the optifine jar in the mods folder.";
        throw new FileNotFoundException("could not find optifine jar");
    }

    private static JarType getJarType(File file) throws IOException {
        ClassNode classNode;
        try (JarFile jarFile = new JarFile(file)) {
            JarEntry jarEntry = jarFile.getJarEntry("Config.class");
            if (jarEntry == null) {
                jarEntry = jarFile.getJarEntry("VersionThread.class");
            }
            System.out.println("jar entry: " + jarEntry);
            if (jarEntry == null) {
                return JarType.SOMETHING_ELSE;
            }
            classNode = ASMUtils.asClassNode(jarEntry, jarFile);
        }

        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.name.equals("VERSION")) {
                version = (String) fieldNode.value;
            }
            if (fieldNode.name.equals("MC_VERSION")) {
                minecraftVersion = (String) fieldNode.value;
            }
        }

        if (version == null || version.isEmpty() || minecraftVersion == null || minecraftVersion.isEmpty()) {
            return JarType.INCOMPATIBLE;
        }

        FabricLoader.getInstance().getModContainer("minecraft").ifPresent(minecraft -> {
            try {
                if (!minecraft.getMetadata().getVersion().equals(Version.parse(minecraftVersion))) {
                    System.err.printf("this version of optifine is not compatible with the current minecraft version\noptifine requires %s, but you have %s", minecraftVersion, version);
                }
            } catch (VersionParsingException e) {
                System.err.println("minecraft version could not be parsed");
            }
        });

        boolean installer;
        try (ZipFile fs = new ZipFile(file)) {
            installer = fs.stream().anyMatch(entry -> entry.getName().startsWith("patch/"));
        }

        return installer ? JarType.OPTIFINE_INSTALLER : JarType.OPTIFINE_MOD;
    }

    public enum JarType {
        OPTIFINE_MOD(false),
        OPTIFINE_INSTALLER(false),
        INCOMPATIBLE(true),
        SOMETHING_ELSE(false);

        final boolean error;

        JarType(boolean error) {
            this.error = error;
        }
    }
}
