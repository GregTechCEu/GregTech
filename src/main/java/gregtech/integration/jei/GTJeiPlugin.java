package gregtech.integration.jei;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.gui.impl.ModularUIGuiHandler;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultipleRecipeMaps;
import gregtech.api.recipes.Recipe;
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
import gregtech.integration.jei.recipe.*;
import gregtech.integration.jei.recipe.fuel.FuelRecipeMapCategory;
import gregtech.integration.jei.recipe.fuel.GTFuelRecipeWrapper;
import gregtech.integration.jei.recipe.primitive.MaterialTree;
import gregtech.integration.jei.recipe.primitive.MaterialTreeCategory;
import gregtech.integration.jei.recipe.primitive.OreByProduct;
import gregtech.integration.jei.recipe.primitive.OreByProductCategory;
import gregtech.integration.jei.utils.CustomItemReturnRecipeWrapper;
import gregtech.integration.jei.utils.MachineSubtypeHandler;
import gregtech.integration.jei.utils.MetaItemSubtypeHandler;
import gregtech.integration.jei.utils.MultiblockInfoRecipeFocusShower;
import gregtech.loaders.recipe.CustomItemReturnShapedOreRecipeRecipe;
import mezz.jei.Internal;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.InputHandler;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                Stream<Recipe> recipeStream = recipeMap.getRecipeList().stream()
                        .filter(recipe -> !recipe.isHidden() && recipe.hasValidInputsForDisplay());

                if (recipeMap.getSmallRecipeMap() != null) {
                    List<Recipe> smallRecipes = recipeMap.getSmallRecipeMap().getRecipeList();
                    recipeStream = recipeStream.filter(recipe -> !smallRecipes.contains(recipe));
                }

                registry.addRecipes(
                        recipeStream.map(r -> new GTRecipeWrapper(recipeMap, r)).collect(Collectors.toList()),
                        GTValues.MODID + ":" + recipeMap.unlocalizedName
                );
            }
        }

        for (FuelRecipeMap fuelRecipeMap : FuelRecipeMap.getRecipeMaps()) {
            List<GTFuelRecipeWrapper> recipeList = fuelRecipeMap.getRecipeList().stream()
                    .map(GTFuelRecipeWrapper::new)
                    .collect(Collectors.toList());
            registry.addRecipes(recipeList, GTValues.MODID + ":" + fuelRecipeMap.unlocalizedName);
        }

        Map<RecipeMap<?>, List<MetaTileEntity>> deferredCatalysts = new HashMap<>();
        for (ResourceLocation metaTileEntityId : GregTechAPI.MTE_REGISTRY.getKeys()) {
            MetaTileEntity metaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(metaTileEntityId);
            assert metaTileEntity != null;
            if (metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null) != null) {
                IControllable workableCapability = metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);

                if (workableCapability instanceof AbstractRecipeLogic) {
                    AbstractRecipeLogic logic = (AbstractRecipeLogic) workableCapability;
                    if (metaTileEntity instanceof SteamMetaTileEntity) {
                        deferredCatalysts.computeIfAbsent(logic.getRecipeMap(), k -> new ArrayList<>()).add(metaTileEntity);
                    } else if (metaTileEntity instanceof IMultipleRecipeMaps && ((IMultipleRecipeMaps) metaTileEntity).hasMultipleRecipeMaps()) {
                        for (RecipeMap<?> recipeMap : ((IMultipleRecipeMaps) metaTileEntity).getAvailableRecipeMaps()) {
                            registerRecipeMapCatalyst(registry, recipeMap, metaTileEntity);
                        }
                    } else {
                        //Special Case here for the processing array
                        RecipeMap<?> recipeMap = logic.getRecipeMap();
                        if(recipeMap == null) {
                            continue;
                        }
                        registerRecipeMapCatalyst(registry, logic.getRecipeMap(), metaTileEntity);
                    }
                }
            }
        }
        for (Map.Entry<RecipeMap<?>, List<MetaTileEntity>> deferredMetaTileEntities : deferredCatalysts.entrySet()) {
            for (MetaTileEntity mte : deferredMetaTileEntities.getValue()) {
                registerRecipeMapCatalyst(registry, deferredMetaTileEntities.getKey(), mte);
            }
        }

        String semiFluidMapId = GTValues.MODID + ":" + RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.getUnlocalizedName();
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_BRONZE_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_STEEL_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_TITANIUM_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(MetaTileEntities.LARGE_TUNGSTENSTEEL_BOILER.getStackForm(), semiFluidMapId);

        List<OreByProduct> oreByproductList = new CopyOnWriteArrayList<>();
        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {
            if (material.hasProperty(PropertyKey.ORE)) {
                oreByproductList.add(new OreByProduct(material));
            }
        }
        String oreByProductId = GTValues.MODID + ":" + "ore_by_product";
        registry.addRecipes(oreByproductList, oreByProductId);
        MetaTileEntity[][] machineLists = new MetaTileEntity[][]{
                MetaTileEntities.MACERATOR,
                MetaTileEntities.ORE_WASHER,
                MetaTileEntities.CENTRIFUGE,
                MetaTileEntities.THERMAL_CENTRIFUGE,
                MetaTileEntities.CHEMICAL_BATH,
                MetaTileEntities.ELECTROMAGNETIC_SEPARATOR,
                MetaTileEntities.SIFTER,
                MetaTileEntities.ORE_WASHER
        };
        for (MetaTileEntity[] machine : machineLists) {
            if (machine.length < 1 || machine[0] == null) continue;
            registry.addRecipeCatalyst(machine[0].getStackForm(), oreByProductId);
        }

        //Material Tree
        List<MaterialTree> materialTreeList = new CopyOnWriteArrayList<>();
        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {
            if (material.hasProperty(PropertyKey.DUST) && !material.isHidden()) {
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
        registry.addRecipeCatalyst(MetaItems.PROSPECTOR_LV.getStackForm(), oreSpawnID);
        registry.addRecipeCatalyst(MetaItems.PROSPECTOR_HV.getStackForm(), oreSpawnID);
        registry.addRecipeCatalyst(MetaItems.PROSPECTOR_LUV.getStackForm(), oreSpawnID);
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
        MultiblockInfoCategory.REGISTER.forEach(mte->{
            String[] desc = mte.getDescription();
            if (desc.length > 0) {
                registry.addIngredientInfo(mte.getStackForm(), VanillaTypes.ITEM, mte.getDescription());
            }
        });
    }

    public static void setupInputHandler() {
        try {
            Field inputHandlerField = Internal.class.getDeclaredField("inputHandler");
            inputHandlerField.setAccessible(true);
            InputHandler inputHandler = (InputHandler) inputHandlerField.get(null);
            List<IShowsRecipeFocuses> showsRecipeFocuses = ObfuscationReflectionHelper.getPrivateValue(InputHandler.class, inputHandler, "showsRecipeFocuses");

            showsRecipeFocuses.add(new MultiblockInfoRecipeFocusShower());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerRecipeMapCatalyst(IModRegistry registry, RecipeMap<?> recipeMap, MetaTileEntity metaTileEntity) {
        registry.addRecipeCatalyst(metaTileEntity.getStackForm(), GTValues.MODID + ":" + recipeMap.unlocalizedName);
        if (recipeMap instanceof RecipeMapFurnace) {
            registry.addRecipeCatalyst(metaTileEntity.getStackForm(), VanillaRecipeCategoryUid.SMELTING);
        }
        if (recipeMap.getSmallRecipeMap() != null) {
            registry.addRecipeCatalyst(metaTileEntity.getStackForm(), GTValues.MODID + ":" + recipeMap.getSmallRecipeMap().unlocalizedName);
        }
    }
}
