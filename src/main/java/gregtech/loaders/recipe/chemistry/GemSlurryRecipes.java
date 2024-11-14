package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.CENTRIFUGE_RECIPES;
import static gregtech.api.recipes.RecipeMaps.MIXER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class GemSlurryRecipes {

    public static void init() {
        // Ruby
        MIXER_RECIPES.recipeBuilder().duration(280).volts(VA[EV])
                .inputItem(crushed, Ruby, 2)
                .fluidInputs(AquaRegia.getFluid(3000))
                .fluidOutputs(RubySlurry.getFluid(3000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(320).volts(VA[HV])
                .fluidInputs(RubySlurry.getFluid(3000))
                .outputItem(dust, Aluminium, 2)
                .outputItem(dust, Chrome).outputItemRoll(dust, Titanium, 200, 0).outputItemRoll(dust, Iron, 200, 0)
                .outputItemRoll(dust, Vanadium, 200, 0)
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(2000))
                .buildAndRegister();

        // Sapphire
        MIXER_RECIPES.recipeBuilder().duration(280).volts(VA[EV])
                .inputItem(crushed, Sapphire, 2)
                .fluidInputs(AquaRegia.getFluid(3000))
                .fluidOutputs(SapphireSlurry.getFluid(3000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(320).volts(VA[HV])
                .fluidInputs(SapphireSlurry.getFluid(3000))
                .outputItem(dust, Aluminium, 2).outputItemRoll(dust, Titanium, 200, 0)
                .outputItemRoll(dust, Iron, 200, 0).outputItemRoll(dust, Vanadium, 200, 0)
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(2000))
                .buildAndRegister();

        // Green Sapphire
        MIXER_RECIPES.recipeBuilder().duration(280).volts(VA[EV])
                .inputItem(crushed, GreenSapphire, 2)
                .fluidInputs(AquaRegia.getFluid(3000))
                .fluidOutputs(GreenSapphireSlurry.getFluid(3000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(320).volts(VA[HV])
                .fluidInputs(GreenSapphireSlurry.getFluid(3000))
                .outputItem(dust, Aluminium, 2).outputItemRoll(dust, Beryllium, 200, 0)
                .outputItemRoll(dust, Titanium, 200, 0).outputItemRoll(dust, Iron, 200, 0)
                .outputItemRoll(dust, Vanadium, 200, 0)
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(2000))
                .buildAndRegister();
    }
}
