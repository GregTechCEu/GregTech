package gregtech.loaders.recipe;

import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials.Color;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.BlockBatteryPart.BatteryPartType;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.MetaBlocks.BATTERY_BLOCK;
import static gregtech.common.items.MetaItems.*;

public class BatteryRecipes {

    public static void init() {
        standardBatteries();
        gemBatteries();
        batteryBlocks();
    }

    private static void standardBatteries() {
        // Tantalum Battery (since it doesn't fit elsewhere)
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Tantalum)
                .inputItem(foil, Manganese)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputItem(BATTERY_ULV_TANTALUM, 8)
                .duration(30).volts(4).buildAndRegister();

        // :trol:
        ModHandler.addShapedRecipe("tantalum_capacitor", BATTERY_ULV_TANTALUM.getStackForm(2),
                " F ", "FDF", "B B",
                'F', new UnificationEntry(foil, Manganese),
                'D', new UnificationEntry(dust, Tantalum),
                'B', new UnificationEntry(bolt, Iron));

        // Battery Hull Recipes

        // LV
        ModHandler.addShapedRecipe("battery_hull_lv", BATTERY_HULL_LV.getStackForm(),
                "C", "P", "P",
                'C', new UnificationEntry(cableGtSingle, Tin),
                'P', new UnificationEntry(plate, BatteryAlloy));

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtSingle, Tin)
                .inputItem(plate, BatteryAlloy)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputItem(BATTERY_HULL_LV)
                .duration(400).volts(1).buildAndRegister();

        // MV
        ModHandler.addShapedRecipe("battery_hull_mv", BATTERY_HULL_MV.getStackForm(),
                "C C", "PPP", "PPP",
                'C', new UnificationEntry(cableGtSingle, Copper),
                'P', new UnificationEntry(plate, BatteryAlloy));

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtSingle, Copper, 2)
                .inputItem(plate, BatteryAlloy, 3)
                .fluidInputs(Polyethylene.getFluid(L * 3))
                .outputItem(BATTERY_HULL_MV)
                .duration(200).volts(2).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtSingle, AnnealedCopper, 2)
                .inputItem(plate, BatteryAlloy, 3)
                .fluidInputs(Polyethylene.getFluid(L * 3))
                .outputItem(BATTERY_HULL_MV)
                .duration(200).volts(2).buildAndRegister();

        // HV
        ASSEMBLER_RECIPES.recipeBuilder().duration(300).volts(4)
                .inputItem(cableGtSingle, Gold, 4)
                .inputItem(plate, BatteryAlloy, 9)
                .fluidInputs(Polyethylene.getFluid(1296))
                .outputItem(BATTERY_HULL_HV)
                .buildAndRegister();

        // EV
        ASSEMBLER_RECIPES.recipeBuilder().duration(100).volts(VA[HV])
                .inputItem(cableGtSingle, Aluminium, 2)
                .inputItem(plate, RedSteel, 2)
                .fluidInputs(Polytetrafluoroethylene.getFluid(144))
                .outputItem(BATTERY_HULL_SMALL_VANADIUM)
                .buildAndRegister();

        // IV
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).volts(VA[EV])
                .inputItem(cableGtSingle, Platinum, 2)
                .inputItem(plate, RoseGold, 6)
                .fluidInputs(Polytetrafluoroethylene.getFluid(288))
                .outputItem(BATTERY_HULL_MEDIUM_VANADIUM)
                .buildAndRegister();

        // LuV
        ASSEMBLER_RECIPES.recipeBuilder().duration(300).volts(VA[IV])
                .inputItem(cableGtSingle, NiobiumTitanium, 2)
                .inputItem(plate, BlueSteel, 18)
                .fluidInputs(Polybenzimidazole.getFluid(144))
                .outputItem(BATTERY_HULL_LARGE_VANADIUM)
                .buildAndRegister();

        // ZPM
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).volts(VA[LuV])
                .inputItem(cableGtSingle, Naquadah, 2)
                .inputItem(plate, Europium, 6)
                .fluidInputs(Polybenzimidazole.getFluid(288))
                .outputItem(BATTERY_HULL_MEDIUM_NAQUADRIA)
                .buildAndRegister();

        // UV
        ASSEMBLER_RECIPES.recipeBuilder().duration(300).volts(VA[ZPM])
                .inputItem(cableGtSingle, YttriumBariumCuprate, 2)
                .inputItem(plate, Americium, 18)
                .fluidInputs(Polybenzimidazole.getFluid(576))
                .outputItem(BATTERY_HULL_LARGE_NAQUADRIA)
                .buildAndRegister();

        // Battery Filling Recipes

        // LV
        CANNER_RECIPES.recipeBuilder().duration(100).volts(2)
                .inputItem(BATTERY_HULL_LV)
                .inputItem(dust, Cadmium, 2)
                .outputItem(BATTERY_LV_CADMIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(100).volts(2)
                .inputItem(BATTERY_HULL_LV)
                .inputItem(dust, Lithium, 2)
                .outputItem(BATTERY_LV_LITHIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(100).volts(2)
                .inputItem(BATTERY_HULL_LV)
                .inputItem(dust, Sodium, 2)
                .outputItem(BATTERY_LV_SODIUM)
                .buildAndRegister();

        // MV
        CANNER_RECIPES.recipeBuilder().duration(400).volts(2)
                .inputItem(BATTERY_HULL_MV)
                .inputItem(dust, Cadmium, 8)
                .outputItem(BATTERY_MV_CADMIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(400).volts(2)
                .inputItem(BATTERY_HULL_MV)
                .inputItem(dust, Lithium, 8)
                .outputItem(BATTERY_MV_LITHIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(400).volts(2)
                .inputItem(BATTERY_HULL_MV)
                .inputItem(dust, Sodium, 8)
                .outputItem(BATTERY_MV_SODIUM)
                .buildAndRegister();

        // HV
        CANNER_RECIPES.recipeBuilder().duration(1600).volts(2)
                .inputItem(BATTERY_HULL_HV)
                .inputItem(dust, Cadmium, 16)
                .outputItem(BATTERY_HV_CADMIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(1600).volts(2)
                .inputItem(BATTERY_HULL_HV)
                .inputItem(dust, Lithium, 16)
                .outputItem(BATTERY_HV_LITHIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(1600).volts(2)
                .inputItem(BATTERY_HULL_HV)
                .inputItem(dust, Sodium, 16)
                .outputItem(BATTERY_HV_SODIUM)
                .buildAndRegister();

        // EV
        CANNER_RECIPES.recipeBuilder().duration(100).volts(VA[HV])
                .inputItem(BATTERY_HULL_SMALL_VANADIUM)
                .inputItem(dust, Vanadium, 2)
                .outputItem(BATTERY_EV_VANADIUM)
                .buildAndRegister();

        // IV
        CANNER_RECIPES.recipeBuilder().duration(150).volts(1024)
                .inputItem(BATTERY_HULL_MEDIUM_VANADIUM)
                .inputItem(dust, Vanadium, 8)
                .outputItem(BATTERY_IV_VANADIUM)
                .buildAndRegister();

        // LuV
        CANNER_RECIPES.recipeBuilder().duration(200).volts(VA[EV])
                .inputItem(BATTERY_HULL_LARGE_VANADIUM)
                .inputItem(dust, Vanadium, 16)
                .outputItem(BATTERY_LUV_VANADIUM)
                .buildAndRegister();

        // ZPM
        CANNER_RECIPES.recipeBuilder().duration(250).volts(4096)
                .inputItem(BATTERY_HULL_MEDIUM_NAQUADRIA)
                .inputItem(dust, Naquadria, 8)
                .outputItem(BATTERY_ZPM_NAQUADRIA)
                .buildAndRegister();

        // UV
        CANNER_RECIPES.recipeBuilder().duration(300).volts(VA[IV])
                .inputItem(BATTERY_HULL_LARGE_NAQUADRIA)
                .inputItem(dust, Naquadria, 16)
                .outputItem(BATTERY_UV_NAQUADRIA)
                .buildAndRegister();

        // Battery Recycling Recipes
        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_LV_CADMIUM)
                .outputItem(BATTERY_HULL_LV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_LV_LITHIUM)
                .outputItem(BATTERY_HULL_LV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_LV_SODIUM)
                .outputItem(BATTERY_HULL_LV).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_MV_CADMIUM)
                .outputItem(BATTERY_HULL_MV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_MV_LITHIUM)
                .outputItem(BATTERY_HULL_MV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_MV_SODIUM)
                .outputItem(BATTERY_HULL_MV).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_HV_CADMIUM)
                .outputItem(BATTERY_HULL_HV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_HV_LITHIUM)
                .outputItem(BATTERY_HULL_HV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_HV_SODIUM)
                .outputItem(BATTERY_HULL_HV).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_EV_VANADIUM)
                .outputItem(BATTERY_HULL_SMALL_VANADIUM).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_IV_VANADIUM)
                .outputItem(BATTERY_HULL_MEDIUM_VANADIUM).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_LUV_VANADIUM)
                .outputItem(BATTERY_HULL_LARGE_VANADIUM).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_ZPM_NAQUADRIA)
                .outputItem(BATTERY_HULL_MEDIUM_NAQUADRIA).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputItem(BATTERY_UV_NAQUADRIA)
                .outputItem(BATTERY_HULL_LARGE_NAQUADRIA).buildAndRegister();
    }

    private static void gemBatteries() {
        // Energy Crystal
        MIXER_RECIPES.recipeBuilder().duration(600).volts(VA[MV])
                .inputItem(dust, Redstone, 5)
                .inputItem(dust, Ruby, 4)
                .circuitMeta(1)
                .outputItem(ENERGIUM_DUST, 9)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(ENERGIUM_DUST, 9)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(ENERGIUM_CRYSTAL)
                .duration(1800).volts(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(ENERGIUM_DUST, 9)
                .fluidInputs(DistilledWater.getFluid(1000))
                .outputItem(ENERGIUM_CRYSTAL)
                .duration(1200).volts(320).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(ENERGIUM_DUST, 9)
                .fluidInputs(BlackSteel.getFluid(L * 2))
                .outputItem(ENERGIUM_CRYSTAL)
                .duration(300).volts(256).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(ENERGIUM_DUST, 9)
                .fluidInputs(RedSteel.getFluid(L / 2))
                .outputItem(ENERGIUM_CRYSTAL)
                .duration(150).volts(192).buildAndRegister();

        // Lapotron Crystal
        MIXER_RECIPES.recipeBuilder()
                .inputItem(ENERGIUM_DUST, 3)
                .inputItem(dust, Lapis, 2)
                .circuitMeta(2)
                .outputItem(dust, Lapotron, 5)
                .duration(200).volts(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(dust, Lapotron, 15)
                .fluidInputs(Water.getFluid(1000))
                .outputItem(gem, Lapotron)
                .duration(1800).volts(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(dust, Lapotron, 15)
                .fluidInputs(DistilledWater.getFluid(1000))
                .outputItem(gem, Lapotron)
                .duration(1200).volts(320).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(dust, Lapotron, 15)
                .fluidInputs(RedSteel.getFluid(L * 2))
                .outputItem(gem, Lapotron)
                .duration(300).volts(256).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(dust, Lapotron, 15)
                .fluidInputs(BlueSteel.getFluid(L / 2))
                .outputItem(gem, Lapotron)
                .duration(150).volts(192).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(gem, Lapotron)
                .inputItem(circuit, Tier.HV, 2)
                .outputItem(LAPOTRON_CRYSTAL)
                .duration(600).volts(VA[EV]).buildAndRegister();

        // Lapotronic Energy Orb
        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .inputItem(LAPOTRON_CRYSTAL)
                .notConsumable(craftingLens, Color.Blue)
                .outputItem(ENGRAVED_LAPOTRON_CHIP, 3)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(256).volts(VA[HV]).buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(512).volts(1024)
                .inputItem(EXTREME_CIRCUIT_BOARD)
                .inputItem(POWER_INTEGRATED_CIRCUIT, 4)
                .inputItem(ENGRAVED_LAPOTRON_CHIP, 24)
                .inputItem(NANO_CENTRAL_PROCESSING_UNIT, 2)
                .inputItem(wireFine, Platinum, 16)
                .inputItem(plate, Platinum, 8)
                .outputItem(ENERGY_LAPOTRONIC_ORB)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // Lapotronic Energy Cluster
        ASSEMBLY_LINE_RECIPES.recipeBuilder().volts(80000).duration(1000)
                .inputItem(EXTREME_CIRCUIT_BOARD)
                .inputItem(plate, Europium, 8)
                .inputItem(circuit, Tier.LuV, 4)
                .inputItem(ENERGY_LAPOTRONIC_ORB)
                .inputItem(FIELD_GENERATOR_IV)
                .inputItem(HIGH_POWER_INTEGRATED_CIRCUIT, 16)
                .inputItem(ADVANCED_SMD_DIODE, 8)
                .inputItem(ADVANCED_SMD_CAPACITOR, 8)
                .inputItem(ADVANCED_SMD_RESISTOR, 8)
                .inputItem(ADVANCED_SMD_TRANSISTOR, 8)
                .inputItem(ADVANCED_SMD_INDUCTOR, 8)
                .inputItem(wireFine, Platinum, 64)
                .inputItem(bolt, Naquadah, 16)
                .fluidInputs(SolderingAlloy.getFluid(L * 5))
                .outputItem(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .scannerResearch(ENERGY_LAPOTRONIC_ORB.getStackForm())
                .buildAndRegister();

        // Energy Module
        ASSEMBLY_LINE_RECIPES.recipeBuilder().volts(100000).duration(1200)
                .inputItem(ELITE_CIRCUIT_BOARD)
                .inputItem(plateDouble, Europium, 8)
                .inputItem(circuit, Tier.ZPM, 4)
                .inputItem(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .inputItem(FIELD_GENERATOR_LuV)
                .inputItem(HIGH_POWER_INTEGRATED_CIRCUIT, 32)
                .inputItem(ADVANCED_SMD_DIODE, 12)
                .inputItem(ADVANCED_SMD_CAPACITOR, 12)
                .inputItem(ADVANCED_SMD_RESISTOR, 12)
                .inputItem(ADVANCED_SMD_TRANSISTOR, 12)
                .inputItem(ADVANCED_SMD_INDUCTOR, 12)
                .inputItem(wireFine, Ruridit, 64)
                .inputItem(bolt, Trinium, 16)
                .fluidInputs(SolderingAlloy.getFluid(L * 10))
                .outputItem(ENERGY_MODULE)
                .stationResearch(b -> b
                        .researchStack(ENERGY_LAPOTRONIC_ORB_CLUSTER.getStackForm())
                        .CWUt(16))
                .buildAndRegister();

        // Energy Cluster
        ASSEMBLY_LINE_RECIPES.recipeBuilder().volts(200000).duration(1400)
                .inputItem(WETWARE_CIRCUIT_BOARD)
                .inputItem(plate, Americium, 16)
                .inputItem(WETWARE_SUPER_COMPUTER_UV, 4)
                .inputItem(ENERGY_MODULE)
                .inputItem(FIELD_GENERATOR_ZPM)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 32)
                .inputItem(ADVANCED_SMD_DIODE, 16)
                .inputItem(ADVANCED_SMD_CAPACITOR, 16)
                .inputItem(ADVANCED_SMD_RESISTOR, 16)
                .inputItem(ADVANCED_SMD_TRANSISTOR, 16)
                .inputItem(ADVANCED_SMD_INDUCTOR, 16)
                .inputItem(wireFine, Osmiridium, 64)
                .inputItem(bolt, Naquadria, 16)
                .fluidInputs(SolderingAlloy.getFluid(L * 20))
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .outputItem(ENERGY_CLUSTER)
                .stationResearch(b -> b
                        .researchStack(ENERGY_MODULE.getStackForm())
                        .CWUt(96)
                        .EUt(VA[ZPM]))
                .buildAndRegister();

        // Ultimate Battery
        ASSEMBLY_LINE_RECIPES.recipeBuilder().volts(300000).duration(2000)
                .inputItem(plateDouble, Darmstadtium, 16)
                .inputItem(circuit, Tier.UHV, 4)
                .inputItem(ENERGY_CLUSTER, 16)
                .inputItem(FIELD_GENERATOR_UV, 4)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 64)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 64)
                .inputItem(ADVANCED_SMD_DIODE, 64)
                .inputItem(ADVANCED_SMD_CAPACITOR, 64)
                .inputItem(ADVANCED_SMD_RESISTOR, 64)
                .inputItem(ADVANCED_SMD_TRANSISTOR, 64)
                .inputItem(ADVANCED_SMD_INDUCTOR, 64)
                .inputItem(wireGtSingle, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItem(bolt, Neutronium, 64)
                .fluidInputs(SolderingAlloy.getFluid(L * 40))
                .fluidInputs(Polybenzimidazole.getFluid(2304))
                .fluidInputs(Naquadria.getFluid(L * 18))
                .outputItem(ULTIMATE_BATTERY)
                .stationResearch(b -> b
                        .researchStack(ENERGY_CLUSTER.getStackForm())
                        .CWUt(144)
                        .EUt(VA[UHV]))
                .buildAndRegister();
    }

    private static void batteryBlocks() {
        // Empty Tier I
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(frameGt, Ultimet)
                .inputItem(plate, Ultimet, 6)
                .inputItem(screw, Ultimet, 24)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_I))
                .duration(400).volts(VA[HV]).buildAndRegister();

        // Lapotronic EV
        CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_I))
                .inputItem(LAPOTRON_CRYSTAL)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_EV))
                .duration(200).volts(VA[HV]).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_EV))
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_I))
                .outputItem(LAPOTRON_CRYSTAL)
                .circuitMeta(2)
                .duration(200).volts(VA[LV]).buildAndRegister();

        // Lapotronic IV
        CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_I))
                .inputItem(ENERGY_LAPOTRONIC_ORB)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_IV))
                .duration(400).volts(VA[HV]).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_IV))
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_I))
                .outputItem(ENERGY_LAPOTRONIC_ORB)
                .circuitMeta(2)
                .duration(200).volts(VA[LV]).buildAndRegister();

        // Empty Tier II
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(frameGt, Ruridit)
                .inputItem(plate, Ruridit, 6)
                .inputItem(screw, Ruridit, 24)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_II))
                .duration(400).volts(VA[IV]).buildAndRegister();

        // Lapotronic LuV
        CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_II))
                .inputItem(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_LuV))
                .duration(200).volts(VA[EV]).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_LuV))
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_II))
                .outputItem(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .circuitMeta(2)
                .duration(200).volts(VA[LV]).buildAndRegister();

        // Lapotronic ZPM
        CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_II))
                .inputItem(ENERGY_MODULE)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_ZPM))
                .duration(400).volts(VA[EV]).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_ZPM))
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_II))
                .outputItem(ENERGY_MODULE)
                .circuitMeta(2)
                .duration(200).volts(VA[LV]).buildAndRegister();

        // Empty Tier III
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(frameGt, Neutronium)
                .inputItem(plate, Neutronium, 6)
                .inputItem(screw, Neutronium, 24)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_III))
                .duration(400).volts(VA[ZPM]).buildAndRegister();

        // Lapotronic UV
        CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_III))
                .inputItem(ENERGY_CLUSTER)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_UV))
                .duration(200).volts(VA[IV]).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_UV))
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_III))
                .outputItem(ENERGY_CLUSTER)
                .circuitMeta(2)
                .duration(200).volts(VA[LV]).buildAndRegister();

        // Ultimate UHV
        CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_III))
                .inputItem(ULTIMATE_BATTERY)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.ULTIMATE_UHV))
                .duration(400).volts(VA[IV]).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.ULTIMATE_UHV))
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_III))
                .outputItem(ULTIMATE_BATTERY)
                .circuitMeta(2)
                .duration(200).volts(VA[LV]).buildAndRegister();
    }
}
