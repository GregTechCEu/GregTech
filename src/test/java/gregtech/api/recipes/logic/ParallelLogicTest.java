package gregtech.api.recipes.logic;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTHashMaps;
import gregtech.api.util.OverlayedFluidHandler;
import gregtech.api.util.OverlayedItemHandler;
import gregtech.common.MetaFluids;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParallelLogicTest {

    MetaTileEntityItemBus importItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, false);
    MetaTileEntityItemBus exportItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, true);
    MetaTileEntityFluidHatch importFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.lv"), 1, false);
    MetaTileEntityFluidHatch exportFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.lv"), 1, true);

    @BeforeClass
    public static void bootStrap() {
        Bootstrap.register();
        Materials.register();
        GregTechAPI.MATERIAL_REGISTRY.flush();
        MetaFluids.init();
    }

    private static ResourceLocation gregtechId(String name) {
        return new ResourceLocation(GTValues.MODID, name);
    }

    @Test
    public void getMaxRecipeMultiplier_ItemLimitTest() {

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 3), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);

        int itemRatio = ParallelLogic.getMaxRecipeMultiplier(recipe, importItemBus.getImportItems(), importFluidBus.getImportFluids(), parallelLimit);

        assertEquals(3, itemRatio);

    }

    @Test
    public void getMaxRecipeMultiplier_FluidLimitTest() {

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(4000))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);

        int itemRatio = ParallelLogic.getMaxRecipeMultiplier(recipe, importItemBus.getImportItems(), importFluidBus.getImportFluids(), parallelLimit);

        assertEquals(2, itemRatio);

    }

    @Test
    public void getMaxRecipeMultiplier_LimitFailureTest() {

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(1000))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.STONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Naphtha.getFluid(8000), true);

        int itemRatio = ParallelLogic.getMaxRecipeMultiplier(recipe, importItemBus.getImportItems(), importFluidBus.getImportFluids(), parallelLimit);

        assertEquals(0, itemRatio);

    }

    @Test
    public void getMaxRecipeMultiplier_ItemFailureTest() {

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.STONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);

        int itemRatio = ParallelLogic.getMaxRecipeMultiplier(recipe, importItemBus.getImportItems(), importFluidBus.getImportFluids(), parallelLimit);

        assertEquals(0, itemRatio);

    }

    @Test
    public void getMaxRecipeMultiplier_FluidFailureTest() {

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Naphtha.getFluid(8000), true);

        int itemRatio = ParallelLogic.getMaxRecipeMultiplier(recipe, importItemBus.getImportItems(), importFluidBus.getImportFluids(), parallelLimit);

        assertEquals(0, itemRatio);

    }

    @Test
    public void limitParallelByItems_MaxParallelTest() {

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);

        int itemRatio = ParallelLogic.limitParallelByItems(recipe, new OverlayedItemHandler(exportItemBus.getExportItems()), parallelLimit);

        assertEquals(4, itemRatio);

    }

    @Test
    public void limitParallelByItems_LessThanMaxParallelsTest() {

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);
        exportItemBus.getExportItems().insertItem(0, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(1, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(2, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(3, new ItemStack(Blocks.STONE, 62), false);


        int itemRatio = ParallelLogic.limitParallelByItems(recipe, new OverlayedItemHandler(exportItemBus.getExportItems()), parallelLimit);

        assertEquals(2, itemRatio);

    }

    @Test
    public void limitParallelByItems_SplitAcrossStacksTest() {

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);
        exportItemBus.getExportItems().insertItem(0, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(1, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(2, new ItemStack(Blocks.STONE, 62), false);
        exportItemBus.getExportItems().insertItem(3, new ItemStack(Blocks.STONE, 62), false);


        int itemRatio = ParallelLogic.limitParallelByItems(recipe, new OverlayedItemHandler(exportItemBus.getExportItems()), parallelLimit);

        assertEquals(4, itemRatio);

    }

    @Test
    public void limitParallelByItems_ItemOutputFullTest() {

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);

        // Fill the export bus
        exportItemBus.getExportItems().insertItem(0, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(1, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(2, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(3, new ItemStack(Blocks.BONE_BLOCK), false);

        int itemRatio = ParallelLogic.limitParallelByItems(recipe, new OverlayedItemHandler(exportItemBus.getExportItems()), parallelLimit);

        assertEquals(0, itemRatio);

    }

    @Test
    public void limitParallelByFluids_MaxParallelTest() {

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidOutputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        int itemRatio = ParallelLogic.limitParallelByFluids(recipe, new OverlayedFluidHandler(exportFluidBus.getExportFluids()), parallelLimit);

        assertEquals(4, itemRatio);

    }

    @Test
    public void limitParallelByFluids_PartialParallelsTest() {

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidOutputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        exportFluidBus.getExportFluids().fill(Materials.Acetone.getFluid(15800), true);

        int itemRatio = ParallelLogic.limitParallelByFluids(recipe, new OverlayedFluidHandler(exportFluidBus.getExportFluids()), parallelLimit);

        assertEquals(2, itemRatio);

    }

    @Test
    public void limitParallelByFluids_FluidOutputFullTest() {

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidOutputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        exportFluidBus.getExportFluids().fill(Materials.Acetone.getFluid(16000), true);

        int itemRatio = ParallelLogic.limitParallelByFluids(recipe, new OverlayedFluidHandler(exportFluidBus.getExportFluids()), parallelLimit);

        assertEquals(0, itemRatio);

    }

    @Test
    public void getMaxRatioItem_SameNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .notConsumable(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 5), false);

        int itemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
                recipe, parallelLimit);

        assertEquals(4, itemRatio);

    }

    @Test
    public void getMaxRatioItem_DifferentNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .notConsumable(new ItemStack(Blocks.STONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 4), false);
        importItemBus.getImportItems().insertItem(1, new ItemStack(Blocks.STONE, 1), false);


        int itemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
                recipe, parallelLimit);

        assertEquals(4, itemRatio);

    }
}
