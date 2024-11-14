package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.UnificationEntry;

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
                .duration(500).volts(2).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(PolyvinylAcetate.getFluid(1000))
                .fluidInputs(Acetone.getFluid(1500))
                .fluidOutputs(Glue.getFluid(2500))
                .duration(50).volts(VA[ULV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(PolyvinylAcetate.getFluid(1000))
                .fluidInputs(MethylAcetate.getFluid(1500))
                .fluidOutputs(Glue.getFluid(2500))
                .duration(50).volts(VA[ULV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .inputItem(dust, Salt, 2)
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(SaltWater.getFluid(1000))
                .duration(40).volts(VA[ULV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(BioDiesel.getFluid(1000))
                .fluidInputs(Tetranitromethane.getFluid(40))
                .fluidOutputs(CetaneBoostedDiesel.getFluid(750))
                .duration(20).volts(VA[HV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(Diesel.getFluid(1000))
                .fluidInputs(Tetranitromethane.getFluid(20))
                .fluidOutputs(CetaneBoostedDiesel.getFluid(1000))
                .duration(20).volts(VA[HV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(Dimethylhydrazine.getFluid(1000))
                .fluidOutputs(RocketFuel.getFluid(3000))
                .duration(60).volts(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(DinitrogenTetroxide.getFluid(1000))
                .fluidInputs(Dimethylhydrazine.getFluid(1000))
                .fluidOutputs(RocketFuel.getFluid(6000))
                .duration(60).volts(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(LightFuel.getFluid(5000))
                .fluidInputs(HeavyFuel.getFluid(1000))
                .fluidOutputs(Diesel.getFluid(6000))
                .duration(16).volts(VA[MV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .inputItem(dust, Clay)
                .inputItem(dust, Stone, 3)
                .fluidInputs(Water.getFluid(500))
                .fluidOutputs(Concrete.getFluid(576))
                .duration(20).volts(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .inputItem(dust, Stone, 2)
                .inputItem(dust, Calcite)
                .inputItem(dust, Gypsum)
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(Concrete.getFluid(1152))
                .duration(40).volts(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .inputItem(dust, Stone, 2)
                .inputItem(dust, Marble)
                .inputItem(dust, Gypsum)
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(Concrete.getFluid(1152))
                .duration(40).volts(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(Concrete.getFluid(576))
                .inputItem(dust, RawRubber)
                .fluidOutputs(ConstructionFoam.getFluid(8000))
                .duration(20).volts(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .inputItem(dust, Stone)
                .fluidInputs(Lubricant.getFluid(20))
                .fluidInputs(Water.getFluid(4980))
                .fluidOutputs(DrillingFluid.getFluid(5000))
                .duration(64).volts(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .inputItem(dust, Stone)
                .fluidInputs(Lubricant.getFluid(20))
                .fluidInputs(DistilledWater.getFluid(4980))
                .fluidOutputs(DrillingFluid.getFluid(5000))
                .duration(48).volts(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(160).volts(VA[HV])
                .inputItem(dust, Beryllium)
                .inputItem(dust, Potassium, 4)
                .fluidInputs(Nitrogen.getFluid(5000))
                .circuitMeta(1)
                .outputItem(dust, EnderPearl, 10)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[HV])
                .fluidInputs(PolychlorinatedBiphenyl.getFluid(750))
                .fluidInputs(DistilledWater.getFluid(250))
                .fluidOutputs(PCBCoolant.getFluid(1000))
                .buildAndRegister();

        // Alloys
        ModHandler.addShapelessRecipe("dust_brass", OreDictUnifier.get(dust, Brass, 3),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Zinc));

        ModHandler.addShapelessRecipe("dust_bronze", OreDictUnifier.get(dust, Bronze, 3),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Tin));

        MIXER_RECIPES.recipeBuilder().duration(100).volts(VA[ULV])
                .inputItem(dust, Copper)
                .inputItem(dust, Redstone, 4)
                .circuitMeta(2)
                .outputItem(dust, RedAlloy)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).volts(VA[ULV])
                .inputItem(dust, Silver)
                .inputItem(dust, Electrotine, 4)
                .circuitMeta(2)
                .outputItem(dust, BlueAlloy)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[ULV])
                .inputItem(dust, Redstone)
                .inputItem(dust, Electrum)
                .circuitMeta(1)
                .outputItem(dust, Electrotine)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[ULV])
                .inputItem(dust, Tin, 6)
                .inputItem(dust, Lead, 3)
                .inputItem(dust, Antimony)
                .circuitMeta(3)
                .outputItem(dust, SolderingAlloy, 10)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[ULV])
                .inputItem(dust, Gallium)
                .inputItem(dust, Arsenic)
                .circuitMeta(1)
                .outputItem(dust, GalliumArsenide, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[ULV])
                .inputItem(dust, Gold)
                .inputItem(dust, Silver)
                .circuitMeta(1)
                .outputItem(dust, Electrum, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[ULV])
                .circuitMeta(1)
                .inputItem(dust, Boron)
                .inputItem(dust, Glass, 7)
                .outputItem(dust, BorosilicateGlass, 8)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[ULV])
                .inputItem(dust, Indium)
                .inputItem(dust, Gallium)
                .inputItem(dust, Phosphorus)
                .circuitMeta(1)
                .outputItem(dust, IndiumGalliumPhosphide, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).volts(VA[ULV])
                .inputItem(dust, Iron, 2)
                .inputItem(dust, Nickel)
                .circuitMeta(1)
                .outputItem(dust, Invar, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).volts(VA[ULV])
                .inputItem(dust, Copper, 3)
                .inputItem(dust, Tin)
                .circuitMeta(1)
                .outputItem(dust, Bronze, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).volts(VA[ULV])
                .inputItem(dust, Copper, 6)
                .inputItem(dust, Tin, 2)
                .inputItem(dust, Lead)
                .circuitMeta(3)
                .outputItem(dust, Potin, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).volts(VA[ULV])
                .inputItem(dust, Copper, 3)
                .inputItem(dust, Zinc)
                .circuitMeta(1)
                .outputItem(dust, Brass, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).volts(VA[ULV])
                .inputItem(dust, Bismuth)
                .inputItem(dust, Brass, 4)
                .circuitMeta(1)
                .outputItem(dust, BismuthBronze, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).volts(VA[ULV])
                .inputItem(dust, Copper, 3)
                .inputItem(dust, Electrum, 2)
                .circuitMeta(1)
                .outputItem(dust, BlackBronze, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).volts(VA[ULV])
                .inputItem(dust, BlackBronze)
                .inputItem(dust, Nickel)
                .inputItem(dust, Steel, 3)
                .circuitMeta(1)
                .outputItem(dust, BlackSteel, 5)
                .buildAndRegister();

        // Black Steel shortcut
        // Normal recipe would be 125 ticks per recipe at HV overclock, plus 125 ticks for the Black Bronze step,
        // for a total of 750 ticks. This recipe is 5 batches at once, so is equivalent time but saves a step.
        MIXER_RECIPES.recipeBuilder().duration(750).volts(VA[HV])
                .inputItem(dust, Copper, 3)
                .inputItem(dust, Gold)
                .inputItem(dust, Silver)
                .inputItem(dust, Nickel, 5)
                .inputItem(dust, Steel, 15)
                .circuitMeta(2)
                .outputItem(dust, BlackSteel, 25)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).volts(VA[ULV])
                .inputItem(dust, Saltpeter, 2)
                .inputItem(dust, Sulfur)
                .inputItem(dust, Coal, 3)
                .circuitMeta(1)
                .outputItem(dust, Gunpowder, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).volts(VA[ULV])
                .inputItem(dust, Saltpeter, 2)
                .inputItem(dust, Sulfur)
                .inputItem(dust, Charcoal, 3)
                .circuitMeta(1)
                .outputItem(dust, Gunpowder, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).volts(VA[ULV])
                .inputItem(dust, Saltpeter, 2)
                .inputItem(dust, Sulfur)
                .inputItem(dust, Carbon, 3)
                .circuitMeta(1)
                .outputItem(dust, Gunpowder, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(800).volts(VA[ULV])
                .inputItem(dust, RoseGold)
                .inputItem(dust, Brass)
                .inputItem(dust, BlackSteel, 4)
                .inputItem(dust, Steel, 2)
                .circuitMeta(1)
                .outputItem(dust, BlueSteel, 8)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(800).volts(VA[ULV])
                .inputItem(dust, SterlingSilver)
                .inputItem(dust, BismuthBronze)
                .inputItem(dust, BlackSteel, 4)
                .inputItem(dust, Steel, 2)
                .circuitMeta(1)
                .outputItem(dust, RedSteel, 8)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(900).volts(VA[ULV])
                .inputItem(dust, Brass, 7)
                .inputItem(dust, Aluminium)
                .inputItem(dust, Cobalt)
                .circuitMeta(1)
                .outputItem(dust, CobaltBrass, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(24)
                .inputItem(dust, Copper)
                .inputItem(dust, Nickel)
                .circuitMeta(1)
                .outputItem(dust, Cupronickel, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[MV])
                .inputItem(dust, Nickel)
                .inputItem(dust, Zinc)
                .inputItem(dust, Iron, 4)
                .circuitMeta(2)
                .outputItem(dust, FerriteMixture, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).volts(VA[MV])
                .inputItem(dust, Iron)
                .inputItem(dust, Aluminium)
                .inputItem(dust, Chrome)
                .circuitMeta(1)
                .outputItem(dust, Kanthal, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).volts(VA[MV])
                .inputItem(dust, Nickel, 4)
                .inputItem(dust, Chrome)
                .circuitMeta(2)
                .outputItem(dust, Nichrome, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).volts(VA[MV])
                .inputItem(dust, Copper)
                .inputItem(dust, Gold, 4)
                .circuitMeta(3)
                .outputItem(dust, RoseGold, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).volts(VA[MV])
                .inputItem(dust, Iron, 4)
                .inputItem(dust, Invar, 3)
                .inputItem(dust, Manganese)
                .inputItem(dust, Chrome)
                .circuitMeta(1)
                .outputItem(dust, StainlessSteel, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).volts(VA[MV])
                .inputItem(dust, Iron, 6)
                .inputItem(dust, Nickel)
                .inputItem(dust, Manganese)
                .inputItem(dust, Chrome)
                .circuitMeta(3)
                .outputItem(dust, StainlessSteel, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).volts(VA[HV])
                .inputItem(dust, Graphite)
                .inputItem(dust, Silicon)
                .inputItem(dust, Carbon, 4)
                .circuitMeta(1)
                .outputItem(dust, Graphene)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).volts(VA[MV])
                .inputItem(dust, Steel, 7)
                .inputItem(dust, Vanadium)
                .inputItem(dust, Chrome)
                .circuitMeta(1)
                .outputItem(dust, VanadiumSteel, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(900).volts(VA[HV])
                .inputItem(dust, Cobalt, 5)
                .inputItem(dust, Chrome, 2)
                .inputItem(dust, Nickel)
                .inputItem(dust, Molybdenum)
                .circuitMeta(1)
                .outputItem(dust, Ultimet, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[EV])
                .inputItem(dust, Tungsten)
                .inputItem(dust, Carbon)
                .circuitMeta(1)
                .outputItem(dust, TungstenCarbide, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[EV])
                .inputItem(dust, Tungsten)
                .inputItem(dust, Steel)
                .circuitMeta(1)
                .outputItem(dust, TungstenSteel, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).volts(VA[EV])
                .inputItem(dust, Vanadium, 3)
                .inputItem(dust, Gallium)
                .circuitMeta(1)
                .outputItem(dust, VanadiumGallium, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).volts(VA[EV])
                .inputItem(dust, TungstenSteel, 5)
                .inputItem(dust, Chrome)
                .inputItem(dust, Molybdenum, 2)
                .inputItem(dust, Vanadium)
                .circuitMeta(1)
                .outputItem(dust, HSSG, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).volts(VA[EV])
                .inputItem(dust, Yttrium)
                .inputItem(dust, Barium, 2)
                .inputItem(dust, Copper, 3)
                .circuitMeta(2)
                .fluidInputs(Oxygen.getFluid(7000))
                .outputItem(dust, YttriumBariumCuprate, 13)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(450).volts(4046)
                .inputItem(dust, HSSG, 6)
                .inputItem(dust, Cobalt)
                .inputItem(dust, Manganese)
                .inputItem(dust, Silicon)
                .circuitMeta(1)
                .outputItem(dust, HSSE, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[IV])
                .inputItem(dust, Niobium)
                .inputItem(dust, Titanium)
                .circuitMeta(1)
                .outputItem(dust, NiobiumTitanium, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).volts(VA[IV])
                .inputItem(dust, HSSG, 6)
                .inputItem(dust, Iridium, 2)
                .inputItem(dust, Osmium)
                .circuitMeta(2)
                .outputItem(dust, HSSS, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).volts(VA[IV])
                .inputItem(dust, Naquadah, 2)
                .inputItem(dust, Osmiridium)
                .inputItem(dust, Trinium)
                .circuitMeta(2)
                .outputItem(dust, NaquadahAlloy, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).volts(VA[LuV])
                .inputItem(dust, Osmium)
                .inputItem(dust, Iridium, 3)
                .circuitMeta(1)
                .outputItem(dust, Osmiridium, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).volts(VA[IV])
                .inputItem(dust, Palladium, 3)
                .inputItem(dust, Rhodium)
                .circuitMeta(1)
                .outputItem(dust, RhodiumPlatedPalladium, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).volts(VA[ULV])
                .inputItem(dust, Aluminium, 2)
                .inputItem(dust, Magnesium)
                .circuitMeta(1)
                .outputItem(dust, Magnalium, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).volts(VA[MV])
                .inputItem(dust, Copper)
                .inputItem(dust, Silver, 4)
                .circuitMeta(1)
                .outputItem(dust, SterlingSilver, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).volts(VA[ULV])
                .inputItem(dust, Tin)
                .inputItem(dust, Iron)
                .circuitMeta(1)
                .outputItem(dust, TinAlloy, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).volts(VA[ULV])
                .inputItem(dust, Lead, 4)
                .inputItem(dust, Antimony)
                .circuitMeta(1)
                .outputItem(dust, BatteryAlloy, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(350).volts(4096)
                .inputItem(dust, Ruthenium, 2)
                .inputItem(dust, Iridium)
                .circuitMeta(1)
                .outputItem(dust, Ruridit, 3)
                .buildAndRegister();

        // Superconductor Alloys
        MIXER_RECIPES.recipeBuilder().duration(400).volts(24)
                .inputItem(dust, Manganese)
                .inputItem(dust, Phosphorus)
                .circuitMeta(4)
                .outputItem(dust, ManganesePhosphide, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).volts(VA[MV])
                .inputItem(dust, Magnesium)
                .inputItem(dust, Boron, 2)
                .circuitMeta(4)
                .outputItem(dust, MagnesiumDiboride, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).volts(VA[MV])
                .inputItem(dust, Barium, 2)
                .inputItem(dust, Calcium, 2)
                .inputItem(dust, Copper, 3)
                .fluidInputs(Mercury.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(8000))
                .circuitMeta(4)
                .outputItem(dust, MercuryBariumCalciumCuprate, 16)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).volts(VA[EV])
                .inputItem(dust, Uranium)
                .inputItem(dust, Platinum, 3)
                .circuitMeta(4)
                .outputItem(dust, UraniumTriplatinum, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).volts(VA[IV])
                .inputItem(dust, Samarium)
                .inputItem(dust, Iron)
                .inputItem(dust, Arsenic)
                .fluidInputs(Oxygen.getFluid(1000))
                .circuitMeta(4)
                .outputItem(dust, SamariumIronArsenicOxide, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).volts(VA[LuV])
                .inputItem(dust, Indium, 4)
                .inputItem(dust, Tin, 2)
                .inputItem(dust, Barium, 2)
                .inputItem(dust, Titanium)
                .inputItem(dust, Copper, 7)
                .fluidInputs(Oxygen.getFluid(14000))
                .circuitMeta(4)
                .outputItem(dust, IndiumTinBariumTitaniumCuprate, 16)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(150).volts(VA[ZPM])
                .inputItem(dust, Uranium)
                .inputItem(dust, Rhodium)
                .inputItem(dust, Naquadah, 2)
                .circuitMeta(4)
                .outputItem(dust, UraniumRhodiumDinaquadide, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(175).volts(VA[UV])
                .inputItem(dust, NaquadahEnriched, 4)
                .inputItem(dust, Trinium, 3)
                .inputItem(dust, Europium, 2)
                .inputItem(dust, Duranium)
                .circuitMeta(4)
                .outputItem(dust, EnrichedNaquadahTriniumEuropiumDuranide, 10)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).volts(VA[UV])
                .inputItem(dust, Ruthenium)
                .inputItem(dust, Trinium, 2)
                .inputItem(dust, Americium)
                .inputItem(dust, Neutronium, 2)
                .fluidInputs(Oxygen.getFluid(8000))
                .circuitMeta(4)
                .outputItem(dust, RutheniumTriniumAmericiumNeutronate, 14)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).volts(VA[EV])
                .inputItem(dust, Ruthenium, 4)
                .inputItem(dust, Tungsten, 2)
                .inputItem(dust, Molybdenum)
                .circuitMeta(1)
                .outputItem(dust, RTMAlloy, 7)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).volts(VA[EV])
                .inputItem(dust, Zirconium, 16)
                .inputItem(dust, Tin, 2)
                .inputItem(dust, Chrome, 1)
                .circuitMeta(1)
                .outputItem(dust, Zircaloy4, 19)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).volts(VA[EV])
                .inputItem(dust, Nickel, 5)
                .inputItem(dust, Chrome, 2)
                .inputItem(dust, Iron, 2)
                .inputItem(dust, Niobium)
                .inputItem(dust, Molybdenum)
                .circuitMeta(4)
                .outputItem(dust, Inconel718, 11)
                .buildAndRegister();
    }
}
