package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class NuclearCraftRecipeHelperVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "nc/integration/gtce/GTCERecipeHelper";

    public static final ObfMapping TARGET_METHOD_NC = new ObfMapping(TARGET_CLASS_NAME, "addGTCERecipe",
            "(Ljava/lang/String;Lnc/recipe/ProcessorRecipe;)V");
    public static final ObfMapping TARGET_METHOD_NCO = new ObfMapping(TARGET_CLASS_NAME, "addGTCERecipe",
            "(Ljava/lang/String;Lnc/recipe/BasicRecipe;)V");

    public NuclearCraftRecipeHelperVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (opcode == GETSTATIC && name.equals("FLUID_EXTRACTION_RECIPES")) { // FLUID_EXTRACTION_RECIPES ->
                                                                              // EXTRACTOR_RECIPES
            name = "EXTRACTOR_RECIPES";
        }
        super.visitFieldInsn(opcode, owner, name, desc);
    }
}
