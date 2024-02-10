package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class AluminiumRecipes {

    public static void init() {
        // Bauxite -> Red Mud

        FURNACE_RECIPES.recipeBuilder()
                .fluidInputs(Water.getFluid(2000))
                .input(dust, SodiumHydroxide, 6)
                .input(dust, Bauxite, 5)
                .fluidOutputs(SodiumAluminateSolution.getFluid(3000))
                .fluidOutputs(RedMud.getFluid(200))
                .duration(200)
                .EUt(30)
                .buildAndRegister();

        // Carbon dioxide bubbling

        CRYSTALLIZER.recipeBuilder()
                .fluidInputs(fluid('sodium_aluminate_solution') * 3000)
                .fluidInputs(fluid('water') * 1000)
                .fluidInputs(fluid('carbon_dioxide') * 1000)
                .fluidOutputs(fluid('impure_soda_ash_solution') * 1000)
                .outputs(metaitem('dustAluminiumHydroxide') * 14)
                .duration(300)
                .EUt(20)
                .buildAndRegister()

        CRYSTALLIZER.recipeBuilder()
                .fluidInputs(fluid('sodium_aluminate_solution') * 1500)
                .fluidInputs(fluid('water') * 1500)
                .notConsumable(ore('dustAluminiumHydroxide'))
                .fluidOutputs(fluid('impure_sodium_hydroxide_solution') * 1000) //TO GALLIUM PROCESSING
                .outputs(metaitem('dustAluminiumHydroxide') * 7)
                .duration(300)
                .EUt(16)
                .buildAndRegister();

        // Alumina from Al(OH)3

        BLAST_RECIPES.recipeBuilder()
                .input(dust, AluminiumHydroxide, 14)
                .output(dust, Alumina, 5)
                .duration(100)
                .blastFurnaceTemp(1400)
                .EUt(40)
                .buildAndRegister();

        // Electrolysis

        ELECTROLYTIC_CELL.recipeBuilder()
                .notConsumable(fluid('cryolite') * 2592)
                .inputs(ore('dustAlumina') * 10)
                .inputs(ore('dustAluminiumTrifluoride'))
                .inputs(ore('dustCoke') * 3)
                .fluidOutputs(fluid('hydrogen_fluoride') * 750)
                .fluidOutputs(fluid('carbon_dioxide') * 3000)
                .outputs(metaitem('ingotAluminium') * 4)
                .duration(300)
                .EUt(40)
                .buildAndRegister()

        ELECTROLYTIC_CELL.recipeBuilder()
                .notConsumable(fluid('cryolite') * 2592)
                .inputs(ore('dustAlumina') * 10)
                .inputs(ore('dustAluminiumTrifluoride'))
                .inputs(ore('dustAnyPurityCarbon') * 3)
                .fluidOutputs(fluid('hydrogen_fluoride') * 750)
                .fluidOutputs(fluid('carbon_dioxide') * 3000)
                .outputs(metaitem('ingotAluminium') * 4)
                .duration(100)
                .EUt(40)
                .buildAndRegister()

        // Production of cryolite

        ROASTER.recipeBuilder()
                .fluidInputs(fluid('hydrofluoric_acid') * 6000)
                .inputs(ore('dustAlumina') * 5)
                .fluidOutputs(fluid('steam') * 9000)
                .outputs(metaitem('dustAluminiumTrifluoride') * 8)
                .duration(300)
                .EUt(16)
                .buildAndRegister()

        BCR.recipeBuilder()
                .fluidInputs(fluid('hydrogen_fluoride') * 50)
                .fluidInputs(fluid('sodium_hydroxide_solution') * 50)
                .fluidOutputs(fluid('sodium_fluoride_solution') * 50)
                .duration(5)
                .EUt(16)
                .buildAndRegister()

        DISTILLERY.recipeBuilder()
                .fluidInputs(fluid('sodium_fluoride_solution') * 1000)
                .fluidOutputs(fluid('water') * 1000)
                .outputs(metaitem('dustSodiumFluoride') * 2)
                .duration(200)
                .EUt(7)
                .buildAndRegister()

        ROASTER.recipeBuilder()
                .inputs(ore('dustAluminiumTrifluoride') * 4)
                .inputs(ore('dustSodiumFluoride') * 6)
                .outputs(metaitem('dustCryolite') * 10)
                .duration(180)
                .EUt(16)
                .buildAndRegister()

        // Red mud processing

        EMSEPARATOR.recipeBuilder()
                .fluidInputs(fluid('red_mud') * 2000)
                .chancedOutput(metaitem('dustIronIiiOxide'), 5000, 0)
                .fluidOutputs(fluid('concentrated_red_mud') * 1000)
                .duration(200)
                .EUt(96)
                .buildAndRegister()

        EBF.recipeBuilder()
                .fluidInputs(fluid('concentrated_red_mud') * 2000)
                .outputs(ore('ingotIron').first())
                .outputs(metaitem('red_mud_slag'))
                .blastFurnaceTemp(1600)
                .duration(300)
                .EUt(Globals.voltAmps[2] * 2)
                .buildAndRegister()

        CENTRIFUGE.recipeBuilder()
                .fluidInputs(fluid('sulfuric_acid') * 250)
                .inputs(metaitem('red_mud_slag'))
                .outputs(metaitem('dustTinyRutile') * 3)
                .outputs(metaitem('leached_red_mud_slag'))
                .duration(100)
                .EUt(Globals.voltAmps[3])
                .buildAndRegister()

        BR.recipeBuilder()
                .fluidInputs(fluid('sulfuric_acid') * 3000)
                .inputs(ore('dustAluminiumHydroxide') * 14)
                .fluidOutputs(fluid('aluminium_sulfate_solution') * 1000)
                .duration(100)
                .EUt(96)
                .buildAndRegister()

        BR.recipeBuilder()
                .fluidInputs(fluid('sulfuric_acid') * 3000)
                .fluidInputs(fluid('water') * 3000)
                .inputs(ore('dustAlumina') * 5)
                .fluidOutputs(fluid('aluminium_sulfate_solution') * 1000)
                .duration(100)
                .EUt(96)
                .buildAndRegister()

        DISTILLERY.recipeBuilder()
                .fluidInputs(fluid('aluminium_sulfate_solution') * 1000)
                .outputs(metaitem('dustAluminiumSulfate') * 17)
                .fluidOutputs(fluid('water') * 6000)
                .duration(100)
                .EUt(96)
                .buildAndRegister()

        ROASTER.recipeBuilder()
                .fluidInputs(fluid('sulfuric_acid') * 3000)
                .inputs(ore('dustAnyPurityAluminium') * 2)
                .outputs(metaitem('dustAluminiumSulfate') * 17)
                .fluidOutputs(fluid('hydrogen') * 6000)
                .duration(100)
                .EUt(120)
                .buildAndRegister()

        // CENTRIFUGE.recipeBuilder()
        // .fluidInputs(fluid('hydrochloric_acid') * 500)
        // .inputs(metaitem('leached_red_mud_slag'))
        // .fluidOutputs(fluid('acidic_ree_solution') * 500)
        // .duration(100)
        // .EUt(Globals.voltAmps[4])
        // .buildAndRegister()
    }
}
