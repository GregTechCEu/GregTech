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
        waferRecipes();
        componentRecipes();
        boardRecipes();
        circuitRecipes();
    }

    private static void waferRecipes() {
        // Boules
        BLAST_RECIPES.recipeBuilder()
                .inputItem(dust, Silicon, 32)
                .inputItem(dustSmall, GalliumArsenide)
                .circuitMeta(2)
                .outputItem(SILICON_BOULE)
                .blastFurnaceTemp(1784)
                .duration(9000).EUt(VA[MV]).buildAndRegister();

        BLAST_RECIPES.recipeBuilder()
                .inputItem(dust, Silicon, 64)
                .inputItem(dust, Phosphorus, 8)
                .inputItem(dustSmall, GalliumArsenide, 2)
                .fluidInputs(Nitrogen.getFluid(8000))
                .outputItem(PHOSPHORUS_BOULE)
                .blastFurnaceTemp(2484)
                .duration(12000).EUt(VA[HV]).buildAndRegister();

        BLAST_RECIPES.recipeBuilder()
                .inputItem(block, Silicon, 16)
                .inputItem(ingot, Naquadah)
                .inputItem(dust, GalliumArsenide)
                .fluidInputs(Argon.getFluid(8000))
                .outputItem(NAQUADAH_BOULE)
                .blastFurnaceTemp(5400)
                .duration(15000).EUt(VA[EV]).buildAndRegister();

        BLAST_RECIPES.recipeBuilder()
                .inputItem(block, Silicon, 32)
                .inputItem(ingot, Neutronium, 4)
                .inputItem(dust, GalliumArsenide, 2)
                .fluidInputs(Xenon.getFluid(8000))
                .outputItem(NEUTRONIUM_BOULE)
                .blastFurnaceTemp(6484)
                .duration(18000).EUt(VA[IV]).buildAndRegister();

        // Boule cutting
        CUTTER_RECIPES.recipeBuilder()
                .inputItem(SILICON_BOULE)
                .outputItem(SILICON_WAFER, 16)
                .duration(400).EUt(64).buildAndRegister();

        CUTTER_RECIPES.recipeBuilder()
                .inputItem(PHOSPHORUS_BOULE)
                .outputItem(PHOSPHORUS_WAFER, 32)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(800).EUt(VA[HV]).buildAndRegister();

        CUTTER_RECIPES.recipeBuilder()
                .inputItem(NAQUADAH_BOULE)
                .outputItem(NAQUADAH_WAFER, 64)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1600).EUt(VA[EV]).buildAndRegister();

        CUTTER_RECIPES.recipeBuilder()
                .inputItem(NEUTRONIUM_BOULE)
                .outputItem(NEUTRONIUM_WAFER, 64)
                .outputItem(NEUTRONIUM_WAFER, 32)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(2400).EUt(VA[IV]).buildAndRegister();

        // Wafer engraving
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).inputItem(SILICON_WAFER)
                .notConsumable(craftingLens, Color.Red).outputItem(INTEGRATED_LOGIC_CIRCUIT_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[HV]).inputItem(PHOSPHORUS_WAFER)
                .notConsumable(craftingLens, Color.Red).outputItem(INTEGRATED_LOGIC_CIRCUIT_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).inputItem(NAQUADAH_WAFER)
                .notConsumable(craftingLens, Color.Red).outputItem(INTEGRATED_LOGIC_CIRCUIT_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(50).EUt(VA[IV]).inputItem(NEUTRONIUM_WAFER)
                .notConsumable(craftingLens, Color.Red).outputItem(INTEGRATED_LOGIC_CIRCUIT_WAFER, 16)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).inputItem(SILICON_WAFER)
                .notConsumable(craftingLens, Color.Green).outputItem(RANDOM_ACCESS_MEMORY_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[HV]).inputItem(PHOSPHORUS_WAFER)
                .notConsumable(craftingLens, Color.Green).outputItem(RANDOM_ACCESS_MEMORY_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).inputItem(NAQUADAH_WAFER)
                .notConsumable(craftingLens, Color.Green).outputItem(RANDOM_ACCESS_MEMORY_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(50).EUt(VA[IV]).inputItem(NEUTRONIUM_WAFER)
                .notConsumable(craftingLens, Color.Green).outputItem(RANDOM_ACCESS_MEMORY_WAFER, 16)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).inputItem(SILICON_WAFER)
                .notConsumable(craftingLens, Color.LightBlue).outputItem(CENTRAL_PROCESSING_UNIT_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[HV]).inputItem(PHOSPHORUS_WAFER)
                .notConsumable(craftingLens, Color.LightBlue).outputItem(CENTRAL_PROCESSING_UNIT_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).inputItem(NAQUADAH_WAFER)
                .notConsumable(craftingLens, Color.LightBlue).outputItem(CENTRAL_PROCESSING_UNIT_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(50).EUt(VA[IV]).inputItem(NEUTRONIUM_WAFER)
                .notConsumable(craftingLens, Color.LightBlue).outputItem(CENTRAL_PROCESSING_UNIT_WAFER, 16)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).inputItem(SILICON_WAFER)
                .notConsumable(craftingLens, Color.Blue).outputItem(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER)
                .buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[HV]).inputItem(PHOSPHORUS_WAFER)
                .notConsumable(craftingLens, Color.Blue).outputItem(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).inputItem(NAQUADAH_WAFER)
                .notConsumable(craftingLens, Color.Blue).outputItem(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(50).EUt(VA[IV]).inputItem(NEUTRONIUM_WAFER)
                .notConsumable(craftingLens, Color.Blue).outputItem(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 16)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).inputItem(SILICON_WAFER)
                .notConsumable(craftingLens, Color.Orange).outputItem(LOW_POWER_INTEGRATED_CIRCUIT_WAFER)
                .buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[HV]).inputItem(PHOSPHORUS_WAFER)
                .notConsumable(craftingLens, Color.Orange).outputItem(LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).inputItem(NAQUADAH_WAFER)
                .notConsumable(craftingLens, Color.Orange).outputItem(LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(50).EUt(VA[IV]).inputItem(NEUTRONIUM_WAFER)
                .notConsumable(craftingLens, Color.Orange).outputItem(LOW_POWER_INTEGRATED_CIRCUIT_WAFER, 16)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).inputItem(SILICON_WAFER)
                .notConsumable(craftingLens, Color.Cyan).outputItem(SIMPLE_SYSTEM_ON_CHIP_WAFER).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[HV]).inputItem(PHOSPHORUS_WAFER)
                .notConsumable(craftingLens, Color.Cyan).outputItem(SIMPLE_SYSTEM_ON_CHIP_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV]).inputItem(NAQUADAH_WAFER)
                .notConsumable(craftingLens, Color.Cyan).outputItem(SIMPLE_SYSTEM_ON_CHIP_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(50).EUt(VA[IV]).inputItem(NEUTRONIUM_WAFER)
                .notConsumable(craftingLens, Color.Cyan).outputItem(SIMPLE_SYSTEM_ON_CHIP_WAFER, 16)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).inputItem(PHOSPHORUS_WAFER)
                .notConsumable(craftingLens, Color.Gray).outputItem(NAND_MEMORY_CHIP_WAFER)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[EV]).inputItem(NAQUADAH_WAFER)
                .notConsumable(craftingLens, Color.Gray).outputItem(NAND_MEMORY_CHIP_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[IV]).inputItem(NEUTRONIUM_WAFER)
                .notConsumable(craftingLens, Color.Gray).outputItem(NAND_MEMORY_CHIP_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).inputItem(PHOSPHORUS_WAFER)
                .notConsumable(craftingLens, Color.Pink).outputItem(NOR_MEMORY_CHIP_WAFER)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[EV]).inputItem(NAQUADAH_WAFER)
                .notConsumable(craftingLens, Color.Pink).outputItem(NOR_MEMORY_CHIP_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[IV]).inputItem(NEUTRONIUM_WAFER)
                .notConsumable(craftingLens, Color.Pink).outputItem(NOR_MEMORY_CHIP_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).inputItem(PHOSPHORUS_WAFER)
                .notConsumable(craftingLens, Color.Brown).outputItem(POWER_INTEGRATED_CIRCUIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[EV]).inputItem(NAQUADAH_WAFER)
                .notConsumable(craftingLens, Color.Brown).outputItem(POWER_INTEGRATED_CIRCUIT_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[IV]).inputItem(NEUTRONIUM_WAFER)
                .notConsumable(craftingLens, Color.Brown).outputItem(POWER_INTEGRATED_CIRCUIT_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).inputItem(PHOSPHORUS_WAFER)
                .notConsumable(craftingLens, Color.Yellow).outputItem(SYSTEM_ON_CHIP_WAFER)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[EV]).inputItem(NAQUADAH_WAFER)
                .notConsumable(craftingLens, Color.Yellow).outputItem(SYSTEM_ON_CHIP_WAFER, 4)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(200).EUt(VA[IV]).inputItem(NEUTRONIUM_WAFER)
                .notConsumable(craftingLens, Color.Yellow).outputItem(SYSTEM_ON_CHIP_WAFER, 8)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[EV]).inputItem(NAQUADAH_WAFER)
                .notConsumable(craftingLens, Color.Purple).outputItem(ADVANCED_SYSTEM_ON_CHIP_WAFER)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(500).EUt(VA[IV]).inputItem(NEUTRONIUM_WAFER)
                .notConsumable(craftingLens, Color.Purple).outputItem(ADVANCED_SYSTEM_ON_CHIP_WAFER, 2)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        // Can replace this with a Quantum Star/Eye Lens if desired
        LASER_ENGRAVER_RECIPES.recipeBuilder().duration(900).EUt(VA[IV]).inputItem(NEUTRONIUM_WAFER)
                .notConsumable(craftingLens, Color.Black).outputItem(HIGHLY_ADVANCED_SOC_WAFER)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        // Wafer chemical refining recipes
        CHEMICAL_RECIPES.recipeBuilder()
                .inputItem(POWER_INTEGRATED_CIRCUIT_WAFER)
                .inputItem(dust, IndiumGalliumPhosphide, 2)
                .fluidInputs(VanadiumGallium.getFluid(L * 2))
                .outputItem(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1200).EUt(VA[IV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputItem(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .inputItem(dust, IndiumGalliumPhosphide, 8)
                .fluidInputs(Naquadah.getFluid(L * 4))
                .outputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1200).EUt(VA[LuV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputItem(CENTRAL_PROCESSING_UNIT_WAFER)
                .inputItem(CARBON_FIBERS, 16)
                .fluidInputs(Glowstone.getFluid(L * 4))
                .outputItem(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1200).EUt(VA[EV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputItem(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .inputItem(QUANTUM_EYE, 2)
                .fluidInputs(GalliumArsenide.getFluid(L * 2))
                .outputItem(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(900).EUt(VA[EV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputItem(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .inputItem(dust, IndiumGalliumPhosphide)
                .fluidInputs(Radon.getFluid(50))
                .outputItem(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1200).EUt(VA[EV]).buildAndRegister();

        // Wafer cutting
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[IV]).inputItem(HIGHLY_ADVANCED_SOC_WAFER)
                .outputItem(HIGHLY_ADVANCED_SOC, 6).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[EV]).inputItem(ADVANCED_SYSTEM_ON_CHIP_WAFER)
                .outputItem(ADVANCED_SYSTEM_ON_CHIP, 6).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).inputItem(SYSTEM_ON_CHIP_WAFER).outputItem(SYSTEM_ON_CHIP, 6)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(64).inputItem(SIMPLE_SYSTEM_ON_CHIP_WAFER)
                .outputItem(SIMPLE_SYSTEM_ON_CHIP, 6).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(96).inputItem(RANDOM_ACCESS_MEMORY_WAFER)
                .outputItem(RANDOM_ACCESS_MEMORY, 32).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[EV]).inputItem(QUBIT_CENTRAL_PROCESSING_UNIT_WAFER)
                .outputItem(QUBIT_CENTRAL_PROCESSING_UNIT, 4).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).inputItem(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER)
                .outputItem(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 6).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).inputItem(LOW_POWER_INTEGRATED_CIRCUIT_WAFER)
                .outputItem(LOW_POWER_INTEGRATED_CIRCUIT, 4).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[EV]).inputItem(POWER_INTEGRATED_CIRCUIT_WAFER)
                .outputItem(POWER_INTEGRATED_CIRCUIT, 4).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[IV]).inputItem(HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .outputItem(HIGH_POWER_INTEGRATED_CIRCUIT, 2).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[LuV]).inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
                .outputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(192).inputItem(NOR_MEMORY_CHIP_WAFER).outputItem(NOR_MEMORY_CHIP, 16)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(192).inputItem(NAND_MEMORY_CHIP_WAFER).outputItem(NAND_MEMORY_CHIP, 32)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[MV]).inputItem(CENTRAL_PROCESSING_UNIT_WAFER)
                .outputItem(CENTRAL_PROCESSING_UNIT, 8).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(64).inputItem(INTEGRATED_LOGIC_CIRCUIT_WAFER)
                .outputItem(INTEGRATED_LOGIC_CIRCUIT, 8).buildAndRegister();
        CUTTER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV]).inputItem(NANO_CENTRAL_PROCESSING_UNIT_WAFER)
                .outputItem(NANO_CENTRAL_PROCESSING_UNIT, 8).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
    }

    private static void componentRecipes() {
        // Vacuum Tube
        ModHandler.addShapedRecipe("vacuum_tube", VACUUM_TUBE.getStackForm(),
                "PTP", "WWW",
                'P', new UnificationEntry(bolt, Steel),
                'T', GLASS_TUBE.getStackForm(),
                'W', new UnificationEntry(wireGtSingle, Copper));

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(GLASS_TUBE)
                .inputItem(bolt, Steel, 2)
                .inputItem(wireGtSingle, Copper, 2)
                .circuitMeta(1)
                .outputItem(VACUUM_TUBE, 2)
                .duration(160).EUt(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(GLASS_TUBE)
                .inputItem(bolt, Steel, 2)
                .inputItem(wireGtSingle, Copper, 2)
                .fluidInputs(RedAlloy.getFluid(18))
                .outputItem(VACUUM_TUBE, 3)
                .duration(160).EUt(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(GLASS_TUBE)
                .inputItem(bolt, Steel, 2)
                .inputItem(wireGtSingle, AnnealedCopper, 2)
                .fluidInputs(RedAlloy.getFluid(18))
                .outputItem(VACUUM_TUBE, 4)
                .duration(160).EUt(VA[ULV]).buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder()
                .inputItem(dust, Glass)
                .notConsumable(SHAPE_MOLD_BALL)
                .outputItem(GLASS_TUBE)
                .duration(160).EUt(16).buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .fluidInputs(Glass.getFluid(GTValues.L))
                .notConsumable(SHAPE_MOLD_BALL)
                .outputItem(GLASS_TUBE)
                .duration(200).EUt(24).buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder()
                .inputItem(dust, Glass)
                .notConsumable(SHAPE_MOLD_BALL)
                .outputItem(GLASS_TUBE)
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
                .inputItem(dust, Coal)
                .inputItem(wireFine, Copper, 4)
                .outputItem(RESISTOR, 2)
                .fluidInputs(Glue.getFluid(100))
                .duration(160).EUt(6).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Charcoal)
                .inputItem(wireFine, Copper, 4)
                .outputItem(RESISTOR, 2)
                .fluidInputs(Glue.getFluid(100))
                .duration(160).EUt(6).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Carbon)
                .inputItem(wireFine, Copper, 4)
                .outputItem(RESISTOR, 2)
                .fluidInputs(Glue.getFluid(100))
                .duration(160).EUt(6).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Coal)
                .inputItem(wireFine, AnnealedCopper, 4)
                .outputItem(RESISTOR, 4)
                .fluidInputs(Glue.getFluid(100))
                .duration(160).EUt(6).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Charcoal)
                .inputItem(wireFine, AnnealedCopper, 4)
                .outputItem(RESISTOR, 4)
                .fluidInputs(Glue.getFluid(100))
                .duration(160).EUt(6).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Carbon)
                .inputItem(wireFine, AnnealedCopper, 4)
                .outputItem(RESISTOR, 4)
                .fluidInputs(Glue.getFluid(100))
                .duration(160).EUt(6).buildAndRegister();

        // Capacitor
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(foil, Polyethylene)
                .inputItem(foil, Aluminium, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputItem(CAPACITOR, 8)
                .duration(320).EUt(VA[MV]).buildAndRegister();

        // Transistor
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(plate, Silicon)
                .inputItem(wireFine, Tin, 6)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputItem(TRANSISTOR, 8)
                .duration(160).EUt(VA[MV]).buildAndRegister();

        // Diode
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(wireFine, Copper, 4)
                .inputItem(dustSmall, GalliumArsenide)
                .fluidInputs(Glass.getFluid(L))
                .outputItem(DIODE)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(wireFine, AnnealedCopper, 4)
                .inputItem(dustSmall, GalliumArsenide)
                .fluidInputs(Glass.getFluid(L))
                .outputItem(DIODE, 2)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(wireFine, Copper, 4)
                .inputItem(dustSmall, GalliumArsenide)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputItem(DIODE, 2)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(wireFine, Copper, 4)
                .inputItem(SILICON_WAFER)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputItem(DIODE, 2)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(wireFine, AnnealedCopper, 4)
                .inputItem(dustSmall, GalliumArsenide)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputItem(DIODE, 4)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(wireFine, AnnealedCopper, 4)
                .inputItem(SILICON_WAFER)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputItem(DIODE, 4)
                .duration(400).EUt(VA[LV]).buildAndRegister();

        // Inductor
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ring, Steel)
                .inputItem(wireFine, Copper, 2)
                .fluidInputs(Polyethylene.getFluid(L / 4))
                .outputItem(INDUCTOR, 2)
                .duration(320).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ring, Steel)
                .inputItem(wireFine, AnnealedCopper, 2)
                .fluidInputs(Polyethylene.getFluid(L / 4))
                .outputItem(INDUCTOR, 4)
                .duration(320).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ring, NickelZincFerrite)
                .inputItem(wireFine, Copper, 2)
                .fluidInputs(Polyethylene.getFluid(L / 4))
                .outputItem(INDUCTOR, 4)
                .duration(320).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ring, NickelZincFerrite)
                .inputItem(wireFine, AnnealedCopper, 2)
                .fluidInputs(Polyethylene.getFluid(L / 4))
                .outputItem(INDUCTOR, 8)
                .duration(320).EUt(VA[MV]).buildAndRegister();

        // SMD Resistor
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Carbon)
                .inputItem(wireFine, Electrum, 4)
                .fluidInputs(Polyethylene.getFluid(L * 2))
                .outputItem(SMD_RESISTOR, 16)
                .duration(160).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Carbon)
                .inputItem(wireFine, Tantalum, 4)
                .fluidInputs(Polyethylene.getFluid(L * 2))
                .outputItem(SMD_RESISTOR, 32)
                .duration(160).EUt(VA[HV]).buildAndRegister();

        // SMD Diode
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, GalliumArsenide)
                .inputItem(wireFine, Platinum, 8)
                .fluidInputs(Polyethylene.getFluid(L * 2))
                .outputItem(SMD_DIODE, 32)
                .duration(200).EUt(VA[HV]).buildAndRegister();

        // SMD Transistor
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(foil, Gallium)
                .inputItem(wireFine, AnnealedCopper, 8)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputItem(SMD_TRANSISTOR, 16)
                .duration(160).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(foil, Gallium)
                .inputItem(wireFine, Tantalum, 8)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputItem(SMD_TRANSISTOR, 32)
                .duration(160).EUt(VA[HV]).buildAndRegister();

        // SMD Capacitor
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(foil, SiliconeRubber)
                .inputItem(foil, Aluminium)
                .fluidInputs(Polyethylene.getFluid(L / 2))
                .outputItem(SMD_CAPACITOR, 8)
                .duration(80).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(foil, PolyvinylChloride, 2)
                .inputItem(foil, Aluminium)
                .fluidInputs(Polyethylene.getFluid(L / 2))
                .outputItem(SMD_CAPACITOR, 12)
                .duration(80).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(foil, SiliconeRubber)
                .inputItem(foil, Tantalum)
                .fluidInputs(Polyethylene.getFluid(L / 2))
                .outputItem(SMD_CAPACITOR, 16)
                .duration(120).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(foil, PolyvinylChloride, 2)
                .inputItem(foil, Tantalum)
                .fluidInputs(Polyethylene.getFluid(L / 2))
                .outputItem(SMD_CAPACITOR, 24)
                .duration(120).EUt(VA[HV]).buildAndRegister();

        // SMD Inductor
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ring, NickelZincFerrite)
                .inputItem(wireFine, Cupronickel, 4)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputItem(SMD_INDUCTOR, 16)
                .duration(160).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ring, NickelZincFerrite)
                .inputItem(wireFine, Tantalum, 4)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputItem(SMD_INDUCTOR, 32)
                .duration(160).EUt(VA[HV]).buildAndRegister();

        // Advanced SMD Resistor
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Graphene)
                .inputItem(wireFine, Platinum, 4)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .outputItem(ADVANCED_SMD_RESISTOR, 16)
                .EUt(3840).duration(160).buildAndRegister();

        // Advanced SMD Diode
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, IndiumGalliumPhosphide)
                .inputItem(wireFine, NiobiumTitanium, 16)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .outputItem(ADVANCED_SMD_DIODE, 64)
                .EUt(3840).duration(640).buildAndRegister();

        // Advanced SMD Transistor
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(foil, VanadiumGallium)
                .inputItem(wireFine, HSSG, 8)
                .fluidInputs(Polybenzimidazole.getFluid(L))
                .outputItem(ADVANCED_SMD_TRANSISTOR, 16)
                .EUt(3840).duration(160).buildAndRegister();

        // Advanced SMD Capacitor
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(foil, Polybenzimidazole, 2)
                .inputItem(foil, HSSS)
                .fluidInputs(Polybenzimidazole.getFluid(L / 4))
                .outputItem(ADVANCED_SMD_CAPACITOR, 16)
                .EUt(3840).duration(80).buildAndRegister();

        // Advanced SMD Inductor
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ring, HSSE)
                .inputItem(wireFine, Palladium, 4)
                .fluidInputs(Polybenzimidazole.getFluid(L))
                .outputItem(ADVANCED_SMD_INDUCTOR, 16)
                .EUt(3840).duration(160).buildAndRegister();

        // Carbon Fibers
        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(dust, Carbon, 4)
                .fluidInputs(Polyethylene.getFluid(L / 4))
                .outputItem(CARBON_FIBERS)
                .duration(37).EUt(VA[LV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(dust, Carbon, 4)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L / 8))
                .outputItem(CARBON_FIBERS, 2)
                .duration(37).EUt(VA[MV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(dust, Carbon, 4)
                .fluidInputs(Epoxy.getFluid(L / 16))
                .outputItem(CARBON_FIBERS, 4)
                .duration(37).EUt(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(dust, Carbon, 8)
                .fluidInputs(Polybenzimidazole.getFluid(L / 16))
                .outputItem(CARBON_FIBERS, 16)
                .duration(37).EUt(VA[EV]).buildAndRegister();

        // Crystal Circuit Components
        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .inputItem(ENGRAVED_CRYSTAL_CHIP)
                .notConsumable(craftingLens, Color.Lime)
                .outputItem(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).EUt(10000).buildAndRegister();

        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .inputItem(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .notConsumable(craftingLens, Color.Blue)
                .outputItem(CRYSTAL_SYSTEM_ON_CHIP)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).EUt(40000).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(gemExquisite, Emerald)
                .fluidInputs(Europium.getFluid(L / 9))
                .chancedOutput(RAW_CRYSTAL_CHIP, 1000, 2000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(320).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(gemExquisite, Olivine)
                .fluidInputs(Europium.getFluid(L / 9))
                .chancedOutput(RAW_CRYSTAL_CHIP, 1000, 2000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(320).buildAndRegister();

        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputItem(RAW_CRYSTAL_CHIP)
                .outputItem(RAW_CRYSTAL_CHIP_PART, 9)
                .EUt(VA[HV]).duration(100).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(RAW_CRYSTAL_CHIP_PART)
                .fluidInputs(Europium.getFluid(L / 9))
                .outputItem(RAW_CRYSTAL_CHIP)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(RAW_CRYSTAL_CHIP_PART)
                .fluidInputs(Mutagen.getFluid(250))
                .chancedOutput(RAW_CRYSTAL_CHIP, 8000, 250)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(RAW_CRYSTAL_CHIP_PART)
                .fluidInputs(BacterialSludge.getFluid(250))
                .chancedOutput(RAW_CRYSTAL_CHIP, 8000, 250)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(12000).EUt(VA[HV]).buildAndRegister();

        BLAST_RECIPES.recipeBuilder()
                .inputItem(plate, Emerald)
                .inputItem(RAW_CRYSTAL_CHIP)
                .fluidInputs(Helium.getFluid(1000))
                .outputItem(ENGRAVED_CRYSTAL_CHIP)
                .blastFurnaceTemp(5000)
                .duration(900).EUt(VA[HV]).buildAndRegister();

        BLAST_RECIPES.recipeBuilder()
                .inputItem(plate, Olivine)
                .inputItem(RAW_CRYSTAL_CHIP)
                .fluidInputs(Helium.getFluid(1000))
                .outputItem(ENGRAVED_CRYSTAL_CHIP)
                .blastFurnaceTemp(5000)
                .duration(900).EUt(VA[HV]).buildAndRegister();

        // Quantum Parts
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(gem, EnderEye)
                .fluidInputs(Radon.getFluid(250))
                .outputItem(QUANTUM_EYE)
                .duration(480).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(gem, NetherStar)
                .fluidInputs(Radon.getFluid(1250))
                .outputItem(QUANTUM_STAR)
                .duration(1920).EUt(VA[HV]).buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputItem(QUANTUM_STAR)
                .fluidInputs(Neutronium.getFluid(L * 2))
                .outputItem(GRAVI_STAR)
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
                .inputItem(foil, Copper, 4)
                .inputItem(plate, Wood)
                .fluidInputs(Glue.getFluid(100))
                .outputItem(BASIC_CIRCUIT_BOARD)
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        // Phenolic Board
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(dust, Wood)
                .circuitMeta(1)
                .fluidInputs(Glue.getFluid(50))
                .outputItem(PHENOLIC_BOARD)
                .duration(150).EUt(VA[LV]).buildAndRegister();

        // Good Circuit Board
        ModHandler.addShapedRecipe("good_circuit_board", GOOD_CIRCUIT_BOARD.getStackForm(),
                "WWW", "WBW", "WWW",
                'W', new UnificationEntry(wireGtSingle, Silver),
                'B', PHENOLIC_BOARD.getStackForm());

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(300)
                .inputItem(foil, Silver, 4)
                .inputItem(PHENOLIC_BOARD)
                .fluidInputs(SodiumPersulfate.getFluid(200))
                .outputItem(GOOD_CIRCUIT_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LV]).duration(300)
                .inputItem(foil, Silver, 4)
                .inputItem(PHENOLIC_BOARD)
                .fluidInputs(Iron3Chloride.getFluid(100))
                .outputItem(GOOD_CIRCUIT_BOARD)
                .buildAndRegister();

        // Plastic Board
        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(10)
                .inputItem(plate, Polyethylene)
                .inputItem(foil, Copper, 4)
                .fluidInputs(SulfuricAcid.getFluid(250))
                .outputItem(PLASTIC_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(10)
                .inputItem(plate, PolyvinylChloride)
                .inputItem(foil, Copper, 4)
                .fluidInputs(SulfuricAcid.getFluid(250))
                .outputItem(PLASTIC_BOARD, 2)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(10)
                .inputItem(plate, Polytetrafluoroethylene)
                .inputItem(foil, Copper, 4)
                .fluidInputs(SulfuricAcid.getFluid(250))
                .outputItem(PLASTIC_BOARD, 4)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(10)
                .inputItem(plate, Polybenzimidazole)
                .inputItem(foil, Copper, 4)
                .fluidInputs(SulfuricAcid.getFluid(250))
                .outputItem(PLASTIC_BOARD, 8)
                .buildAndRegister();

        // Plastic Circuit Board
        CHEMICAL_RECIPES.recipeBuilder().duration(600).EUt(VA[LV])
                .inputItem(PLASTIC_BOARD)
                .inputItem(foil, Copper, 6)
                .fluidInputs(SodiumPersulfate.getFluid(500))
                .outputItem(PLASTIC_CIRCUIT_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(600).EUt(VA[LV])
                .inputItem(PLASTIC_BOARD)
                .inputItem(foil, Copper, 6)
                .fluidInputs(Iron3Chloride.getFluid(250))
                .outputItem(PLASTIC_CIRCUIT_BOARD)
                .buildAndRegister();

        // Epoxy Board
        CHEMICAL_RECIPES.recipeBuilder().duration(600).EUt(VA[LV])
                .inputItem(plate, Epoxy)
                .inputItem(foil, Gold, 8)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .outputItem(EPOXY_BOARD)
                .buildAndRegister();

        // Advanced Circuit Board
        CHEMICAL_RECIPES.recipeBuilder().duration(900).EUt(VA[LV])
                .inputItem(EPOXY_BOARD)
                .inputItem(foil, Electrum, 8)
                .fluidInputs(SodiumPersulfate.getFluid(1000))
                .outputItem(ADVANCED_CIRCUIT_BOARD)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(900).EUt(VA[LV])
                .inputItem(EPOXY_BOARD)
                .inputItem(foil, Electrum, 8)
                .fluidInputs(Iron3Chloride.getFluid(500))
                .outputItem(ADVANCED_CIRCUIT_BOARD)
                .buildAndRegister();

        // Fiber Reinforced Epoxy Board
        CHEMICAL_BATH_RECIPES.recipeBuilder().duration(240).EUt(16)
                .inputItem(wireFine, BorosilicateGlass)
                .fluidInputs(Epoxy.getFluid(L))
                .outputItem(plate, ReinforcedEpoxyResin)
                .buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder().duration(240).EUt(16)
                .inputItem(CARBON_FIBERS)
                .fluidInputs(Epoxy.getFluid(L))
                .outputItem(plate, ReinforcedEpoxyResin)
                .buildAndRegister();

        // Borosilicate Glass Recipes
        EXTRUDER_RECIPES.recipeBuilder().duration(160).EUt(96)
                .inputItem(ingot, BorosilicateGlass)
                .notConsumable(SHAPE_EXTRUDER_WIRE)
                .outputItem(wireFine, BorosilicateGlass, 8)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(10)
                .inputItem(plate, ReinforcedEpoxyResin)
                .inputItem(foil, AnnealedCopper, 8)
                .fluidInputs(SulfuricAcid.getFluid(125))
                .outputItem(FIBER_BOARD)
                .buildAndRegister();

        // Extreme Circuit Board
        CHEMICAL_RECIPES.recipeBuilder().duration(1200).EUt(VA[LV])
                .inputItem(FIBER_BOARD)
                .inputItem(foil, AnnealedCopper, 12)
                .fluidInputs(SodiumPersulfate.getFluid(2000))
                .outputItem(EXTREME_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(1200).EUt(VA[LV])
                .inputItem(FIBER_BOARD)
                .inputItem(foil, AnnealedCopper, 12)
                .fluidInputs(Iron3Chloride.getFluid(1000))
                .outputItem(EXTREME_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // Multi-Layer Fiber Reinforced Epoxy Board
        CHEMICAL_RECIPES.recipeBuilder().duration(500).EUt(VA[HV])
                .inputItem(FIBER_BOARD, 2)
                .inputItem(foil, Palladium, 8)
                .fluidInputs(SulfuricAcid.getFluid(500))
                .outputItem(MULTILAYER_FIBER_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // Elite Circuit Board
        CHEMICAL_RECIPES.recipeBuilder().duration(1500).EUt(VA[MV])
                .inputItem(MULTILAYER_FIBER_BOARD)
                .inputItem(foil, Platinum, 8)
                .fluidInputs(SodiumPersulfate.getFluid(4000))
                .outputItem(ELITE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(1500).EUt(VA[MV])
                .inputItem(MULTILAYER_FIBER_BOARD)
                .inputItem(foil, Platinum, 8)
                .fluidInputs(Iron3Chloride.getFluid(2000))
                .outputItem(ELITE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // Wetware Board

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(160).EUt(VA[HV])
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L / 4))
                .outputItem(PETRI_DISH)
                .buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder().duration(40).EUt(VA[HV])
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .fluidInputs(Polybenzimidazole.getFluid(L / 8))
                .outputItem(PETRI_DISH, 2)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().duration(1200).EUt(VA[LuV])
                .inputItem(MULTILAYER_FIBER_BOARD, 16)
                .inputItem(PETRI_DISH)
                .inputItem(ELECTRIC_PUMP_LuV)
                .inputItem(SENSOR_IV)
                .inputItem(circuit, Tier.IV)
                .inputItem(foil, NiobiumTitanium, 16)
                .fluidInputs(SterileGrowthMedium.getFluid(4000))
                .outputItem(WETWARE_BOARD, 16)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(1800).EUt(VA[HV])
                .inputItem(WETWARE_BOARD)
                .inputItem(foil, NiobiumTitanium, 32)
                .fluidInputs(SodiumPersulfate.getFluid(10000))
                .outputItem(WETWARE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().duration(1800).EUt(VA[HV])
                .inputItem(WETWARE_BOARD)
                .inputItem(foil, NiobiumTitanium, 32)
                .fluidInputs(Iron3Chloride.getFluid(5000))
                .outputItem(WETWARE_CIRCUIT_BOARD)
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
                .inputItem(BASIC_CIRCUIT_BOARD)
                .inputItem(component, Component.Resistor, 2)
                .inputItem(wireGtSingle, RedAlloy, 2)
                .inputItem(circuit, Tier.ULV, 2)
                .outputItem(ELECTRONIC_CIRCUIT_LV, outputAmount)
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
                .inputItem(GOOD_CIRCUIT_BOARD)
                .inputItem(circuit, Tier.LV, 2)
                .inputItem(component, Component.Diode, 2)
                .inputItem(wireGtSingle, Copper, 2)
                .outputItem(ELECTRONIC_CIRCUIT_MV)
                .buildAndRegister();

        // T2: Integrated ==============================================================================================

        // LV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(16).duration(200)
                .inputItem(BASIC_CIRCUIT_BOARD)
                .inputItem(INTEGRATED_LOGIC_CIRCUIT)
                .inputItem(component, Component.Resistor, 2)
                .inputItem(component, Component.Diode, 2)
                .inputItem(wireFine, Copper, 2)
                .inputItem(bolt, Tin, 2)
                .outputItem(INTEGRATED_CIRCUIT_LV, outputAmount)
                .buildAndRegister();

        // MV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(24).duration(400)
                .inputItem(GOOD_CIRCUIT_BOARD)
                .inputItem(INTEGRATED_CIRCUIT_LV, 2)
                .inputItem(component, Component.Resistor, 2)
                .inputItem(component, Component.Diode, 2)
                .inputItem(wireFine, Gold, 4)
                .inputItem(bolt, Silver, 4)
                .outputItem(INTEGRATED_CIRCUIT_MV, outputAmount)
                .buildAndRegister();

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LV]).duration(800)
                .inputItem(INTEGRATED_CIRCUIT_MV, outputAmount) // a little generous for this first HV if harder recipes
                                                            // enabled
                .inputItem(INTEGRATED_LOGIC_CIRCUIT, 2)
                .inputItem(RANDOM_ACCESS_MEMORY, 2)
                .inputItem(component, Component.Transistor, 4)
                .inputItem(wireFine, Electrum, 8)
                .inputItem(bolt, AnnealedCopper, 8)
                .outputItem(INTEGRATED_CIRCUIT_HV)
                .buildAndRegister();

        // T2.5: Misc ==================================================================================================

        // NAND Chip ULV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[MV]).duration(300)
                .inputItem(GOOD_CIRCUIT_BOARD)
                .inputItem(SIMPLE_SYSTEM_ON_CHIP)
                .inputItem(bolt, RedAlloy, 2)
                .inputItem(wireFine, Tin, 2)
                .outputItem(NAND_CHIP_ULV, outputAmount * 4)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[MV]).duration(300)
                .inputItem(PLASTIC_CIRCUIT_BOARD)
                .inputItem(SIMPLE_SYSTEM_ON_CHIP)
                .inputItem(bolt, RedAlloy, 2)
                .inputItem(wireFine, Tin, 2)
                .outputItem(NAND_CHIP_ULV, outputAmount * 6)
                .buildAndRegister();

        // Microprocessor LV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(60).duration(200)
                .inputItem(PLASTIC_CIRCUIT_BOARD)
                .inputItem(CENTRAL_PROCESSING_UNIT)
                .inputItem(component, Component.Resistor, 2)
                .inputItem(component, Component.Capacitor, 2)
                .inputItem(component, Component.Transistor, 2)
                .inputItem(wireFine, Copper, 2)
                .outputItem(MICROPROCESSOR_LV, ConfigHolder.recipes.harderCircuitRecipes ? 2 : 3)
                .buildAndRegister();

        // Microprocessor LV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(50)
                .inputItem(PLASTIC_CIRCUIT_BOARD)
                .inputItem(SYSTEM_ON_CHIP)
                .inputItem(wireFine, Copper, 2)
                .inputItem(bolt, Tin, 2)
                .outputItem(MICROPROCESSOR_LV, ConfigHolder.recipes.harderCircuitRecipes ? 3 : 6)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // T3: Processor ===============================================================================================

        // MV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(60).duration(200)
                .inputItem(PLASTIC_CIRCUIT_BOARD)
                .inputItem(CENTRAL_PROCESSING_UNIT)
                .inputItem(component, Component.Resistor, 4)
                .inputItem(component, Component.Capacitor, 4)
                .inputItem(component, Component.Transistor, 4)
                .inputItem(wireFine, RedAlloy, 4)
                .outputItem(PROCESSOR_MV, outputAmount)
                .buildAndRegister();

        // MV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(50)
                .inputItem(PLASTIC_CIRCUIT_BOARD)
                .inputItem(SYSTEM_ON_CHIP)
                .inputItem(wireFine, RedAlloy, 4)
                .inputItem(bolt, AnnealedCopper, 4)
                .outputItem(PROCESSOR_MV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(90).duration(400)
                .inputItem(PLASTIC_CIRCUIT_BOARD)
                .inputItem(PROCESSOR_MV, 2)
                .inputItem(component, Component.Inductor, 4)
                .inputItem(component, Component.Capacitor, 8)
                .inputItem(RANDOM_ACCESS_MEMORY, 4)
                .inputItem(wireFine, RedAlloy, 8)
                .outputItem(PROCESSOR_ASSEMBLY_HV)
                .solderMultiplier(2)
                .buildAndRegister();

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[MV]).duration(400)
                .inputItem(PLASTIC_CIRCUIT_BOARD)
                .inputItem(PROCESSOR_ASSEMBLY_HV, 2)
                .inputItem(component, Component.Diode, 4)
                .inputItem(RANDOM_ACCESS_MEMORY, 4)
                .inputItem(wireFine, Electrum, 16)
                .inputItem(bolt, BlueAlloy, 16)
                .outputItem(WORKSTATION_EV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[HV]).duration(800)
                .inputItem(frameGt, Aluminium, 2)
                .inputItem(WORKSTATION_EV, 2)
                .inputItem(component, Component.Inductor, 8)
                .inputItem(component, Component.Capacitor, 16)
                .inputItem(RANDOM_ACCESS_MEMORY, 16)
                .inputItem(wireGtSingle, AnnealedCopper, 16)
                .outputItem(MAINFRAME_IV)
                .solderMultiplier(4)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[HV]).duration(400)
                .inputItem(frameGt, Aluminium, 2)
                .inputItem(WORKSTATION_EV, 2)
                .inputItem(ADVANCED_SMD_INDUCTOR, 2)
                .inputItem(ADVANCED_SMD_CAPACITOR, 4)
                .inputItem(RANDOM_ACCESS_MEMORY, 16)
                .inputItem(wireGtSingle, AnnealedCopper, 16)
                .outputItem(MAINFRAME_IV)
                .solderMultiplier(4)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // T4: Nano ====================================================================================================

        // HV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(200)
                .inputItem(ADVANCED_CIRCUIT_BOARD)
                .inputItem(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItem(SMD_RESISTOR, 8)
                .inputItem(SMD_CAPACITOR, 8)
                .inputItem(SMD_TRANSISTOR, 8)
                .inputItem(wireFine, Electrum, 8)
                .outputItem(NANO_PROCESSOR_HV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(100)
                .inputItem(ADVANCED_CIRCUIT_BOARD)
                .inputItem(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItem(ADVANCED_SMD_RESISTOR, 2)
                .inputItem(ADVANCED_SMD_CAPACITOR, 2)
                .inputItem(ADVANCED_SMD_TRANSISTOR, 2)
                .inputItem(wireFine, Electrum, 8)
                .outputItem(NANO_PROCESSOR_HV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // HV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(9600).duration(50)
                .inputItem(ADVANCED_CIRCUIT_BOARD)
                .inputItem(ADVANCED_SYSTEM_ON_CHIP)
                .inputItem(wireFine, Electrum, 4)
                .inputItem(bolt, Platinum, 4)
                .outputItem(NANO_PROCESSOR_HV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(400)
                .inputItem(ADVANCED_CIRCUIT_BOARD)
                .inputItem(NANO_PROCESSOR_HV, 2)
                .inputItem(SMD_INDUCTOR, 4)
                .inputItem(SMD_CAPACITOR, 8)
                .inputItem(RANDOM_ACCESS_MEMORY, 8)
                .inputItem(wireFine, Electrum, 16)
                .outputItem(NANO_PROCESSOR_ASSEMBLY_EV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(200)
                .inputItem(ADVANCED_CIRCUIT_BOARD)
                .inputItem(NANO_PROCESSOR_HV, 2)
                .inputItem(ADVANCED_SMD_INDUCTOR)
                .inputItem(ADVANCED_SMD_CAPACITOR, 2)
                .inputItem(RANDOM_ACCESS_MEMORY, 8)
                .inputItem(wireFine, Electrum, 16)
                .outputItem(NANO_PROCESSOR_ASSEMBLY_EV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(400)
                .inputItem(ADVANCED_CIRCUIT_BOARD)
                .inputItem(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .inputItem(SMD_DIODE, 8)
                .inputItem(NOR_MEMORY_CHIP, 4)
                .inputItem(RANDOM_ACCESS_MEMORY, 16)
                .inputItem(wireFine, Electrum, 16)
                .outputItem(NANO_COMPUTER_IV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(600).duration(200)
                .inputItem(ADVANCED_CIRCUIT_BOARD)
                .inputItem(NANO_PROCESSOR_ASSEMBLY_EV, 2)
                .inputItem(ADVANCED_SMD_DIODE, 2)
                .inputItem(NOR_MEMORY_CHIP, 4)
                .inputItem(RANDOM_ACCESS_MEMORY, 16)
                .inputItem(wireFine, Electrum, 16)
                .outputItem(NANO_COMPUTER_IV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[EV]).duration(800)
                .inputItem(frameGt, Aluminium, 2)
                .inputItem(NANO_COMPUTER_IV, 2)
                .inputItem(SMD_INDUCTOR, 16)
                .inputItem(SMD_CAPACITOR, 32)
                .inputItem(RANDOM_ACCESS_MEMORY, 16)
                .inputItem(wireGtSingle, AnnealedCopper, 32)
                .outputItem(NANO_MAINFRAME_LUV)
                .solderMultiplier(4)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[EV]).duration(400)
                .inputItem(frameGt, Aluminium, 2)
                .inputItem(NANO_COMPUTER_IV, 2)
                .inputItem(ADVANCED_SMD_INDUCTOR, 4)
                .inputItem(ADVANCED_SMD_CAPACITOR, 8)
                .inputItem(RANDOM_ACCESS_MEMORY, 16)
                .inputItem(wireGtSingle, AnnealedCopper, 32)
                .outputItem(NANO_MAINFRAME_LUV)
                .solderMultiplier(4)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // T5: Quantum =================================================================================================

        // EV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(200)
                .inputItem(EXTREME_CIRCUIT_BOARD)
                .inputItem(QUBIT_CENTRAL_PROCESSING_UNIT)
                .inputItem(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItem(SMD_CAPACITOR, 12)
                .inputItem(SMD_TRANSISTOR, 12)
                .inputItem(wireFine, Platinum, 12)
                .outputItem(QUANTUM_PROCESSOR_EV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(100)
                .inputItem(EXTREME_CIRCUIT_BOARD)
                .inputItem(QUBIT_CENTRAL_PROCESSING_UNIT)
                .inputItem(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItem(ADVANCED_SMD_CAPACITOR, 3)
                .inputItem(ADVANCED_SMD_TRANSISTOR, 3)
                .inputItem(wireFine, Platinum, 12)
                .outputItem(QUANTUM_PROCESSOR_EV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // EV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(38400).duration(50)
                .inputItem(EXTREME_CIRCUIT_BOARD)
                .inputItem(ADVANCED_SYSTEM_ON_CHIP)
                .inputItem(wireFine, Platinum, 12)
                .inputItem(bolt, NiobiumTitanium, 8)
                .outputItem(QUANTUM_PROCESSOR_EV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(400)
                .inputItem(EXTREME_CIRCUIT_BOARD)
                .inputItem(QUANTUM_PROCESSOR_EV, 2)
                .inputItem(SMD_INDUCTOR, 8)
                .inputItem(SMD_CAPACITOR, 16)
                .inputItem(RANDOM_ACCESS_MEMORY, 4)
                .inputItem(wireFine, Platinum, 16)
                .outputItem(QUANTUM_ASSEMBLY_IV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(200)
                .inputItem(EXTREME_CIRCUIT_BOARD)
                .inputItem(QUANTUM_PROCESSOR_EV, 2)
                .inputItem(ADVANCED_SMD_INDUCTOR, 2)
                .inputItem(ADVANCED_SMD_CAPACITOR, 4)
                .inputItem(RANDOM_ACCESS_MEMORY, 4)
                .inputItem(wireFine, Platinum, 16)
                .outputItem(QUANTUM_ASSEMBLY_IV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(400)
                .inputItem(EXTREME_CIRCUIT_BOARD)
                .inputItem(QUANTUM_ASSEMBLY_IV, 2)
                .inputItem(SMD_DIODE, 8)
                .inputItem(NOR_MEMORY_CHIP, 4)
                .inputItem(RANDOM_ACCESS_MEMORY, 16)
                .inputItem(wireFine, Platinum, 32)
                .outputItem(QUANTUM_COMPUTER_LUV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(2400).duration(200)
                .inputItem(EXTREME_CIRCUIT_BOARD)
                .inputItem(QUANTUM_ASSEMBLY_IV, 2)
                .inputItem(ADVANCED_SMD_DIODE, 2)
                .inputItem(NOR_MEMORY_CHIP, 4)
                .inputItem(RANDOM_ACCESS_MEMORY, 16)
                .inputItem(wireFine, Platinum, 32)
                .outputItem(QUANTUM_COMPUTER_LUV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[IV]).duration(800)
                .inputItem(frameGt, HSSG, 2)
                .inputItem(QUANTUM_COMPUTER_LUV, 2)
                .inputItem(SMD_INDUCTOR, 24)
                .inputItem(SMD_CAPACITOR, 48)
                .inputItem(RANDOM_ACCESS_MEMORY, 24)
                .inputItem(wireGtSingle, AnnealedCopper, 48)
                .solderMultiplier(4)
                .outputItem(QUANTUM_MAINFRAME_ZPM)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[IV]).duration(400)
                .inputItem(frameGt, HSSG, 2)
                .inputItem(QUANTUM_COMPUTER_LUV, 2)
                .inputItem(ADVANCED_SMD_INDUCTOR, 6)
                .inputItem(ADVANCED_SMD_CAPACITOR, 12)
                .inputItem(RANDOM_ACCESS_MEMORY, 24)
                .inputItem(wireGtSingle, AnnealedCopper, 48)
                .solderMultiplier(4)
                .outputItem(QUANTUM_MAINFRAME_ZPM)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // T6: Crystal =================================================================================================

        // IV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(9600).duration(200)
                .inputItem(ELITE_CIRCUIT_BOARD)
                .inputItem(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .inputItem(NANO_CENTRAL_PROCESSING_UNIT, 2)
                .inputItem(ADVANCED_SMD_CAPACITOR, 6)
                .inputItem(ADVANCED_SMD_TRANSISTOR, 6)
                .inputItem(wireFine, NiobiumTitanium, 8)
                .outputItem(CRYSTAL_PROCESSOR_IV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // IV SoC
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(86000).duration(100)
                .inputItem(ELITE_CIRCUIT_BOARD)
                .inputItem(CRYSTAL_SYSTEM_ON_CHIP)
                .inputItem(wireFine, NiobiumTitanium, 8)
                .inputItem(bolt, YttriumBariumCuprate, 8)
                .outputItem(CRYSTAL_PROCESSOR_IV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(9600).duration(400)
                .inputItem(ELITE_CIRCUIT_BOARD)
                .inputItem(CRYSTAL_PROCESSOR_IV, 2)
                .inputItem(ADVANCED_SMD_INDUCTOR, 4)
                .inputItem(ADVANCED_SMD_CAPACITOR, 8)
                .inputItem(RANDOM_ACCESS_MEMORY, 24)
                .inputItem(wireFine, NiobiumTitanium, 16)
                .outputItem(CRYSTAL_ASSEMBLY_LUV)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(9600).duration(400)
                .inputItem(ELITE_CIRCUIT_BOARD)
                .inputItem(CRYSTAL_ASSEMBLY_LUV, 2)
                .inputItem(RANDOM_ACCESS_MEMORY, 4)
                .inputItem(NOR_MEMORY_CHIP, 32)
                .inputItem(NAND_MEMORY_CHIP, 64)
                .inputItem(wireFine, NiobiumTitanium, 32)
                .solderMultiplier(2)
                .outputItem(CRYSTAL_COMPUTER_ZPM)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // UV
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(VA[LuV]).duration(800)
                .inputItem(frameGt, HSSE, 2)
                .inputItem(CRYSTAL_COMPUTER_ZPM, 2)
                .inputItem(RANDOM_ACCESS_MEMORY, 32)
                .inputItem(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(wireGtSingle, NiobiumTitanium, 8)
                .inputItem(ADVANCED_SMD_INDUCTOR, 8)
                .inputItem(ADVANCED_SMD_CAPACITOR, 16)
                .inputItem(ADVANCED_SMD_DIODE, 8)
                .fluidInputs(SolderingAlloy.getFluid(L * 10))
                .outputItem(CRYSTAL_MAINFRAME_UV)
                .stationResearch(b -> b
                        .researchStack(CRYSTAL_COMPUTER_ZPM.getStackForm())
                        .CWUt(16))
                .buildAndRegister();

        // T7: Wetware =================================================================================================

        // Neuro Processing Unit
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(80000).duration(600)
                .inputItem(WETWARE_CIRCUIT_BOARD)
                .inputItem(STEM_CELLS, 16)
                .inputItem(pipeSmallFluid, Polybenzimidazole, 8)
                .inputItem(plate, Electrum, 8)
                .inputItem(foil, SiliconeRubber, 16)
                .inputItem(bolt, HSSE, 8)
                .fluidInputs(SterileGrowthMedium.getFluid(250))
                .outputItem(NEURO_PROCESSOR)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(38400).duration(200)
                .inputItem(NEURO_PROCESSOR)
                .inputItem(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .inputItem(NANO_CENTRAL_PROCESSING_UNIT)
                .inputItem(ADVANCED_SMD_CAPACITOR, 8)
                .inputItem(ADVANCED_SMD_TRANSISTOR, 8)
                .inputItem(wireFine, YttriumBariumCuprate, 8)
                .outputItem(WETWARE_PROCESSOR_LUV, outputAmount)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // SoC LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(150000).duration(100)
                .inputItem(NEURO_PROCESSOR)
                .inputItem(HIGHLY_ADVANCED_SOC)
                .inputItem(wireFine, YttriumBariumCuprate, 8)
                .inputItem(bolt, Naquadah, 8)
                .outputItem(WETWARE_PROCESSOR_LUV, outputAmount * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(38400).duration(400)
                .inputItem(WETWARE_CIRCUIT_BOARD)
                .inputItem(WETWARE_PROCESSOR_LUV, 2)
                .inputItem(ADVANCED_SMD_INDUCTOR, 6)
                .inputItem(ADVANCED_SMD_CAPACITOR, 12)
                .inputItem(RANDOM_ACCESS_MEMORY, 24)
                .inputItem(wireFine, YttriumBariumCuprate, 16)
                .solderMultiplier(2)
                .outputItem(WETWARE_PROCESSOR_ASSEMBLY_ZPM)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // UV
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(38400).duration(400)
                .inputItem(WETWARE_CIRCUIT_BOARD)
                .inputItem(WETWARE_PROCESSOR_ASSEMBLY_ZPM, 2)
                .inputItem(ADVANCED_SMD_DIODE, 8)
                .inputItem(NOR_MEMORY_CHIP, 16)
                .inputItem(RANDOM_ACCESS_MEMORY, 32)
                .inputItem(wireFine, YttriumBariumCuprate, 24)
                .inputItem(foil, Polybenzimidazole, 32)
                .inputItem(plate, Europium, 4)
                .fluidInputs(SolderingAlloy.getFluid(1152))
                .outputItem(WETWARE_SUPER_COMPUTER_UV)
                .stationResearch(b -> b
                        .researchStack(WETWARE_PROCESSOR_ASSEMBLY_ZPM.getStackForm())
                        .CWUt(16))
                .buildAndRegister();

        // UHV
        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(frameGt, Tritanium, 2)
                .inputItem(WETWARE_SUPER_COMPUTER_UV, 2)
                .inputItem(ADVANCED_SMD_DIODE, 32)
                .inputItem(ADVANCED_SMD_CAPACITOR, 32)
                .inputItem(ADVANCED_SMD_TRANSISTOR, 32)
                .inputItem(ADVANCED_SMD_RESISTOR, 32)
                .inputItem(ADVANCED_SMD_INDUCTOR, 32)
                .inputItem(foil, Polybenzimidazole, 64)
                .inputItem(RANDOM_ACCESS_MEMORY, 32)
                .inputItem(wireGtDouble, EnrichedNaquadahTriniumEuropiumDuranide, 16)
                .inputItem(plate, Europium, 8)
                .fluidInputs(SolderingAlloy.getFluid(L * 20))
                .fluidInputs(Polybenzimidazole.getFluid(L * 8))
                .outputItem(WETWARE_MAINFRAME_UHV)
                .stationResearch(b -> b
                        .researchStack(WETWARE_SUPER_COMPUTER_UV.getStackForm())
                        .CWUt(96)
                        .EUt(VA[UV]))
                .EUt(300000).duration(2000).buildAndRegister();

        // Misc ========================================================================================================

        // Data Stick
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ADVANCED_CIRCUIT_BOARD)
                .inputItem(circuit, Tier.HV, 2)
                .inputItem(RANDOM_ACCESS_MEMORY, 4)
                .inputItem(NOR_MEMORY_CHIP, 16)
                .inputItem(NAND_MEMORY_CHIP, 32)
                .inputItem(wireFine, Platinum, 32)
                .outputItem(TOOL_DATA_STICK)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(400).EUt(1200).buildAndRegister();

        // Data Orb
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(EXTREME_CIRCUIT_BOARD)
                .inputItem(circuit, Tier.IV, 2)
                .inputItem(RANDOM_ACCESS_MEMORY, 8)
                .inputItem(NOR_MEMORY_CHIP, 32)
                .inputItem(NAND_MEMORY_CHIP, 48)
                .inputItem(wireFine, NiobiumTitanium, 32)
                .outputItem(TOOL_DATA_ORB)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(400).EUt(9600).buildAndRegister();

        // Data Module
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(WETWARE_CIRCUIT_BOARD)
                .inputItem(circuit, Tier.ZPM, 2)
                .inputItem(RANDOM_ACCESS_MEMORY, 32)
                .inputItem(NOR_MEMORY_CHIP, 64)
                .inputItem(NAND_MEMORY_CHIP, 64)
                .inputItem(wireFine, YttriumBariumCuprate, 32)
                .outputItem(TOOL_DATA_MODULE)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .duration(400).EUt(38400).buildAndRegister();
    }
}
