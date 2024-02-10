package gregtech.loaders.recipe.chemistry;

import gregtech.common.items.MetaItems;

import net.minecraft.init.Items;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class FermentingRecipes {

    public static void init() {
        PYROLYSE_RECIPES.recipeBuilder()
                .input(MetaItems.BIO_CHAFF, 4)
                .fluidInputs(Water.getFluid(4000))
                .fluidOutputs(Biomass.getFluid(4000))
                .duration(320)
                .EUt(64)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .input(MetaItems.BIO_CHAFF, 2)
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(Biomass.getFluid(1000))
                .duration(200)
                .EUt(30)
                .buildAndRegister();

        FERMENTING_RECIPES.recipeBuilder()
                .fluidInputs(Biomass.getFluid(16000))
                .fluidInputs(FermentedBiomass.getFluid(16000))
                .fluidOutputs(Methane.getFluid(10000))
                .duration(2400)
                .EUt(16)
                .buildAndRegister();

        //ETHANOL CHAIN

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(Items.WHEAT_SEEDS, 4)
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(Mash.getFluid(1000))
                .duration(100)
                .EUt(30)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(dust, Wheat, 4)
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(Mash.getFluid(1000))
                .duration(100)
                .EUt(30)
                .buildAndRegister();

        FERMENTING_RECIPES.recipeBuilder()
                .fluidInputs(Mash.getFluid(16000))
                .fluidOutputs(Alcohol.getFluid(16000))
                .duration(2400)
                .EUt(16)
                .buildAndRegister();

        FERMENTING_RECIPES.recipeBuilder()
                .input(Items.SUGAR, 24)
                .fluidInputs(Water.getFluid(16000))
                .fluidOutputs(Alcohol.getFluid(16000))
                .circuitMeta(3)
                .duration(1600)
                .EUt(16)
                .buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .fluidInputs(Alcohol.getFluid(1000))
                .fluidOutputs(Ethanol.getFluid(300))
                .duration(200)
                .EUt(30)
                .buildAndRegister();

        FERMENTING_RECIPES.recipeBuilder()
                .fluidInputs(Ethanol.getFluid(8000))
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidOutputs(Vinegar.getFluid(16000))
                .duration(2400)
                .EUt(16)
                .buildAndRegister();

        FERMENTING_RECIPES.recipeBuilder()
                .fluidInputs(Ethanol.getFluid(8000))
                .fluidInputs(Air.getFluid(10000))
                .fluidOutputs(Vinegar.getFluid(16000))
                .duration(2400)
                .EUt(16)
                .buildAndRegister();

        FERMENTING_RECIPES.recipeBuilder()
                .fluidInputs(Alcohol.getFluid(16000))
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidOutputs(Vinegar.getFluid(16000))
                .duration(2400)
                .EUt(16)
                .buildAndRegister();

        FERMENTING_RECIPES.recipeBuilder()
                .fluidInputs(Alcohol.getFluid(16000))
                .fluidInputs(Air.getFluid(10000))
                .fluidOutputs(Vinegar.getFluid(16000))
                .duration(2400)
                .EUt(16)
                .buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .fluidInputs(Vinegar.getFluid(1000))
                .fluidOutputs(AceticAcid.getFluid(250))
                .duration(200)
                .EUt(30)
                .buildAndRegister();
    }
}
