package gregtech.loaders.recipe.chemistry;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockGregLog;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Collection;
import java.util.List;

import static gregtech.api.GTValues.L;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.PLANT_BALL;
import static gregtech.common.items.MetaItems.RUBBER_DROP;

public class SeparationRecipes {

    public static void init() {

        // Centrifuge
        CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(RefineryGas.getFluid(8000))
                .fluidOutputs(Methane.getFluid(4000))
                .fluidOutputs(LPG.getFluid(4000))
                .duration(200).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(LiquidAir.getFluid(53000))
                .fluidOutputs(Nitrogen.getFluid(32000))
                .fluidOutputs(Nitrogen.getFluid(8000))
                .fluidOutputs(Oxygen.getFluid(11000))
                .fluidOutputs(Argon.getFluid(1000))
                .fluidOutputs(NobleGases.getFluid(1000))
                .duration(1484).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(NobleGases.getFluid(34000))
                .fluidOutputs(CarbonDioxide.getFluid(21000))
                .fluidOutputs(Helium.getFluid(9000))
                .fluidOutputs(Methane.getFluid(3000))
                .fluidOutputs(Deuterium.getFluid(1000))
                .duration(680).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(Butane.getFluid(320))
                .fluidOutputs(LPG.getFluid(370))
                .duration(20).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(Propane.getFluid(320))
                .fluidOutputs(LPG.getFluid(290))
                .duration(20).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(NitrationMixture.getFluid(2000))
                .fluidOutputs(NitricAcid.getFluid(1000))
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .duration(192).EUt(30).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .input(dust, ReinforcedEpoxyResin)
                .output(dust, Epoxy)
                .duration(24).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .input(OrePrefix.ore, Oilsands)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 5000)
                .fluidOutputs(Oil.getFluid(500))
                .duration(1000).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(144).EUt(5)
                .inputs(new ItemStack(Items.NETHER_WART))
                .fluidOutputs(Methane.getFluid(18))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(144).EUt(5)
                .inputs(new ItemStack(Blocks.BROWN_MUSHROOM))
                .fluidOutputs(Methane.getFluid(18))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(144).EUt(5)
                .inputs(new ItemStack(Blocks.RED_MUSHROOM))
                .fluidOutputs(Methane.getFluid(18))
                .buildAndRegister();


        if (ConfigHolder.addFoodMethaneRecipes) {
            for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
                if (item instanceof ItemFood) {
                    ItemFood itemFood = (ItemFood) item;
                    Collection<ItemStack> subItems = ModHandler.getAllSubItems(new ItemStack(item, 1, GTValues.W));
                    for (ItemStack itemStack : subItems) {
                        int healAmount = itemFood.getHealAmount(itemStack);
                        float saturationModifier = itemFood.getSaturationModifier(itemStack);
                        if (healAmount > 0) {
                            FluidStack outputStack = Methane.getFluid(Math.round(9 * healAmount * (1.0f + saturationModifier)));

                            CENTRIFUGE_RECIPES.recipeBuilder().duration(144).EUt(5)
                                    .inputs(itemStack)
                                    .fluidOutputs(outputStack)
                                    .buildAndRegister();
                        }
                    }
                }
            }
        }

        CENTRIFUGE_RECIPES.recipeBuilder().duration(400).EUt(5)
                .input(RUBBER_DROP)
                .output(dust, RawRubber, 3)
                .chancedOutput(PLANT_BALL, 1000, 850)
                .fluidOutputs(Glue.getFluid(100))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(20)
                .inputs(MetaBlocks.LOG.getItem(BlockGregLog.LogVariant.RUBBER_WOOD))
                .chancedOutput(RUBBER_DROP, 5000, 1200)
                .chancedOutput(PLANT_BALL, 3750, 900)
                .chancedOutput(dust, Carbon, 2500, 600)
                .chancedOutput(dust, Wood, 2500, 700)
                .fluidOutputs(Methane.getFluid(60))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(250).EUt(30)
                .inputs(new ItemStack(Blocks.DIRT, 1, GTValues.W))
                .chancedOutput(PLANT_BALL, 1250, 700)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 1200)
                .chancedOutput(dustTiny, Clay, 4000, 900)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(250).EUt(30)
                .inputs(new ItemStack(Blocks.GRASS))
                .chancedOutput(MetaItems.PLANT_BALL.getStackForm(), 3000, 1200)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 1200)
                .chancedOutput(dustTiny, Clay, 5000, 900)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(650).EUt(30)
                .inputs(new ItemStack(Blocks.MYCELIUM))
                .chancedOutput(new ItemStack(Blocks.RED_MUSHROOM), 2500, 900)
                .chancedOutput(new ItemStack(Blocks.BROWN_MUSHROOM), 2500, 900)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 1200)
                .chancedOutput(dustTiny, Clay, 5000, 900)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(240).EUt(30)
                .input(dust, Ash)
                .chancedOutput(dustSmall, Quicklime, 2, 9900, 0)
                .chancedOutput(dustSmall, Potash, 6400, 0)
                .chancedOutput(dustSmall, Magnesia, 6000, 0)
                .chancedOutput(dustTiny, PhosphorusPentoxide, 500, 0)
                .chancedOutput(dustTiny, SodaAsh, 5000, 0)
                .chancedOutput(dustTiny, BandedIron, 2500, 0)
                .buildAndRegister();


        CENTRIFUGE_RECIPES.recipeBuilder().duration(250).EUt(6)
                .input(dust, DarkAsh)
                .output(dust, Ash)
                .output(dust, Carbon)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(488).EUt(80)
                .input(dust, Glowstone)
                .output(dustSmall, Redstone, 2)
                .output(dustSmall, Gold, 2)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(36).EUt(30)
                .input(dust, Coal)
                .output(dust, Carbon, 2)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(800).EUt(320)
                .input(dust, Uranium238)
                .chancedOutput(dustTiny, Plutonium239, 200, 80)
                .chancedOutput(dustTiny, Uranium235, 2000, 350)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(1600).EUt(320)
                .input(dust, Plutonium239)
                .chancedOutput(dustTiny, Uranium238, 3000, 450)
                .chancedOutput(dustTiny, Plutonium241, 2000, 300)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(3200).EUt(320)
                .input(dust, Naquadah)
                .chancedOutput(dustTiny, Naquadria, 1000, 300)
                .chancedOutput(dustTiny, NaquadahEnriched, 5000, 750)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(6400).EUt(640)
                .input(dust, NaquadahEnriched)
                .chancedOutput(dustSmall, Naquadah, 3000, 400)
                .chancedOutput(dustSmall, Naquadria, 2000, 450)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(320).EUt(20)
                .input(dust, Endstone)
                .chancedOutput(new ItemStack(Blocks.SAND), 9000, 300)
                .chancedOutput(dustSmall, Tungstate, 1250, 450)
                .chancedOutput(dustTiny, Platinum, 625, 150)
                .fluidOutputs(Helium.getFluid(120))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(160).EUt(20)
                .input(dust, Netherrack)
                .chancedOutput(dustTiny, Redstone, 5625, 850)
                .chancedOutput(dustTiny, Gold, 625, 120)
                .chancedOutput(dustSmall, Sulfur, 9900, 100)
                .chancedOutput(dustTiny, Coal, 5625, 850)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(80)
                .inputs(new ItemStack(Blocks.SOUL_SAND))
                .chancedOutput(new ItemStack(Blocks.SAND), 9000, 130)
                .chancedOutput(dustSmall, Saltpeter, 8000, 480)
                .chancedOutput(dustTiny, Coal, 2000, 340)
                .fluidOutputs(Oil.getFluid(80))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(80).EUt(80)
                .fluidInputs(Lava.getFluid(100))
                .chancedOutput(nugget, Tantalum, 250, 90)
                .chancedOutput(nugget, Gold, 250, 80)
                .chancedOutput(nugget, Tin, 1000, 270)
                .chancedOutput(dustSmall, Tungstate, 250, 70)
                .chancedOutput(nugget, Copper, 2000, 320)
                .chancedOutput(nugget, Silver, 250, 80)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(64).EUt(20)
                .input(dust, RareEarth)
                .chancedOutput(dustSmall, Cadmium, 2500, 400)
                .chancedOutput(dustSmall, Neodymium, 2500, 400)
                .chancedOutput(dustSmall, Caesium, 2500, 400)
                .chancedOutput(dustSmall, Cerium, 2500, 400)
                .chancedOutput(dustSmall, Yttrium, 2500, 400)
                .chancedOutput(dustSmall, Lanthanum, 2500, 400)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(900).EUt(30)
                .input(dust, PlatinumGroupSludge)
                .output(dust, SiliconDioxide)
                .output(dustTiny, Gold)
                .output(dustTiny, Platinum)
                .chancedOutput(dustTiny, Palladium, 8000, 900)
                .chancedOutput(dustTiny, Iridium, 6000, 850)
                .chancedOutput(dustTiny, Osmium, 6000, 850)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(50).EUt(30)
                .inputs(new ItemStack(Blocks.SAND, 1, 1))
                .chancedOutput(dust, Iron, 5000, 500)
                .chancedOutput(dustTiny, Diamond, 100, 100)
                .chancedOutput(new ItemStack(Blocks.SAND, 1, 0), 5000, 5000)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(160).EUt(20)
                .fluidInputs(Hydrogen.getFluid(160))
                .fluidOutputs(Deuterium.getFluid(40))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(160).EUt(80)
                .fluidInputs(Deuterium.getFluid(160))
                .fluidOutputs(Tritium.getFluid(40))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(160).EUt(80)
                .fluidInputs(Helium.getFluid(80))
                .fluidOutputs(Helium3.getFluid(5))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(1600).EUt(8)
                .fluidInputs(Air.getFluid(10000))
                .fluidOutputs(Nitrogen.getFluid(3900))
                .fluidOutputs(Oxygen.getFluid(1000))
                .buildAndRegister();

        // Stone Dust
        CENTRIFUGE_RECIPES.recipeBuilder().duration(480).EUt(120)
                .input(dust, Stone)
                .output(dustSmall, Quartzite)
                .output(dustSmall, PotassiumFeldspar)
                .output(dustTiny, Marble, 2)
                .output(dustTiny, Biotite)
                .chancedOutput(dustTiny, MetalMixture, 7500, 750)
                .chancedOutput(dustTiny, Sodalite, 5000, 500)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(1000).EUt(900)
                .input(dust, MetalMixture)
                .output(dustSmall, BandedIron)
                .output(dustSmall, Bauxite)
                .output(dustTiny, Pyrolusite, 2)
                .output(dustTiny, Barite)
                .chancedOutput(dustTiny, Chromite, 7500, 750)
                .chancedOutput(dustTiny, Ilmenite, 5000, 500)
                .buildAndRegister();


        // Electrolyzer
        ELECTROLYZER_RECIPES.recipeBuilder()
                .input(dust, SodiumBisulfate, 7)
                .fluidOutputs(SodiumPersulfate.getFluid(500))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(150).EUt(30).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(SaltWater.getFluid(1000))
                .output(dust, SodiumHydroxide, 3)
                .fluidOutputs(Chlorine.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(720).EUt(30).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .input(dust, Sphalerite, 2)
                .output(dust, Zinc)
                .output(dust, Sulfur)
                .chancedOutput(dustTiny, Gallium, 2500, 1000)
                .duration(200).EUt(30).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .input(dust, Bauxite, 39)
                .output(dust, Rutile, 6)
                .output(dust, Aluminium, 16)
                .fluidOutputs(Hydrogen.getFluid(10000))
                .fluidOutputs(Oxygen.getFluid(11000))
                .duration(624).EUt(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .fluidOutputs(Oxygen.getFluid(1000))
                .duration(1500).EUt(30).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(DistilledWater.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .fluidOutputs(Oxygen.getFluid(1000))
                .duration(1500).EUt(30).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.DYE, 3))
                .output(dust, Calcium)
                .duration(96).EUt(26).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.SAND, 8))
                .output(dust, SiliconDioxide)
                .duration(500).EUt(25).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .input(dust, Graphite)
                .output(dust, Carbon, 4)
                .duration(100).EUt(26).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(AceticAcid.getFluid(2000))
                .fluidOutputs(Ethane.getFluid(1000))
                .fluidOutputs(CarbonDioxide.getFluid(2000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(512).EUt(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Chloromethane.getFluid(2000))
                .fluidOutputs(Ethane.getFluid(1000))
                .fluidOutputs(Chlorine.getFluid(2000))
                .duration(400).EUt(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Acetone.getFluid(2000))
                .output(dust, Carbon, 3)
                .fluidOutputs(Propane.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .duration(480).EUt(60).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Butane.getFluid(1000))
                .fluidOutputs(Butene.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(240).EUt(120).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Butene.getFluid(1000))
                .fluidOutputs(Butadiene.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(240).EUt(120).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(Propane.getFluid(1000))
                .fluidOutputs(Propene.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(640).EUt(120).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .input(dust, Diamond)
                .output(dust, Carbon, 64)
                .duration(768).EUt(30).buildAndRegister();

        // Thermal Centrifuge
        THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE, 1, GTValues.W))
                .output(dust, Stone)
                .duration(500).EUt(48).buildAndRegister();

        // Extractor
        EXTRACTOR_RECIPES.recipeBuilder()
                .input(dust, Monazite)
                .fluidOutputs(Helium.getFluid(200))
                .duration(64).EUt(64).buildAndRegister();

        List<Tuple<ItemStack, Integer>> seedEntries = GTUtility.getGrassSeedEntries();
        for (Tuple<ItemStack, Integer> seedEntry : seedEntries) {
            EXTRACTOR_RECIPES.recipeBuilder()
                    .duration(32).EUt(2)
                    .inputs(seedEntry.getFirst())
                    .fluidOutputs(SeedOil.getFluid(10))
                    .buildAndRegister();
        }

        EXTRACTOR_RECIPES.recipeBuilder().duration(32).EUt(2)
                .inputs(new ItemStack(Items.BEETROOT_SEEDS))
                .fluidOutputs(SeedOil.getFluid(10))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(32).EUt(2)
                .inputs(new ItemStack(Items.MELON_SEEDS, 1, GTValues.W))
                .fluidOutputs(SeedOil.getFluid(3))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(32).EUt(2)
                .inputs(new ItemStack(Items.PUMPKIN_SEEDS, 1, GTValues.W))
                .fluidOutputs(SeedOil.getFluid(6))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(600).EUt(28)
                .input(dust, Quartzite)
                .fluidOutputs(Glass.getFluid(L / 2))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(128).EUt(4)
                .inputs(new ItemStack(Items.COAL, 1, 1))
                .chancedOutput(dust, Ash, 1000, 200)
                .fluidOutputs(WoodTar.getFluid(100))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(16).EUt(4)
                .input(dust, Wood)
                .chancedOutput(PLANT_BALL, 200, 30)
                .fluidOutputs(Creosote.getFluid(5))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(32).EUt(4)
                .input(dust, HydratedCoal)
                .output(dust, Coal)
                .fluidOutputs(Water.getFluid(100))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(32).EUt(4)
                .inputs(new ItemStack(Items.SNOWBALL))
                .fluidOutputs(Water.getFluid(250))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(128).EUt(4)
                .inputs(new ItemStack(Blocks.SNOW))
                .fluidOutputs(Water.getFluid(1000))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.BRICK_BLOCK))
                .outputs(new ItemStack(Items.BRICK, 4))
                .duration(300).EUt(2).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.CLAY))
                .outputs(new ItemStack(Items.CLAY_BALL, 4))
                .duration(300).EUt(2).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.NETHER_BRICK))
                .outputs(new ItemStack(Items.NETHERBRICK, 4))
                .duration(300).EUt(2).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.BOOKSHELF))
                .outputs(new ItemStack(Items.BOOK, 3))
                .duration(300).EUt(2).buildAndRegister();
    }
}
