package gregtech.core;

import gregtech.api.GTValues;
import gregtech.common.ConfigHolder;
import gregtech.core.util.TargetClassVisitor;
import gregtech.core.visitors.*;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class GTCETransformer implements IClassTransformer, Opcodes {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        String internalName = transformedName.replace('.', '/');
        switch (internalName) {
            case JEIVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(0);
                classReader.accept(new TargetClassVisitor(classWriter, JEIVisitor.TARGET_METHOD, JEIVisitor::new), 0);
                return classWriter.toByteArray();

            }
            case ConcretePowderVisitor.TARGET_CLASS_NAME:
                if (ConfigHolder.vanillaRecipes.disableConcreteInWorld) {
                    ClassReader classReader = new ClassReader(basicClass);
                    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                    classReader.accept(new TargetClassVisitor(classWriter, ConcretePowderVisitor.TARGET_METHOD, ConcretePowderVisitor::new), 0);
                    return classWriter.toByteArray();
                }
                break;
            case LayerCustomHeadVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, LayerCustomHeadVisitor.TARGET_METHOD, LayerCustomHeadVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case SpecialArmorApplyVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new SpecialArmorClassVisitor(classWriter, SpecialArmorApplyVisitor.TARGET_METHOD, SpecialArmorApplyVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case LayerArmorBaseVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, LayerArmorBaseVisitor.TARGET_METHOD, LayerArmorBaseVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case RegionRenderCacheBuilderVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, RegionRenderCacheBuilderVisitor.TARGET_METHOD, RegionRenderCacheBuilderVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case EntityRendererVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, EntityRendererVisitor.TARGET_METHOD, EntityRendererVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case BlockVisitor.TARGET_CLASS_NAME:{
                if (!GTValues.isModLoaded("team.chisel.ctm.CTM")) {
                    ClassReader classReader = new ClassReader(basicClass);
                    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, 0);
                    BlockVisitor.handleClassNode(classNode).accept(classWriter);
                    return classWriter.toByteArray();
                }
            }
        }
        return basicClass;
    }
}
