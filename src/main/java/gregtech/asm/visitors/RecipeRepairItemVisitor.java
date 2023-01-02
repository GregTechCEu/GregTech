package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RecipeRepairItemVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/item/crafting/RecipeRepairItem";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "matches", targetSignature()).toRuntime();

    private static final String MATCHES_HOOK_OWNER = "gregtech/asm/hooks/RecipeRepairItemHooks";
    private static final String MATCHES_HOOK_SIGNATURE = tooltipSignature();
    private static final String MATCHES_HOOK_METHOD_NAME = "matchesGTTool";

    public RecipeRepairItemVisitor(MethodVisitor mv) {
        super(org.objectweb.asm.Opcodes.ASM5, mv);
    }

    // Need to call RecipeRepairItemHooks#matchesGTTool(InventoryCrafting)
    @Override
    public void visitCode() {
        mv.visitVarInsn(ALOAD, 1); // InventoryCrafting inv
        mv.visitVarInsn(ALOAD, 2); //  World worldIn

        // statically call matchesGTTool(InventoryCrafting)
        mv.visitMethodInsn(INVOKESTATIC, MATCHES_HOOK_OWNER, MATCHES_HOOK_METHOD_NAME, MATCHES_HOOK_SIGNATURE, false);

        mv.visitInsn(IRETURN);
    }

    // public boolean matches(InventoryCrafting inv, World worldIn)
    private static String targetSignature() {
        return "(" +
                "Lnet/minecraft/inventory/InventoryCrafting;" + // InventoryCrafting inv
                "Lnet/minecraft/world/World;" + //  World worldIn
                ")Z"; // return boolean
    }

    // public boolean matchesGTTool(InventoryCrafting inv)
    private static String tooltipSignature() {
        return "(" +
                "Lnet/minecraft/inventory/InventoryCrafting;" + // InventoryCrafting inv
                ")Z"; // return void
    }
}
