package gregtech.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials.Color;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.type.IngotMaterial;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.ConfigHolder;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.L;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

public class CircuitRecipes {

    private CircuitRecipes() {
    }

    private static final MaterialStack[] solderingList = {
            new MaterialStack(Tin, 2L),
            new MaterialStack(SolderingAlloy, 1L)
    };

    public static void init() {
        waferRecipes();
        componentRecipes();
        boardRecipes();
        circuitRecipes();
    }

    private static void waferRecipes() {

        // Boules
        BLAST_RECIPES.recipeBuilder().duration(9000).EUt(120)
                .input(dust, Silicon, 32)
                .input(dustSmall, GalliumArsenide)
                .output(SILICON_BOULE)
                .blastFurnaceTemp(1784)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(12000).EUt(480)
                .input(dust, Silicon, 64)
                .input(dust, Glowstone, 8)
                .fluidInputs(Nitrogen.getFluid(8000))
                .output(GLOWSTONE_BOULE)
                .blastFurnaceTemp(2484)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(1500).EUt(1920)
                .input(block, Silicon, 9)
                .input(ingot, Naquadah)
                .fluidInputs(Argon.getFluid(8000))
                .output(NAQUADAH_BOULE)
                .blastFurnaceTemp(5400)
                .buildAndRegister();

        // Boule cutting
        CUTTER_RECIPES.recipeBuilder().duration(200).EUt(8)  .input(SILICON_BOULE).output(SILICON_WAFER, 16).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(400).EUt(64) .input(GLOWSTONE_BOULE).output(GLOWSTONE_WAFER, 32).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(800).EUt(384).input(NAQUADAH_BOULE).output(NAQUADAH_WAFER, 64).buildAndRegister();

        // Wafer engraving
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(120) .input(SILICON_WAFER)  .notConsumable(craftingLens, Color.Red)      .output(INTEGRATED_LOGIC_CIRCUIT_WAFER)         .buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(480) .input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Red)      .output(INTEGRATED_LOGIC_CIRCUIT_WAFER, 4).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(1920).input(NAQUADAH_WAFER) .notConsumable(craftingLens, Color.Red)      .output(INTEGRATED_LOGIC_CIRCUIT_WAFER, 8).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(120) .input(SILICON_WAFER)  .notConsumable(craftingLens, Color.Silver)   .output(RANDOM_ACCESS_MEMORY_WAFER)             .buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(480) .input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Silver)   .output(RANDOM_ACCESS_MEMORY_WAFER, 4)    .buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(1920).input(NAQUADAH_WAFER) .notConsumable(craftingLens, Color.Silver)   .output(RANDOM_ACCESS_MEMORY_WAFER, 8)    .buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(480) .input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.LightBlue).output(NAND_MEMORY_CHIP_WAFER)                 .buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(1920).input(NAQUADAH_WAFER) .notConsumable(craftingLens, Color.LightBlue).output(NAND_MEMORY_CHIP_WAFER, 4)        .buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(480) .input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Lime)     .output(NOR_MEMORY_CHIP_WAFER)                  .buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(1920).input(NAQUADAH_WAFER) .notConsumable(craftingLens, Color.Lime)     .output(NOR_MEMORY_CHIP_WAFER, 4)         .buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(120) .input(SILICON_WAFER)  .notConsumable(craftingLens, Color.White)    .output(CENTRAL_PROCESSING_UNIT_WAFER)          .buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(480) .input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.White)    .output(CENTRAL_PROCESSING_UNIT_WAFER, 4) .buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(1920).input(NAQUADAH_WAFER) .notConsumable(craftingLens, Color.White)    .output(CENTRAL_PROCESSING_UNIT_WAFER, 8) .buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(480) .input(GLOWSTONE_WAFER).notConsumable(craftingLens, Color.Blue)     .output(POWER_INTEGRATED_CIRCUIT_WAFER)         .buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(1920).input(NAQUADAH_WAFER) .notConsumable(craftingLens, Color.Blue)     .output(POWER_INTEGRATED_CIRCUIT_WAFER, 4).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(1920).input(NAQUADAH_WAFER) .notConsumable(craftingLens, Color.Yellow)   .output(SYSTEM_ON_CHIP_WAFER, 2)          .buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(1920).input(NAQUADAH_WAFER) .notConsumable(craftingLens, Color.Orange)   .output(ADVANCED_SYSTEM_ON_CHIP_WAFER)          .buildAndRegister();

        // Wafer chemical refining recipes
        CHEMICAL_RECIPES.recipeBuilder().duration(1200).EUt(1920)
                .input(POWER_INTEGRATED_CIRCUIT_WAFER)
                .input(dust, IndiumGalliumPhosphide, 2)
                .fluidInputs(RedAlloy.getFluid(288))
                .output(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(400).EUt(480)
                .input(CENTRAL_PROCESSING_UNIT_WAFER)
                .input(CARBON_FIBERS, 16)
                .fluidInputs(Glowstone.getFluid(576))
                .output(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(400).EUt(1920)
                .input(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .input(QUANTUM_EYE, 2)
                .fluidInputs(GalliumArsenide.getFluid(288))
                .output(QBIT_CENTRAL_PROCESSING_UNIT_WAFER)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(600).EUt(1920)
                .input(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .input(dust, IndiumGalliumPhosphide)
                .fluidInputs(Radon.getFluid(50))
                .output(QBIT_CENTRAL_PROCESSING_UNIT_WAFER)
                .buildAndRegister();

        // Wafer cutting
        CUTTER_RECIPES.recipeBuilder().duration(600).EUt(48).input(ADVANCED_SYSTEM_ON_CHIP_WAFER)      .output(ADVANCED_SYSTEM_ON_CHIP, 6)      .buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(600).EUt(48).input(SYSTEM_ON_CHIP_WAFER)               .output(SYSTEM_ON_CHIP, 6)               .buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(600).EUt(48).input(RANDOM_ACCESS_MEMORY_WAFER)         .output(RANDOM_ACCESS_MEMORY, 32)        .buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(600).EUt(48).input(QBIT_CENTRAL_PROCESSING_UNIT_WAFER) .output(QBIT_CENTRAL_PROCESSING_UNIT, 5) .buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(600).EUt(48).input(POWER_INTEGRATED_CIRCUIT_WAFER)     .output(POWER_INTEGRATED_CIRCUIT, 4)     .buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(600).EUt(48).input(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER).output(HIGH_POWER_INTEGRATED_CIRCUIT, 2).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(600).EUt(48).input(NOR_MEMORY_CHIP_WAFER)              .output(NOR_MEMORY_CHIP, 16)             .buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(600).EUt(48).input(NAND_MEMORY_CHIP_WAFER)             .output(NAND_MEMORY_CHIP, 32)            .buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(600).EUt(48).input(CENTRAL_PROCESSING_UNIT_WAFER)      .output(CENTRAL_PROCESSING_UNIT, 8)      .buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(600).EUt(48).input(INTEGRATED_LOGIC_CIRCUIT_WAFER)     .output(INTEGRATED_LOGIC_CIRCUIT, 8)     .buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(600).EUt(48).input(NANO_CENTRAL_PROCESSING_UNIT_WAFER) .output(NANO_CENTRAL_PROCESSING_UNIT, 7) .buildAndRegister();
    }

    private static void componentRecipes() {

        // Vacuum Tube
        ModHandler.addShapedRecipe("vacuum_tube_wire", VACUUM_TUBE.getStackForm(),
                "PTP", "WWW",
                'P', new ItemStack(Items.PAPER),
                'T', GLASS_TUBE.getStackForm(),
                'W', new UnificationEntry(wireGtSingle, Copper));

        ModHandler.addShapedRecipe("vacuum_tube_wire_fine", VACUUM_TUBE.getStackForm(),
                "PTP", "WWW",
                'P', new ItemStack(Items.PAPER),
                'T', GLASS_TUBE.getStackForm(),
                'W', new UnificationEntry(wireFine, Copper));

        ASSEMBLER_RECIPES.recipeBuilder().duration(120).EUt(8)
                .input(GLASS_TUBE)
                .inputs(new ItemStack(Items.PAPER, 2))
                .input(wireGtSingle, Copper, 2)
                .output(VACUUM_TUBE)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(120).EUt(8)
                .input(GLASS_TUBE)
                .inputs(new ItemStack(Items.PAPER, 2))
                .input(wireFine, Copper, 2)
                .output(VACUUM_TUBE)
                .buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(160).EUt(8)
                .input(dust, Glass)
                .notConsumable(SHAPE_MOLD_BALL)
                .output(GLASS_TUBE)
                .buildAndRegister();

        // Resistor
        ModHandler.addShapedRecipe("resistor_wire", RESISTOR.getStackForm(3),
                " P ", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'W', new UnificationEntry(wireGtSingle, Copper),
                'C', new UnificationEntry(dust, Coal));

        ModHandler.addShapedRecipe("resistor_wire_fine", RESISTOR.getStackForm(3),
                " P ", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'W', new UnificationEntry(wireFine, Copper),
                'C', new UnificationEntry(dust, Coal));

        ModHandler.addShapedRecipe("resistor_wire_charcoal", RESISTOR.getStackForm(3),
                " P ", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'W', new UnificationEntry(wireGtSingle, Copper),
                'C', new UnificationEntry(dust, Charcoal));

        ModHandler.addShapedRecipe("resistor_wire_fine_charcoal", RESISTOR.getStackForm(3),
                " P ", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'W', new UnificationEntry(wireFine, Copper),
                'C', new UnificationEntry(dust, Charcoal));

        ModHandler.addShapedRecipe("resistor_wire_carbon", RESISTOR.getStackForm(3),
                " P ", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'W', new UnificationEntry(wireGtSingle, Copper),
                'C', new UnificationEntry(dust, Carbon));

        ModHandler.addShapedRecipe("resistor_wire_fine_carbon", RESISTOR.getStackForm(3),
                " P ", "WCW", " P ",
                'P', new ItemStack(Items.PAPER),
                'W', new UnificationEntry(wireFine, Copper),
                'C', new UnificationEntry(dust, Carbon));

        ASSEMBLER_RECIPES.recipeBuilder().duration(160).EUt(6)
                .input(dust, Coal)
                .input(wireFine, Copper, 4)
                .output(RESISTOR, 12)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(160).EUt(6)
                .input(dust, Charcoal)
                .input(wireFine, Copper, 4)
                .output(RESISTOR, 12)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(160).EUt(6)
                .input(dust, Carbon)
                .input(wireFine, Copper, 4)
                .output(RESISTOR, 12)
                .buildAndRegister();

        // Capacitor
        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(96)
                .input(plate, Polyethylene)
                .input(foil, Aluminium, 2)
                .output(CAPACITOR, 2)
                .buildAndRegister();

        // Transistor
        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(24)
                .input(plate, Silicon)
                .input(wireFine, Tin, 6)
                .fluidInputs(Polyethylene.getFluid(144))
                .output(TRANSISTOR, 8)
                .buildAndRegister();

        // Diode
        ModHandler.addShapedRecipe("diode", DIODE.getStackForm(),
                "DG ", "TWT", "DG ",
                'D', "dyeBlack",
                'G', "paneGlass",
                'T', new UnificationEntry(wireGtSingle, Tin),
                'W', new UnificationEntry(dustSmall, GalliumArsenide));

        ModHandler.addShapedRecipe("diode_wire", DIODE.getStackForm(),
                "DG ", "TWT", "DG ",
                'D', "dyeBlack",
                'G', "paneGlass",
                'T', new UnificationEntry(wireFine, Tin),
                'W', new UnificationEntry(dustSmall, GalliumArsenide));

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(30)
                .input(wireFine, Copper, 4)
                .input(dustSmall, GalliumArsenide)
                .fluidInputs(Polyethylene.getFluid(144))
                .output(DIODE, 2)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(30)
                .input(wireFine, Copper, 4)
                .input(SILICON_WAFER)
                .fluidInputs(Polyethylene.getFluid(144))
                .output(DIODE, 4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(30)
                .input(wireFine, AnnealedCopper, 4)
                .input(dustSmall, GalliumArsenide)
                .fluidInputs(Polyethylene.getFluid(144))
                .output(DIODE, 6)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(30)
                .input(wireFine, AnnealedCopper, 4)
                .input(SILICON_WAFER)
                .fluidInputs(Polyethylene.getFluid(144))
                .output(DIODE, 8)
                .buildAndRegister();

        // Small Coil
        ModHandler.addShapedRecipe("small_coil_copper_steel", SMALL_COIL.getStackForm(),
                "WWW", "WBW", "WWW",
                'W', new UnificationEntry(wireFine, Copper),
                'B', new UnificationEntry(ring, Steel));

        ASSEMBLER_RECIPES.recipeBuilder().duration(320).EUt(60)
                .input(ring, Steel)
                .input(wireFine, Copper, 2)
                .output(SMALL_COIL, 2)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(320).EUt(60)
                .input(ring, NickelZincFerrite)
                .input(wireFine, Copper, 2)
                .output(SMALL_COIL, 4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(320).EUt(60)
                .input(ring, Steel)
                .input(wireFine, AnnealedCopper, 2)
                .output(SMALL_COIL, 4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(320).EUt(60)
                .input(ring, NickelZincFerrite)
                .input(wireFine, AnnealedCopper, 2)
                .output(SMALL_COIL, 8)
                .buildAndRegister();

        // SMD Resistor
        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(120)
                .input(dust, Carbon)
                .input(wireFine, Electrum, 4)
                .fluidInputs(Polyethylene.getFluid(144))
                .output(SMD_RESISTOR, 24)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(480)
                .input(dust, Carbon)
                .input(wireFine, Electrum, 4)
                .fluidInputs(Polybenzimidazole.getFluid(72))
                .output(SMD_RESISTOR, 48)
                .buildAndRegister();

        // SMD Diode
        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(120)
                .input(dust, GalliumArsenide)
                .input(wireFine, Platinum, 8)
                .fluidInputs(Polyethylene.getFluid(144))
                .output(SMD_DIODE, 32)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(480)
                .input(dust, GalliumArsenide)
                .input(wireFine, Platinum, 8)
                .fluidInputs(Polybenzimidazole.getFluid(72))
                .output(SMD_DIODE, 64)
                .buildAndRegister();

        // SMD Transistor
        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(120)
                .input(foil, Gallium)
                .input(wireFine, AnnealedCopper, 8)
                .fluidInputs(Polyethylene.getFluid(288))
                .output(SMD_TRANSISTOR, 32)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(480)
                .input(foil, Gallium)
                .input(wireFine, AnnealedCopper, 8)
                .fluidInputs(Polybenzimidazole.getFluid(144))
                .output(SMD_TRANSISTOR, 64)
                .buildAndRegister();

        // SMD Capacitor
        ASSEMBLER_RECIPES.recipeBuilder().duration(60).EUt(120)
                .input(foil, SiliconeRubber)
                .input(foil, Aluminium)
                .fluidInputs(Polyethylene.getFluid(72))
                .output(SMD_CAPACITOR, 8)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(60).EUt(120)
                .input(foil, PolyvinylChloride, 2)
                .input(foil, Aluminium)
                .fluidInputs(Polyethylene.getFluid(72))
                .output(SMD_CAPACITOR, 12)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(60).EUt(120)
                .input(foil, SiliconeRubber)
                .input(foil, Tantalum)
                .fluidInputs(Polyethylene.getFluid(72))
                .output(SMD_CAPACITOR, 16)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(60).EUt(120)
                .input(foil, PolyvinylChloride, 2)
                .input(foil, Tantalum)
                .fluidInputs(Polyethylene.getFluid(72))
                .output(SMD_CAPACITOR, 24)
                .buildAndRegister();
        // TODO PBI SMD_CAPACITOR RECIPES

        // Carbon Fibers
        if (ConfigHolder.U.GT5u.polymerCarbonFiber) {
            AUTOCLAVE_RECIPES.recipeBuilder().duration(37).EUt(30)
                    .input(dust, Carbon, 4)
                    .fluidInputs(Polyethylene.getFluid(36))
                    .output(CARBON_FIBERS)
                    .buildAndRegister();

            AUTOCLAVE_RECIPES.recipeBuilder().duration(37).EUt(120)
                    .input(dust, Carbon, 4)
                    .fluidInputs(Polytetrafluoroethylene.getFluid(18))
                    .output(CARBON_FIBERS, 2)
                    .buildAndRegister();

            AUTOCLAVE_RECIPES.recipeBuilder().duration(37).EUt(480)
                    .input(dust, Carbon, 4)
                    .fluidInputs(Epoxy.getFluid(9))
                    .output(CARBON_FIBERS, 4)
                    .buildAndRegister();

            AUTOCLAVE_RECIPES.recipeBuilder().duration(37).EUt(1920)
                    .input(dust, Carbon, 8)
                    .fluidInputs(Polybenzimidazole.getFluid(9))
                    .output(CARBON_FIBERS, 16)
                    .buildAndRegister();
        } else {
            AUTOCLAVE_RECIPES.recipeBuilder().duration(37).EUt(30)
                    .input(dust, Carbon, 4)
                    .fluidInputs(Palladium.getFluid(1))
                    .chancedOutput(CARBON_FIBERS, 2, 9000, 1000)
                    .buildAndRegister();

            AUTOCLAVE_RECIPES.recipeBuilder().duration(37).EUt(30)
                    .input(dust, Carbon, 4)
                    .fluidInputs(Platinum.getFluid(1))
                    .chancedOutput(CARBON_FIBERS, 2, 5000, 5000)
                    .buildAndRegister();

            AUTOCLAVE_RECIPES.recipeBuilder().duration(37).EUt(30)
                    .input(dust, Carbon, 4)
                    .fluidInputs(Lutetium.getFluid(1))
                    .chancedOutput(CARBON_FIBERS, 2, 3333, 3334)
                    .buildAndRegister();
        }

        // Crystal Circuit Components
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(256).EUt(480)
                .input(LAPOTRON_CRYSTAL)
                .notConsumable(craftingLens, Color.Blue)
                .output(ENGRAVED_LAPOTRON_CHIP, 3)
                .buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(100).EUt(7600)
                .input(ENGRAVED_CRYSTAL_CHIP)
                .notConsumable(craftingLens, Color.Lime)
                .output(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(100).EUt(32000)
                .input(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .notConsumable(craftingLens, Color.Blue)
                .output(CRYSTAL_SYSTEM_ON_CHIP)
                .buildAndRegister();

        CUTTER_RECIPES.recipeBuilder().EUt(60).duration(960)
                .input(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .output(RAW_CRYSTAL_CHIP, 2)
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

        BLAST_RECIPES.recipeBuilder().duration(900).EUt(480).blastFurnaceTemp(5000)
                .input(plate, Emerald)
                .input(RAW_CRYSTAL_CHIP)
                .fluidInputs(Helium.getFluid(1000))
                .output(ENGRAVED_CRYSTAL_CHIP)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(900).EUt(480).blastFurnaceTemp(5000)
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

        AUTOCLAVE_RECIPES.recipeBuilder().duration(480).EUt(7680)
                .input(gem, NetherStar)
                .fluidInputs(Neutronium.getFluid(L * 2))
                .output(GRAVI_STAR)
                .buildAndRegister();

        // Neutron Reflector TODO Move out of here
        ASSEMBLER_RECIPES.recipeBuilder().duration(4000).EUt(120)
                .input(PLATE_IRIDIUM_ALLOY)
                .input(plate, Beryllium, 32)
                .input(plate, TungstenCarbide, 4)
                .fluidInputs(TinAlloy.getFluid(L * 32))
                .output(NEUTRON_REFLECTOR)
                .buildAndRegister();

    }

    private static void boardRecipes() {

        // Coated board
        ModHandler.addShapedRecipe("coated_board", COATED_BOARD.getStackForm(3),
                " R ", "PPP", " R ",
                'R', RUBBER_DROP.getStackForm(),
                'P', new UnificationEntry(plate, Wood));

        ModHandler.addShapelessRecipe("coated_board_1x", COATED_BOARD.getStackForm(),
                RUBBER_DROP.getStackForm(),
                new UnificationEntry(plate, Wood),
                new UnificationEntry(plate, Wood));

        ASSEMBLER_RECIPES.recipeBuilder().duration(160).EUt(8)
                .input(plate, Wood, 8)
                .input(RUBBER_DROP)
                .fluidInputs(Glue.getFluid(100))
                .output(COATED_BOARD, 8)
                .buildAndRegister();

        // Phenolic Board
        ASSEMBLER_RECIPES.recipeBuilder().duration(30).EUt(8)
                .input(dust, Wood)
                .notConsumable(SHAPE_MOLD_PLATE)
                .fluidInputs(Glue.getFluid(36))
                .output(PHENOLIC_BOARD)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(30).EUt(8)
                .input(dust, Wood)
                .notConsumable(SHAPE_MOLD_PLATE)
                .fluidInputs(BisphenolA.getFluid(18))
                .output(PHENOLIC_BOARD, 2)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(30).EUt(8)
                .input(dust, Wood)
                .notConsumable(SHAPE_MOLD_PLATE)
                .fluidInputs(Epoxy.getFluid(18))
                .output(PHENOLIC_BOARD, 3)
                .buildAndRegister();

        // Plastic Board
        CHEMICAL_RECIPES.recipeBuilder().duration(600).EUt(10)
                .input(plate, Polyethylene)
                .input(foil, Copper, 4)
                .fluidInputs(SulfuricAcid.getFluid(125))
                .output(PLASTIC_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(600).EUt(10)
                .input(plate, PolyvinylChloride)
                .input(foil, Copper, 4)
                .fluidInputs(SulfuricAcid.getFluid(125))
                .output(PLASTIC_BOARD, 2)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(600).EUt(10)
                .input(plate, Polytetrafluoroethylene)
                .input(foil, Copper, 4)
                .fluidInputs(SulfuricAcid.getFluid(125))
                .output(PLASTIC_BOARD, 4)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(600).EUt(10)
                .input(plate, Polybenzimidazole)
                .input(foil, Copper, 4)
                .fluidInputs(SulfuricAcid.getFluid(125))
                .output(PLASTIC_BOARD, 8)
                .buildAndRegister();

        // Epoxy Board
        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(10)
                .input(plate, Epoxy)
                .input(foil, Copper)
                .fluidInputs(SulfuricAcid.getFluid(125))
                .output(EPOXY_BOARD)
                .buildAndRegister();

        // Fiber Reinforced Epoxy Board
        CHEMICAL_BATH_RECIPES.recipeBuilder().duration(240).EUt(16)
                .input(GLASS_FIBER)
                .fluidInputs(Epoxy.getFluid(144))
                .output(plate, ReinforcedEpoxyResin)
                .buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder().duration(240).EUt(16)
                .input(CARBON_FIBERS)
                .fluidInputs(Epoxy.getFluid(144))
                .output(plate, ReinforcedEpoxyResin)
                .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder().duration(160).EUt(96)
                .input(ingot, BorosilicateGlass)
                .notConsumable(SHAPE_EXTRUDER_WIRE)
                .output(GLASS_FIBER, 8)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(10)
                .input(plate, ReinforcedEpoxyResin)
                .input(foil, Copper)
                .fluidInputs(SulfuricAcid.getFluid(125))
                .output(FIBER_BOARD)
                .buildAndRegister();

        // Multi-Layer Fiber Reinforced Epoxy Board
        CHEMICAL_RECIPES.recipeBuilder().duration(100).EUt(480)
                .input(FIBER_BOARD)
                .input(foil, Electrum, 16)
                .fluidInputs(SulfuricAcid.getFluid(250))
                .output(MULTILAYER_FIBER_BOARD)
                .buildAndRegister();

        // Wetware Board
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(160).EUt(16)
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .fluidInputs(Polystyrene.getFluid(L / 4))
                .output(PETRI_DISH)
                .buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(160).EUt(16)
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L / 4))
                .output(PETRI_DISH)
                .buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(40).EUt(480)
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .fluidInputs(Polybenzimidazole.getFluid(L / 8))
                .output(PETRI_DISH, 2)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(480)
                .input(MULTILAYER_FIBER_BOARD)
                .input(circuit, Tier.Good)
                .input(ELECTRIC_PUMP_LV)
                .input(SENSOR_LV)
                .input(PETRI_DISH)
                .fluidInputs(SterileGrowthMedium.getFluid(250))
                .output(WETWARE_BOARD)
                .buildAndRegister();
    }

    private static void circuitRecipes() {

        // Handcrafted Circuits
        ModHandler.addShapedRecipe("basic_circuit", BASIC_CIRCUIT_LV.getStackForm(),
                "RPR", "VBV", "CCC",
                'R', RESISTOR.getStackForm(),
                'P', new UnificationEntry(plate, Steel),
                'V', VACUUM_TUBE.getStackForm(),
                'B', COATED_BOARD.getStackForm(),
                'C', new UnificationEntry(cableGtSingle, RedAlloy));

        ModHandler.addShapedRecipe("good_circuit", GOOD_INTEGRATED_CIRCUIT_MV.getStackForm(),
                "RCP", "CDC", "PCR",
                'R', new UnificationEntry(cableGtSingle, RedAlloy),
                'P', new UnificationEntry(plate, Steel),
                'C', BASIC_CIRCUIT_LV.getStackForm(),
                'D', DIODE.getStackForm());

        //circuit assembling recipes TODO
        for (MaterialStack stack : solderingList) {
            IngotMaterial material = (IngotMaterial) stack.material;
            int multiplier = (int) stack.amount;

            // LV Circuits
            ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(8)
                    .input(PHENOLIC_BOARD)
                    .input(INTEGRATED_LOGIC_CIRCUIT)
                    .input(RESISTOR, 2)
                    .input(wireFine, Copper)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(BASIC_ELECTRONIC_CIRCUIT_LV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(8)
                    .input(PHENOLIC_BOARD)
                    .input(INTEGRATED_LOGIC_CIRCUIT)
                    .input(SMD_RESISTOR, 2)
                    .input(wireFine, Copper)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(BASIC_ELECTRONIC_CIRCUIT_LV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(60)
                    .input(PLASTIC_BOARD)
                    .input(CENTRAL_PROCESSING_UNIT, 4)
                    .input(RESISTOR, 4)
                    .input(CAPACITOR, 4)
                    .input(TRANSISTOR, 4)
                    .input(wireFine, Copper, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(ADVANCED_CIRCUIT_PARTS_LV, 4)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(60)
                    .input(PLASTIC_BOARD)
                    .input(CENTRAL_PROCESSING_UNIT, 4)
                    .input(SMD_RESISTOR, 4)
                    .input(SMD_CAPACITOR, 4)
                    .input(SMD_TRANSISTOR, 4)
                    .input(wireFine, Copper, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(ADVANCED_CIRCUIT_PARTS_LV, 4)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(600)
                    .input(PLASTIC_BOARD)
                    .input(SYSTEM_ON_CHIP, 4)
                    .input(wireFine, Copper, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(ADVANCED_CIRCUIT_PARTS_LV, 4)
                    .buildAndRegister();

            // MV Circuits
            ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(16)
                    .input(PHENOLIC_BOARD)
                    .input(BASIC_ELECTRONIC_CIRCUIT_LV, 3)
                    .input(RESISTOR, 4)
                    .input(wireFine, Gold, 8)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(GOOD_INTEGRATED_CIRCUIT_MV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(16)
                    .input(PHENOLIC_BOARD)
                    .input(BASIC_ELECTRONIC_CIRCUIT_LV, 3)
                    .input(SMD_RESISTOR, 4)
                    .input(wireFine, Gold, 8)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(GOOD_INTEGRATED_CIRCUIT_MV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(60)
                    .input(PLASTIC_BOARD)
                    .input(CENTRAL_PROCESSING_UNIT)
                    .input(RESISTOR, 2)
                    .input(CAPACITOR, 2)
                    .input(TRANSISTOR, 2)
                    .input(wireFine, RedAlloy, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(ADVANCED_CIRCUIT_MV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(60)
                    .input(PLASTIC_BOARD)
                    .input(CENTRAL_PROCESSING_UNIT)
                    .input(SMD_RESISTOR, 2)
                    .input(SMD_CAPACITOR, 2)
                    .input(SMD_TRANSISTOR, 2)
                    .input(wireFine, RedAlloy, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(ADVANCED_CIRCUIT_MV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(2400)
                    .input(PLASTIC_BOARD)
                    .input(SYSTEM_ON_CHIP)
                    .input(wireFine, RedAlloy, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(ADVANCED_CIRCUIT_MV)
                    .buildAndRegister();

            // HV Circuits
            ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(90)
                    .input(PLASTIC_BOARD)
                    .input(ADVANCED_CIRCUIT_MV, 2)
                    .input(SMALL_COIL, 4)
                    .input(CAPACITOR, 4)
                    .input(RANDOM_ACCESS_MEMORY, 4)
                    .input(wireFine, RedAlloy, 12)
                    .fluidInputs(material.getFluid(L * multiplier))
                    .output(PROCESSOR_ASSEMBLY_HV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(90)
                    .input(PLASTIC_BOARD)
                    .input(ADVANCED_CIRCUIT_MV, 2)
                    .input(SMALL_COIL, 4)
                    .input(SMD_CAPACITOR, 4)
                    .input(RANDOM_ACCESS_MEMORY, 4)
                    .input(wireFine, RedAlloy, 12)
                    .fluidInputs(material.getFluid(L * multiplier))
                    .output(PROCESSOR_ASSEMBLY_HV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(480)
                    .input(EPOXY_BOARD)
                    .input(NANO_CENTRAL_PROCESSING_UNIT)
                    .input(SMD_RESISTOR, 2)
                    .input(SMD_CAPACITOR, 2)
                    .input(SMD_TRANSISTOR, 2)
                    .input(wireFine, Electrum, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(NANO_PROCESSOR_HV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(9600)
                    .input(EPOXY_BOARD)
                    .input(SYSTEM_ON_CHIP)
                    .input(wireFine, Electrum, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(NANO_PROCESSOR_HV)
                    .buildAndRegister();

            // EV Circuits
            ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(480)
                    .input(EPOXY_BOARD)
                    .input(NANO_PROCESSOR_HV, 2)
                    .input(SMALL_COIL, 4)
                    .input(SMD_CAPACITOR, 4)
                    .input(RANDOM_ACCESS_MEMORY, 4)
                    .input(wireFine, Electrum, 6)
                    .fluidInputs(material.getFluid(L * multiplier))
                    .output(NANO_PROCESSOR_ASSEMBLY_EV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(1960)
                    .input(FIBER_BOARD)
                    .input(QBIT_CENTRAL_PROCESSING_UNIT)
                    .input(NANO_CENTRAL_PROCESSING_UNIT)
                    .input(SMD_CAPACITOR, 2)
                    .input(SMD_TRANSISTOR, 2)
                    .input(wireFine, Platinum, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(QUANTUM_PROCESSOR_EV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(32000)
                    .input(FIBER_BOARD)
                    .input(ADVANCED_SYSTEM_ON_CHIP)
                    .input(wireFine, Platinum, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(QUANTUM_PROCESSOR_EV)
                    .buildAndRegister();

            // IV Circuits
            ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(1960)
                    .input(FIBER_BOARD)
                    .input(QUANTUM_PROCESSOR_EV, 2)
                    .input(SMALL_COIL, 4)
                    .input(SMD_CAPACITOR, 4)
                    .input(RANDOM_ACCESS_MEMORY, 4)
                    .input(wireFine, Platinum, 6)
                    .fluidInputs(material.getFluid(L * multiplier))
                    .output(DATA_CONTROL_CIRCUIT_IV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(7600)
                    .input(MULTILAYER_FIBER_BOARD)
                    .input(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                    .input(NANO_CENTRAL_PROCESSING_UNIT)
                    .input(SMD_CAPACITOR, 2)
                    .input(SMD_TRANSISTOR, 2)
                    .input(wireFine, NiobiumTitanium, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(CRYSTAL_PROCESSOR_IV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(31900)
                    .input(MULTILAYER_FIBER_BOARD)
                    .input(CRYSTAL_SYSTEM_ON_CHIP)
                    .input(wireFine, NiobiumTitanium, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(CRYSTAL_PROCESSOR_IV)
                    .buildAndRegister();

            // LuV Circuits
            ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(7600)
                    .input(MULTILAYER_FIBER_BOARD)
                    .input(CRYSTAL_PROCESSOR_IV, 2)
                    .input(SMALL_COIL, 4)
                    .input(SMD_CAPACITOR, 4)
                    .input(RANDOM_ACCESS_MEMORY, 4)
                    .input(wireFine, NiobiumTitanium, 6)
                    .fluidInputs(material.getFluid(L * multiplier))
                    .output(ENERGY_FLOW_CIRCUIT_LUV)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(32800)
                    .input(WETWARE_BOARD)
                    .input(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                    .input(NANO_CENTRAL_PROCESSING_UNIT)
                    .input(SMD_CAPACITOR, 2)
                    .input(SMD_TRANSISTOR, 2)
                    .input(wireFine, YttriumBariumCuprate, 2)
                    .fluidInputs(material.getFluid(L / 2 * multiplier))
                    .output(WETWARE_PROCESSOR_LUV)
                    .buildAndRegister();

            // ZPM Circuits
            ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(34400)
                    .input(WETWARE_BOARD)
                    .input(WETWARE_PROCESSOR_LUV, 2)
                    .input(SMALL_COIL, 4)
                    .input(SMD_CAPACITOR, 4)
                    .input(RANDOM_ACCESS_MEMORY, 4)
                    .input(wireFine, YttriumBariumCuprate, 6)
                    .fluidInputs(material.getFluid(L * multiplier))
                    .output(WETWARE_PROCESSOR_ASSEMBLY_ZPM)
                    .buildAndRegister();

            // UV Circuits
            ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(34400)
                    .input(WETWARE_BOARD, 2)
                    .input(WETWARE_PROCESSOR_ASSEMBLY_ZPM, 3)
                    .input(SMD_DIODE, 4)
                    .input(NOR_MEMORY_CHIP, 4)
                    .input(RANDOM_ACCESS_MEMORY, 4)
                    .input(wireFine, YttriumBariumCuprate, 6)
                    .fluidInputs(material.getFluid(L * multiplier))
                    .output(WETWARE_SUPER_COMPUTER_UV)
                    .buildAndRegister();

            // Misc
            ASSEMBLER_RECIPES.recipeBuilder().duration(512).EUt(1024)
                    .input(FIBER_BOARD)
                    .input(POWER_INTEGRATED_CIRCUIT, 4)
                    .input(ENGRAVED_LAPOTRON_CHIP, 18)
                    .input(NANO_CENTRAL_PROCESSING_UNIT)
                    .input(wireFine, Platinum, 16)
                    .fluidInputs(material.getFluid(L * multiplier))
                    .output(ENERGY_LAPOTRONIC_ORB)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(1024).EUt(4096)
                    .input(FIBER_BOARD)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 4)
                    .input(ENERGY_LAPOTRONIC_ORB, 8)
                    .input(QBIT_CENTRAL_PROCESSING_UNIT)
                    .input(wireFine, Platinum, 16)
                    .input(plate, Europium, 4)
                    .fluidInputs(material.getFluid(L * multiplier))
                    .output(ENERGY_LAPOTRONIC_ORB2)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(90)
                    .input(PLASTIC_BOARD)
                    .input(ADVANCED_CIRCUIT_MV)
                    .input(NAND_MEMORY_CHIP, 32)
                    .input(RANDOM_ACCESS_MEMORY, 4)
                    .input(wireFine, RedAlloy, 8)
                    .input(plate, Polyethylene, 4)
                    .fluidInputs(material.getFluid(L * multiplier))
                    .output(TOOL_DATA_STICK)
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder().duration(400).EUt(1200)
                    .input(EPOXY_BOARD)
                    .input(NANO_PROCESSOR_HV)
                    .input(RANDOM_ACCESS_MEMORY, 4)
                    .input(NOR_MEMORY_CHIP, 32)
                    .input(NAND_MEMORY_CHIP, 64)
                    .input(wireFine, Platinum, 32)
                    .fluidInputs(material.getFluid(L * multiplier))
                    .output(TOOL_DATA_ORB)
                    .buildAndRegister();
        }

        // UHV Circuits
        ASSEMBLY_LINE_RECIPES.recipeBuilder().duration(2000).EUt(300000)
                .input(frameGt, Tritanium, 4)
                .input(WETWARE_SUPER_COMPUTER_UV, 8)
                .input(SMALL_COIL, 4)
                .input(SMD_CAPACITOR, 32)
                .input(SMD_RESISTOR, 32)
                .input(SMD_TRANSISTOR, 32)
                .input(SMD_DIODE, 32)
                .input(RANDOM_ACCESS_MEMORY, 16)
                .input(wireGtDouble, Tier.Superconductor, 16)
                .input(foil, SiliconeRubber, 64)
                .fluidInputs(SolderingAlloy.getFluid(L * 20))
                .fluidInputs(Polybenzimidazole.getFluid(1000))
                .output(WETWARE_MAINFRAME_MAX)
                .buildAndRegister();
    }
}
