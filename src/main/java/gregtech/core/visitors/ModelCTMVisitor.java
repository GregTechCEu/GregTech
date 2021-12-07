package gregtech.core.visitors;

import gregtech.core.util.ObfMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ModelCTMVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "team/chisel/ctm/client/model/ModelCTM";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME,
            "canRenderInLayer",
            "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockRenderLayer;)Z");

    private static final ObfMapping FIELD_LAYERS = new ObfMapping(TARGET_CLASS_NAME,
            "layers",
            "B").toRuntime();

    private static final ObfMapping METHOD_CAN_RENDERER_IN_LAYER_HOOKS = new ObfMapping(
            "gregtech/core/hooks/CTMHooks",
            "canRenderInLayer",
            "(ZBLnet/minecraft/util/BlockRenderLayer;)Z");


    public ModelCTMVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == IRETURN) {
            visitVarInsn(ALOAD, 0);
            FIELD_LAYERS.visitFieldInsn(this, GETFIELD);
            visitVarInsn(ALOAD, 2);
            METHOD_CAN_RENDERER_IN_LAYER_HOOKS.visitMethodInsn(this, INVOKESTATIC);
        }
        super.visitInsn(opcode);
    }
}
