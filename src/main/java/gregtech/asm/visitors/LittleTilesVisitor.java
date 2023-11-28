package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LittleTilesVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "com/creativemd/littletiles/client/render/world/TileEntityRenderManager";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(
            TARGET_CLASS_NAME,
            "<init>",
            "(Lcom/creativemd/littletiles/common/tileentity/TileEntityLittleTiles;)V");
    private static final ObfMapping FIELD_LAYER_RENDER_BOX_CACHE = new ObfMapping(
            TARGET_CLASS_NAME,
            "boxCache",
            "Lcom/creativemd/littletiles/client/render/cache/LayeredRenderBoxCache;");
    private static final ObfMapping METHOD_LAYER_RENDER_BOX_CACHE_HOOKS = new ObfMapping(
            "gregtech/asm/hooks/LittleTilesHooks",
            "initLayeredRenderBoxCache",
            "()Lcom/creativemd/littletiles/client/render/cache/LayeredRenderBoxCache;");

    public LittleTilesVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            super.visitVarInsn(ALOAD, 0);
            METHOD_LAYER_RENDER_BOX_CACHE_HOOKS.visitMethodInsn(this, INVOKESTATIC);
            FIELD_LAYER_RENDER_BOX_CACHE.visitFieldInsn(this, PUTFIELD);
        }
        super.visitInsn(opcode);
    }
}
