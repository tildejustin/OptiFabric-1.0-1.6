package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import me.modmuss50.optifabric.patcher.*;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.function.Consumer;

public class OptifineInjector {

    private static final List<String> patched = new ArrayList<>();
    ClassCache classCache;
    // I have no idea why and how this works, if you know better please let me know
    public final Consumer<ClassNode> transformer = target -> {
        if (patched.contains(target.name)) {
            System.out.println("Already patched" + target.name);
            return;
        }
        patched.add(target.name);

        // I cannot imagine this being very good at all
        ClassNode source = this.getSourceClassNode(target);

        target.methods = source.methods;
        target.fields = source.fields;
        target.interfaces = source.interfaces;
        target.superName = source.superName;

        // classes should be read with frames expanded (as mixin itself does it), in which case this should all be fine
        for (MethodNode methodNode : target.methods) {
            for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                if (insnNode instanceof FrameNode) {
                    FrameNode frameNode = (FrameNode) insnNode;
                    if (frameNode.local == null) {
                        throw new IllegalStateException("null locals in " + frameNode.type + " frame @ " + source.name + "#" + methodNode.name + methodNode.desc);
                    }
                }
            }
        }

        // let's make every class we touch public
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            target.access = OptifineInjector.modAccess(target.access);
            target.methods.forEach(methodNode -> methodNode.access = OptifineInjector.modAccess(methodNode.access));
            target.fields.forEach(fieldNode -> fieldNode.access = OptifineInjector.modAccess(fieldNode.access));
        }
    };

    public OptifineInjector(ClassCache classCache) {
        this.classCache = classCache;
    }

    private static int modAccess(int access) {
        if ((access & 0x7) != Opcodes.ACC_PRIVATE) {
            return (access & (~0x7)) | Opcodes.ACC_PUBLIC;
        }
        return access;
    }

    public void setup() {
        this.classCache.getClasses().forEach(s -> ClassTinkerers.addReplacement(s.replaceAll("/", ".").substring(0, s.length() - ".class".length()), this.transformer));
    }

    private ClassNode getSourceClassNode(ClassNode classNode) {
        String name = classNode.name.replaceAll("\\.", "/") + ".class";
        byte[] bytes = this.classCache.getAndRemove(name);
        if (bytes == null) {
            throw new RuntimeException("failed to find patched class for: " + name);
        }
        return ASMUtils.readClassFromBytes(bytes);
    }
}
