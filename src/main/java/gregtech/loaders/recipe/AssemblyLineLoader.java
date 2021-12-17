package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.BlockFusionCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;

import static gregtech.api.GTValues.*;

public class AssemblyLineLoader {
    public static void init() {

        // TODO Relocate
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Deuterium.getFluid(125), Materials.Tritium.getFluid(125)).fluidOutputs(Materials.Helium.getPlasma(125)).duration(16).EUt(4096).EUToStart(40000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Deuterium.getFluid(125), Materials.Helium3.getFluid(125)).fluidOutputs(Materials.Helium.getPlasma(125)).duration(16).EUt(VA[EV]).EUToStart(60000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Carbon.getFluid(125), Materials.Helium3.getFluid(125)).fluidOutputs(Materials.Oxygen.getPlasma(125)).duration(32).EUt(4096).EUToStart(80000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Beryllium.getFluid(16), Materials.Deuterium.getFluid(375)).fluidOutputs(Materials.Nitrogen.getPlasma(175)).duration(16).EUt(16384).EUToStart(180000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Silicon.getFluid(16), Materials.Magnesium.getFluid(16)).fluidOutputs(Materials.Iron.getPlasma(125)).duration(32).EUt(VA[IV]).EUToStart(360000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Potassium.getFluid(16), Materials.Fluorine.getFluid(125)).fluidOutputs(Materials.Nickel.getPlasma(125)).duration(16).EUt(VA[LuV]).EUToStart(480000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Beryllium.getFluid(16), Materials.Tungsten.getFluid(16)).fluidOutputs(Materials.Platinum.getFluid(16)).duration(32).EUt(VA[LuV]).EUToStart(150000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Neodymium.getFluid(16), Materials.Hydrogen.getFluid(48)).fluidOutputs(Materials.Europium.getFluid(16)).duration(64).EUt(24576).EUToStart(150000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Lutetium.getFluid(16), Materials.Chrome.getFluid(16)).fluidOutputs(Materials.Americium.getFluid(16)).duration(96).EUt(49152).EUToStart(200000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Plutonium239.getFluid(16), Materials.Thorium.getFluid(16)).fluidOutputs(Materials.Naquadah.getFluid(16)).duration(64).EUt(VA[LuV]).EUToStart(300000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Americium.getFluid(16), Materials.Naquadria.getFluid(16)).fluidOutputs(Materials.Neutronium.getFluid(2)).duration(200).EUt(98304).EUToStart(600000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Tungsten.getFluid(16), Materials.Helium.getFluid(16)).fluidOutputs(Materials.Osmium.getFluid(16)).duration(64).EUt(24578).EUToStart(150000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Manganese.getFluid(16), Materials.Hydrogen.getFluid(16)).fluidOutputs(Materials.Iron.getFluid(16)).duration(64).EUt(VA[IV]).EUToStart(120000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Mercury.getFluid(16), Materials.Magnesium.getFluid(16)).fluidOutputs(Materials.Uranium238.getFluid(16)).duration(64).EUt(49152).EUToStart(240000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Gold.getFluid(16), Materials.Aluminium.getFluid(16)).fluidOutputs(Materials.Uranium238.getFluid(16)).duration(64).EUt(49152).EUToStart(240000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Uranium238.getFluid(16), Materials.Helium.getFluid(16)).fluidOutputs(Materials.Plutonium239.getFluid(16)).duration(128).EUt(49152).EUToStart(480000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Vanadium.getFluid(16), Materials.Hydrogen.getFluid(125)).fluidOutputs(Materials.Chrome.getFluid(16)).duration(64).EUt(24576).EUToStart(140000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Gallium.getFluid(16), Materials.Radon.getFluid(125)).fluidOutputs(Materials.Duranium.getFluid(16)).duration(64).EUt(16384).EUToStart(140000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Titanium.getFluid(48), Materials.Duranium.getFluid(32)).fluidOutputs(Materials.Tritanium.getFluid(16)).duration(64).EUt(VA[LuV]).EUToStart(200000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Gold.getFluid(16), Materials.Mercury.getFluid(16)).fluidOutputs(Materials.Radon.getFluid(125)).duration(64).EUt(VA[LuV]).EUToStart(200000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Tantalum.getFluid(16), Materials.Tritium.getFluid(16)).fluidOutputs(Materials.Tungsten.getFluid(16)).duration(16).EUt(24576).EUToStart(200000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Silver.getFluid(16), Materials.Lithium.getFluid(16)).fluidOutputs(Materials.Indium.getFluid(16)).duration(32).EUt(24576).EUToStart(380000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.NaquadahEnriched.getFluid(15), Materials.Radon.getFluid(125)).fluidOutputs(Materials.Naquadria.getFluid(3)).duration(64).EUt(49152).EUToStart(400000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Lanthanum.getFluid(16), Materials.Silicon.getFluid(16)).fluidOutputs(Materials.Lutetium.getFluid(16)).duration(16).EUt(VA[IV]).EUToStart(80000000).buildAndRegister();
        RecipeMaps.FUSION_RECIPES.recipeBuilder().fluidInputs(Materials.Plutonium241.getFluid(16), Materials.Titanium.getFluid(16)).fluidOutputs(Materials.Livermorium.getFluid(16)).duration(32).EUt(VA[LuV]).EUToStart(200000000).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder().inputs(OreDictUnifier.get(OrePrefix.plate, Materials.Europium, 16), MetaItems.WETWARE_SUPER_COMPUTER_UV.getStackForm(4), MetaItems.ENERGY_LAPOTRONIC_ORB_CLUSTER.getStackForm(8), MetaItems.FIELD_GENERATOR_LUV.getStackForm(2), MetaItems.NANO_CENTRAL_PROCESSING_UNIT.getStackForm(64), MetaItems.NANO_CENTRAL_PROCESSING_UNIT.getStackForm(64), MetaItems.SMD_DIODE.getStackForm(8), OreDictUnifier.get(OrePrefix.cableGtSingle, Materials.Naquadah, 32)).fluidInputs(Materials.SolderingAlloy.getFluid(2880), Materials.Polybenzimidazole.getFluid(288)).outputs(MetaItems.ENERGY_LAPOTRONIC_MODULE.getStackForm()).duration(2000).EUt(100000).buildAndRegister();
        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder().inputs(OreDictUnifier.get(OrePrefix.plate, Materials.Americium, 16), MetaItems.WETWARE_SUPER_COMPUTER_UV.getStackForm(4), MetaItems.ENERGY_LAPOTRONIC_MODULE.getStackForm(8), MetaItems.FIELD_GENERATOR_ZPM.getStackForm(2), MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(64), MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(64), MetaItems.SMD_DIODE.getStackForm(16), OreDictUnifier.get(OrePrefix.cableGtSingle, Materials.NaquadahAlloy, 32)).fluidInputs(Materials.SolderingAlloy.getFluid(2880), Materials.Polybenzimidazole.getFluid(576)).outputs(MetaItems.ENERGY_LAPOTRONIC_CLUSTER.getStackForm()).duration(2000).EUt(200000).buildAndRegister();
        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder().inputs(OreDictUnifier.get(OrePrefix.plate, Materials.Neutronium, 16), MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(), MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(), MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(), MetaItems.WETWARE_MAINFRAME_UHV.getStackForm(), MetaItems.ENERGY_LAPOTRONIC_CLUSTER.getStackForm(8), MetaItems.FIELD_GENERATOR_UV.getStackForm(2), MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT_WAFER.getStackForm(64), MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT_WAFER.getStackForm(64), MetaItems.SMD_DIODE.getStackForm(16), OreDictUnifier.get(OrePrefix.wireGtSingle, Materials.EnrichedNaquadahTriniumEuropiumDuranide, 32)).fluidInputs(Materials.SolderingAlloy.getFluid(2880), Materials.Polybenzimidazole.getFluid(1000), Materials.Naquadria.getFluid(1152)).outputs(MetaItems.ULTIMATE_BATTERY.getStackForm()).duration(2000).EUt(300000).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.plate, Materials.Plutonium241)
                .input(OrePrefix.plate, Materials.Trinium)
                .inputs(MetaItems.FIELD_GENERATOR_IV.getStackForm(2))
                .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(32))
                .input(OrePrefix.wireGtSingle, Materials.IndiumTinBariumTitaniumCuprate, 32)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L * 8))
                .fluidInputs(Materials.NiobiumTitanium.getFluid(GTValues.L * 8))
                .outputs(MetaTileEntities.FUSION_REACTOR[0].getStackForm())
                .duration(800)
                .EUt(VA[LuV])
                .buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Super)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Super)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Super)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Super)
                .input(OrePrefix.plate, Materials.Naquadria)
                .input(OrePrefix.plate, Materials.Europium)
                .inputs(MetaItems.FIELD_GENERATOR_LUV.getStackForm(2))
                .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(48))
                .input(OrePrefix.wireGtSingle, Materials.UraniumRhodiumDinaquadide, 32)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L * 8))
                .fluidInputs(Materials.NaquadahAlloy.getFluid(GTValues.L * 8))
                .outputs(MetaTileEntities.FUSION_REACTOR[1].getStackForm())
                .duration(1000)
                .EUt(61440)
                .buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Infinite)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Infinite)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Infinite)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Infinite)
                .input(OrePrefix.plate, Materials.NetherStar)
                .input(OrePrefix.plate, Materials.Americium)
                .inputs(MetaItems.FIELD_GENERATOR_ZPM.getStackForm(2))
                .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(64))
                .input(OrePrefix.wireGtSingle, Materials.EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L * 8))
                .fluidInputs(Materials.YttriumBariumCuprate.getFluid(GTValues.L * 8))
                .outputs(MetaTileEntities.FUSION_REACTOR[2].getStackForm())
                .duration(1000)
                .EUt(VA[ZPM])
                .buildAndRegister();
    }
}
