package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Class needed for when a specific Enchantment class does not behave well, and
 * {@link net.minecraft.enchantment.Enchantment#canApply} does not also check
 * {@link net.minecraft.item.Item#canApplyAtEnchantingTable}.
 * <p>
 * Any enchantments modified here, must also implement the desired behavior in
 * {@link gregtech.api.items.toolitem.IGTTool#definition$canApplyAtEnchantingTable}
 * for this ASM to make a change in-game.
 */
public final class EnchantmentCanApplyVisitor extends AdviceAdapter implements Opcodes {

    public static final Map<String, ObfMapping> CLASS_TO_MAPPING_MAP = new HashMap<>();

    static {
        createMapping("net/minecraft/enchantment/EnchantmentDurability"); // Minecraft Unbreaking
        createMapping("cofh/core/enchantment/EnchantmentSmelting"); // CoFHCore Smelting
        createMapping("cofh/core/enchantment/EnchantmentSmashing"); // CoFHCore Smashing
    }

    private static void createMapping(String className) {
        CLASS_TO_MAPPING_MAP.put(className,
                new ObfMapping(className, "func_92089_a", "(Lnet/minecraft/item/ItemStack;)Z").toRuntime());
    }

    public EnchantmentCanApplyVisitor(MethodVisitor mv, ObfMapping mapping) {
        super(ASM5, mv, ACC_PUBLIC, mapping.s_name, mapping.s_desc);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode == IRETURN) {
            visitInsn(DUP); // load return value
            visitVarInsn(ALOAD, 1); // load ItemStack
            visitVarInsn(ALOAD, 0); // load this (Enchantment)
            visitMethodInsn(INVOKESTATIC, "gregtech/asm/hooks/EnchantmentHooks", "checkTool",
                    "(ZLnet/minecraft/item/ItemStack;Lnet/minecraft/enchantment/Enchantment;)Z", false); // do GT tool
                                                                                                         // checking
                                                                                                         // logic
        }
    }
}
