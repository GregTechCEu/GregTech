package gregtech.core.visitors;

import gregtech.core.util.ObfMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ConcretePowderVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/block/BlockConcretePowder";
    public static final String TARGET_SIGNATURE = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "tryTouchWater", TARGET_SIGNATURE);

    public ConcretePowderVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitCode() {
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.IRETURN);
    }
}
