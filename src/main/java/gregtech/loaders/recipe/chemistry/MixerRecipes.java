package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.MIXER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class MixerRecipes {

    public static void init() {
        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(NitricAcid.getFluid(1000))
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .fluidOutputs(NitrationMixture.getFluid(2000))
                .duration(500).EUt(2).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(PolyvinylAcetate.getFluid(1000))
                .fluidInputs(Acetone.getFluid(1500))
                .fluidOutputs(Glue.getFluid(2500))
                .duration(50).EUt(VA[ULV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(PolyvinylAcetate.getFluid(1000))
                .fluidInputs(MethylAcetate.getFluid(1500))
                .fluidOutputs(Glue.getFluid(2500))
                .duration(50).EUt(VA[ULV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .input(dust, Gallium)
                .input(dust, Arsenic)
                .output(dust, GalliumArsenide, 2)
                .duration(300).EUt(VA[LV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .input(dust, Salt, 2)
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(SaltWater.getFluid(1000))
                .duration(40).EUt(VA[ULV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(BioDiesel.getFluid(1000))
                .fluidInputs(Tetranitromethane.getFluid(40))
                .fluidOutputs(NitroDiesel.getFluid(750))
                .duration(20).EUt(VA[HV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(Diesel.getFluid(1000))
                .fluidInputs(Tetranitromethane.getFluid(20))
                .fluidOutputs(NitroDiesel.getFluid(1000))
                .duration(20).EUt(VA[HV]).buildAndRegister();

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
                .duration(16).EUt(VA[MV]).buildAndRegister();

        // Alloys
        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[EV])
                .input(dust, Yttrium)
                .input(dust, Barium, 2)
                .input(dust, Copper, 3)
                .notConsumable(new IntCircuitIngredient(2))
                .fluidInputs(Oxygen.getFluid(7000))
                .output(dust, YttriumBariumCuprate, 13)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(dust, Boron)
                .input(dust, Glass, 7)
                .notConsumable(new IntCircuitIngredient(2))
                .output(dust, BorosilicateGlass, 8)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(dust, Indium)
                .input(dust, Gallium)
                .input(dust, Phosphorus)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, IndiumGalliumPhosphide, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(dust, Nickel)
                .input(dust, Zinc)
                .input(dust, Iron, 4)
                .notConsumable(new IntCircuitIngredient(2))
                .output(dust, FerriteMixture, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(48)
                .input(dust, EnderPearl)
                .input(dust, Blaze)
                .notConsumable(new IntCircuitIngredient(2))
                .output(dust, EnderEye)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(dust, Gold)
                .input(dust, Silver)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Electrum, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).EUt(VA[ULV])
                .input(dust, Iron, 2)
                .input(dust, Nickel)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Invar, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[MV])
                .input(dust, Iron, 4)
                .input(dust, Invar, 3)
                .input(dust, Manganese)
                .input(dust, Chrome)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, StainlessSteel, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[MV])
                .input(dust, Iron, 6)
                .input(dust, Nickel)
                .input(dust, Manganese)
                .input(dust, Chrome)
                .notConsumable(new IntCircuitIngredient(2))
                .output(dust, StainlessSteel, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).EUt(VA[MV])
                .input(dust, Iron)
                .input(dust, Aluminium)
                .input(dust, Chrome)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Kanthal, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(VA[MV])
                .input(dust, Chrome, 4)
                .input(dust, Nickel)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Nichrome, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[ULV])
                .input(dust, Copper, 3)
                .input(dust, Zinc)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Brass, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[ULV])
                .input(dust, Copper, 3)
                .input(dust, Tin)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Bronze, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[ULV])
                .input(dust, Lead, 2)
                .input(dust, Bronze, 2)
                .input(dust, Tin, 1)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Potin, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(24)
                .input(dust, Copper)
                .input(dust, Nickel)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Cupronickel, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[MV])
                .input(dust, Copper)
                .input(dust, Gold, 4)
                .notConsumable(new IntCircuitIngredient(2))
                .output(dust, RoseGold, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(VA[MV])
                .input(dust, Copper)
                .input(dust, Silver, 4)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, SterlingSilver, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(VA[ULV])
                .input(dust, Copper, 3)
                .input(dust, Electrum, 2)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, BlackBronze, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(VA[ULV])
                .input(dust, Bismuth)
                .input(dust, Brass, 4)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, BismuthBronze, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(VA[ULV])
                .input(dust, BlackBronze)
                .input(dust, Nickel)
                .input(dust, Steel, 3)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, BlackSteel, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(800).EUt(VA[ULV])
                .input(dust, SterlingSilver)
                .input(dust, BismuthBronze)
                .input(dust, BlackSteel, 4)
                .input(dust, Steel, 2)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, RedSteel, 8)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(800).EUt(VA[ULV])
                .input(dust, RoseGold)
                .input(dust, Brass)
                .input(dust, BlackSteel, 4)
                .input(dust, Steel, 2)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, BlueSteel, 8)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV])
                .input(dust, Cobalt, 5)
                .input(dust, Chrome, 2)
                .input(dust, Nickel)
                .input(dust, Molybdenum)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Ultimet, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(900).EUt(VA[ULV])
                .input(dust, Brass, 7)
                .input(dust, Aluminium)
                .input(dust, Cobalt)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, CobaltBrass, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[ULV])
                .input(dust, Saltpeter, 2)
                .input(dust, Sulfur)
                .input(dust, Coal, 3)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Gunpowder, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[ULV])
                .input(dust, Saltpeter, 2)
                .input(dust, Sulfur)
                .input(dust, Charcoal, 3)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Gunpowder, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).EUt(VA[ULV])
                .input(dust, Saltpeter, 2)
                .input(dust, Sulfur)
                .input(dust, Carbon, 3)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Gunpowder, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(900)
                .input(dust, Electrum)
                .input(dust, NaquadahAlloy)
                .input(dust, BlueSteel)
                .input(dust, RedSteel)
                .notConsumable(new IntCircuitIngredient(2))
                .output(dust, FluxedElectrum, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(900)
                .input(dust, Americium, 2)
                .input(dust, Titanium)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, DiamericiumTitanium, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[ULV])
                .input(dust, Tin)
                .input(dust, Iron)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, TinAlloy, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[ULV])
                .input(dust, Tin, 9)
                .input(dust, Antimony)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, SolderingAlloy, 10)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[ULV])
                .input(dust, Lead, 4)
                .input(dust, Antimony)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, BatteryAlloy, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(4)
                .input(dust, Aluminium, 2)
                .input(dust, Magnesium)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Magnalium, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[LV])
                .input(dust, Vanadium)
                .input(dust, Chrome)
                .input(dust, Steel, 7)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, VanadiumSteel, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[HV])
                .input(dust, Tungsten)
                .input(dust, Steel)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, TungstenSteel, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(3500)
                .input(dust, TungstenSteel, 5)
                .input(dust, Chrome)
                .input(dust, Molybdenum, 2)
                .input(dust, Vanadium)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, HSSG, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(250).EUt(4000)
                .input(dust, HSSG, 6)
                .input(dust, Cobalt)
                .input(dust, Manganese)
                .input(dust, Silicon)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, HSSE, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).EUt(4500)
                .input(dust, HSSG, 6)
                .input(dust, Iridium, 2)
                .input(dust, Osmium)
                .notConsumable(new IntCircuitIngredient(2))
                .output(dust, HSSS, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(350).EUt(2500)
                .input(dust, Ruthenium, 2)
                .input(dust, Iridium)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Ruridit, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).EUt(VA[EV])
                .input(dust, Osmium)
                .input(dust, Iridium, 3)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Osmiridium, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[IV])
                .input(dust, Palladium, 3)
                .input(dust, Rhodium)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, RhodiumPlatedPalladium, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[IV])
                .input(dust, Naquadah, 2)
                .input(dust, Osmiridium)
                .input(dust, Trinium)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, NaquadahAlloy, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[HV])
                .input(dust, Graphite)
                .input(dust, Silicon)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, Graphene)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).EUt(24)
                .input(dust, Manganese)
                .input(dust, Phosphorus)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, ManganesePhosphide, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[MV])
                .input(dust, Magnesium)
                .input(dust, Boron, 2)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, MagnesiumDiboride, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[HV])
                .input(dust, Barium, 2)
                .input(dust, Calcium, 2)
                .input(dust, Copper, 3)
                .fluidInputs(Mercury.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(8000))
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, MercuryBariumCalciumCuprate, 16)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .input(dust, Uranium238)
                .input(dust, Platinum, 3)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, UraniumTriplatinum, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[IV])
                .input(dust, Samarium)
                .input(dust, Iron)
                .input(dust, Arsenic)
                .fluidInputs(Oxygen.getFluid(1000))
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, SamariumIronArsenicOxide, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[LuV])
                .input(dust, Indium, 4)
                .input(dust, Tin, 2)
                .input(dust, Barium, 2)
                .input(dust, Titanium)
                .input(dust, Copper, 7)
                .fluidInputs(Oxygen.getFluid(14000))
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, IndiumTinBariumTitaniumCuprate, 16)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(150).EUt(VA[ZPM])
                .input(dust, Uranium238)
                .input(dust, Rhodium)
                .input(dust, Naquadah, 2)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, UraniumRhodiumDinaquadide, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(175).EUt(VA[UV])
                .input(dust, NaquadahEnriched, 4)
                .input(dust, Trinium, 3)
                .input(dust, Europium, 2)
                .input(dust, Duranium)
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, EnrichedNaquadahTriniumEuropiumDuranide, 10)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[UV])
                .input(dust, Ruthenium)
                .input(dust, Trinium, 2)
                .input(dust, Americium)
                .input(dust, Neutronium, 2)
                .fluidInputs(Oxygen.getFluid(8000))
                .notConsumable(new IntCircuitIngredient(1))
                .output(dust, RutheniumTriniumAmericiumNeutronate, 14)
                .buildAndRegister();
    }
}
