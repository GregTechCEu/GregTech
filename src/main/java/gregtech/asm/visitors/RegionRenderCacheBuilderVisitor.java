package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RegionRenderCacheBuilderVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/client/renderer/RegionRenderCacheBuilder";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "<init>", "()V");

    private static final ObfMapping FIELD_WORLD_RENDERERS = new ObfMapping(TARGET_CLASS_NAME,
            "field_179040_a",
            "[Lnet/minecraft/client/renderer/BufferBuilder;").toRuntime();
    private static final ObfMapping METHOD_BLOOM_HOOKS = new ObfMapping(
            "gregtech/client/utils/BloomEffectUtil",
            "initBloomRenderLayer",
            "([Lnet/minecraft/client/renderer/BufferBuilder;)V");

    public RegionRenderCacheBuilderVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            super.visitVarInsn(ALOAD, 0);
            FIELD_WORLD_RENDERERS.visitFieldInsn(this, GETFIELD);
            METHOD_BLOOM_HOOKS.visitMethodInsn(this, INVOKESTATIC);
        }
        super.visitInsn(opcode);
    }
}
