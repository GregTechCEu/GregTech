package gregtech.loaders.recipe.chemistry;

import gregtech.api.unification.material.Materials;
import gregtech.common.items.MetaItems;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.DISTILLATION_RECIPES;
import static gregtech.api.recipes.RecipeMaps.DISTILLERY_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class DistillationRecipes {

    public static void init() {
        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(Creosote.getFluid(24))
                .fluidOutputs(Lubricant.getFluid(12))
                .duration(16).EUt(96).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(DilutedHydrochloricAcid.getFluid(2000))
                .fluidOutputs(Water.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .duration(600).EUt(64).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(DilutedSulfuricAcid.getFluid(3000))
                .fluidOutputs(SulfuricAcid.getFluid(2000))
                .fluidOutputs(Water.getFluid(1000))
                .duration(600).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(CharcoalByproducts.getFluid(1000))
                .output(dustSmall, Charcoal)
                .fluidOutputs(WoodTar.getFluid(250))
                .fluidOutputs(WoodVinegar.getFluid(400))
                .fluidOutputs(WoodGas.getFluid(250))
                .fluidOutputs(Dimethylbenzene.getFluid(100))
                .duration(40).EUt(256).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(WoodTar.getFluid(1000))
                .fluidOutputs(Creosote.getFluid(300))
                .fluidOutputs(Phenol.getFluid(75))
                .fluidOutputs(Benzene.getFluid(350))
                .fluidOutputs(Toluene.getFluid(75))
                .fluidOutputs(Dimethylbenzene.getFluid(200))
                .duration(40).EUt(256).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(WoodVinegar.getFluid(1000))
                .fluidOutputs(AceticAcid.getFluid(100))
                .fluidOutputs(Water.getFluid(500))
                .fluidOutputs(Ethanol.getFluid(10))
                .fluidOutputs(Methanol.getFluid(300))
                .fluidOutputs(Acetone.getFluid(50))
                .fluidOutputs(MethylAcetate.getFluid(10))
                .duration(40).EUt(256).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(WoodGas.getFluid(1000))
                .fluidOutputs(CarbonDioxide.getFluid(490))
                .fluidOutputs(Ethylene.getFluid(20))
                .fluidOutputs(Methane.getFluid(130))
                .fluidOutputs(CarbonMonoxide.getFluid(340))
                .fluidOutputs(Hydrogen.getFluid(20))
                .duration(40).EUt(256).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(Water.getFluid(576))
                .fluidOutputs(DistilledWater.getFluid(520))
                .duration(160).EUt(VA[MV]).buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .fluidInputs(Water.getFluid(5))
                .circuitMeta(5)
                .fluidOutputs(Materials.DistilledWater.getFluid(5))
                .duration(16).EUt(10).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(Acetone.getFluid(1000))
                .fluidOutputs(Ethenone.getFluid(1000))
                .fluidOutputs(Methane.getFluid(1000))
                .duration(80).EUt(640).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(DissolvedCalciumAcetate.getFluid(1000))
                .output(dust, Quicklime, 2)
                .fluidOutputs(Acetone.getFluid(1000))
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .duration(80).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SeedOil.getFluid(24))
                .fluidOutputs(Lubricant.getFluid(12))
                .duration(16).EUt(96).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(FishOil.getFluid(1200))
                .fluidOutputs(Lubricant.getFluid(500))
                .duration(16).EUt(96).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(FermentedBiomass.getFluid(1000))
                .output(MetaItems.FERTILIZER)
                .fluidOutputs(AceticAcid.getFluid(25))
                .fluidOutputs(Water.getFluid(375))
                .fluidOutputs(Ethanol.getFluid(150))
                .fluidOutputs(Methanol.getFluid(150))
                .fluidOutputs(Ammonia.getFluid(100))
                .fluidOutputs(CarbonDioxide.getFluid(400))
                .fluidOutputs(Methane.getFluid(600))
                .duration(75).EUt(180).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(Biomass.getFluid(1000))
                .output(dustSmall, Wood, 2)
                .fluidOutputs(Ethanol.getFluid(600))
                .fluidOutputs(Water.getFluid(300))
                .duration(32).EUt(400).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(CoalGas.getFluid(1000))
                .output(dustSmall, Coke)
                .fluidOutputs(CoalTar.getFluid(200))
                .fluidOutputs(Ammonia.getFluid(300))
                .fluidOutputs(Ethylbenzene.getFluid(250))
                .fluidOutputs(CarbonDioxide.getFluid(250))
                .duration(80).EUt(VA[MV])
                .buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(CoalTar.getFluid(1000))
                .output(dustSmall, Coke)
                .fluidOutputs(Naphthalene.getFluid(400))
                .fluidOutputs(HydrogenSulfide.getFluid(300))
                .fluidOutputs(Creosote.getFluid(200))
                .fluidOutputs(Phenol.getFluid(100))
                .duration(80).EUt(VA[MV])
                .buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(LiquidAir.getFluid(50000))
                .fluidOutputs(Nitrogen.getFluid(35000))
                .fluidOutputs(Oxygen.getFluid(11000))
                .fluidOutputs(CarbonDioxide.getFluid(2500))
                .fluidOutputs(Helium.getFluid(1000))
                .fluidOutputs(Argon.getFluid(500))
                .chancedOutput(dust, Ice, 9000, 0)
                .disableDistilleryRecipes()
                .duration(2000).EUt(VA[HV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(LiquidNetherAir.getFluid(100000))
                .fluidOutputs(CarbonMonoxide.getFluid(72000))
                .fluidOutputs(CoalGas.getFluid(10000))
                .fluidOutputs(HydrogenSulfide.getFluid(7500))
                .fluidOutputs(SulfurDioxide.getFluid(7500))
                .fluidOutputs(Helium3.getFluid(2500))
                .fluidOutputs(Neon.getFluid(500))
                .chancedOutput(dustSmall, Ash, 9000, 0)
                .disableDistilleryRecipes()
                .duration(2000).EUt(VA[EV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(LiquidEnderAir.getFluid(200000))
                .fluidOutputs(NitrogenDioxide.getFluid(122000))
                .fluidOutputs(Deuterium.getFluid(50000))
                .fluidOutputs(Helium.getFluid(15000))
                .fluidOutputs(Tritium.getFluid(10000))
                .fluidOutputs(Krypton.getFluid(1000))
                .fluidOutputs(Xenon.getFluid(1000))
                .fluidOutputs(Radon.getFluid(1000))
                .chancedOutput(dustTiny, EnderPearl, 9000, 0)
                .disableDistilleryRecipes()
                .duration(2000).EUt(VA[IV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SluiceJuice.getFluid(1000))
                .output(dust, SluiceSand)
                .fluidOutputs(Water.getFluid(500))
                .duration(100).EUt(16).buildAndRegister();
    }
}
