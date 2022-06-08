package gregtech.core.visitors;

import gregtech.core.util.ObfMapping;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RenderItemVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/client/renderer/RenderItem";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "func_180453_a", "(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"); // renderItemOverlayIntoGUI
    private static final ObfMapping METHOD_RENDER_ITEM_OVERLAY = new ObfMapping(
            "gregtech/core/hooks/RenderItemHooks",
            "renderElectricBar",
            "(Lnet/minecraft/item/ItemStack;II)V");

    private boolean primed, onFrame, applied;
    private Label target;

    public RenderItemVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (!this.applied && !this.primed && this.target == null && opcode == INVOKEVIRTUAL && "showDurabilityBar".equals(name)) {
            this.primed = true;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (!this.applied && this.primed && opcode == IFEQ) {
            this.target = label;
            this.primed = false;
        }
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(Label label) {
        if (!this.applied && this.target == label) {
            this.onFrame = true;
        }
        super.visitLabel(label);
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        if (!this.applied && this.onFrame) {
            super.visitVarInsn(ALOAD, 2);
            super.visitVarInsn(ILOAD, 3);
            super.visitVarInsn(ILOAD, 4);
            METHOD_RENDER_ITEM_OVERLAY.visitMethodInsn(this, INVOKESTATIC);
            this.applied = true;
        }
        super.visitFrame(type, nLocal, local, nStack, stack);
    }
}
