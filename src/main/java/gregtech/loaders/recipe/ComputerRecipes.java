package gregtech.loaders.recipe;

import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.BlockGlassCasing;

import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.MetaBlocks.*;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;

public class ComputerRecipes {

    public static void init() {
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ITEM_IMPORT_BUS[EV])
                .inputItem(TOOL_DATA_STICK, 4)
                .inputItem(circuit, Tier.IV, 4)
                .outputItem(DATA_ACCESS_HATCH)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 2))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).volts(VA[EV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(ITEM_IMPORT_BUS[LuV])
                .inputItem(TOOL_DATA_ORB, 4)
                .inputItem(circuit, Tier.ZPM, 4)
                .outputItem(ADVANCED_DATA_ACCESS_HATCH)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .stationResearch(b -> b.researchStack(DATA_BANK.getStackForm()).CWUt(4))
                .duration(400).volts(6000).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(frameGt, Iridium)
                .inputItem(plate, Iridium, 6)
                .inputItem(circuit, Tier.IV)
                .inputItem(wireFine, Cobalt, 16)
                .inputItem(wireFine, Copper, 16)
                .inputItem(wireGtSingle, NiobiumTitanium, 2)
                .outputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.HIGH_POWER_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(100).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(frameGt, Iridium)
                .inputItem(plate, Iridium, 6)
                .inputItem(circuit, Tier.LuV)
                .inputItem(wireFine, Cobalt, 32)
                .inputItem(wireFine, Copper, 32)
                .inputItem(wireGtSingle, VanadiumGallium, 2)
                .outputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING))
                .inputItem(circuit, Tier.ZPM)
                .inputItem(wireFine, Cobalt, 64)
                .inputItem(wireFine, Electrum, 64)
                .inputItem(wireGtSingle, IndiumTinBariumTitaniumCuprate, 4)
                .outputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING))
                .duration(200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(frameGt, StainlessSteel)
                .inputItem(ELECTRIC_MOTOR_IV, 2)
                .inputItem(rotor, StainlessSteel, 2)
                .inputItem(pipeTinyFluid, StainlessSteel, 16)
                .inputItem(plate, Copper, 16)
                .inputItem(wireGtSingle, SamariumIronArsenicOxide)
                .outputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_HEAT_VENT,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(100).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(wireFine, BorosilicateGlass, 8)
                .inputItem(foil, Silver, 8)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L))
                .cleanroom(CleanroomType.CLEANROOM)
                .outputItem(OPTICAL_PIPES[0])
                .duration(100).volts(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING))
                .inputItem(circuit, Tier.LuV, 8)
                .inputItem(TOOL_DATA_ORB)
                .inputItem(wireFine, Cobalt, 64)
                .inputItem(wireFine, Copper, 64)
                .inputItem(OPTICAL_PIPES[0], 4)
                .inputItem(wireGtDouble, IndiumTinBariumTitaniumCuprate, 16)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .fluidInputs(Lubricant.getFluid(500))
                .outputItem(DATA_BANK)
                .scannerResearch(b -> b
                        .researchStack(DATA_ACCESS_HATCH.getStackForm())
                        .duration(2400)
                        .EUt(VA[EV]))
                .duration(1200).volts(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(DATA_BANK)
                .inputItem(SENSOR_LuV, 8)
                .inputItem(circuit, Tier.ZPM, 8)
                .inputItem(FIELD_GENERATOR_LuV, 2)
                .inputItem(ELECTRIC_MOTOR_ZPM, 2)
                .inputItem(wireGtDouble, UraniumRhodiumDinaquadide, 32)
                .inputItem(foil, Trinium, 32)
                .inputItem(OPTICAL_PIPES[0], 16)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(VanadiumGallium.getFluid(L * 8))
                .outputItem(RESEARCH_STATION)
                .scannerResearch(b -> b
                        .researchStack(SCANNER[LuV].getStackForm())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(1200).volts(100000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(ITEM_IMPORT_BUS[ZPM])
                .inputItem(EMITTER_LuV, 8)
                .inputItem(circuit, Tier.ZPM)
                .inputItem(ROBOT_ARM_ZPM, 2)
                .inputItem(ELECTRIC_MOTOR_ZPM, 2)
                .inputItem(wireGtDouble, UraniumRhodiumDinaquadide, 16)
                .inputItem(OPTICAL_PIPES[0], 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .outputItem(OBJECT_HOLDER)
                .scannerResearch(b -> b
                        .researchStack(ITEM_IMPORT_BUS[ZPM].getStackForm())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(1200).volts(100000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING))
                .inputItem(EMITTER_ZPM, 4)
                .inputItem(SENSOR_ZPM, 4)
                .inputItem(circuit, Tier.UV, 4)
                .inputItem(wireGtDouble, EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .inputItem(foil, Tritanium, 64)
                .inputItem(foil, Tritanium, 64)
                .inputItem(OPTICAL_PIPES[0], 8)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .outputItem(NETWORK_SWITCH)
                .stationResearch(b -> b
                        .researchStack(new ItemStack(OPTICAL_PIPES[0]))
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(1200).volts(100000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(DATA_BANK)
                .inputItem(circuit, Tier.ZPM, 4)
                .inputItem(FIELD_GENERATOR_LuV, 8)
                .inputItem(TOOL_DATA_ORB)
                .inputItem(COVER_SCREEN)
                .inputItem(wireGtDouble, UraniumRhodiumDinaquadide, 64)
                .inputItem(OPTICAL_PIPES[0], 16)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(VanadiumGallium.getFluid(L * 8))
                .fluidInputs(PCBCoolant.getFluid(4000))
                .outputItem(HIGH_PERFORMANCE_COMPUTING_ARRAY)
                .scannerResearch(b -> b
                        .researchStack(COVER_SCREEN.getStackForm())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(1200).volts(100000).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING))
                .inputItem(circuit, Tier.IV)
                .inputItem(TOOL_DATA_STICK)
                .outputItem(HPCA_EMPTY_COMPONENT)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HPCA_EMPTY_COMPONENT)
                .inputItem(plate, Aluminium, 32)
                .inputItem(screw, StainlessSteel, 8)
                .outputItem(HPCA_HEAT_SINK_COMPONENT)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING))
                .inputItem(plate, Aluminium, 16)
                .inputItem(pipeTinyFluid, StainlessSteel, 16)
                .inputItem(screw, StainlessSteel, 8)
                .outputItem(HPCA_ACTIVE_COOLER_COMPONENT)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING))
                .inputItem(circuit, Tier.UV)
                .inputItem(EMITTER_ZPM)
                .inputItem(OPTICAL_PIPES[0], 2)
                .outputItem(HPCA_BRIDGE_COMPONENT)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HPCA_EMPTY_COMPONENT)
                .inputItem(circuit, Tier.ZPM, 4)
                .inputItem(FIELD_GENERATOR_LuV)
                .outputItem(HPCA_COMPUTATION_COMPONENT)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HPCA_COMPUTATION_COMPONENT)
                .inputItem(circuit, Tier.UV, 4)
                .inputItem(FIELD_GENERATOR_ZPM)
                .outputItem(HPCA_ADVANCED_COMPUTATION_COMPONENT)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING))
                .inputItem(ITEM_IMPORT_BUS[LuV])
                .inputItem(circuit, Tier.LuV)
                .inputItem(SENSOR_IV)
                .inputItem(OPTICAL_PIPES[0], 2)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .outputItem(OPTICAL_DATA_HATCH_RECEIVER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING))
                .inputItem(ITEM_EXPORT_BUS[LuV])
                .inputItem(circuit, Tier.LuV)
                .inputItem(EMITTER_IV)
                .inputItem(OPTICAL_PIPES[0], 2)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .outputItem(OPTICAL_DATA_HATCH_TRANSMITTER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(OPTICAL_DATA_HATCH_RECEIVER)
                .inputItem(circuit, Tier.ZPM)
                .inputItem(SENSOR_LuV)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .outputItem(COMPUTATION_HATCH_RECEIVER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(OPTICAL_DATA_HATCH_TRANSMITTER)
                .inputItem(circuit, Tier.ZPM)
                .inputItem(EMITTER_LuV)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .outputItem(COMPUTATION_HATCH_TRANSMITTER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(POWER_TRANSFORMER[LuV])
                .inputItem(circuit, Tier.LuV, 2)
                .inputItem(wireGtSingle, IndiumTinBariumTitaniumCuprate, 8)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .outputItem(ACTIVE_TRANSFORMER)
                .duration(300).volts(VA[LuV]).buildAndRegister();

        /*
         * TODO UPSATUPDATE
         * ASSEMBLER_RECIPES.recipeBuilder()
         * .input(HULL[LuV])
         * .input(lens, Diamond)
         * .input(EMITTER_LuV)
         * .input(wireGtSingle, IndiumTinBariumTitaniumCuprate, 2)
         * .output(LASER_OUTPUT_HATCH)
         * .circuitMeta(1)
         * .duration(300).EUt(VA[IV]).buildAndRegister();
         * 
         * ASSEMBLER_RECIPES.recipeBuilder()
         * .input(HULL[LuV])
         * .input(lens, Diamond)
         * .input(SENSOR_LuV)
         * .input(wireGtSingle, IndiumTinBariumTitaniumCuprate, 2)
         * .output(LASER_INPUT_HATCH)
         * .circuitMeta(2)
         * .duration(300).EUt(VA[IV]).buildAndRegister();
         */

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.LAMINATED_GLASS))
                .inputItem(foil, Osmiridium, 2)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L))
                .outputItem(LASER_PIPES[0])
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).volts(VA[IV]).buildAndRegister();
    }
}
