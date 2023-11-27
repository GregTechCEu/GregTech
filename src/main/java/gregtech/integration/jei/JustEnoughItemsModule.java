package gregtech.integration.jei;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IMultipleRecipeMaps;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.modules.GregTechModule;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.recipes.machines.IScannerRecipeMap;
import gregtech.api.recipes.machines.RecipeMapFurnace;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.worldgen.config.BedrockFluidDepositDefinition;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.gui.widget.craftingstation.CraftingSlotWidget;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.jei.basic.*;
import gregtech.integration.jei.multiblock.MultiblockInfoCategory;
import gregtech.integration.jei.recipe.*;
import gregtech.integration.jei.utils.MachineSubtypeHandler;
import gregtech.integration.jei.utils.MetaItemSubtypeHandler;
import gregtech.integration.jei.utils.ModularUIGuiHandler;
import gregtech.integration.jei.utils.MultiblockInfoRecipeFocusShower;
import gregtech.modules.GregTechModules;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.relauncher.Side;

import mezz.jei.Internal;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.InputHandler;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JEIPlugin
@GregTechModule(
                moduleID = GregTechModules.MODULE_JEI,
                containerID = GTValues.MODID,
                modDependencies = GTValues.MODID_JEI,
                name = "GregTech JEI Integration",
                description = "JustEnoughItems Integration Module")
public class JustEnoughItemsModule extends IntegrationSubmodule implements IModPlugin {

    public static IIngredientRegistry ingredientRegistry;
    public static IJeiRuntime jeiRuntime;
    public static IGuiHelper guiHelper;

    @Override
    public void loadComplete(FMLLoadCompleteEvent event) {
        if (event.getSide() == Side.CLIENT) {
            setupInputHandler();
        }
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        JustEnoughItemsModule.jeiRuntime = jeiRuntime;
    }

    @Override
    public void registerItemSubtypes(@NotNull ISubtypeRegistry subtypeRegistry) {
        MetaItemSubtypeHandler subtype = new MetaItemSubtypeHandler();
        for (MetaItem<?> metaItem : MetaItems.ITEMS) {
            subtypeRegistry.registerSubtypeInterpreter(metaItem, subtype);
        }
        subtypeRegistry.registerSubtypeInterpreter(Item.getItemFromBlock(MetaBlocks.MACHINE),
                new MachineSubtypeHandler());
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
        guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new IntCircuitCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new MultiblockInfoCategory(registry.getJeiHelpers()));
        for (RecipeMap<?> recipeMap : RecipeMap.getRecipeMaps()) {
            if (!recipeMap.isHidden) {
                for (GTRecipeCategory category : recipeMap.getRecipesByCategory().keySet()) {
                    registry.addRecipeCategories(
                            new RecipeMapCategory(recipeMap, category, registry.getJeiHelpers().getGuiHelper()));
                }
            }
        }
        registry.addRecipeCategories(new OreByProductCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new GTOreCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new GTFluidVeinCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new MaterialTreeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();

        registry.addRecipes(IntCircuitRecipeWrapper.create(), IntCircuitCategory.UID);
        MultiblockInfoCategory.registerRecipes(registry);
        registry.addRecipeRegistryPlugin(new FacadeRegistryPlugin());

        // register transfer handler for all categories, but not for the crafting station
        ModularUIGuiHandler modularUIGuiHandler = new ModularUIGuiHandler(jeiHelpers.recipeTransferHandlerHelper());
        modularUIGuiHandler.setValidHandlers(widget -> !(widget instanceof CraftingSlotWidget));
        modularUIGuiHandler.blacklistCategory(
                IntCircuitCategory.UID,
                GTValues.MODID + ":material_tree",
                VanillaRecipeCategoryUid.INFORMATION,
                VanillaRecipeCategoryUid.FUEL);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(modularUIGuiHandler,
                Constants.UNIVERSAL_RECIPE_TRANSFER_UID);

        registry.addAdvancedGuiHandlers(modularUIGuiHandler);
        registry.addGhostIngredientHandler(modularUIGuiHandler.getGuiContainerClass(), modularUIGuiHandler);
        // register transfer handler for crafting recipes
        ModularUIGuiHandler craftingStationGuiHandler = new ModularUIGuiHandler(
                jeiHelpers.recipeTransferHandlerHelper());
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(craftingStationGuiHandler,
                VanillaRecipeCategoryUid.CRAFTING);

        for (RecipeMap<?> recipeMap : RecipeMap.getRecipeMaps()) {
            if (!recipeMap.isHidden) {
                for (Map.Entry<GTRecipeCategory, List<Recipe>> entry : recipeMap.getRecipesByCategory().entrySet()) {
                    Stream<Recipe> recipeStream = entry.getValue().stream()
                            .filter(recipe -> !recipe.isHidden() && recipe.hasValidInputsForDisplay());

                    if (recipeMap.getSmallRecipeMap() != null) {
                        Collection<Recipe> smallRecipes = recipeMap.getSmallRecipeMap().getRecipeList();
                        recipeStream = recipeStream.filter(recipe -> !smallRecipes.contains(recipe));
                    }

                    if (recipeMap instanceof IScannerRecipeMap scannerMap) {
                        List<Recipe> scannerRecipes = scannerMap.getRepresentativeRecipes();
                        if (!scannerRecipes.isEmpty()) {
                            registry.addRecipes(scannerRecipes.stream()
                                    .map(r -> new GTRecipeWrapper(recipeMap, r))
                                    .collect(Collectors.toList()), entry.getKey().getUniqueID());
                        }
                    }

                    registry.addRecipes(recipeStream.map(r -> new GTRecipeWrapper(recipeMap, r))
                            .collect(Collectors.toList()),
                            entry.getKey().getUniqueID());
                }
            }
        }

        for (ResourceLocation metaTileEntityId : GregTechAPI.MTE_REGISTRY.getKeys()) {
            MetaTileEntity metaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(metaTileEntityId);
            assert metaTileEntity != null;
            if (metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null) != null) {
                IControllable workableCapability = metaTileEntity
                        .getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);

                if (workableCapability instanceof AbstractRecipeLogic logic) {
                    if (metaTileEntity instanceof IMultipleRecipeMaps) {
                        for (RecipeMap<?> recipeMap : ((IMultipleRecipeMaps) metaTileEntity).getAvailableRecipeMaps()) {
                            registerRecipeMapCatalyst(registry, recipeMap, metaTileEntity);
                        }
                    } else if (logic.getRecipeMap() != null) {
                        registerRecipeMapCatalyst(registry, logic.getRecipeMap(), metaTileEntity);
                    }
                }
            }
        }

        String semiFluidMapId = GTValues.MODID + ":" + RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.getUnlocalizedName();
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_BRONZE_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_STEEL_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_TITANIUM_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_TUNGSTENSTEEL_BOILER.getStackForm(), semiFluidMapId);

        List<OreByProduct> oreByproductList = new CopyOnWriteArrayList<>();
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(PropertyKey.ORE)) {
                oreByproductList.add(new OreByProduct(material));
            }
        }
        String oreByProductId = GTValues.MODID + ":" + "ore_by_product";
        registry.addRecipes(oreByproductList, oreByProductId);
        MetaTileEntity[][] machineLists = new MetaTileEntity[][] {
                MetaTileEntities.MACERATOR,
                MetaTileEntities.ORE_WASHER,
                MetaTileEntities.CENTRIFUGE,
                MetaTileEntities.THERMAL_CENTRIFUGE,
                MetaTileEntities.CHEMICAL_BATH,
                MetaTileEntities.ELECTROMAGNETIC_SEPARATOR,
                MetaTileEntities.SIFTER
        };
        for (MetaTileEntity[] machine : machineLists) {
            if (machine.length < GTValues.LV + 1 || machine[GTValues.LV] == null) continue;
            registry.addRecipeCatalyst(machine[GTValues.LV].getStackForm(), oreByProductId);
        }

        // Material Tree
        List<MaterialTree> materialTreeList = new CopyOnWriteArrayList<>();
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(PropertyKey.DUST)) {
                materialTreeList.add(new MaterialTree(material));
            }
        }
        registry.addRecipes(materialTreeList, GTValues.MODID + ":" + "material_tree");

        // Ore Veins
        List<OreDepositDefinition> oreVeins = WorldGenRegistry.getOreDeposits();
        List<GTOreInfo> oreInfoList = new CopyOnWriteArrayList<>();
        for (OreDepositDefinition vein : oreVeins) {
            oreInfoList.add(new GTOreInfo(vein));
        }

        String oreSpawnID = GTValues.MODID + ":" + "ore_spawn_location";
        registry.addRecipes(oreInfoList, oreSpawnID);
        registry.addRecipeCatalyst(MetaItems.PROSPECTOR_LV.getStackForm(), oreSpawnID);
        registry.addRecipeCatalyst(MetaItems.PROSPECTOR_HV.getStackForm(), oreSpawnID);
        registry.addRecipeCatalyst(MetaItems.PROSPECTOR_LUV.getStackForm(), oreSpawnID);
        // Ore Veins End

        // Fluid Veins
        List<BedrockFluidDepositDefinition> fluidVeins = WorldGenRegistry.getBedrockVeinDeposits();
        List<GTFluidVeinInfo> fluidVeinInfos = new CopyOnWriteArrayList<>();
        for (BedrockFluidDepositDefinition fluidVein : fluidVeins) {
            fluidVeinInfos.add(new GTFluidVeinInfo(fluidVein));
        }

        String fluidVeinSpawnID = GTValues.MODID + ":" + "fluid_spawn_location";
        registry.addRecipes(fluidVeinInfos, fluidVeinSpawnID);
        registry.addRecipeCatalyst(MetaItems.PROSPECTOR_HV.getStackForm(), fluidVeinSpawnID);
        registry.addRecipeCatalyst(MetaItems.PROSPECTOR_LUV.getStackForm(), fluidVeinSpawnID);
        // Fluid Veins End

        ingredientRegistry = registry.getIngredientRegistry();
        for (int i = 0; i <= IntCircuitIngredient.CIRCUIT_MAX; i++) {
            registry.addIngredientInfo(IntCircuitIngredient.getIntegratedCircuit(i), VanillaTypes.ITEM,
                    "metaitem.circuit.integrated.jei_description");
        }

        registry.addRecipeCatalyst(MetaTileEntities.WORKBENCH.getStackForm(), VanillaRecipeCategoryUid.CRAFTING);

        for (MetaTileEntity machine : MetaTileEntities.CANNER) {
            if (machine == null) continue;
            registry.addIngredientInfo(machine.getStackForm(), VanillaTypes.ITEM,
                    "gregtech.machine.canner.jei_description");
        }

        // Multiblock info page registration
        MultiblockInfoCategory.REGISTER.forEach(mte -> {
            String[] desc = mte.getDescription();
            if (desc.length > 0) {
                registry.addIngredientInfo(mte.getStackForm(), VanillaTypes.ITEM, mte.getDescription());
            }
        });
        registry.addIngredientInfo(new ItemStack(MetaBlocks.BRITTLE_CHARCOAL), VanillaTypes.ITEM,
                I18n.format("tile.brittle_charcoal.tooltip.1", I18n.format("tile.brittle_charcoal.tooltip.2")));
    }

    private void setupInputHandler() {
        try {
            Field inputHandlerField = Internal.class.getDeclaredField("inputHandler");
            inputHandlerField.setAccessible(true);
            InputHandler inputHandler = (InputHandler) inputHandlerField.get(null);
            List<IShowsRecipeFocuses> showsRecipeFocuses = ObfuscationReflectionHelper
                    .getPrivateValue(InputHandler.class, inputHandler, "showsRecipeFocuses");

            showsRecipeFocuses.add(new MultiblockInfoRecipeFocusShower());

        } catch (Exception e) {
            getLogger().error("Could not reflect JEI Internal inputHandler", e);
        }
    }

    private void registerRecipeMapCatalyst(IModRegistry registry, RecipeMap<?> recipeMap,
                                           MetaTileEntity metaTileEntity) {
        for (GTRecipeCategory category : recipeMap.getRecipesByCategory().keySet()) {
            RecipeMapCategory jeiCategory = RecipeMapCategory.getCategoryFor(category);
            if (jeiCategory != null) {
                registry.addRecipeCatalyst(metaTileEntity.getStackForm(), jeiCategory.getUid());
            }
        }

        if (recipeMap instanceof RecipeMapFurnace) {
            registry.addRecipeCatalyst(metaTileEntity.getStackForm(), VanillaRecipeCategoryUid.SMELTING);
            return;
        }
        if (recipeMap.getSmallRecipeMap() != null) {
            registry.addRecipeCatalyst(metaTileEntity.getStackForm(),
                    GTValues.MODID + ":" + recipeMap.getSmallRecipeMap().unlocalizedName);
            return;
        }

        for (GTRecipeCategory category : recipeMap.getRecipesByCategory().keySet()) {
            RecipeMapCategory jeiCategory = RecipeMapCategory.getCategoryFor(category);
            // don't allow a Steam Machine to be a JEI tab icon
            if (jeiCategory != null && !(metaTileEntity instanceof SteamMetaTileEntity)) {
                Object icon = category.getJEIIcon();
                if (icon instanceof TextureArea textureArea) {
                    icon = guiHelper.drawableBuilder(textureArea.imageLocation, 0, 0, 18, 18)
                            .setTextureSize(18, 18)
                            .build();
                } else if (icon == null) {
                    icon = metaTileEntity.getStackForm();
                }
                jeiCategory.setIcon(icon);
            }
        }
    }
}
