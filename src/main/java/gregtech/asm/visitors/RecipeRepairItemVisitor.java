package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class RecipeRepairItemVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/item/crafting/RecipeRepairItem";
    private static final String HOOK_CLASS_NAME = "gregtech/asm/hooks/RecipeRepairItemHooks";

    private static final ObfMapping MATCHES_METHOD = new ObfMapping(TARGET_CLASS_NAME, "func_77569_a",
            "(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)Z").toRuntime();
    private static final String MATCHES_HOOK_SIGNATURE = "(Lnet/minecraft/inventory/InventoryCrafting;)Z";
    private static final String MATCHES_HOOK_METHOD_NAME = "matches";

    private static final ObfMapping RESULT_METHOD = new ObfMapping(TARGET_CLASS_NAME, "func_77572_b",
            "(Lnet/minecraft/inventory/InventoryCrafting;)Lnet/minecraft/item/ItemStack;").toRuntime();
    private static final String RESULT_HOOK_SIGNATURE = "(Lnet/minecraft/inventory/InventoryCrafting;)Lnet/minecraft/item/ItemStack;";
    private static final String RESULT_HOOK_METHOD_NAME = "getCraftingResult";

    private static final ObfMapping REMAINING_METHOD = new ObfMapping(TARGET_CLASS_NAME, "func_179532_b",
            "(Lnet/minecraft/inventory/InventoryCrafting;)Lnet/minecraft/util/NonNullList;").toRuntime();
    private static final String REMAINING_HOOK_SIGNATURE = "(Lnet/minecraft/inventory/InventoryCrafting;)Lnet/minecraft/util/NonNullList;";
    private static final String REMAINING_HOOK_METHOD_NAME = "getRemainingItems";

    public static ClassNode handleClassNode(ClassNode classNode) {
        int done = 0;
        for (MethodNode m : classNode.methods) {
            if (done == 3) break;

            // matches() method
            if (m.name.equals(MATCHES_METHOD.s_name) && m.desc.equals(MATCHES_METHOD.s_desc)) {
                InsnList insns = new InsnList();
                insns.add(new VarInsnNode(ALOAD, 1));
                insns.add(new MethodInsnNode(INVOKESTATIC, HOOK_CLASS_NAME, MATCHES_HOOK_METHOD_NAME,
                        MATCHES_HOOK_SIGNATURE, false));
                insns.add(new InsnNode(IRETURN));
                AbstractInsnNode first = m.instructions.getFirst();
                m.instructions.insertBefore(first, insns);
                done++;
            }

            // getCraftingResult() method
            if (m.name.equals(RESULT_METHOD.s_name) && m.desc.equals(RESULT_METHOD.s_desc)) {
                InsnList insns = new InsnList();
                insns.add(new VarInsnNode(ALOAD, 1));
                insns.add(new MethodInsnNode(INVOKESTATIC, HOOK_CLASS_NAME, RESULT_HOOK_METHOD_NAME,
                        RESULT_HOOK_SIGNATURE, false));
                insns.add(new InsnNode(ARETURN));
                AbstractInsnNode first = m.instructions.getFirst();
                m.instructions.insertBefore(first, insns);
                done++;
            }

            // getRemainingItems() method
            if (m.name.equals(REMAINING_METHOD.s_name) && m.desc.equals(REMAINING_METHOD.s_desc)) {
                InsnList insns = new InsnList();
                insns.add(new VarInsnNode(ALOAD, 1));
                insns.add(new MethodInsnNode(INVOKESTATIC, HOOK_CLASS_NAME, REMAINING_HOOK_METHOD_NAME,
                        REMAINING_HOOK_SIGNATURE, false));
                insns.add(new InsnNode(ARETURN));
                AbstractInsnNode first = m.instructions.getFirst();
                m.instructions.insertBefore(first, insns);
                done++;
            }
        }
        return classNode;
    }
}
