package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CCLVisitor extends MethodVisitor {

    public static final String TARGET_CLASS_NAME = "codechicken/lib/internal/ModDescriptionEnhancer";
    public static final String TARGET_SIGNATURE = "()V";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "init", TARGET_SIGNATURE);

    public CCLVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitCode() {
        mv.visitInsn(Opcodes.RETURN);
    }
}
