package gregtech.integration.jei;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.gui.impl.ModularUIGuiHandler;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.machines.RecipeMapFurnace;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoCategory;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.recipe.*;
import gregtech.integration.jei.recipe.fuel.FuelRecipeMapCategory;
import gregtech.integration.jei.recipe.fuel.GTFuelRecipeWrapper;
import gregtech.integration.jei.recipe.primitive.*;
import gregtech.integration.jei.utils.CustomItemReturnRecipeWrapper;
import gregtech.integration.jei.utils.MachineSubtypeHandler;
import gregtech.integration.jei.utils.MetaItemSubtypeHandler;
import gregtech.loaders.recipe.CustomItemReturnShapedOreRecipeRecipe;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@JEIPlugin
public class GTJeiPlugin implements IModPlugin {

    public static IIngredientRegistry ingredientRegistry;
    public static IJeiRuntime jeiRuntime;

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        GTJeiPlugin.jeiRuntime = jeiRuntime;
    }

    @Override
    public void registerItemSubtypes(@Nonnull ISubtypeRegistry subtypeRegistry) {
        MetaItemSubtypeHandler subtype = new MetaItemSubtypeHandler();
        for (MetaItem<?> metaItem : MetaItems.ITEMS) {
            subtypeRegistry.registerSubtypeInterpreter(metaItem, subtype);
        }
        subtypeRegistry.registerSubtypeInterpreter(Item.getItemFromBlock(MetaBlocks.MACHINE), new MachineSubtypeHandler());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new IntCircuitCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new MultiblockInfoCategory(registry.getJeiHelpers()));
        for (RecipeMap<?> recipeMap : RecipeMap.getRecipeMaps()) {
            if (!recipeMap.isHidden) {
                registry.addRecipeCategories(new RecipeMapCategory(recipeMap, registry.getJeiHelpers().getGuiHelper()));
            }
        }
        for (FuelRecipeMap fuelRecipeMap : FuelRecipeMap.getRecipeMaps()) {
            registry.addRecipeCategories(new FuelRecipeMapCategory(fuelRecipeMap, registry.getJeiHelpers().getGuiHelper()));
        }
        registry.addRecipeCategories(new OreByProductCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new GTOreCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new MaterialTreeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();

        registry.addRecipes(IntCircuitRecipeWrapper.create(), IntCircuitCategory.UID);
        MultiblockInfoCategory.registerRecipes(registry);
        registry.handleRecipes(CustomItemReturnShapedOreRecipeRecipe.class, recipe -> new CustomItemReturnRecipeWrapper(jeiHelpers, recipe), VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipeRegistryPlugin(new FacadeRegistryPlugin());

        ModularUIGuiHandler modularUIGuiHandler = new ModularUIGuiHandler(jeiHelpers.recipeTransferHandlerHelper());
        registry.addAdvancedGuiHandlers(modularUIGuiHandler);
        registry.addGhostIngredientHandler(modularUIGuiHandler.getGuiContainerClass(), modularUIGuiHandler);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(modularUIGuiHandler, Constants.UNIVERSAL_RECIPE_TRANSFER_UID);

        for (RecipeMap<?> recipeMap : RecipeMap.getRecipeMaps()) {
            if(!recipeMap.isHidden) {
                List<GTRecipeWrapper> recipesList = recipeMap.getRecipeList()
                        .stream()
                        .filter(recipe -> !recipe.isHidden() && recipe.hasValidInputsForDisplay())
                        .map(GTRecipeWrapper::new)
                        .collect(Collectors.toList());
                registry.addRecipes(recipesList, GTValues.MODID + ":" + recipeMap.unlocalizedName);
            }
        }

        for (FuelRecipeMap fuelRecipeMap : FuelRecipeMap.getRecipeMaps()) {
            List<GTFuelRecipeWrapper> recipeList = fuelRecipeMap.getRecipeList().stream()
                    .map(GTFuelRecipeWrapper::new)
                    .collect(Collectors.toList());
            registry.addRecipes(recipeList, GTValues.MODID + ":" + fuelRecipeMap.unlocalizedName);
        }

        List<SteamMetaTileEntity> deferredCatalysts = new ArrayList<>();
        for (ResourceLocation metaTileEntityId : GregTechAPI.MTE_REGISTRY.getKeys()) {
            MetaTileEntity metaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(metaTileEntityId);
            assert metaTileEntity != null;
            if (metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null) != null) {
                IControllable workableCapability = metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);

                if (workableCapability instanceof AbstractRecipeLogic) {
                    if (metaTileEntity instanceof SteamMetaTileEntity) {
                        deferredCatalysts.add((SteamMetaTileEntity) metaTileEntity);
                    } else {
                        RecipeMap<?> recipeMap = ((AbstractRecipeLogic) workableCapability).recipeMap;
                        registry.addRecipeCatalyst(metaTileEntity.getStackForm(), GTValues.MODID + ":" + recipeMap.unlocalizedName);
                        if (recipeMap instanceof RecipeMapFurnace) {
                            registry.addRecipeCatalyst(metaTileEntity.getStackForm(), VanillaRecipeCategoryUid.SMELTING);
                        }
                    }
                } else if (workableCapability instanceof FuelRecipeLogic) {
                    FuelRecipeMap recipeMap = ((FuelRecipeLogic) workableCapability).recipeMap;
                    registry.addRecipeCatalyst(metaTileEntity.getStackForm(), GTValues.MODID + ":" + recipeMap.unlocalizedName);
                }
            }
        }
        for (SteamMetaTileEntity steamMetaTileEntity : deferredCatalysts) {
            IControllable workableCapability = steamMetaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
            RecipeMap<?> recipeMap = ((AbstractRecipeLogic) workableCapability).recipeMap;
            registry.addRecipeCatalyst(steamMetaTileEntity.getStackForm(), GTValues.MODID + ":" + recipeMap.unlocalizedName);
            if (recipeMap instanceof RecipeMapFurnace) {
                registry.addRecipeCatalyst(steamMetaTileEntity.getStackForm(), VanillaRecipeCategoryUid.SMELTING);
            }
        }

        String semiFluidMapId = GTValues.MODID + ":" + RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.getUnlocalizedName();
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_BRONZE_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_STEEL_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_TITANIUM_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_TUNGSTENSTEEL_BOILER.getStackForm(), semiFluidMapId);

        registry.addIngredientInfo(Objects.requireNonNull(MetaItems.FLUID_CELL.getStackForm()), VanillaTypes.ITEM, I18n.format("gregtech.item.fluid_cell.jei_description"));

        //TODO, add Electromagnetic Separator to the Ore Byproduct page
        List<OreByProduct> oreByproductList = new CopyOnWriteArrayList<>();
        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {
            if (material.hasProperty(PropertyKey.ORE)) {
                final OreByProduct oreByProduct = new OreByProduct(material);
                if (oreByProduct.hasByProducts())
                    oreByproductList.add(oreByProduct);
            }
        }
        String oreByProductId = GTValues.MODID + ":" + "ore_by_product";
        registry.addRecipes(oreByproductList, oreByProductId);
        for (MetaTileEntity machine : MetaTileEntities.MACERATOR) {
            if (machine == null) continue;
            registry.addRecipeCatalyst(machine.getStackForm(), oreByProductId);
        }
        for (MetaTileEntity machine : MetaTileEntities.ORE_WASHER) {
            if (machine == null) continue;
            registry.addRecipeCatalyst(machine.getStackForm(), oreByProductId);
        }
        for (MetaTileEntity machine : MetaTileEntities.CENTRIFUGE) {
            if (machine == null) continue;
            registry.addRecipeCatalyst(machine.getStackForm(), oreByProductId);
        }
        for (MetaTileEntity machine : MetaTileEntities.THERMAL_CENTRIFUGE) {
            if (machine == null) continue;
            registry.addRecipeCatalyst(machine.getStackForm(), oreByProductId);
        }
        for (MetaTileEntity machine : MetaTileEntities.CHEMICAL_BATH) {
            if (machine == null) continue;
            registry.addRecipeCatalyst(machine.getStackForm(), oreByProductId);
        }

        //Material Tree
        List<MaterialTree> materialTreeList = new CopyOnWriteArrayList<>();
        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {
            if (material.hasProperty(PropertyKey.DUST)) {
                materialTreeList.add(new MaterialTree(material));
            }
        }
        registry.addRecipes(materialTreeList, GTValues.MODID + ":" + "material_tree");

        //Ore Veins
        List<OreDepositDefinition> oreVeins = WorldGenRegistry.getOreDeposits();
        List<GTOreInfo> oreInfoList = new CopyOnWriteArrayList<>();
        for (OreDepositDefinition vein : oreVeins) {
            oreInfoList.add(new GTOreInfo(vein));
        }

        String oreSpawnID = GTValues.MODID + ":" + "ore_spawn_location";
        registry.addRecipes(oreInfoList, oreSpawnID);
        registry.addRecipeCatalyst(MetaItems.SCANNER.getStackForm(), oreSpawnID);
        //Ore Veins End


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

        //Multiblock info page registration
        MultiblockInfoCategory.multiblockRecipes.values().forEach(v -> {
            MultiblockInfoPage infoPage = v.getInfoPage();
            registry.addIngredientInfo(infoPage.getController().getStackForm(),
                    VanillaTypes.ITEM,
                    infoPage.getDescription());
        });
    }
}
