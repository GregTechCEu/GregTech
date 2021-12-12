package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.CHEMICAL_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class ReactorRecipes {

    public static void init() {

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Isoprene.getFluid(144))
                .fluidInputs(Air.getFluid(2000))
                .output(dust, RawRubber)
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Isoprene.getFluid(144))
                .fluidInputs(Oxygen.getFluid(2000))
                .output(dust, RawRubber, 3)
                .duration(160).EUt(VA[LV]).buildAndRegister();

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

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Propene.getFluid(2000))
                .fluidOutputs(Methane.getFluid(1000))
                .fluidOutputs(Isoprene.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .input(dust, Carbon)
                .fluidInputs(Hydrogen.getFluid(4000))
                .fluidOutputs(Methane.getFluid(1000))
                .duration(3500).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Ethylene.getFluid(1000))
                .fluidInputs(Propene.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .fluidOutputs(Isoprene.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Sodium, 2)
                .input(dust, Sulfur)
                .output(dust, SodiumSulfide, 3)
                .duration(60).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumSulfide, 3)
                .fluidInputs(Dichlorobenzene.getFluid(1000))
                .fluidInputs(Air.getFluid(16000))
                .output(dust, Salt, 4)
                .fluidOutputs(PolyphenyleneSulfide.getFluid(1000))
                .duration(240).EUt(360).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumSulfide, 3)
                .fluidInputs(Dichlorobenzene.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(8000))
                .output(dust, Salt, 4)
                .fluidOutputs(PolyphenyleneSulfide.getFluid(1500))
                .duration(240).EUt(360).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Air.getFluid(1000))
                .fluidInputs(Ethylene.getFluid(144))
                .fluidOutputs(Polyethylene.getFluid(144))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(Ethylene.getFluid(144))
                .fluidOutputs(Polyethylene.getFluid(216))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Air.getFluid(7500))
                .fluidInputs(Ethylene.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(Polyethylene.getFluid(3240))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Oxygen.getFluid(7500))
                .fluidInputs(Ethylene.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(Polyethylene.getFluid(4320))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Air.getFluid(1000))
                .fluidInputs(VinylChloride.getFluid(144))
                .fluidOutputs(PolyvinylChloride.getFluid(144))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(VinylChloride.getFluid(144))
                .fluidOutputs(PolyvinylChloride.getFluid(216))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Air.getFluid(7500))
                .fluidInputs(VinylChloride.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(PolyvinylChloride.getFluid(3240))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Oxygen.getFluid(7500))
                .fluidInputs(VinylChloride.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(PolyvinylChloride.getFluid(4320))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Polydimethylsiloxane, 9)
                .input(dust, Sulfur)
                .fluidOutputs(SiliconeRubber.getFluid(1296))
                .duration(600).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(HydrochloricAcid.getFluid(1000))
                .fluidInputs(Acetone.getFluid(1000))
                .fluidInputs(Phenol.getFluid(2000))
                .fluidOutputs(BisphenolA.getFluid(1000))
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfurTrioxide.getFluid(1000))
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .duration(320).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Air.getFluid(1000))
                .fluidInputs(Tetrafluoroethylene.getFluid(144))
                .fluidOutputs(Polytetrafluoroethylene.getFluid(144))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(Tetrafluoroethylene.getFluid(144))
                .fluidOutputs(Polytetrafluoroethylene.getFluid(216))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Air.getFluid(7500))
                .fluidInputs(Tetrafluoroethylene.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(Polytetrafluoroethylene.getFluid(3240))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Oxygen.getFluid(7500))
                .fluidInputs(Tetrafluoroethylene.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(Polytetrafluoroethylene.getFluid(4320))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumHydroxide, 3)
                .fluidInputs(Epichlorohydrin.getFluid(1000))
                .fluidInputs(BisphenolA.getFluid(1000))
                .fluidOutputs(Epoxy.getFluid(1000))
                .fluidOutputs(SaltWater.getFluid(1000))
                .duration(200).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Carbon, 2)
                .input(dust, Rutile)
                .fluidInputs(Chlorine.getFluid(4000))
                .fluidOutputs(CarbonMonoxide.getFluid(2000))
                .fluidOutputs(TitaniumTetrachloride.getFluid(1000))
                .duration(400).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Dimethyldichlorosilane.getFluid(1000))
                .fluidInputs(Water.getFluid(1000))
                .output(dust, Polydimethylsiloxane, 3)
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(1000))
                .duration(240).EUt(96).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Silicon)
                .fluidInputs(HydrochloricAcid.getFluid(2000))
                .fluidInputs(Methanol.getFluid(2000))
                .output(dust, Polydimethylsiloxane, 3)
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(2000))
                .duration(480).EUt(96).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .input(dust, Silicon)
                .fluidInputs(Water.getFluid(1000))
                .fluidInputs(Chlorine.getFluid(4000))
                .fluidInputs(Methane.getFluid(2000))
                .output(dust, Polydimethylsiloxane, 3)
                .fluidOutputs(HydrochloricAcid.getFluid(2000))
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(2000))
                .duration(480).EUt(96).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Chlorine.getFluid(1000))
                .fluidInputs(Hydrogen.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .duration(60).EUt(VA[ULV]).buildAndRegister();

        // NaCl + H2SO4 -> NaHSO4 + HCl
        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Salt, 2)
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(dust, SodiumBisulfate, 7)
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .duration(60).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Iron)
                .fluidInputs(Chlorine.getFluid(3000))
                .notConsumable(new IntCircuitIngredient(1))
                .fluidOutputs(Iron3Chloride.getFluid(1000))
                .duration(400).EUt(VA[LV])
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Chlorine.getFluid(6000))
                .fluidInputs(Methane.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(3000))
                .fluidOutputs(Chloroform.getFluid(1000))
                .duration(80).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(3))
                .fluidInputs(Chlorine.getFluid(2000))
                .fluidInputs(Methane.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(Chloromethane.getFluid(1000))
                .duration(80).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Chlorine.getFluid(4000))
                .fluidInputs(Benzene.getFluid(1000))
                .notConsumable(new IntCircuitIngredient(2))
                .fluidOutputs(HydrochloricAcid.getFluid(2000))
                .fluidOutputs(Dichlorobenzene.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Propene.getFluid(1000))
                .fluidInputs(Chlorine.getFluid(2000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(AllylChloride.getFluid(1000))
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
                .input(dust, Apatite, 9)
                .fluidInputs(SulfuricAcid.getFluid(5000))
                .fluidInputs(Water.getFluid(10000))
                .output(dust, Gypsum, 40)
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(PhosphoricAcid.getFluid(3000))
                .duration(320).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfurDioxide.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(SulfurTrioxide.getFluid(1000))
                .duration(200).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(3))
                .input(dust, Sulfur)
                .fluidInputs(Oxygen.getFluid(3000))
                .fluidOutputs(SulfurTrioxide.getFluid(1000))
                .duration(280).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Chloroform.getFluid(2000))
                .fluidInputs(HydrofluoricAcid.getFluid(4000))
                .fluidOutputs(HydrochloricAcid.getFluid(6000))
                .fluidOutputs(Tetrafluoroethylene.getFluid(1000))
                .duration(240).EUt(256).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .input(dust, Sodium)
                .fluidInputs(Water.getFluid(1000))
                .output(dust, SodiumHydroxide, 3)
                .fluidOutputs(Hydrogen.getFluid(1000))
                .duration(80).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(3))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(HydrochloricAcid.getFluid(1000))
                .fluidInputs(Ethylene.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .fluidOutputs(VinylChloride.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidInputs(Cumene.getFluid(1000))
                .fluidOutputs(Phenol.getFluid(1000))
                .fluidOutputs(Acetone.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(NitrationMixture.getFluid(3000))
                .fluidInputs(Glycerol.getFluid(1000))
                .fluidOutputs(GlycerylTrinitrate.getFluid(1000))
                .fluidOutputs(DilutedSulfuricAcid.getFluid(3000))
                .duration(180).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .fluidInputs(AceticAcid.getFluid(1000))
                .fluidOutputs(Ethenone.getFluid(1000))
                .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
                .duration(160).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Calcite, 5)
                .fluidInputs(AceticAcid.getFluid(2000))
                .fluidOutputs(DissolvedCalciumAcetate.getFluid(1000))
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(200).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Quicklime, 2)
                .fluidInputs(AceticAcid.getFluid(2000))
                .fluidOutputs(DissolvedCalciumAcetate.getFluid(1000))
                .duration(400).EUt(380).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Calcium)
                .fluidInputs(AceticAcid.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(DissolvedCalciumAcetate.getFluid(1000))
                .duration(400).EUt(380).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Methanol.getFluid(1000))
                .fluidInputs(AceticAcid.getFluid(1000))
                .fluidOutputs(MethylAcetate.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .duration(240).EUt(VA[LV]).buildAndRegister();

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

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Sulfur)
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .duration(60).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricLightFuel.getFluid(12000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .fluidOutputs(LightFuel.getFluid(12000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricHeavyFuel.getFluid(8000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .fluidOutputs(HeavyFuel.getFluid(8000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricNaphtha.getFluid(12000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .fluidOutputs(Naphtha.getFluid(12000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricGas.getFluid(16000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .fluidOutputs(RefineryGas.getFluid(16000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(NaturalGas.getFluid(16000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .fluidOutputs(RefineryGas.getFluid(16000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Nitrogen.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidOutputs(NitrogenDioxide.getFluid(1000))
                .duration(1250).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(NitricOxide.getFluid(1000))
                .fluidOutputs(NitrogenDioxide.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Air.getFluid(1000))
                .fluidInputs(VinylAcetate.getFluid(144))
                .fluidOutputs(PolyvinylAcetate.getFluid(144))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(VinylAcetate.getFluid(144))
                .fluidOutputs(PolyvinylAcetate.getFluid(216))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Air.getFluid(7500))
                .fluidInputs(VinylAcetate.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(PolyvinylAcetate.getFluid(3240))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Oxygen.getFluid(7500))
                .fluidInputs(VinylAcetate.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(PolyvinylAcetate.getFluid(4320))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(6000))
                .fluidInputs(CarbonDioxide.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .fluidOutputs(Methanol.getFluid(1000))
                .duration(120).EUt(96).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Hydrogen.getFluid(4000))
                .fluidInputs(CarbonMonoxide.getFluid(1000))
                .fluidOutputs(Methanol.getFluid(1000))
                .duration(120).EUt(96).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(3))
                .input(dust, Carbon)
                .fluidInputs(Hydrogen.getFluid(4000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(Methanol.getFluid(1000))
                .duration(320).EUt(96).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Mercury.getFluid(1000))
                .fluidInputs(Water.getFluid(10000))
                .fluidInputs(Chlorine.getFluid(10000))
                .fluidOutputs(HypochlorousAcid.getFluid(10000))
                .duration(600).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Water.getFluid(1000))
                .fluidInputs(Chlorine.getFluid(2000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .fluidOutputs(HypochlorousAcid.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Dimethylamine.getFluid(1000))
                .fluidInputs(Monochloramine.getFluid(1000))
                .fluidOutputs(Dimethylhydrazine.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .duration(960).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Methanol.getFluid(2000))
                .fluidInputs(Ammonia.getFluid(2000))
                .fluidInputs(HypochlorousAcid.getFluid(1000))
                .fluidOutputs(Dimethylhydrazine.getFluid(1000))
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(2000))
                .duration(1040).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .input(dust, Sulfur)
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .duration(60).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Oxygen.getFluid(3000))
                .fluidInputs(HydrogenSulfide.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(1000))
                .fluidInputs(Fluorine.getFluid(1000))
                .fluidOutputs(HydrofluoricAcid.getFluid(1000))
                .duration(60).EUt(VA[ULV]).buildAndRegister();

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
                .fluidInputs(PhosphoricAcid.getFluid(1000))
                .fluidInputs(Benzene.getFluid(8000))
                .fluidInputs(Propene.getFluid(8000))
                .fluidOutputs(Cumene.getFluid(8000))
                .duration(1920).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Silicon)
                .fluidInputs(Chloromethane.getFluid(2000))
                .fluidOutputs(Dimethyldichlorosilane.getFluid(1000))
                .duration(240).EUt(96).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidInputs(Ethylene.getFluid(1000))
                .fluidOutputs(AceticAcid.getFluid(1000))
                .duration(100).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(CarbonMonoxide.getFluid(1000))
                .fluidInputs(Methanol.getFluid(1000))
                .fluidOutputs(AceticAcid.getFluid(1000))
                .duration(300).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Hydrogen.getFluid(4000))
                .fluidInputs(CarbonMonoxide.getFluid(2000))
                .fluidOutputs(AceticAcid.getFluid(1000))
                .duration(320).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(4))
                .input(dust, Carbon, 2)
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidInputs(Hydrogen.getFluid(4000))
                .fluidOutputs(AceticAcid.getFluid(1000))
                .duration(480).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Aluminium, 4)
                .fluidInputs(IndiumConcentrate.getFluid(1000))
                .output(dustSmall, Indium)
                .fluidOutputs(LeadZincSolution.getFluid(1000))
                .duration(50).EUt(600).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(NitrogenDioxide.getFluid(3000))
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(NitricOxide.getFluid(1000))
                .fluidOutputs(NitricAcid.getFluid(2000))
                .duration(240).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Oxygen.getFluid(5000))
                .fluidInputs(Ammonia.getFluid(2000))
                .fluidOutputs(NitricOxide.getFluid(2000))
                .fluidOutputs(Water.getFluid(3000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(3))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(AceticAcid.getFluid(1000))
                .fluidInputs(Ethylene.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .fluidOutputs(VinylAcetate.getFluid(1000))
                .duration(180).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .input(dust, Carbon)
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(CarbonMonoxide.getFluid(1000))
                .duration(40).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .input(gem, Charcoal)
                .fluidInputs(Oxygen.getFluid(1000))
                .output(dustTiny, Ash)
                .fluidOutputs(CarbonMonoxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .input(gem, Coal)
                .fluidInputs(Oxygen.getFluid(1000))
                .output(dustTiny, Ash)
                .fluidOutputs(CarbonMonoxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .input(dust, Charcoal)
                .fluidInputs(Oxygen.getFluid(1000))
                .output(dustTiny, Ash)
                .fluidOutputs(CarbonMonoxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .duration(80).EUt(VA[ULV])
                .input(dust, Coal)
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Oxygen.getFluid(1000))
                .outputs(OreDictUnifier.get(dustTiny, Ash))
                .fluidOutputs(CarbonMonoxide.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Carbon)
                .fluidInputs(CarbonDioxide.getFluid(1000))
                .fluidOutputs(CarbonMonoxide.getFluid(2000))
                .duration(800).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Hydrogen.getFluid(3000))
                .fluidInputs(Nitrogen.getFluid(1000))
                .fluidOutputs(Ammonia.getFluid(1000))
                .duration(320).EUt(384).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(HypochlorousAcid.getFluid(1000))
                .fluidInputs(Ammonia.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .fluidOutputs(Monochloramine.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Ammonia.getFluid(1000))
                .fluidInputs(Methanol.getFluid(2000))
                .fluidOutputs(Water.getFluid(2000))
                .fluidOutputs(Dimethylamine.getFluid(1000))
                .duration(240).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, PhosphorusPentoxide, 14)
                .fluidInputs(Water.getFluid(6000))
                .fluidOutputs(PhosphoricAcid.getFluid(4000))
                .duration(40).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .input(dust, Phosphorus, 2)
                .fluidInputs(Water.getFluid(3000))
                .fluidInputs(Oxygen.getFluid(5000))
                .fluidOutputs(PhosphoricAcid.getFluid(2000))
                .duration(320).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(HydrochloricAcid.getFluid(1000))
                .fluidInputs(Methanol.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .fluidOutputs(Chloromethane.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .input(dust, Phosphorus, 4)
                .fluidInputs(Oxygen.getFluid(10000))
                .output(dust, PhosphorusPentoxide, 14)
                .duration(40).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .input(dust, Carbon)
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(40).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .input(gem, Charcoal)
                .fluidInputs(Oxygen.getFluid(2000))
                .output(dustTiny, Ash)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .input(gem, Coal)
                .fluidInputs(Oxygen.getFluid(2000))
                .output(dustTiny, Ash)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .input(dust, Charcoal)
                .fluidInputs(Oxygen.getFluid(2000))
                .output(dustTiny, Ash)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .input(dust, Coal)
                .fluidInputs(Oxygen.getFluid(2000))
                .output(dustTiny, Ash)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .fluidInputs(Water.getFluid(2000))
                .fluidInputs(Methane.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(8000))
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(150).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(MethylAcetate.getFluid(2000))
                .fluidInputs(NitricAcid.getFluid(4000))
                .output(dust, Carbon, 5)
                .fluidOutputs(Tetranitromethane.getFluid(1000))
                .fluidOutputs(Water.getFluid(8000))
                .duration(480).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(NitricAcid.getFluid(8000))
                .fluidInputs(Ethenone.getFluid(1000))
                .fluidOutputs(Tetranitromethane.getFluid(2000))
                .fluidOutputs(Water.getFluid(5000))
                .duration(480).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(3))
                .fluidInputs(Oxygen.getFluid(7000))
                .fluidInputs(Ammonia.getFluid(2000))
                .fluidOutputs(DinitrogenTetroxide.getFluid(1000))
                .fluidOutputs(Water.getFluid(3000))
                .duration(480).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(NitrogenDioxide.getFluid(2000))
                .fluidOutputs(DinitrogenTetroxide.getFluid(1000))
                .duration(640).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(3))
                .fluidInputs(Oxygen.getFluid(7000))
                .fluidInputs(Nitrogen.getFluid(2000))
                .fluidInputs(Hydrogen.getFluid(6000))
                .fluidOutputs(DinitrogenTetroxide.getFluid(1000))
                .fluidOutputs(Water.getFluid(3000))
                .duration(1100).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Oxygen.getFluid(4000))
                .fluidInputs(Ammonia.getFluid(1000))
                .fluidOutputs(NitricAcid.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .duration(480).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(3))
                .fluidInputs(Water.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(NitrogenDioxide.getFluid(2000))
                .fluidOutputs(NitricAcid.getFluid(2000))
                .duration(240).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Oxygen.getFluid(4000))
                .fluidInputs(Nitrogen.getFluid(1000))
                .fluidInputs(Hydrogen.getFluid(3000))
                .fluidOutputs(NitricAcid.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .duration(720).EUt(VA[HV]).buildAndRegister();

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

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .fluidInputs(Ethanol.getFluid(1000))
                .fluidOutputs(Ethylene.getFluid(1000))
                .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
                .duration(1200).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumBisulfate, 7)
                .fluidInputs(Water.getFluid(1000))
                .output(dust, SodiumHydroxide, 3)
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .duration(60).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.SUGAR, 9))
                .input(dust, Polyethylene)
                .fluidInputs(Toluene.getFluid(1000))
                .outputs(MetaItems.GELLED_TOLUENE.getStackForm(20))
                .duration(140).EUt(192).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(HydrogenSulfide.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(4000))
                .fluidOutputs(SulfuricAcid.getFluid(1000))
                .duration(320).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Saltpeter)
                .fluidInputs(Naphtha.getFluid(576))
                .output(dustTiny, Potassium)
                .fluidOutputs(Polycaprolactam.getFluid(1296))
                .duration(640).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Epichlorohydrin.getFluid(144))
                .fluidInputs(Naphtha.getFluid(3000))
                .fluidInputs(NitrogenDioxide.getFluid(1000))
                .fluidOutputs(Epoxy.getFluid(288))
                .duration(240).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Calcium)
                .input(dust, Carbon)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, Calcite, 5)
                .duration(500).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Quicklime, 2)
                .fluidInputs(CarbonDioxide.getFluid(1000))
                .output(dust, Calcite, 5)
                .duration(80).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Magnesia, 2)
                .fluidInputs(CarbonDioxide.getFluid(1000))
                .output(dust, Magnesite, 5)
                .duration(80).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .input(dust, Calcite, 5)
                .output(dust, Quicklime, 2)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(240).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Magnesite, 5)
                .output(dust, Magnesia, 2)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(240).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, RawRubber, 9)
                .input(dust, Sulfur)
                .fluidOutputs(Rubber.getFluid(1296))
                .duration(600).EUt(16).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.MELON, 1, OreDictionary.WILDCARD_VALUE))
                .input(nugget, Gold, 8)
                .outputs(new ItemStack(Items.SPECKLED_MELON))
                .duration(50).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.CARROT, 1, OreDictionary.WILDCARD_VALUE))
                .input(nugget, Gold, 8)
                .outputs(new ItemStack(Items.GOLDEN_CARROT))
                .duration(50).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.APPLE, 1, OreDictionary.WILDCARD_VALUE))
                .input(ingot, Gold, 8)
                .outputs(new ItemStack(Items.GOLDEN_APPLE))
                .duration(50).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.APPLE, 1, OreDictionary.WILDCARD_VALUE))
                .input(block, Gold, 8)
                .outputs(new ItemStack(Items.GOLDEN_APPLE, 1, 1))
                .duration(50).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.BLAZE_POWDER))
                .inputs(new ItemStack(Items.SLIME_BALL))
                .outputs(new ItemStack(Items.MAGMA_CREAM))
                .duration(50).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.BLAZE_POWDER))
                .input(OrePrefix.gem, Materials.EnderPearl)
                .outputs(new ItemStack(Items.ENDER_EYE))
                .duration(50).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(MetaItems.GELLED_TOLUENE.getStackForm(4))
                .fluidInputs(SulfuricAcid.getFluid(250))
                .outputs(new ItemStack(Blocks.TNT))
                .duration(200).EUt(24).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumHydroxide, 6)
                .fluidInputs(Dichlorobenzene.getFluid(1000))
                .output(dust, Salt, 4)
                .fluidOutputs(Phenol.getFluid(1000))
                .fluidOutputs(Oxygen.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Benzene.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(Phenol.getFluid(1000))
                .duration(400).EUt(2000).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Glycerol.getFluid(1000))
                .fluidInputs(CarbonDioxide.getFluid(1000))
                .fluidOutputs(Ethylene.getFluid(2000))
                .fluidOutputs(Oxygen.getFluid(5000))
                .duration(400).EUt(200).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(MethylAcetate.getFluid(1000))
                .fluidInputs(Water.getFluid(1000))
                .notConsumable(OreDictUnifier.get(dust, SodiumHydroxide))
                .fluidOutputs(AceticAcid.getFluid(1000))
                .fluidOutputs(Methanol.getFluid(1000))
                .duration(264).EUt(60).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(ingot, Plutonium239, 8)
                .input(dust, Uranium238)
                .fluidInputs(Air.getFluid(10000))
                .output(dust, Plutonium239, 8)
                .fluidOutputs(Radon.getFluid(1000))
                .duration(100000).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(Items.PAPER)
                .input(Items.STRING)
                .fluidInputs(GlycerylTrinitrate.getFluid(500))
                .output(MetaItems.DYNAMITE)
                .duration(160).EUt(4).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Niobium)
                .fluidInputs(Nitrogen.getFluid(1000))
                .output(dust, NiobiumNitride, 2)
                .duration(200).EUt(VA[HV]).buildAndRegister();

        // Dyes
        for (int i = 0; i < Materials.CHEMICAL_DYES.length; i++) {
            CHEMICAL_RECIPES.recipeBuilder()
                    .input(dye, MarkerMaterials.Color.VALUES[i])
                    .input(dust, Salt, 2)
                    .fluidInputs(SulfuricAcid.getFluid(250))
                    .fluidOutputs(Materials.CHEMICAL_DYES[i].getFluid(288))
                    .duration(600).EUt(24).buildAndRegister();
        }
    }
}
