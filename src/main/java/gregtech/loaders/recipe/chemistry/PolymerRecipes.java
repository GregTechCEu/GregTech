package gregtech.loaders.recipe.chemistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class PolymerRecipes {

    public static void init() {
        polyethyleneProcess();
        polyvinylChlorideProcess();
        ptfeProcess();
        epoxyProcess();
        styreneButadieneProcess();
        polybenzimidazoleProcess();
        polycaprolactamProcess();
    }

    private static void polyethyleneProcess() {
        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .fluidInputs(Ethanol.getFluid(1000))
                .fluidOutputs(Ethylene.getFluid(1000))
                .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
                .duration(1200).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Glycerol.getFluid(1000))
                .fluidInputs(CarbonDioxide.getFluid(1000))
                .fluidOutputs(Ethylene.getFluid(2000))
                .fluidOutputs(Oxygen.getFluid(5000))
                .duration(400).EUt(200).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Air.getFluid(1000))
                .fluidInputs(Ethylene.getFluid(144))
                .fluidOutputs(Polyethylene.getFluid(144))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(Ethylene.getFluid(144))
                .fluidOutputs(Polyethylene.getFluid(216))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidInputs(Air.getFluid(7500))
                .fluidInputs(Ethylene.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(Polyethylene.getFluid(3240))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidInputs(Oxygen.getFluid(7500))
                .fluidInputs(Ethylene.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(Polyethylene.getFluid(4320))
                .duration(800).EUt(VA[LV]).buildAndRegister();
    }

    private static void polyvinylChlorideProcess() {
        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(HydrochloricAcid.getFluid(1000))
                .fluidInputs(Ethylene.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .fluidOutputs(VinylChloride.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Chlorine.getFluid(2000))
                .fluidInputs(Ethylene.getFluid(1000))
                .fluidOutputs(VinylChloride.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Chlorine.getFluid(4000))
                .fluidInputs(Ethane.getFluid(1000))
                .fluidOutputs(VinylChloride.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(3000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Air.getFluid(1000))
                .fluidInputs(VinylChloride.getFluid(144))
                .fluidOutputs(PolyvinylChloride.getFluid(144))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(VinylChloride.getFluid(144))
                .fluidOutputs(PolyvinylChloride.getFluid(216))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(Air.getFluid(7500))
                .fluidInputs(VinylChloride.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(PolyvinylChloride.getFluid(3240))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(Oxygen.getFluid(7500))
                .fluidInputs(VinylChloride.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(PolyvinylChloride.getFluid(4320))
                .duration(800).EUt(VA[LV]).buildAndRegister();
    }

    private static void ptfeProcess() {
        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Chlorine.getFluid(6000))
                .fluidInputs(Methane.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(3000))
                .fluidOutputs(Chloroform.getFluid(1000))
                .duration(80).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Chloroform.getFluid(2000))
                .fluidInputs(HydrofluoricAcid.getFluid(4000))
                .fluidOutputs(HydrochloricAcid.getFluid(6000))
                .fluidOutputs(Tetrafluoroethylene.getFluid(1000))
                .duration(480).EUt(240).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(24)
                .fluidInputs(HydrofluoricAcid.getFluid(4000))
                .fluidInputs(Methane.getFluid(2000))
                .fluidInputs(Chlorine.getFluid(12000))
                .fluidOutputs(Tetrafluoroethylene.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(12000))
                .duration(540).EUt(VA[IV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Air.getFluid(1000))
                .fluidInputs(Tetrafluoroethylene.getFluid(144))
                .fluidOutputs(Polytetrafluoroethylene.getFluid(144))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(Tetrafluoroethylene.getFluid(144))
                .fluidOutputs(Polytetrafluoroethylene.getFluid(216))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(Air.getFluid(7500))
                .fluidInputs(Tetrafluoroethylene.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(Polytetrafluoroethylene.getFluid(3240))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(Oxygen.getFluid(7500))
                .fluidInputs(Tetrafluoroethylene.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(Polytetrafluoroethylene.getFluid(4320))
                .duration(800).EUt(VA[LV]).buildAndRegister();
    }

    private static void epoxyProcess() {
        CHEMICAL_RECIPES.recipeBuilder()
                .input(dustTiny, SodiumHydroxide)
                .fluidInputs(SeedOil.getFluid(6000))
                .fluidInputs(Methanol.getFluid(1000))
                .fluidOutputs(Glycerol.getFluid(1000))
                .fluidOutputs(BioDiesel.getFluid(6000))
                .duration(600).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dustTiny, SodiumHydroxide)
                .fluidInputs(SeedOil.getFluid(6000))
                .fluidInputs(Ethanol.getFluid(1000))
                .fluidOutputs(Glycerol.getFluid(1000))
                .fluidOutputs(BioDiesel.getFluid(6000))
                .duration(600).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dustTiny, SodiumHydroxide)
                .fluidInputs(FishOil.getFluid(6000))
                .fluidInputs(Methanol.getFluid(1000))
                .fluidOutputs(Glycerol.getFluid(1000))
                .fluidOutputs(BioDiesel.getFluid(6000))
                .duration(600).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dustTiny, SodiumHydroxide)
                .fluidInputs(FishOil.getFluid(6000))
                .fluidInputs(Ethanol.getFluid(1000))
                .fluidOutputs(Glycerol.getFluid(1000))
                .fluidOutputs(BioDiesel.getFluid(6000))
                .duration(600).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumHydroxide)
                .fluidInputs(SeedOil.getFluid(54000))
                .fluidInputs(Methanol.getFluid(9000))
                .fluidOutputs(Glycerol.getFluid(9000))
                .fluidOutputs(BioDiesel.getFluid(54000))
                .duration(5400).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumHydroxide)
                .fluidInputs(SeedOil.getFluid(54000))
                .fluidInputs(Ethanol.getFluid(9000))
                .fluidOutputs(Glycerol.getFluid(9000))
                .fluidOutputs(BioDiesel.getFluid(54000))
                .duration(5400).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumHydroxide)
                .fluidInputs(FishOil.getFluid(54000))
                .fluidInputs(Methanol.getFluid(9000))
                .fluidOutputs(Glycerol.getFluid(9000))
                .fluidOutputs(BioDiesel.getFluid(54000))
                .duration(5400).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumHydroxide)
                .fluidInputs(FishOil.getFluid(54000))
                .fluidInputs(Ethanol.getFluid(9000))
                .fluidOutputs(Glycerol.getFluid(9000))
                .fluidOutputs(BioDiesel.getFluid(54000))
                .duration(5400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Propene.getFluid(1000))
                .fluidInputs(Chlorine.getFluid(2000))
                .circuitMeta(1)
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(AllylChloride.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Glycerol.getFluid(1000))
                .fluidInputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .fluidOutputs(Epichlorohydrin.getFluid(1000))
                .duration(480).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumHydroxide, 3)
                .fluidInputs(AllylChloride.getFluid(1000))
                .fluidInputs(HypochlorousAcid.getFluid(1000))
                .fluidOutputs(SaltWater.getFluid(1000))
                .fluidOutputs(Epichlorohydrin.getFluid(1000))
                .duration(480).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(23)
                .fluidInputs(Chlorine.getFluid(4000))
                .fluidInputs(Propene.getFluid(1000))
                .fluidInputs(Water.getFluid(1000))
                .input(dust, SodiumHydroxide, 3)
                .fluidOutputs(Epichlorohydrin.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(2000))
                .fluidOutputs(SaltWater.getFluid(1000))
                .duration(640).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(24)
                .fluidInputs(Chlorine.getFluid(2000))
                .fluidInputs(Propene.getFluid(1000))
                .fluidInputs(HypochlorousAcid.getFluid(1000))
                .input(dust, SodiumHydroxide, 3)
                .fluidOutputs(Epichlorohydrin.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(SaltWater.getFluid(1000))
                .duration(640).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidInputs(Cumene.getFluid(1000))
                .fluidOutputs(Phenol.getFluid(1000))
                .fluidOutputs(Acetone.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(HydrochloricAcid.getFluid(1000))
                .fluidInputs(Acetone.getFluid(1000))
                .fluidInputs(Phenol.getFluid(2000))
                .fluidOutputs(BisphenolA.getFluid(1000))
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumHydroxide, 3)
                .fluidInputs(Epichlorohydrin.getFluid(1000))
                .fluidInputs(BisphenolA.getFluid(1000))
                .fluidOutputs(Epoxy.getFluid(1000))
                .fluidOutputs(SaltWater.getFluid(1000))
                .duration(200).EUt(VA[LV]).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(24)
                .fluidInputs(Epichlorohydrin.getFluid(1000))
                .fluidInputs(Phenol.getFluid(2000))
                .fluidInputs(Acetone.getFluid(1000))
                .fluidInputs(HydrochloricAcid.getFluid(1000))
                .input(dust, SodiumHydroxide, 3)
                .fluidOutputs(Epoxy.getFluid(1000))
                .fluidOutputs(SaltWater.getFluid(1000))
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(1000))
                .duration(480).EUt(VA[LV]).buildAndRegister();
    }

    private static void styreneButadieneProcess() {
        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Ethylene.getFluid(1000))
                .fluidInputs(Benzene.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .fluidOutputs(Styrene.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Ethylbenzene.getFluid(1000))
                .fluidOutputs(Styrene.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(30).EUt(VA[LV])
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Butadiene.getFluid(3000))
                .fluidInputs(Styrene.getFluid(1000))
                .fluidInputs(Air.getFluid(15000))
                .output(dust, RawStyreneButadieneRubber, 27)
                .duration(480).EUt(240).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Butadiene.getFluid(3000))
                .fluidInputs(Styrene.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(15000))
                .output(dust, RawStyreneButadieneRubber, 41)
                .duration(480).EUt(240).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, RawStyreneButadieneRubber, 9)
                .input(dust, Sulfur)
                .fluidOutputs(StyreneButadieneRubber.getFluid(1296))
                .duration(600).EUt(VA[LV]).buildAndRegister();
    }

    private static void polybenzimidazoleProcess() {
        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[IV]).duration(100)
                .fluidInputs(Diaminobenzidine.getFluid(1000))
                .fluidInputs(DiphenylIsophtalate.getFluid(1000))
                .fluidOutputs(Phenol.getFluid(1000))
                .fluidOutputs(Polybenzimidazole.getFluid(1008))
                .buildAndRegister();

        // 3,3-Diaminobenzidine
        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[IV]).duration(100)
                .fluidInputs(Dichlorobenzidine.getFluid(1000))
                .fluidInputs(Ammonia.getFluid(2000))
                .notConsumable(dust, Zinc)
                .fluidOutputs(Diaminobenzidine.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(2000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[EV]).duration(200)
                .input(dustTiny, Copper)
                .fluidInputs(Nitrochlorobenzene.getFluid(2000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(Dichlorobenzidine.getFluid(1000))
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[EV]).duration(1800)
                .input(dust, Copper)
                .fluidInputs(Nitrochlorobenzene.getFluid(18000))
                .fluidInputs(Hydrogen.getFluid(18000))
                .fluidOutputs(Dichlorobenzidine.getFluid(9000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(100)
                .fluidInputs(NitrationMixture.getFluid(2000))
                .fluidInputs(Chlorobenzene.getFluid(1000))
                .fluidOutputs(Nitrochlorobenzene.getFluid(1000))
                .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(240)
                .fluidInputs(Chlorine.getFluid(2000))
                .fluidInputs(Benzene.getFluid(1000))
                .circuitMeta(1)
                .fluidOutputs(Chlorobenzene.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .buildAndRegister();

        // Diphenyl Isophthalate
        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[IV]).duration(100)
                .fluidInputs(Phenol.getFluid(2000))
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .fluidInputs(PhthalicAcid.getFluid(1000))
                .fluidOutputs(DiphenylIsophtalate.getFluid(1000))
                .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[EV]).duration(100)
                .input(dustTiny, PotassiumDichromate)
                .fluidInputs(Dimethylbenzene.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidOutputs(PhthalicAcid.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000))
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[EV]).duration(900)
                .input(dust, PotassiumDichromate)
                .fluidInputs(Dimethylbenzene.getFluid(9000))
                .fluidInputs(Oxygen.getFluid(18000))
                .fluidOutputs(PhthalicAcid.getFluid(9000))
                .fluidOutputs(Water.getFluid(18000))
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(125)
                .fluidInputs(Naphthalene.getFluid(2000))
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .input(dustTiny, Potassium)
                .fluidOutputs(PhthalicAcid.getFluid(2500))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(1125)
                .fluidInputs(Naphthalene.getFluid(18000))
                .fluidInputs(SulfuricAcid.getFluid(9000))
                .input(dust, Potassium)
                .fluidOutputs(PhthalicAcid.getFluid(22500))
                .fluidOutputs(HydrogenSulfide.getFluid(9000))
                .buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder().EUt(VA[MV]).duration(4000)
                .fluidInputs(Methane.getFluid(2000))
                .fluidInputs(Benzene.getFluid(1000))
                .circuitMeta(1)
                .fluidOutputs(Dimethylbenzene.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(100)
                .input(dust, Saltpeter, 10)
                .input(dust, ChromiumTrioxide, 8)
                .output(dust, PotassiumDichromate, 11)
                .fluidOutputs(NitrogenDioxide.getFluid(2000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(60).duration(100)
                .input(dust, Chrome)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, ChromiumTrioxide, 4)
                .buildAndRegister();
    }

    public static void polycaprolactamProcess() {
        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(400)
                .notConsumable(dust, Nickel)
                .fluidInputs(Benzene.getFluid(1000))
                .fluidInputs(Hydrogen.getFluid(6000))
                .fluidOutputs(Cyclohexane.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(100)
                .fluidInputs(Chlorine.getFluid(1000))
                .fluidInputs(NitricOxide.getFluid(1000))
                .fluidOutputs(NitrosylChloride.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[MV]).duration(100)
                .fluidInputs(Cyclohexane.getFluid(1000))
                .fluidInputs(NitrosylChloride.getFluid(1000))
                .output(dust, CyclohexanoneOxime, 19)
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(200)
                .input(dust, CyclohexanoneOxime, 19)
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(dust, Caprolactam, 19)
                .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().EUt(VA[MV]).duration(150).blastFurnaceTemp(533)
                .input(dust, Caprolactam, 1)
                .fluidInputs(Nitrogen.getFluid(1000))
                .output(ingot, Polycaprolactam, 1)
                .buildAndRegister();
    }
}
