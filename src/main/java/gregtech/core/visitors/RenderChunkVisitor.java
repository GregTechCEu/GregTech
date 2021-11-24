package gregtech.core.visitors;

import gregtech.core.util.ObfMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RenderChunkVisitor extends MethodVisitor implements Opcodes {
    public static final String TARGET_CLASS_NAME = "net/minecraft/client/renderer/chunk/RenderChunk";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "func_178581_b", "(FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V");
    private static final ObfMapping METHOD_GET_RENDERER = new ObfMapping(
            "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher",
            "func_147546_a",
            "(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;").toRuntime();
    private static final ObfMapping METHOD_GET_RENDERER_HOOKS = new ObfMapping(
            "gregtech/core/hooks/RenderChunkHooks",
            "getRenderer",
            "(Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;");

    public RenderChunkVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }


    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == INVOKEVIRTUAL && (METHOD_GET_RENDERER.matches(name, desc))) {
            METHOD_GET_RENDERER_HOOKS.visitMethodInsn(this, INVOKESTATIC);
            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

}
