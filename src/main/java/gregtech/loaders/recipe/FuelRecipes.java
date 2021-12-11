package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipes.FuelRecipe;
import net.minecraftforge.fluids.FluidStack;

import static gregtech.api.GTValues.LV;
import static gregtech.api.unification.material.Materials.*;

public class FuelRecipes {

    public static void registerFuels() {
        //diesel generator fuels
        registerCombustionGeneratorFuel(Naphtha.getFluid(1), 8, LV);
        registerCombustionGeneratorFuel(Oil.getFluid(2), 1, LV);
        registerCombustionGeneratorFuel(SulfuricLightFuel.getFluid(4), 5, LV);
        registerCombustionGeneratorFuel(Methanol.getFluid(8), 21, LV);
        registerCombustionGeneratorFuel(Ethanol.getFluid(1), 6, LV);
        registerCombustionGeneratorFuel(Octane.getFluid(2), 5, LV);
        registerCombustionGeneratorFuel(BioDiesel.getFluid(1), 8, LV);
        registerCombustionGeneratorFuel(LightFuel.getFluid(32), 305, LV);
        registerCombustionGeneratorFuel(Diesel.getFluid(1), 15, LV);
        registerCombustionGeneratorFuel(CetaneBoostedDiesel.getFluid(2), 45, LV);
        registerCombustionGeneratorFuel(RocketFuel.getFluid(2), 7, LV);
        registerCombustionGeneratorFuel(Gasoline.getFluid(1), 24, LV);
        registerCombustionGeneratorFuel(HighOctaneGasoline.getFluid(1), 68, LV);

        RecipeMaps.COMBUSTION_GENERATOR_FUELS_2.recipeBuilder()
                .fluidInputs(Diesel.getFluid(1))
                .boostFluid(Air.getFluid(2000), 20, 3)
                .boostFluid(Lubricant.getFluid(1), 20, -1)
                .duration(15)
                .EUt(32)
                .buildAndRegister();

        //steam generator fuels
        registerSteamGeneratorFuel(Steam.getFluid(640), 10, LV);

        //gas turbine fuels
        registerGasGeneratorFuel(NaturalGas.getFluid(8), 5, LV);
        registerGasGeneratorFuel(Hydrogen.getFluid(8), 5, LV);
        registerGasGeneratorFuel(CarbonMonoxide.getFluid(8), 6, LV);
        registerGasGeneratorFuel(WoodGas.getFluid(8), 6, LV);
        registerGasGeneratorFuel(SulfuricGas.getFluid(32), 25, LV);
        registerGasGeneratorFuel(SulfuricNaphtha.getFluid(4), 5, LV);
        registerGasGeneratorFuel(Methane.getFluid(4), 14, LV);
        registerGasGeneratorFuel(Ethylene.getFluid(1), 4, LV);
        registerGasGeneratorFuel(RefineryGas.getFluid(1), 5, LV);
        registerGasGeneratorFuel(Ethane.getFluid(4), 21, LV);
        registerGasGeneratorFuel(Propene.getFluid(1), 6, LV);
        registerGasGeneratorFuel(Butadiene.getFluid(16), 103, LV);
        registerGasGeneratorFuel(Propane.getFluid(4), 29, LV);
        registerGasGeneratorFuel(Butene.getFluid(1), 8, LV);
        registerGasGeneratorFuel(Phenol.getFluid(1), 9, LV);
        registerGasGeneratorFuel(Benzene.getFluid(1), 11, LV);
        registerGasGeneratorFuel(Butane.getFluid(4), 37, LV);
        registerGasGeneratorFuel(LPG.getFluid(1), 10, LV);
        registerGasGeneratorFuel(Naphtha.getFluid(1), 10, LV);
        registerGasGeneratorFuel(Toluene.getFluid(4), 41, LV);
        registerGasGeneratorFuel(RocketFuel.getFluid(16), 125, LV);
        registerGasGeneratorFuel(Nitrobenzene.getFluid(1), 40, LV); // TODO Too OP pls nerf

        //semi-fluid fuels, like creosote
        registerSemiFluidGeneratorFuel(Creosote.getFluid(16), 1, LV);
        registerSemiFluidGeneratorFuel(Biomass.getFluid(16), 1, LV);
        registerSemiFluidGeneratorFuel(OilLight.getFluid(32), 5, LV);
        registerSemiFluidGeneratorFuel(RawOil.getFluid(64), 15, LV);
        registerSemiFluidGeneratorFuel(OilHeavy.getFluid(16), 5, LV);
        registerSemiFluidGeneratorFuel(SulfuricHeavyFuel.getFluid(16), 5, LV);
        registerSemiFluidGeneratorFuel(HeavyFuel.getFluid(8), 15, LV);
        registerSemiFluidGeneratorFuel(FishOil.getFluid(8), 1, LV);

        //plasma turbine
        registerPlasmaFuel(Helium.getPlasma(1), 2560, LV);
        registerPlasmaFuel(Oxygen.getPlasma(1), 3072, LV);
        registerPlasmaFuel(Nitrogen.getPlasma(1), 4096, LV);
        registerPlasmaFuel(Iron.getPlasma(1), 6144, LV);
        registerPlasmaFuel(Nickel.getPlasma(1), 12288, LV);

    }

    public static void registerPlasmaFuel(FluidStack fuelStack, int duration, int tier) {
        RecipeMaps.PLASMA_GENERATOR_FUELS.addRecipe(new FuelRecipe(fuelStack, duration, GTValues.V[tier]));
    }

    public static void registerCombustionGeneratorFuel(FluidStack fuelStack, int duration, int tier) {
        RecipeMaps.COMBUSTION_GENERATOR_FUELS.addRecipe(new FuelRecipe(fuelStack, duration, GTValues.V[tier]));
    }

    public static void registerSteamGeneratorFuel(FluidStack fuelStack, int duration, int tier) {
        RecipeMaps.STEAM_TURBINE_FUELS.addRecipe(new FuelRecipe(fuelStack, duration, GTValues.V[tier]));
    }

    public static void registerGasGeneratorFuel(FluidStack fuelStack, int duration, int tier) {
        RecipeMaps.GAS_TURBINE_FUELS.addRecipe(new FuelRecipe(fuelStack, duration, GTValues.V[tier]));
    }

    public static void registerSemiFluidGeneratorFuel(FluidStack fuelStack, int duration, int tier) {
        RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.addRecipe(new FuelRecipe(fuelStack, duration, GTValues.V[tier]));
    }
}
