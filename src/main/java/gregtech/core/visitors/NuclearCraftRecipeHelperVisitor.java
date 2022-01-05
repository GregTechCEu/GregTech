package gregtech.core.visitors;

import gregtech.core.util.ObfMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
public class NuclearCraftRecipeHelperVisitor extends MethodVisitor implements Opcodes {
    public static final String TARGET_CLASS_NAME = "nc/integration/gtce/GTCERecipeHelper";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "addGTCERecipe", "(Ljava/lang/String;Lnc/recipe/BasicRecipe;)V");
    public static final ObfMapping TARGET_METHOD_2 = new ObfMapping(TARGET_CLASS_NAME, "findConflictByInputs", "(Lgregtech/api/recipes/RecipeMap;Ljava/util/List;Ljava/util/List;)Z");
    private static final ObfMapping METHOD_GET_RECIPE_LIST = new ObfMapping(
            "gregtech/api/recipes/RecipeMap",
            "getRecipeList",
            "()Ljava/util/Collection;").toRuntime();

    public NuclearCraftRecipeHelperVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (opcode == GETSTATIC && name.equals("FLUID_EXTRACTION_RECIPES")) { // FLUID_EXTRACTION_RECIPES -> EXTRACTOR_RECIPES
            name = "EXTRACTOR_RECIPES";
        }
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == INVOKEVIRTUAL && (METHOD_GET_RECIPE_LIST.matches(name, desc))) { // Collection<?> -> List<?>
            desc = "()Ljava/util/List;";
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
