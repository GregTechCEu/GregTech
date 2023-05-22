package gregtech.integration.groovy;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.brackets.BracketHandlerManager;
import com.cleanroommc.groovyscript.compat.mods.ModPropertyContainer;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.cleanroommc.groovyscript.sandbox.expand.ExpansionHelper;
import com.google.common.collect.ImmutableList;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.modules.GregTechModule;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.crafttweaker.material.MaterialExpansion;
import gregtech.integration.crafttweaker.material.MaterialPropertyExpansion;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import gregtech.modules.GregTechModules;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static gregtech.api.GregTechAPI.MATERIAL_REGISTRY;

@GregTechModule(
        moduleID = GregTechModules.MODULE_GRS,
        containerID = GTValues.MODID,
        modDependencies = GTValues.MODID_GROOVYSCRIPT,
        name = "GregTech GroovyScript Integration",
        descriptionKey = "gregtech.modules.grs_integration.description"
)
public class GroovyScriptModule extends IntegrationSubmodule {

    private static ModSupport.Container<Container> modSupportContainer;
    private static final Map<String, ItemStack> metaItems = new Object2ObjectOpenHashMap<>();

    @Nonnull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return ImmutableList.of(GroovyHandCommand.class, GroovyScriptModule.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRecipeEvent(RegistryEvent.Register<IRecipe> event) {
        GroovyScriptModule.loadMetaItemBracketHandler();
    }

    @Override
    public void construction(FMLConstructionEvent event) {
        modSupportContainer = new ModSupport.Container<>(GTValues.MODID, "GregTech", Container::new, "gt");
    }

    public static boolean isCurrentlyRunning() {
        return GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_GRS)
                && GroovyScript.getSandbox().isRunning();
    }

    public static Container getInstance() {
        return modSupportContainer.get();
    }

    public static boolean validateNonNull(Object o, Supplier<String> errorMsg) {
        if (o == null) {
            if (isCurrentlyRunning()) {
                GroovyLog.get().error(errorMsg.get());
            }
            return false;
        }
        return true;
    }

    public static ItemStack getMetaItem(String name) {
        ItemStack item;
        if ((item = metaItems.get(name)) != null) {
            return item.copy();
        }
        if ((item = getMetaTileEntityItem(name)) != null) {
            return item.copy();
        }
        return null;
    }

    @Nullable
    public static ItemStack getMetaTileEntityItem(String name) {
        String[] resultName = splitObjectName(name);
        MetaTileEntity metaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(new ResourceLocation(resultName[0], resultName[1]));
        return metaTileEntity == null ? null : metaTileEntity.getStackForm();
    }

    public static String[] splitObjectName(String toSplit) {
        String[] resultSplit = new String[]{GTValues.MODID, toSplit};
        int i = toSplit.indexOf(':');
        if (i >= 0) {
            resultSplit[1] = toSplit.substring(i + 1);
            if (i > 1) {
                resultSplit[0] = toSplit.substring(0, i);
            }
        }
        return resultSplit;
    }

    public static void loadMetaItemBracketHandler() {
        metaItems.clear();
        for (Map.Entry<Material, BlockCompressed> entry : MetaBlocks.COMPRESSED.entrySet()) {
            metaItems.put("block" + entry.getKey().toCamelCaseString(), entry.getValue().getItem(entry.getKey()));
        }
        for (Map.Entry<Material, BlockFrame> entry : MetaBlocks.FRAMES.entrySet()) {
            metaItems.put("frame" + entry.getKey().toCamelCaseString(), entry.getValue().getItem(entry.getKey()));
        }

        for (BlockCable cable : MetaBlocks.CABLES) {
            for (Material material : cable.getEnabledMaterials()) {
                metaItems.put(cable.getPrefix().name + material.toCamelCaseString(), cable.getItem(material));
            }
        }
        for (BlockItemPipe cable : MetaBlocks.ITEM_PIPES) {
            for (Material material : cable.getEnabledMaterials()) {
                metaItems.put(cable.getPrefix().name + material.toCamelCaseString(), cable.getItem(material));
            }
        }
        for (BlockFluidPipe cable : MetaBlocks.FLUID_PIPES) {
            for (Material material : cable.getEnabledMaterials()) {
                metaItems.put(cable.getPrefix().name + material.toCamelCaseString(), cable.getItem(material));
            }
        }

        for (MetaItem<?> item : MetaItem.getMetaItems()) {
            for (MetaItem<?>.MetaValueItem entry : item.getAllItems()) {
                if (!entry.unlocalizedName.equals("meta_item")) {
                    metaItems.put(entry.unlocalizedName, entry.getStackForm());
                }
            }
        }
    }

    /**
     * A GroovyScript mod compat container. This should not be referenced when GrS is not installed!
     */
    public static class Container extends ModPropertyContainer {

        private Container() {
        }

        @Override
        protected void addRegistry(VirtualizedRegistry<?> registry) {
            super.addRegistry(registry);
        }

        @Override
        public void initialize() {
            BracketHandlerManager.registerBracketHandler(GTValues.MODID, "recipemap", RecipeMap::getByName);
            BracketHandlerManager.registerBracketHandler(GTValues.MODID, "material", MATERIAL_REGISTRY::getObject);
            BracketHandlerManager.registerBracketHandler(GTValues.MODID, "oreprefix", OrePrefix::getPrefix);
            BracketHandlerManager.registerBracketHandler(GTValues.MODID, "metaitem", GroovyScriptModule::getMetaItem, ItemStack.EMPTY);

            ExpansionHelper.mixinClass(Material.class, MaterialExpansion.class);
            ExpansionHelper.mixinClass(Material.class, MaterialPropertyExpansion.class);
            ExpansionHelper.mixinClass(Material.Builder.class, GroovyMaterialBuilderExpansion.class);
            ExpansionHelper.mixinClass(RecipeBuilder.class, GroovyRecipeBuilderExpansion.class);
        }
    }
}
