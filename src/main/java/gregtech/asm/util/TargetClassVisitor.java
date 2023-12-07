package gregtech.asm.util;

import net.minecraftforge.fml.common.FMLLog;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Function;

public class TargetClassVisitor extends ClassVisitor {

    private String className;
    private final ObfMapping methodKey;
    private final Function<MethodVisitor, MethodVisitor> visitorCreator;
    private boolean foundMethod = false;

    public TargetClassVisitor(ClassVisitor cv, ObfMapping methodKey,
                              Function<MethodVisitor, MethodVisitor> visitorCreator) {
        super(Opcodes.ASM5, cv);
        this.methodKey = methodKey.toRuntime();
        this.visitorCreator = visitorCreator;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @SuppressWarnings("deprecation")
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        String methodKey = name + desc;
        if (this.methodKey.matches(name, desc)) {
            FMLLog.log("GregTechTransformer", Level.INFO, "Patched method %s in %s successfully", methodKey, className);
            this.foundMethod = true;
            return visitorCreator.apply(visitor);
        }
        return visitor;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void visitEnd() {
        super.visitEnd();
        if (!foundMethod) {
            FMLLog.log("GregTechTransformer", Level.FATAL, "Failed to find method %s in %s.", methodKey, className);
            throw new RuntimeException("Failed to patch method " + methodKey +
                    ", loading cannot continue. Check your environment is correct.");
        }
    }
}
