package gregtech.core.visitors;

import gregtech.core.util.ObfMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class EntityRendererVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/client/renderer/EntityRenderer";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "func_175068_a", "(IFJ)V");
    private static final ObfMapping METHOD_RENDER_BLOCK_LAYER = new ObfMapping(
            "net/minecraft/client/renderer/RenderGlobal",
            "func_174977_a",
            "(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I").toRuntime();
    private static final ObfMapping METHOD_RENDER_BLOCK_LAYER2 = new ObfMapping(
            "net/minecraft/client/renderer/RenderGlobal",
            "func_174982_a",
            "(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I").toRuntime();

    private static final ObfMapping METHOD_BLOOM_HOOKS = new ObfMapping(
            "gregtech/client/utils/BloomEffectUtil",
            "renderBloomBlockLayer",
            "(Lnet/minecraft/client/renderer/RenderGlobal;Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I");

    public EntityRendererVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    private int time = 0;

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == INVOKEVIRTUAL && (METHOD_RENDER_BLOCK_LAYER.matches(name, desc) || METHOD_RENDER_BLOCK_LAYER2.matches(name, desc))) {
            time++;
            if (time == 4) {
                METHOD_BLOOM_HOOKS.visitMethodInsn(this, INVOKESTATIC);
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

}
