package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.items.OreDictNames;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.ingredients.GTItemIngredient;
import gregtech.api.recipes.ingredients.OreItemIngredient;
import gregtech.api.recipes.ingredients.StandardItemIngredient;
import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.MarkerMaterials.Tier;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.MetaBlocks.LD_FLUID_PIPE;
import static gregtech.common.blocks.MetaBlocks.LD_ITEM_PIPE;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;

public class MetaTileEntityMachineRecipeLoader {

    public static void init() {
        // Fluid Hatches
        registerHatchBusRecipe(ULV, FLUID_IMPORT_HATCH[ULV], FLUID_EXPORT_HATCH[ULV], new ItemStack(Blocks.GLASS));
        registerHatchBusRecipe(LV, FLUID_IMPORT_HATCH[LV], FLUID_EXPORT_HATCH[LV], new ItemStack(Blocks.GLASS));
        registerHatchBusRecipe(MV, FLUID_IMPORT_HATCH[MV], FLUID_EXPORT_HATCH[MV], BRONZE_DRUM.getStackForm());
        registerHatchBusRecipe(HV, FLUID_IMPORT_HATCH[HV], FLUID_EXPORT_HATCH[HV], STEEL_DRUM.getStackForm());
        registerHatchBusRecipe(EV, FLUID_IMPORT_HATCH[EV], FLUID_EXPORT_HATCH[EV], ALUMINIUM_DRUM.getStackForm());
        registerHatchBusRecipe(IV, FLUID_IMPORT_HATCH[IV], FLUID_EXPORT_HATCH[IV], STAINLESS_STEEL_DRUM.getStackForm());
        registerHatchBusRecipe(LuV, FLUID_IMPORT_HATCH[LuV], FLUID_EXPORT_HATCH[LuV], TITANIUM_DRUM.getStackForm());
        registerHatchBusRecipe(ZPM, FLUID_IMPORT_HATCH[ZPM], FLUID_EXPORT_HATCH[ZPM],
                TUNGSTENSTEEL_DRUM.getStackForm());
        registerHatchBusRecipe(UV, FLUID_IMPORT_HATCH[UV], FLUID_EXPORT_HATCH[UV], QUANTUM_TANK[0].getStackForm());
        registerHatchBusRecipe(UHV, FLUID_IMPORT_HATCH[UHV], FLUID_EXPORT_HATCH[UHV], QUANTUM_TANK[1].getStackForm());

        // Quadruple Fluid Input Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_IMPORT_HATCH[EV])
                .inputItem(pipeQuadrupleFluid, Titanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(4)
                .outputItem(QUADRUPLE_IMPORT_HATCH[0])
                .duration(300).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_IMPORT_HATCH[IV])
                .inputItem(pipeQuadrupleFluid, TungstenSteel)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(4)
                .outputItem(QUADRUPLE_IMPORT_HATCH[1])
                .duration(300).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_IMPORT_HATCH[LuV])
                .inputItem(pipeQuadrupleFluid, NiobiumTitanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(4)
                .outputItem(QUADRUPLE_IMPORT_HATCH[2])
                .duration(300).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_IMPORT_HATCH[ZPM])
                .inputItem(pipeQuadrupleFluid, Iridium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .circuitMeta(4)
                .outputItem(QUADRUPLE_IMPORT_HATCH[3])
                .duration(300).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_IMPORT_HATCH[UV])
                .inputItem(pipeQuadrupleFluid, Naquadah)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .circuitMeta(4)
                .outputItem(QUADRUPLE_IMPORT_HATCH[4])
                .duration(300).volts(VA[UV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_IMPORT_HATCH[UHV])
                .inputItem(pipeQuadrupleFluid, Neutronium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .circuitMeta(4)
                .outputItem(QUADRUPLE_IMPORT_HATCH[5])
                .duration(300).volts(VA[UV]).buildAndRegister();

        // Nonuple Fluid Input Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_IMPORT_HATCH[EV])
                .inputItem(pipeNonupleFluid, Titanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 9))
                .circuitMeta(9)
                .outputItem(NONUPLE_IMPORT_HATCH[0])
                .duration(600).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_IMPORT_HATCH[IV])
                .inputItem(pipeNonupleFluid, TungstenSteel)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 9))
                .circuitMeta(9)
                .outputItem(NONUPLE_IMPORT_HATCH[1])
                .duration(600).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_IMPORT_HATCH[LuV])
                .inputItem(pipeNonupleFluid, NiobiumTitanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 9))
                .circuitMeta(9)
                .outputItem(NONUPLE_IMPORT_HATCH[2])
                .duration(600).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_IMPORT_HATCH[ZPM])
                .inputItem(pipeNonupleFluid, Iridium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 9))
                .circuitMeta(9)
                .outputItem(NONUPLE_IMPORT_HATCH[3])
                .duration(600).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_IMPORT_HATCH[UV])
                .inputItem(pipeNonupleFluid, Naquadah)
                .fluidInputs(Polybenzimidazole.getFluid(L * 9))
                .circuitMeta(9)
                .outputItem(NONUPLE_IMPORT_HATCH[4])
                .duration(600).volts(VA[UV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_IMPORT_HATCH[UHV])
                .inputItem(pipeNonupleFluid, Neutronium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 9))
                .circuitMeta(9)
                .outputItem(NONUPLE_IMPORT_HATCH[5])
                .duration(600).volts(VA[UV]).buildAndRegister();

        // Quadruple Fluid Output Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_EXPORT_HATCH[EV])
                .inputItem(pipeQuadrupleFluid, Titanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(4)
                .outputItem(QUADRUPLE_EXPORT_HATCH[0])
                .duration(300).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_EXPORT_HATCH[IV])
                .inputItem(pipeQuadrupleFluid, TungstenSteel)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(4)
                .outputItem(QUADRUPLE_EXPORT_HATCH[1])
                .duration(300).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_EXPORT_HATCH[LuV])
                .inputItem(pipeQuadrupleFluid, NiobiumTitanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(4)
                .outputItem(QUADRUPLE_EXPORT_HATCH[2])
                .duration(300).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_EXPORT_HATCH[ZPM])
                .inputItem(pipeQuadrupleFluid, Iridium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .circuitMeta(4)
                .outputItem(QUADRUPLE_EXPORT_HATCH[3])
                .duration(300).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_EXPORT_HATCH[UV])
                .inputItem(pipeQuadrupleFluid, Naquadah)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .circuitMeta(4)
                .outputItem(QUADRUPLE_EXPORT_HATCH[4])
                .duration(300).volts(VA[UV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_EXPORT_HATCH[UHV])
                .inputItem(pipeQuadrupleFluid, Neutronium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .circuitMeta(4)
                .outputItem(QUADRUPLE_EXPORT_HATCH[5])
                .duration(300).volts(VA[UV]).buildAndRegister();

        // Nonuple Fluid Output Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_EXPORT_HATCH[EV])
                .inputItem(pipeNonupleFluid, Titanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 9))
                .circuitMeta(9)
                .outputItem(NONUPLE_EXPORT_HATCH[0])
                .duration(600).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_EXPORT_HATCH[IV])
                .inputItem(pipeNonupleFluid, TungstenSteel)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 9))
                .circuitMeta(9)
                .outputItem(NONUPLE_EXPORT_HATCH[1])
                .duration(600).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_EXPORT_HATCH[LuV])
                .inputItem(pipeNonupleFluid, NiobiumTitanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 9))
                .circuitMeta(9)
                .outputItem(NONUPLE_EXPORT_HATCH[2])
                .duration(600).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_EXPORT_HATCH[ZPM])
                .inputItem(pipeNonupleFluid, Iridium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 9))
                .circuitMeta(9)
                .outputItem(NONUPLE_EXPORT_HATCH[3])
                .duration(600).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_EXPORT_HATCH[UV])
                .inputItem(pipeNonupleFluid, Naquadah)
                .fluidInputs(Polybenzimidazole.getFluid(L * 9))
                .circuitMeta(9)
                .outputItem(NONUPLE_EXPORT_HATCH[4])
                .duration(600).volts(VA[UV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(FLUID_EXPORT_HATCH[UHV])
                .inputItem(pipeNonupleFluid, Neutronium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 9))
                .circuitMeta(9)
                .outputItem(NONUPLE_EXPORT_HATCH[5])
                .duration(600).volts(VA[UV]).buildAndRegister();

        // Reservoir Hatch
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(COVER_INFINITE_WATER)
                .inputItem(FLUID_IMPORT_HATCH[EV])
                .inputItem(ELECTRIC_PUMP_EV)
                .outputItem(RESERVOIR_HATCH)
                .duration(300).volts(VA[EV]).buildAndRegister();

        // Item Buses
        registerHatchBusRecipe(ULV, ITEM_IMPORT_BUS[ULV], ITEM_EXPORT_BUS[ULV], OreDictNames.chestWood.toString());
        registerHatchBusRecipe(LV, ITEM_IMPORT_BUS[LV], ITEM_EXPORT_BUS[LV], OreDictNames.chestWood.toString());
        registerHatchBusRecipe(MV, ITEM_IMPORT_BUS[MV], ITEM_EXPORT_BUS[MV], BRONZE_CRATE.getStackForm());
        registerHatchBusRecipe(HV, ITEM_IMPORT_BUS[HV], ITEM_EXPORT_BUS[HV], STEEL_CRATE.getStackForm());
        registerHatchBusRecipe(EV, ITEM_IMPORT_BUS[EV], ITEM_EXPORT_BUS[EV], ALUMINIUM_CRATE.getStackForm());
        registerHatchBusRecipe(IV, ITEM_IMPORT_BUS[IV], ITEM_EXPORT_BUS[IV], STAINLESS_STEEL_CRATE.getStackForm());
        registerHatchBusRecipe(LuV, ITEM_IMPORT_BUS[LuV], ITEM_EXPORT_BUS[LuV], TITANIUM_CRATE.getStackForm());
        registerHatchBusRecipe(ZPM, ITEM_IMPORT_BUS[ZPM], ITEM_EXPORT_BUS[ZPM], TUNGSTENSTEEL_CRATE.getStackForm());
        registerHatchBusRecipe(UV, ITEM_IMPORT_BUS[UV], ITEM_EXPORT_BUS[UV], QUANTUM_CHEST[0].getStackForm());
        registerHatchBusRecipe(UHV, ITEM_IMPORT_BUS[UHV], ITEM_EXPORT_BUS[UHV], QUANTUM_CHEST[1].getStackForm());

        // Laser Hatches
        registerLaserRecipes();

        // Energy Output Hatches

        ModHandler.addShapedRecipe(true, "dynamo_hatch.ulv", ENERGY_OUTPUT_HATCH[ULV].getStackForm(),
                " V ", "SHS", "   ",
                'S', new UnificationEntry(spring, Lead),
                'V', VOLTAGE_COIL_ULV.getStackForm(),
                'H', HULL[ULV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[ULV])
                .inputItem(spring, Lead, 2)
                .inputItem(VOLTAGE_COIL_ULV)
                .outputItem(ENERGY_OUTPUT_HATCH[ULV])
                .duration(200).volts(VA[ULV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "dynamo_hatch.lv", ENERGY_OUTPUT_HATCH[LV].getStackForm(),
                " V ", "SHS", "   ",
                'S', new UnificationEntry(spring, Tin),
                'V', VOLTAGE_COIL_LV.getStackForm(),
                'H', HULL[LV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[LV])
                .inputItem(spring, Tin, 2)
                .inputItem(VOLTAGE_COIL_LV)
                .outputItem(ENERGY_OUTPUT_HATCH[LV])
                .duration(200).volts(VA[LV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "dynamo_hatch.mv", ENERGY_OUTPUT_HATCH[MV].getStackForm(),
                " V ", "SHS", " P ",
                'P', ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(),
                'S', new UnificationEntry(spring, Copper),
                'V', VOLTAGE_COIL_MV.getStackForm(),
                'H', HULL[MV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[MV])
                .inputItem(spring, Copper, 2)
                .inputItem(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
                .inputItem(VOLTAGE_COIL_MV)
                .outputItem(ENERGY_OUTPUT_HATCH[MV])
                .duration(200).volts(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[HV])
                .inputItem(spring, Gold, 2)
                .inputItem(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(VOLTAGE_COIL_HV)
                .fluidInputs(SodiumPotassium.getFluid(1000))
                .outputItem(ENERGY_OUTPUT_HATCH[HV])
                .duration(200).volts(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[EV])
                .inputItem(spring, Aluminium, 2)
                .inputItem(POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(VOLTAGE_COIL_EV)
                .fluidInputs(SodiumPotassium.getFluid(2000))
                .outputItem(ENERGY_OUTPUT_HATCH[EV])
                .duration(200).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[IV])
                .inputItem(spring, Tungsten, 2)
                .inputItem(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(VOLTAGE_COIL_IV)
                .fluidInputs(SodiumPotassium.getFluid(3000))
                .outputItem(ENERGY_OUTPUT_HATCH[IV])
                .duration(200).volts(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(HULL[LuV])
                .inputItem(spring, NiobiumTitanium, 4)
                .inputItem(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(circuit, Tier.LuV)
                .inputItem(VOLTAGE_COIL_LuV, 2)
                .fluidInputs(SodiumPotassium.getFluid(6000))
                .fluidInputs(SolderingAlloy.getFluid(720))
                .outputItem(ENERGY_OUTPUT_HATCH[LuV])
                .scannerResearch(b -> b
                        .researchStack(ENERGY_OUTPUT_HATCH[IV].getStackForm())
                        .EUt(VA[EV]))
                .duration(400).volts(VA[LuV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(HULL[ZPM])
                .inputItem(spring, VanadiumGallium, 4)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(circuit, Tier.ZPM)
                .inputItem(VOLTAGE_COIL_ZPM, 2)
                .fluidInputs(SodiumPotassium.getFluid(8000))
                .fluidInputs(SolderingAlloy.getFluid(1440))
                .outputItem(ENERGY_OUTPUT_HATCH[ZPM])
                .stationResearch(b -> b
                        .researchStack(ENERGY_OUTPUT_HATCH[LuV].getStackForm())
                        .CWUt(8))
                .duration(600).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(HULL[UV])
                .inputItem(spring, YttriumBariumCuprate, 4)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(circuit, Tier.UV)
                .inputItem(VOLTAGE_COIL_UV, 2)
                .fluidInputs(SodiumPotassium.getFluid(10000))
                .fluidInputs(SolderingAlloy.getFluid(2880))
                .outputItem(ENERGY_OUTPUT_HATCH[UV])
                .stationResearch(b -> b
                        .researchStack(ENERGY_OUTPUT_HATCH[ZPM].getStackForm())
                        .CWUt(64)
                        .EUt(VA[ZPM]))
                .duration(800).volts(VA[UV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(HULL[UHV])
                .inputItem(spring, Europium, 4)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(circuit, Tier.UHV)
                .inputItem(wireGtDouble, RutheniumTriniumAmericiumNeutronate, 2)
                .fluidInputs(SodiumPotassium.getFluid(12000))
                .fluidInputs(SolderingAlloy.getFluid(5760))
                .outputItem(ENERGY_OUTPUT_HATCH[UHV])
                .stationResearch(b -> b
                        .researchStack(ENERGY_OUTPUT_HATCH[UV].getStackForm())
                        .CWUt(128)
                        .EUt(VA[UV]))
                .duration(1000).volts(VA[UHV]).buildAndRegister();

        // Energy Input Hatches

        ModHandler.addShapedRecipe(true, "energy_hatch.ulv", ENERGY_INPUT_HATCH[ULV].getStackForm(),
                " V ", "CHC", "   ",
                'C', new UnificationEntry(cableGtSingle, RedAlloy),
                'V', VOLTAGE_COIL_ULV.getStackForm(),
                'H', HULL[ULV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[ULV])
                .inputItem(cableGtSingle, RedAlloy, 2)
                .inputItem(VOLTAGE_COIL_ULV)
                .outputItem(ENERGY_INPUT_HATCH[ULV])
                .duration(200).volts(VA[ULV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "energy_hatch.lv", ENERGY_INPUT_HATCH[LV].getStackForm(),
                " V ", "CHC", "   ",
                'C', new UnificationEntry(cableGtSingle, Tin),
                'V', VOLTAGE_COIL_LV.getStackForm(),
                'H', HULL[LV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[LV])
                .inputItem(cableGtSingle, Tin, 2)
                .inputItem(VOLTAGE_COIL_LV)
                .outputItem(ENERGY_INPUT_HATCH[LV])
                .duration(200).volts(VA[LV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "energy_hatch.mv", ENERGY_INPUT_HATCH[MV].getStackForm(),
                " V ", "CHC", " P ",
                'C', new UnificationEntry(cableGtSingle, Copper),
                'P', ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(),
                'V', VOLTAGE_COIL_MV.getStackForm(),
                'H', HULL[MV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[MV])
                .inputItem(cableGtSingle, Copper, 2)
                .inputItem(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
                .inputItem(VOLTAGE_COIL_MV)
                .outputItem(ENERGY_INPUT_HATCH[MV])
                .duration(200).volts(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[HV])
                .inputItem(cableGtSingle, Gold, 2)
                .inputItem(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(VOLTAGE_COIL_HV)
                .fluidInputs(SodiumPotassium.getFluid(1000))
                .outputItem(ENERGY_INPUT_HATCH[HV])
                .duration(200).volts(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[EV])
                .inputItem(cableGtSingle, Aluminium, 2)
                .inputItem(POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(VOLTAGE_COIL_EV)
                .fluidInputs(SodiumPotassium.getFluid(2000))
                .outputItem(ENERGY_INPUT_HATCH[EV])
                .duration(200).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[IV])
                .inputItem(cableGtSingle, Tungsten, 2)
                .inputItem(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(VOLTAGE_COIL_IV)
                .fluidInputs(SodiumPotassium.getFluid(3000))
                .outputItem(ENERGY_INPUT_HATCH[IV])
                .duration(200).volts(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(HULL[LuV])
                .inputItem(cableGtSingle, NiobiumTitanium, 4)
                .inputItem(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(circuit, Tier.LuV)
                .inputItem(VOLTAGE_COIL_LuV, 2)
                .fluidInputs(SodiumPotassium.getFluid(6000))
                .fluidInputs(SolderingAlloy.getFluid(720))
                .outputItem(ENERGY_INPUT_HATCH[LuV])
                .scannerResearch(b -> b
                        .researchStack(ENERGY_INPUT_HATCH[IV].getStackForm())
                        .EUt(VA[EV]))
                .duration(400).volts(VA[LuV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(HULL[ZPM])
                .inputItem(cableGtSingle, VanadiumGallium, 4)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(circuit, Tier.ZPM)
                .inputItem(VOLTAGE_COIL_ZPM, 2)
                .fluidInputs(SodiumPotassium.getFluid(8000))
                .fluidInputs(SolderingAlloy.getFluid(1440))
                .outputItem(ENERGY_INPUT_HATCH[ZPM])
                .stationResearch(b -> b
                        .researchStack(ENERGY_INPUT_HATCH[LuV].getStackForm())
                        .CWUt(8))
                .duration(600).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(HULL[UV])
                .inputItem(cableGtSingle, YttriumBariumCuprate, 4)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(circuit, Tier.UV)
                .inputItem(VOLTAGE_COIL_UV, 2)
                .fluidInputs(SodiumPotassium.getFluid(10000))
                .fluidInputs(SolderingAlloy.getFluid(2880))
                .outputItem(ENERGY_INPUT_HATCH[UV])
                .stationResearch(b -> b
                        .researchStack(ENERGY_INPUT_HATCH[ZPM].getStackForm())
                        .CWUt(64)
                        .EUt(VA[ZPM]))
                .duration(800).volts(VA[UV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(HULL[UHV])
                .inputItem(cableGtSingle, Europium, 4)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItem(circuit, Tier.UHV)
                .inputItem(wireGtDouble, RutheniumTriniumAmericiumNeutronate, 2)
                .fluidInputs(SodiumPotassium.getFluid(12000))
                .fluidInputs(SolderingAlloy.getFluid(5760))
                .outputItem(ENERGY_INPUT_HATCH[UHV])
                .stationResearch(b -> b
                        .researchStack(ENERGY_INPUT_HATCH[UV].getStackForm())
                        .CWUt(128)
                        .EUt(VA[UV]))
                .duration(1000).volts(VA[UHV]).buildAndRegister();

        // Power Transformers

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HI_AMP_TRANSFORMER[ULV])
                .inputItem(ELECTRIC_PUMP_LV)
                .inputItem(cableGtOctal, Tin)
                .inputItem(cableGtHex, Lead, 2)
                .inputItem(springSmall, Lead)
                .inputItem(spring, Tin)
                .fluidInputs(Lubricant.getFluid(2000))
                .outputItem(POWER_TRANSFORMER[ULV])
                .duration(200).volts(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HI_AMP_TRANSFORMER[LV])
                .inputItem(ELECTRIC_PUMP_LV)
                .inputItem(cableGtOctal, Copper)
                .inputItem(cableGtHex, Tin, 2)
                .inputItem(springSmall, Tin)
                .inputItem(spring, Copper)
                .fluidInputs(Lubricant.getFluid(2000))
                .outputItem(POWER_TRANSFORMER[LV])
                .duration(200).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HI_AMP_TRANSFORMER[MV])
                .inputItem(ELECTRIC_PUMP_MV)
                .inputItem(cableGtOctal, Gold)
                .inputItem(cableGtHex, Copper, 2)
                .inputItem(springSmall, Copper)
                .inputItem(spring, Gold)
                .fluidInputs(Lubricant.getFluid(2000))
                .outputItem(POWER_TRANSFORMER[MV])
                .duration(200).volts(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HI_AMP_TRANSFORMER[HV])
                .inputItem(ELECTRIC_PUMP_MV)
                .inputItem(cableGtOctal, Aluminium)
                .inputItem(cableGtHex, Gold, 2)
                .inputItem(springSmall, Gold)
                .inputItem(spring, Aluminium)
                .fluidInputs(Lubricant.getFluid(2000))
                .outputItem(POWER_TRANSFORMER[HV])
                .duration(200).volts(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HI_AMP_TRANSFORMER[EV])
                .inputItem(ELECTRIC_PUMP_HV)
                .inputItem(cableGtOctal, Tungsten)
                .inputItem(cableGtHex, Aluminium, 2)
                .inputItem(springSmall, Aluminium)
                .inputItem(spring, Tungsten)
                .fluidInputs(Lubricant.getFluid(2000))
                .outputItem(POWER_TRANSFORMER[EV])
                .duration(200).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HI_AMP_TRANSFORMER[IV])
                .inputItem(ELECTRIC_PUMP_HV)
                .inputItem(cableGtOctal, NiobiumTitanium)
                .inputItem(cableGtHex, Tungsten, 2)
                .inputItem(springSmall, Tungsten)
                .inputItem(spring, NiobiumTitanium)
                .fluidInputs(Lubricant.getFluid(2000))
                .outputItem(POWER_TRANSFORMER[IV])
                .duration(200).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HI_AMP_TRANSFORMER[LuV])
                .inputItem(ELECTRIC_PUMP_EV)
                .inputItem(cableGtOctal, VanadiumGallium)
                .inputItem(cableGtHex, NiobiumTitanium, 2)
                .inputItem(springSmall, NiobiumTitanium)
                .inputItem(spring, VanadiumGallium)
                .fluidInputs(Lubricant.getFluid(2000))
                .outputItem(POWER_TRANSFORMER[LuV])
                .duration(200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HI_AMP_TRANSFORMER[ZPM])
                .inputItem(ELECTRIC_PUMP_EV)
                .inputItem(cableGtOctal, YttriumBariumCuprate)
                .inputItem(cableGtHex, VanadiumGallium, 2)
                .inputItem(springSmall, VanadiumGallium)
                .inputItem(spring, YttriumBariumCuprate)
                .fluidInputs(Lubricant.getFluid(2000))
                .outputItem(POWER_TRANSFORMER[ZPM])
                .duration(200).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HI_AMP_TRANSFORMER[UV])
                .inputItem(ELECTRIC_PUMP_IV)
                .inputItem(cableGtOctal, Europium)
                .inputItem(cableGtHex, YttriumBariumCuprate, 2)
                .inputItem(springSmall, YttriumBariumCuprate)
                .inputItem(spring, Europium)
                .fluidInputs(Lubricant.getFluid(2000))
                .outputItem(POWER_TRANSFORMER[UV])
                .duration(200).volts(VA[UV]).buildAndRegister();

        // 4A Energy Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ENERGY_INPUT_HATCH[EV])
                .inputItem(wireGtQuadruple, Aluminium, 2)
                .inputItem(plate, Titanium, 2)
                .outputItem(ENERGY_INPUT_HATCH_4A[0])
                .duration(100).volts(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ENERGY_INPUT_HATCH[IV])
                .inputItem(wireGtQuadruple, Tungsten, 2)
                .inputItem(plate, TungstenSteel, 2)
                .outputItem(ENERGY_INPUT_HATCH_4A[1])
                .duration(100).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ENERGY_INPUT_HATCH[LuV])
                .inputItem(wireGtQuadruple, NiobiumTitanium, 2)
                .inputItem(plate, RhodiumPlatedPalladium, 2)
                .outputItem(ENERGY_INPUT_HATCH_4A[2])
                .duration(100).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ENERGY_INPUT_HATCH[ZPM])
                .inputItem(wireGtQuadruple, VanadiumGallium, 2)
                .inputItem(plate, NaquadahAlloy, 2)
                .outputItem(ENERGY_INPUT_HATCH_4A[3])
                .duration(100).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ENERGY_INPUT_HATCH[UV])
                .inputItem(wireGtQuadruple, YttriumBariumCuprate, 2)
                .inputItem(plate, Darmstadtium, 2)
                .outputItem(ENERGY_INPUT_HATCH_4A[4])
                .duration(100).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ENERGY_INPUT_HATCH[UHV])
                .inputItem(wireGtQuadruple, Europium, 2)
                .inputItem(plate, Neutronium, 2)
                .outputItem(ENERGY_INPUT_HATCH_4A[5])
                .duration(100).volts(VA[UV]).buildAndRegister();

        // 16A Energy Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(TRANSFORMER[IV])
                .inputItem(ENERGY_INPUT_HATCH_4A[1])
                .inputItem(wireGtOctal, Tungsten, 2)
                .inputItem(plate, TungstenSteel, 4)
                .outputItem(ENERGY_INPUT_HATCH_16A[0])
                .duration(200).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(TRANSFORMER[LuV])
                .inputItem(ENERGY_INPUT_HATCH_4A[2])
                .inputItem(wireGtOctal, NiobiumTitanium, 2)
                .inputItem(plate, RhodiumPlatedPalladium, 4)
                .outputItem(ENERGY_INPUT_HATCH_16A[1])
                .duration(200).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(TRANSFORMER[ZPM])
                .inputItem(ENERGY_INPUT_HATCH_4A[3])
                .inputItem(wireGtOctal, VanadiumGallium, 2)
                .inputItem(plate, NaquadahAlloy, 4)
                .outputItem(ENERGY_INPUT_HATCH_16A[2])
                .duration(200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(TRANSFORMER[UV])
                .inputItem(ENERGY_INPUT_HATCH_4A[4])
                .inputItem(wireGtOctal, YttriumBariumCuprate, 2)
                .inputItem(plate, Darmstadtium, 4)
                .outputItem(ENERGY_INPUT_HATCH_16A[3])
                .duration(200).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HI_AMP_TRANSFORMER[UV])
                .inputItem(ENERGY_INPUT_HATCH_4A[5], 2)
                .inputItem(wireGtOctal, Europium, 2)
                .inputItem(plate, Neutronium, 4)
                .outputItem(ENERGY_INPUT_HATCH_16A[4])
                .duration(200).volts(VA[UV]).buildAndRegister();

        // 64A Substation Energy Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(POWER_TRANSFORMER[IV])
                .inputItem(ENERGY_INPUT_HATCH_16A[0])
                .inputItem(wireGtHex, Tungsten, 2)
                .inputItem(plate, TungstenSteel, 6)
                .outputItem(SUBSTATION_ENERGY_INPUT_HATCH[0])
                .duration(400).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(POWER_TRANSFORMER[LuV])
                .inputItem(ENERGY_INPUT_HATCH_16A[1])
                .inputItem(wireGtHex, NiobiumTitanium, 2)
                .inputItem(plate, RhodiumPlatedPalladium, 6)
                .outputItem(SUBSTATION_ENERGY_INPUT_HATCH[1])
                .duration(400).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(POWER_TRANSFORMER[ZPM])
                .inputItem(ENERGY_INPUT_HATCH_16A[2])
                .inputItem(wireGtHex, VanadiumGallium, 2)
                .inputItem(plate, NaquadahAlloy, 6)
                .outputItem(SUBSTATION_ENERGY_INPUT_HATCH[2])
                .duration(400).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(POWER_TRANSFORMER[UV])
                .inputItem(ENERGY_INPUT_HATCH_16A[3])
                .inputItem(wireGtHex, YttriumBariumCuprate, 2)
                .inputItem(plate, Darmstadtium, 6)
                .outputItem(SUBSTATION_ENERGY_INPUT_HATCH[3])
                .duration(400).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(POWER_TRANSFORMER[UV])
                .inputItem(ENERGY_INPUT_HATCH_16A[4])
                .inputItem(wireGtHex, Europium, 2)
                .inputItem(plate, Neutronium, 6)
                .outputItem(SUBSTATION_ENERGY_INPUT_HATCH[4])
                .duration(400).volts(VA[UV]).buildAndRegister();

        // 4A Dynamo Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ENERGY_OUTPUT_HATCH[EV])
                .inputItem(wireGtQuadruple, Aluminium, 2)
                .inputItem(plate, Titanium, 2)
                .outputItem(ENERGY_OUTPUT_HATCH_4A[0])
                .duration(100).volts(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ENERGY_OUTPUT_HATCH[IV])
                .inputItem(wireGtQuadruple, Tungsten, 2)
                .inputItem(plate, TungstenSteel, 2)
                .outputItem(ENERGY_OUTPUT_HATCH_4A[1])
                .duration(100).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ENERGY_OUTPUT_HATCH[LuV])
                .inputItem(wireGtQuadruple, NiobiumTitanium, 2)
                .inputItem(plate, RhodiumPlatedPalladium, 2)
                .outputItem(ENERGY_OUTPUT_HATCH_4A[2])
                .duration(100).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(TRANSFORMER[ZPM])
                .inputItem(ENERGY_OUTPUT_HATCH[ZPM])
                .inputItem(wireGtQuadruple, VanadiumGallium, 2)
                .inputItem(plate, NaquadahAlloy, 2)
                .outputItem(ENERGY_OUTPUT_HATCH_4A[3])
                .duration(100).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ENERGY_OUTPUT_HATCH[UV])
                .inputItem(wireGtQuadruple, YttriumBariumCuprate, 2)
                .inputItem(plate, Darmstadtium, 2)
                .outputItem(ENERGY_OUTPUT_HATCH_4A[4])
                .duration(100).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(ENERGY_OUTPUT_HATCH[UHV])
                .inputItem(wireGtQuadruple, Europium, 2)
                .inputItem(plate, Neutronium, 2)
                .outputItem(ENERGY_OUTPUT_HATCH_4A[5])
                .duration(100).volts(VA[UV]).buildAndRegister();

        // 16A Dynamo Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(TRANSFORMER[IV])
                .inputItem(ENERGY_OUTPUT_HATCH_4A[1])
                .inputItem(wireGtOctal, Tungsten, 2)
                .inputItem(plate, TungstenSteel, 4)
                .outputItem(ENERGY_OUTPUT_HATCH_16A[0])
                .duration(200).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(TRANSFORMER[LuV])
                .inputItem(ENERGY_OUTPUT_HATCH_4A[2])
                .inputItem(wireGtOctal, NiobiumTitanium, 2)
                .inputItem(plate, RhodiumPlatedPalladium, 4)
                .outputItem(ENERGY_OUTPUT_HATCH_16A[1])
                .duration(200).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(TRANSFORMER[ZPM])
                .inputItem(ENERGY_OUTPUT_HATCH_4A[3])
                .inputItem(wireGtOctal, VanadiumGallium, 2)
                .inputItem(plate, NaquadahAlloy, 4)
                .outputItem(ENERGY_OUTPUT_HATCH_16A[2])
                .duration(200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(TRANSFORMER[UV])
                .inputItem(ENERGY_OUTPUT_HATCH_4A[4])
                .inputItem(wireGtOctal, YttriumBariumCuprate, 2)
                .inputItem(plate, Darmstadtium, 4)
                .outputItem(ENERGY_OUTPUT_HATCH_16A[3])
                .duration(200).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HI_AMP_TRANSFORMER[UV])
                .inputItem(ENERGY_OUTPUT_HATCH_4A[5])
                .inputItem(wireGtOctal, Europium, 2)
                .inputItem(plate, Neutronium, 4)
                .outputItem(ENERGY_OUTPUT_HATCH_16A[4])
                .duration(200).volts(VA[UV]).buildAndRegister();

        // 64A Substation Dynamo Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(POWER_TRANSFORMER[IV])
                .inputItem(ENERGY_OUTPUT_HATCH_16A[0])
                .inputItem(wireGtHex, Tungsten, 2)
                .inputItem(plate, TungstenSteel, 6)
                .outputItem(SUBSTATION_ENERGY_OUTPUT_HATCH[0])
                .duration(400).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(POWER_TRANSFORMER[LuV])
                .inputItem(ENERGY_OUTPUT_HATCH_16A[1])
                .inputItem(wireGtHex, NiobiumTitanium, 2)
                .inputItem(plate, RhodiumPlatedPalladium, 6)
                .outputItem(SUBSTATION_ENERGY_OUTPUT_HATCH[1])
                .duration(400).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(POWER_TRANSFORMER[ZPM])
                .inputItem(ENERGY_OUTPUT_HATCH_16A[2])
                .inputItem(wireGtHex, VanadiumGallium, 2)
                .inputItem(plate, NaquadahAlloy, 6)
                .outputItem(SUBSTATION_ENERGY_OUTPUT_HATCH[2])
                .duration(400).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(POWER_TRANSFORMER[UV])
                .inputItem(ENERGY_OUTPUT_HATCH_16A[3])
                .inputItem(wireGtHex, YttriumBariumCuprate, 2)
                .inputItem(plate, Darmstadtium, 6)
                .outputItem(SUBSTATION_ENERGY_OUTPUT_HATCH[3])
                .duration(400).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(POWER_TRANSFORMER[UV])
                .inputItem(ENERGY_OUTPUT_HATCH_16A[4])
                .inputItem(wireGtHex, Europium, 2)
                .inputItem(plate, Neutronium, 6)
                .outputItem(SUBSTATION_ENERGY_OUTPUT_HATCH[4])
                .duration(400).volts(VA[UV]).buildAndRegister();

        // Maintenance Hatch

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[LV])
                .circuitMeta(8)
                .outputItem(MAINTENANCE_HATCH)
                .duration(100).volts(VA[LV]).buildAndRegister();

        // Multiblock Miners

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[EV])
                .inputItem(frameGt, Titanium, 4)
                .inputItem(circuit, Tier.EV, 4)
                .inputItem(ELECTRIC_MOTOR_EV, 4)
                .inputItem(ELECTRIC_PUMP_EV, 4)
                .inputItem(CONVEYOR_MODULE_EV, 4)
                .inputItem(gear, Tungsten, 4)
                .circuitMeta(2)
                .outputItem(BASIC_LARGE_MINER)
                .duration(400).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[IV])
                .inputItem(frameGt, TungstenSteel, 4)
                .inputItem(circuit, Tier.IV, 4)
                .inputItem(ELECTRIC_MOTOR_IV, 4)
                .inputItem(ELECTRIC_PUMP_IV, 4)
                .inputItem(CONVEYOR_MODULE_IV, 4)
                .inputItem(gear, Iridium, 4)
                .circuitMeta(2)
                .outputItem(LARGE_MINER)
                .duration(400).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[LuV])
                .inputItem(frameGt, HSSS, 4)
                .inputItem(circuit, Tier.LuV, 4)
                .inputItem(ELECTRIC_MOTOR_LuV, 4)
                .inputItem(ELECTRIC_PUMP_LuV, 4)
                .inputItem(CONVEYOR_MODULE_LuV, 4)
                .inputItem(gear, Ruridit, 4)
                .circuitMeta(2)
                .outputItem(ADVANCED_LARGE_MINER)
                .duration(400).volts(VA[LuV]).buildAndRegister();

        // Multiblock Fluid Drills

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[MV])
                .inputItem(frameGt, Steel, 4)
                .inputItem(circuit, Tier.MV, 4)
                .inputItem(ELECTRIC_MOTOR_MV, 4)
                .inputItem(ELECTRIC_PUMP_MV, 4)
                .inputItem(gear, VanadiumSteel, 4)
                .circuitMeta(2)
                .outputItem(BASIC_FLUID_DRILLING_RIG)
                .duration(400).volts(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[EV])
                .inputItem(frameGt, Titanium, 4)
                .inputItem(circuit, Tier.EV, 4)
                .inputItem(ELECTRIC_MOTOR_EV, 4)
                .inputItem(ELECTRIC_PUMP_EV, 4)
                .inputItem(gear, TungstenCarbide, 4)
                .circuitMeta(2)
                .outputItem(FLUID_DRILLING_RIG)
                .duration(400).volts(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[LuV])
                .inputItem(frameGt, TungstenSteel, 4)
                .inputItem(circuit, Tier.LuV, 4)
                .inputItem(ELECTRIC_MOTOR_LuV, 4)
                .inputItem(ELECTRIC_PUMP_LuV, 4)
                .inputItem(gear, Osmiridium, 4)
                .circuitMeta(2)
                .outputItem(ADVANCED_FLUID_DRILLING_RIG)
                .duration(400).volts(VA[LuV]).buildAndRegister();

        // Long Distance Pipes
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(pipeLargeItem, Tin, 2)
                .inputItem(plate, Steel, 8)
                .inputItem(gear, Steel, 2)
                .circuitMeta(1)
                .fluidInputs(SolderingAlloy.getFluid(L / 2))
                .outputItem(LONG_DIST_ITEM_ENDPOINT, 2)
                .duration(400).volts(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(pipeLargeFluid, Bronze, 2)
                .inputItem(plate, Steel, 8)
                .inputItem(gear, Steel, 2)
                .circuitMeta(1)
                .fluidInputs(SolderingAlloy.getFluid(L / 2))
                .outputItem(LONG_DIST_FLUID_ENDPOINT, 2)
                .duration(400).volts(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(pipeLargeItem, Tin, 2)
                .inputItem(plate, Steel, 8)
                .circuitMeta(2)
                .fluidInputs(SolderingAlloy.getFluid(L / 2))
                .outputItem(LD_ITEM_PIPE, 64)
                .duration(600).volts(24).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(pipeLargeFluid, Bronze, 2)
                .inputItem(plate, Steel, 8)
                .circuitMeta(2)
                .fluidInputs(SolderingAlloy.getFluid(L / 2))
                .outputItem(LD_FLUID_PIPE, 64)
                .duration(600).volts(24).buildAndRegister();

        // ME Parts

        if (Mods.AppliedEnergistics2.isModLoaded()) {

            ItemStack fluidInterface = Mods.AppliedEnergistics2.getItem("fluid_interface");
            ItemStack normalInterface = Mods.AppliedEnergistics2.getItem("interface");
            ItemStack accelerationCard = Mods.AppliedEnergistics2.getItem("material", 30, 2);

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(FLUID_EXPORT_HATCH[EV])
                    .inputs(fluidInterface.copy())
                    .inputs(accelerationCard.copy())
                    .outputItem(FLUID_EXPORT_HATCH_ME)
                    .duration(300).volts(VA[HV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(FLUID_IMPORT_HATCH[EV])
                    .inputs(fluidInterface.copy())
                    .inputs(accelerationCard.copy())
                    .outputItem(FLUID_IMPORT_HATCH_ME)
                    .duration(300).volts(VA[HV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(ITEM_EXPORT_BUS[EV])
                    .inputs(normalInterface.copy())
                    .inputs(accelerationCard.copy())
                    .outputItem(ITEM_EXPORT_BUS_ME)
                    .duration(300).volts(VA[HV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(ITEM_IMPORT_BUS[EV])
                    .inputs(normalInterface.copy())
                    .inputs(accelerationCard.copy())
                    .outputItem(ITEM_IMPORT_BUS_ME)
                    .duration(300).volts(VA[HV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(ITEM_IMPORT_BUS[IV])
                    .inputs(normalInterface.copy())
                    .inputItem(CONVEYOR_MODULE_IV)
                    .inputItem(SENSOR_IV)
                    .inputs(GTUtility.copy(4, accelerationCard))
                    .outputItem(STOCKING_BUS_ME)
                    .duration(300).volts(VA[IV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(FLUID_IMPORT_HATCH[IV])
                    .inputs(fluidInterface.copy())
                    .inputItem(ELECTRIC_PUMP_IV)
                    .inputItem(SENSOR_IV)
                    .inputs(GTUtility.copy(4, accelerationCard))
                    .outputItem(STOCKING_HATCH_ME)
                    .duration(300).volts(VA[IV]).buildAndRegister();
        }
    }

    private static void registerHatchBusRecipe(int tier, MetaTileEntity inputBus, MetaTileEntity outputBus,
                                               Object extraInput) {
        GTItemIngredient extra;
        if (extraInput instanceof ItemStack stack) {
            extra = StandardItemIngredient.builder().setCount(stack.getCount()).addStack(stack)
                    .clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT);
        } else if (extraInput instanceof String oreName) {
            extra = OreItemIngredient.of(oreName);
        } else {
            throw new IllegalArgumentException("extraInput must be ItemStack or String (for oredict)");
        }

        // Glue recipe for ULV and LV
        // 250L for ULV, 500L for LV
        if (tier <= GTValues.LV) {
            int fluidAmount = tier == GTValues.ULV ? 250 : 500;
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(HULL[tier])
                    .ingredient(extra)
                    .fluidInputs(Glue.getFluid(fluidAmount))
                    .circuitMeta(1)
                    .outputItem(inputBus)
                    .duration(300).volts(VA[tier]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(HULL[tier])
                    .ingredient(extra)
                    .fluidInputs(Glue.getFluid(fluidAmount))
                    .circuitMeta(2)
                    .outputItem(outputBus)
                    .duration(300).volts(VA[tier]).buildAndRegister();
        }

        // Polyethylene recipe for HV and below
        // 72L for ULV, 144L for LV, 288L for MV, 432L for HV
        if (tier <= GTValues.HV) {
            int peAmount = getFluidAmount(tier + 4);
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(HULL[tier])
                    .ingredient(extra)
                    .fluidInputs(Polyethylene.getFluid(peAmount))
                    .circuitMeta(1)
                    .outputItem(inputBus)
                    .duration(300).volts(VA[tier]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(HULL[tier])
                    .ingredient(extra)
                    .fluidInputs(Polyethylene.getFluid(peAmount))
                    .circuitMeta(2)
                    .outputItem(outputBus)
                    .duration(300).volts(VA[tier]).buildAndRegister();
        }

        // Polytetrafluoroethylene recipe for LuV and below
        // 36L for ULV, 72L for LV, 144L for MV, 288L for HV, 432L for EV, 576L for IV, 720L for LuV
        if (tier <= GTValues.LuV) {
            int ptfeAmount = getFluidAmount(tier + 3);
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(HULL[tier])
                    .ingredient(extra)
                    .fluidInputs(Polytetrafluoroethylene.getFluid(ptfeAmount))
                    .circuitMeta(1)
                    .outputItem(inputBus)
                    .duration(300).volts(VA[tier]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(HULL[tier])
                    .ingredient(extra)
                    .fluidInputs(Polytetrafluoroethylene.getFluid(ptfeAmount))
                    .circuitMeta(2)
                    .outputItem(outputBus)
                    .duration(300).volts(VA[tier]).buildAndRegister();
        }

        // PBI recipe for all
        // 4L for ULV, 9L for LV, 18L for MV, 36L for HV, 72L for EV, 144L for IV,
        // 288L for LuV, 432L for ZPM, 576L for UV, 720L for UHV
        // Use a Math.min() call on tier so that UHV hatches are still UV voltage
        int pbiAmount = getFluidAmount(tier);
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[tier])
                .ingredient(extra)
                .fluidInputs(Polybenzimidazole.getFluid(pbiAmount))
                .circuitMeta(1)
                .outputItem(inputBus)
                .withRecycling()
                .duration(300).volts(VA[Math.min(GTValues.UV, tier)]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[tier])
                .ingredient(extra)
                .fluidInputs(Polybenzimidazole.getFluid(pbiAmount))
                .circuitMeta(2)
                .outputItem(outputBus)
                .withRecycling()
                .duration(300).volts(VA[Math.min(GTValues.UV, tier)]).buildAndRegister();
    }

    private static int getFluidAmount(int offsetTier) {
        return switch (offsetTier) {
            case 0 -> 4;
            case 1 -> 9;
            case 2 -> 18;
            case 3 -> 36;
            case 4 -> 72;
            case 5 -> 144;
            case 6 -> 288;
            case 7 -> 432;
            case 8 -> 576;
            default -> 720;
        };
    }

    // TODO clean this up with a CraftingComponent rework
    private static void registerLaserRecipes() {
        // 256A Laser Source Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[IV])
                .inputItem(lens, Diamond)
                .inputItem(EMITTER_IV)
                .inputItem(ELECTRIC_PUMP_IV)
                .inputItem(cableGtSingle, Platinum, 4)
                .circuitMeta(1)
                .outputItem(LASER_OUTPUT_HATCH_256[0])
                .duration(300).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[LuV])
                .inputItem(lens, Diamond)
                .inputItem(EMITTER_LuV)
                .inputItem(ELECTRIC_PUMP_LuV)
                .inputItem(cableGtSingle, NiobiumTitanium, 4)
                .circuitMeta(1)
                .outputItem(LASER_OUTPUT_HATCH_256[1])
                .duration(300).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[ZPM])
                .inputItem(lens, Diamond)
                .inputItem(EMITTER_ZPM)
                .inputItem(ELECTRIC_PUMP_ZPM)
                .inputItem(cableGtSingle, VanadiumGallium, 4)
                .circuitMeta(1)
                .outputItem(LASER_OUTPUT_HATCH_256[2])
                .duration(300).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[UV])
                .inputItem(lens, Diamond)
                .inputItem(EMITTER_UV)
                .inputItem(ELECTRIC_PUMP_UV)
                .inputItem(cableGtSingle, YttriumBariumCuprate, 4)
                .circuitMeta(1)
                .outputItem(LASER_OUTPUT_HATCH_256[3])
                .duration(300).volts(VA[UV]).buildAndRegister();

        // 256A Laser Target Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[IV])
                .inputItem(lens, Diamond)
                .inputItem(SENSOR_IV)
                .inputItem(ELECTRIC_PUMP_IV)
                .inputItem(cableGtSingle, Platinum, 4)
                .circuitMeta(1)
                .outputItem(LASER_INPUT_HATCH_256[0])
                .duration(300).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[LuV])
                .inputItem(lens, Diamond)
                .inputItem(SENSOR_LuV)
                .inputItem(ELECTRIC_PUMP_LuV)
                .inputItem(cableGtSingle, NiobiumTitanium, 4)
                .circuitMeta(1)
                .outputItem(LASER_INPUT_HATCH_256[1])
                .duration(300).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[ZPM])
                .inputItem(lens, Diamond)
                .inputItem(SENSOR_ZPM)
                .inputItem(ELECTRIC_PUMP_ZPM)
                .inputItem(cableGtSingle, VanadiumGallium, 4)
                .circuitMeta(1)
                .outputItem(LASER_INPUT_HATCH_256[2])
                .duration(300).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[UV])
                .inputItem(lens, Diamond)
                .inputItem(SENSOR_UV)
                .inputItem(ELECTRIC_PUMP_UV)
                .inputItem(cableGtSingle, YttriumBariumCuprate, 4)
                .circuitMeta(1)
                .outputItem(LASER_INPUT_HATCH_256[3])
                .duration(300).volts(VA[UV]).buildAndRegister();

        // 1024A Laser Source Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[IV])
                .inputItem(lens, Diamond, 2)
                .inputItem(EMITTER_IV, 2)
                .inputItem(ELECTRIC_PUMP_IV, 2)
                .inputItem(cableGtDouble, Platinum, 4)
                .circuitMeta(2)
                .outputItem(LASER_OUTPUT_HATCH_1024[0])
                .duration(600).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[LuV])
                .inputItem(lens, Diamond, 2)
                .inputItem(EMITTER_LuV, 2)
                .inputItem(ELECTRIC_PUMP_LuV, 2)
                .inputItem(cableGtDouble, NiobiumTitanium, 4)
                .circuitMeta(2)
                .outputItem(LASER_OUTPUT_HATCH_1024[1])
                .duration(600).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[ZPM])
                .inputItem(lens, Diamond, 2)
                .inputItem(EMITTER_ZPM, 2)
                .inputItem(ELECTRIC_PUMP_ZPM, 2)
                .inputItem(cableGtDouble, VanadiumGallium, 4)
                .circuitMeta(2)
                .outputItem(LASER_OUTPUT_HATCH_1024[2])
                .duration(600).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[UV])
                .inputItem(lens, Diamond, 2)
                .inputItem(EMITTER_UV, 2)
                .inputItem(ELECTRIC_PUMP_UV, 2)
                .inputItem(cableGtDouble, YttriumBariumCuprate, 4)
                .circuitMeta(2)
                .outputItem(LASER_OUTPUT_HATCH_1024[3])
                .duration(600).volts(VA[UV]).buildAndRegister();

        // 1024A Laser Target Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[IV])
                .inputItem(lens, Diamond, 2)
                .inputItem(SENSOR_IV, 2)
                .inputItem(ELECTRIC_PUMP_IV, 2)
                .inputItem(cableGtDouble, Platinum, 4)
                .circuitMeta(2)
                .outputItem(LASER_INPUT_HATCH_1024[0])
                .duration(600).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[LuV])
                .inputItem(lens, Diamond, 2)
                .inputItem(SENSOR_LuV, 2)
                .inputItem(ELECTRIC_PUMP_LuV, 2)
                .inputItem(cableGtDouble, NiobiumTitanium, 4)
                .circuitMeta(2)
                .outputItem(LASER_INPUT_HATCH_1024[1])
                .duration(600).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[ZPM])
                .inputItem(lens, Diamond, 2)
                .inputItem(SENSOR_ZPM, 2)
                .inputItem(ELECTRIC_PUMP_ZPM, 2)
                .inputItem(cableGtDouble, VanadiumGallium, 4)
                .circuitMeta(2)
                .outputItem(LASER_INPUT_HATCH_1024[2])
                .duration(600).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[UV])
                .inputItem(lens, Diamond, 2)
                .inputItem(SENSOR_UV, 2)
                .inputItem(ELECTRIC_PUMP_UV, 2)
                .inputItem(cableGtDouble, YttriumBariumCuprate, 4)
                .circuitMeta(2)
                .outputItem(LASER_INPUT_HATCH_1024[3])
                .duration(600).volts(VA[UV]).buildAndRegister();

        // 4096A Laser Source Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[IV])
                .inputItem(lens, Diamond, 4)
                .inputItem(EMITTER_IV, 4)
                .inputItem(ELECTRIC_PUMP_IV, 4)
                .inputItem(cableGtQuadruple, Platinum, 4)
                .circuitMeta(3)
                .outputItem(LASER_OUTPUT_HATCH_4096[0])
                .duration(1200).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[LuV])
                .inputItem(lens, Diamond, 4)
                .inputItem(EMITTER_LuV, 4)
                .inputItem(ELECTRIC_PUMP_LuV, 4)
                .inputItem(cableGtQuadruple, NiobiumTitanium, 4)
                .circuitMeta(3)
                .outputItem(LASER_OUTPUT_HATCH_4096[1])
                .duration(1200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[ZPM])
                .inputItem(lens, Diamond, 4)
                .inputItem(EMITTER_ZPM, 4)
                .inputItem(ELECTRIC_PUMP_ZPM, 4)
                .inputItem(cableGtQuadruple, VanadiumGallium, 4)
                .circuitMeta(3)
                .outputItem(LASER_OUTPUT_HATCH_4096[2])
                .duration(1200).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[UV])
                .inputItem(lens, Diamond, 4)
                .inputItem(EMITTER_UV, 4)
                .inputItem(ELECTRIC_PUMP_UV, 4)
                .inputItem(cableGtQuadruple, YttriumBariumCuprate, 4)
                .circuitMeta(3)
                .outputItem(LASER_OUTPUT_HATCH_4096[3])
                .duration(1200).volts(VA[UV]).buildAndRegister();

        // 4096A Laser Target Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[IV])
                .inputItem(lens, Diamond, 4)
                .inputItem(SENSOR_IV, 4)
                .inputItem(ELECTRIC_PUMP_IV, 4)
                .inputItem(cableGtQuadruple, Platinum, 4)
                .circuitMeta(3)
                .outputItem(LASER_INPUT_HATCH_4096[0])
                .duration(1200).volts(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[LuV])
                .inputItem(lens, Diamond, 4)
                .inputItem(SENSOR_LuV, 4)
                .inputItem(ELECTRIC_PUMP_LuV, 4)
                .inputItem(cableGtQuadruple, NiobiumTitanium, 4)
                .circuitMeta(3)
                .outputItem(LASER_INPUT_HATCH_4096[1])
                .duration(1200).volts(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[ZPM])
                .inputItem(lens, Diamond, 4)
                .inputItem(SENSOR_ZPM, 4)
                .inputItem(ELECTRIC_PUMP_ZPM, 4)
                .inputItem(cableGtQuadruple, VanadiumGallium, 4)
                .circuitMeta(3)
                .outputItem(LASER_INPUT_HATCH_4096[2])
                .duration(1200).volts(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(HULL[UV])
                .inputItem(lens, Diamond, 4)
                .inputItem(SENSOR_UV, 4)
                .inputItem(ELECTRIC_PUMP_UV, 4)
                .inputItem(cableGtQuadruple, YttriumBariumCuprate, 4)
                .circuitMeta(3)
                .outputItem(LASER_INPUT_HATCH_4096[3])
                .duration(1200).volts(VA[UV]).buildAndRegister();
    }
}
