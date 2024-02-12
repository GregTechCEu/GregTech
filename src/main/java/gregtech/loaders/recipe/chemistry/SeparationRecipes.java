package gregtech.loaders.recipe.chemistry;

import gregtech.api.GTValues;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;

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
                .duration(200).EUt(5).buildAndRegister();

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
                .duration(192).EUt(VA[LV]).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .input(dust, ReinforcedEpoxyResin)
                .output(dust, Epoxy)
                .duration(24).EUt(5).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .input(ore, Oilsands)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 5000)
                .fluidOutputs(OilHeavy.getFluid(2000))
                .duration(200).EUt(30).buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(7)
                .input(dust, Oilsands)
                .fluidOutputs(OilHeavy.getFluid(1000))
                .buildAndRegister();

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

        CENTRIFUGE_RECIPES.recipeBuilder().duration(500).EUt(5)
                .inputs(new ItemStack(Items.MAGMA_CREAM))
                .outputs(new ItemStack(Items.BLAZE_POWDER))
                .outputs(new ItemStack(Items.SLIME_BALL))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(400).EUt(5)
                .input(STICKY_RESIN)
                .fluidOutputs(Glue.getFluid(250))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(250).EUt(VA[LV])
                .inputs(new ItemStack(Blocks.DIRT, 1, GTValues.W))
                .chancedOutput(PLANT_BALL, 1250, 700)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 1200)
                .chancedOutput(dust, Clay, 450, 100)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(250).EUt(VA[LV])
                .inputs(new ItemStack(Blocks.GRASS))
                .chancedOutput(MetaItems.PLANT_BALL.getStackForm(), 3000, 1200)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 1200)
                .chancedOutput(dust, Clay, 450, 100)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(650).EUt(VA[LV])
                .inputs(new ItemStack(Blocks.MYCELIUM))
                .chancedOutput(new ItemStack(Blocks.RED_MUSHROOM), 2500, 900)
                .chancedOutput(new ItemStack(Blocks.BROWN_MUSHROOM), 2500, 900)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 1200)
                .chancedOutput(dust, Clay, 450, 100)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(240).EUt(VA[LV])
                .input(dust, Ash)
                .chancedOutput(dust, Quicklime, 4950, 0)
                .chancedOutput(dust, Potash, 1600, 0)
                .chancedOutput(dust, Magnesia, 1500, 0)
                .chancedOutput(dust, PhosphorusPentoxide, 60, 0)
                .chancedOutput(dust, SodaAsh, 600, 0)
                .chancedOutput(dust, BandedIron, 275, 0)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(250).EUt(6)
                .input(dust, DarkAsh)
                .output(dust, Ash)
                .output(dust, Carbon)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(976).EUt(80)
                .input(dust, Glowstone, 2)
                .output(dust, Redstone)
                .output(dust, Gold)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(36).EUt(VA[LV])
                .input(dust, Coal)
                .output(dust, Carbon, 2)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(800).EUt(320)
                .input(dust, Uranium238)
                .chancedOutput(dust, Plutonium239, 20, 8)
                .chancedOutput(dust, Uranium235, 200, 35)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(1600).EUt(320)
                .input(dust, Plutonium239)
                .chancedOutput(dust, Uranium238, 300, 45)
                .chancedOutput(dust, Plutonium241, 2000, 300)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(320).EUt(20)
                .input(dust, Endstone)
                .chancedOutput(new ItemStack(Blocks.SAND), 9000, 300)
                .chancedOutput(dust, Tungstate, 315, 110)
                .chancedOutput(dust, Platinum, 70, 15)
                .fluidOutputs(Helium.getFluid(120))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(160).EUt(20)
                .input(dust, Netherrack)
                .chancedOutput(dust, Redstone, 625, 95)
                .chancedOutput(dust, Gold, 70, 15)
                .chancedOutput(dust, Sulfur, 2475, 25)
                .chancedOutput(dust, Coal, 625, 95)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(80)
                .inputs(new ItemStack(Blocks.SOUL_SAND))
                .chancedOutput(new ItemStack(Blocks.SAND), 9000, 130)
                .chancedOutput(dust, Saltpeter, 2000, 160)
                .chancedOutput(dust, Coal, 225, 40)
                .fluidOutputs(Oil.getFluid(80))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(80).EUt(80)
                .fluidInputs(Lava.getFluid(100))
                .chancedOutput(dust, SiliconDioxide, 1250, 80)
                .chancedOutput(dust, Magnesia, 250, 70)
                .chancedOutput(dust, Quicklime, 250, 70)
                .chancedOutput(nugget, Gold, 250, 80)
                .chancedOutput(dust, Sapphire, 315, 70)
                .chancedOutput(dust, Tantalite, 125, 35)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(64).EUt(20)
                .input(dust, RareEarth)
                .chancedOutput(dust, Cadmium, 600, 100)
                .chancedOutput(dust, Neodymium, 600, 100)
                .chancedOutput(dust, Samarium, 600, 100)
                .chancedOutput(dust, Cerium, 600, 100)
                .chancedOutput(dust, Yttrium, 600, 100)
                .chancedOutput(dust, Lanthanum, 600, 100)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(50).EUt(VA[LV])
                .inputs(new ItemStack(Blocks.SAND, 1, 1))
                .chancedOutput(dust, Iron, 5000, 500)
                .chancedOutput(dust, Diamond, 10, 10)
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

        CENTRIFUGE_RECIPES.recipeBuilder().duration(1600).EUt(VA[MV])
                .fluidInputs(NetherAir.getFluid(10000))
                .fluidOutputs(CarbonMonoxide.getFluid(3900))
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(1600).EUt(VA[HV])
                .fluidInputs(EnderAir.getFluid(10000))
                .fluidOutputs(NitrogenDioxide.getFluid(3900))
                .fluidOutputs(Deuterium.getFluid(1000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(900).EUt(VA[LV])
                .input(dust, RedAlloy)
                .output(dust, Redstone, 4)
                .output(dust, Copper)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(1000).EUt(900)
                .input(dust, MetalMixture)
                .chancedOutput(dust, BandedIron, 2500, 0)
                .chancedOutput(dust, Bauxite, 2500, 0)
                .chancedOutput(dust, Pyrolusite, 2222, 0)
                .chancedOutput(dust, Barite, 1111, 0)
                .chancedOutput(dust, Chromite, 825, 80)
                .chancedOutput(dust, Ilmenite, 550, 55)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(60).EUt(VA[LV])
                .input(dust, QuartzSand, 2)
                .output(dust, Quartzite)
                .chancedOutput(dust, CertusQuartz, 2000, 200)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(51).EUt(VA[LV])
                .fluidInputs(SaltWater.getFluid(1000))
                .output(dust, Salt, 2)
                .fluidOutputs(Water.getFluid(1000))
                .buildAndRegister();

        for (ItemStack seed : getGrassSeedItems()) {
            EXTRACTOR_RECIPES.recipeBuilder()
                    .duration(32).EUt(2)
                    .inputs(GTUtility.copy(1, seed))
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

        EXTRACTOR_RECIPES.recipeBuilder().duration(16).EUt(4)
                .inputs(new ItemStack(Items.FISH))
                .fluidOutputs(FishOil.getFluid(40))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(16).EUt(4)
                .inputs(new ItemStack(Items.FISH, 1, 1))
                .fluidOutputs(FishOil.getFluid(60))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(16).EUt(4)
                .inputs(new ItemStack(Items.FISH, 1, 2))
                .fluidOutputs(FishOil.getFluid(70))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(16).EUt(4)
                .inputs(new ItemStack(Items.FISH, 1, 3))
                .fluidOutputs(FishOil.getFluid(30))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(600).EUt(28)
                .input(dust, Quartzite)
                .fluidOutputs(Glass.getFluid(L / 2))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(128).EUt(4)
                .inputs(new ItemStack(Items.COAL, 1, 1))
                .fluidOutputs(WoodTar.getFluid(100))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(16).EUt(4)
                .input(dust, Wood)
                .chancedOutput(PLANT_BALL, 200, 30)
                .fluidOutputs(Creosote.getFluid(5))
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
