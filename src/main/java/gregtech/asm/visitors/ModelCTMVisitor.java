package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ModelCTMVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "team/chisel/ctm/client/asm/CTMCoreMethods";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME,
            "canRenderInLayer",
            "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockRenderLayer;)Ljava/lang/Boolean;");
    private static final ObfMapping METHOD_CAN_RENDER_IN_LAYER = new ObfMapping(
            "team/chisel/ctm/api/model/IModelCTM",
            "canRenderInLayer",
            "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockRenderLayer;)Z");

    private static final ObfMapping METHOD_CAN_RENDERER_IN_LAYER_HOOKS = new ObfMapping(
            "gregtech/asm/hooks/CTMModHooks",
            "canRenderInLayer",
            "(Lteam/chisel/ctm/api/model/IModelCTM;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockRenderLayer;)Z");

    public ModelCTMVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == INVOKEINTERFACE && (METHOD_CAN_RENDER_IN_LAYER.matches(name, desc))) {
            METHOD_CAN_RENDERER_IN_LAYER_HOOKS.visitMethodInsn(this, INVOKESTATIC);
            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
