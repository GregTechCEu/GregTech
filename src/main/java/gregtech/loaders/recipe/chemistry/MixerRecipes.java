package gregtech.loaders.recipe.chemistry;

import net.minecraft.init.Items;

import static gregtech.api.recipes.RecipeMaps.MIXER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.dustTiny;

public class MixerRecipes {

    public static void init() {
        MIXER_RECIPES.recipeBuilder()
            .fluidInputs(NitricAcid.getFluid(1000))
            .fluidInputs(SulfuricAcid.getFluid(1000))
            .fluidOutputs(NitrationMixture.getFluid(2000))
            .duration(500).EUt(2).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .input(dust, Sodium, 2)
            .input(dust, Sulfur)
            .output(dust, SodiumSulfide, 3)
            .duration(60).EUt(30).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .fluidInputs(PolyvinylAcetate.getFluid(1000))
            .fluidInputs(Acetone.getFluid(1500))
            .fluidOutputs(Glue.getFluid(2500))
            .duration(50).EUt(8).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .fluidInputs(PolyvinylAcetate.getFluid(1000))
            .fluidInputs(MethylAcetate.getFluid(1500))
            .fluidOutputs(Glue.getFluid(2500))
            .duration(50).EUt(8).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .input(dust, Wood, 4)
            .fluidInputs(SulfuricAcid.getFluid(1000))
            .output(Items.COAL, 1, 1)
            .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
            .duration(1200).EUt(2).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .input(Items.SUGAR, 4)
            .fluidInputs(SulfuricAcid.getFluid(1000))
            .output(Items.COAL, 1, 1)
            .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
            .duration(1200).EUt(2).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .input(dust, Gallium)
            .input(dust, Arsenic)
            .output(dust, GalliumArsenide, 2)
            .duration(300).EUt(30).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .input(dust, Salt, 2)
            .fluidInputs(Water.getFluid(1000))
            .fluidOutputs(SaltWater.getFluid(1000))
            .duration(40).EUt(8).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .fluidInputs(BioDiesel.getFluid(1000))
            .fluidInputs(Tetranitromethane.getFluid(40))
            .fluidOutputs(NitroDiesel.getFluid(750))
            .duration(20).EUt(480).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .fluidInputs(Diesel.getFluid(1000))
            .fluidInputs(Tetranitromethane.getFluid(20))
            .fluidOutputs(NitroDiesel.getFluid(1000))
            .duration(20).EUt(480).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .fluidInputs(Oxygen.getFluid(1000))
            .fluidInputs(Dimethylhydrazine.getFluid(1000))
            .fluidOutputs(RocketFuel.getFluid(3000))
            .duration(60).EUt(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .fluidInputs(DinitrogenTetroxide.getFluid(1000))
            .fluidInputs(Dimethylhydrazine.getFluid(1000))
            .fluidOutputs(RocketFuel.getFluid(6000))
            .duration(60).EUt(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .fluidInputs(LightFuel.getFluid(5000))
            .fluidInputs(HeavyFuel.getFluid(1000))
            .fluidOutputs(Diesel.getFluid(6000))
            .duration(16).EUt(120).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
            .input(dust, Yttrium)
            .input(dust, Barium, 2)
            .input(dust, Copper, 3)
            .fluidInputs(Oxygen.getFluid(7000))
            .output(dust, YttriumBariumCuprate, 13)
            .EUt(8).duration(8000).buildAndRegister();
    }
}
