package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;

import net.minecraft.init.Blocks;

public class FissionLoader {

    public static void init() {
        // TODO replace these with actual recipes
        RecipeMaps.FISSION_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.Uranium)
                .output(Blocks.COBBLESTONE) // what
                .heatOutput(GTValues.VH[GTValues.IV])
                .optimalTemperature(1000)
                .heatPenalty(0.9995)
                .buildAndRegister();
        RecipeMaps.FISSION_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, Materials.Plutonium239)
                .output(Blocks.COBBLESTONE)
                .heatOutput(GTValues.V[GTValues.IV])
                .optimalTemperature(3000)
                .heatPenalty(0.997)
                .buildAndRegister();

        RecipeMaps.FISSION_COOLANT_RECIPES.recipeBuilder()
                .fluidInput(Materials.Water.getFluid())
                .fluidOutputs(Materials.Steam.getFluid(160))
                .minimumTemperature(373)
                .heatAbsorption(160)
                .buildAndRegister();
    }
}
