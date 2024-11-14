package gregtech.loaders.recipe.chemistry;

import gregtech.api.GTValues;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.PLANT_BALL;
import static gregtech.common.items.MetaItems.STICKY_RESIN;

public class SeparationRecipes {

    public static void init() {
        // Centrifuge
        CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(RefineryGas.getFluid(8000))
                .fluidOutputs(Methane.getFluid(4000))
                .fluidOutputs(LPG.getFluid(4000))
                .duration(200).volts(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(Butane.getFluid(320))
                .fluidOutputs(LPG.getFluid(370))
                .duration(20).volts(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(Propane.getFluid(320))
                .fluidOutputs(LPG.getFluid(290))
                .duration(20).volts(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(NitrationMixture.getFluid(2000))
                .fluidOutputs(NitricAcid.getFluid(1000))
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .duration(192).volts(VA[LV]).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .inputItem(dust, ReinforcedEpoxyResin)
                .outputItem(dust, Epoxy)
                .duration(24).volts(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .inputItem(ore, Oilsands)
                .outputItemRoll(Blocks.SAND, 5000, 5000)
                .fluidOutputs(OilHeavy.getFluid(2000))
                .duration(200).volts(30).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).volts(7)
                .inputItem(dust, Oilsands)
                .fluidOutputs(OilHeavy.getFluid(1000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(144).volts(5)
                .inputs(new ItemStack(Items.NETHER_WART))
                .fluidOutputs(Methane.getFluid(18))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(144).volts(5)
                .inputs(new ItemStack(Blocks.BROWN_MUSHROOM))
                .fluidOutputs(Methane.getFluid(18))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(144).volts(5)
                .inputs(new ItemStack(Blocks.RED_MUSHROOM))
                .fluidOutputs(Methane.getFluid(18))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(500).volts(5)
                .inputs(new ItemStack(Items.MAGMA_CREAM))
                .outputs(new ItemStack(Items.BLAZE_POWDER))
                .outputs(new ItemStack(Items.SLIME_BALL))
                .buildAndRegister();

        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            if (item instanceof ItemFood) {
                ItemFood itemFood = (ItemFood) item;
                for (ItemStack itemStack : GTUtility.getAllSubItems(item)) {
                    int healAmount = itemFood.getHealAmount(itemStack);
                    float saturationModifier = itemFood.getSaturationModifier(itemStack);
                    if (healAmount > 0) {
                        FluidStack outputStack = Methane
                                .getFluid(Math.round(9 * healAmount * (1.0f + saturationModifier)));

                        CENTRIFUGE_RECIPES.recipeBuilder().duration(144).volts(5)
                                .inputs(itemStack)
                                .fluidOutputs(outputStack)
                                .buildAndRegister();
                    }
                }
            }
        }

        CENTRIFUGE_RECIPES.recipeBuilder().duration(400).volts(5)
                .inputItem(STICKY_RESIN)
                .outputItem(dust, RawRubber, 3).outputItemRoll(PLANT_BALL, 1000, 850)
                .fluidOutputs(Glue.getFluid(100))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).volts(20)
                .inputs(new ItemStack(MetaBlocks.RUBBER_LOG)).outputItemRoll(STICKY_RESIN, 5000, 1200)
                .outputItemRoll(PLANT_BALL, 3750, 900).outputItemRoll(dust, Carbon, 2500, 600)
                .outputItemRoll(dust, Wood, 2500, 700)
                .fluidOutputs(Methane.getFluid(60))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(250).volts(VA[LV])
                .inputs(new ItemStack(Blocks.DIRT, 1, W)).outputItemRoll(PLANT_BALL, 1250, 700)
                .outputsRolled(5000, 1200, new ItemStack(Blocks.SAND)).outputItemRoll(dust, Clay, 450, 100)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(250).volts(VA[LV])
                .inputs(new ItemStack(Blocks.GRASS)).outputsRolled(3000, 1200, PLANT_BALL.getStackForm())
                .outputsRolled(5000, 1200, new ItemStack(Blocks.SAND)).outputItemRoll(dust, Clay, 450, 100)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(650).volts(VA[LV])
                .inputs(new ItemStack(Blocks.MYCELIUM)).outputsRolled(2500, 900, new ItemStack(Blocks.RED_MUSHROOM))
                .outputsRolled(2500, 900, new ItemStack(Blocks.BROWN_MUSHROOM))
                .outputsRolled(5000, 1200, new ItemStack(Blocks.SAND)).outputItemRoll(dust, Clay, 450, 100)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(240).volts(VA[LV])
                .inputItem(dust, Ash).outputItemRoll(dust, Quicklime, 4950, 0).outputItemRoll(dust, Potash, 1600, 0)
                .outputItemRoll(dust, Magnesia, 1500, 0).outputItemRoll(dust, PhosphorusPentoxide, 60, 0)
                .outputItemRoll(dust, SodaAsh, 600, 0).outputItemRoll(dust, BandedIron, 275, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(250).volts(6)
                .inputItem(dust, DarkAsh)
                .outputItem(dust, Ash)
                .outputItem(dust, Carbon)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(976).volts(80)
                .inputItem(dust, Glowstone, 2)
                .outputItem(dust, Redstone)
                .outputItem(dust, Gold)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(36).volts(VA[LV])
                .inputItem(dust, Coal)
                .outputItem(dust, Carbon, 2)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(320).volts(20)
                .inputItem(dust, Endstone).outputsRolled(9000, 300, new ItemStack(Blocks.SAND))
                .outputItemRoll(dust, Tungstate, 315, 110).outputItemRoll(dust, Platinum, 70, 15)
                .fluidOutputs(Helium.getFluid(120))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(160).volts(20)
                .inputItem(dust, Netherrack).outputItemRoll(dust, Redstone, 625, 95).outputItemRoll(dust, Gold, 70, 15)
                .outputItemRoll(dust, Sulfur, 2475, 25).outputItemRoll(dust, Coal, 625, 95)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).volts(80)
                .inputs(new ItemStack(Blocks.SOUL_SAND)).outputsRolled(9000, 130, new ItemStack(Blocks.SAND))
                .outputItemRoll(dust, Saltpeter, 2000, 160).outputItemRoll(dust, Coal, 225, 40)
                .fluidOutputs(Oil.getFluid(80))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(80).volts(80)
                .fluidInputs(Lava.getFluid(100)).outputItemRoll(dust, SiliconDioxide, 1250, 80)
                .outputItemRoll(dust, Magnesia, 250, 70).outputItemRoll(dust, Quicklime, 250, 70)
                .outputItemRoll(nugget, Gold, 250, 80).outputItemRoll(dust, Sapphire, 315, 70)
                .outputItemRoll(dust, Tantalite, 125, 35)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(64).volts(20)
                .inputItem(dust, RareEarth).outputItemRoll(dustSmall, Cadmium, 2500, 400)
                .outputItemRoll(dustSmall, Neodymium, 2500, 400).outputItemRoll(dustSmall, Samarium, 2500, 400)
                .outputItemRoll(dustSmall, Cerium, 2500, 400).outputItemRoll(dustSmall, Yttrium, 2500, 400)
                .outputItemRoll(dustSmall, Lanthanum, 2500, 400)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(50).volts(VA[LV])
                .inputs(new ItemStack(Blocks.SAND, 1, 1)).outputItemRoll(dust, Iron, 5000, 500)
                .outputItemRoll(dust, Diamond, 10, 10).outputsRolled(5000, 5000, new ItemStack(Blocks.SAND, 1, 0))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(160).volts(20)
                .fluidInputs(Hydrogen.getFluid(160))
                .fluidOutputs(Deuterium.getFluid(40))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(160).volts(80)
                .fluidInputs(Deuterium.getFluid(160))
                .fluidOutputs(Tritium.getFluid(40))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(160).volts(80)
                .fluidInputs(Helium.getFluid(80))
                .fluidOutputs(Helium3.getFluid(5))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(1600).volts(VA[ULV])
                .fluidInputs(Air.getFluid(10000))
                .fluidOutputs(Nitrogen.getFluid(3900))
                .fluidOutputs(Oxygen.getFluid(1000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(1600).volts(VA[MV])
                .fluidInputs(NetherAir.getFluid(10000))
                .fluidOutputs(CarbonMonoxide.getFluid(3900))
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(1600).volts(VA[HV])
                .fluidInputs(EnderAir.getFluid(10000))
                .fluidOutputs(NitrogenDioxide.getFluid(3900))
                .fluidOutputs(Deuterium.getFluid(1000))
                .buildAndRegister();

        // Stone Dust
        CENTRIFUGE_RECIPES.recipeBuilder().duration(480).volts(VA[MV])
                .inputItem(dust, Stone).outputItemRoll(dust, Quartzite, 2500, 0)
                .outputItemRoll(dust, PotassiumFeldspar, 2500, 0).outputItemRoll(dust, Marble, 2222, 0)
                .outputItemRoll(dust, Biotite, 1111, 0).outputItemRoll(dust, MetalMixture, 825, 80)
                .outputItemRoll(dust, Sodalite, 550, 55)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(1000).volts(900)
                .inputItem(dust, MetalMixture).outputItemRoll(dust, BandedIron, 2500, 0)
                .outputItemRoll(dust, Bauxite, 2500, 0).outputItemRoll(dust, Pyrolusite, 2222, 0)
                .outputItemRoll(dust, Barite, 1111, 0).outputItemRoll(dust, Chromite, 825, 80)
                .outputItemRoll(dust, Ilmenite, 550, 55)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(60).volts(VA[LV])
                .inputItem(dust, QuartzSand, 2)
                .outputItem(dust, Quartzite).outputItemRoll(dust, CertusQuartz, 2000, 200)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(900).volts(VA[LV])
                .inputItem(dust, RedAlloy)
                .outputItem(dust, Redstone, 4)
                .outputItem(dust, Copper)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(1200).volts(VA[LV])
                .inputItem(dust, BlueAlloy)
                .outputItem(dust, Electrotine, 4)
                .outputItem(dust, Silver)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(800).volts(VA[LV])
                .inputItem(dust, Electrotine, 8)
                .outputItem(dust, Redstone)
                .outputItem(dust, Electrum)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(51).volts(VA[LV])
                .fluidInputs(SaltWater.getFluid(1000))
                .outputItem(dust, Salt, 2)
                .fluidOutputs(Water.getFluid(1000))
                .buildAndRegister();

        // Electrolyzer
        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(dust, SodiumBisulfate, 7)
                .fluidOutputs(SodiumPersulfate.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(150).volts(VA[LV]).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(SaltWater.getFluid(1000))
                .outputItem(dust, SodiumHydroxide, 3)
                .fluidOutputs(Chlorine.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(720).volts(VA[LV]).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(dust, Sphalerite, 2)
                .outputItem(dust, Zinc)
                .outputItem(dust, Sulfur).outputItemRoll(dust, Gallium, 500, 250)
                .duration(200).volts(VA[LV]).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .fluidOutputs(Oxygen.getFluid(1000))
                .duration(1500).volts(VA[LV]).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(DistilledWater.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .fluidOutputs(Oxygen.getFluid(1000))
                .duration(1500).volts(VA[LV]).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.DYE, 3))
                .outputItem(dust, Calcium)
                .duration(96).volts(26).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.SAND, 8))
                .outputItem(dust, SiliconDioxide)
                .duration(500).volts(25).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(dust, Graphite)
                .outputItem(dust, Carbon, 4)
                .duration(100).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(dust, Diamond)
                .outputItem(dust, Carbon, 64)
                .duration(768).volts(VA[LV]).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(dust, Trona, 16)
                .outputItem(dust, SodaAsh, 6)
                .outputItem(dust, SodiumBicarbonate, 6)
                .fluidOutputs(Water.getFluid(2000))
                .duration(784).volts(VA[LV] * 2).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(dust, Bauxite, 15)
                .outputItem(dust, Aluminium, 6)
                .outputItem(dust, Rutile)
                .fluidOutputs(Oxygen.getFluid(9000))
                .duration(270).volts(VA[LV] * 2).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(dust, Zeolite, 41)
                .outputItem(dust, Sodium)
                .outputItem(dust, Calcium, 4)
                .outputItem(dust, Silicon, 27)
                .outputItem(dust, Aluminium, 9)
                .duration(656).volts(VA[MV]).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(dust, Bentonite, 30)
                .outputItem(dust, Sodium)
                .outputItem(dust, Magnesium, 6)
                .outputItem(dust, Silicon, 12)
                .fluidOutputs(Water.getFluid(5000))
                .fluidOutputs(Hydrogen.getFluid(6000))
                .duration(480).volts(VA[MV]).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(dust, TungsticAcid, 7)
                .outputItem(dust, Tungsten)
                .fluidOutputs(Hydrogen.getFluid(2000))
                .fluidOutputs(Oxygen.getFluid(4000))
                .duration(210).volts(960).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(dust, SodiumHydroxide, 3)
                .outputItem(dust, Sodium)
                .fluidOutputs(Oxygen.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(150).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(dust, Sugar, 3)
                .outputItem(dust, Carbon)
                .fluidOutputs(Water.getFluid(1000))
                .duration(64).volts(VA[LV]).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputItem(dust, Apatite, 9)
                .outputItem(dust, Calcium, 5)
                .outputItem(dust, Phosphorus, 3)
                .fluidOutputs(Chlorine.getFluid(1000))
                .duration(288).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Propane.getFluid(1000))
                .outputItem(dust, Carbon, 3)
                .fluidOutputs(Hydrogen.getFluid(8000))
                .duration(176).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Butene.getFluid(1000))
                .outputItem(dust, Carbon, 4)
                .fluidOutputs(Hydrogen.getFluid(8000))
                .duration(192).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Butane.getFluid(1000))
                .outputItem(dust, Carbon, 4)
                .fluidOutputs(Hydrogen.getFluid(10000))
                .duration(224).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Styrene.getFluid(1000))
                .outputItem(dust, Carbon, 8)
                .fluidOutputs(Hydrogen.getFluid(8000))
                .duration(384).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Butadiene.getFluid(1000))
                .outputItem(dust, Carbon, 4)
                .fluidOutputs(Hydrogen.getFluid(6000))
                .duration(240).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Phenol.getFluid(1000))
                .outputItem(dust, Carbon, 6)
                .fluidOutputs(Hydrogen.getFluid(6000))
                .fluidOutputs(Oxygen.getFluid(1000))
                .duration(312).volts(90).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Ethylene.getFluid(1000))
                .outputItem(dust, Carbon, 2)
                .fluidOutputs(Hydrogen.getFluid(4000))
                .duration(96).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Benzene.getFluid(1000))
                .outputItem(dust, Carbon, 6)
                .fluidOutputs(Hydrogen.getFluid(6000))
                .duration(288).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Ethanol.getFluid(1000))
                .outputItem(dust, Carbon, 2)
                .fluidOutputs(Hydrogen.getFluid(6000))
                .fluidOutputs(Oxygen.getFluid(1000))
                .duration(144).volts(90).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Toluene.getFluid(1000))
                .outputItem(dust, Carbon, 7)
                .fluidOutputs(Hydrogen.getFluid(8000))
                .duration(360).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Dimethylbenzene.getFluid(1000))
                .outputItem(dust, Carbon, 8)
                .fluidOutputs(Hydrogen.getFluid(10000))
                .duration(432).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Octane.getFluid(1000))
                .outputItem(dust, Carbon, 8)
                .fluidOutputs(Hydrogen.getFluid(18000))
                .duration(624).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Propene.getFluid(1000))
                .outputItem(dust, Carbon, 3)
                .fluidOutputs(Hydrogen.getFluid(6000))
                .duration(144).volts(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Ethane.getFluid(1000))
                .outputItem(dust, Carbon, 2)
                .fluidOutputs(Hydrogen.getFluid(6000))
                .duration(128).volts(60).buildAndRegister();

        // Thermal Centrifuge
        THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE, 1, W))
                .outputItem(dust, Stone)
                .duration(500).volts(48).buildAndRegister();

        // Extractor
        EXTRACTOR_RECIPES.recipeBuilder()
                .inputItem(dust, Monazite)
                .outputItem(dustSmall, RareEarth)
                .fluidOutputs(Helium.getFluid(200))
                .duration(64).volts(64).buildAndRegister();

        for (ItemStack seed : getGrassSeedItems()) {
            EXTRACTOR_RECIPES.recipeBuilder()
                    .duration(32).volts(2)
                    .inputs(GTUtility.copy(1, seed))
                    .fluidOutputs(SeedOil.getFluid(10))
                    .buildAndRegister();
        }

        EXTRACTOR_RECIPES.recipeBuilder().duration(32).volts(2)
                .inputs(new ItemStack(Items.BEETROOT_SEEDS))
                .fluidOutputs(SeedOil.getFluid(10))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(32).volts(2)
                .inputs(new ItemStack(Items.MELON_SEEDS, 1, GTValues.W))
                .fluidOutputs(SeedOil.getFluid(3))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(32).volts(2)
                .inputs(new ItemStack(Items.PUMPKIN_SEEDS, 1, GTValues.W))
                .fluidOutputs(SeedOil.getFluid(6))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(16).volts(4)
                .inputs(new ItemStack(Items.FISH))
                .fluidOutputs(FishOil.getFluid(40))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(16).volts(4)
                .inputs(new ItemStack(Items.FISH, 1, 1))
                .fluidOutputs(FishOil.getFluid(60))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(16).volts(4)
                .inputs(new ItemStack(Items.FISH, 1, 2))
                .fluidOutputs(FishOil.getFluid(70))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(16).volts(4)
                .inputs(new ItemStack(Items.FISH, 1, 3))
                .fluidOutputs(FishOil.getFluid(30))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(600).volts(28)
                .inputItem(dust, Quartzite)
                .fluidOutputs(Glass.getFluid(L / 2))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(128).volts(4)
                .inputs(new ItemStack(Items.COAL, 1, 1))
                .fluidOutputs(WoodTar.getFluid(100))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(16).volts(4)
                .inputItem(dust, Wood).outputItemRoll(PLANT_BALL, 200, 30)
                .fluidOutputs(Creosote.getFluid(5))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(32).volts(4)
                .inputs(new ItemStack(Items.SNOWBALL))
                .fluidOutputs(Water.getFluid(250))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(128).volts(4)
                .inputs(new ItemStack(Blocks.SNOW))
                .fluidOutputs(Water.getFluid(1000))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.BRICK_BLOCK))
                .outputs(new ItemStack(Items.BRICK, 4))
                .duration(300).volts(2).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.CLAY))
                .outputs(new ItemStack(Items.CLAY_BALL, 4))
                .duration(300).volts(2).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.NETHER_BRICK))
                .outputs(new ItemStack(Items.NETHERBRICK, 4))
                .duration(300).volts(2).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.BOOKSHELF))
                .outputs(new ItemStack(Items.BOOK, 3))
                .duration(300).volts(2).buildAndRegister();
    }

    @SuppressWarnings("unchecked")
    private static List<ItemStack> getGrassSeedItems() {
        List<ItemStack> result = new ArrayList<>();
        try {
            Field seedListField = ForgeHooks.class.getDeclaredField("seedList");
            seedListField.setAccessible(true);
            Class<?> seedEntryClass = Class.forName("net.minecraftforge.common.ForgeHooks$SeedEntry");
            Field seedField = seedEntryClass.getDeclaredField("seed");
            seedField.setAccessible(true);

            List<WeightedRandom.Item> seedList = (List<WeightedRandom.Item>) seedListField.get(null);
            for (WeightedRandom.Item seedEntryObject : seedList) {
                ItemStack seedStack = (ItemStack) seedField.get(seedEntryObject);
                if (!seedStack.isEmpty()) {
                    result.add(seedStack);
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException ex) {
            GTLog.logger.error("Failed to get forge grass seed list", ex);
        }
        return result;
    }
}
