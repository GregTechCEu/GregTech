package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class VintagiumPassManagerVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "me/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPassManager";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "createDefaultMappings",
            "()Lme/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPassManager;");
    public static final ObfMapping GET_BLOOM_LAYER = new ObfMapping("gregtech/client/utils/BloomEffectUtil",
            "getBloomLayer", "()Lnet/minecraft/util/BlockRenderLayer;");
    public static final ObfMapping GET_BLOOM_PASS = new ObfMapping("gregtech/client/utils/BloomEffectVintagiumUtil",
            "getBloomPass", "()Lme/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPass;");
    public static final ObfMapping ADD_MAPPING = new ObfMapping(TARGET_CLASS_NAME, "addMapping",
            "(Lnet/minecraft/util/BlockRenderLayer;Lme/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPass;)V");

    public VintagiumPassManagerVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitInsn(int opcode) {
        // add mapper.addMapping(BloomEffectUtil.getBloomLayer(), BloomEffectVintagiumUtil.getBloomPass());
        if (opcode == ARETURN) {
            GET_BLOOM_LAYER.visitMethodInsn(this, INVOKESTATIC);
            GET_BLOOM_PASS.visitMethodInsn(this, INVOKESTATIC);
            ADD_MAPPING.visitMethodInsn(this, INVOKESPECIAL);
            super.visitVarInsn(ALOAD, 0);
        }
        super.visitInsn(opcode);
    }
}
