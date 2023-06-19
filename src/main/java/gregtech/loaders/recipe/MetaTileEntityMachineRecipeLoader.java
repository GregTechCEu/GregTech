package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.stack.UnificationEntry;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

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

        // Fluid Hatches
        registerHatchBusRecipe(ULV, FLUID_IMPORT_HATCH[ULV], FLUID_EXPORT_HATCH[ULV], new ItemStack(Blocks.GLASS));
        registerHatchBusRecipe(LV, FLUID_IMPORT_HATCH[LV], FLUID_EXPORT_HATCH[LV], new ItemStack(Blocks.GLASS));
        registerHatchBusRecipe(MV, FLUID_IMPORT_HATCH[MV], FLUID_EXPORT_HATCH[MV], BRONZE_DRUM.getStackForm());
        registerHatchBusRecipe(HV, FLUID_IMPORT_HATCH[HV], FLUID_EXPORT_HATCH[HV], STEEL_DRUM.getStackForm());
        registerHatchBusRecipe(EV, FLUID_IMPORT_HATCH[EV], FLUID_EXPORT_HATCH[EV], ALUMINIUM_DRUM.getStackForm());
        registerHatchBusRecipe(IV, FLUID_IMPORT_HATCH[IV], FLUID_EXPORT_HATCH[IV], STAINLESS_STEEL_DRUM.getStackForm());
        registerHatchBusRecipe(LuV, FLUID_IMPORT_HATCH[LuV], FLUID_EXPORT_HATCH[LuV], TITANIUM_DRUM.getStackForm());
        registerHatchBusRecipe(ZPM, FLUID_IMPORT_HATCH[ZPM], FLUID_EXPORT_HATCH[ZPM], TUNGSTENSTEEL_DRUM.getStackForm());
        registerHatchBusRecipe(UV, FLUID_IMPORT_HATCH[UV], FLUID_EXPORT_HATCH[UV], QUANTUM_TANK[0].getStackForm());
        registerHatchBusRecipe(UHV, FLUID_IMPORT_HATCH[UHV], FLUID_EXPORT_HATCH[UHV], QUANTUM_TANK[1].getStackForm());

        // Item Buses
        registerHatchBusRecipe(ULV, ITEM_IMPORT_BUS[ULV], ITEM_EXPORT_BUS[ULV], new ItemStack(Blocks.CHEST));
        registerHatchBusRecipe(LV, ITEM_IMPORT_BUS[LV], ITEM_EXPORT_BUS[LV], new ItemStack(Blocks.CHEST));
        registerHatchBusRecipe(MV, ITEM_IMPORT_BUS[MV], ITEM_EXPORT_BUS[MV], BRONZE_CRATE.getStackForm());
        registerHatchBusRecipe(HV, ITEM_IMPORT_BUS[HV], ITEM_EXPORT_BUS[HV], STEEL_CRATE.getStackForm());
        registerHatchBusRecipe(EV, ITEM_IMPORT_BUS[EV], ITEM_EXPORT_BUS[EV], ALUMINIUM_CRATE.getStackForm());
        registerHatchBusRecipe(IV, ITEM_IMPORT_BUS[IV], ITEM_EXPORT_BUS[IV], STAINLESS_STEEL_CRATE.getStackForm());
        registerHatchBusRecipe(LuV, ITEM_IMPORT_BUS[LuV], ITEM_EXPORT_BUS[LuV], TITANIUM_CRATE.getStackForm());
        registerHatchBusRecipe(ZPM, ITEM_IMPORT_BUS[ZPM], ITEM_EXPORT_BUS[ZPM], TUNGSTENSTEEL_CRATE.getStackForm());
        registerHatchBusRecipe(UV, ITEM_IMPORT_BUS[UV], ITEM_EXPORT_BUS[UV], QUANTUM_CHEST[0].getStackForm());
        registerHatchBusRecipe(UHV, ITEM_IMPORT_BUS[UHV], ITEM_EXPORT_BUS[UHV], QUANTUM_CHEST[1].getStackForm());

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
                .output(POWER_TRANSFORMER[ULV])
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[LV])
                .input(ELECTRIC_PUMP_LV)
                .input(wireGtQuadruple, Copper)
                .input(wireGtOctal, Tin)
                .input(springSmall, Tin)
                .input(spring, Copper)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[LV])
                .duration(200).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[MV])
                .input(ELECTRIC_PUMP_MV)
                .input(wireGtQuadruple, Gold)
                .input(wireGtOctal, Copper)
                .input(springSmall, Copper)
                .input(spring, Gold)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[MV])
                .duration(200).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[HV])
                .input(ELECTRIC_PUMP_MV)
                .input(wireGtQuadruple, Aluminium)
                .input(wireGtOctal, Gold)
                .input(springSmall, Gold)
                .input(spring, Aluminium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[HV])
                .duration(200).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[EV])
                .input(ELECTRIC_PUMP_HV)
                .input(wireGtQuadruple, Tungsten)
                .input(wireGtOctal, Aluminium)
                .input(springSmall, Aluminium)
                .input(spring, Tungsten)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[EV])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[IV])
                .input(ELECTRIC_PUMP_HV)
                .input(wireGtQuadruple, NiobiumTitanium)
                .input(wireGtOctal, Tungsten)
                .input(springSmall, Tungsten)
                .input(spring, NiobiumTitanium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[IV])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[LuV])
                .input(ELECTRIC_PUMP_EV)
                .input(wireGtQuadruple, VanadiumGallium)
                .input(wireGtOctal, NiobiumTitanium)
                .input(springSmall, NiobiumTitanium)
                .input(spring, VanadiumGallium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[LuV])
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[ZPM])
                .input(ELECTRIC_PUMP_EV)
                .input(wireGtQuadruple, YttriumBariumCuprate)
                .input(wireGtOctal, VanadiumGallium)
                .input(springSmall, VanadiumGallium)
                .input(spring, YttriumBariumCuprate)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[ZPM])
                .duration(200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[UV])
                .input(ELECTRIC_PUMP_IV)
                .input(wireGtQuadruple, Europium)
                .input(wireGtOctal, YttriumBariumCuprate)
                .input(springSmall, YttriumBariumCuprate)
                .input(spring, Europium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[UV])
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
                .input(POWER_TRANSFORMER[IV])
                .input(ENERGY_INPUT_HATCH_4A[1])
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_IV)
                .input(wireGtOctal, Tungsten, 2)
                .output(ENERGY_INPUT_HATCH_16A[0])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[LuV])
                .input(ENERGY_INPUT_HATCH_4A[2])
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_LuV)
                .input(wireGtOctal, NiobiumTitanium, 2)
                .output(ENERGY_INPUT_HATCH_16A[1])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[ZPM])
                .input(ENERGY_INPUT_HATCH_4A[3])
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_ZPM)
                .input(wireGtOctal, VanadiumGallium, 2)
                .output(ENERGY_INPUT_HATCH_16A[2])
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[UV])
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
                .input(POWER_TRANSFORMER[IV])
                .input(ENERGY_OUTPUT_HATCH_4A[1])
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_IV)
                .input(wireGtOctal, Tungsten, 2)
                .output(ENERGY_OUTPUT_HATCH_16A[0])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[LuV])
                .input(ENERGY_OUTPUT_HATCH_4A[2])
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_LuV)
                .input(wireGtOctal, NiobiumTitanium, 2)
                .output(ENERGY_OUTPUT_HATCH_16A[1])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[ZPM])
                .input(ENERGY_OUTPUT_HATCH_4A[3])
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_ZPM)
                .input(wireGtOctal, VanadiumGallium, 2)
                .output(ENERGY_OUTPUT_HATCH_16A[2])
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[UV])
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
                .circuitMeta(8)
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

    private static void registerHatchBusRecipe(int tier, MetaTileEntity inputBus, MetaTileEntity outputBus, ItemStack extra) {
        // Glue recipe for ULV and LV
        // 250L for ULV, 500L for LV
        if (tier <= GTValues.LV) {
            int fluidAmount = tier == GTValues.ULV ? 250 : 500;
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[tier])
                    .inputs(extra)
                    .fluidInputs(Glue.getFluid(fluidAmount))
                    .circuitMeta(1)
                    .output(inputBus)
                    .duration(300).EUt(VA[tier]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[tier])
                    .inputs(extra)
                    .fluidInputs(Glue.getFluid(fluidAmount))
                    .circuitMeta(2)
                    .output(outputBus)
                    .duration(300).EUt(VA[tier]).buildAndRegister();
        }

        // Polyethylene recipe for HV and below
        // 72L for ULV, 144L for LV, 288L for MV, 432L for HV
        if (tier <= GTValues.HV) {
            int peAmount = getFluidAmount(tier + 4);
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[tier])
                    .inputs(extra)
                    .fluidInputs(Polyethylene.getFluid(peAmount))
                    .circuitMeta(1)
                    .output(inputBus)
                    .duration(300).EUt(VA[tier]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[tier])
                    .inputs(extra)
                    .fluidInputs(Polyethylene.getFluid(peAmount))
                    .circuitMeta(2)
                    .output(outputBus)
                    .duration(300).EUt(VA[tier]).buildAndRegister();
        }

        // Polytetrafluoroethylene recipe for LuV and below
        // 36L for ULV, 72L for LV, 144L for MV, 288L for HV, 432L for EV, 576L for IV, 720L for LuV
        if (tier <= GTValues.LuV) {
            int ptfeAmount = getFluidAmount(tier + 3);
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[tier])
                    .inputs(extra)
                    .fluidInputs(Polytetrafluoroethylene.getFluid(ptfeAmount))
                    .circuitMeta(1)
                    .output(inputBus)
                    .duration(300).EUt(VA[tier]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[tier])
                    .inputs(extra)
                    .fluidInputs(Polytetrafluoroethylene.getFluid(ptfeAmount))
                    .circuitMeta(2)
                    .output(outputBus)
                    .duration(300).EUt(VA[tier]).buildAndRegister();
        }

        // PBI recipe for all
        // 4L for ULV, 9L for LV, 18L for MV, 36L for HV, 72L for EV, 144L for IV,
        // 288L for LuV, 432L for ZPM, 576L for UV, 720L for UHV
        // Use a Math.min() call on tier so that UHV hatches are still UV voltage
        int pbiAmount = getFluidAmount(tier);
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[tier])
                .inputs(extra)
                .fluidInputs(Polybenzimidazole.getFluid(pbiAmount))
                .circuitMeta(1)
                .output(inputBus)
                .duration(300).EUt(VA[Math.min(GTValues.UV, tier)]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[tier])
                .inputs(extra)
                .fluidInputs(Polybenzimidazole.getFluid(pbiAmount))
                .circuitMeta(2)
                .output(outputBus)
                .duration(300).EUt(VA[Math.min(GTValues.UV, tier)]).buildAndRegister();
    }

    private static int getFluidAmount(int offsetTier) {
        switch (offsetTier) {
            case 0: return 4;
            case 1: return 9;
            case 2: return 18;
            case 3: return 36;
            case 4: return 72;
            case 5: return 144;
            case 6: return 288;
            case 7: return 432;
            case 8: return 576;
            case 9:
            default: return 720;
        }
    }
}
