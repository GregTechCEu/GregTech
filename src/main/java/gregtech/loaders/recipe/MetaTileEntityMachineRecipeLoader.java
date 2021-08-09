package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;

import static gregtech.common.metatileentities.MetaTileEntities.HULL;

public class MetaTileEntityMachineRecipeLoader {

    public static void init() {
        if (ConfigHolder.U.GT5u.harderEnergyHatches) {

            // Energy Output Hatches

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.ULV].getStackForm())
                    .input(OrePrefix.spring, Materials.Lead)
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Primitive, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_ULV.getStackForm(2))
                    .input(OrePrefix.rotor, Materials.Lead)
                    .fluidInputs(Materials.Lubricant.getFluid(2000))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.ULV].getStackForm())
                    .duration(200).EUt(7).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.LV].getStackForm())
                    .input(OrePrefix.spring, Materials.Tin)
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Basic, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_LV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_LV.getStackForm())
                    .fluidInputs(Materials.Lubricant.getFluid(2000))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.LV].getStackForm())
                    .duration(200).EUt(30).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.MV].getStackForm())
                    .input(OrePrefix.spring, Materials.Copper)
                    .inputs(MetaItems.SMALL_COIL.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_MV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_MV.getStackForm())
                    .fluidInputs(Materials.Lubricant.getFluid(2000))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.MV].getStackForm())
                    .duration(200).EUt(120).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.HV].getStackForm())
                    .input(OrePrefix.spring, Materials.Gold)
                    .inputs(MetaItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_HV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_HV.getStackForm())
                    .fluidInputs(Materials.Helium.getFluid(1000))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.HV].getStackForm())
                    .duration(200).EUt(480).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.HV].getStackForm())
                    .input(OrePrefix.spring, Materials.Gold)
                    .inputs(MetaItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_HV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_HV.getStackForm())
                    .fluidInputs(Materials.SodiumPotassium.getFluid(1000))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.HV].getStackForm())
                    .duration(200).EUt(480).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.EV].getStackForm())
                    .input(OrePrefix.spring, Materials.Aluminium)
                    .inputs(MetaItems.LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_EV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_EV.getStackForm())
                    .fluidInputs(Materials.Helium.getFluid(2000))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.EV].getStackForm())
                    .duration(200).EUt(1920).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.EV].getStackForm())
                    .input(OrePrefix.spring, Materials.Aluminium)
                    .inputs(MetaItems.LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_EV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_EV.getStackForm())
                    .fluidInputs(Materials.SodiumPotassium.getFluid(2000))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.EV].getStackForm())
                    .duration(200).EUt(1920).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.IV].getStackForm())
                    .input(OrePrefix.spring, Materials.Tungsten)
                    .inputs(MetaItems.POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_IV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_IV.getStackForm())
                    .fluidInputs(Materials.Helium.getFluid(3000))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.IV].getStackForm())
                    .duration(200).EUt(7680).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.IV].getStackForm())
                    .input(OrePrefix.spring, Materials.Tungsten)
                    .inputs(MetaItems.POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_IV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_IV.getStackForm())
                    .fluidInputs(Materials.SodiumPotassium.getFluid(3000))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.IV].getStackForm())
                    .duration(200).EUt(7680).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.LuV].getStackForm())
                    .input(OrePrefix.spring, Materials.YttriumBariumCuprate, 2)
                    .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Master, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_LUV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_LUV.getStackForm())
                    .fluidInputs(Materials.Helium.getFluid(6000))
                    .fluidInputs(Materials.SolderingAlloy.getFluid(720))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.LuV].getStackForm())
                    .duration(400).EUt(30720).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.LuV].getStackForm())
                    .input(OrePrefix.spring, Materials.YttriumBariumCuprate, 2)
                    .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Master, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_LUV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_LUV.getStackForm())
                    .fluidInputs(Materials.SodiumPotassium.getFluid(6000))
                    .fluidInputs(Materials.SolderingAlloy.getFluid(720))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.LuV].getStackForm())
                    .duration(400).EUt(30720).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.ZPM].getStackForm())
                    .input(OrePrefix.spring, Materials.VanadiumGallium, 4)
                    .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_ZPM.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_ZPM.getStackForm())
                    .fluidInputs(Materials.Helium.getFluid(6000))
                    .fluidInputs(Materials.SolderingAlloy.getFluid(1440))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.ZPM].getStackForm())
                    .duration(600).EUt(122880).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.ZPM].getStackForm())
                    .input(OrePrefix.spring, Materials.VanadiumGallium, 4)
                    .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_ZPM.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_ZPM.getStackForm())
                    .fluidInputs(Materials.SodiumPotassium.getFluid(6000))
                    .fluidInputs(Materials.SolderingAlloy.getFluid(1440))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.ZPM].getStackForm())
                    .duration(600).EUt(122880).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.UV].getStackForm())
                    .input(OrePrefix.spring, Materials.NiobiumTitanium, 4)
                    .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Superconductor, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_UV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_UV.getStackForm())
                    .fluidInputs(Materials.Helium.getFluid(12000))
                    .fluidInputs(Materials.SolderingAlloy.getFluid(2880))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.UV].getStackForm())
                    .duration(800).EUt(491520).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.UV].getStackForm())
                    .input(OrePrefix.spring, Materials.NiobiumTitanium, 4)
                    .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Superconductor, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_UV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_UV.getStackForm())
                    .fluidInputs(Materials.SodiumPotassium.getFluid(12000))
                    .fluidInputs(Materials.SolderingAlloy.getFluid(2880))
                    .outputs(MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.UV].getStackForm())
                    .duration(800).EUt(491520).buildAndRegister();

            // Energy Input Hatches

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.ULV].getStackForm())
                    .input(OrePrefix.cableGtSingle, Materials.Lead, 2)
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Primitive)
                    .inputs(MetaItems.VOLTAGE_COIL_ULV.getStackForm(2))
                    .input(OrePrefix.rotor, Materials.Lead)
                    .fluidInputs(Materials.Lubricant.getFluid(2000))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.ULV].getStackForm())
                    .duration(200).EUt(7).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.LV].getStackForm())
                    .input(OrePrefix.cableGtSingle, Materials.Tin, 2)
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Basic)
                    .inputs(MetaItems.VOLTAGE_COIL_LV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_LV.getStackForm())
                    .fluidInputs(Materials.Lubricant.getFluid(2000))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LV].getStackForm())
                    .duration(200).EUt(30).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.MV].getStackForm())
                    .input(OrePrefix.cableGtSingle, Materials.Copper)
                    .inputs(MetaItems.SMALL_COIL.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_MV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_MV.getStackForm())
                    .fluidInputs(Materials.Lubricant.getFluid(2000))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.MV].getStackForm())
                    .duration(200).EUt(120).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.HV].getStackForm())
                    .input(OrePrefix.cableGtSingle, Materials.Gold)
                    .inputs(MetaItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_HV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_HV.getStackForm())
                    .fluidInputs(Materials.Helium.getFluid(1000))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.HV].getStackForm())
                    .duration(200).EUt(480).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.HV].getStackForm())
                    .input(OrePrefix.cableGtSingle, Materials.Gold)
                    .inputs(MetaItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_HV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_HV.getStackForm())
                    .fluidInputs(Materials.SodiumPotassium.getFluid(1000))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.HV].getStackForm())
                    .duration(200).EUt(480).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.EV].getStackForm())
                    .input(OrePrefix.cableGtSingle, Materials.Aluminium)
                    .inputs(MetaItems.LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_EV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_EV.getStackForm())
                    .fluidInputs(Materials.Helium.getFluid(2000))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.EV].getStackForm())
                    .duration(200).EUt(1920).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.EV].getStackForm())
                    .input(OrePrefix.cableGtSingle, Materials.Aluminium)
                    .inputs(MetaItems.LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_EV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_EV.getStackForm())
                    .fluidInputs(Materials.SodiumPotassium.getFluid(2000))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.EV].getStackForm())
                    .duration(200).EUt(1920).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.IV].getStackForm())
                    .input(OrePrefix.cableGtSingle, Materials.Tungsten)
                    .inputs(MetaItems.POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_IV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_IV.getStackForm())
                    .fluidInputs(Materials.Helium.getFluid(3000))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.IV].getStackForm())
                    .duration(200).EUt(7680).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.IV].getStackForm())
                    .input(OrePrefix.cableGtSingle, Materials.Tungsten)
                    .inputs(MetaItems.POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .inputs(MetaItems.VOLTAGE_COIL_IV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_IV.getStackForm())
                    .fluidInputs(Materials.SodiumPotassium.getFluid(3000))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.IV].getStackForm())
                    .duration(200).EUt(7680).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.LuV].getStackForm())
                    .input(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate, 2)
                    .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Master, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_LUV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_LUV.getStackForm())
                    .fluidInputs(Materials.Helium.getFluid(6000))
                    .fluidInputs(Materials.SolderingAlloy.getFluid(720))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LuV].getStackForm())
                    .duration(400).EUt(30720).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.LuV].getStackForm())
                    .input(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate, 2)
                    .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Master, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_LUV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_LUV.getStackForm())
                    .fluidInputs(Materials.SodiumPotassium.getFluid(6000))
                    .fluidInputs(Materials.SolderingAlloy.getFluid(720))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LuV].getStackForm())
                    .duration(400).EUt(30720).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.ZPM].getStackForm())
                    .input(OrePrefix.cableGtDouble, Materials.VanadiumGallium, 2)
                    .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_ZPM.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_ZPM.getStackForm())
                    .fluidInputs(Materials.Helium.getFluid(6000))
                    .fluidInputs(Materials.SolderingAlloy.getFluid(1440))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.ZPM].getStackForm())
                    .duration(600).EUt(122880).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.ZPM].getStackForm())
                    .input(OrePrefix.cableGtDouble, Materials.VanadiumGallium, 2)
                    .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_ZPM.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_ZPM.getStackForm())
                    .fluidInputs(Materials.SodiumPotassium.getFluid(6000))
                    .fluidInputs(Materials.SolderingAlloy.getFluid(1440))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.ZPM].getStackForm())
                    .duration(600).EUt(122880).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.UV].getStackForm())
                    .input(OrePrefix.cableGtDouble, Materials.NiobiumTitanium, 2)
                    .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Superconductor, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_UV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_UV.getStackForm())
                    .fluidInputs(Materials.Helium.getFluid(12000))
                    .fluidInputs(Materials.SolderingAlloy.getFluid(2880))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.UV].getStackForm())
                    .duration(800).EUt(491520).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .inputs(HULL[GTValues.UV].getStackForm())
                    .input(OrePrefix.cableGtDouble, Materials.NiobiumTitanium, 2)
                    .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                    .input(OrePrefix.circuit, MarkerMaterials.Tier.Superconductor, 2)
                    .inputs(MetaItems.VOLTAGE_COIL_UV.getStackForm(2))
                    .inputs(MetaItems.ELECTRIC_PUMP_UV.getStackForm())
                    .fluidInputs(Materials.SodiumPotassium.getFluid(12000))
                    .fluidInputs(Materials.SolderingAlloy.getFluid(2880))
                    .outputs(MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.UV].getStackForm())
                    .duration(800).EUt(491520).buildAndRegister();
        }
    }
}
