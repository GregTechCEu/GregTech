package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.CENTRIFUGE_RECIPES;
import static gregtech.api.recipes.RecipeMaps.MIXER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.dustTiny;

public class GemSlurryRecipes {

    public static void init() {

        // Ruby
        MIXER_RECIPES.recipeBuilder().duration(280).EUt(VA[EV])
                .input(dust, Ruby, 6)
                .fluidInputs(AquaRegia.getFluid(3000))
                .fluidOutputs(RubySlurry.getFluid(3000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(320).EUt(VA[HV])
                .fluidInputs(RubySlurry.getFluid(3000))
                .output(dust, Aluminium, 2)
                .output(dust, Chrome)
                .chancedOutput(dustTiny, Titanium, 2000, 0)
                .chancedOutput(dustTiny, Iron, 2000, 0)
                .chancedOutput(dustTiny, Vanadium, 2000, 0)
                .fluidOutputs(Oxygen.getFluid(3000))
                .fluidOutputs(NitricAcid.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(2000))
                .buildAndRegister();

        // Sapphire
        MIXER_RECIPES.recipeBuilder().duration(280).EUt(VA[EV])
                .input(dust, Sapphire, 5)
                .fluidInputs(AquaRegia.getFluid(3000))
                .fluidOutputs(SapphireSlurry.getFluid(3000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(320).EUt(VA[HV])
                .fluidInputs(SapphireSlurry.getFluid(3000))
                .output(dust, Aluminium, 2)
                .chancedOutput(dustTiny, Titanium, 2000, 0)
                .chancedOutput(dustTiny, Iron, 2000, 0)
                .chancedOutput(dustTiny, Vanadium, 2000, 0)
                .fluidOutputs(Oxygen.getFluid(3000))
                .fluidOutputs(NitricAcid.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(2000))
                .buildAndRegister();

        // Green Sapphire
        MIXER_RECIPES.recipeBuilder().duration(280).EUt(VA[EV])
                .input(dust, GreenSapphire, 5)
                .fluidInputs(AquaRegia.getFluid(3000))
                .fluidOutputs(GreenSapphireSlurry.getFluid(3000))
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().duration(320).EUt(VA[HV])
                .fluidInputs(GreenSapphireSlurry.getFluid(3000))
                .output(dust, Aluminium, 2)
                .chancedOutput(dustTiny, Beryllium, 2000, 0)
                .chancedOutput(dustTiny, Titanium, 2000, 0)
                .chancedOutput(dustTiny, Iron, 2000, 0)
                .chancedOutput(dustTiny, Vanadium, 2000, 0)
                .fluidOutputs(Oxygen.getFluid(3000))
                .fluidOutputs(NitricAcid.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(2000))
                .buildAndRegister();
    }
}
