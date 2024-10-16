package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class OreIngredientVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraftforge/oredict/OreIngredient";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "apply",
            targetSignature());

    private static final String OWNER = "gregtech/asm/hooks/OreIngredientHooks";
    private static final String SIGNATURE = signature();
    private static final String METHOD_NAME = "checkToolbelt";

    public OreIngredientVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitCode() {
        mv.visitVarInsn(ALOAD, 1); // ItemStack
        mv.visitVarInsn(ALOAD, 0); // OreIngredient
        mv.visitMethodInsn(INVOKESTATIC, OWNER, METHOD_NAME, SIGNATURE, false);
        Label L1 = new Label();
        mv.visitJumpInsn(IFEQ, L1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        mv.visitLabel(L1);
        mv.visitFrame(F_SAME, 0, null, 0, null);

        mv.visitCode();
    }

    // public boolean apply(@Nullable ItemStack input)
    private static String targetSignature() {
        return "(" +
                "Lnet/minecraft/item/ItemStack;" + // ItemStack
                ")Z;"; // return boolean
    }

    // public static boolean extendedApply(@Nullable ItemStack input, @NotNull OreIngredient ingredient)
    private static String signature() {
        return "(" +
                "Lnet/minecraft/item/ItemStack;" + // ItemStack
                "Lnet/minecraftforge/oredict/OreIngredient;" + // OreIngredient
                ")Z"; // return boolean
    }
}
