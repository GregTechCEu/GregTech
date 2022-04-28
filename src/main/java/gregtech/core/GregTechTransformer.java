package gregtech.core;

import gregtech.common.ConfigHolder;
import gregtech.core.util.TargetClassVisitor;
import gregtech.core.visitors.*;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class GregTechTransformer implements IClassTransformer, Opcodes {

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
                if (ConfigHolder.recipes.disableConcreteInWorld) {
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
            case RenderChunkVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, RenderChunkVisitor.TARGET_METHOD, RenderChunkVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case EntityRendererVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, EntityRendererVisitor.TARGET_METHOD, EntityRendererVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case BlockVisitor.TARGET_CLASS_NAME: {
                try {
                    Class.forName("team.chisel.ctm.CTM", false, Launch.classLoader);
                } catch (ClassNotFoundException ignored) {
                    ClassReader classReader = new ClassReader(basicClass);
                    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, 0);
                    BlockVisitor.handleClassNode(classNode).accept(classWriter);
                    return classWriter.toByteArray();
                }
                break;
            }
            case WorldVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, WorldVisitor.TARGET_METHOD, WorldVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case ModelCTMVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, ModelCTMVisitor.TARGET_METHOD, ModelCTMVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case AbstractCTMBakedModelVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, AbstractCTMBakedModelVisitor.TARGET_METHOD, AbstractCTMBakedModelVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case LittleTilesVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, LittleTilesVisitor.TARGET_METHOD, LittleTilesVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case CCLVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, CCLVisitor.TARGET_METHOD, CCLVisitor::new), 0);
                return classWriter.toByteArray();
            }
            case NuclearCraftRecipeHelperVisitor.TARGET_CLASS_NAME: {
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(0);

                ModContainer container = Loader.instance().getIndexedModList().get("nuclearcraft");
                if (container.getVersion().contains("2o")) { // overhauled
                    classReader.accept(new TargetClassVisitor(classWriter, NuclearCraftRecipeHelperVisitor.TARGET_METHOD_NCO, NuclearCraftRecipeHelperVisitor::new), 0);
                } else {
                    classReader.accept(new TargetClassVisitor(classWriter, NuclearCraftRecipeHelperVisitor.TARGET_METHOD_NC, NuclearCraftRecipeHelperVisitor::new), 0);
                }
                return classWriter.toByteArray();
            }
            case RenderItemVisitor.TARGET_CLASS_NAME: {
                if (Loader.isModLoaded("enderio")) {
                    return basicClass;
                }
                ClassReader classReader = new ClassReader(basicClass);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classReader.accept(new TargetClassVisitor(classWriter, RenderItemVisitor.TARGET_METHOD, RenderItemVisitor::new), 0);
                return classWriter.toByteArray();
            }
        }
        return basicClass;
    }
}
