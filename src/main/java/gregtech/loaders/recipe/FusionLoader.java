package gregtech.loaders.recipe;

import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;

import static gregtech.api.GTValues.*;

public class FusionLoader {

    public static void init() {

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Deuterium.getFluid(125))
                .fluidInputs(Materials.Tritium.getFluid(125))
                .fluidOutputs(Materials.Helium.getPlasma(125))
                .duration(16)
                .EUt(4096)
                .EUToStart(40000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Deuterium.getFluid(125))
                .fluidInputs(Materials.Helium3.getFluid(125))
                .fluidOutputs(Materials.Helium.getPlasma(125))
                .duration(16)
                .EUt(VA[EV])
                .EUToStart(60000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Carbon.getFluid(125))
                .fluidInputs(Materials.Helium3.getFluid(125))
                .fluidOutputs(Materials.Oxygen.getPlasma(125))
                .duration(32)
                .EUt(4096)
                .EUToStart(80000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Beryllium.getFluid(16))
                .fluidInputs(Materials.Deuterium.getFluid(375))
                .fluidOutputs(Materials.Nitrogen.getPlasma(175))
                .duration(16)
                .EUt(16384)
                .EUToStart(180000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Silicon.getFluid(16))
                .fluidInputs(Materials.Magnesium.getFluid(16))
                .fluidOutputs(Materials.Iron.getPlasma(125))
                .duration(32)
                .EUt(VA[IV])
                .EUToStart(360000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Potassium.getFluid(16))
                .fluidInputs(Materials.Fluorine.getFluid(125))
                .fluidOutputs(Materials.Nickel.getPlasma(125))
                .duration(16)
                .EUt(VA[LuV])
                .EUToStart(480000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Beryllium.getFluid(16))
                .fluidInputs(Materials.Tungsten.getFluid(16))
                .fluidOutputs(Materials.Platinum.getFluid(16))
                .duration(32)
                .EUt(VA[LuV])
                .EUToStart(150000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Neodymium.getFluid(16))
                .fluidInputs(Materials.Hydrogen.getFluid(48))
                .fluidOutputs(Materials.Europium.getFluid(16))
                .duration(64)
                .EUt(24576)
                .EUToStart(150000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Lutetium.getFluid(16))
                .fluidInputs(Materials.Chrome.getFluid(16))
                .fluidOutputs(Materials.Americium.getFluid(16))
                .duration(96)
                .EUt(49152)
                .EUToStart(200000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Plutonium239.getFluid(16))
                .fluidInputs(Materials.Thorium.getFluid(16))
                .fluidOutputs(Materials.Naquadah.getFluid(16))
                .duration(64)
                .EUt(VA[LuV])
                .EUToStart(300000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Americium.getFluid(16))
                .fluidInputs(Materials.Naquadria.getFluid(16))
                .fluidOutputs(Materials.Neutronium.getFluid(2))
                .duration(200)
                .EUt(98304)
                .EUToStart(600000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Tungsten.getFluid(16))
                .fluidInputs(Materials.Helium.getFluid(16))
                .fluidOutputs(Materials.Osmium.getFluid(16))
                .duration(64)
                .EUt(24578)
                .EUToStart(150000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Manganese.getFluid(16))
                .fluidInputs(Materials.Hydrogen.getFluid(16))
                .fluidOutputs(Materials.Iron.getFluid(16))
                .duration(64)
                .EUt(VA[IV])
                .EUToStart(120000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Mercury.getFluid(16))
                .fluidInputs(Materials.Magnesium.getFluid(16))
                .fluidOutputs(Materials.Uranium238.getFluid(16))
                .duration(64)
                .EUt(49152)
                .EUToStart(240000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Gold.getFluid(16))
                .fluidInputs(Materials.Aluminium.getFluid(16))
                .fluidOutputs(Materials.Uranium238.getFluid(16))
                .duration(64)
                .EUt(49152)
                .EUToStart(240000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Uranium238.getFluid(16))
                .fluidInputs(Materials.Helium.getFluid(16))
                .fluidOutputs(Materials.Plutonium239.getFluid(16))
                .duration(128)
                .EUt(49152)
                .EUToStart(480000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Vanadium.getFluid(16))
                .fluidInputs(Materials.Hydrogen.getFluid(125))
                .fluidOutputs(Materials.Chrome.getFluid(16))
                .duration(64)
                .EUt(24576)
                .EUToStart(140000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Gallium.getFluid(16))
                .fluidInputs(Materials.Radon.getFluid(125))
                .fluidOutputs(Materials.Duranium.getFluid(16))
                .duration(64)
                .EUt(16384)
                .EUToStart(140000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Titanium.getFluid(48))
                .fluidInputs(Materials.Duranium.getFluid(32))
                .fluidOutputs(Materials.Tritanium.getFluid(16))
                .duration(64)
                .EUt(VA[LuV])
                .EUToStart(200000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Gold.getFluid(16))
                .fluidInputs(Materials.Mercury.getFluid(16))
                .fluidOutputs(Materials.Radon.getFluid(125))
                .duration(64)
                .EUt(VA[LuV])
                .EUToStart(200000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Tantalum.getFluid(16))
                .fluidInputs(Materials.Tritium.getFluid(16))
                .fluidOutputs(Materials.Tungsten.getFluid(16))
                .duration(16)
                .EUt(24576)
                .EUToStart(200000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Silver.getFluid(16))
                .fluidInputs(Materials.Lithium.getFluid(16))
                .fluidOutputs(Materials.Indium.getFluid(16))
                .duration(32)
                .EUt(24576)
                .EUToStart(380000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.NaquadahEnriched.getFluid(15))
                .fluidInputs(Materials.Radon.getFluid(125))
                .fluidOutputs(Materials.Naquadria.getFluid(3))
                .duration(64)
                .EUt(49152)
                .EUToStart(400000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Lanthanum.getFluid(16))
                .fluidInputs(Materials.Silicon.getFluid(16))
                .fluidOutputs(Materials.Lutetium.getFluid(16))
                .duration(16)
                .EUt(VA[IV])
                .EUToStart(80000000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Plutonium241.getFluid(16))
                .fluidInputs(Materials.Titanium.getFluid(16))
                .fluidOutputs(Materials.Livermorium.getFluid(16))
                .duration(32)
                .EUt(VA[LuV])
                .EUToStart(200000000)
                .buildAndRegister();

    }
}
