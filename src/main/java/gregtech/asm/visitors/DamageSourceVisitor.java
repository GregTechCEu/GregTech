package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class DamageSourceVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/util/DamageSource";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "func_76365_a", "(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/util/DamageSource;").toRuntime();

    private static final String DAMAGE_SOURCE_OWNER = "gregtech/asm/hooks/DamageSourceHooks";
    private static final String DAMAGE_SOURCE_SIGNATURE = "(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/util/DamageSource;";
    private static final String DAMAGE_SOURCE_METHOD_NAME = "causePlayerDamage";

    public DamageSourceVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitCode() {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, DAMAGE_SOURCE_OWNER, DAMAGE_SOURCE_METHOD_NAME, DAMAGE_SOURCE_SIGNATURE, false);
        mv.visitInsn(ARETURN);
    }
}
