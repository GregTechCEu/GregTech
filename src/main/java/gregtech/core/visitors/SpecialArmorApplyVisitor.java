package gregtech.core.visitors;

import gregtech.core.util.ObfMapping;
import gregtech.core.util.SafeMethodVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SpecialArmorApplyVisitor extends SafeMethodVisitor {

    public static final String TARGET_CLASS_NAME = "net/minecraftforge/common/ISpecialArmor$ArmorProperties";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "applyArmor", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/util/NonNullList;Lnet/minecraft/util/DamageSource;D)F");

    private static final ObfMapping METHOD_MAPPING = new ObfMapping("net/minecraft/util/CombatRules", "func_189427_a", "(FFF)F").toRuntime();

    private static final String ARMOR_HOOKS_OWNER = "gregtech/core/hooks/ArmorHooks";
    private static final String ARMOR_HOOKS_SIGNATURE = "(FLnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/util/NonNullList;Lnet/minecraft/util/DamageSource;)V";
    private static final String ARMOR_HOOKS_METHOD_NAME = "damageArmor";

    public SpecialArmorApplyVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    private boolean checkTargetInsn(int opcode, String owner, String name, String desc) {
        return opcode == Opcodes.INVOKESTATIC && METHOD_MAPPING.s_owner.equals(owner) && METHOD_MAPPING.matches(name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (checkTargetInsn(opcode, owner, name, desc)) {
            markPatchedSuccessfully();
            super.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_NAME, SpecialArmorClassVisitor.CACHED_TOUGHNESS_FIELD_NAME, "F"); //store armorToughness
            super.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_NAME, SpecialArmorClassVisitor.CACHED_TOTAL_ARMOR_FIELD_NAME, "F"); //store totalArmor
            super.visitInsn(Opcodes.DUP); //duplicate damage
            super.visitVarInsn(Opcodes.ALOAD, 0); //load entity
            super.visitVarInsn(Opcodes.ALOAD, 1); //load inventory
            super.visitVarInsn(Opcodes.ALOAD, 2); //load damageSource
            super.visitMethodInsn(Opcodes.INVOKESTATIC, ARMOR_HOOKS_OWNER, ARMOR_HOOKS_METHOD_NAME, ARMOR_HOOKS_SIGNATURE, false); //call ArmorHooks
            super.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_NAME, SpecialArmorClassVisitor.CACHED_TOTAL_ARMOR_FIELD_NAME, "F"); //load totalArmor back
            super.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_NAME, SpecialArmorClassVisitor.CACHED_TOUGHNESS_FIELD_NAME, "F"); //load armorToughness back
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    protected String getInjectTargetString() {
        return String.format("Patch target: %s; injection point: %s; (point not found)", TARGET_METHOD, METHOD_MAPPING);
    }
}
