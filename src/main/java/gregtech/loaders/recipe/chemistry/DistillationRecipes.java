package gregtech.loaders.recipe.chemistry;

import gregtech.api.unification.material.Materials;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

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
                .fluidInputs(HydroCrackedEthane.getFluid(1000))
                .fluidOutputs(Methane.getFluid(2000))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedEthane.getFluid(1000))
                .output(dustSmall, Carbon, 2)
                .fluidOutputs(Methane.getFluid(1500))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedEthylene.getFluid(1000))
                .fluidOutputs(Ethane.getFluid(1000))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedEthylene.getFluid(1000))
                .output(dust, Carbon)
                .fluidOutputs(Methane.getFluid(1000))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedPropene.getFluid(1000))
                .fluidOutputs(Propane.getFluid(500))
                .fluidOutputs(Ethylene.getFluid(500))
                .fluidOutputs(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedPropene.getFluid(1000))
                .output(dustSmall, Carbon, 6)
                .fluidOutputs(Methane.getFluid(1500))
                .duration(180).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedPropane.getFluid(1000))
                .fluidOutputs(Ethane.getFluid(1000))
                .fluidOutputs(Methane.getFluid(1000))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedPropane.getFluid(1000))
                .output(dustSmall, Carbon, 2)
                .fluidOutputs(Ethylene.getFluid(750))
                .fluidOutputs(Methane.getFluid(1250))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedLightFuel.getFluid(1000))
                .fluidOutputs(Naphtha.getFluid(800))
                .fluidOutputs(Octane.getFluid(100))
                .fluidOutputs(Butane.getFluid(150))
                .fluidOutputs(Propane.getFluid(200))
                .fluidOutputs(Ethane.getFluid(125))
                .fluidOutputs(Methane.getFluid(125))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedLightFuel.getFluid(1000))
                .output(dustTiny, Carbon, 3)
                .fluidOutputs(HeavyFuel.getFluid(50))
                .fluidOutputs(Naphtha.getFluid(100))
                .fluidOutputs(Toluene.getFluid(30))
                .fluidOutputs(Benzene.getFluid(150))
                .fluidOutputs(Butene.getFluid(65))
                .fluidOutputs(Butadiene.getFluid(50))
                .fluidOutputs(Propane.getFluid(50))
                .fluidOutputs(Propene.getFluid(250))
                .fluidOutputs(Ethane.getFluid(50))
                .fluidOutputs(Ethylene.getFluid(250))
                .fluidOutputs(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedButane.getFluid(750))
                .fluidOutputs(Propane.getFluid(500))
                .fluidOutputs(Ethane.getFluid(500))
                .fluidOutputs(Methane.getFluid(500))
                .duration(90).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedButane.getFluid(1000))
                .output(dustTiny, Carbon, 11)
                .fluidOutputs(Propane.getFluid(125))
                .fluidOutputs(Ethane.getFluid(125))
                .fluidOutputs(Ethylene.getFluid(125))
                .fluidOutputs(Methane.getFluid(2000))
                .duration(240).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedNaphtha.getFluid(1000))
                .fluidOutputs(Butane.getFluid(800))
                .fluidOutputs(Propane.getFluid(300))
                .fluidOutputs(Ethane.getFluid(250))
                .fluidOutputs(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedNaphtha.getFluid(1000))
                .output(dustTiny, Carbon, 3)
                .fluidOutputs(HeavyFuel.getFluid(25))
                .fluidOutputs(LightFuel.getFluid(50))
                .fluidOutputs(Toluene.getFluid(20))
                .fluidOutputs(Benzene.getFluid(100))
                .fluidOutputs(Butene.getFluid(50))
                .fluidOutputs(Butadiene.getFluid(50))
                .fluidOutputs(Propane.getFluid(15))
                .fluidOutputs(Propene.getFluid(300))
                .fluidOutputs(Ethane.getFluid(65))
                .fluidOutputs(Ethylene.getFluid(500))
                .fluidOutputs(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedHeavyFuel.getFluid(1000))
                .fluidOutputs(LightFuel.getFluid(600))
                .fluidOutputs(Naphtha.getFluid(100))
                .fluidOutputs(Butane.getFluid(100))
                .fluidOutputs(Propane.getFluid(100))
                .fluidOutputs(Ethane.getFluid(75))
                .fluidOutputs(Methane.getFluid(75))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedHeavyFuel.getFluid(1000))
                .output(dustTiny, Carbon, 3)
                .fluidOutputs(LightFuel.getFluid(100))
                .fluidOutputs(Naphtha.getFluid(125))
                .fluidOutputs(Toluene.getFluid(80))
                .fluidOutputs(Benzene.getFluid(400))
                .fluidOutputs(Butene.getFluid(80))
                .fluidOutputs(Butadiene.getFluid(50))
                .fluidOutputs(Propane.getFluid(10))
                .fluidOutputs(Propene.getFluid(100))
                .fluidOutputs(Ethane.getFluid(15))
                .fluidOutputs(Ethylene.getFluid(150))
                .fluidOutputs(Methane.getFluid(150))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedGas.getFluid(1000))
                .fluidOutputs(Methane.getFluid(1400))
                .fluidOutputs(Hydrogen.getFluid(1340))
                .fluidOutputs(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedGas.getFluid(800))
                .output(dustTiny, Carbon)
                .fluidOutputs(Propene.getFluid(6))
                .fluidOutputs(Ethane.getFluid(6))
                .fluidOutputs(Ethylene.getFluid(20))
                .fluidOutputs(Methane.getFluid(914))
                .fluidOutputs(Helium.getFluid(16))
                .duration(96).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedButene.getFluid(750))
                .fluidOutputs(Butane.getFluid(250))
                .fluidOutputs(Propene.getFluid(250))
                .fluidOutputs(Ethane.getFluid(250))
                .fluidOutputs(Methane.getFluid(250))
                .duration(90).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedButene.getFluid(2000))
                .output(dust, Carbon, 3)
                .fluidOutputs(Propene.getFluid(250))
                .fluidOutputs(Ethylene.getFluid(625))
                .fluidOutputs(Methane.getFluid(3000))
                .duration(240).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedButadiene.getFluid(750))
                .fluidOutputs(Butene.getFluid(500))
                .fluidOutputs(Ethylene.getFluid(500))
                .duration(90).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedButadiene.getFluid(2000))
                .output(dust, Carbon, 2)
                .fluidOutputs(Propene.getFluid(250))
                .fluidOutputs(Ethylene.getFluid(375))
                .fluidOutputs(Methane.getFluid(2250))
                .duration(240).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(OilLight.getFluid(150))
                .fluidOutputs(SulfuricHeavyFuel.getFluid(10))
                .fluidOutputs(SulfuricLightFuel.getFluid(20))
                .fluidOutputs(SulfuricNaphtha.getFluid(30))
                .fluidOutputs(SulfuricGas.getFluid(240))
                .duration(20).EUt(96).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(OilMedium.getFluid(100))
                .fluidOutputs(SulfuricHeavyFuel.getFluid(15))
                .fluidOutputs(SulfuricLightFuel.getFluid(50))
                .fluidOutputs(SulfuricNaphtha.getFluid(20))
                .fluidOutputs(SulfuricGas.getFluid(60))
                .duration(20).EUt(96).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(OilHeavy.getFluid(150))
                .fluidOutputs(SulfuricHeavyFuel.getFluid(250))
                .fluidOutputs(SulfuricLightFuel.getFluid(45))
                .fluidOutputs(SulfuricNaphtha.getFluid(15))
                .fluidOutputs(SulfuricGas.getFluid(60))
                .duration(20).EUt(288).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(Oil.getFluid(50))
                .fluidOutputs(SulfuricHeavyFuel.getFluid(15))
                .fluidOutputs(SulfuricLightFuel.getFluid(50))
                .fluidOutputs(SulfuricNaphtha.getFluid(20))
                .fluidOutputs(SulfuricGas.getFluid(60))
                .duration(20).EUt(96).buildAndRegister();

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
                .outputs(new ItemStack(Items.DYE, 1, 15))
                .fluidOutputs(AceticAcid.getFluid(25))
                .fluidOutputs(Water.getFluid(375))
                .fluidOutputs(Ethanol.getFluid(150))
                .fluidOutputs(Methanol.getFluid(150))
                .fluidOutputs(Ammonia.getFluid(100))
                .fluidOutputs(CarbonDioxide.getFluid(400))
                .fluidOutputs(Methane.getFluid(600))
                .duration(75).EUt(180).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(RefineryGas.getFluid(1000))
                .fluidOutputs(Butane.getFluid(60))
                .fluidOutputs(Propane.getFluid(70))
                .fluidOutputs(Ethane.getFluid(100))
                .fluidOutputs(Methane.getFluid(750))
                .fluidOutputs(Helium.getFluid(20))
                .duration(240).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(Biomass.getFluid(600))
                .output(dustSmall, Wood, 2)
                .fluidOutputs(Ethanol.getFluid(240))
                .fluidOutputs(Water.getFluid(240))
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

        DISTILLERY_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Toluene.getFluid(30))
                .fluidOutputs(LightFuel.getFluid(30))
                .duration(160).EUt(24).buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(HeavyFuel.getFluid(10))
                .fluidOutputs(Toluene.getFluid(4))
                .duration(16).EUt(24).buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(HeavyFuel.getFluid(10))
                .fluidOutputs(Benzene.getFluid(4))
                .duration(16).EUt(24).buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidInputs(HeavyFuel.getFluid(20))
                .fluidOutputs(Phenol.getFluid(5))
                .duration(32).EUt(24).buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .circuitMeta(5)
                .fluidInputs(OilLight.getFluid(300))
                .fluidOutputs(Oil.getFluid(100))
                .duration(16).EUt(24).buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .circuitMeta(5)
                .fluidInputs(OilMedium.getFluid(200))
                .fluidOutputs(Oil.getFluid(100))
                .duration(16).EUt(24).buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .circuitMeta(5)
                .fluidInputs(OilHeavy.getFluid(100))
                .fluidOutputs(Oil.getFluid(100))
                .duration(16).EUt(24).buildAndRegister();

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
    }
}
