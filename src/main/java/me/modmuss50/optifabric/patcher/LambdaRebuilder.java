package me.modmuss50.optifabric.patcher;

import net.fabricmc.loader.impl.lib.tinyremapper.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;

public class LambdaRebuilder implements IMappingProvider {
    private final JarFile optifineJar;
    private final JarFile clientJar;

    private final Map<String, String> methodMap = new HashMap<>();
    private final List<String> usedMethods = new ArrayList<>(); // used to prevent duplicates

    public LambdaRebuilder(File optifineFile, File minecraftClientFile) throws IOException {
        optifineJar = new JarFile(optifineFile);
        clientJar = new JarFile(minecraftClientFile);

    }

    public void buildLambdaMap() throws IOException {
        Enumeration<JarEntry> entries = optifineJar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class") && !entry.getName().startsWith("net/") && !entry.getName().startsWith("optifine/") && !entry.getName().startsWith("javax/")) {
                buildClassMap(entry);
            }
        }
        optifineJar.close();
        clientJar.close();
    }

    private void buildClassMap(JarEntry jarEntry) throws IOException {
        ClassNode classNode = ASMUtils.asClassNode(jarEntry, optifineJar);
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
        ClassNode minecraftClass = ASMUtils.asClassNode(clientJar.getJarEntry(jarEntry.getName()), clientJar);
        if (!minecraftClass.name.equals(classNode.name)) {
            throw new RuntimeException("something went wrong");
        }
        for (MethodNode methodNode : lambdaNodes) {
            MethodNode actualNode = findMethod(methodNode, minecraftClass);
            if (actualNode == null) {
                continue;
            }
            String key = classNode.name + "." + MemberInstance.getMethodId(actualNode.name, actualNode.desc);
            if (usedMethods.contains(key)) {
                System.out.println("skipping duplicate: " + key);
                continue;
            }
            usedMethods.add(classNode.name + "." + MemberInstance.getMethodId(actualNode.name, actualNode.desc));
            methodMap.put(classNode.name + "/" + MemberInstance.getMethodId(methodNode.name, methodNode.desc), actualNode.name);
        }
    }

    private MethodNode findMethod(MethodNode optifineMethod, ClassNode minecraftClass) {
        {
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
        }
        return null;
    }

    @SuppressWarnings("CollectionAddedToSelf")
    @Override
    public void load(MappingAcceptor out) {
        methodMap.putAll(this.methodMap);
    }
}
