package gregtech.common.asm;

import gregtech.common.ConfigHolder;
import gregtech.common.asm.util.TargetClassVisitor;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class GTCETransformer implements IClassTransformer, Opcodes {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        String internalName = transformedName.replace('.', '/');
        if (internalName.equals(JEIVisitor.TARGET_CLASS_NAME)) {
            ClassReader classReader = new ClassReader(basicClass);
            ClassWriter classWriter = new ClassWriter(0);
            classReader.accept(new TargetClassVisitor(classWriter, JEIVisitor.TARGET_METHOD, JEIVisitor::new), 0);
            return classWriter.toByteArray();
        } else if (internalName.equals(ConcretePowderVisitor.TARGET_CLASS_NAME)) {
            if (ConfigHolder.vanillaRecipes.disableConcreteInWorld) {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, ConcretePowderVisitor.TARGET_METHOD, ConcretePowderVisitor::new), 0);
                return classWriter.toByteArray();
            }
        }
        return basicClass;
    }
}
