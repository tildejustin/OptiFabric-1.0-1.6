package me.modmuss50.optifabric.patcher;

import me.modmuss50.optifabric.IOUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.Objects;
import java.util.jar.*;

public class ASMUtils {
    public static ClassNode readClassFromBytes(byte[] bytes) {
        Objects.requireNonNull(bytes, "Cannot read null bytes");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, ClassReader.SKIP_FRAMES);
        return classNode;
    }

    public static ClassNode asClassNode(JarEntry entry, JarFile jarFile) throws IOException {
        InputStream is = jarFile.getInputStream(entry);
        byte[] bytes = IOUtils.toByteArray(is);
        return ASMUtils.readClassFromBytes(bytes);
    }

    public static boolean isSynthetic(int flags) {
        return (flags & Opcodes.ACC_SYNTHETIC) != 0;
    }
}
