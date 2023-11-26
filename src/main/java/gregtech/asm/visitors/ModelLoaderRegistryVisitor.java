package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ModelLoaderRegistryVisitor extends MethodVisitor {

    public static final String TARGET_CLASS_NAME = "net/minecraftforge/client/model/ModelLoaderRegistry";

    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME,
            "clearModelCache",
            "(Lnet/minecraft/client/resources/IResourceManager;)V");

    private static final String CLEAR_CACHE_OWNER = "gregtech/api/unification/material/info/MaterialIconType";
    private static final String CLEAR_CACHE_METHOD_NAME = "clearCache";
    private static final String CLEAR_CACHE_DESC = "()V";

    public ModelLoaderRegistryVisitor(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            visitMethodInsn(INVOKESTATIC, CLEAR_CACHE_OWNER, CLEAR_CACHE_METHOD_NAME, CLEAR_CACHE_DESC, false);
        }
        super.visitInsn(opcode);
    }
}
