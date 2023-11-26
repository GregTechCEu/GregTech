package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public final class MinecraftVisitor extends AdviceAdapter implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/client/Minecraft";

    public static final ObfMapping PROCESS_KEY_F3 = new ObfMapping(TARGET_CLASS_NAME, "func_184122_c", "(I)Z");

    public MinecraftVisitor(MethodVisitor mv) {
        super(ASM5, mv, ACC_PRIVATE, PROCESS_KEY_F3.s_name, PROCESS_KEY_F3.s_desc);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode == IRETURN) {
            visitVarInsn(ILOAD, 1); // load key pressed
            visitMethodInsn(INVOKESTATIC, "gregtech/asm/hooks/MinecraftHooks", "sendF3HMessage", "(I)V", false);
        }
    }
}
