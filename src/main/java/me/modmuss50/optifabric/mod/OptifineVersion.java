package me.modmuss50.optifabric.mod;

import me.modmuss50.optifabric.patcher.ASMUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.jar.*;

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
                            throw new RuntimeException("An error occurred when trying to find the optifine jar: " + type.name());
                        } else {
                            continue;
                        }
                    }
                    if (type == JarType.OPTIFINE_MOD) {
                        if (optifineJar != null) {
                            Optifabric.error = "Found 2 or more optifine jars, please ensure you only have 1 copy of optifine in the mods folder!";
                            throw new FileNotFoundException("Multiple optifine jars");
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

        Optifabric.error = "Optifabric could not find the Optifine jar in the mods folder.";
        throw new FileNotFoundException("Could not find optifine jar");
    }

    private static JarType getJarType(File file) throws IOException {
        ClassNode classNode;
        JarEntry jarEntry;
        try (JarFile jarFile = new JarFile(file)) {
            jarEntry = jarFile.getJarEntry("Config.class");
            if (jarEntry == null) {
                jarEntry = jarFile.getJarEntry("VersionThread.class");
            }
            System.out.println("jar entry: " + jarEntry);
            if (jarEntry == null) {
                // 1.1 light has no Config or VersionThread class
                if (file.getName().endsWith(".zip")) {
                    version = "unknown";
                    return JarType.OPTIFINE_MOD;
                }
                return JarType.SOMETHING_ELSE;
            }
            classNode = ASMUtils.asClassNode(jarEntry, jarFile);
        }

        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.name.equals("VERSION") || fieldNode.name.equals("version")) {
                version = (String) fieldNode.value;
            }
        }

        if (version == null || version.isEmpty()) {
            // inlined in getVersion pre 1.2
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("getVersion")) {
                    version = (String) ((LdcInsnNode) methodNode.instructions.get(2)).cst;
                }
            }

            if (version == null || version.isEmpty()) {
                return JarType.INCOMPATIBLE;
            }
        }

        // no version checking because pre 1.2 optifine does not specify a target minecraft
        return JarType.OPTIFINE_MOD;
    }

    public enum JarType {
        OPTIFINE_MOD(false),
        INCOMPATIBLE(true),
        SOMETHING_ELSE(false);

        final boolean error;

        JarType(boolean error) {
            this.error = error;
        }

    }

}
