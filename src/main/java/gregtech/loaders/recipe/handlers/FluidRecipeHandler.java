package gregtech.loaders.recipe.handlers;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.CoolantProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;

public class FluidRecipeHandler {
    public static void runRecipeGeneration() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(PropertyKey.COOLANT))
                processCoolant(material, material.getProperty(PropertyKey.COOLANT));
        }
    }

    public static void processCoolant(Material mat, CoolantProperty coolant) {
        RecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(6), Materials.Water.getFluid(6))
                .fluidOutputs(mat.getFluid(6), Materials.Steam.getFluid(960))
                .buildAndRegister();

        RecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(6), Materials.DistilledWater.getFluid(6))
                .fluidOutputs(mat.getFluid(6), Materials.Steam.getFluid(960))
                .buildAndRegister();
    }
}
