package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials.Color;
import gregtech.api.unification.material.MarkerMaterials.Component;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.ConfigHolder;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

public class CircuitRecipes {

    public static void init() {
        componentRecipes();
        boardRecipes();
        circuitRecipes();
        logicControllerRecipes();
    }

    private static void logicControllerRecipes() {

        //MV
        PROGRAMMER_RECIPES .recipeBuilder()
                .input(ELECTRONIC_CIRCUIT_MV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_MV, 4)
                .output(LOGIC_CONTROLLER_MV, 4)
                .duration(300).EUt(VA[LV]).buildAndRegister();

        PROGRAMMER_RECIPES .recipeBuilder()
                .input(INTEGRATED_CIRCUIT_MV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_MV, 8)
                .output(LOGIC_CONTROLLER_MV, 8)
                .duration(200).EUt(VA[LV]).buildAndRegister();

        PROGRAMMER_RECIPES .recipeBuilder()
                .input(PROCESSOR_MV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_MV, 16)
                .output(LOGIC_CONTROLLER_MV, 16)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        //HV
        PROGRAMMER_RECIPES .recipeBuilder()
                .input(INTEGRATED_CIRCUIT_HV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_HV, 4)
                .output(LOGIC_CONTROLLER_HV, 4)
                .duration(300).EUt(VA[LV]).buildAndRegister();

        PROGRAMMER_RECIPES .recipeBuilder()
                .input(PROCESSOR_ASSEMBLY_HV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_HV, 8)
                .output(LOGIC_CONTROLLER_HV, 8)
                .duration(200).EUt(VA[LV]).buildAndRegister();

        PROGRAMMER_RECIPES .recipeBuilder()
                .input(NANO_PROCESSOR_HV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_HV, 16)
                .output(LOGIC_CONTROLLER_HV, 16)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        //EV
        PROGRAMMER_RECIPES .recipeBuilder()
                .input(WORKSTATION_EV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_EV, 4)
                .output(LOGIC_CONTROLLER_EV, 4)
                .duration(300).EUt(VA[LV]).buildAndRegister();

        PROGRAMMER_RECIPES .recipeBuilder()
                .input(NANO_PROCESSOR_ASSEMBLY_EV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_EV, 8)
                .output(LOGIC_CONTROLLER_EV, 8)
                .duration(200).EUt(VA[LV]).buildAndRegister();

        PROGRAMMER_RECIPES .recipeBuilder()
                .input(QUANTUM_PROCESSOR_EV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_EV, 16)
                .output(LOGIC_CONTROLLER_EV, 16)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        //IV
        PROGRAMMER_RECIPES .recipeBuilder()
                .input(NANO_COMPUTER_IV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_IV, 4)
                .output(LOGIC_CONTROLLER_IV, 4)
                .duration(300).EUt(VA[LV]).buildAndRegister();

        PROGRAMMER_RECIPES .recipeBuilder()
                .input(QUANTUM_ASSEMBLY_IV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_IV, 8)
                .output(LOGIC_CONTROLLER_IV, 8)
                .duration(200).EUt(VA[LV]).buildAndRegister();

        PROGRAMMER_RECIPES .recipeBuilder()
                .input(CRYSTAL_PROCESSOR_IV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_IV, 16)
                .output(LOGIC_CONTROLLER_IV, 16)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        //LUV
        PROGRAMMER_RECIPES .recipeBuilder()
                .input(QUANTUM_COMPUTER_LUV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_LuV, 4)
                .output(LOGIC_CONTROLLER_LuV, 4)
                .duration(300).EUt(VA[LV]).buildAndRegister();

        PROGRAMMER_RECIPES .recipeBuilder()
                .input(CRYSTAL_ASSEMBLY_LUV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_LuV, 8)
                .output(LOGIC_CONTROLLER_LuV, 8)
                .duration(200).EUt(VA[LV]).buildAndRegister();

        PROGRAMMER_RECIPES .recipeBuilder()
                .input(WETWARE_PROCESSOR_LUV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_LuV, 16)
                .output(LOGIC_CONTROLLER_LuV, 16)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        //ZPM
        PROGRAMMER_RECIPES .recipeBuilder()
                .input(CRYSTAL_COMPUTER_ZPM, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_ZPM, 4)
                .output(LOGIC_CONTROLLER_ZPM, 4)
                .duration(300).EUt(VA[LV]).buildAndRegister();

        PROGRAMMER_RECIPES .recipeBuilder()
                .input(WETWARE_PROCESSOR_ASSEMBLY_ZPM, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_ZPM, 8)
                .output(LOGIC_CONTROLLER_ZPM, 8)
                .duration(200).EUt(VA[LV]).buildAndRegister();

        //UV
        PROGRAMMER_RECIPES .recipeBuilder()
                .input(WETWARE_SUPER_COMPUTER_UV, 1)
                .input(PROGRAMMABLE_LOGIC_CONTROLLER_UV, 4)
                .output(LOGIC_CONTROLLER_UV, 4)
                .duration(300).EUt(VA[LV]).buildAndRegister();
    }

    private static void componentRecipes() {
        // Vacuum Tube
        ModHandler.addShapedRecipe("vacuum_tube", VACUUM_TUBE.getStackForm(),
                "PTP", "WWW",
                'P', new UnificationEntry(bolt, Steel),
                'T', GLASS_TUBE.getStackForm(),
                'W', new UnificationEntry(wireGtSingle, Copper));

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(GLASS_TUBE)
                .input(bolt, Steel, 2)
                .input(wireGtSingle, Copper, 2)
                .circuitMeta(1)
                .output(VACUUM_TUBE, 2)
                .duration(160).EUt(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(GLASS_TUBE)
                .input(bolt, Steel, 2)
                .input(wireGtSingle, Copper, 2)
                .fluidInputs(RedAlloy.getFluid(18))
                .output(VACUUM_TUBE, 3)
                .duration(160).EUt(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(GLASS_TUBE)
                .input(bolt, Steel, 2)
                .input(wireGtSingle, AnnealedCopper, 2)
                .fluidInputs(RedAlloy.getFluid(18))
                .output(VACUUM_TUBE, 4)
                .duration(160).EUt(VA[ULV]).buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder()
                .input(dust, Glass)
                .notConsumable(SHAPE_MOLD_BALL)
                .output(GLASS_TUBE)
                .duration(160).EUt(16).buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .fluidInputs(Glass.getFluid(GTValues.L))
                .notConsumable(SHAPE_MOLD_BALL)
                .output(GLASS_TUBE)
                .duration(200).EUt(24).buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder()
                .input(dust, Glass)
                .notConsumable(SHAPE_MOLD_BALL)
                .output(GLASS_TUBE)
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        // Resistor
        ModHandler.addShapedRecipe("resistor_wire", RESISTOR.getStackForm(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.getStackForm(),
                'W', new UnificationEntry(wireGtSingle, Copper),
                'C', new UnificationEntry(dust, Coal));

        ModHandler.addShapedRecipe("resistor_wire_fine", RESISTOR.getStackForm(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.getStackForm(),
                'W', new UnificationEntry(wireFine, Copper),
                'C', new UnificationEntry(dust, Coal));

        ModHandler.addShapedRecipe("resistor_wire_charcoal", RESISTOR.getStackForm(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.getStackForm(),
                'W', new UnificationEntry(wireGtSingle, Copper),
                'C', new UnificationEntry(dust, Charcoal));

        ModHandler.addShapedRecipe("resistor_wire_fine_charcoal", RESISTOR.getStackForm(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.getStackForm(),
                'W', new UnificationEntry(wireFine, Copper),
                'C', new UnificationEntry(dust, Charcoal));

        ModHandler.addShapedRecipe("resistor_wire_carbon", RESISTOR.getStackForm(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.getStackForm(),
                'W', new UnificationEntry(wireGtSingle, Copper),
                'C', new UnificationEntry(dust, Carbon));

        ModHandler.addShapedRecipe("resistor_wire_fine_carbon", RESISTOR.getStackForm(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', STICKY_RESIN.getStackForm(),
                'W', new UnificationEntry(wireFine, Copper),
                'C', new UnificationEntry(dust, Carbon));

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, Coal)
                .input(wireFine, Copper, 4)
                .output(RESISTOR, 2)
                .fluidInputs(Glue.getFluid(100))
                .duration(160).EUt(6).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, Charcoal)
                .input(wireFine, Copper, 4)
                .output(RESISTOR, 2)
                .fluidInputs(Glue.getFluid(100))
                .duration(160).EUt(6).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, Carbon)
                .input(wireFine, Copper, 4)
                .output(RESISTOR, 2)
                .fluidInputs(Glue.getFluid(100))
                .duration(160).EUt(6).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, Coal)
                .input(wireFine, AnnealedCopper, 4)
                .output(RESISTOR, 4)
                .fluidInputs(Glue.getFluid(100))
                .duration(160).EUt(6).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, Charcoal)
                .input(wireFine, AnnealedCopper, 4)
                .output(RESISTOR, 4)
                .fluidInputs(Glue.getFluid(100))
                .duration(160).EUt(6).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, Carbon)
                .input(wireFine, AnnealedCopper, 4)
                .output(RESISTOR, 4)
                .fluidInputs(Glue.getFluid(100))
                .duration(160).EUt(6).buildAndRegister();

        // Capacitor
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(foil, Polyethylene)
                .input(foil, Aluminium, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(CAPACITOR, 8)
                .duration(320).EUt(VA[MV]).buildAndRegister();

        // Transistor
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Silicon)
                .input(wireFine, Tin, 6)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(TRANSISTOR, 8)
                .duration(160).EUt(VA[MV]).buildAndRegister();

        // Diode
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(wireFine, Copper, 4)
                .input(dust, GalliumArsenide)
                .fluidInputs(Glass.getFluid(L))
                .output(DIODE)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(wireFine, AnnealedCopper, 4)
                .input(dust, GalliumArsenide)
                .fluidInputs(Glass.getFluid(L))
                .output(DIODE, 2)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(wireFine, Copper, 4)
                .input(dust, GalliumArsenide)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(DIODE, 2)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(wireFine, Copper, 4)
                .input(wafer, Silicon)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(DIODE, 2)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(wireFine, AnnealedCopper, 4)
                .input(dust, GalliumArsenide)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(DIODE, 4)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(wireFine, AnnealedCopper, 4)
                .input(wafer, Silicon)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(DIODE, 4)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        // Inductor
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ring, Steel)
                .input(wireFine, Copper, 2)
                .fluidInputs(Polyethylene.getFluid(L / 4))
                .output(INDUCTOR, 2)
                .duration(320).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ring, Steel)
                .input(wireFine, AnnealedCopper, 2)
                .fluidInputs(Polyethylene.getFluid(L / 4))
                .output(INDUCTOR, 4)
                .duration(320).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ring, NickelZincFerrite)
                .input(wireFine, Copper, 2)
                .fluidInputs(Polyethylene.getFluid(L / 4))
                .output(INDUCTOR, 4)
                .duration(320).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ring, NickelZincFerrite)
                .input(wireFine, AnnealedCopper, 2)
                .fluidInputs(Polyethylene.getFluid(L / 4))
                .output(INDUCTOR, 8)
                .duration(320).EUt(VA[MV]).buildAndRegister();

        // SMD Resistor
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, Carbon)
                .input(wireFine, Electrum, 4)
                .fluidInputs(Polyethylene.getFluid(L * 2))
                .output(SMD_RESISTOR, 16)
                .duration(160).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, Carbon)
                .input(wireFine, Tantalum, 4)
                .fluidInputs(Polyethylene.getFluid(L * 2))
                .output(SMD_RESISTOR, 32)
                .duration(160).EUt(VA[HV]).buildAndRegister();

        // SMD Diode
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, GalliumArsenide)
                .input(wireFine, Platinum, 8)
                .fluidInputs(Polyethylene.getFluid(L * 2))
                .output(SMD_DIODE, 32)
                .duration(200).EUt(VA[HV]).buildAndRegister();

        // SMD Transistor
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(foil, Gallium)
                .input(wireFine, AnnealedCopper, 8)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(SMD_TRANSISTOR, 16)
                .duration(160).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(foil, Gallium)
                .input(wireFine, Tantalum, 8)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(SMD_TRANSISTOR, 32)
                .duration(160).EUt(VA[HV]).buildAndRegister();

        // SMD Capacitor
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(foil, SiliconeRubber)
                .input(foil, Aluminium)
                .fluidInputs(Polyethylene.getFluid(L / 2))
                .output(SMD_CAPACITOR, 8)
                .duration(80).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(foil, PolyvinylChloride, 2)
                .input(foil, Aluminium)
                .fluidInputs(Polyethylene.getFluid(L / 2))
                .output(SMD_CAPACITOR, 12)
                .duration(80).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(foil, SiliconeRubber)
                .input(foil, Tantalum)
                .fluidInputs(Polyethylene.getFluid(L / 2))
                .output(SMD_CAPACITOR, 16)
                .duration(120).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(foil, PolyvinylChloride, 2)
                .input(foil, Tantalum)
                .fluidInputs(Polyethylene.getFluid(L / 2))
                .output(SMD_CAPACITOR, 24)
                .duration(120).EUt(VA[HV]).buildAndRegister();

        // SMD Inductor
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ring, NickelZincFerrite)
                .input(wireFine, Cupronickel, 4)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(SMD_INDUCTOR, 16)
                .duration(160).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ring, NickelZincFerrite)
                .input(wireFine, Tantalum, 4)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(SMD_INDUCTOR, 32)
                .duration(160).EUt(VA[HV]).buildAndRegister();

        // Advanced SMD Resistor
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, Graphene)
                .input(wireFine, Platinum, 4)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .output(ADVANCED_SMD_RESISTOR, 16)
                .EUt(3840).duration(160).buildAndRegister();

        // Advanced SMD Diode
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, IndiumGalliumPhosphide)
                .input(wireFine, NiobiumTitanium, 16)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .output(ADVANCED_SMD_DIODE, 64)
                .EUt(3840).duration(640).buildAndRegister();

        // Advanced SMD Transistor
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(foil, VanadiumGallium)
                .input(wireFine, HSSG, 8)
                .fluidInputs(Polybenzimidazole.getFluid(L))
                .output(ADVANCED_SMD_TRANSISTOR, 16)
                .EUt(3840).duration(160).buildAndRegister();

        // Advanced SMD Capacitor
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(foil, Polybenzimidazole, 2)
                .input(foil, HSSS)
                .fluidInputs(Polybenzimidazole.getFluid(L / 4))
                .output(ADVANCED_SMD_CAPACITOR, 16)
                .EUt(3840).duration(80).buildAndRegister();

        // Advanced SMD Inductor
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ring, HSSE)
                .input(wireFine, Palladium, 4)
                .fluidInputs(Polybenzimidazole.getFluid(L))
                .output(ADVANCED_SMD_INDUCTOR, 16)
                .EUt(3840).duration(160).buildAndRegister();

        // Carbon Fibers
        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(dust, Carbon, 4)
                .fluidInputs(Polyethylene.getFluid(L / 4))
                .output(CARBON_FIBERS)
                .duration(37).EUt(VA[LV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(dust, Carbon, 4)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L / 8))
                .output(CARBON_FIBERS, 2)
                .duration(37).EUt(VA[MV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(dust, Carbon, 4)
                .fluidInputs(Epoxy.getFluid(L / 16))
                .output(CARBON_FIBERS, 4)
                .duration(37).EUt(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(dust, Carbon, 8)
                .fluidInputs(Polybenzimidazole.getFluid(L / 16))
                .output(CARBON_FIBERS, 16)
                .duration(37).EUt(VA[EV]).buildAndRegister();

        // Crystal Circuit Components
        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .input(ENGRAVED_CRYSTAL_CHIP)
                .notConsumable(craftingLens, Color.Lime)
                .output(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).EUt(10000).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .input(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .notConsumable(craftingLens, Color.Blue)
                .output(CRYSTAL_SYSTEM_ON_CHIP)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).EUt(40000).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(gemExquisite, Emerald)
                .fluidInputs(Europium.getFluid(L / 9))
                .chancedOutput(RAW_CRYSTAL_CHIP, 1000, 2000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(320).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(gemExquisite, Olivine)
                .fluidInputs(Europium.getFluid(L / 9))
                .chancedOutput(RAW_CRYSTAL_CHIP, 1000, 2000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(320).buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(RAW_CRYSTAL_CHIP)
                .output(RAW_CRYSTAL_CHIP_PART, 9)
                .EUt(VA[HV]).duration(100).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(RAW_CRYSTAL_CHIP_PART)
                .fluidInputs(Europium.getFluid(L / 9))
                .output(RAW_CRYSTAL_CHIP)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(RAW_CRYSTAL_CHIP_PART)
                .fluidInputs(Mutagen.getFluid(250))
                .chancedOutput(RAW_CRYSTAL_CHIP, 8000, 250)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(RAW_CRYSTAL_CHIP_PART)
                .fluidInputs(BacterialSludge.getFluid(250))
                .chancedOutput(RAW_CRYSTAL_CHIP, 8000, 250)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(VA[HV]).buildAndRegister();

        // Quantum Parts
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(gem, EnderEye)
                .fluidInputs(Radon.getFluid(250))
                .output(QUANTUM_EYE)
                .duration(480).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(gem, NetherStar)
                .fluidInputs(Radon.getFluid(1250))
                .output(QUANTUM_STAR)
                .duration(1920).EUt(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input(QUANTUM_STAR)
                .fluidInputs(Neutronium.getFluid(L * 2))
                .output(GRAVI_STAR)
                .duration(480).EUt(VA[IV]).buildAndRegister();
    }

    private static void boardRecipes() {
        // Coated Board
        ModHandler.addShapedRecipe("coated_board", COATED_BOARD.getStackForm(3),
                "RRR", "PPP", "RRR",
                'R', STICKY_RESIN.getStackForm(),
                'P', new UnificationEntry(plate, Wood));

        ModHandler.addShapelessRecipe("coated_board_1x", COATED_BOARD.getStackForm(),
                new UnificationEntry(plate, Wood),
                STICKY_RESIN.getStackForm(),
                STICKY_RESIN.getStackForm());

        ModHandler.addShapedRecipe("basic_circuit_board", BASIC_CIRCUIT_BOARD.getStackForm(),
                "WWW", "WBW", "WWW",
                'W', new UnificationEntry(wireGtSingle, Copper),
                'B', COATED_BOARD.getStackForm());

        // Basic Circuit Board
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(foil, Copper, 4)
                .input(plate, Wood)
                .fluidInputs(Glue.getFluid(100))
                .output(BASIC_CIRCUIT_BOARD)
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        // Phenolic Board
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, Wood)
                .circuitMeta(1)
                .fluidInputs(Glue.getFluid(50))
                .output(PHENOLIC_BOARD)
                .duration(150).EUt(VA[LV]).buildAndRegister();

        // Good Circuit Board
        ModHandler.addShapedRecipe("good_circuit_board", GOOD_CIRCUIT_BOARD.getStackForm(),
                "WWW", "WBW", "WWW",
                'W', new UnificationEntry(wireGtSingle, Silver),
                'B', PHENOLIC_BOARD.getStackForm());

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(300)
                .input(foil, Silver, 4)
                .input(PHENOLIC_BOARD)
                .fluidInputs(SodiumPersulfate.getFluid(200))
                .output(GOOD_CIRCUIT_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(300)
                .input(foil, Silver, 4)
                .input(PHENOLIC_BOARD)
                .fluidInputs(Iron3Chloride.getFluid(100))
                .output(GOOD_CIRCUIT_BOARD)
                .buildAndRegister();

        // Plastic Board
        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(10)
                .input(plate, Polyethylene)
                .input(foil, Copper, 4)
                .fluidInputs(SulfuricAcid.getFluid(250))
                .output(PLASTIC_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(10)
                .input(plate, PolyvinylChloride)
                .input(foil, Copper, 4)
                .fluidInputs(SulfuricAcid.getFluid(250))
                .output(PLASTIC_BOARD, 2)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(10)
                .input(plate, Polytetrafluoroethylene)
                .input(foil, Copper, 4)
                .fluidInputs(SulfuricAcid.getFluid(250))
                .output(PLASTIC_BOARD, 4)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(10)
                .input(plate, Polybenzimidazole)
                .input(foil, Copper, 4)
                .fluidInputs(SulfuricAcid.getFluid(250))
                .output(PLASTIC_BOARD, 8)
                .buildAndRegister();

        // Plastic Circuit Board
        CHEMICAL_RECIPES.recipeBuilder().duration(600).EUt(VA[LV])
                .input(PLASTIC_BOARD)
                .input(foil, Copper, 6)
                .fluidInputs(SodiumPersulfate.getFluid(500))
                .output(PLASTIC_CIRCUIT_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(600).EUt(VA[LV])
                .input(PLASTIC_BOARD)
                .input(foil, Copper, 6)
                .fluidInputs(Iron3Chloride.getFluid(250))
                .output(PLASTIC_CIRCUIT_BOARD)
                .buildAndRegister();

        // Epoxy Board
        CHEMICAL_RECIPES.recipeBuilder().duration(600).EUt(VA[LV])
                .input(plate, Epoxy)
                .input(foil, Gold, 8)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(EPOXY_BOARD)
                .buildAndRegister();

        // Advanced Circuit Board
        CHEMICAL_RECIPES.recipeBuilder().duration(900).EUt(VA[LV])
                .input(EPOXY_BOARD)
                .input(foil, Electrum, 8)
                .fluidInputs(SodiumPersulfate.getFluid(1000))
                .output(ADVANCED_CIRCUIT_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(900).EUt(VA[LV])
                .input(EPOXY_BOARD)
                .input(foil, Electrum, 8)
                .fluidInputs(Iron3Chloride.getFluid(500))
                .output(ADVANCED_CIRCUIT_BOARD)
                .buildAndRegister();

        // Fiber Reinforced Epoxy Board
        CHEMICAL_BATH_RECIPES.recipeBuilder().duration(240).EUt(16)
                .input(wireFine, BorosilicateGlass)
                .fluidInputs(Epoxy.getFluid(L))
                .output(plate, ReinforcedEpoxyResin)
                .buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder().duration(240).EUt(16)
                .input(CARBON_FIBERS)
                .fluidInputs(Epoxy.getFluid(L))
                .output(plate, ReinforcedEpoxyResin)
                .buildAndRegister();

        // Borosilicate Glass Recipes
        EXTRUDER_RECIPES.recipeBuilder().duration(160).EUt(96)
                .input(ingot, BorosilicateGlass)
                .notConsumable(SHAPE_EXTRUDER_WIRE)
                .output(wireFine, BorosilicateGlass, 8)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(10)
                .input(plate, ReinforcedEpoxyResin)
                .input(foil, AnnealedCopper, 8)
                .fluidInputs(SulfuricAcid.getFluid(125))
                .output(FIBER_BOARD)
                .buildAndRegister();

        // Extreme Circuit Board
        CHEMICAL_RECIPES.recipeBuilder().duration(1200).EUt(VA[LV])
                .input(FIBER_BOARD)
                .input(foil, AnnealedCopper, 12)
                .fluidInputs(SodiumPersulfate.getFluid(2000))
                .output(EXTREME_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(1200).EUt(VA[LV])
                .input(FIBER_BOARD)
                .input(foil, AnnealedCopper, 12)
                .fluidInputs(Iron3Chloride.getFluid(1000))
                .output(EXTREME_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // Multi-Layer Fiber Reinforced Epoxy Board
        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(VA[HV])
                .input(FIBER_BOARD, 2)
                .input(foil, Palladium, 8)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(MULTILAYER_FIBER_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // Elite Circuit Board
        CHEMICAL_RECIPES.recipeBuilder().duration(1500).EUt(VA[MV])
                .input(MULTILAYER_FIBER_BOARD)
                .input(foil, Platinum, 8)
                .fluidInputs(SodiumPersulfate.getFluid(4000))
                .output(ELITE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(1500).EUt(VA[MV])
                .input(MULTILAYER_FIBER_BOARD)
                .input(foil, Platinum, 8)
                .fluidInputs(Iron3Chloride.getFluid(2000))
                .output(ELITE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // Wetware Board

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(160).EUt(VA[HV])
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L / 4))
                .output(PETRI_DISH)
                .buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(40).EUt(VA[HV])
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .fluidInputs(Polybenzimidazole.getFluid(L / 8))
                .output(PETRI_DISH, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(1200).EUt(VA[LuV])
                .input(MULTILAYER_FIBER_BOARD, 16)
                .input(PETRI_DISH)
                .input(ELECTRIC_PUMP_LuV)
                .input(circuit, Tier.IV)
                .input(foil, NiobiumTitanium, 16)
                .fluidInputs(SterileGrowthMedium.getFluid(4000))
                .output(WETWARE_BOARD, 16)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(1800).EUt(VA[HV])
                .input(WETWARE_BOARD)
                .input(foil, NiobiumTitanium, 32)
                .fluidInputs(SodiumPersulfate.getFluid(10000))
                .output(WETWARE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(1800).EUt(VA[HV])
                .input(WETWARE_BOARD)
                .input(foil, NiobiumTitanium, 32)
                .fluidInputs(Iron3Chloride.getFluid(5000))
                .output(WETWARE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();
    }

    private static void circuitRecipes() {
        int outputAmount = ConfigHolder.recipes.harderCircuitRecipes ? 1 : 2;

        // T1: Electronic ==============================================================================================

        // LV
        ModHandler.addShapedRecipe("electronic_circuit_lv", ELECTRONIC_CIRCUIT_LV.getStackForm(),
                "RPR", "VBV", "CCC",
                'R', RESISTOR.getStackForm(),
                'P', new UnificationEntry(plate, Steel),
                'V', VACUUM_TUBE.getStackForm(),
                'B', BASIC_CIRCUIT_BOARD.getStackForm(),
                'C', new UnificationEntry(cableGtSingle, RedAlloy));

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(16).duration(200)
                .input(BASIC_CIRCUIT_BOARD)
                .input(component, Component.Resistor, 2)
                .input(wireGtSingle, RedAlloy, 2)
                .input(circuit, Tier.ULV, 2)
                .output(ELECTRONIC_CIRCUIT_LV, outputAmount)
                .buildAndRegister();

        // MV
        ModHandler.addShapedRecipe("electronic_circuit_mv", ELECTRONIC_CIRCUIT_MV.getStackForm(),
                "DPD", "CBC", "WCW",
                'W', new UnificationEntry(wireGtSingle, Copper),
                'P', new UnificationEntry(plate, Steel),
                'C', ELECTRONIC_CIRCUIT_LV.getStackForm(),
                'B', GOOD_CIRCUIT_BOARD.getStackForm(),
                'D', DIODE.getStackForm());

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(16).duration(300)
                .input(GOOD_CIRCUIT_BOARD)
                .input(circuit, Tier.LV, 2)
                .input(component, Component.Diode, 2)
                .input(wireGtSingle, Copper, 2)
                .output(ELECTRONIC_CIRCUIT_MV)
                .buildAndRegister();

        // T2: Integrated ==============================================================================================

        // LV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(16).duration(200)
                .input(BASIC_CIRCUIT_BOARD)
                .input(INTEGRATED_LOGIC_CIRCUIT)
                .input(component, Component.Resistor, 2)
                .input(component, Component.Diode, 2)
                .input(wireFine, Copper, 2)
                .input(bolt, Tin, 2)
                .output(INTEGRATED_CIRCUIT_LV, outputAmount)
                .buildAndRegister();

        // MV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(24).duration(400)
                .input(GOOD_CIRCUIT_BOARD)
                .input(INTEGRATED_CIRCUIT_LV, 2)
                .input(component, Component.Resistor, 2)
                .input(component, Component.Diode, 2)
                .input(wireFine, Gold, 4)
                .input(bolt, Silver, 4)
                .output(INTEGRATED_CIRCUIT_MV, outputAmount)
                .buildAndRegister();

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LV]).duration(800)
                .input(INTEGRATED_CIRCUIT_MV, outputAmount) // a little generous for this first HV if harder recipes
                                                            // enabled
                .input(INTEGRATED_LOGIC_CIRCUIT, 2)
                .input(RANDOM_ACCESS_MEMORY, 2)
                .input(component, Component.Transistor, 4)
                .input(wireFine, Electrum, 8)
                .input(bolt, AnnealedCopper, 8)
                .output(INTEGRATED_CIRCUIT_HV)
                .buildAndRegister();

        // T2.5: Misc ==================================================================================================

        // NAND Chip ULV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[MV]).duration(300)
                .input(GOOD_CIRCUIT_BOARD)
                .input(SIMPLE_SYSTEM_ON_CHIP)
                .input(bolt, RedAlloy, 2)
                .input(wireFine, Tin, 2)
                .output(NAND_CHIP_ULV, outputAmount * 4)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[MV]).duration(300)
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(SIMPLE_SYSTEM_ON_CHIP)
                .input(bolt, RedAlloy, 2)
                .input(wireFine, Tin, 2)
                .output(NAND_CHIP_ULV, outputAmount * 6)
                .buildAndRegister();

        // Microprocessor LV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(60).duration(200)
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(CENTRAL_PROCESSING_UNIT)
                .input(component, Component.Resistor, 2)
                .input(component, Component.Capacitor, 2)
                .input(component, Component.Transistor, 2)
                .input(wireFine, Copper, 2)
                .output(MICROPROCESSOR_LV, ConfigHolder.recipes.harderCircuitRecipes ? 2 : 3)
                .buildAndRegister();

        // Microprocessor LV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(50)
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(SYSTEM_ON_CHIP)
                .input(wireFine, Copper, 2)
                .input(bolt, Tin, 2)
                .output(MICROPROCESSOR_LV, ConfigHolder.recipes.harderCircuitRecipes ? 3 : 6)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // T3: Processor ===============================================================================================

        // MV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(60).duration(200)
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(CENTRAL_PROCESSING_UNIT)
                .input(component, Component.Resistor, 4)
                .input(component, Component.Capacitor, 4)
                .input(component, Component.Transistor, 4)
                .input(wireFine, RedAlloy, 4)
                .output(PROCESSOR_MV, outputAmount)
                .buildAndRegister();

        // MV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(50)
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(SYSTEM_ON_CHIP)
                .input(wireFine, RedAlloy, 4)
                .input(bolt, AnnealedCopper, 4)
                .output(PROCESSOR_MV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(90).duration(400)
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(PROCESSOR_MV, 2)
                .input(component, Component.Inductor, 4)
                .input(component, Component.Capacitor, 8)
                .input(RANDOM_ACCESS_MEMORY, 4)
                .input(wireFine, RedAlloy, 8)
                .output(PROCESSOR_ASSEMBLY_HV)
                .solderMultiplier(2)
                .buildAndRegister();

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[MV]).duration(400)
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(PROCESSOR_ASSEMBLY_HV, 2)
                .input(component, Component.Diode, 4)
                .input(RANDOM_ACCESS_MEMORY, 4)
                .input(wireFine, Electrum, 16)
                .input(bolt, BlueAlloy, 16)
                .output(WORKSTATION_EV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // T4: Nano ====================================================================================================

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(200)
                .input(ADVANCED_CIRCUIT_BOARD)
                .input(NANO_CENTRAL_PROCESSING_UNIT)
                .input(SMD_RESISTOR, 8)
                .input(SMD_CAPACITOR, 8)
                .input(SMD_TRANSISTOR, 8)
                .input(wireFine, Electrum, 8)
                .output(NANO_PROCESSOR_HV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(100)
                .input(ADVANCED_CIRCUIT_BOARD)
                .input(NANO_CENTRAL_PROCESSING_UNIT)
                .input(ADVANCED_SMD_RESISTOR, 2)
                .input(ADVANCED_SMD_CAPACITOR, 2)
                .input(ADVANCED_SMD_TRANSISTOR, 2)
                .input(wireFine, Electrum, 8)
                .output(NANO_PROCESSOR_HV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // HV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(9600).duration(50)
                .input(ADVANCED_CIRCUIT_BOARD)
                .input(ADVANCED_SYSTEM_ON_CHIP)
                .input(wireFine, Electrum, 4)
                .input(bolt, Platinum, 4)
                .output(NANO_PROCESSOR_HV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(400)
                .input(ADVANCED_CIRCUIT_BOARD)
                .input(NANO_PROCESSOR_HV, 2)
                .input(SMD_INDUCTOR, 4)
                .input(SMD_CAPACITOR, 8)
                .input(RANDOM_ACCESS_MEMORY, 8)
                .input(wireFine, Electrum, 16)
                .output(NANO_PROCESSOR_ASSEMBLY_EV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(200)
                .input(ADVANCED_CIRCUIT_BOARD)
                .input(NANO_PROCESSOR_HV, 2)
                .input(ADVANCED_SMD_INDUCTOR)
                .input(ADVANCED_SMD_CAPACITOR, 2)
                .input(RANDOM_ACCESS_MEMORY, 8)
                .input(wireFine, Electrum, 16)
                .output(NANO_PROCESSOR_ASSEMBLY_EV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(400)
                .input(ADVANCED_CIRCUIT_BOARD)
                .input(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .input(SMD_DIODE, 8)
                .input(NOR_MEMORY_CHIP, 4)
                .input(RANDOM_ACCESS_MEMORY, 16)
                .input(wireFine, Electrum, 16)
                .output(NANO_COMPUTER_IV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(200)
                .input(ADVANCED_CIRCUIT_BOARD)
                .input(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .input(ADVANCED_SMD_DIODE, 2)
                .input(NOR_MEMORY_CHIP, 4)
                .input(RANDOM_ACCESS_MEMORY, 16)
                .input(wireFine, Electrum, 16)
                .output(NANO_COMPUTER_IV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // T5: Quantum =================================================================================================

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(200)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(QUBIT_CENTRAL_PROCESSING_UNIT)
                .input(NANO_CENTRAL_PROCESSING_UNIT)
                .input(SMD_CAPACITOR, 12)
                .input(SMD_TRANSISTOR, 12)
                .input(wireFine, Platinum, 12)
                .output(QUANTUM_PROCESSOR_EV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(100)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(QUBIT_CENTRAL_PROCESSING_UNIT)
                .input(NANO_CENTRAL_PROCESSING_UNIT)
                .input(ADVANCED_SMD_CAPACITOR, 3)
                .input(ADVANCED_SMD_TRANSISTOR, 3)
                .input(wireFine, Platinum, 12)
                .output(QUANTUM_PROCESSOR_EV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // EV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(38400).duration(50)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(ADVANCED_SYSTEM_ON_CHIP)
                .input(wireFine, Platinum, 12)
                .input(bolt, NiobiumTitanium, 8)
                .output(QUANTUM_PROCESSOR_EV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(400)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(QUANTUM_PROCESSOR_EV, 2)
                .input(SMD_INDUCTOR, 8)
                .input(SMD_CAPACITOR, 16)
                .input(RANDOM_ACCESS_MEMORY, 4)
                .input(wireFine, Platinum, 16)
                .output(QUANTUM_ASSEMBLY_IV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(200)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(QUANTUM_PROCESSOR_EV, 2)
                .input(ADVANCED_SMD_INDUCTOR, 2)
                .input(ADVANCED_SMD_CAPACITOR, 4)
                .input(RANDOM_ACCESS_MEMORY, 4)
                .input(wireFine, Platinum, 16)
                .output(QUANTUM_ASSEMBLY_IV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(400)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(QUANTUM_ASSEMBLY_IV, 2)
                .input(SMD_DIODE, 8)
                .input(NOR_MEMORY_CHIP, 4)
                .input(RANDOM_ACCESS_MEMORY, 16)
                .input(wireFine, Platinum, 32)
                .output(QUANTUM_COMPUTER_LUV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(200)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(QUANTUM_ASSEMBLY_IV, 2)
                .input(ADVANCED_SMD_DIODE, 2)
                .input(NOR_MEMORY_CHIP, 4)
                .input(RANDOM_ACCESS_MEMORY, 16)
                .input(wireFine, Platinum, 32)
                .output(QUANTUM_COMPUTER_LUV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // T6: Crystal =================================================================================================

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(9600).duration(200)
                .input(ELITE_CIRCUIT_BOARD)
                .input(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .input(NANO_CENTRAL_PROCESSING_UNIT, 2)
                .input(ADVANCED_SMD_CAPACITOR, 6)
                .input(ADVANCED_SMD_TRANSISTOR, 6)
                .input(wireFine, NiobiumTitanium, 8)
                .output(CRYSTAL_PROCESSOR_IV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // IV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(86000).duration(100)
                .input(ELITE_CIRCUIT_BOARD)
                .input(CRYSTAL_SYSTEM_ON_CHIP)
                .input(wireFine, NiobiumTitanium, 8)
                .input(bolt, YttriumBariumCuprate, 8)
                .output(CRYSTAL_PROCESSOR_IV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(9600).duration(400)
                .input(ELITE_CIRCUIT_BOARD)
                .input(CRYSTAL_PROCESSOR_IV, 2)
                .input(ADVANCED_SMD_INDUCTOR, 4)
                .input(ADVANCED_SMD_CAPACITOR, 8)
                .input(RANDOM_ACCESS_MEMORY, 24)
                .input(wireFine, NiobiumTitanium, 16)
                .output(CRYSTAL_ASSEMBLY_LUV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(9600).duration(400)
                .input(ELITE_CIRCUIT_BOARD)
                .input(CRYSTAL_ASSEMBLY_LUV, 2)
                .input(RANDOM_ACCESS_MEMORY, 4)
                .input(NOR_MEMORY_CHIP, 32)
                .input(NAND_MEMORY_CHIP, 64)
                .input(wireFine, NiobiumTitanium, 32)
                .solderMultiplier(2)
                .output(CRYSTAL_COMPUTER_ZPM)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // T7: Wetware =================================================================================================

        // Neuro Processing Unit
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(80000).duration(600)
                .input(WETWARE_CIRCUIT_BOARD)
                .input(STEM_CELLS, 16)
                .input(pipeFluid, Polybenzimidazole, 2)
                .input(plate, Electrum, 8)
                .input(foil, SiliconeRubber, 16)
                .input(bolt, HSSE, 8)
                .fluidInputs(SterileGrowthMedium.getFluid(250))
                .output(NEURO_PROCESSOR)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(38400).duration(200)
                .input(NEURO_PROCESSOR)
                .input(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .input(NANO_CENTRAL_PROCESSING_UNIT)
                .input(ADVANCED_SMD_CAPACITOR, 8)
                .input(ADVANCED_SMD_TRANSISTOR, 8)
                .input(wireFine, YttriumBariumCuprate, 8)
                .output(WETWARE_PROCESSOR_LUV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // SoC LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(150000).duration(100)
                .input(NEURO_PROCESSOR)
                .input(HIGHLY_ADVANCED_SOC)
                .input(wireFine, YttriumBariumCuprate, 8)
                .input(bolt, Naquadah, 8)
                .output(WETWARE_PROCESSOR_LUV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(38400).duration(400)
                .input(WETWARE_CIRCUIT_BOARD)
                .input(WETWARE_PROCESSOR_LUV, 2)
                .input(ADVANCED_SMD_INDUCTOR, 6)
                .input(ADVANCED_SMD_CAPACITOR, 12)
                .input(RANDOM_ACCESS_MEMORY, 24)
                .input(wireFine, YttriumBariumCuprate, 16)
                .solderMultiplier(2)
                .output(WETWARE_PROCESSOR_ASSEMBLY_ZPM)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // UV
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(38400).duration(400)
                .input(WETWARE_CIRCUIT_BOARD)
                .input(WETWARE_PROCESSOR_ASSEMBLY_ZPM, 2)
                .input(ADVANCED_SMD_DIODE, 8)
                .input(NOR_MEMORY_CHIP, 16)
                .input(RANDOM_ACCESS_MEMORY, 32)
                .input(wireFine, YttriumBariumCuprate, 24)
                .input(foil, Polybenzimidazole, 32)
                .input(plate, Europium, 4)
                .fluidInputs(SolderingAlloy.getFluid(1152))
                .output(WETWARE_SUPER_COMPUTER_UV)
                .stationResearch(b -> b
                        .researchStack(WETWARE_PROCESSOR_ASSEMBLY_ZPM.getStackForm())
                        .CWUt(16))
                .buildAndRegister();

        // Misc ========================================================================================================

        // Data Stick
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(ADVANCED_CIRCUIT_BOARD)
                .input(circuit, Tier.HV, 2)
                .input(RANDOM_ACCESS_MEMORY, 4)
                .input(NOR_MEMORY_CHIP, 16)
                .input(NAND_MEMORY_CHIP, 32)
                .input(wireFine, Platinum, 32)
                .output(TOOL_DATA_STICK)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(400).EUt(1200).buildAndRegister();

        // Data Orb
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(EXTREME_CIRCUIT_BOARD)
                .input(circuit, Tier.IV, 2)
                .input(RANDOM_ACCESS_MEMORY, 8)
                .input(NOR_MEMORY_CHIP, 32)
                .input(NAND_MEMORY_CHIP, 48)
                .input(wireFine, NiobiumTitanium, 32)
                .output(TOOL_DATA_ORB)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(400).EUt(9600).buildAndRegister();

        // Data Module
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(WETWARE_CIRCUIT_BOARD)
                .input(circuit, Tier.ZPM, 2)
                .input(RANDOM_ACCESS_MEMORY, 32)
                .input(NOR_MEMORY_CHIP, 64)
                .input(NAND_MEMORY_CHIP, 64)
                .input(wireFine, YttriumBariumCuprate, 32)
                .output(TOOL_DATA_MODULE)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .duration(400).EUt(38400).buildAndRegister();
    }
}
