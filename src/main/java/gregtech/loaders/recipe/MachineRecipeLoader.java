package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.CokeOvenRecipeBuilder;
import gregtech.api.recipes.builders.PBFRecipeBuilder;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.IMaterial;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.IngotMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockConcrete.ConcreteVariant;
import gregtech.common.blocks.BlockGranite.GraniteVariant;
import gregtech.common.blocks.BlockMachineCasing.MachineCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockMineral.MineralVariant;
import gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType;
import gregtech.common.blocks.BlockTransparentCasing;
import gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneBlock;
import gregtech.common.blocks.StoneBlock.ChiselingVariant;
import gregtech.common.blocks.wood.BlockGregLog.LogVariant;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.loaders.recipe.chemistry.AssemblerRecipeLoader;
import gregtech.loaders.recipe.chemistry.ChemistryRecipes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import static gregtech.api.GTValues.L;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.api.util.DyeUtil.getOrdictColorName;
import static gregtech.common.items.MetaItems.*;

public class MachineRecipeLoader {

    private MachineRecipeLoader() {
    }

    public static void init() {
        ChemistryRecipes.init();
        FuelRecipes.registerFuels();
        AssemblyLineLoader.init();
        AssemblerRecipeLoader.init();
        ComponentRecipes.register();
        MiscRecipeLoader.init();

        CircuitRecipes.init();
        registerCutterRecipes();
        registerDecompositionRecipes();
        registerBlastFurnaceRecipes();
        registerAssemblerRecipes();
        registerAlloyRecipes();
        registerBendingCompressingRecipes();
        registerCokeOvenRecipes();
        registerFluidRecipes();
        registerMixingCrystallizationRecipes();
        registerPrimitiveBlastFurnaceRecipes();
        registerRecyclingRecipes();
        registerStoneBricksRecipes();
        registerOrganicRecyclingRecipes();
    }

    private static void registerBendingCompressingRecipes() {
        COMPRESSOR_RECIPES.recipeBuilder().inputs(new ItemStack(Blocks.ICE, 2, GTValues.W)).outputs(new ItemStack(Blocks.PACKED_ICE)).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().input(OrePrefix.dust, Materials.Ice, 1).outputs(new ItemStack(Blocks.ICE)).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.WHEAT, 9))
                .inputs(new CountableIngredient(new IntCircuitIngredient(9), 0))
                .outputs(new ItemStack(Blocks.HAY_BLOCK))
                .duration(200).EUt(2)
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
            .input(OrePrefix.dust, Materials.Fireclay)
            .outputs(MetaItems.COMPRESSED_FIRECLAY.getStackForm())
            .duration(100).EUt(2)
            .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder()
            .duration(100).EUt(16)
            .notConsumable(MetaItems.SHAPE_MOLD_CREDIT.getStackForm())
            .input(OrePrefix.plate, Materials.Cupronickel, 1)
            .outputs(MetaItems.CREDIT_CUPRONICKEL.getStackForm(4))
            .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder()
            .duration(100).EUt(16)
            .notConsumable(MetaItems.SHAPE_MOLD_CREDIT.getStackForm())
            .input(OrePrefix.plate, Materials.Brass, 1)
            .outputs(MetaItems.COIN_DOGE.getStackForm(4))
            .buildAndRegister();

        for (MetaItem<?>.MetaValueItem shapeMold : SHAPE_MOLDS) {
            FORMING_PRESS_RECIPES.recipeBuilder()
                .duration(120).EUt(22)
                .notConsumable(shapeMold.getStackForm())
                .inputs(MetaItems.SHAPE_EMPTY.getStackForm())
                .outputs(shapeMold.getStackForm())
                .buildAndRegister();
        }

        for (MetaItem<?>.MetaValueItem shapeExtruder : SHAPE_EXTRUDERS) {
            FORMING_PRESS_RECIPES.recipeBuilder()
                .duration(120).EUt(22)
                .notConsumable(shapeExtruder.getStackForm())
                .inputs(MetaItems.SHAPE_EMPTY.getStackForm())
                .outputs(shapeExtruder.getStackForm())
                .buildAndRegister();
        }

        BENDER_RECIPES.recipeBuilder()
            .circuitMeta(4)
            .input(OrePrefix.plate, Materials.Steel, 4)
            .outputs(MetaItems.SHAPE_EMPTY.getStackForm())
            .duration(180).EUt(12)
            .buildAndRegister();

        BENDER_RECIPES.recipeBuilder()
            .circuitMeta(1)
            .input(OrePrefix.plate, Materials.Iron, 12)
            .outputs(new ItemStack(Items.BUCKET, 4))
            .duration(800).EUt(4)
            .buildAndRegister();

        BENDER_RECIPES.recipeBuilder()
            .circuitMeta(1)
            .input(OrePrefix.plate, Materials.WroughtIron, 12)
            .outputs(new ItemStack(Items.BUCKET, 4))
            .duration(800).EUt(4)
            .buildAndRegister();

        BENDER_RECIPES.recipeBuilder()
            .circuitMeta(12)
            .input(OrePrefix.plate, Materials.Iron, 2)
            .outputs(MetaItems.FLUID_CELL.getStackForm())
            .duration(200).EUt(30)
            .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder()
            .input(OrePrefix.plate, Materials.Iron, 2)
            .notConsumable(MetaItems.SHAPE_EXTRUDER_CELL)
            .outputs(MetaItems.FLUID_CELL.getStackForm())
            .duration(200).EUt(30)
            .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
            .input(OrePrefix.dust, Materials.NetherQuartz)
            .output(OrePrefix.plate, Materials.NetherQuartz)
            .duration(400).EUt(2).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
            .input(OrePrefix.dust, Materials.CertusQuartz)
            .output(OrePrefix.plate, Materials.CertusQuartz)
            .duration(400).EUt(2).buildAndRegister();
    }

    private static void registerPrimitiveBlastFurnaceRecipes() {
        PBFRecipeBuilder.start().input(ingot, Iron).output(ingot, Steel).duration(1500).fuelAmount(2).buildAndRegister();
        PBFRecipeBuilder.start().input(block, Iron).output(block, Steel).duration(13500).fuelAmount(18).buildAndRegister();
        PBFRecipeBuilder.start().input(ingot, WroughtIron).output(ingot, Steel).duration(600).fuelAmount(2).buildAndRegister();
        PBFRecipeBuilder.start().input(block, WroughtIron).output(block, Steel).duration(5600).fuelAmount(18).buildAndRegister();
    }

    private static void registerCokeOvenRecipes() {
        CokeOvenRecipeBuilder.start().input(OrePrefix.log, Materials.Wood).output(OreDictUnifier.get(OrePrefix.gem, Materials.Charcoal)).fluidOutput(Materials.Creosote.getFluid(250)).duration(900).buildAndRegister();
        CokeOvenRecipeBuilder.start().input(OrePrefix.gem, Materials.Coal).output(OreDictUnifier.get(OrePrefix.gem, Materials.Coke)).fluidOutput(Materials.Creosote.getFluid(500)).duration(900).buildAndRegister();
        CokeOvenRecipeBuilder.start().input(OrePrefix.block, Materials.Coal).output(OreDictUnifier.get(OrePrefix.block, Materials.Coke)).fluidOutput(Materials.Creosote.getFluid(4500)).duration(8100).buildAndRegister();
    }

    private static void registerStoneBricksRecipes() {
        //decorative blocks: normal variant -> brick variant
        registerBrickRecipe(MetaBlocks.CONCRETE, ConcreteVariant.LIGHT_CONCRETE, ConcreteVariant.LIGHT_BRICKS);
        registerBrickRecipe(MetaBlocks.CONCRETE, ConcreteVariant.DARK_CONCRETE, ConcreteVariant.DARK_BRICKS);
        registerBrickRecipe(MetaBlocks.GRANITE, GraniteVariant.BLACK_GRANITE, GraniteVariant.BLACK_GRANITE_BRICKS);
        registerBrickRecipe(MetaBlocks.GRANITE, GraniteVariant.RED_GRANITE, GraniteVariant.RED_GRANITE_BRICKS);
        registerBrickRecipe(MetaBlocks.MINERAL, MineralVariant.MARBLE, MineralVariant.MARBLE_BRICKS);
        registerBrickRecipe(MetaBlocks.MINERAL, MineralVariant.BASALT, MineralVariant.BASALT_BRICKS);

        //decorative blocks: normal chiseling -> different chiseling
        registerChiselingRecipes(MetaBlocks.CONCRETE);
        registerChiselingRecipes(MetaBlocks.GRANITE);
        registerChiselingRecipes(MetaBlocks.MINERAL);
    }

    private static void registerMixingCrystallizationRecipes() {
        RecipeMaps.MIXER_RECIPES.recipeBuilder()
            .input(OrePrefix.dust, Materials.Stone, 1)
            .fluidInputs(Materials.Lubricant.getFluid(20), ModHandler.getWater(4980))
            .fluidOutputs(Materials.DrillingFluid.getFluid(5000))
            .duration(64).EUt(16)
            .buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
            .input(OrePrefix.dust, Materials.Clay, 1)
            .input(OrePrefix.dust, Materials.Stone, 3)
            .fluidInputs(Materials.Water.getFluid(500))
            .fluidOutputs(Materials.Concrete.getFluid(576))
            .duration(20).EUt(16)
            .buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
            .inputs(MetaBlocks.CONCRETE.getItemVariant(ConcreteVariant.LIGHT_CONCRETE, ChiselingVariant.NORMAL))
            .fluidInputs(Materials.Water.getFluid(144))
            .outputs(MetaBlocks.CONCRETE.getItemVariant(ConcreteVariant.DARK_CONCRETE, ChiselingVariant.NORMAL))
            .duration(12).EUt(4)
            .buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
            .duration(64).EUt(16)
            .fluidInputs(Materials.Water.getFluid(1000))
            .input("sand", 2)
            .input(OrePrefix.dust, Materials.Stone, 6)
            .input(OrePrefix.dust, Materials.Flint)
            .fluidOutputs(Materials.ConstructionFoam.getFluid(1000))
            .buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
            .duration(1200).EUt(16)
            .input(OrePrefix.dust, Materials.Ruby, 9)
            .input(OrePrefix.dust, Materials.Redstone, 9)
            .outputs(MetaItems.ENERGIUM_DUST.getStackForm(9))
            .buildAndRegister();

        RecipeMaps.AUTOCLAVE_RECIPES.recipeBuilder().inputs(MetaItems.ENERGIUM_DUST.getStackForm(9))
            .fluidInputs(Materials.Water.getFluid(1800))
            .outputs(MetaItems.ENERGY_CRYSTAL.getStackForm())
            .duration(2000).EUt(120)
            .buildAndRegister();

        RecipeMaps.AUTOCLAVE_RECIPES.recipeBuilder()
            .inputs(MetaItems.ENERGIUM_DUST.getStackForm(9))
            .fluidInputs(ModHandler.getDistilledWater(1800))
            .outputs(MetaItems.ENERGY_CRYSTAL.getStackForm())
            .duration(1500).EUt(120)
            .buildAndRegister();

        RecipeMaps.AUTOCLAVE_RECIPES.recipeBuilder()
            .input(OrePrefix.dust, Materials.SiliconDioxide)
            .fluidInputs(ModHandler.getDistilledWater(200))
            .chancedOutput(OreDictUnifier.get(OrePrefix.gem, Materials.Quartzite), 1000, 1000)
            .duration(1500).EUt(24).buildAndRegister();

        RecipeMaps.AUTOCLAVE_RECIPES.recipeBuilder()
            .input(OrePrefix.dust, Materials.NetherStar)
            .fluidInputs(Materials.UUMatter.getFluid(576))
            .chancedOutput(new ItemStack(Items.NETHER_STAR), 3333, 3333)
            .duration(72000).EUt(480).buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
            .input(OrePrefix.crushedPurified, Materials.Sphalerite)
            .input(OrePrefix.crushedPurified, Materials.Galena)
            .fluidInputs(Materials.SulfuricAcid.getFluid(4000))
            .fluidOutputs(Materials.IndiumConcentrate.getFluid(1000))
            .duration(60).EUt(150).buildAndRegister();

    }

    private static void registerOrganicRecyclingRecipes() {


    }

    private static final MaterialStack[][] alloySmelterList = {
        {new MaterialStack(Materials.Copper, 3L), new MaterialStack(Materials.Tin, 1), new MaterialStack(Materials.Bronze, 4L)},
        {new MaterialStack(Materials.Copper, 3L), new MaterialStack(Materials.Zinc, 1), new MaterialStack(Materials.Brass, 4L)},
        {new MaterialStack(Materials.Copper, 1), new MaterialStack(Materials.Nickel, 1), new MaterialStack(Materials.Cupronickel, 2L)},
        {new MaterialStack(Materials.Copper, 1), new MaterialStack(Materials.Redstone, 4L), new MaterialStack(Materials.RedAlloy, 1)},
        {new MaterialStack(Materials.AnnealedCopper, 3L), new MaterialStack(Materials.Tin, 1), new MaterialStack(Materials.Bronze, 4L)},
        {new MaterialStack(Materials.AnnealedCopper, 3L), new MaterialStack(Materials.Zinc, 1), new MaterialStack(Materials.Brass, 4L)},
        {new MaterialStack(Materials.AnnealedCopper, 1), new MaterialStack(Materials.Nickel, 1), new MaterialStack(Materials.Cupronickel, 2L)},
        {new MaterialStack(Materials.AnnealedCopper, 1), new MaterialStack(Materials.Redstone, 4L), new MaterialStack(Materials.RedAlloy, 1)},
        {new MaterialStack(Materials.Iron, 1), new MaterialStack(Materials.Tin, 1), new MaterialStack(Materials.TinAlloy, 2L)},
        {new MaterialStack(Materials.WroughtIron, 1), new MaterialStack(Materials.Tin, 1), new MaterialStack(Materials.TinAlloy, 2L)},
        {new MaterialStack(Materials.Iron, 2L), new MaterialStack(Materials.Nickel, 1), new MaterialStack(Materials.Invar, 3L)},
        {new MaterialStack(Materials.WroughtIron, 2L), new MaterialStack(Materials.Nickel, 1), new MaterialStack(Materials.Invar, 3L)},
        {new MaterialStack(Materials.Tin, 9L), new MaterialStack(Materials.Antimony, 1), new MaterialStack(Materials.SolderingAlloy, 10L)},
        {new MaterialStack(Materials.Lead, 4L), new MaterialStack(Materials.Antimony, 1), new MaterialStack(Materials.BatteryAlloy, 5L)},
        {new MaterialStack(Materials.Gold, 1), new MaterialStack(Materials.Silver, 1), new MaterialStack(Materials.Electrum, 2L)},
        {new MaterialStack(Materials.Magnesium, 1), new MaterialStack(Materials.Aluminium, 2L), new MaterialStack(Materials.Magnalium, 3L)}};

    private static void registerAlloyRecipes() {
        for (MaterialStack[] stack : alloySmelterList) {
            if (stack[0].material instanceof IngotMaterial) {
                RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                    .duration((int) stack[2].amount * 50).EUt(16)
                    .input(OrePrefix.ingot, stack[0].material, (int) stack[0].amount)
                    .input(OrePrefix.dust, stack[1].material, (int) stack[1].amount)
                    .outputs(OreDictUnifier.get(OrePrefix.ingot, stack[2].material, (int) stack[2].amount))
                    .buildAndRegister();
            }
            if (stack[1].material instanceof IngotMaterial) {
                RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                    .duration((int) stack[2].amount * 50).EUt(16)
                    .input(OrePrefix.dust, stack[0].material, (int) stack[0].amount)
                    .input(OrePrefix.ingot, stack[1].material, (int) stack[1].amount)
                    .outputs(OreDictUnifier.get(OrePrefix.ingot, stack[2].material, (int) stack[2].amount))
                    .buildAndRegister();
            }
            if (stack[0].material instanceof IngotMaterial && stack[1].material instanceof IngotMaterial) {
                RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                    .duration((int) stack[2].amount * 50).EUt(16)
                    .input(OrePrefix.ingot, stack[0].material, (int) stack[0].amount)
                    .input(OrePrefix.ingot, stack[1].material, (int) stack[1].amount)
                    .outputs(OreDictUnifier.get(OrePrefix.ingot, stack[2].material, (int) stack[2].amount))
                    .buildAndRegister();
            }
            RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                .duration((int) stack[2].amount * 50).EUt(16)
                .input(OrePrefix.dust, stack[0].material, (int) stack[0].amount)
                .input(OrePrefix.dust, stack[1].material, (int) stack[1].amount)
                .outputs(OreDictUnifier.get(OrePrefix.ingot, stack[2].material, (int) stack[2].amount))
                .buildAndRegister();
        }

        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(800).EUt(8).input(OrePrefix.dust, Materials.Boron, 1).input(OrePrefix.dust, Materials.Glass, 7).output(OrePrefix.dust, Materials.BorosilicateGlass, 8).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(100).EUt(8).input(OrePrefix.dust, Materials.Indium, 1).input(OrePrefix.dust, Materials.Gallium).input(OrePrefix.dust, Materials.Phosphorus, 1).output(OrePrefix.dust, Materials.IndiumGalliumPhosphide, 3).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(600).EUt(8).input(OrePrefix.dust, Materials.Nickel, 1).input(OrePrefix.dust, Materials.Zinc).input(OrePrefix.dust, Materials.Iron, 4).notConsumable(new IntCircuitIngredient(2)).output(OrePrefix.dust, Materials.FerriteMixture, 6).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(100).EUt(8).input(OrePrefix.dust, Materials.EnderPearl, 1).input(OrePrefix.dust, Materials.Blaze).output(OrePrefix.dust, Materials.EnderEye).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(200).EUt(8).input(OrePrefix.dust, Materials.Gold, 1).input(OrePrefix.dust, Materials.Silver).output(OrePrefix.dust, Materials.Electrum, 2).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(300).EUt(8).input(OrePrefix.dust, Materials.Iron, 2).input(OrePrefix.dust, Materials.Nickel).notConsumable(new IntCircuitIngredient(1)).output(OrePrefix.dust, Materials.Invar, 3).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(900).EUt(8).input(OrePrefix.dust, Materials.Iron, 4).input(OrePrefix.dust, Materials.Invar, 3).input(OrePrefix.dust, Materials.Manganese).input(OrePrefix.dust, Materials.Chrome, 1).output(OrePrefix.dust, Materials.StainlessSteel, 9).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(300).EUt(8).input(OrePrefix.dust, Materials.Iron, 1).input(OrePrefix.dust, Materials.Aluminium).input(OrePrefix.dust, Materials.Chrome).output(OrePrefix.dust, Materials.Kanthal, 3).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(400).EUt(8).input(OrePrefix.dust, Materials.Copper, 3).input(OrePrefix.dust, Materials.Zinc).output(OrePrefix.dust, Materials.Brass, 4).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(400).EUt(8).input(OrePrefix.dust, Materials.Copper, 3).input(OrePrefix.dust, Materials.Tin).output(OrePrefix.dust, Materials.Bronze, 4).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(200).EUt(8).input(OrePrefix.dust, Materials.Copper, 1).input(OrePrefix.dust, Materials.Nickel).output(OrePrefix.dust, Materials.Cupronickel, 2).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(500).EUt(8).input(OrePrefix.dust, Materials.Copper, 1).input(OrePrefix.dust, Materials.Gold, 4).output(OrePrefix.dust, Materials.RoseGold, 5).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(500).EUt(8).input(OrePrefix.dust, Materials.Copper, 1).input(OrePrefix.dust, Materials.Silver, 4).output(OrePrefix.dust, Materials.SterlingSilver, 5).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(500).EUt(8).input(OrePrefix.dust, Materials.Copper, 3).input(OrePrefix.dust, Materials.Electrum, 2).output(OrePrefix.dust, Materials.BlackBronze, 5).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(500).EUt(8).input(OrePrefix.dust, Materials.Bismuth, 1).input(OrePrefix.dust, Materials.Brass, 4).output(OrePrefix.dust, Materials.BismuthBronze, 5).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(500).EUt(8).input(OrePrefix.dust, Materials.BlackBronze, 1).input(OrePrefix.dust, Materials.Nickel).input(OrePrefix.dust, Materials.Steel, 3).output(OrePrefix.dust, Materials.BlackSteel, 5).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(800).EUt(8).input(OrePrefix.dust, Materials.SterlingSilver, 1).input(OrePrefix.dust, Materials.BismuthBronze).input(OrePrefix.dust, Materials.BlackSteel, 4).input(OrePrefix.dust, Materials.Steel, 2).output(OrePrefix.dust, Materials.RedSteel, 8).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(800).EUt(8).input(OrePrefix.dust, Materials.RoseGold, 1).input(OrePrefix.dust, Materials.Brass).input(OrePrefix.dust, Materials.BlackSteel, 4).input(OrePrefix.dust, Materials.Steel, 2).output(OrePrefix.dust, Materials.BlueSteel, 8).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(900).EUt(8).input(OrePrefix.dust, Materials.Cobalt, 5).input(OrePrefix.dust, Materials.Chrome, 2).input(OrePrefix.dust, Materials.Nickel).input(OrePrefix.dust, Materials.Molybdenum).output(OrePrefix.dust, Materials.Ultimet, 9).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(900).EUt(8).input(OrePrefix.dust, Materials.Brass, 7).input(OrePrefix.dust, Materials.Aluminium).input(OrePrefix.dust, Materials.Cobalt).output(OrePrefix.dust, Materials.CobaltBrass, 9).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(400).EUt(8).input(OrePrefix.dust, Materials.Saltpeter, 2).input(OrePrefix.dust, Materials.Sulfur).input(OrePrefix.dust, Materials.Coal).output(OrePrefix.dust, Materials.Gunpowder, 4).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(300).EUt(8).input(OrePrefix.dust, Materials.Saltpeter, 2).input(OrePrefix.dust, Materials.Sulfur).input(OrePrefix.dust, Materials.Charcoal).output(OrePrefix.dust, Materials.Gunpowder, 3).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(400).EUt(8).input(OrePrefix.dust, Materials.Electrum, 1).input(OrePrefix.dust, Materials.NaquadahAlloy).input(OrePrefix.dust, Materials.BlueSteel).input(OrePrefix.dust, Materials.RedSteel).output(OrePrefix.dust, Materials.FluxedElectrum, 4).buildAndRegister();
        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(300).EUt(8).input(OrePrefix.dust, Materials.Americium, 2).input(OrePrefix.dust, Materials.Titanium).output(OrePrefix.dust, Materials.DiamericiumTitanium, 3).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().inputs(MetaItems.INGOT_MIXED_METAL.getStackForm()).outputs(MetaItems.ADVANCED_ALLOY_PLATE.getStackForm()).duration(300).EUt(2).buildAndRegister();
        BENDER_RECIPES.recipeBuilder().inputs(MetaItems.INGOT_MIXED_METAL.getStackForm()).circuitMeta(1).outputs(MetaItems.ADVANCED_ALLOY_PLATE.getStackForm()).duration(100).EUt(8).buildAndRegister();
        IMPLOSION_RECIPES.recipeBuilder().inputs(MetaItems.INGOT_IRIDIUM_ALLOY.getStackForm()).outputs(MetaItems.PLATE_IRIDIUM_ALLOY.getStackForm()).output(OrePrefix.dustTiny, Materials.DarkAsh, 4).explosivesAmount(8).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().inputs(MetaItems.CARBON_FIBERS.getStackForm(2)).outputs(MetaItems.CARBON_MESH.getStackForm()).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().inputs(MetaItems.CARBON_MESH.getStackForm()).outputs(MetaItems.CARBON_PLATE.getStackForm()).buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(400).EUt(4).input(OrePrefix.dust, Materials.Glass, 3).inputs(MetaItems.ADVANCED_ALLOY_PLATE.getStackForm()).outputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS, 4)).buildAndRegister();
        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(400).EUt(4).inputs(new ItemStack(Blocks.GLASS)).inputs(MetaItems.ADVANCED_ALLOY_PLATE.getStackForm()).outputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockTransparentCasing.CasingType.REINFORCED_GLASS, 4)).buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(10).EUt(8).input(OrePrefix.ingot, Materials.Rubber, 2).notConsumable(MetaItems.SHAPE_MOLD_PLATE).output(OrePrefix.plate, Materials.Rubber).buildAndRegister();
        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(100).EUt(1).input(OrePrefix.dust, Materials.Sulfur).input(OrePrefix.dust, Materials.RawRubber, 3).output(OrePrefix.ingot, Materials.Rubber).buildAndRegister();
    }

    private static void registerAssemblerRecipes() {
        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            CANNER_RECIPES.recipeBuilder()
                .inputs(MetaItems.SPRAY_EMPTY.getStackForm())
                .input(getOrdictColorName(dyeColor), 1)
                .outputs(MetaItems.SPRAY_CAN_DYES[dyeColor.getMetadata()].getStackForm())
                .EUt(8).duration(200)
                .buildAndRegister();
        }

        for (IngotMaterial cableMaterial : new IngotMaterial[]{Materials.YttriumBariumCuprate, Materials.NiobiumTitanium, Materials.VanadiumGallium, Materials.Naquadah}) {
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.wireGtSingle, cableMaterial, 3)
                .input(OrePrefix.pipeTiny, Materials.TungstenSteel, 2)
                .inputs(MetaItems.ELECTRIC_PUMP_LV.getStackForm(2))
                .fluidInputs(Materials.Nitrogen.getFluid(2000))
                .outputs(OreDictUnifier.get(OrePrefix.wireGtSingle, Tier.Superconductor, 3))
                .duration(20).EUt(512)
                .buildAndRegister();
        }

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
            .duration(4096).EUt(4096)
            .inputs(MetaItems.ENERGY_LAPOTRONIC_ORB2.getStackForm(8))
            .input(OrePrefix.plate, Materials.Darmstadtium, 16)
            .outputs(MetaItems.ZPM2.getStackForm())
            .buildAndRegister();

        Material[] coverMaterials = new Material[]{Materials.Iron, Materials.WroughtIron, Materials.Aluminium};

        for (Material material : coverMaterials) {
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(Items.IRON_DOOR))
                    .input(OrePrefix.plate, material, 2)
                    .outputs(MetaItems.COVER_SHUTTER.getStackForm(2))
                    .EUt(16).duration(800)
                    .buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(Blocks.LEVER))
                    .input(OrePrefix.plate, material)
                    .fluidInputs(Materials.Tin.getFluid(L))
                    .outputs(MetaItems.COVER_MACHINE_CONTROLLER.getStackForm(1))
                    .EUt(16).duration(200)
                    .buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(Blocks.LEVER))
                    .input(OrePrefix.plate, material)
                    .fluidInputs(Materials.SolderingAlloy.getFluid(L / 2))
                    .outputs(MetaItems.COVER_MACHINE_CONTROLLER.getStackForm(1))
                    .EUt(16).duration(200)
                    .buildAndRegister();
        }

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(480).EUt(240).input(OrePrefix.dust, Materials.Graphite, 8).input(OrePrefix.foil, Materials.Silicon, 1).fluidInputs(Materials.Glue.getFluid(250)).outputs(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Graphene, 1)).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(1).inputs(new ItemStack(Items.COAL, 1, GTValues.W)).input(OrePrefix.stick, Materials.Wood, 1).outputs(new ItemStack(Blocks.TORCH, 4)).duration(400).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(OrePrefix.plate, Materials.Gold, 2).outputs(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1)).circuitMeta(2).duration(200).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(OrePrefix.plate, Materials.Iron, 2).outputs(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1)).circuitMeta(2).duration(200).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(OrePrefix.plate, Materials.Iron, 6).outputs(new ItemStack(Items.IRON_DOOR, 3)).circuitMeta(6).duration(600).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(OrePrefix.plate, Materials.Iron, 7).outputs(new ItemStack(Items.CAULDRON, 1)).circuitMeta(7).duration(700).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(OrePrefix.stick, Materials.Iron, 3).outputs(new ItemStack(Blocks.IRON_BARS, 4)).circuitMeta(3).duration(300).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(OrePrefix.plate, Materials.WroughtIron, 2).outputs(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1)).circuitMeta(2).duration(200).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(OrePrefix.plate, Materials.WroughtIron, 6).outputs(new ItemStack(Items.IRON_DOOR, 3)).circuitMeta(6).duration(600).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(OrePrefix.plate, Materials.WroughtIron, 7).outputs(new ItemStack(Items.CAULDRON, 1)).circuitMeta(7).duration(700).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(OrePrefix.stick, Materials.WroughtIron, 3).outputs(new ItemStack(Blocks.IRON_BARS, 4)).circuitMeta(3).duration(300).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(OrePrefix.stick, Materials.Wood, 3).outputs(new ItemStack(Blocks.OAK_FENCE, 1)).circuitMeta(3).duration(300).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(OrePrefix.stick, Materials.Wood, 2).input(OrePrefix.ring, Materials.Iron, 2).outputs(new ItemStack(Blocks.TRIPWIRE_HOOK, 1)).duration(400).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(OrePrefix.stick, Materials.Wood, 2).input(OrePrefix.ring, Materials.WroughtIron, 2).outputs(new ItemStack(Blocks.TRIPWIRE_HOOK, 1)).duration(400).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).inputs(new ItemStack(Items.STRING, 3, GTValues.W)).input(OrePrefix.stick, Materials.Wood, 3).outputs(new ItemStack(Items.BOW, 1)).duration(400).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).inputs(new ItemStack(Blocks.STONE)).outputs(new ItemStack(Blocks.STONEBRICK, 1, 0)).circuitMeta(4).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).inputs(new ItemStack(Blocks.SANDSTONE)).outputs(new ItemStack(Blocks.SANDSTONE, 1, 2)).circuitMeta(1).duration(50).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).inputs(new ItemStack(Blocks.SANDSTONE, 1, 1)).outputs(new ItemStack(Blocks.SANDSTONE, 1, 0)).circuitMeta(1).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(4).inputs(new ItemStack(Blocks.SANDSTONE, 1, 2)).outputs(new ItemStack(Blocks.SANDSTONE, 1, 0)).circuitMeta(1).duration(50).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.WroughtIron, 8).outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ULV)).circuitMeta(8).duration(25).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Steel, 8).outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LV)).circuitMeta(8).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Aluminium, 8).outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MV)).circuitMeta(8).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.StainlessSteel, 8).outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.HV)).circuitMeta(8).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Titanium, 8).outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.EV)).circuitMeta(8).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.TungstenSteel, 8).outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.IV)).circuitMeta(8).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Chrome, 8).outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LuV)).circuitMeta(8).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Iridium, 8).outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ZPM)).circuitMeta(8).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Osmium, 8).outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UV)).circuitMeta(8).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Darmstadtium, 8).outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UHV)).circuitMeta(8).duration(50).buildAndRegister();

        for (CoilType coilType : CoilType.values()) {
            if (coilType.getMaterial() != null) {
                ItemStack outputStack = MetaBlocks.WIRE_COIL.getItemVariant(coilType);
                ASSEMBLER_RECIPES.recipeBuilder()
                    .circuitMeta(8)
                    .input(wireGtDouble, coilType.getMaterial(), 8)
                    .outputs(outputStack)
                    .duration(50).EUt(16).buildAndRegister();
            }
        }

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Invar, 6).input(OrePrefix.frameGt, Materials.Invar, 1).outputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.INVAR_HEATPROOF, 2)).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Steel, 6).input(OrePrefix.frameGt, Materials.Steel, 1).outputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.STEEL_SOLID, 2)).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Aluminium, 6).input(OrePrefix.frameGt, Materials.Aluminium, 1).outputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.ALUMINIUM_FROSTPROOF, 2)).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.TungstenSteel, 6).input(OrePrefix.frameGt, Materials.TungstenSteel, 1).outputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.TUNGSTENSTEEL_ROBUST, 2)).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.StainlessSteel, 6).input(OrePrefix.frameGt, Materials.StainlessSteel, 1).outputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.STAINLESS_CLEAN, 2)).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Titanium, 6).input(OrePrefix.frameGt, Materials.Titanium, 1).outputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.TITANIUM_STABLE, 2)).duration(50).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.STEEL_SOLID)).fluidInputs(Materials.Polytetrafluoroethylene.getFluid(216)).notConsumable(new IntCircuitIngredient(6)).outputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.PTFE_INERT_CASING)).duration(50).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LuV)).input(OrePrefix.plate, Materials.TungstenSteel, 6).outputs(MetaBlocks.MULTIBLOCK_CASING.getItemVariant(MultiblockCasingType.FUSION_CASING)).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputs(MetaBlocks.MULTIBLOCK_CASING.getItemVariant(MultiblockCasingType.FUSION_CASING)).input(OrePrefix.plate, Materials.Americium, 6).outputs(MetaBlocks.MULTIBLOCK_CASING.getItemVariant(MultiblockCasingType.FUSION_CASING_MK2)).duration(50).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Magnalium, 6).input(OrePrefix.frameGt, Materials.BlueSteel, 1).outputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STEEL_TURBINE_CASING, 2)).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STEEL_TURBINE_CASING)).input(OrePrefix.plate, Materials.StainlessSteel, 6).outputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STAINLESS_TURBINE_CASING, 2)).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STEEL_TURBINE_CASING)).input(OrePrefix.plate, Materials.Titanium, 6).outputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.TITANIUM_TURBINE_CASING, 2)).duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STEEL_TURBINE_CASING)).input(OrePrefix.plate, Materials.TungstenSteel, 6).outputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.TUNGSTENSTEEL_TURBINE_CASING, 2)).duration(50).buildAndRegister();

        if (ConfigHolder.harderMachineHulls) {
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(25).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ULV)).input(OrePrefix.cableGtSingle, Materials.Lead, 2).fluidInputs(Materials.Polyethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[0].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LV)).input(OrePrefix.cableGtSingle, Materials.Tin, 2).fluidInputs(Materials.Polyethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[1].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MV)).input(OrePrefix.cableGtSingle, Materials.Copper, 2).fluidInputs(Materials.Polyethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[2].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MV)).input(OrePrefix.cableGtSingle, Materials.AnnealedCopper, 2).fluidInputs(Materials.Polyethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[2].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.HV)).input(OrePrefix.cableGtSingle, Materials.Gold, 2).fluidInputs(Materials.Polyethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[3].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.EV)).input(OrePrefix.cableGtSingle, Materials.Aluminium, 2).fluidInputs(Materials.Polyethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[4].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.IV)).input(OrePrefix.cableGtSingle, Materials.Tungsten, 2).fluidInputs(Polytetrafluoroethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[5].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LuV)).input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2).fluidInputs(Polytetrafluoroethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[6].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ZPM)).input(OrePrefix.cableGtSingle, Materials.Naquadah, 2).fluidInputs(Polybenzimidazole.getFluid(L * 2)).outputs(MetaTileEntities.HULL[7].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UV)).input(OrePrefix.wireGtQuadruple, Materials.NaquadahAlloy, 2).fluidInputs(Polybenzimidazole.getFluid(L * 2)).outputs(MetaTileEntities.HULL[8].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MAX)).input(OrePrefix.wireGtSingle, MarkerMaterials.Tier.Superconductor, 2).fluidInputs(Polybenzimidazole.getFluid(L * 2)).outputs(MetaTileEntities.HULL[14].getStackForm()).buildAndRegister();
        } else {
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(25).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ULV)).input(OrePrefix.cableGtSingle, Materials.Lead, 2).outputs(MetaTileEntities.HULL[0].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LV)).input(OrePrefix.cableGtSingle, Materials.Tin, 2).outputs(MetaTileEntities.HULL[1].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MV)).input(OrePrefix.cableGtSingle, Materials.Copper, 2).outputs(MetaTileEntities.HULL[2].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MV)).input(OrePrefix.cableGtSingle, Materials.AnnealedCopper, 2).outputs(MetaTileEntities.HULL[2].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.HV)).input(OrePrefix.cableGtSingle, Materials.Gold, 2).outputs(MetaTileEntities.HULL[3].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.EV)).input(OrePrefix.cableGtSingle, Materials.Aluminium, 2).outputs(MetaTileEntities.HULL[4].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.IV)).input(OrePrefix.cableGtSingle, Materials.Tungsten, 2).outputs(MetaTileEntities.HULL[5].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LuV)).input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2).outputs(MetaTileEntities.HULL[6].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ZPM)).input(OrePrefix.cableGtSingle, Materials.Naquadah, 2).outputs(MetaTileEntities.HULL[7].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UV)).input(OrePrefix.wireGtQuadruple, Materials.NaquadahAlloy, 2).outputs(MetaTileEntities.HULL[8].getStackForm()).buildAndRegister();
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16).inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MAX)).input(OrePrefix.wireGtSingle, MarkerMaterials.Tier.Superconductor, 2).outputs(MetaTileEntities.HULL[14].getStackForm()).buildAndRegister();
        }

        ASSEMBLER_RECIPES.recipeBuilder().duration(800).EUt(1).input(cableGtSingle, Tin).input(plate, BatteryAlloy).fluidInputs(Polyethylene.getFluid(144)).output(BATTERY_HULL_LV).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(1600).EUt(2).input(cableGtSingle, Copper, 2).input(plate, BatteryAlloy, 3).fluidInputs(Polyethylene.getFluid(432)).output(BATTERY_HULL_MV).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(1600).EUt(2).input(cableGtSingle, AnnealedCopper, 2).input(plate, BatteryAlloy, 3).fluidInputs(Polyethylene.getFluid(432)).output(BATTERY_HULL_MV).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().duration(3200).EUt(4).input(cableGtSingle, Gold, 4).input(plate, BatteryAlloy, 9).fluidInputs(Polyethylene.getFluid(1296)).output(BATTERY_HULL_HV).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(2).inputs(new ItemStack(Items.STRING, 4, GTValues.W), new ItemStack(Items.SLIME_BALL, 1, GTValues.W)).outputs(new ItemStack(Items.LEAD, 2)).duration(200).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().EUt(2).inputs(new ItemStack(Blocks.CHEST, 1, GTValues.W)).input(plate, Iron, 5).outputs(new ItemStack(Blocks.HOPPER)).duration(800).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(2).inputs(new ItemStack(Blocks.TRAPPED_CHEST, 1, GTValues.W)).input(plate, Iron, 5).outputs(new ItemStack(Blocks.HOPPER)).duration(800).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(2).inputs(new ItemStack(Blocks.CHEST, 1, GTValues.W)).input(plate, WroughtIron, 5).outputs(new ItemStack(Blocks.HOPPER)).duration(800).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(2).inputs(new ItemStack(Blocks.TRAPPED_CHEST, 1, GTValues.W)).input(plate, WroughtIron, 5).outputs(new ItemStack(Blocks.HOPPER)).duration(800).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(2).inputs(new ItemStack(Items.BLAZE_ROD)).input(gem, EnderPearl, 6).outputs(new ItemStack(Items.ENDER_EYE, 6, 0)).duration(2500).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(2).input(gear, CobaltBrass).input(dust, Diamond).output(COMPONENT_SAW_BLADE_DIAMOND).duration(1600).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(1).input(dust, Redstone, 4).input(dust, Glowstone, 4).outputs(new ItemStack(Blocks.REDSTONE_LAMP, 1)).duration(400).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(1).input(dust, Redstone).input(stick, Wood).outputs(new ItemStack(Blocks.REDSTONE_TORCH, 1)).duration(400).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(dust, Redstone).input(plate, Iron, 4).outputs(new ItemStack(Items.COMPASS, 1)).duration(400).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(dust, Redstone).input(plate, WroughtIron, 4).outputs(new ItemStack(Items.COMPASS, 1)).duration(400).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(4).input(dust, Redstone).input(plate, Gold, 4).outputs(new ItemStack(Items.CLOCK, 1)).duration(400).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(1).input(stick, Wood).input(dust, Sulfur).outputs(new ItemStack(Blocks.TORCH, 2)).duration(400).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(1).input(stick, Wood).input(dust, Phosphorus).outputs(new ItemStack(Blocks.TORCH, 6)).duration(400).buildAndRegister();

    }

    private static void registerBlastFurnaceRecipes() {
        BLAST_RECIPES.recipeBuilder().duration((int) Math.max(TungstenSteel.getAverageMass() / 80, 1) * TungstenSteel.blastFurnaceTemperature).EUt(480).input(ingot, Tungsten).input(ingot, Steel).outputs(OreDictUnifier.get(OrePrefix.ingotHot, Materials.TungstenSteel, 2), OreDictUnifier.get(OrePrefix.dustSmall, Materials.DarkAsh)).blastFurnaceTemp(TungstenSteel.blastFurnaceTemperature).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration((int) Math.max(TungstenCarbide.getAverageMass() / 40, 1) * TungstenCarbide.blastFurnaceTemperature).EUt(480).input(ingot, Tungsten).input(dust, Carbon).outputs(OreDictUnifier.get(OrePrefix.ingotHot, Materials.TungstenCarbide, 2), OreDictUnifier.get(OrePrefix.dustSmall, Materials.DarkAsh)).blastFurnaceTemp(TungstenCarbide.blastFurnaceTemperature).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration((int) Math.max(VanadiumGallium.getAverageMass() / 40, 1) * VanadiumGallium.blastFurnaceTemperature).EUt(480).input(ingot, Vanadium, 3).input(ingot, Gallium).outputs(OreDictUnifier.get(OrePrefix.ingotHot, Materials.VanadiumGallium, 4), OreDictUnifier.get(OrePrefix.dustSmall, Materials.DarkAsh, 2)).blastFurnaceTemp(VanadiumGallium.blastFurnaceTemperature).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration((int) Math.max(NiobiumTitanium.getAverageMass() / 80, 1) * NiobiumTitanium.blastFurnaceTemperature).EUt(480).input(ingot, Niobium).input(ingot, Titanium).outputs(OreDictUnifier.get(OrePrefix.ingotHot, Materials.NiobiumTitanium, 2), OreDictUnifier.get(OrePrefix.dustSmall, Materials.DarkAsh)).blastFurnaceTemp(NiobiumTitanium.blastFurnaceTemperature).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration((int) Math.max(Nichrome.getAverageMass() / 32, 1) * Nichrome.blastFurnaceTemperature).EUt(480).input(ingot, Nickel, 4).input(ingot, Chrome).outputs(OreDictUnifier.get(OrePrefix.ingotHot, Materials.Nichrome, 5), OreDictUnifier.get(OrePrefix.dustSmall, Materials.DarkAsh, 2)).blastFurnaceTemp(Nichrome.blastFurnaceTemperature).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(400).EUt(100).input(dust, Ruby).output(nugget, Aluminium, 3).output(dustTiny, DarkAsh).blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(320).EUt(100).input(gem, Ruby).output(nugget, Aluminium, 3).output(dustTiny, DarkAsh).blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(400).EUt(100).input(dust, GreenSapphire).output(nugget, Aluminium, 3).output(dustTiny, DarkAsh).blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(320).EUt(100).input(gem, GreenSapphire).output(nugget, Aluminium, 3).output(dustTiny, DarkAsh).blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(400).EUt(100).input(dust, Sapphire).output(nugget, Aluminium, 3).blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(320).EUt(100).input(gem, Sapphire).output(nugget, Aluminium, 3).blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(800).EUt(500).input(dust, Ilmenite, 5).outputs(OreDictUnifier.get(ingot, WroughtIron), OreDictUnifier.get(dust, Rutile, 3)).blastFurnaceTemp(1700).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(800).EUt(480).input(dust, Magnesium, 2).fluidInputs(TitaniumTetrachloride.getFluid(1000)).outputs(OreDictUnifier.get(OrePrefix.ingotHot, Materials.Titanium), OreDictUnifier.get(OrePrefix.dust, Materials.MagnesiumChloride, 6)).blastFurnaceTemp(Materials.Titanium.blastFurnaceTemperature + 200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(500).EUt(120).input(ingot, Iron).fluidInputs(Oxygen.getFluid(1000)).output(ingot, Steel).output(dustTiny, Ash).blastFurnaceTemp(1000).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(100).EUt(120).input(ingot, PigIron).fluidInputs(Oxygen.getFluid(1000)).output(ingot, Steel).output(dustTiny, Ash).blastFurnaceTemp(1000).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(100).EUt(120).input(ingot, WroughtIron).fluidInputs(Oxygen.getFluid(1000)).output(ingot, Steel).output(dustTiny, Ash).blastFurnaceTemp(1000).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(200).EUt(120).input(dust, Copper).fluidInputs(Oxygen.getFluid(1000)).output(ingot, AnnealedCopper).blastFurnaceTemp(1200).notConsumable(new IntCircuitIngredient(1)).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(500).EUt(120).input(ingot, Copper).fluidInputs(Oxygen.getFluid(1000)).output(ingot, AnnealedCopper).blastFurnaceTemp(1200).notConsumable(new IntCircuitIngredient(1)).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(500).EUt(1920).input(ingot, Iridium, 3).input(ingot, Osmium).fluidInputs(Helium.getFluid(1000)).output(ingotHot, Osmiridium, 4).blastFurnaceTemp(2900).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(500).EUt(30720).input(ingot, Naquadah).input(ingot, Osmiridium).fluidInputs(Argon.getFluid(1000)).output(ingotHot, NaquadahAlloy, 2).blastFurnaceTemp(NaquadahAlloy.blastFurnaceTemperature).buildAndRegister();

        registerBlastFurnaceMetallurgyRecipes();
    }

    private static void registerBlastFurnaceMetallurgyRecipes() {
        createSulfurDioxideRecipe(Stibnite, AntimonyTrioxide, 1500);
        createSulfurDioxideRecipe(Sphalerite, Zincite, 1000);
        createSulfurDioxideRecipe(Pyrite, BandedIron, 2000);
        createSulfurDioxideRecipe(Pentlandite, Garnierite, 1000);

        BLAST_RECIPES.recipeBuilder().duration(120).EUt(120).blastFurnaceTemp(1200)
                .input(dust, Tetrahedrite)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, CupricOxide)
                .output(dustTiny, AntimonyTrioxide, 3)
                .fluidOutputs(SulfurDioxide.getFluid(2000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(120).EUt(120).blastFurnaceTemp(1200)
                .input(dust, Cobaltite)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, CobaltOxide)
                .output(dust, ArsenicTrioxide)
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(120).EUt(120).blastFurnaceTemp(1200)
                .input(dust, Galena)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, Massicot)
                .output(nugget, Silver, 6)
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(120).EUt(120).blastFurnaceTemp(1200)
                .input(dust, Chalcopyrite)
                .input(dust, SiliconDioxide)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, CupricOxide)
                .output(dust, Ferrosilite)
                .fluidOutputs(SulfurDioxide.getFluid(2000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(240).EUt(120).blastFurnaceTemp(1200)
                .input(dust, SiliconDioxide)
                .input(dust, Carbon, 2)
                .output(ingot, Silicon)
                .output(dustTiny, Ash)
                .fluidOutputs(CarbonMonoxide.getFluid(2000))
                .buildAndRegister();
    }

    private static void createSulfurDioxideRecipe(IMaterial<?> inputMaterial, IMaterial<?> outputMaterial, int sulfurDioxideAmount) {
        BLAST_RECIPES.recipeBuilder().duration(120).EUt(120).blastFurnaceTemp(1200)
                .input(dust, inputMaterial)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, outputMaterial)
                .output(dustTiny, Ash)
                .fluidOutputs(SulfurDioxide.getFluid(sulfurDioxideAmount))
                .buildAndRegister();
    }

    private static void registerDecompositionRecipes() {


        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(RUBBER_DROP.getStackForm())
                .output(dust, RawRubber, 4)
                .duration(300).EUt(2)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(MetaBlocks.LEAVES.getItem(LogVariant.RUBBER_WOOD, 16))
                .output(dust, RawRubber)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(MetaBlocks.LOG.getItem(LogVariant.RUBBER_WOOD))
                .output(dust, RawRubber)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(MetaBlocks.SAPLING.getItem(LogVariant.RUBBER_WOOD))
                .output(dust, RawRubber)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(150).EUt(2)
                .inputs(new ItemStack(Items.SLIME_BALL))
                .output(dust, RawRubber, 2)
                .buildAndRegister();



        //electromagnetic separation recipes
        //todo add to ore byproducts page and cleanup
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, BrownLimonite).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.BrownLimonite)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Iron), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Iron), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, YellowLimonite).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.YellowLimonite)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Iron), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Iron), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, Nickel).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Nickel)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Iron), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Iron), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, Pentlandite).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Pentlandite)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Iron), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Iron), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, BandedIron).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.BandedIron)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Iron), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Iron), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, Ilmenite).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Ilmenite)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Iron), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Iron), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, Pyrite).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Pyrite)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Iron), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Iron), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, Tin).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Tin)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Iron), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Iron), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, Chromite).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Chromite)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Iron), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Iron), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, Monazite).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Monazite)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Neodymium), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Neodymium), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, Bastnasite).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Bastnasite)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Neodymium), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Neodymium), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, VanadiumMagnetite).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.VanadiumMagnetite)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Gold), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Gold), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, Magnetite).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Magnetite)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Gold), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.nugget, Materials.Gold), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, Naquadah).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Naquadah)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.NaquadahEnriched), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Trinium), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, Iridium).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Iridium)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Osmium), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Trinium), 2000, 600).buildAndRegister();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder().duration(400).EUt(24).input(dustPure, Wulfenite).outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Wulfenite)).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Trinium), 4000, 900).chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Trinium), 2000, 600).buildAndRegister();





        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).input("treeSapling", 8).output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Items.WHEAT, 8)).output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Items.POTATO, 8)).output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Items.CARROT, 8)).output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Blocks.CACTUS, 8)).output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Items.REEDS, 8)).output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Blocks.BROWN_MUSHROOM, 8)).output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Blocks.RED_MUSHROOM, 8)).output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Items.BEETROOT, 8)).output(PLANT_BALL).buildAndRegister();

    }

    private static void registerCutterRecipes() {
        for (int i = 0; i < 16; i++) {
            CUTTER_RECIPES.recipeBuilder().duration(50).EUt(8).inputs(new ItemStack(Blocks.STAINED_GLASS, 3, i)).outputs(new ItemStack(Blocks.STAINED_GLASS_PANE, 8, i)).buildAndRegister();
        }
        CUTTER_RECIPES.recipeBuilder().duration(50).EUt(8).inputs(new ItemStack(Blocks.GLASS, 3)).outputs(new ItemStack(Blocks.GLASS_PANE, 8)).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(25).EUt(8).inputs(new ItemStack(Blocks.STONE)).outputs(new ItemStack(Blocks.STONE_SLAB, 2)).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(25).EUt(8).inputs(new ItemStack(Blocks.SANDSTONE)).outputs(new ItemStack(Blocks.STONE_SLAB, 2, 1)).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(25).EUt(8).inputs(new ItemStack(Blocks.COBBLESTONE)).outputs(new ItemStack(Blocks.STONE_SLAB, 2, 3)).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(25).EUt(8).inputs(new ItemStack(Blocks.BRICK_BLOCK)).outputs(new ItemStack(Blocks.STONE_SLAB, 2, 4)).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(25).EUt(8).inputs(new ItemStack(Blocks.STONEBRICK)).outputs(new ItemStack(Blocks.STONE_SLAB, 2, 5)).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(25).EUt(8).inputs(new ItemStack(Blocks.NETHER_BRICK)).outputs(new ItemStack(Blocks.STONE_SLAB, 2, 6)).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(100).EUt(16).inputs(new ItemStack(Blocks.GLOWSTONE)).output(plate, Glowstone, 4).buildAndRegister();
    }

    private static void registerRecyclingRecipes() {
        FORGE_HAMMER_RECIPES.recipeBuilder()
            .input(stone.name(), 1)
            .outputs(new ItemStack(Blocks.COBBLESTONE, 1))
            .EUt(8).duration(200)
            .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
            .input(cobblestone.name(), 1)
            .outputs(new ItemStack(Blocks.GRAVEL, 1))
            .EUt(8).duration(200)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.GRAVEL, 1))
            .outputs(new ItemStack(Blocks.SAND))
            .EUt(8).duration(200)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .input(stone, Endstone)
            .output(dust, Endstone)
            .chancedOutput(dustTiny, Tungstate, 1200, 280)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .input(stone, Netherrack)
            .output(dust, Netherrack)
            .chancedOutput(nugget, Gold, 500, 120)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .input(stone, Soapstone)
            .output(dustImpure, Talc)
            .chancedOutput(dustTiny, Chromite, 1000, 280)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .input(stone, Redrock)
            .output(dust, Redrock)
            .chancedOutput(dust, Redrock, 1000, 380)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .input(stone, Marble)
            .output(dust, Marble)
            .chancedOutput(dust, Marble, 1000, 380)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .input(stone, Basalt)
            .output(dust, Basalt)
            .chancedOutput(dust, Basalt, 1000, 380)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .input(stone, GraniteBlack)
            .output(dust, GraniteBlack)
            .chancedOutput(dust, Thorium, 100, 40)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .input(stone, GraniteRed)
            .output(dust, GraniteRed)
            .chancedOutput(dustSmall, Uranium238, 100, 40)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .input(stone, Andesite)
            .output(dust, Andesite)
            .chancedOutput(dustSmall, Stone, 100, 40)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .input(stone, Diorite)
            .output(dust, Diorite)
            .chancedOutput(dustSmall, Stone, 100, 40)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .input(stone, Granite)
            .output(dust, Granite)
            .chancedOutput(dustSmall, Stone, 100, 40)
            .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Items.PORKCHOP))
            .output(dustSmall, Meat, 6)
            .output(dustTiny, Bone)
            .duration(102).EUt(4).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Items.FISH, 1, GTValues.W))
            .output(dustSmall, Meat, 6)
            .output(dustTiny, Bone)
            .duration(102).EUt(4).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Items.CHICKEN))
            .output(dust, Meat)
            .output(dustTiny, Bone)
            .duration(102).EUt(4).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Items.BEEF))
            .output(dustSmall, Meat, 6)
            .output(dustTiny, Bone)
            .duration(102).EUt(4).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Items.RABBIT))
            .output(dustSmall, Meat, 6)
            .output(dustTiny, Bone)
            .duration(102).EUt(4).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Items.MUTTON))
            .output(dust, Meat)
            .output(dustTiny, Bone)
            .duration(102).EUt(4).buildAndRegister();


    }

    private static void registerFluidRecipes() {


        FLUID_HEATER_RECIPES.recipeBuilder().duration(32).EUt(4)
                .fluidInputs(Ice.getFluid(L))
                .circuitMeta(1)
                .fluidOutputs(Water.getFluid(L)).buildAndRegister();



        FLUID_CANNER_RECIPES.recipeBuilder().duration(100).EUt(30).input(BATTERY_HULL_LV).fluidInputs(Mercury.getFluid(1000)).outputs(BATTERY_SU_LV_MERCURY.getChargedStack(Long.MAX_VALUE)).buildAndRegister();
        FLUID_CANNER_RECIPES.recipeBuilder().duration(200).EUt(30).input(BATTERY_HULL_MV).fluidInputs(Mercury.getFluid(4000)).outputs(BATTERY_SU_MV_MERCURY.getChargedStack(Long.MAX_VALUE)).buildAndRegister();
        FLUID_CANNER_RECIPES.recipeBuilder().duration(400).EUt(30).input(BATTERY_HULL_HV).fluidInputs(Mercury.getFluid(16000)).outputs(BATTERY_SU_HV_MERCURY.getChargedStack(Long.MAX_VALUE)).buildAndRegister();
        FLUID_CANNER_RECIPES.recipeBuilder().duration(100).EUt(30).input(BATTERY_HULL_LV).fluidInputs(SulfuricAcid.getFluid(1000)).outputs(BATTERY_SU_LV_SULFURIC_ACID.getChargedStack(Long.MAX_VALUE)).buildAndRegister();
        FLUID_CANNER_RECIPES.recipeBuilder().duration(200).EUt(30).input(BATTERY_HULL_MV).fluidInputs(SulfuricAcid.getFluid(4000)).outputs(BATTERY_SU_MV_SULFURIC_ACID.getChargedStack(Long.MAX_VALUE)).buildAndRegister();
        FLUID_CANNER_RECIPES.recipeBuilder().duration(400).EUt(30).input(BATTERY_HULL_HV).fluidInputs(SulfuricAcid.getFluid(16000)).outputs(BATTERY_SU_HV_SULFURIC_ACID.getChargedStack(Long.MAX_VALUE)).buildAndRegister();



        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(128).EUt(4).notConsumable(SHAPE_MOLD_BALL).fluidInputs(Water.getFluid(250)).outputs(new ItemStack(Items.SNOWBALL)).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(128).EUt(4).notConsumable(SHAPE_MOLD_BALL).fluidInputs(DistilledWater.getFluid(250)).outputs(new ItemStack(Items.SNOWBALL)).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(512).EUt(4).notConsumable(SHAPE_MOLD_BLOCK).fluidInputs(Water.getFluid(1000)).outputs(new ItemStack(Blocks.SNOW)).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(512).EUt(4).notConsumable(SHAPE_MOLD_BLOCK).fluidInputs(DistilledWater.getFluid(1000)).outputs(new ItemStack(Blocks.SNOW)).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(1024).EUt(16).notConsumable(SHAPE_MOLD_BLOCK).fluidInputs(Lava.getFluid(1000)).outputs(new ItemStack(Blocks.OBSIDIAN)).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(12).EUt(4).notConsumable(SHAPE_MOLD_BLOCK).fluidInputs(Glowstone.getFluid(L * 4)).outputs(new ItemStack(Blocks.GLOWSTONE)).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(12).EUt(4).notConsumable(SHAPE_MOLD_BLOCK).fluidInputs(Glass.getFluid(L)).outputs(new ItemStack(Blocks.GLASS)).buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(12).EUt(4).notConsumable(SHAPE_MOLD_PLATE).fluidInputs(Glass.getFluid(L)).output(plate, Glass).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(128).EUt(16).notConsumable(SHAPE_MOLD_ANVIL).fluidInputs(Iron.getFluid(L * 31)).outputs(new ItemStack(Blocks.ANVIL)).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(128).EUt(16).notConsumable(SHAPE_MOLD_ANVIL).fluidInputs(WroughtIron.getFluid(L * 31)).outputs(new ItemStack(Blocks.ANVIL)).buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(100).EUt(16).notConsumable(SHAPE_MOLD_BALL).fluidInputs(Toluene.getFluid(100)).output(GELLED_TOLUENE).buildAndRegister();

        FLUID_HEATER_RECIPES.recipeBuilder().duration(30).EUt(32).fluidInputs(Water.getFluid(6)).circuitMeta(1).fluidOutputs(Steam.getFluid(960)).buildAndRegister();
        FLUID_HEATER_RECIPES.recipeBuilder().duration(30).EUt(32).fluidInputs(DistilledWater.getFluid(6)).circuitMeta(1).fluidOutputs(Steam.getFluid(960)).buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().input(ingot, Iron, 31).notConsumable(SHAPE_MOLD_ANVIL).outputs(new ItemStack(Blocks.ANVIL)).duration(512).EUt(60).buildAndRegister();
        ALLOY_SMELTER_RECIPES.recipeBuilder().input(ingot, WroughtIron, 31).notConsumable(SHAPE_MOLD_ANVIL).outputs(new ItemStack(Blocks.ANVIL)).duration(512).EUt(60).buildAndRegister();
    }

    private static <T extends Enum<T> & IStringSerializable> void registerBrickRecipe(StoneBlock<T> stoneBlock, T normalVariant, T brickVariant) {
        ModHandler.addShapedRecipe(stoneBlock.getRegistryName().getNamespace() + "_" + normalVariant + "_bricks",
            stoneBlock.getItemVariant(brickVariant, ChiselingVariant.NORMAL, 4),
            "XX", "XX", 'X',
            stoneBlock.getItemVariant(normalVariant, ChiselingVariant.NORMAL));
    }

    private static <T extends Enum<T> & IStringSerializable> void registerChiselingRecipes(StoneBlock<T> stoneBlock) {
        for (T variant : stoneBlock.getVariantValues()) {
            boolean isBricksVariant = variant.getName().endsWith("_bricks");
            if (!isBricksVariant) {
                ModHandler.addSmeltingRecipe(stoneBlock.getItemVariant(variant, ChiselingVariant.CRACKED), stoneBlock.getItemVariant(variant, ChiselingVariant.NORMAL));
                FORGE_HAMMER_RECIPES.recipeBuilder().duration(12).EUt(4)
                    .inputs(stoneBlock.getItemVariant(variant, ChiselingVariant.NORMAL))
                    .outputs(stoneBlock.getItemVariant(variant, ChiselingVariant.CRACKED))
                    .buildAndRegister();
            } else {
                ModHandler.addSmeltingRecipe(stoneBlock.getItemVariant(variant, ChiselingVariant.NORMAL), stoneBlock.getItemVariant(variant, ChiselingVariant.CRACKED));
            }
            CHEMICAL_BATH_RECIPES.recipeBuilder().duration(12).EUt(4)
                .inputs(stoneBlock.getItemVariant(variant, !isBricksVariant ? ChiselingVariant.CRACKED : ChiselingVariant.NORMAL))
                .fluidInputs(Materials.Water.getFluid(144))
                .outputs(stoneBlock.getItemVariant(variant, ChiselingVariant.MOSSY))
                .buildAndRegister();
            ModHandler.addShapelessRecipe(stoneBlock.getRegistryName().getPath() + "_chiseling_" + variant,
                stoneBlock.getItemVariant(variant, ChiselingVariant.CHISELED),
                stoneBlock.getItemVariant(variant, ChiselingVariant.NORMAL));
        }
    }
}
