package gregtech.core.visitors;

import gregtech.core.util.ObfMapping;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class BlockVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/block/Block";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "canRenderInLayer", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockRenderLayer;)Z");

    private static final String BLOCK_HOOKS_OWNER = "gregtech/core/hooks/BlockHooks";
    private static final String BLOCK_HOOKS_SIGNATURE = "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockRenderLayer;)Ljava/lang/Boolean;";
    private static final String BLOCK_HOOKS_METHOD_NAME = "canRenderInLayer";

    public static ClassNode handleClassNode(ClassNode classNode) {
        for (MethodNode m : classNode.methods) {
            if (m.name.equals(TARGET_METHOD.s_name) && m.desc.equals(TARGET_METHOD.s_desc)) {
                InsnList toAdd = new InsnList();
                toAdd.add(new VarInsnNode(ALOAD, 1)); // Load state
                toAdd.add(new VarInsnNode(ALOAD, 2)); // Load layer
                // Invoke hook
                toAdd.add(new MethodInsnNode(INVOKESTATIC, BLOCK_HOOKS_OWNER, BLOCK_HOOKS_METHOD_NAME, BLOCK_HOOKS_SIGNATURE, false));
                toAdd.add(new InsnNode(DUP)); // Copy value on stack, avoids need for local var
                toAdd.add(new JumpInsnNode(IFNULL, (LabelNode) m.instructions.getFirst())); // Check if return is null, if it is, jump to vanilla code
                toAdd.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false)); // Otherwise evaluate the bool
                toAdd.add(new InsnNode(IRETURN)); // And return it
                AbstractInsnNode first = m.instructions.getFirst(); // First vanilla instruction
                m.instructions.insertBefore(first, toAdd); // Put this before the first instruction (L1 label node)
                m.instructions.insert(first, new InsnNode(POP)); // Pop the extra value that vanilla doesn't need
                break;
            }
        }
        return classNode;
    }
}
