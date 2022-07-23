package me.modmuss50.optifabric.compat;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.transformer.ClassInfo.Method;
import org.spongepowered.asm.util.Annotations;

import me.modmuss50.optifabric.util.MixinFinder;
import me.modmuss50.optifabric.util.MixinFinder.Mixin;

public class InterceptingMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return Collections.emptyList();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        ClassNode thisMixin = Mixin.create(mixinInfo).getClassNode();

        AnnotationNode interception = Annotations.getInvisible(thisMixin, InterceptingMixin.class);
        if (interception == null) return; //Nothing to do for this particular Mixin

        Mixin interceptionMixin = findMixin(targetClassName, Annotations.getValue(interception));
        on: for (MethodNode method : thisMixin.methods) {
            AnnotationNode surrogateNode = Annotations.getInvisible(method, PlacatingSurrogate.class);

            if (surrogateNode != null) {
                for (Method realMethod : interceptionMixin.getMethods()) {
                    if (realMethod.getOriginalName().equals(method.name)) {
                        method.name = realMethod.getName(); //Mangle name to whatever Mixin is using for the real injection
                        method.invisibleAnnotations.remove(surrogateNode);
                        Annotations.setVisible(method, Surrogate.class);

                        targetClass.methods.add(method);
                        continue on;
                    }
                }

                throw new IllegalStateException("Cannot find original Mixin method for surrogate " + method.name + method.desc + " in " + interceptionMixin);
            }
        }
    }

    private static Mixin findMixin(String targetClass, String mixinTarget) {
        for (Mixin mixin : MixinFinder.getMixinsFor(targetClass)) {
            if (mixinTarget.equals(mixin.getName())) {
                return mixin;
            }
        }

        throw new IllegalArgumentException("Can't find Mixin class " + mixinTarget + " targetting " + targetClass);
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        ClassNode thisMixin = Mixin.create(mixinInfo).getClassNode();

        AnnotationNode interception = Annotations.getInvisible(thisMixin, InterceptingMixin.class);
        if (interception == null) return; //Nothing to do for this particular Mixin

        Mixin interceptionMixin = findMixin(targetClassName, Annotations.getValue(interception));
        Map<String, String> shims = thisMixin.methods.stream().filter(method -> Annotations.getInvisible(method, Shim.class) != null).collect(Collectors.toMap(method -> method.name.concat(method.desc), method -> {
            Method realMethod = interceptionMixin.getMethod(method.name, method.desc);

            if (realMethod == null) {
                throw new IllegalStateException("Cannot find shim method " + method.name + method.desc + " in " + interceptionMixin);
            }

            assert method.name.equals(realMethod.getOriginalName());
            return realMethod.getName();
        }));
        if (shims.isEmpty()) return; //Nothing to do

        targetClassName = targetClassName.replace('.', '/');
        for (Iterator<MethodNode> it = targetClass.methods.iterator(); it.hasNext();) {
            MethodNode method = it.next();

            if (shims.containsKey(method.name.concat(method.desc)) || Annotations.getInvisible(method, PlacatingSurrogate.class) != null) {
                it.remove(); //Don't want to keep the shim methods
            } else {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;

                        String replacedName = shims.get(methodInsn.name.concat(methodInsn.desc));
                        if (replacedName != null && targetClassName.equals(methodInsn.owner)) {
                            methodInsn.name = replacedName;
                        }
                    }
                }
            }
        }
    }
}