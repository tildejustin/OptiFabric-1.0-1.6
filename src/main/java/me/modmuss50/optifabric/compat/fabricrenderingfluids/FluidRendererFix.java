package me.modmuss50.optifabric.compat.fabricrenderingfluids;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import me.modmuss50.optifabric.util.RemappingUtils;
import me.modmuss50.optifabric.patcher.fixes.ClassFixer;

public class FluidRendererFix implements ClassFixer {
    //Add a little decoy so Fabric injects a little earlier, then patch the result

    @Override
    public void fix(ClassNode optifine, ClassNode minecraft) {
        //(BlockRenderView, BlockPos, VertexConsumer, FluidState)
        String renderDesc = "(Lnet/minecraft/class_1920;Lnet/minecraft/class_2338;Lnet/minecraft/class_4588;Lnet/minecraft/class_3610;)Z";
        //FluidRenderer, render, (..^..)
        String render = RemappingUtils.getMethodName("class_775", "method_3347", renderDesc);
        renderDesc = RemappingUtils.mapMethodDescriptor(renderDesc);

        for (MethodNode method : optifine.methods) {
            if (render.equals(method.name) && renderDesc.equals(method.desc)) {
                JumpInsnNode setTint = null;

                for (AbstractInsnNode node : method.instructions) {
                    if (node.getType() == AbstractInsnNode.METHOD_INSN) {
                        MethodInsnNode methodInsn = (MethodInsnNode) node;

                        if ("net/optifine/CustomColors".equals(methodInsn.owner) && "getFluidColor".equals(methodInsn.name)) {
                            do {
                                node = node.getPrevious();
                            } while (node != null && node.getType() != AbstractInsnNode.JUMP_INSN);
                            if (node == null) throw new IllegalStateException("Unable to find injection point in " + optifine.name + '#' + method.name + method.desc);

                            setTint = (JumpInsnNode) node;
                            break;
                        }
                    }
                }

                if (setTint == null) throw new IllegalStateException("Unable to find injection point in " + optifine.name + '#' + method.name + method.desc);

                LabelNode needsOptiFine = new LabelNode();
                LabelNode noNeed = setTint.label;

                InsnList extra = new InsnList();
                extra.add(new IntInsnNode(Opcodes.BIPUSH, 16));
                extra.add(new InsnNode(Opcodes.POP));
                method.instructions.insertBefore(setTint, extra);

                setTint.label = needsOptiFine;
                setTint.setOpcode(Opcodes.IFLT);

                extra.add(new VarInsnNode(Opcodes.ALOAD, 4));
                extra.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "me/modmuss50/optifabric/compat/fabricrenderingfluids/FluidRendererFixExternal",
                        "needsOptiFine", RemappingUtils.mapMethodDescriptor("(Lnet/minecraft/class_3610;)Z"))); //FluidState
                extra.add(new JumpInsnNode(Opcodes.IFEQ, noNeed));
                extra.add(needsOptiFine);
                method.instructions.insert(setTint, extra);
                break;
            }
        }
    }
}