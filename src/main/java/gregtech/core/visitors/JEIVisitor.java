package gregtech.core.visitors;

import gregtech.core.util.ObfMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class JEIVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "mezz/jei/startup/ForgeModIdHelper";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "addModNameToIngredientTooltip", targetSignature());

    private static final String FLUID_TOOLTIP_OWNER = "gregtech/core/hooks/JEIHooks";
    private static final String FLUID_TOOLTIP_SIGNATURE = tooltipSignature();
    private static final String FLUID_TOOLTIP_METHOD_NAME = "addFluidTooltip";

    public JEIVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    // Need to call JEIHooks#addFluidTooltip(List<String>, Object)
    @Override
    public void visitCode() {

        mv.visitVarInsn(ALOAD, 1); // List<String> tooltip
        mv.visitVarInsn(ALOAD, 2); // T ingredient

        // statically call addFluidTooltip(List<String>, Object)
        mv.visitMethodInsn(INVOKESTATIC, FLUID_TOOLTIP_OWNER, FLUID_TOOLTIP_METHOD_NAME, FLUID_TOOLTIP_SIGNATURE, false);

        mv.visitCode();
    }

    // public <E> List<String> addModNameToIngredientTooltip(List<String> tooltip, E ingredient, IIngredientHelper<E> ingredientHelper)
    private static String targetSignature() {

        return "(" +
                "Ljava/util/List;" + // List<String>
                "Ljava/lang/Object;" + // E
                "Lmezz/jei/api/ingredients/IIngredientHelper;" + // IIngredientHelper<E>
                ")Ljava/util/List;"; // return List<String>
    }

    // public void addFluidTooltip(List<String> tooltip, Object ingredient)
    private static String tooltipSignature() {

        return "(" +
                "Ljava/util/List;" + // List<String>
                "Ljava/lang/Object;" + // Object
                ")V"; // return void
    }
}
