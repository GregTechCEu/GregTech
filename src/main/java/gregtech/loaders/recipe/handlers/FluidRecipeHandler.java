package gregtech.loaders.recipe.handlers;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.CoolantProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.common.ConfigHolder;

public class FluidRecipeHandler {

    public static void runRecipeGeneration() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(PropertyKey.COOLANT))
                processCoolant(material, material.getProperty(PropertyKey.COOLANT));
        }
    }

    public static void processCoolant(Material mat, CoolantProperty coolant) {
        int water = (int) (ConfigHolder.machines.coolantRecovery * 6);
        RecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1).circuitMeta(1)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(6), Materials.Water.getFluid(water))
                .fluidOutputs(mat.getFluid(6), Materials.Steam.getFluid(water * 160))
                .buildAndRegister();

        RecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1).circuitMeta(1)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(6), Materials.DistilledWater.getFluid(water))
                .fluidOutputs(mat.getFluid(6), Materials.Steam.getFluid(water * 160))
                .buildAndRegister();

        water = (int) (ConfigHolder.machines.coolantRecovery * 600);
        RecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1).circuitMeta(2)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(6), Materials.Water.getFluid(water))
                .fluidOutputs(mat.getFluid(6), Materials.Steam.getFluid(water * 160))
                .buildAndRegister();

        RecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1).circuitMeta(2)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(6), Materials.DistilledWater.getFluid(water))
                .fluidOutputs(mat.getFluid(6), Materials.Steam.getFluid(water * 160))
                .buildAndRegister();

        // Radiator
        RecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(10).circuitMeta(3)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(8000))
                .fluidOutputs(mat.getFluid(8000))
                .buildAndRegister();
    }
}
