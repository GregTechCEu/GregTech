package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class AluminiumRecipes {

    public static void init() {
        // Bauxite -> Red Mud

        FURNACE_RECIPES.recipeBuilder()
                .fluidInputs(SodiumHydroxideSolution.getFluid(2000))
                .input(dust, Bauxite, 5)
                .fluidOutputs(SodiumAluminateSolution.getFluid(3000))
                .fluidOutputs(RedMud.getFluid(200))
                .duration(300)
                .EUt(30)
                .buildAndRegister();

        CRYSTALLIZER_RECIPES.recipeBuilder()
                .fluidInputs(SodiumAluminateSolution.getFluid(1500))
                .fluidInputs(Water.getFluid(1500))
                .fluidOutputs(GalliumLiquor.getFluid(1000)) //TO GALLIUM PROCESSING
                .output(dust, AluminiumHydroxide, 7)
                .duration(150)
                .EUt(16)
                .buildAndRegister();

        // Alumina from Al(OH)3

        BLAST_RECIPES.recipeBuilder()
                .input(dust, AluminiumHydroxide, 14)
                .output(dust, Alumina, 5)
                .duration(300)
                .blastFurnaceTemp(1400)
                .EUt(120)
                .buildAndRegister();

        // Electrolysis

        ELECTROLYTIC_CELL_RECIPES.recipeBuilder()
                .notConsumable(Cryolite.getFluid(2592))
                .input(dust, Alumina, 10)
                .input(dust, AluminiumTrifluoride, 1)
                .input(dust, Coke, 3)
                .fluidOutputs(HydrogenFluoride.getFluid(750))
                .fluidOutputs(Aluminium.getFluid(576))
                .duration(300)
                .EUt(120)
                .buildAndRegister();

        // Production of cryolite

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(HydrogenFluoride.getFluid(6000))
                .input(dust, Alumina, 5)
                .output(dust, AluminiumTrifluoride, 8)
                .duration(200)
                .EUt(24)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(HydrogenFluoride.getFluid(1000))
                .input(dust, SodiumHydroxide, 3)
                .output(dust, SodiumFluoride, 2)
                .fluidOutputs(Water.getFluid(1000))
                .duration(60)
                .EUt(24)
                .buildAndRegister();

        FURNACE_RECIPES.recipeBuilder()
                .input(dust, AluminiumTrifluoride, 4)
                .input(dust, SodiumFluoride, 6)
                .output(dust, Cryolite, 10)
                .duration(180)
                .EUt(24)
                .buildAndRegister();

        // Red mud processing

        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder()
                .fluidInputs(RedMud.getFluid(1000))
                .chancedOutput(dust, Iron, 1000, 0)
                .duration(300)
                .EUt(30)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(RedMud.getFluid(1000))
                .fluidInputs(SulfuricAcid.getFluid(250))
                .chancedOutput(dust, Rutile, 2500, 0)
                .duration(300)
                .EUt(480)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder()
                .fluidInputs(RedMud.getFluid(1000))
                .fluidInputs(HydrochloricAcid.getFluid(250))
                .fluidOutputs(AcidicREESolution.getFluid(250))
                .duration(300)
                .EUt(1280)
                .buildAndRegister();

        // Byproducts

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricAcid.getFluid(3000))
                .input(dust, Aluminium, 2)
                .output(dust, AluminiumSulfate, 17)
                .fluidOutputs(Hydrogen.getFluid(6000))
                .duration(200)
                .EUt(30)
                .buildAndRegister();
    }
}
