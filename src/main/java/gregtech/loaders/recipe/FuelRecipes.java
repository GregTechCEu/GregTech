package gregtech.loaders.recipe;

import gregtech.api.recipes.RecipeMaps;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.Materials.*;

public class FuelRecipes {

    public static void registerFuels() {
        //diesel generator fuels
        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Naphtha.getFluid(1))
                .duration(10)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(SulfuricLightFuel.getFluid(4))
                .duration(5)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Methanol.getFluid(4))
                .duration(8)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Ethanol.getFluid(1))
                .duration(6)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Octane.getFluid(2))
                .duration(5)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(BioDiesel.getFluid(1))
                .duration(8)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(LightFuel.getFluid(1))
                .duration(10)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Diesel.getFluid(1))
                .duration(15)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(CetaneBoostedDiesel.getFluid(2))
                .duration(45)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(RocketFuel.getFluid(16))
                .duration(125)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Gasoline.getFluid(1))
                .duration(50)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(HighOctaneGasoline.getFluid(1))
                .duration(100)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Toluene.getFluid(1))
                .duration(10)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(OilLight.getFluid(32))
                .duration(5)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(RawOil.getFluid(64))
                .duration(15)
                .EUt((int) V[LV])
                .buildAndRegister();

        //steam generator fuels
        RecipeMaps.STEAM_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(Steam.getFluid(640))
                .fluidOutputs(DistilledWater.getFluid(4))
                .duration(10)
                .EUt((int) V[LV])
                .buildAndRegister();

        //gas turbine fuels
        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(NaturalGas.getFluid(8))
                .duration(5)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(WoodGas.getFluid(8))
                .duration(6)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(SulfuricGas.getFluid(32))
                .duration(25)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(SulfuricNaphtha.getFluid(4))
                .duration(5)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(CoalGas.getFluid(1))
                .duration(3)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(Methane.getFluid(2))
                .duration(7)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(Ethylene.getFluid(1))
                .duration(4)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(RefineryGas.getFluid(1))
                .duration(5)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(Ethane.getFluid(4))
                .duration(21)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(Propene.getFluid(1))
                .duration(6)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(Butadiene.getFluid(16))
                .duration(102)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(Propane.getFluid(4))
                .duration(29)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(Butene.getFluid(1))
                .duration(8)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(Phenol.getFluid(1))
                .duration(9)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(Benzene.getFluid(1))
                .duration(11)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(Butane.getFluid(4))
                .duration(37)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder()
                .fluidInputs(LPG.getFluid(1))
                .duration(10)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.GAS_TURBINE_FUELS.recipeBuilder() // TODO Too OP pls nerf
                .fluidInputs(Nitrobenzene.getFluid(1))
                .duration(40)
                .EUt((int) V[LV])
                .buildAndRegister();

        //semi-fluid fuels, like creosote
        RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Creosote.getFluid(16))
                .duration(1)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Biomass.getFluid(4))
                .duration(1)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Oil.getFluid(2))
                .duration(1)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(OilHeavy.getFluid(16))
                .duration(5)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(SulfuricHeavyFuel.getFluid(16))
                .duration(5)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(HeavyFuel.getFluid(8))
                .duration(15)
                .EUt((int) V[LV])
                .buildAndRegister();

        RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(FishOil.getFluid(16))
                .duration(1)
                .EUt((int) V[LV])
                .buildAndRegister();

        //plasma turbine
        RecipeMaps.PLASMA_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Helium.getPlasma(1))
                .fluidOutputs(Helium.getFluid(1))
                .duration(40)
                .EUt((int) V[EV])
                .buildAndRegister();

        RecipeMaps.PLASMA_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Oxygen.getPlasma(1))
                .fluidOutputs(Oxygen.getFluid(1))
                .duration(48)
                .EUt((int) V[EV])
                .buildAndRegister();

        RecipeMaps.PLASMA_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Nitrogen.getPlasma(1))
                .fluidOutputs(Nitrogen.getFluid(1))
                .duration(64)
                .EUt((int) V[EV])
                .buildAndRegister();

        RecipeMaps.PLASMA_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Iron.getPlasma(1))
                .fluidOutputs(Iron.getFluid(1))
                .duration(96)
                .EUt((int) V[EV])
                .buildAndRegister();

        RecipeMaps.PLASMA_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Nickel.getPlasma(1))
                .fluidOutputs(Nickel.getFluid(1))
                .duration(192)
                .EUt((int) V[EV])
                .buildAndRegister();
    }
}
