package gregtech.loaders.recipe;

import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
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
                .input(dust, Tantalum)
                .input(foil, Manganese)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(BATTERY_ULV_TANTALUM, 8)
                .duration(30).EUt(4).buildAndRegister();

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
                .input(cableGtSingle, Tin)
                .input(plate, BatteryAlloy)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(BATTERY_HULL_LV)
                .duration(400).EUt(1).buildAndRegister();

        // MV
        ModHandler.addShapedRecipe("battery_hull_mv", BATTERY_HULL_MV.getStackForm(),
                "C C", "PPP", "PPP",
                'C', new UnificationEntry(cableGtSingle, Copper),
                'P', new UnificationEntry(plate, BatteryAlloy));

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(cableGtSingle, Copper, 2)
                .input(plate, BatteryAlloy, 3)
                .fluidInputs(Polyethylene.getFluid(L * 3))
                .output(BATTERY_HULL_MV)
                .duration(200).EUt(2).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(cableGtSingle, AnnealedCopper, 2)
                .input(plate, BatteryAlloy, 3)
                .fluidInputs(Polyethylene.getFluid(L * 3))
                .output(BATTERY_HULL_MV)
                .duration(200).EUt(2).buildAndRegister();

        // HV
        ASSEMBLER_RECIPES.recipeBuilder().duration(300).EUt(4)
                .input(cableGtSingle, Gold, 4)
                .input(plate, BatteryAlloy, 9)
                .fluidInputs(Polyethylene.getFluid(1296))
                .output(BATTERY_HULL_HV)
                .buildAndRegister();

        // EV
        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[HV])
                .input(cableGtSingle, Aluminium, 2)
                .input(plate, BlueSteel, 2)
                .fluidInputs(Polytetrafluoroethylene.getFluid(144))
                .output(BATTERY_HULL_SMALL_VANADIUM)
                .buildAndRegister();

        // IV
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .input(cableGtSingle, Platinum, 2)
                .input(plate, RoseGold, 6)
                .fluidInputs(Polytetrafluoroethylene.getFluid(288))
                .output(BATTERY_HULL_MEDIUM_VANADIUM)
                .buildAndRegister();

        // LuV
        ASSEMBLER_RECIPES.recipeBuilder().duration(300).EUt(VA[IV])
                .input(cableGtSingle, NiobiumTitanium, 2)
                .input(plate, RedSteel, 18)
                .fluidInputs(Polybenzimidazole.getFluid(144))
                .output(BATTERY_HULL_LARGE_VANADIUM)
                .buildAndRegister();

        // ZPM
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[LuV])
                .input(cableGtSingle, Naquadah, 2)
                .input(plate, Europium, 6)
                .fluidInputs(Polybenzimidazole.getFluid(288))
                .output(BATTERY_HULL_MEDIUM_NAQUADRIA)
                .buildAndRegister();

        // UV
        ASSEMBLER_RECIPES.recipeBuilder().duration(300).EUt(VA[ZPM])
                .input(cableGtSingle, YttriumBariumCuprate, 2)
                .input(plate, Americium, 18)
                .fluidInputs(Polybenzimidazole.getFluid(576))
                .output(BATTERY_HULL_LARGE_NAQUADRIA)
                .buildAndRegister();

        // Battery Filling Recipes

        // LV
        CANNER_RECIPES.recipeBuilder().duration(100).EUt(2)
                .input(BATTERY_HULL_LV)
                .input(dust, Cadmium, 2)
                .output(BATTERY_LV_CADMIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(100).EUt(2)
                .input(BATTERY_HULL_LV)
                .input(dust, Lithium, 2)
                .output(BATTERY_LV_LITHIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(100).EUt(2)
                .input(BATTERY_HULL_LV)
                .input(dust, Sodium, 2)
                .output(BATTERY_LV_SODIUM)
                .buildAndRegister();

        // MV
        CANNER_RECIPES.recipeBuilder().duration(400).EUt(2)
                .input(BATTERY_HULL_MV)
                .input(dust, Cadmium, 8)
                .output(BATTERY_MV_CADMIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(400).EUt(2)
                .input(BATTERY_HULL_MV)
                .input(dust, Lithium, 8)
                .output(BATTERY_MV_LITHIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(400).EUt(2)
                .input(BATTERY_HULL_MV)
                .input(dust, Sodium, 8)
                .output(BATTERY_MV_SODIUM)
                .buildAndRegister();

        // HV
        CANNER_RECIPES.recipeBuilder().duration(1600).EUt(2)
                .input(BATTERY_HULL_HV)
                .input(dust, Cadmium, 16)
                .output(BATTERY_HV_CADMIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(1600).EUt(2)
                .input(BATTERY_HULL_HV)
                .input(dust, Lithium, 16)
                .output(BATTERY_HV_LITHIUM)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(1600).EUt(2)
                .input(BATTERY_HULL_HV)
                .input(dust, Sodium, 16)
                .output(BATTERY_HV_SODIUM)
                .buildAndRegister();

        // EV
        CANNER_RECIPES.recipeBuilder().duration(100).EUt(VA[HV])
                .input(BATTERY_HULL_SMALL_VANADIUM)
                .input(dust, Vanadium, 2)
                .output(BATTERY_EV_VANADIUM)
                .buildAndRegister();

        // IV
        CANNER_RECIPES.recipeBuilder().duration(150).EUt(1024)
                .input(BATTERY_HULL_MEDIUM_VANADIUM)
                .input(dust, Vanadium, 8)
                .output(BATTERY_IV_VANADIUM)
                .buildAndRegister();

        // LuV
        CANNER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .input(BATTERY_HULL_LARGE_VANADIUM)
                .input(dust, Vanadium, 16)
                .output(BATTERY_LUV_VANADIUM)
                .buildAndRegister();

        // ZPM
        CANNER_RECIPES.recipeBuilder().duration(250).EUt(4096)
                .input(BATTERY_HULL_MEDIUM_NAQUADRIA)
                .input(dust, Naquadria, 8)
                .output(BATTERY_ZPM_NAQUADRIA)
                .buildAndRegister();

        // UV
        CANNER_RECIPES.recipeBuilder().duration(300).EUt(VA[IV])
                .input(BATTERY_HULL_LARGE_NAQUADRIA)
                .input(dust, Naquadria, 16)
                .output(BATTERY_UV_NAQUADRIA)
                .buildAndRegister();

        // Battery Recycling Recipes
        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_LV_CADMIUM, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_LV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_LV_LITHIUM, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_LV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_LV_SODIUM, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_LV).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_MV_CADMIUM, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_MV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_MV_LITHIUM, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_MV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_MV_SODIUM, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_MV).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_HV_CADMIUM, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_HV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_HV_LITHIUM, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_HV).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_HV_SODIUM, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_HV).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_EV_VANADIUM, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_SMALL_VANADIUM).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_IV_VANADIUM, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_MEDIUM_VANADIUM).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_LUV_VANADIUM, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_LARGE_VANADIUM).buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_ZPM_NAQUADRIA, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_MEDIUM_NAQUADRIA).buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder().inputNBT(BATTERY_UV_NAQUADRIA, NBTMatcher.ANY, NBTCondition.ANY)
                .output(BATTERY_HULL_LARGE_NAQUADRIA).buildAndRegister();
    }

    private static void gemBatteries() {
        // Energy Crystal
        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[MV])
                .input(dust, Redstone, 5)
                .input(dust, Ruby, 4)
                .circuitMeta(1)
                .output(ENERGIUM_DUST, 9)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(ENERGIUM_DUST, 9)
                .fluidInputs(Water.getFluid(1000))
                .output(ENERGIUM_CRYSTAL)
                .duration(1800).EUt(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(ENERGIUM_DUST, 9)
                .fluidInputs(DistilledWater.getFluid(1000))
                .output(ENERGIUM_CRYSTAL)
                .duration(1200).EUt(320).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(ENERGIUM_DUST, 9)
                .fluidInputs(BlackSteel.getFluid(L * 2))
                .output(ENERGIUM_CRYSTAL)
                .duration(300).EUt(256).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(ENERGIUM_DUST, 9)
                .fluidInputs(BlueSteel.getFluid(L / 2))
                .output(ENERGIUM_CRYSTAL)
                .duration(150).EUt(192).buildAndRegister();

        // Lapotron Crystal
        MIXER_RECIPES.recipeBuilder()
                .input(ENERGIUM_DUST, 3)
                .input(dust, Lapis, 2)
                .circuitMeta(2)
                .output(dust, Lapotron, 5)
                .duration(200).EUt(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(dust, Lapotron, 15)
                .fluidInputs(Water.getFluid(1000))
                .output(gem, Lapotron)
                .duration(1800).EUt(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(dust, Lapotron, 15)
                .fluidInputs(DistilledWater.getFluid(1000))
                .output(gem, Lapotron)
                .duration(1200).EUt(320).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(dust, Lapotron, 15)
                .fluidInputs(BlueSteel.getFluid(L * 2))
                .output(gem, Lapotron)
                .duration(300).EUt(256).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(dust, Lapotron, 15)
                .fluidInputs(RedSteel.getFluid(L / 2))
                .output(gem, Lapotron)
                .duration(150).EUt(192).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(gem, Lapotron)
                .input(circuit, Tier.HV, 2)
                .output(LAPOTRON_CRYSTAL)
                .duration(600).EUt(VA[EV]).buildAndRegister();

        // Lapotronic Energy Orb
        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .inputNBT(LAPOTRON_CRYSTAL, NBTMatcher.ANY, NBTCondition.ANY)
                .notConsumable(craftingLens, Color.Blue)
                .output(ENGRAVED_LAPOTRON_CHIP, 3)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(256).EUt(VA[HV]).buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(512).EUt(1024)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(POWER_INTEGRATED_CIRCUIT, 4)
                .input(ENGRAVED_LAPOTRON_CHIP, 24)
                .input(NANO_CENTRAL_PROCESSING_UNIT, 2)
                .input(wireFine, Platinum, 16)
                .input(plate, Platinum, 8)
                .output(ENERGY_LAPOTRONIC_ORB)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // Lapotronic Energy Cluster
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(80000).duration(1000)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(plate, Europium, 8)
                .input(circuit, Tier.LuV, 4)
                .inputNBT(ENERGY_LAPOTRONIC_ORB, NBTMatcher.ANY, NBTCondition.ANY)
                .input(FIELD_GENERATOR_IV)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 16)
                .input(ADVANCED_SMD_DIODE, 8)
                .input(ADVANCED_SMD_CAPACITOR, 8)
                .input(ADVANCED_SMD_RESISTOR, 8)
                .input(ADVANCED_SMD_TRANSISTOR, 8)
                .input(ADVANCED_SMD_INDUCTOR, 8)
                .input(wireFine, Platinum, 64)
                .input(bolt, Naquadah, 16)
                .fluidInputs(SolderingAlloy.getFluid(L * 5))
                .output(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .scannerResearch(ENERGY_LAPOTRONIC_ORB.getStackForm())
                .buildAndRegister();

        // Energy Module
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(100000).duration(1200)
                .input(ELITE_CIRCUIT_BOARD)
                .input(plateDouble, Europium, 8)
                .input(circuit, Tier.ZPM, 4)
                .inputNBT(ENERGY_LAPOTRONIC_ORB_CLUSTER, NBTMatcher.ANY, NBTCondition.ANY)
                .input(FIELD_GENERATOR_LuV)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 32)
                .input(ADVANCED_SMD_DIODE, 12)
                .input(ADVANCED_SMD_CAPACITOR, 12)
                .input(ADVANCED_SMD_RESISTOR, 12)
                .input(ADVANCED_SMD_TRANSISTOR, 12)
                .input(ADVANCED_SMD_INDUCTOR, 12)
                .input(wireFine, Ruridit, 64)
                .input(bolt, Trinium, 16)
                .fluidInputs(SolderingAlloy.getFluid(L * 10))
                .output(ENERGY_MODULE)
                .stationResearch(b -> b
                        .researchStack(ENERGY_LAPOTRONIC_ORB_CLUSTER.getStackForm())
                        .CWUt(16))
                .buildAndRegister();

        // Energy Cluster
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(200000).duration(1400)
                .input(WETWARE_CIRCUIT_BOARD)
                .input(plate, Americium, 16)
                .input(WETWARE_SUPER_COMPUTER_UV, 4)
                .input(ENERGY_MODULE)
                .input(FIELD_GENERATOR_ZPM)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 32)
                .input(ADVANCED_SMD_DIODE, 16)
                .input(ADVANCED_SMD_CAPACITOR, 16)
                .input(ADVANCED_SMD_RESISTOR, 16)
                .input(ADVANCED_SMD_TRANSISTOR, 16)
                .input(ADVANCED_SMD_INDUCTOR, 16)
                .input(wireFine, Osmiridium, 64)
                .input(bolt, Naquadria, 16)
                .fluidInputs(SolderingAlloy.getFluid(L * 20))
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .output(ENERGY_CLUSTER)
                .stationResearch(b -> b
                        .researchStack(ENERGY_MODULE.getStackForm())
                        .CWUt(96)
                        .EUt(VA[ZPM]))
                .buildAndRegister();

        // Ultimate Battery
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(300000).duration(2000)
                .input(plateDouble, Darmstadtium, 16)
                .input(circuit, Tier.UHV, 4)
                .input(ENERGY_CLUSTER, 16)
                .input(FIELD_GENERATOR_UV, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 64)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 64)
                .input(ADVANCED_SMD_DIODE, 64)
                .input(ADVANCED_SMD_CAPACITOR, 64)
                .input(ADVANCED_SMD_RESISTOR, 64)
                .input(ADVANCED_SMD_TRANSISTOR, 64)
                .input(ADVANCED_SMD_INDUCTOR, 64)
                .input(wireGtSingle, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .input(bolt, Neutronium, 64)
                .fluidInputs(SolderingAlloy.getFluid(L * 40))
                .fluidInputs(Polybenzimidazole.getFluid(2304))
                .fluidInputs(Naquadria.getFluid(L * 18))
                .output(ULTIMATE_BATTERY)
                .stationResearch(b -> b
                        .researchStack(ENERGY_CLUSTER.getStackForm())
                        .CWUt(144)
                        .EUt(VA[UHV]))
                .buildAndRegister();
    }

    private static void batteryBlocks() {
        // Empty Tier I
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(frameGt, Ultimet)
                .input(plate, Ultimet, 6)
                .input(screw, Ultimet, 24)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_I))
                .duration(400).EUt(VA[HV]).buildAndRegister();

        // Lapotronic EV
        CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_I))
                .inputNBT(LAPOTRON_CRYSTAL, NBTMatcher.ANY, NBTCondition.ANY)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_EV))
                .duration(200).EUt(VA[HV]).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_EV))
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_I))
                .output(LAPOTRON_CRYSTAL)
                .circuitMeta(2)
                .duration(200).EUt(VA[LV]).buildAndRegister();

        // Lapotronic IV
        CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_I))
                .inputNBT(ENERGY_LAPOTRONIC_ORB, NBTMatcher.ANY, NBTCondition.ANY)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_IV))
                .duration(400).EUt(VA[HV]).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_IV))
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_I))
                .output(ENERGY_LAPOTRONIC_ORB)
                .circuitMeta(2)
                .duration(200).EUt(VA[LV]).buildAndRegister();

        // Empty Tier II
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(frameGt, Ruridit)
                .input(plate, Ruridit, 6)
                .input(screw, Ruridit, 24)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_II))
                .duration(400).EUt(VA[IV]).buildAndRegister();

        // Lapotronic LuV
        CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_II))
                .inputNBT(ENERGY_LAPOTRONIC_ORB_CLUSTER, NBTMatcher.ANY, NBTCondition.ANY)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_LuV))
                .duration(200).EUt(VA[EV]).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_LuV))
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_II))
                .output(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .circuitMeta(2)
                .duration(200).EUt(VA[LV]).buildAndRegister();

        // Lapotronic ZPM
        CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_II))
                .inputNBT(ENERGY_MODULE, NBTMatcher.ANY, NBTCondition.ANY)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_ZPM))
                .duration(400).EUt(VA[EV]).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_ZPM))
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_II))
                .output(ENERGY_MODULE)
                .circuitMeta(2)
                .duration(200).EUt(VA[LV]).buildAndRegister();

        // Empty Tier III
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(frameGt, Neutronium)
                .input(plate, Neutronium, 6)
                .input(screw, Neutronium, 24)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_III))
                .duration(400).EUt(VA[ZPM]).buildAndRegister();

        // Lapotronic UV
        CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_III))
                .inputNBT(ENERGY_CLUSTER, NBTMatcher.ANY, NBTCondition.ANY)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_UV))
                .duration(200).EUt(VA[IV]).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.LAPOTRONIC_UV))
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_III))
                .output(ENERGY_CLUSTER)
                .circuitMeta(2)
                .duration(200).EUt(VA[LV]).buildAndRegister();

        // Ultimate UHV
        CANNER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_III))
                .inputNBT(ULTIMATE_BATTERY, NBTMatcher.ANY, NBTCondition.ANY)
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.ULTIMATE_UHV))
                .duration(400).EUt(VA[IV]).buildAndRegister();

        PACKER_RECIPES.recipeBuilder()
                .inputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.ULTIMATE_UHV))
                .outputs(BATTERY_BLOCK.getItemVariant(BatteryPartType.EMPTY_TIER_III))
                .output(ULTIMATE_BATTERY)
                .circuitMeta(2)
                .duration(200).EUt(VA[LV]).buildAndRegister();
    }
}
