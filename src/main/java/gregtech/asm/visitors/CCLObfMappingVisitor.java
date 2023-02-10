package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CCLObfMappingVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "codechicken/lib/reflect/ObfMapping$MCPRemapper";
    public static final String TARGET_SIGNATURE = "()[Ljava/io/File;";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "getConfFiles", TARGET_SIGNATURE);

    public CCLObfMappingVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitCode() {
        mv.visitMethodInsn(INVOKESTATIC, "gregtech/asm/hooks/CCLObfMappingHooks", "getConfFiles", "()[Ljava/io/File;", false);
        mv.visitInsn(ARETURN);
    }
}
