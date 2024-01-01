package me.modmuss50.optifabric.patcher;

import net.fabricmc.tinyremapper.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.*;

public class LambdaRebuilder implements IMappingProvider {
    private final JarFile optifineJar;
    private final JarFile clientJar;

    private final Map<String, String> methodMap = new HashMap<>();
    private final List<String> usedMethods = new ArrayList<>(); // used to prevent duplicates

    public LambdaRebuilder(Path optifineFile, Path minecraftClientFile) throws IOException {
        this.optifineJar = new JarFile(optifineFile.toFile());
        this.clientJar = new JarFile(minecraftClientFile.toFile());
    }

    public void buildLambdaMap() throws IOException {
        this.optifineJar.stream().forEach(entry -> {
            if (entry.getName().endsWith(".class") && !entry.getName().startsWith("net/") && !entry.getName().startsWith("optifine/") && !entry.getName().startsWith("javax/")) {
                try {
                    this.buildClassMap(entry);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        this.optifineJar.close();
        this.clientJar.close();
    }

    private void buildClassMap(JarEntry jarEntry) throws IOException {
        ClassNode classNode = ASMUtils.asClassNode(jarEntry, this.optifineJar);
        List<MethodNode> lambdaNodes = new ArrayList<>();
        for (MethodNode methodNode : classNode.methods) {
            if (!methodNode.name.startsWith("lambda$") || methodNode.name.startsWith("lambda$static")) {
                continue;
            }
            lambdaNodes.add(methodNode);
        }
        if (lambdaNodes.isEmpty()) {
            return;
        }
        ClassNode minecraftClass = ASMUtils.asClassNode(this.clientJar.getJarEntry(jarEntry.getName()), this.clientJar);
        if (!minecraftClass.name.equals(classNode.name)) {
            throw new RuntimeException("something went wrong");
        }
        for (MethodNode methodNode : lambdaNodes) {
            MethodNode actualNode = this.findMethod(methodNode, minecraftClass);
            if (actualNode == null) {
                continue;
            }
            String key = classNode.name + "." + MemberInstance.getMethodId(actualNode.name, actualNode.desc);
            if (this.usedMethods.contains(key)) {
                System.out.println("skipping duplicate: " + key);
                continue;
            }
            this.usedMethods.add(classNode.name + "." + MemberInstance.getMethodId(actualNode.name, actualNode.desc));
            this.methodMap.put(classNode.name + "/" + MemberInstance.getMethodId(methodNode.name, methodNode.desc), actualNode.name);
        }
    }

    private MethodNode findMethod(MethodNode optifineMethod, ClassNode minecraftClass) {
        MethodNode lastNode = null;
        int identicalMethods = 0;
        for (MethodNode methodNode : minecraftClass.methods) {
            if (ASMUtils.isSynthetic(methodNode.access) && methodNode.desc.equals(optifineMethod.desc)) {
                identicalMethods++;
                lastNode = methodNode;
            }
        }
        if (identicalMethods == 1) {
            return lastNode;
        }
        return null;
    }

    @SuppressWarnings("CollectionAddedToSelf")
    @Override
    public void load(MappingAcceptor out) {
        this.methodMap.putAll(this.methodMap);
    }
}
