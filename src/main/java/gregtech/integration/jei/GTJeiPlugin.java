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
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.machines.RecipeMapFurnace;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
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
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@JEIPlugin
public class GTJeiPlugin implements IModPlugin {

    public static IIngredientRegistry ingredientRegistry;

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
            if(!recipeMap.isHidden) {
                registry.addRecipeCategories(new RecipeMapCategory(recipeMap, registry.getJeiHelpers().getGuiHelper()));
            }
        }
        for (FuelRecipeMap fuelRecipeMap : FuelRecipeMap.getRecipeMaps()) {
            registry.addRecipeCategories(new FuelRecipeMapCategory(fuelRecipeMap, registry.getJeiHelpers().getGuiHelper()));
        }
        registry.addRecipeCategories(new PrimitiveBlastRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new CokeOvenRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new OreByProductCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new GTOreCategory(registry.getJeiHelpers().getGuiHelper()));
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
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(modularUIGuiHandler, VanillaRecipeCategoryUid.CRAFTING);

        for (RecipeMap<?> recipeMap : RecipeMap.getRecipeMaps()) {
            List<GTRecipeWrapper> recipesList = recipeMap.getRecipeList()
                .stream().filter(recipe -> !recipe.isHidden() && recipe.hasValidInputsForDisplay())
                .map(GTRecipeWrapper::new)
                .collect(Collectors.toList());
            registry.addRecipes(recipesList, GTValues.MODID + ":" + recipeMap.unlocalizedName);
        }

        for (FuelRecipeMap fuelRecipeMap : FuelRecipeMap.getRecipeMaps()) {
            List<GTFuelRecipeWrapper> recipeList = fuelRecipeMap.getRecipeList().stream()
                .map(GTFuelRecipeWrapper::new)
                .collect(Collectors.toList());
            registry.addRecipes(recipeList, GTValues.MODID + ":" + fuelRecipeMap.unlocalizedName);
        }

        for (ResourceLocation metaTileEntityId : GregTechAPI.META_TILE_ENTITY_REGISTRY.getKeys()) {
            MetaTileEntity metaTileEntity = GregTechAPI.META_TILE_ENTITY_REGISTRY.getObject(metaTileEntityId);
            assert metaTileEntity != null;
            if (metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null) != null) {
                IControllable workableCapability = metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);

                if (workableCapability instanceof AbstractRecipeLogic) {
                    RecipeMap<?> recipeMap = ((AbstractRecipeLogic) workableCapability).recipeMap;
                    registry.addRecipeCatalyst(metaTileEntity.getStackForm(), GTValues.MODID + ":" + recipeMap.unlocalizedName);
                    if (recipeMap instanceof RecipeMapFurnace) {
                        registry.addRecipeCatalyst(metaTileEntity.getStackForm(), VanillaRecipeCategoryUid.SMELTING);
                    }
                } else if (workableCapability instanceof FuelRecipeLogic) {
                    FuelRecipeMap recipeMap = ((FuelRecipeLogic) workableCapability).recipeMap;
                    registry.addRecipeCatalyst(metaTileEntity.getStackForm(), GTValues.MODID + ":" + recipeMap.unlocalizedName);
                }
            }
        }

        for (MetaTileEntity breweryTile : MetaTileEntities.BREWERY) {
            if (breweryTile == null) continue;
            registry.addRecipeCatalyst(breweryTile.getStackForm(), VanillaRecipeCategoryUid.BREWING);
        }

        String semiFluidMapId = GTValues.MODID + ":" + RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.getUnlocalizedName();
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_BRONZE_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_STEEL_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_TITANIUM_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_TUNGSTENSTEEL_BOILER.getStackForm(), semiFluidMapId);

        registry.addIngredientInfo(Objects.requireNonNull(Materials.Air.getFluid(1000)), VanillaTypes.FLUID, I18n.format("gregtech.machine.air_collector.jei_description"));
        registry.addIngredientInfo(Objects.requireNonNull(MetaItems.FLUID_CELL.getStackForm()), VanillaTypes.ITEM, I18n.format("gregtech.item.fluid_cell.jei_description"));

        String primitiveBlastId = GTValues.MODID + ":" + "primitive_blast_furnace";
        registry.addRecipes(RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES.stream()
            .map(PrimitiveBlastRecipeWrapper::new)
            .collect(Collectors.toList()), primitiveBlastId);
        registry.addRecipeCatalyst(MetaTileEntities.PRIMITIVE_BLAST_FURNACE.getStackForm(), primitiveBlastId);

        String cokeOvenId = GTValues.MODID + ":" + "coke_oven";
        registry.addRecipes(RecipeMaps.COKE_OVEN_RECIPES.stream()
            .map(CokeOvenRecipeWrapper::new)
            .collect(Collectors.toList()), cokeOvenId);
        registry.addRecipeCatalyst(MetaTileEntities.COKE_OVEN.getStackForm(), cokeOvenId);

        List<OreByProduct> oreByproductList = new CopyOnWriteArrayList<>();
        for (Material material : Material.MATERIAL_REGISTRY) {
            if (material instanceof DustMaterial && OreDictUnifier.get(OrePrefix.ore, material) != ItemStack.EMPTY) {
                final OreByProduct oreByProduct = new OreByProduct((DustMaterial) material);
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

        //Ore Veins
        List<OreDepositDefinition> oreVeins = WorldGenRegistry.getOreDeposits();
        List<GTOreInfo> oreInfoList = new CopyOnWriteArrayList<>();
        for(OreDepositDefinition vein : oreVeins) {
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

        for (MetaTileEntity machine : MetaTileEntities.FLUID_CANNER) {
            if (machine == null) continue;
            registry.addIngredientInfo(machine.getStackForm(), VanillaTypes.ITEM,
                "gregtech.machine.fluid_canner.jei_description");
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
