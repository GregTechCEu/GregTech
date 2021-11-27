package gregtech.core.visitors;

import gregtech.core.util.ObfMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RenderGlobalVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/client/renderer/RenderGlobal";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "playEvent", targetSignature());

    private static final String PLAY_RECORD_OWNER = "gregtech/core/hooks/SoundHooks";
    private static final String PLAY_RECORD_SIGNATURE = recordSignature();
    private static final String PLAY_RECORD_METHOD_NAME = "playRecord";

    private static final ObfMapping ITEM_GETBYID_INVOKE = new ObfMapping(
            "net/minecraft/item/Item",
            "func_150899_d",
            "(I)Lnet/minecraft/item/Item;"
    ).toRuntime();

    public RenderGlobalVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == INVOKESTATIC && ITEM_GETBYID_INVOKE.matches(name, desc)) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ILOAD, 4);
            mv.visitMethodInsn(INVOKESTATIC, PLAY_RECORD_OWNER, PLAY_RECORD_METHOD_NAME, PLAY_RECORD_SIGNATURE, false);
            mv.visitInsn(RETURN);
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    private static String targetSignature() {
        return "(" +
                "Lnet/minecraft/entity/player/EntityPlayer;" +
                "I" +
                "Lnet/minecraft/util/math/BlockPos;" +
                "I" +
                ")V";
    }

    private static String recordSignature() {
        return "(" +
                "Lnet/minecraft/client/renderer/RenderGlobal;" +
                "I" +
                "Lnet/minecraft/util/math/BlockPos;" +
                "I" +
                ")V";
    }
}
