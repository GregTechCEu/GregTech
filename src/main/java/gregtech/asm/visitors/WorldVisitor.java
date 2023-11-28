package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class WorldVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/world/World";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(
            TARGET_CLASS_NAME,
            "func_180498_a",
            "(Lnet/minecraft/entity/player/EntityPlayer;ILnet/minecraft/util/math/BlockPos;I)V");

    private static final ObfMapping METHOD_PLAY_RECORD_HOOKS = new ObfMapping(
            "gregtech/asm/hooks/SoundHooks",
            "playRecord",
            "(Lnet/minecraft/world/IWorldEventListener;Lnet/minecraft/entity/player/EntityPlayer;ILnet/minecraft/util/math/BlockPos;I)V");

    public WorldVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == INVOKEINTERFACE &&
                desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;ILnet/minecraft/util/math/BlockPos;I)V")) {
            METHOD_PLAY_RECORD_HOOKS.visitMethodInsn(this, INVOKESTATIC);
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
