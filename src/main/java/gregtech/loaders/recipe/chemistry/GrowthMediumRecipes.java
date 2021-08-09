package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.dustTiny;
import static gregtech.common.items.MetaItems.*;

public class GrowthMediumRecipes {

    public static void init() {

        // Bio Chaff
        MACERATOR_RECIPES.recipeBuilder().EUt(30).duration(200)
                .input(PLANT_BALL, 2)
                .output(BIO_CHAFF)
                .output(BIO_CHAFF)
                .chancedOutput(BIO_CHAFF, 5000, 0)
                //.chancedOutput(BIO_CHAFF, 2500, 0) TODO Enable once macerator gets 4th slot
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder().EUt(2).duration(300)
                .input(BIO_CHAFF)
                .outputs(new ItemStack(Blocks.DIRT))
                .buildAndRegister();

        BREWING_RECIPES.recipeBuilder().EUt(4).duration(128)
                .input(BIO_CHAFF)
                .fluidInputs(Water.getFluid(750))
                .fluidOutputs(Biomass.getFluid(750))
                .buildAndRegister();

        BREWING_RECIPES.recipeBuilder().EUt(4).duration(128)
                .input(BIO_CHAFF)
                .fluidInputs(DistilledWater.getFluid(750))
                .fluidOutputs(Biomass.getFluid(750))
                .buildAndRegister();

        // Bacteria
        MIXER_RECIPES.recipeBuilder().EUt(480).duration(300)
                .input(BIO_CHAFF, 4)
                .fluidInputs(DistilledWater.getFluid(4000))
                .notConsumable(new IntCircuitIngredient(1))
                .fluidOutputs(Bacteria.getFluid(2000))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().EUt(1920).duration(300)
                .input(dust, Vinteum)
                .fluidInputs(DistilledWater.getFluid(8000))
                .notConsumable(new IntCircuitIngredient(1))
                .fluidOutputs(Bacteria.getFluid(4000))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().EUt(7680).duration(300)
                .input(dustTiny, Naquadria)
                .fluidInputs(DistilledWater.getFluid(16000))
                .notConsumable(new IntCircuitIngredient(1))
                .fluidOutputs(Bacteria.getFluid(8000))
                .buildAndRegister();

        // Bacterial Sludge
        MIXER_RECIPES.recipeBuilder().EUt(1920).duration(600)
                .input(dust, Endstone, 16)
                .fluidInputs(DistilledWater.getFluid(4000))
                .notConsumable(new IntCircuitIngredient(2))
                .fluidOutputs(BacterialSludge.getFluid(1000))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().EUt(7680).duration(600)
                .input(dust, Vinteum)
                .fluidInputs(DistilledWater.getFluid(8000))
                .notConsumable(new IntCircuitIngredient(2))
                .fluidOutputs(BacterialSludge.getFluid(2000))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().EUt(30720).duration(600)
                .input(dustTiny, Naquadria)
                .fluidInputs(DistilledWater.getFluid(16000))
                .notConsumable(new IntCircuitIngredient(2))
                .fluidOutputs(BacterialSludge.getFluid(4000))
                .buildAndRegister();

        // Enriched Bacterial Sludge
        BREWING_RECIPES.recipeBuilder().EUt(4).duration(128)
                .input(dust, Uranium238)
                .fluidInputs(BacterialSludge.getFluid(750))
                .fluidOutputs(EnrichedBacterialSludge.getFluid(750))
                .buildAndRegister();

        BREWING_RECIPES.recipeBuilder().EUt(4).duration(128)
                .input(dustTiny, Uranium235)
                .fluidInputs(BacterialSludge.getFluid(750))
                .fluidOutputs(EnrichedBacterialSludge.getFluid(750))
                .buildAndRegister();

        // Fermented Bacterial Sludge
        FERMENTING_RECIPES.recipeBuilder().EUt(2).duration(2400)
                .fluidInputs(EnrichedBacterialSludge.getFluid(750))
                .fluidOutputs(FermentedBacterialSludge.getFluid(75))
                .buildAndRegister();

        // Mutagen
        DISTILLERY_RECIPES.recipeBuilder().EUt(1920).duration(60)
                .fluidInputs(FermentedBacterialSludge.getFluid(10))
                .notConsumable(new IntCircuitIngredient(3))
                .fluidOutputs(Mutagen.getFluid(1))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().EUt(7680).duration(1200)
                .input(dust, Vinteum, 4)
                .fluidInputs(DistilledWater.getFluid(4000))
                .notConsumable(new IntCircuitIngredient(4))
                .fluidOutputs(Mutagen.getFluid(1000))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().EUt(30720).duration(1200)
                .input(dustTiny, Naquadria)
                .fluidInputs(DistilledWater.getFluid(8000))
                .notConsumable(new IntCircuitIngredient(4))
                .fluidOutputs(Mutagen.getFluid(2000))
                .buildAndRegister();

        // Collagen
        CHEMICAL_RECIPES.recipeBuilder().EUt(480).duration(800)
                .input(dust, Meat)
                .inputs(new ItemStack(Items.DYE, 1, 15))
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(dust, Collagen)
                .fluidOutputs(DilutedSulfuricAcid.getFluid(500))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(480).duration(1600)
                .input(dust, Meat, 2)
                .inputs(new ItemStack(Items.BONE))
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(dust, Collagen, 2)
                .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
                .buildAndRegister();

        // Gelatin
        CHEMICAL_RECIPES.recipeBuilder().EUt(480).duration(1600)
                .input(dust, Collagen, 4)
                .fluidInputs(PhosphoricAcid.getFluid(1000))
                .fluidInputs(Water.getFluid(3000))
                .fluidOutputs(GelatinMixture.getFluid(4000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().EUt(480).duration(2400)
                .fluidInputs(GelatinMixture.getFluid(6000))
                .output(dust, Phosphorus)
                .output(dust, Gelatin, 4)
                .buildAndRegister();

        // Agar
        AUTOCLAVE_RECIPES.recipeBuilder().EUt(480).duration(600)
                .input(dust, Gelatin)
                .fluidInputs(DistilledWater.getFluid(1000))
                .output(dust, Agar)
                .buildAndRegister();

        // Raw Growth Medium
        MIXER_RECIPES.recipeBuilder().EUt(7680).duration(1200)
                .input(dust, Meat, 4)
                .input(dust, Salt, 4)
                .input(dust, Calcium, 4)
                .input(dust, Agar, 4)
                .fluidInputs(Bacteria.getFluid(4000))
                .fluidOutputs(RawGrowthMedium.getFluid(1000))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().EUt(30720).duration(1200)
                .input(dust, Meat, 8)
                .input(dust, Salt, 8)
                .input(dust, Calcium, 8)
                .input(dust, Agar, 4)
                .fluidInputs(BacterialSludge.getFluid(4000))
                .fluidOutputs(RawGrowthMedium.getFluid(2000))
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().EUt(122880).duration(1200)
                .input(dust, Meat, 12)
                .input(dust, Salt, 12)
                .input(dust, Calcium, 12)
                .input(dust, Agar, 4)
                .fluidInputs(Mutagen.getFluid(4000))
                .fluidOutputs(RawGrowthMedium.getFluid(4000))
                .buildAndRegister();

        // Sterile Growth Medium
        FLUID_HEATER_RECIPES.recipeBuilder().EUt(7680).duration(20)
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(RawGrowthMedium.getFluid(100))
                .fluidOutputs(SterileGrowthMedium.getFluid(100))
                .buildAndRegister();

        // Stem Cells
        CHEMICAL_RECIPES.recipeBuilder().EUt(30720).duration(300)
                .input(dust, Osmiridium)
                .input(dust, Vinteum, 2)
                .fluidInputs(SterileGrowthMedium.getFluid(500))
                .output(STEM_CELLS, 32)
                .fluidOutputs(BacterialSludge.getFluid(500))
                .buildAndRegister();
    }
}
