package gregtech.asm.visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

public class VintagiumManagerVistor extends ClassVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "me/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPass";

    public VintagiumManagerVistor(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        // Make BlockRenderPass.VALUES and BlockRenderPass.COUNT not final
        if (name.equals("VALUES") || name.equals("COUNT")) {
            return super.visitField(access & ~ACC_FINAL, name, desc, signature, value);
        } else {
            return super.visitField(access, name, desc, signature, value);
        }
    }
}
