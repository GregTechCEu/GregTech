package gregtech.loaders.recipe.chemistry;

import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import net.minecraft.init.Items;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.CHEMICAL_BATH_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class ChemicalBathRecipes {

    public static void init() {

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(dust, Wood)
                .fluidInputs(Water.getFluid(100))
                .output(Items.PAPER)
                .duration(200).EUt(4).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(dust, Paper)
                .fluidInputs(Water.getFluid(100))
                .output(Items.PAPER)
                .duration(100).EUt(4).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(Items.REEDS, 1, true)
                .fluidInputs(Water.getFluid(100))
                .output(Items.PAPER)
                .duration(100).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(dust, Wood)
                .fluidInputs(DistilledWater.getFluid(100))
                .output(Items.PAPER)
                .duration(200).EUt(4).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(dust, Paper)
                .fluidInputs(DistilledWater.getFluid(100))
                .output(Items.PAPER)
                .duration(100).EUt(4).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(Items.REEDS, 1, true)
                .fluidInputs(DistilledWater.getFluid(100))
                .output(Items.PAPER)
                .duration(100).EUt(VA[ULV]).buildAndRegister();

        //todo add these to ore byproducts
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushed, Gold)
                .fluidInputs(SodiumPersulfate.getFluid(100))
                .output(crushedPurified, Gold)
                .chancedOutput(OreDictUnifier.get(dust, Copper), 7000, 580)
                .chancedOutput(OreDictUnifier.get(dust, Stone), 4000, 650)
                .duration(800).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushed, Nickel)
                .fluidInputs(SodiumPersulfate.getFluid(100))
                .output(crushedPurified, Nickel)
                .chancedOutput(OreDictUnifier.get(dust, Nickel), 7000, 580)
                .chancedOutput(OreDictUnifier.get(dust, Stone), 4000, 650)
                .duration(800).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushed, Cooperite)
                .fluidInputs(SodiumPersulfate.getFluid(100))
                .output(crushedPurified, Cooperite)
                .chancedOutput(OreDictUnifier.get(dust, Nickel), 7000, 580)
                .chancedOutput(OreDictUnifier.get(dust, Stone), 4000, 650)
                .duration(800).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushed, Copper)
                .fluidInputs(SodiumPersulfate.getFluid(100))
                .output(crushedPurified, Copper)
                .chancedOutput(OreDictUnifier.get(dust, Copper), 7000, 580)
                .chancedOutput(OreDictUnifier.get(dust, Stone), 4000, 650)
                .duration(800).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushed, Platinum)
                .fluidInputs(SodiumPersulfate.getFluid(100))
                .output(crushedPurified, Platinum)
                .chancedOutput(OreDictUnifier.get(dust, Nickel), 7000, 580)
                .chancedOutput(OreDictUnifier.get(dust, Stone), 4000, 650)
                .duration(800).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(crushed, Chalcopyrite)
                .fluidInputs(SodiumPersulfate.getFluid(100))
                .output(crushedPurified, Chalcopyrite)
                .chancedOutput(OreDictUnifier.get(dust, Cobalt), 7000, 580)
                .chancedOutput(OreDictUnifier.get(dust, Stone), 4000, 650)
                .duration(800).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(dust, Scheelite, 6)
                .fluidInputs(HydrochloricAcid.getFluid(2000))
                .output(dust, TungsticAcid, 7)
                .output(dust, CalciumChloride, 3)
                .duration(210).EUt(VA[GTValues.EV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(dust, Tungstate, 7)
                .fluidInputs(HydrochloricAcid.getFluid(2000))
                .output(dust, TungsticAcid, 7)
                .output(dust, LithiumChloride, 4)
                .duration(210).EUt(VA[GTValues.EV]).buildAndRegister();
    }
}
