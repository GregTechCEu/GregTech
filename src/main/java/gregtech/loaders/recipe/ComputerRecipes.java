package gregtech.loaders.recipe;

import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
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
                .input(ITEM_IMPORT_BUS[EV])
                .inputNBT(TOOL_DATA_STICK, 4, NBTMatcher.ANY, NBTCondition.ANY)
                .input(circuit, Tier.IV, 4)
                .output(DATA_ACCESS_HATCH)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 2))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(ITEM_IMPORT_BUS[LuV])
                .inputNBT(TOOL_DATA_ORB, 4, NBTMatcher.ANY, NBTCondition.ANY)
                .input(circuit, Tier.ZPM, 4)
                .output(ADVANCED_DATA_ACCESS_HATCH)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .stationResearch(b -> b.researchStack(DATA_BANK.getStackForm()).CWUt(4))
                .duration(400).EUt(6000).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(frameGt, Iridium)
                .input(plate, Iridium, 6)
                .input(circuit, Tier.IV)
                .input(wireFine, Cobalt, 16)
                .input(wireFine, Copper, 16)
                .input(wireGtSingle, NiobiumTitanium, 2)
                .outputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.HIGH_POWER_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(frameGt, Iridium)
                .input(plate, Iridium, 6)
                .input(circuit, Tier.LuV)
                .input(wireFine, Cobalt, 32)
                .input(wireFine, Copper, 32)
                .input(wireGtSingle, VanadiumGallium, 2)
                .outputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING))
                .input(circuit, Tier.ZPM)
                .input(wireFine, Cobalt, 64)
                .input(wireFine, Electrum, 64)
                .input(wireGtSingle, IndiumTinBariumTitaniumCuprate, 4)
                .outputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING))
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(frameGt, StainlessSteel)
                .input(ELECTRIC_MOTOR_IV, 2)
                .input(rotor, StainlessSteel, 2)
                .input(pipeTinyFluid, StainlessSteel, 16)
                .input(plate, Copper, 16)
                .input(wireGtSingle, SamariumIronArsenicOxide)
                .outputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_HEAT_VENT,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(100).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(wireFine, BorosilicateGlass, 8)
                .input(foil, Silver, 8)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L))
                .cleanroom(CleanroomType.CLEANROOM)
                .output(OPTICAL_PIPES[0])
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING))
                .input(circuit, Tier.LuV, 8)
                .inputNBT(TOOL_DATA_ORB, NBTMatcher.ANY, NBTCondition.ANY)
                .input(wireFine, Cobalt, 64)
                .input(wireFine, Copper, 64)
                .input(OPTICAL_PIPES[0], 4)
                .input(wireGtDouble, IndiumTinBariumTitaniumCuprate, 16)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .fluidInputs(Lubricant.getFluid(500))
                .output(DATA_BANK)
                .scannerResearch(b -> b
                        .researchStack(DATA_ACCESS_HATCH.getStackForm())
                        .duration(2400)
                        .EUt(VA[EV]))
                .duration(1200).EUt(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(DATA_BANK)
                .input(SENSOR_LuV, 8)
                .input(circuit, Tier.ZPM, 8)
                .input(FIELD_GENERATOR_LuV, 2)
                .input(ELECTRIC_MOTOR_ZPM, 2)
                .input(wireGtDouble, UraniumRhodiumDinaquadide, 32)
                .input(foil, Trinium, 32)
                .input(OPTICAL_PIPES[0], 16)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(VanadiumGallium.getFluid(L * 8))
                .output(RESEARCH_STATION)
                .scannerResearch(b -> b
                        .researchStack(SCANNER[LuV].getStackForm())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(1200).EUt(100000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(ITEM_IMPORT_BUS[ZPM])
                .input(EMITTER_LuV, 8)
                .input(circuit, Tier.ZPM)
                .input(ROBOT_ARM_ZPM, 2)
                .input(ELECTRIC_MOTOR_ZPM, 2)
                .input(wireGtDouble, UraniumRhodiumDinaquadide, 16)
                .input(OPTICAL_PIPES[0], 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .output(OBJECT_HOLDER)
                .scannerResearch(b -> b
                        .researchStack(ITEM_IMPORT_BUS[ZPM].getStackForm())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(1200).EUt(100000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING))
                .input(EMITTER_ZPM, 4)
                .input(SENSOR_ZPM, 4)
                .input(circuit, Tier.UV, 4)
                .input(wireGtDouble, EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .input(foil, Tritanium, 64)
                .input(foil, Tritanium, 64)
                .input(OPTICAL_PIPES[0], 8)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .output(NETWORK_SWITCH)
                .stationResearch(b -> b
                        .researchStack(new ItemStack(OPTICAL_PIPES[0]))
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(1200).EUt(100000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(DATA_BANK)
                .input(circuit, Tier.ZPM, 4)
                .input(FIELD_GENERATOR_LuV, 8)
                .inputNBT(TOOL_DATA_ORB, NBTMatcher.ANY, NBTCondition.ANY)
                .input(COVER_SCREEN)
                .input(wireGtDouble, UraniumRhodiumDinaquadide, 64)
                .input(OPTICAL_PIPES[0], 16)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(VanadiumGallium.getFluid(L * 8))
                .fluidInputs(PCBCoolant.getFluid(4000))
                .output(HIGH_PERFORMANCE_COMPUTING_ARRAY)
                .scannerResearch(b -> b
                        .researchStack(COVER_SCREEN.getStackForm())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(1200).EUt(100000).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING))
                .input(circuit, Tier.IV)
                .inputNBT(TOOL_DATA_STICK, NBTMatcher.ANY, NBTCondition.ANY)
                .output(HPCA_EMPTY_COMPONENT)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HPCA_EMPTY_COMPONENT)
                .input(plate, Aluminium, 32)
                .input(screw, StainlessSteel, 8)
                .output(HPCA_HEAT_SINK_COMPONENT)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING))
                .input(plate, Aluminium, 16)
                .input(pipeTinyFluid, StainlessSteel, 16)
                .input(screw, StainlessSteel, 8)
                .output(HPCA_ACTIVE_COOLER_COMPONENT)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING))
                .input(circuit, Tier.UV)
                .input(EMITTER_ZPM)
                .input(OPTICAL_PIPES[0], 2)
                .output(HPCA_BRIDGE_COMPONENT)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HPCA_EMPTY_COMPONENT)
                .input(circuit, Tier.ZPM, 4)
                .input(FIELD_GENERATOR_LuV)
                .output(HPCA_COMPUTATION_COMPONENT)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HPCA_COMPUTATION_COMPONENT)
                .input(circuit, Tier.UV, 4)
                .input(FIELD_GENERATOR_ZPM)
                .output(HPCA_ADVANCED_COMPUTATION_COMPONENT)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING))
                .input(ITEM_IMPORT_BUS[LuV])
                .input(circuit, Tier.LuV)
                .input(SENSOR_IV)
                .input(OPTICAL_PIPES[0], 2)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .output(OPTICAL_DATA_HATCH_RECEIVER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING))
                .input(ITEM_EXPORT_BUS[LuV])
                .input(circuit, Tier.LuV)
                .input(EMITTER_IV)
                .input(OPTICAL_PIPES[0], 2)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .output(OPTICAL_DATA_HATCH_TRANSMITTER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OPTICAL_DATA_HATCH_RECEIVER)
                .input(circuit, Tier.ZPM)
                .input(SENSOR_LuV)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .output(COMPUTATION_HATCH_RECEIVER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OPTICAL_DATA_HATCH_TRANSMITTER)
                .input(circuit, Tier.ZPM)
                .input(EMITTER_LuV)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .output(COMPUTATION_HATCH_TRANSMITTER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[LuV])
                .input(circuit, Tier.LuV, 2)
                .input(wireGtSingle, IndiumTinBariumTitaniumCuprate, 8)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .fluidInputs(PCBCoolant.getFluid(1000))
                .output(ACTIVE_TRANSFORMER)
                .duration(300).EUt(VA[LuV]).buildAndRegister();

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
                .input(foil, Osmiridium, 2)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L))
                .output(LASER_PIPES[0])
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).EUt(VA[IV]).buildAndRegister();
    }
}
