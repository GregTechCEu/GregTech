package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TheOneProbeVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "mcjty/theoneprobe/network/PacketGetInfo";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "getProbeInfo", getSignature());

    private static final ObfMapping GET_BLOCK_STATE_METHOD = new ObfMapping("net/minecraft/world/World",
            "func_180495_p", "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;").toRuntime();

    public TheOneProbeVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == INVOKEVIRTUAL && name.equals(GET_BLOCK_STATE_METHOD.s_name) &&
                desc.equals(GET_BLOCK_STATE_METHOD.s_desc)) {
            visitMethodInsn(INVOKESTATIC, "gregtech/asm/hooks/TheOneProbeHooks", "getActualState",
                    "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
                    false);
        } else super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    private static String getSignature() {
        return "(" + "Lnet/minecraft/entity/player/EntityPlayer;" + "Lmcjty/theoneprobe/api/ProbeMode;" +
                "Lnet/minecraft/world/World;" + "Lnet/minecraft/util/math/BlockPos;" +
                "Lnet/minecraft/util/EnumFacing;" + "Lnet/minecraft/util/math/Vec3d;" +
                "Lnet/minecraft/item/ItemStack;" + ")Lmcjty/theoneprobe/apiimpl/ProbeInfo;";
    }
}
