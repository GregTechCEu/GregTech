package gregtech.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.stack.UnificationEntry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.MarkerMaterials.Tier;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;

public class MetaTileEntityMachineRecipeLoader {

    public static void init() {

        // Energy Output Hatches

        ModHandler.addShapedRecipe(true, "dynamo_hatch.ulv", ENERGY_OUTPUT_HATCH[ULV].getStackForm(),
                " V ", "SHS", "   ",
                'S', new UnificationEntry(spring, Lead),
                'V', VOLTAGE_COIL_ULV.getStackForm(),
                'H', HULL[ULV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[ULV])
                .input(spring, Lead, 2)
                .input(VOLTAGE_COIL_ULV)
                .output(ENERGY_OUTPUT_HATCH[ULV])
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "dynamo_hatch.lv", ENERGY_OUTPUT_HATCH[LV].getStackForm(),
                " V ", "SHS", "   ",
                'S', new UnificationEntry(spring, Tin),
                'V', VOLTAGE_COIL_LV.getStackForm(),
                'H', HULL[LV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LV])
                .input(spring, Tin, 2)
                .input(VOLTAGE_COIL_LV)
                .output(ENERGY_OUTPUT_HATCH[LV])
                .duration(200).EUt(VA[LV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "dynamo_hatch.mv", ENERGY_OUTPUT_HATCH[MV].getStackForm(),
                " V ", "SHS", " P ",
                'P', ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(),
                'S', new UnificationEntry(spring, Copper),
                'V', VOLTAGE_COIL_MV.getStackForm(),
                'H', HULL[MV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[MV])
                .input(spring, Copper, 2)
                .input(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_MV)
                .output(ENERGY_OUTPUT_HATCH[MV])
                .duration(200).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[HV])
                .input(spring, Gold, 2)
                .input(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_HV)
                .fluidInputs(SodiumPotassium.getFluid(1000))
                .output(ENERGY_OUTPUT_HATCH[HV])
                .duration(200).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[EV])
                .input(spring, Aluminium, 2)
                .input(POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_EV)
                .fluidInputs(SodiumPotassium.getFluid(2000))
                .output(ENERGY_OUTPUT_HATCH[EV])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(spring, Tungsten, 2)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_IV)
                .fluidInputs(SodiumPotassium.getFluid(3000))
                .output(ENERGY_OUTPUT_HATCH[IV])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(spring, NiobiumTitanium, 4)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.LuV)
                .input(VOLTAGE_COIL_LuV, 2)
                .fluidInputs(SodiumPotassium.getFluid(6000))
                .fluidInputs(SolderingAlloy.getFluid(720))
                .output(ENERGY_OUTPUT_HATCH[LuV])
                .duration(400).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[ZPM])
                .input(spring, VanadiumGallium, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.ZPM)
                .input(VOLTAGE_COIL_ZPM, 2)
                .fluidInputs(SodiumPotassium.getFluid(8000))
                .fluidInputs(SolderingAlloy.getFluid(1440))
                .output(ENERGY_OUTPUT_HATCH[ZPM])
                .duration(600).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[UV])
                .input(spring, YttriumBariumCuprate, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.UV)
                .input(VOLTAGE_COIL_UV, 2)
                .fluidInputs(SodiumPotassium.getFluid(10000))
                .fluidInputs(SolderingAlloy.getFluid(2880))
                .output(ENERGY_OUTPUT_HATCH[UV])
                .duration(800).EUt(VA[UV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[UHV])
                .input(spring, Europium, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.UHV)
                .input(wireGtDouble, RutheniumTriniumAmericiumNeutronate, 2)
                .fluidInputs(SodiumPotassium.getFluid(12000))
                .fluidInputs(SolderingAlloy.getFluid(5760))
                .output(ENERGY_OUTPUT_HATCH[UHV])
                .duration(1000).EUt(VA[UHV]).buildAndRegister();

        // Energy Input Hatches

        ModHandler.addShapedRecipe(true, "energy_hatch.ulv", ENERGY_INPUT_HATCH[ULV].getStackForm(),
                " V ", "CHC", "   ",
                'C', new UnificationEntry(cableGtSingle, RedAlloy),
                'V', VOLTAGE_COIL_ULV.getStackForm(),
                'H', HULL[ULV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[ULV])
                .input(cableGtSingle, RedAlloy, 2)
                .input(VOLTAGE_COIL_ULV)
                .output(ENERGY_INPUT_HATCH[ULV])
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "energy_hatch.lv", ENERGY_INPUT_HATCH[LV].getStackForm(),
                " V ", "CHC", "   ",
                'C', new UnificationEntry(cableGtSingle, Tin),
                'V', VOLTAGE_COIL_LV.getStackForm(),
                'H', HULL[LV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LV])
                .input(cableGtSingle, Tin, 2)
                .input(VOLTAGE_COIL_LV)
                .output(ENERGY_INPUT_HATCH[LV])
                .duration(200).EUt(VA[LV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "energy_hatch.mv", ENERGY_INPUT_HATCH[MV].getStackForm(),
                " V ", "CHC", " P ",
                'C', new UnificationEntry(cableGtSingle, Copper),
                'P', ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(),
                'V', VOLTAGE_COIL_MV.getStackForm(),
                'H', HULL[MV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[MV])
                .input(cableGtSingle, Copper, 2)
                .input(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_MV)
                .output(ENERGY_INPUT_HATCH[MV])
                .duration(200).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[HV])
                .input(cableGtSingle, Gold, 2)
                .input(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_HV)
                .fluidInputs(SodiumPotassium.getFluid(1000))
                .output(ENERGY_INPUT_HATCH[HV])
                .duration(200).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[EV])
                .input(cableGtSingle, Aluminium, 2)
                .input(POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_EV)
                .fluidInputs(SodiumPotassium.getFluid(2000))
                .output(ENERGY_INPUT_HATCH[EV])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(cableGtSingle, Tungsten, 2)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_IV)
                .fluidInputs(SodiumPotassium.getFluid(3000))
                .output(ENERGY_INPUT_HATCH[IV])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(cableGtSingle, NiobiumTitanium, 4)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.LuV)
                .input(VOLTAGE_COIL_LuV, 2)
                .fluidInputs(SodiumPotassium.getFluid(6000))
                .fluidInputs(SolderingAlloy.getFluid(720))
                .output(ENERGY_INPUT_HATCH[LuV])
                .duration(400).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[ZPM])
                .input(cableGtSingle, VanadiumGallium, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.ZPM)
                .input(VOLTAGE_COIL_ZPM, 2)
                .fluidInputs(SodiumPotassium.getFluid(8000))
                .fluidInputs(SolderingAlloy.getFluid(1440))
                .output(ENERGY_INPUT_HATCH[ZPM])
                .duration(600).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[UV])
                .input(cableGtSingle, YttriumBariumCuprate, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.UV)
                .input(VOLTAGE_COIL_UV, 2)
                .fluidInputs(SodiumPotassium.getFluid(10000))
                .fluidInputs(SolderingAlloy.getFluid(2880))
                .output(ENERGY_INPUT_HATCH[UV])
                .duration(800).EUt(VA[UV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[UHV])
                .input(cableGtSingle, Europium, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.UHV)
                .input(wireGtDouble, RutheniumTriniumAmericiumNeutronate, 2)
                .fluidInputs(SodiumPotassium.getFluid(12000))
                .fluidInputs(SolderingAlloy.getFluid(5760))
                .output(ENERGY_INPUT_HATCH[UHV])
                .duration(1000).EUt(VA[UHV]).buildAndRegister();


        // Adjustable Transformers

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[ULV])
                .input(ELECTRIC_PUMP_LV)
                .input(wireGtQuadruple, Tin)
                .input(wireGtOctal, Lead)
                .input(springSmall, Lead)
                .input(spring, Tin)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[ULV])
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[LV])
                .input(ELECTRIC_PUMP_LV)
                .input(wireGtQuadruple, Copper)
                .input(wireGtOctal, Tin)
                .input(springSmall, Tin)
                .input(spring, Copper)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[LV])
                .duration(200).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[MV])
                .input(ELECTRIC_PUMP_MV)
                .input(wireGtQuadruple, Gold)
                .input(wireGtOctal, Copper)
                .input(springSmall, Copper)
                .input(spring, Gold)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[MV])
                .duration(200).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[HV])
                .input(ELECTRIC_PUMP_MV)
                .input(wireGtQuadruple, Aluminium)
                .input(wireGtOctal, Gold)
                .input(springSmall, Gold)
                .input(spring, Aluminium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[HV])
                .duration(200).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[EV])
                .input(ELECTRIC_PUMP_HV)
                .input(wireGtQuadruple, Tungsten)
                .input(wireGtOctal, Aluminium)
                .input(springSmall, Aluminium)
                .input(spring, Tungsten)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[EV])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[IV])
                .input(ELECTRIC_PUMP_HV)
                .input(wireGtQuadruple, NiobiumTitanium)
                .input(wireGtOctal, Tungsten)
                .input(springSmall, Tungsten)
                .input(spring, NiobiumTitanium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[IV])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[LuV])
                .input(ELECTRIC_PUMP_EV)
                .input(wireGtQuadruple, VanadiumGallium)
                .input(wireGtOctal, NiobiumTitanium)
                .input(springSmall, NiobiumTitanium)
                .input(spring, VanadiumGallium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[LuV])
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[ZPM])
                .input(ELECTRIC_PUMP_EV)
                .input(wireGtQuadruple, YttriumBariumCuprate)
                .input(wireGtOctal, VanadiumGallium)
                .input(springSmall, VanadiumGallium)
                .input(spring, YttriumBariumCuprate)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[ZPM])
                .duration(200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[UV])
                .input(ELECTRIC_PUMP_IV)
                .input(wireGtQuadruple, Europium)
                .input(wireGtOctal, YttriumBariumCuprate)
                .input(springSmall, YttriumBariumCuprate)
                .input(spring, Europium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[UV])
                .duration(200).EUt(VA[UV]).buildAndRegister();

        // 4A Energy Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[EV])
                .input(ENERGY_INPUT_HATCH[EV])
                .input(POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_EV)
                .input(wireGtQuadruple, Aluminium, 2)
                .output(ENERGY_INPUT_HATCH_4A[0])
                .duration(100).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[IV])
                .input(ENERGY_INPUT_HATCH[IV])
                .input(HIGH_POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_IV)
                .input(wireGtQuadruple, Tungsten, 2)
                .output(ENERGY_INPUT_HATCH_4A[1])
                .duration(100).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[LuV])
                .input(ENERGY_INPUT_HATCH[LuV])
                .input(HIGH_POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_LuV)
                .input(wireGtQuadruple, NiobiumTitanium, 2)
                .output(ENERGY_INPUT_HATCH_4A[2])
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[ZPM])
                .input(ENERGY_INPUT_HATCH[ZPM])
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_ZPM)
                .input(wireGtQuadruple, VanadiumGallium, 2)
                .output(ENERGY_INPUT_HATCH_4A[3])
                .duration(100).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[UV])
                .input(ENERGY_INPUT_HATCH[UV])
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_UV)
                .input(wireGtQuadruple, YttriumBariumCuprate, 2)
                .output(ENERGY_INPUT_HATCH_4A[4])
                .duration(100).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH[UHV], 2)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .input(wireGtDouble, RutheniumTriniumAmericiumNeutronate)
                .input(wireGtQuadruple, Europium, 2)
                .output(ENERGY_INPUT_HATCH_4A[5])
                .duration(100).EUt(VA[UV]).buildAndRegister();

        // 16A Energy Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ADJUSTABLE_TRANSFORMER[IV])
                .input(ENERGY_INPUT_HATCH_4A[1])
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_IV)
                .input(wireGtOctal, Tungsten, 2)
                .output(ENERGY_INPUT_HATCH_16A[0])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ADJUSTABLE_TRANSFORMER[LuV])
                .input(ENERGY_INPUT_HATCH_4A[2])
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_LuV)
                .input(wireGtOctal, NiobiumTitanium, 2)
                .output(ENERGY_INPUT_HATCH_16A[1])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ADJUSTABLE_TRANSFORMER[ZPM])
                .input(ENERGY_INPUT_HATCH_4A[3])
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_ZPM)
                .input(wireGtOctal, VanadiumGallium, 2)
                .output(ENERGY_INPUT_HATCH_16A[2])
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ADJUSTABLE_TRANSFORMER[UV])
                .input(ENERGY_INPUT_HATCH_4A[4])
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_UV)
                .input(wireGtOctal, YttriumBariumCuprate, 2)
                .output(ENERGY_INPUT_HATCH_16A[3])
                .duration(200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH_4A[5], 2)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(wireGtDouble, RutheniumTriniumAmericiumNeutronate)
                .input(wireGtOctal, Europium, 2)
                .output(ENERGY_INPUT_HATCH_16A[4])
                .duration(200).EUt(VA[UV]).buildAndRegister();

        // 4A Dynamo Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[EV])
                .input(ENERGY_OUTPUT_HATCH[EV])
                .input(POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_EV)
                .input(wireGtQuadruple, Aluminium, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[0])
                .duration(100).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[IV])
                .input(ENERGY_OUTPUT_HATCH[IV])
                .input(HIGH_POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_IV)
                .input(wireGtQuadruple, Tungsten, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[1])
                .duration(100).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[LuV])
                .input(ENERGY_OUTPUT_HATCH[LuV])
                .input(HIGH_POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_LuV)
                .input(wireGtQuadruple, NiobiumTitanium, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[2])
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[ZPM])
                .input(ENERGY_OUTPUT_HATCH[ZPM])
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_ZPM)
                .input(wireGtQuadruple, VanadiumGallium, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[3])
                .duration(100).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[UV])
                .input(ENERGY_OUTPUT_HATCH[UV])
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_UV)
                .input(wireGtQuadruple, YttriumBariumCuprate, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[4])
                .duration(100).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH[UHV], 2)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .input(wireGtDouble, RutheniumTriniumAmericiumNeutronate)
                .input(wireGtQuadruple, Europium, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[5])
                .duration(100).EUt(VA[UV]).buildAndRegister();

        // 16A Dynamo Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ADJUSTABLE_TRANSFORMER[IV])
                .input(ENERGY_OUTPUT_HATCH_4A[1])
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_IV)
                .input(wireGtOctal, Tungsten, 2)
                .output(ENERGY_OUTPUT_HATCH_16A[0])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ADJUSTABLE_TRANSFORMER[LuV])
                .input(ENERGY_OUTPUT_HATCH_4A[2])
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_LuV)
                .input(wireGtOctal, NiobiumTitanium, 2)
                .output(ENERGY_OUTPUT_HATCH_16A[1])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ADJUSTABLE_TRANSFORMER[ZPM])
                .input(ENERGY_OUTPUT_HATCH_4A[3])
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_ZPM)
                .input(wireGtOctal, VanadiumGallium, 2)
                .output(ENERGY_OUTPUT_HATCH_16A[2])
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ADJUSTABLE_TRANSFORMER[UV])
                .input(ENERGY_OUTPUT_HATCH_4A[4])
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_UV)
                .input(wireGtOctal, YttriumBariumCuprate, 2)
                .output(ENERGY_OUTPUT_HATCH_16A[3])
                .duration(200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH_4A[5], 2)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(wireGtDouble, RutheniumTriniumAmericiumNeutronate)
                .input(wireGtOctal, Europium, 2)
                .output(ENERGY_OUTPUT_HATCH_16A[4])
                .duration(200).EUt(VA[UV]).buildAndRegister();

        // Maintenance Hatch

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LV])
                .circuitMeta(1)
                .output(MAINTENANCE_HATCH)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        // Multiblock Miners

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[EV])
                .input(frameGt, Titanium, 4)
                .input(circuit, Tier.EV, 4)
                .input(ELECTRIC_MOTOR_EV, 4)
                .input(ELECTRIC_PUMP_EV, 4)
                .input(CONVEYOR_MODULE_EV, 4)
                .input(gear, Tungsten, 4)
                .circuitMeta(2)
                .output(BASIC_LARGE_MINER)
                .duration(400).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(frameGt, TungstenSteel, 4)
                .input(circuit, Tier.IV, 4)
                .input(ELECTRIC_MOTOR_IV, 4)
                .input(ELECTRIC_PUMP_IV, 4)
                .input(CONVEYOR_MODULE_IV, 4)
                .input(gear, Iridium, 4)
                .circuitMeta(2)
                .output(LARGE_MINER)
                .duration(400).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(frameGt, HSSS, 4)
                .input(circuit, Tier.LuV, 4)
                .input(ELECTRIC_MOTOR_LuV, 4)
                .input(ELECTRIC_PUMP_LuV, 4)
                .input(CONVEYOR_MODULE_LuV, 4)
                .input(gear, Ruridit, 4)
                .circuitMeta(2)
                .output(ADVANCED_LARGE_MINER)
                .duration(400).EUt(VA[LuV]).buildAndRegister();

        // Multiblock Fluid Drills

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[MV])
                .input(frameGt, Steel, 4)
                .input(circuit, Tier.MV, 4)
                .input(ELECTRIC_MOTOR_MV, 4)
                .input(ELECTRIC_PUMP_MV, 4)
                .input(gear, VanadiumSteel, 4)
                .circuitMeta(2)
                .output(BASIC_FLUID_DRILLING_RIG)
                .duration(400).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[EV])
                .input(frameGt, Titanium, 4)
                .input(circuit, Tier.EV, 4)
                .input(ELECTRIC_MOTOR_EV, 4)
                .input(ELECTRIC_PUMP_EV, 4)
                .input(gear, TungstenCarbide, 4)
                .circuitMeta(2)
                .output(FLUID_DRILLING_RIG)
                .duration(400).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(frameGt, TungstenSteel, 4)
                .input(circuit, Tier.LuV, 4)
                .input(ELECTRIC_MOTOR_LuV, 4)
                .input(ELECTRIC_PUMP_LuV, 4)
                .input(gear, Osmiridium, 4)
                .circuitMeta(2)
                .output(ADVANCED_FLUID_DRILLING_RIG)
                .duration(400).EUt(VA[LuV]).buildAndRegister();
    }
}
