package gregtech.integration.groovy;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.modules.GregTechModule;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.crafttweaker.material.MaterialExpansion;
import gregtech.integration.crafttweaker.material.MaterialPropertyExpansion;
import gregtech.modules.GregTechModules;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.GroovyPlugin;
import com.cleanroommc.groovyscript.api.IGameObjectHandler;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.gameobjects.GameObjectHandlerManager;
import com.cleanroommc.groovyscript.sandbox.expand.ExpansionHelper;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Optional.Interface(modid = GTValues.MODID_GROOVYSCRIPT,
                    iface = "com.cleanroommc.groovyscript.api.GroovyPlugin",
                    striprefs = true)
@GregTechModule(
                moduleID = GregTechModules.MODULE_GRS,
                containerID = GTValues.MODID,
                modDependencies = GTValues.MODID_GROOVYSCRIPT,
                name = "GregTech GroovyScript Integration",
                description = "GroovyScript Integration Module")
public class GroovyScriptModule extends IntegrationSubmodule implements GroovyPlugin {

    private static GroovyContainer<?> modSupportContainer;
    private static final Map<String, Map<String, ItemStack>> metaItems = new Object2ObjectOpenHashMap<>();

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return ImmutableList.of(GroovyHandCommand.class, GroovyScriptModule.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRecipeEvent(RegistryEvent.Register<IRecipe> event) {
        GroovyScriptModule.loadMetaItemBracketHandler();
    }

    public static boolean isCurrentlyRunning() {
        return GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_GRS) &&
                GroovyScript.getSandbox().isRunning();
    }

    public static GroovyContainer<?> getInstance() {
        return modSupportContainer;
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

    @Nullable
    public static ItemStack getMetaItem(String name) {
        String[] resultName = splitObjectName(name);
        Map<String, ItemStack> map = metaItems.get(resultName[0]);
        if (map != null) {
            ItemStack stack = map.get(resultName[1]);
            if (stack != null) {
                return stack.copy();
            }
        }

        ItemStack stack = getMetaTileEntityItem(resultName);
        if (stack != null) {
            return stack.copy();
        }
        return null;
    }

    @Nullable
    public static ItemStack getMetaTileEntityItem(String[] split) {
        MetaTileEntity metaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(new ResourceLocation(split[0], split[1]));
        return metaTileEntity == null ? null : metaTileEntity.getStackForm();
    }

    public static String[] splitObjectName(String toSplit) {
        String[] resultSplit = { GTValues.MODID, toSplit };
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
            String modid = entry.getKey().getModid();
            Map<String, ItemStack> map = metaItems.computeIfAbsent(modid, (k) -> new Object2ObjectOpenHashMap<>());
            String name = "block" + entry.getKey().toCamelCaseString();
            ItemStack stack = entry.getValue().getItem(entry.getKey());
            map.put(name, stack);
        }
        for (Map.Entry<Material, BlockFrame> entry : MetaBlocks.FRAMES.entrySet()) {
            String modid = entry.getKey().getModid();
            Map<String, ItemStack> map = metaItems.computeIfAbsent(modid, (k) -> new Object2ObjectOpenHashMap<>());
            String name = "frame" + entry.getKey().toCamelCaseString();
            ItemStack stack = entry.getValue().getItem(entry.getKey());
            map.put(name, stack);
        }

        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
            String modid = registry.getModid();
            Map<String, ItemStack> map = metaItems.computeIfAbsent(modid, (k) -> new Object2ObjectOpenHashMap<>());

            for (BlockCable cable : MetaBlocks.CABLES.get(modid)) {
                for (Material material : cable.getEnabledMaterials()) {
                    String name = cable.getPrefix().name + material.toCamelCaseString();
                    ItemStack stack = cable.getItem(material);
                    map.put(name, stack);
                }
            }
            for (BlockItemPipe pipe : MetaBlocks.ITEM_PIPES.get(modid)) {
                for (Material material : pipe.getEnabledMaterials()) {
                    String name = pipe.getPrefix().name + material.toCamelCaseString();
                    ItemStack stack = pipe.getItem(material);
                    map.put(name, stack);
                }
            }
            for (BlockFluidPipe pipe : MetaBlocks.FLUID_PIPES.get(modid)) {
                for (Material material : pipe.getEnabledMaterials()) {
                    String name = pipe.getPrefix().name + material.toCamelCaseString();
                    ItemStack stack = pipe.getItem(material);
                    map.put(name, stack);
                }
            }
            metaItems.put(modid, map);
        }

        for (MetaItem<?> item : MetaItem.getMetaItems()) {
            Map<String, ItemStack> map = metaItems.computeIfAbsent(
                    Objects.requireNonNull(item.getRegistryName()).getNamespace(),
                    (k) -> new Object2ObjectOpenHashMap<>());
            for (MetaItem<?>.MetaValueItem entry : item.getAllItems()) {
                if (!entry.unlocalizedName.equals("meta_item")) {
                    map.put(entry.unlocalizedName, entry.getStackForm());
                }
            }
        }
    }

    @Override
    public @NotNull String getModId() {
        return GTValues.MODID;
    }

    @Override
    public @NotNull String getModName() {
        return "GregTech";
    }

    @Override
    public void onCompatLoaded(GroovyContainer<?> groovyContainer) {
        modSupportContainer = groovyContainer;
        GameObjectHandlerManager.registerGameObjectHandler(GTValues.MODID, "recipemap",
                IGameObjectHandler.wrapStringGetter(RecipeMap::getByName));

        GameObjectHandlerManager.registerGameObjectHandler(GTValues.MODID, "material",
                IGameObjectHandler.wrapStringGetter(GregTechAPI.materialManager::getMaterial));

        GameObjectHandlerManager.registerGameObjectHandler(GTValues.MODID, "oreprefix",
                IGameObjectHandler.wrapStringGetter(OrePrefix::getPrefix));

        GameObjectHandlerManager.registerGameObjectHandler(GTValues.MODID, "metaitem",
                IGameObjectHandler.wrapStringGetter(GroovyScriptModule::getMetaItem), ItemStack.EMPTY);

        ExpansionHelper.mixinClass(Material.class, MaterialExpansion.class);
        ExpansionHelper.mixinClass(Material.class, MaterialPropertyExpansion.class);
        ExpansionHelper.mixinClass(Material.Builder.class, GroovyMaterialBuilderExpansion.class);
        ExpansionHelper.mixinClass(RecipeBuilder.class, GroovyRecipeBuilderExpansion.class);
    }
}
