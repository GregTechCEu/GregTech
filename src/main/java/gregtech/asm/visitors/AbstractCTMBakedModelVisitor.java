package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AbstractCTMBakedModelVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "team/chisel/ctm/client/model/AbstractCTMBakedModel";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME,
            "func_188616_a",
            "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/EnumFacing;J)Ljava/util/List;");

    private static final ObfMapping METHOD_GET_QUADS_HOOKS = new ObfMapping(
            "gregtech/asm/hooks/CTMHooks",
            "getQuadsWithOptiFine",
            "(Ljava/util/List;" +
                    "Lnet/minecraft/util/BlockRenderLayer;" +
                    "Lnet/minecraft/client/renderer/block/model/IBakedModel;" +
                    "Lnet/minecraft/block/state/IBlockState;" +
                    "Lnet/minecraft/util/EnumFacing;" +
                    "J" +
                    ")Ljava/util/List;");

    public AbstractCTMBakedModelVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    int times = 0;

    @Override
    public void visitInsn(int opcode) {
        if (opcode == ARETURN && times++ == 1) {
            visitVarInsn(ALOAD, 7);
            visitVarInsn(ALOAD, 0);
            visitVarInsn(ALOAD, 1);
            visitVarInsn(ALOAD, 2);
            visitVarInsn(LLOAD, 3);
            METHOD_GET_QUADS_HOOKS.visitMethodInsn(this, INVOKESTATIC);
        }
        super.visitInsn(opcode);
    }
}
