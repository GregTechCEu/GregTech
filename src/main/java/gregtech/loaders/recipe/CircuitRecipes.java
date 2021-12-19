package gregtech.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials.Color;
import gregtech.api.unification.material.MarkerMaterials.Component;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.stack.UnificationEntry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

public class CircuitRecipes {

    private CircuitRecipes() {
    }

    public static void init() {
        waferRecipes();
        componentRecipes();
        boardRecipes();
        circuitRecipes();
    }

    private static void waferRecipes() {

        // Boules
        BLAST_RECIPES.recipeBuilder().duration(9000).EUt(VA[MV])
                .input(dust, Silicon, 32)
                .input(dustSmall, GalliumArsenide)
                .output(SILICON_BOULE)
                .blastFurnaceTemp(1784)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(12000).EUt(VA[HV])
                .input(dust, Silicon, 64)
                .input(dust, Glowstone, 8)
                .fluidInputs(Nitrogen.getFluid(8000))
                .output(GLOWSTONE_BOULE)
                .blastFurnaceTemp(2484)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(15000).EUt(VA[EV])
                .input(block, Silicon, 16)
                .input(ingot, Naquadah)
                .fluidInputs(Argon.getFluid(8000))
                .output(NAQUADAH_BOULE)
                .blastFurnaceTemp(5400)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(18000).EUt(VA[IV])
                .input(block, Silicon, 32)
                .input(ingot, Neutronium, 4)
                .fluidInputs(Xenon.getFluid(8000))
                .output(NEUTRONIUM_BOULE)
                .blastFurnaceTemp(6484)
                .buildAndRegister();

        // Boule cutting
        CUTTER_RECIPES.recipeBuilder().duration(400).EUt(16).input(SILICON_BOULE).output(SILICON_WAFER, 16).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(800).EUt(VA[MV]).input(GLOWSTONE_BOULE).output(GLOWSTONE_WAFER, 32).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(1600).EUt(VA[HV]).input(NAQUADAH_BOULE).output(NAQUADAH_WAFER, 64).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(2400).EUt(VA[EV]).input(NEUTRONIUM_BOULE).output(NEUTRONIUM_WAFER, 64).output(NEUTRONIUM_WAFER, 32).buildAndRegister();

        // Wafer engraving
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).input(SILICON_WAFER).notConsumable(craftingLens, Color.Red).output(INTEGRATED_LOGIC_CIRCUIT_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[HV]).input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Red).output(INTEGRATED_LOGIC_CIRCUIT_WAFER, 4).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).input(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Red).output(INTEGRATED_LOGIC_CIRCUIT_WAFER, 8).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(50).EUt(VA[IV]).input(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Red).output(INTEGRATED_LOGIC_CIRCUIT_WAFER, 16).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).input(SILICON_WAFER).notConsumable(craftingLens, Color.Green).output(RANDOM_ACCESS_MEMORY_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[HV]).input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Green).output(RANDOM_ACCESS_MEMORY_WAFER, 4).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).input(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Green).output(RANDOM_ACCESS_MEMORY_WAFER, 8).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(50).EUt(VA[IV]).input(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Green).output(RANDOM_ACCESS_MEMORY_WAFER, 16).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).input(SILICON_WAFER).notConsumable(craftingLens, Color.LightBlue).output(CENTRAL_PROCESSING_UNIT_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[HV]).input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.LightBlue).output(CENTRAL_PROCESSING_UNIT_WAFER, 4).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).input(NAQUADAH_WAFER).notConsumable(craftingLens, Color.LightBlue).output(CENTRAL_PROCESSING_UNIT_WAFER, 8).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(50).EUt(VA[IV]).input(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.LightBlue).output(CENTRAL_PROCESSING_UNIT_WAFER, 16).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).input(SILICON_WAFER).notConsumable(craftingLens, Color.Magenta).output(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[HV]).input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Magenta).output(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 4).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).input(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Magenta).output(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 8).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(50).EUt(VA[IV]).input(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Magenta).output(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 16).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).input(SILICON_WAFER).notConsumable(craftingLens, Color.Orange).output(LOW_POWER_INTEGRATED_CIRCUIT_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[HV]).input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Orange).output(LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 4).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).input(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Orange).output(LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 8).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(50).EUt(VA[IV]).input(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Orange).output(LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 16).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).input(SILICON_WAFER).notConsumable(craftingLens, Color.Cyan).output(SIMPLE_SYSTEM_ON_CHIP_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[HV]).input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Cyan).output(SIMPLE_SYSTEM_ON_CHIP_WAFER, 4).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).input(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Cyan).output(SIMPLE_SYSTEM_ON_CHIP_WAFER, 8).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(50).EUt(VA[IV]).input(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Cyan).output(SIMPLE_SYSTEM_ON_CHIP_WAFER, 16).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Gray).output(NAND_MEMORY_CHIP_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[EV]).input(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Gray).output(NAND_MEMORY_CHIP_WAFER, 4).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[IV]).input(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Gray).output(NAND_MEMORY_CHIP_WAFER, 8).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Pink).output(NOR_MEMORY_CHIP_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[EV]).input(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Pink).output(NOR_MEMORY_CHIP_WAFER, 4).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[IV]).input(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Pink).output(NOR_MEMORY_CHIP_WAFER, 8).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Brown).output(POWER_INTEGRATED_CIRCUIT_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[EV]).input(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Brown).output(POWER_INTEGRATED_CIRCUIT_WAFER, 4).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[IV]).input(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Brown).output(POWER_INTEGRATED_CIRCUIT_WAFER, 8).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Yellow).output(SYSTEM_ON_CHIP_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[EV]).input(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Yellow).output(SYSTEM_ON_CHIP_WAFER, 4).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[IV]).input(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Yellow).output(SYSTEM_ON_CHIP_WAFER, 8).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[EV]).input(NAQUADAH_WAFER).notConsumable(craftingLens, Color.Purple).output(ADVANCED_SYSTEM_ON_CHIP_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[IV]).input(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Purple).output(ADVANCED_SYSTEM_ON_CHIP_WAFER, 2).buildAndRegister();

        // Can replace this with a Quantum Star/Eye Lens if desired
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[IV]).input(NEUTRONIUM_WAFER).notConsumable(craftingLens, Color.Black).output(HIGHLY_ADVANCED_SOC_WAFER).buildAndRegister();

        // Wafer chemical refining recipes
        CHEMICAL_RECIPES.recipeBuilder().duration(1200).EUt(VA[IV])
                .input(POWER_INTEGRATED_CIRCUIT_WAFER)
                .input(dust, IndiumGalliumPhosphide, 2)
                .fluidInputs(RedAlloy.getFluid(L * 2))
                .output(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(1200).EUt(VA[EV])
                .input(CENTRAL_PROCESSING_UNIT_WAFER)
                .input(CARBON_FIBERS, 16)
                .fluidInputs(Glowstone.getFluid(L * 4))
                .output(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(900).EUt(VA[EV])
                .input(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .input(QUANTUM_EYE, 2)
                .fluidInputs(GalliumArsenide.getFluid(L * 2))
                .output(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(1200).EUt(VA[EV])
                .input(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .input(dust, IndiumGalliumPhosphide)
                .fluidInputs(Radon.getFluid(50))
                .output(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER)
                .buildAndRegister();

        // Wafer cutting
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[IV]).input(HIGHLY_ADVANCED_SOC_WAFER).output(HIGHLY_ADVANCED_SOC, 6).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[EV]).input(ADVANCED_SYSTEM_ON_CHIP_WAFER).output(ADVANCED_SYSTEM_ON_CHIP, 6).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).input(SYSTEM_ON_CHIP_WAFER).output(SYSTEM_ON_CHIP, 6).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(64).input(SIMPLE_SYSTEM_ON_CHIP_WAFER).output(SIMPLE_SYSTEM_ON_CHIP, 6).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(96).input(RANDOM_ACCESS_MEMORY_WAFER).output(RANDOM_ACCESS_MEMORY, 32).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[EV]).input(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER).output(QUBIT_CENTRAL_PROCESSING_UNIT, 4).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).input(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER).output(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 6).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).input(LOW_POWER_INTEGRATED_CIRCUIT_WAFER).output(LOW_POWER_INTEGRATED_CIRCUIT, 4).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[EV]).input(POWER_INTEGRATED_CIRCUIT_WAFER).output(POWER_INTEGRATED_CIRCUIT, 4).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[IV]).input(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER).output(HIGH_POWER_INTEGRATED_CIRCUIT, 2).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(192).input(NOR_MEMORY_CHIP_WAFER).output(NOR_MEMORY_CHIP, 16).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(192).input(NAND_MEMORY_CHIP_WAFER).output(NAND_MEMORY_CHIP, 32).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).input(CENTRAL_PROCESSING_UNIT_WAFER).output(CENTRAL_PROCESSING_UNIT, 8).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(64).input(INTEGRATED_LOGIC_CIRCUIT_WAFER).output(INTEGRATED_LOGIC_CIRCUIT, 8).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).input(NANO_CENTRAL_PROCESSING_UNIT_WAFER).output(NANO_CENTRAL_PROCESSING_UNIT, 8).buildAndRegister();
    }

    private static void componentRecipes() {

        // Vacuum Tube
        ModHandler.addShapedRecipe("vacuum_tube", VACUUM_TUBE.getStackForm(),
                "PTP", "WWW",
                'P', new UnificationEntry(stick, Steel),
                'T', GLASS_TUBE.getStackForm(),
                'W', new UnificationEntry(wireGtSingle, Copper));

        ASSEMBLER_RECIPES.recipeBuilder().duration(120).EUt(VA[ULV]).circuitMeta(1)
                .input(GLASS_TUBE)
                .input(bolt, Steel, 2)
                .input(wireGtSingle, Copper, 2)
                .output(VACUUM_TUBE, 2)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(40).EUt(VA[ULV])
                .input(GLASS_TUBE)
                .input(bolt, Steel)
                .input(wireGtSingle, Copper, 2)
                .fluidInputs(RedAlloy.getFluid(18))
                .output(VACUUM_TUBE, 4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(120).EUt(VA[ULV]).circuitMeta(1)
                .input(GLASS_TUBE)
                .input(bolt, Steel, 2)
                .input(wireGtSingle, AnnealedCopper, 2)
                .output(VACUUM_TUBE, 4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(40).EUt(VA[ULV])
                .input(GLASS_TUBE)
                .input(bolt, Steel)
                .input(wireGtSingle, AnnealedCopper, 2)
                .fluidInputs(RedAlloy.getFluid(18))
                .output(VACUUM_TUBE, 6)
                .buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(160).EUt(16)
                .input(dust, Glass)
                .notConsumable(SHAPE_MOLD_BALL)
                .output(GLASS_TUBE)
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(80).EUt(VA[ULV])
                .input(dust, Glass)
                .notConsumable(SHAPE_MOLD_BALL)
                .output(GLASS_TUBE)
                .buildAndRegister();

        // Resistor
        ModHandler.addShapedRecipe("resistor_wire", RESISTOR.getStackForm(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', RUBBER_DROP.getStackForm(),
                'W', new UnificationEntry(wireGtSingle, Copper),
                'C', new UnificationEntry(dust, Coal));

        ModHandler.addShapedRecipe("resistor_wire_fine", RESISTOR.getStackForm(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', RUBBER_DROP.getStackForm(),
                'W', new UnificationEntry(wireFine, Copper),
                'C', new UnificationEntry(dust, Coal));

        ModHandler.addShapedRecipe("resistor_wire_charcoal", RESISTOR.getStackForm(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', RUBBER_DROP.getStackForm(),
                'W', new UnificationEntry(wireGtSingle, Copper),
                'C', new UnificationEntry(dust, Charcoal));

        ModHandler.addShapedRecipe("resistor_wire_fine_charcoal", RESISTOR.getStackForm(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', RUBBER_DROP.getStackForm(),
                'W', new UnificationEntry(wireFine, Copper),
                'C', new UnificationEntry(dust, Charcoal));

        ModHandler.addShapedRecipe("resistor_wire_carbon", RESISTOR.getStackForm(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', RUBBER_DROP.getStackForm(),
                'W', new UnificationEntry(wireGtSingle, Copper),
                'C', new UnificationEntry(dust, Carbon));

        ModHandler.addShapedRecipe("resistor_wire_fine_carbon", RESISTOR.getStackForm(2),
                "SPS", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'S', RUBBER_DROP.getStackForm(),
                'W', new UnificationEntry(wireFine, Copper),
                'C', new UnificationEntry(dust, Carbon));

        ASSEMBLER_RECIPES.recipeBuilder().duration(160).EUt(6)
                .input(dust, Coal)
                .input(wireFine, Copper, 4)
                .output(RESISTOR, 4)
                .fluidInputs(Glue.getFluid(100))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(160).EUt(6)
                .input(dust, Charcoal)
                .input(wireFine, Copper, 4)
                .output(RESISTOR, 4)
                .fluidInputs(Glue.getFluid(100))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(160).EUt(6)
                .input(dust, Carbon)
                .input(wireFine, Copper, 4)
                .output(RESISTOR, 4)
                .fluidInputs(Glue.getFluid(100))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(160).EUt(6)
                .input(dust, Coal)
                .input(wireFine, AnnealedCopper, 4)
                .output(RESISTOR, 8)
                .fluidInputs(Glue.getFluid(100))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(160).EUt(6)
                .input(dust, Charcoal)
                .input(wireFine, AnnealedCopper, 4)
                .output(RESISTOR, 8)
                .fluidInputs(Glue.getFluid(100))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(160).EUt(6)
                .input(dust, Carbon)
                .input(wireFine, AnnealedCopper, 4)
                .output(RESISTOR, 8)
                .fluidInputs(Glue.getFluid(100))
                .buildAndRegister();

        // Capacitor
        ASSEMBLER_RECIPES.recipeBuilder().duration(320).EUt(VA[MV])
                .input(foil, Polyethylene)
                .input(foil, Aluminium, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(CAPACITOR, 8)
                .buildAndRegister();

        // Transistor
        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(24)
                .input(plate, Silicon)
                .input(wireFine, Tin, 6)
                .fluidInputs(Polyethylene.getFluid(144))
                .output(TRANSISTOR, 8)
                .buildAndRegister();

        // Diode
        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(VA[LV])
                .input(wireFine, Copper, 4)
                .input(dustSmall, GalliumArsenide)
                .fluidInputs(Glass.getFluid(L))
                .output(DIODE)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(VA[LV])
                .input(wireFine, Copper, 4)
                .input(dustSmall, GalliumArsenide)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(DIODE, 2)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(VA[LV])
                .input(wireFine, Copper, 4)
                .input(SILICON_WAFER)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(DIODE, 4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(VA[LV])
                .input(wireFine, AnnealedCopper, 4)
                .input(dustSmall, GalliumArsenide)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(DIODE, 6)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(VA[LV])
                .input(wireFine, AnnealedCopper, 4)
                .input(SILICON_WAFER)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(DIODE, 8)
                .buildAndRegister();

        // Small Coil
        ASSEMBLER_RECIPES.recipeBuilder().duration(320).EUt(60)
                .input(ring, NickelZincFerrite)
                .input(wireFine, Copper, 2)
                .fluidInputs(Polyethylene.getFluid(L / 4))
                .output(SMALL_COIL, 4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(320).EUt(60)
                .input(ring, NickelZincFerrite)
                .input(wireFine, AnnealedCopper, 2)
                .fluidInputs(Polyethylene.getFluid(L / 4))
                .output(SMALL_COIL, 8)
                .buildAndRegister();

        // SMD Resistor
        ASSEMBLER_RECIPES.recipeBuilder().duration(160).EUt(VA[HV])
                .input(dust, Carbon)
                .input(wireFine, Electrum, 4)
                .fluidInputs(Polyethylene.getFluid(L * 2))
                .output(SMD_RESISTOR, 16)
                .buildAndRegister();

        // SMD Diode
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[HV])
                .input(dust, GalliumArsenide)
                .input(wireFine, Platinum, 8)
                .fluidInputs(Polyethylene.getFluid(L * 2))
                .output(SMD_DIODE, 32)
                .buildAndRegister();

        // SMD Transistor
        ASSEMBLER_RECIPES.recipeBuilder().duration(160).EUt(VA[HV])
                .input(foil, Gallium)
                .input(wireFine, AnnealedCopper, 8)
                .fluidInputs(Polyethylene.getFluid(L))
                .output(SMD_TRANSISTOR, 16)
                .buildAndRegister();

        // SMD Capacitor
        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(VA[HV])
                .input(foil, SiliconeRubber)
                .input(foil, Aluminium)
                .fluidInputs(Polyethylene.getFluid(72))
                .output(SMD_CAPACITOR, 8)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(VA[HV])
                .input(foil, PolyvinylChloride, 2)
                .input(foil, Aluminium)
                .fluidInputs(Polyethylene.getFluid(72))
                .output(SMD_CAPACITOR, 12)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(120).EUt(VA[HV])
                .input(foil, SiliconeRubber)
                .input(foil, Tantalum)
                .fluidInputs(Polyethylene.getFluid(72))
                .output(SMD_CAPACITOR, 16)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(120).EUt(VA[HV])
                .input(foil, PolyvinylChloride, 2)
                .input(foil, Tantalum)
                .fluidInputs(Polyethylene.getFluid(72))
                .output(SMD_CAPACITOR, 24)
                .buildAndRegister();

        // Advanced SMD Resistor
        ASSEMBLER_RECIPES.recipeBuilder().EUt(384).duration(160)
                .input(dust, Graphene)
                .input(wireFine, Platinum, 4)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .output(ADVANCED_SMD_RESISTOR, 16)
                .buildAndRegister();

        // Advanced SMD Diode
        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[HV]).duration(150)
                .input(dustSmall, IndiumGalliumPhosphide)
                .input(wireFine, NiobiumTitanium, 4)
                .fluidInputs(Polybenzimidazole.getFluid(L / 2))
                .output(ADVANCED_SMD_DIODE, 16)
                .buildAndRegister();

        // Advanced SMD Transistor
        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[HV]).duration(160)
                .input(foil, VanadiumGallium)
                .input(wireFine, HSSG, 8)
                .fluidInputs(Polybenzimidazole.getFluid(L))
                .output(ADVANCED_SMD_TRANSISTOR, 16)
                .buildAndRegister();

        // Advanced SMD Capacitor
        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[HV]).duration(80)
                .input(foil, Polybenzimidazole, 2)
                .input(foil, HSSS)
                .fluidInputs(Polybenzimidazole.getFluid(L / 4))
                .output(ADVANCED_SMD_CAPACITOR, 16)
                .buildAndRegister();

        // Carbon Fibers
        AUTOCLAVE_RECIPES.recipeBuilder().duration(37).EUt(VA[LV])
                .input(dust, Carbon, 4)
                .fluidInputs(Polyethylene.getFluid(36))
                .output(CARBON_FIBERS)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder().duration(37).EUt(VA[MV])
                .input(dust, Carbon, 4)
                .fluidInputs(Polytetrafluoroethylene.getFluid(18))
                .output(CARBON_FIBERS, 2)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder().duration(37).EUt(VA[HV])
                .input(dust, Carbon, 4)
                .fluidInputs(Epoxy.getFluid(9))
                .output(CARBON_FIBERS, 4)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder().duration(37).EUt(VA[EV])
                .input(dust, Carbon, 8)
                .fluidInputs(Polybenzimidazole.getFluid(9))
                .output(CARBON_FIBERS, 16)
                .buildAndRegister();

        // Crystal Circuit Components
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(256).EUt(VA[HV])
                .input(LAPOTRON_CRYSTAL)
                .notConsumable(craftingLens, Color.Blue)
                .output(ENGRAVED_LAPOTRON_CHIP, 3)
                .buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(100).EUt(10000)
                .input(ENGRAVED_CRYSTAL_CHIP)
                .notConsumable(craftingLens, Color.Lime)
                .output(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(100).EUt(40000)
                .input(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .notConsumable(craftingLens, Color.Blue)
                .output(CRYSTAL_SYSTEM_ON_CHIP)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder().duration(12000).EUt(320)
                .input(gemExquisite, Emerald)
                .fluidInputs(Europium.getFluid(L / 9))
                .chancedOutput(RAW_CRYSTAL_CHIP, 1000, 2000)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder().duration(12000).EUt(320)
                .input(gemExquisite, Olivine)
                .fluidInputs(Europium.getFluid(L / 9))
                .chancedOutput(RAW_CRYSTAL_CHIP, 1000, 2000)
                .buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder().EUt(VA[HV]).duration(100)
                .input(RAW_CRYSTAL_CHIP)
                .output(RAW_CRYSTAL_CHIP_PART, 9)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder().duration(12000).EUt(VA[HV])
                .input(RAW_CRYSTAL_CHIP_PART)
                .fluidInputs(Europium.getFluid(L / 9))
                .chancedOutput(RAW_CRYSTAL_CHIP, 8000, 250)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder().duration(12000).EUt(VA[HV])
                .input(RAW_CRYSTAL_CHIP_PART)
                .fluidInputs(Mutagen.getFluid(250))
                .chancedOutput(RAW_CRYSTAL_CHIP, 8000, 250)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder().duration(12000).EUt(VA[HV])
                .input(RAW_CRYSTAL_CHIP_PART)
                .fluidInputs(BacterialSludge.getFluid(250))
                .chancedOutput(RAW_CRYSTAL_CHIP, 8000, 250)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).blastFurnaceTemp(5000)
                .input(plate, Emerald)
                .input(RAW_CRYSTAL_CHIP)
                .fluidInputs(Helium.getFluid(1000))
                .output(ENGRAVED_CRYSTAL_CHIP)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).blastFurnaceTemp(5000)
                .input(plate, Olivine)
                .input(RAW_CRYSTAL_CHIP)
                .fluidInputs(Helium.getFluid(1000))
                .output(ENGRAVED_CRYSTAL_CHIP)
                .buildAndRegister();

        // Quantum Parts
        CHEMICAL_BATH_RECIPES.recipeBuilder().duration(480).EUt(384)
                .input(gem, EnderEye)
                .fluidInputs(Radon.getFluid(250))
                .output(QUANTUM_EYE)
                .buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder().duration(1920).EUt(384)
                .input(gem, NetherStar)
                .fluidInputs(Radon.getFluid(1250))
                .output(QUANTUM_STAR)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder().duration(480).EUt(VA[IV])
                .input(gem, NetherStar)
                .fluidInputs(Neutronium.getFluid(L * 2))
                .output(GRAVI_STAR)
                .buildAndRegister();

    }

    private static void boardRecipes() {

        // Coated Board
        ModHandler.addShapedRecipe("coated_board", COATED_BOARD.getStackForm(3),
                "RRR", "PPP", "RRR",
                'R', RUBBER_DROP.getStackForm(),
                'P', new UnificationEntry(plate, Wood));

        ModHandler.addShapelessRecipe("coated_board_1x", COATED_BOARD.getStackForm(),
                new UnificationEntry(plate, Wood),
                RUBBER_DROP.getStackForm(),
                RUBBER_DROP.getStackForm());

        ModHandler.addShapedRecipe("basic_circuit_board", BASIC_CIRCUIT_BOARD.getStackForm(),
                "WWW", "WBW", "WWW",
                'W', new UnificationEntry(wireGtSingle, Copper),
                'B', COATED_BOARD.getStackForm());

        // Basic Circuit Board
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(foil, Copper, 4)
                .input(plate, Wood)
                .fluidInputs(Glue.getFluid(100))
                .output(BASIC_CIRCUIT_BOARD)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(foil, Copper, 4)
                .input(plate, Wood)
                .fluidInputs(Polyethylene.getFluid(36))
                .output(BASIC_CIRCUIT_BOARD, 2)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(foil, Copper, 4)
                .input(plate, Wood)
                .fluidInputs(Polytetrafluoroethylene.getFluid(18))
                .output(BASIC_CIRCUIT_BOARD, 2)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(foil, Copper, 4)
                .input(plate, Wood)
                .fluidInputs(Epoxy.getFluid(18))
                .output(BASIC_CIRCUIT_BOARD, 3)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(foil, Copper, 4)
                .input(plate, Wood)
                .fluidInputs(Polybenzimidazole.getFluid(9))
                .output(BASIC_CIRCUIT_BOARD, 4)
                .buildAndRegister();

        // Phenolic Board
        ASSEMBLER_RECIPES.recipeBuilder().duration(30).EUt(VA[ULV])
                .input(dust, Wood)
                .notConsumable(SHAPE_MOLD_PLATE)
                .fluidInputs(Glue.getFluid(50))
                .output(PHENOLIC_BOARD)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(30).EUt(VA[ULV])
                .input(dust, Wood)
                .notConsumable(SHAPE_MOLD_PLATE)
                .fluidInputs(BisphenolA.getFluid(18))
                .output(PHENOLIC_BOARD, 2)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(30).EUt(VA[ULV])
                .input(dust, Wood)
                .notConsumable(SHAPE_MOLD_PLATE)
                .fluidInputs(Epoxy.getFluid(18))
                .output(PHENOLIC_BOARD, 3)
                .buildAndRegister();

        // Good Circuit Board
        ModHandler.addShapedRecipe("good_circuit_board", GOOD_CIRCUIT_BOARD.getStackForm(),
                "WWW", "WBW", "WWW",
                'W', new UnificationEntry(wireGtSingle, Gold),
                'B', PHENOLIC_BOARD.getStackForm());

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(300)
                .input(foil, Gold, 4)
                .input(PHENOLIC_BOARD)
                .fluidInputs(SodiumPersulfate.getFluid(250))
                .output(GOOD_CIRCUIT_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(75)
                .input(foil, Gold, 4)
                .input(PHENOLIC_BOARD)
                .fluidInputs(Iron3Chloride.getFluid(125))
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

        CHEMICAL_RECIPES.recipeBuilder().duration(150).EUt(VA[HV])
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

        CHEMICAL_RECIPES.recipeBuilder().duration(225).EUt(VA[HV])
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
        CHEMICAL_RECIPES.recipeBuilder().duration(300).EUt(VA[HV])
                .input(FIBER_BOARD)
                .input(foil, AnnealedCopper, 12)
                .fluidInputs(SodiumPersulfate.getFluid(2000))
                .output(EXTREME_CIRCUIT_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(150).EUt(VA[EV])
                .input(FIBER_BOARD)
                .input(foil, AnnealedCopper, 12)
                .fluidInputs(Iron3Chloride.getFluid(1000))
                .output(EXTREME_CIRCUIT_BOARD)
                .buildAndRegister();

        // Multi-Layer Fiber Reinforced Epoxy Board
        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(VA[HV])
                .input(FIBER_BOARD, 2)
                .input(foil, Platinum, 8)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(MULTILAYER_FIBER_BOARD)
                .buildAndRegister();

        // Elite Circuit Board
        CHEMICAL_RECIPES.recipeBuilder().duration(300).EUt(VA[EV])
                .input(MULTILAYER_FIBER_BOARD)
                .input(foil, Platinum, 8)
                .fluidInputs(SodiumPersulfate.getFluid(4000))
                .output(ELITE_CIRCUIT_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(150).EUt(VA[IV])
                .input(MULTILAYER_FIBER_BOARD)
                .input(foil, Platinum, 8)
                .fluidInputs(Iron3Chloride.getFluid(2000))
                .output(ELITE_CIRCUIT_BOARD)
                .buildAndRegister();

        // Wetware Board

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(160).EUt(16)
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
                .input(ELECTRIC_PUMP_LUV)
                .input(SENSOR_IV)
                .input(circuit, Tier.Elite)
                .input(foil, NiobiumTitanium, 16)
                .fluidInputs(SterileGrowthMedium.getFluid(4000))
                .output(WETWARE_BOARD, 16)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(300).EUt(VA[IV])
                .input(WETWARE_BOARD)
                .input(foil, NiobiumTitanium, 32)
                .fluidInputs(SodiumPersulfate.getFluid(8000))
                .output(WETWARE_CIRCUIT_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(150).EUt(VA[LuV])
                .input(WETWARE_BOARD)
                .input(foil, NiobiumTitanium, 32)
                .fluidInputs(Iron3Chloride.getFluid(4000))
                .output(WETWARE_CIRCUIT_BOARD)
                .buildAndRegister();
    }

    private static void circuitRecipes() {

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
                .input(circuit, Tier.Primitive, 2)
                .output(ELECTRONIC_CIRCUIT_LV, 2)
                .buildAndRegister();

        // MV
        ModHandler.addShapedRecipe("electronic_circuit_mv", ELECTRONIC_CIRCUIT_MV.getStackForm(),
                "DPD", "CBC", "WWW",
                'W', new UnificationEntry(wireGtSingle, Copper),
                'P', new UnificationEntry(plate, Steel),
                'C', ELECTRONIC_CIRCUIT_LV.getStackForm(),
                'B', GOOD_CIRCUIT_BOARD.getStackForm(),
                'D', DIODE.getStackForm());

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LV]).duration(300)
                .input(GOOD_CIRCUIT_BOARD)
                .input(circuit, Tier.Basic, 2)
                .input(component, Component.Diode, 2)
                .input(wireGtSingle, Copper, 2)
                .output(ELECTRONIC_CIRCUIT_MV, 2)
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
                .output(INTEGRATED_CIRCUIT_LV, 2)
                .buildAndRegister();

        // MV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(24).duration(400)
                .input(GOOD_CIRCUIT_BOARD)
                .input(INTEGRATED_CIRCUIT_LV, 2)
                .input(component, Component.Resistor, 4)
                .input(component, Component.Diode, 4)
                .input(wireFine, Gold, 4)
                .input(bolt, Silver, 4)
                .output(INTEGRATED_CIRCUIT_MV)
                .buildAndRegister();

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LV]).duration(800)
                .input(INTEGRATED_CIRCUIT_MV)
                .input(INTEGRATED_LOGIC_CIRCUIT, 2)
                .input(RANDOM_ACCESS_MEMORY, 2)
                .input(component, Component.Transistor, 4)
                .input(wireFine, Electrum, 8)
                .input(bolt, AnnealedCopper, 8)
                .output(INTEGRATED_CIRCUIT_HV)
                .buildAndRegister();

        // T2.5: Misc ==================================================================================================

        // NAND Chip ULV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LV]).duration(300)
                .input(GOOD_CIRCUIT_BOARD)
                .input(SIMPLE_SYSTEM_ON_CHIP)
                .input(bolt, RedAlloy, 2)
                .input(wireFine, Tin, 2)
                .output(NAND_CHIP_ULV, 8)
                .buildAndRegister();

        // Microprocessor LV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(60).duration(200)
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(CENTRAL_PROCESSING_UNIT)
                .input(component, Component.Resistor, 2)
                .input(component, Component.Capacitor, 2)
                .input(component, Component.Transistor, 2)
                .input(wireFine, Copper, 2)
                .output(MICROPROCESSOR_LV, 3)
                .buildAndRegister();

        // Microprocessor LV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(50)
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(SYSTEM_ON_CHIP)
                .input(wireFine, Copper, 2)
                .input(bolt, Tin, 2)
                .output(MICROPROCESSOR_LV, 6)
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
                .output(PROCESSOR_MV, 2)
                .buildAndRegister();

        // MV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(50)
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(SYSTEM_ON_CHIP)
                .input(wireFine, RedAlloy, 4)
                .input(bolt, AnnealedCopper, 4)
                .output(PROCESSOR_MV, 4)
                .buildAndRegister();

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[MV]).duration(400)
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(PROCESSOR_MV, 2)
                .input(SMALL_COIL, 4)
                .input(component, Component.Capacitor, 8)
                .input(RANDOM_ACCESS_MEMORY, 4)
                .input(wireFine, RedAlloy, 8)
                .output(PROCESSOR_ASSEMBLY_HV, 2)
                .solderMultiplier(2)
                .buildAndRegister();

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[MV]).duration(400)
                .input(PLASTIC_CIRCUIT_BOARD)
                .input(PROCESSOR_ASSEMBLY_HV, 2)
                .input(component, Component.Diode, 4)
                .input(RANDOM_ACCESS_MEMORY, 4)
                .input(wireFine, Electrum, 16)
                .input(bolt, Platinum, 16)
                .output(WORKSTATION_EV)
                .solderMultiplier(2)
                .buildAndRegister();

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[HV]).duration(800)
                .input(frameGt, Aluminium, 2)
                .input(WORKSTATION_EV, 2)
                .input(SMALL_COIL, 12)
                .input(component, Component.Capacitor, 16)
                .input(RANDOM_ACCESS_MEMORY, 16)
                .input(wireGtSingle, AnnealedCopper, 16)
                .output(MAINFRAME_IV)
                .solderMultiplier(4)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[HV]).duration(400)
                .input(frameGt, Aluminium, 2)
                .input(WORKSTATION_EV, 2)
                .input(SMALL_COIL, 12)
                .input(ADVANCED_SMD_CAPACITOR, 4)
                .input(RANDOM_ACCESS_MEMORY, 16)
                .input(wireGtSingle, AnnealedCopper, 16)
                .output(MAINFRAME_IV)
                .solderMultiplier(4)
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
                .output(NANO_PROCESSOR_HV, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(100)
                .input(ADVANCED_CIRCUIT_BOARD)
                .input(NANO_CENTRAL_PROCESSING_UNIT)
                .input(ADVANCED_SMD_RESISTOR, 2)
                .input(ADVANCED_SMD_CAPACITOR, 2)
                .input(ADVANCED_SMD_TRANSISTOR, 2)
                .input(wireFine, Electrum, 8)
                .output(NANO_PROCESSOR_HV, 2)
                .buildAndRegister();

        // HV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(9600).duration(50)
                .input(ADVANCED_CIRCUIT_BOARD)
                .input(ADVANCED_SYSTEM_ON_CHIP)
                .input(wireFine, Electrum, 4)
                .input(bolt, Platinum, 4)
                .output(NANO_PROCESSOR_HV, 4)
                .buildAndRegister();

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(400)
                .input(ADVANCED_CIRCUIT_BOARD)
                .input(NANO_PROCESSOR_HV, 2)
                .input(SMALL_COIL, 8)
                .input(SMD_CAPACITOR, 8)
                .input(RANDOM_ACCESS_MEMORY, 8)
                .input(wireFine, Electrum, 16)
                .output(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .solderMultiplier(2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(200)
                .input(ADVANCED_CIRCUIT_BOARD)
                .input(NANO_PROCESSOR_HV, 2)
                .input(SMALL_COIL, 8)
                .input(ADVANCED_SMD_CAPACITOR, 2)
                .input(RANDOM_ACCESS_MEMORY, 8)
                .input(wireFine, Electrum, 16)
                .output(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .solderMultiplier(2)
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
                .buildAndRegister();

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[EV]).duration(800)
                .input(frameGt, Aluminium, 2)
                .input(NANO_COMPUTER_IV, 2)
                .input(SMALL_COIL, 16)
                .input(SMD_CAPACITOR, 32)
                .input(RANDOM_ACCESS_MEMORY, 16)
                .input(wireGtSingle, AnnealedCopper, 32)
                .output(NANO_MAINFRAME_LUV)
                .solderMultiplier(4)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[EV]).duration(400)
                .input(frameGt, Aluminium, 2)
                .input(NANO_COMPUTER_IV, 2)
                .input(SMALL_COIL, 16)
                .input(ADVANCED_SMD_CAPACITOR, 8)
                .input(RANDOM_ACCESS_MEMORY, 16)
                .input(wireGtSingle, AnnealedCopper, 32)
                .output(NANO_MAINFRAME_LUV)
                .solderMultiplier(4)
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
                .output(QUANTUM_PROCESSOR_EV, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(100)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(QUBIT_CENTRAL_PROCESSING_UNIT)
                .input(NANO_CENTRAL_PROCESSING_UNIT)
                .input(ADVANCED_SMD_CAPACITOR, 3)
                .input(ADVANCED_SMD_TRANSISTOR, 3)
                .input(wireFine, Platinum, 12)
                .output(QUANTUM_PROCESSOR_EV, 2)
                .buildAndRegister();

        // EV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(38400).duration(50)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(ADVANCED_SYSTEM_ON_CHIP)
                .input(wireFine, Platinum, 12)
                .input(bolt, NiobiumTitanium, 8)
                .output(QUANTUM_PROCESSOR_EV, 4)
                .buildAndRegister();

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(400)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(QUANTUM_PROCESSOR_EV, 2)
                .input(SMALL_COIL, 12)
                .input(SMD_CAPACITOR, 16)
                .input(RANDOM_ACCESS_MEMORY, 4)
                .input(wireFine, Platinum, 16)
                .output(QUANTUM_ASSEMBLY_IV, 2)
                .solderMultiplier(2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(200)
                .input(EXTREME_CIRCUIT_BOARD)
                .input(QUANTUM_PROCESSOR_EV, 2)
                .input(SMALL_COIL, 12)
                .input(ADVANCED_SMD_CAPACITOR, 4)
                .input(RANDOM_ACCESS_MEMORY, 4)
                .input(wireFine, Platinum, 16)
                .output(QUANTUM_ASSEMBLY_IV, 2)
                .solderMultiplier(2)
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
                .buildAndRegister();

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[IV]).duration(800)
                .input(frameGt, HSSG, 2)
                .input(QUANTUM_COMPUTER_LUV, 2)
                .input(SMALL_COIL, 24)
                .input(SMD_CAPACITOR, 48)
                .input(RANDOM_ACCESS_MEMORY, 24)
                .input(wireGtSingle, AnnealedCopper, 48)
                .solderMultiplier(4)
                .output(QUANTUM_MAINFRAME_ZPM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[IV]).duration(400)
                .input(frameGt, HSSG, 2)
                .input(QUANTUM_COMPUTER_LUV, 2)
                .input(SMALL_COIL, 24)
                .input(ADVANCED_SMD_CAPACITOR, 12)
                .input(RANDOM_ACCESS_MEMORY, 24)
                .input(wireGtSingle, AnnealedCopper, 48)
                .solderMultiplier(4)
                .output(QUANTUM_MAINFRAME_ZPM)
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
                .output(CRYSTAL_PROCESSOR_IV, 2)
                .buildAndRegister();

        // IV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(86000).duration(100)
                .input(ELITE_CIRCUIT_BOARD)
                .input(CRYSTAL_SYSTEM_ON_CHIP)
                .input(wireFine, NiobiumTitanium, 8)
                .input(bolt, YttriumBariumCuprate, 8)
                .output(CRYSTAL_PROCESSOR_IV, 4)
                .buildAndRegister();

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(9600).duration(400)
                .input(ELITE_CIRCUIT_BOARD)
                .input(CRYSTAL_PROCESSOR_IV, 2)
                .input(SMALL_COIL, 24)
                .input(ADVANCED_SMD_CAPACITOR, 8)
                .input(RANDOM_ACCESS_MEMORY, 24)
                .input(wireFine, NiobiumTitanium, 16)
                .output(CRYSTAL_ASSEMBLY_LUV, 2)
                .solderMultiplier(2)
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
                .buildAndRegister();

        // UV
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(VA[LuV]).duration(800)
                .input(frameGt, HSSE, 2)
                .input(CRYSTAL_COMPUTER_ZPM, 2)
                .input(RANDOM_ACCESS_MEMORY, 32)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(wireGtSingle, NiobiumTitanium, 8)
                .input(SMALL_COIL, 32)
                .input(ADVANCED_SMD_CAPACITOR, 16)
                .input(ADVANCED_SMD_DIODE, 8)
                .fluidInputs(SolderingAlloy.getFluid(L * 10))
                .researchItem(CRYSTAL_COMPUTER_ZPM.getStackForm())
                .output(CRYSTAL_MAINFRAME_UV)
                .buildAndRegister();

        // T7: Wetware =================================================================================================

        // Neuro Processing Unit
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(80000).duration(600)
                .input(WETWARE_CIRCUIT_BOARD)
                .input(STEM_CELLS, 16)
                .input(pipeSmallFluid, Polybenzimidazole, 8)
                .input(plate, Electrum, 8)
                .input(foil, SiliconeRubber, 16)
                .input(bolt, HSSE, 8)
                .fluidInputs(SterileGrowthMedium.getFluid(250))
                .output(NEURO_PROCESSOR)
                .buildAndRegister();

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(38400).duration(200)
                .input(NEURO_PROCESSOR)
                .input(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .input(NANO_CENTRAL_PROCESSING_UNIT)
                .input(ADVANCED_SMD_CAPACITOR, 8)
                .input(ADVANCED_SMD_TRANSISTOR, 8)
                .input(wireFine, YttriumBariumCuprate, 8)
                .output(WETWARE_PROCESSOR_LUV, 2)
                .buildAndRegister();

        // SoC LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(150000).duration(100)
                .input(NEURO_PROCESSOR)
                .input(HIGHLY_ADVANCED_SOC)
                .input(wireFine, YttriumBariumCuprate, 8)
                .input(bolt, Naquadah, 8)
                .output(WETWARE_PROCESSOR_LUV, 4)
                .buildAndRegister();

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(38400).duration(400)
                .input(WETWARE_CIRCUIT_BOARD)
                .input(WETWARE_PROCESSOR_LUV, 2)
                .input(SMALL_COIL, 32)
                .input(ADVANCED_SMD_CAPACITOR, 12)
                .input(RANDOM_ACCESS_MEMORY, 24)
                .input(wireFine, YttriumBariumCuprate, 16)
                .solderMultiplier(2)
                .output(WETWARE_PROCESSOR_ASSEMBLY_ZPM, 2)
                .buildAndRegister();

        // UV
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(38400).duration(400)
                .input(WETWARE_CIRCUIT_BOARD)
                .input(WETWARE_PROCESSOR_ASSEMBLY_ZPM, 2)
                .input(ADVANCED_SMD_DIODE, 8)
                .input(NOR_MEMORY_CHIP, 16)
                .input(RANDOM_ACCESS_MEMORY, 32)
                .input(wireFine, YttriumBariumCuprate, 24)
                .input(foil, SiliconeRubber, 32)
                .input(plate, Europium, 4)
                .fluidInputs(SolderingAlloy.getFluid(1152))
                .researchItem(WETWARE_PROCESSOR_ASSEMBLY_ZPM.getStackForm())
                .output(WETWARE_SUPER_COMPUTER_UV)
                .buildAndRegister();

        // UHV
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(300000).duration(2000)
                .input(frameGt, Tritanium, 2)
                .input(WETWARE_SUPER_COMPUTER_UV, 2)
                .input(ADVANCED_SMD_DIODE, 32)
                .input(ADVANCED_SMD_CAPACITOR, 32)
                .input(ADVANCED_SMD_TRANSISTOR, 32)
                .input(ADVANCED_SMD_RESISTOR, 32)
                .input(foil, SiliconeRubber, 64)
                .input(RANDOM_ACCESS_MEMORY, 32)
                .input(wireGtDouble, EnrichedNaquadahTriniumEuropiumDuranide, 16)
                .input(plate, Europium, 8)
                .fluidInputs(SolderingAlloy.getFluid(L * 20))
                .fluidInputs(Polybenzimidazole.getFluid(L * 8))
                .researchItem(WETWARE_SUPER_COMPUTER_UV.getStackForm())
                .output(WETWARE_MAINFRAME_UHV)
                .buildAndRegister();

        // Misc ========================================================================================================

        // Data Stick
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(90)
                .input(PLASTIC_BOARD)
                .input(CENTRAL_PROCESSING_UNIT, 2)
                .input(NAND_MEMORY_CHIP, 32)
                .input(RANDOM_ACCESS_MEMORY, 4)
                .input(wireFine, RedAlloy, 16)
                .input(plate, Polyethylene, 4)
                .output(TOOL_DATA_STICK)
                .solderMultiplier(2)
                .buildAndRegister();

        // Data Orb
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(1200)
                .input(EPOXY_BOARD)
                .input(circuit, Tier.Advanced, 2)
                .input(RANDOM_ACCESS_MEMORY, 4)
                .input(NOR_MEMORY_CHIP, 32)
                .input(NAND_MEMORY_CHIP, 64)
                .input(wireFine, Platinum, 32)
                .solderMultiplier(2)
                .output(TOOL_DATA_ORB)
                .buildAndRegister();

        // Lapotronic Energy Orb
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(512).EUt(1024)
                .input(FIBER_BOARD)
                .input(POWER_INTEGRATED_CIRCUIT, 4)
                .input(ENGRAVED_LAPOTRON_CHIP, 24)
                .input(NANO_CENTRAL_PROCESSING_UNIT, 2)
                .input(wireFine, Platinum, 16)
                .input(plate, Platinum, 8)
                .output(ENERGY_LAPOTRONIC_ORB)
                .solderMultiplier(2)
                .buildAndRegister();

        // Lapotronic Energy Cluster
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(80000).duration(1000)
                .input(MULTILAYER_FIBER_BOARD)
                .input(foil, Europium, 32)
                .input(circuit, Tier.Master, 4)
                .input(ENGRAVED_LAPOTRON_CHIP, 36)
                .input(ENGRAVED_LAPOTRON_CHIP, 36)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .input(SMD_DIODE, 32)
                .input(SMD_CAPACITOR, 32)
                .input(SMD_RESISTOR, 32)
                .input(SMD_TRANSISTOR, 32)
                .input(wireFine, Platinum, 64)
                .fluidInputs(SolderingAlloy.getFluid(L * 5))
                .researchItem(ENERGY_LAPOTRONIC_ORB.getStackForm())
                .output(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(80000).duration(500)
                .input(MULTILAYER_FIBER_BOARD)
                .input(foil, Europium, 32)
                .input(circuit, Tier.Master, 4)
                .input(ENGRAVED_LAPOTRON_CHIP, 36)
                .input(ENGRAVED_LAPOTRON_CHIP, 36)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .input(ADVANCED_SMD_DIODE, 8)
                .input(ADVANCED_SMD_CAPACITOR, 8)
                .input(ADVANCED_SMD_RESISTOR, 8)
                .input(ADVANCED_SMD_TRANSISTOR, 8)
                .input(wireFine, Platinum, 64)
                .fluidInputs(SolderingAlloy.getFluid(L * 5))
                .researchItem(ENERGY_LAPOTRONIC_ORB.getStackForm())
                .output(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .buildAndRegister();
    }
}
