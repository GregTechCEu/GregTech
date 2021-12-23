package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
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

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.plateDouble, Materials.Plutonium241)
                .input(OrePrefix.plateDouble, Materials.Osmiridium)
                .inputs(MetaItems.FIELD_GENERATOR_IV.getStackForm(2))
                .inputs(MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(64))
                .input(OrePrefix.wireGtSingle, Materials.IndiumTinBariumTitaniumCuprate, 32)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L * 8))
                .fluidInputs(Materials.NiobiumTitanium.getFluid(GTValues.L * 8))
                .outputs(MetaTileEntities.FUSION_REACTOR[0].getStackForm())
                .duration(800)
                .EUt(VA[LuV])
                .buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL))
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Super)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Super)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Super)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Super)
                .input(OrePrefix.plateDouble, Materials.Naquadria)
                .input(OrePrefix.plateDouble, Materials.Europium)
                .inputs(MetaItems.FIELD_GENERATOR_LUV.getStackForm(2))
                .inputs(MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(48))
                .inputs(MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(48))
                .input(OrePrefix.wireGtSingle, Materials.UraniumRhodiumDinaquadide, 32)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L * 8))
                .fluidInputs(Materials.VanadiumGallium.getFluid(GTValues.L * 8))
                .outputs(MetaTileEntities.FUSION_REACTOR[1].getStackForm())
                .duration(1000)
                .EUt(61440)
                .buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL))
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Infinite)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Infinite)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Infinite)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Infinite)
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.plateDouble, Materials.Americium)
                .inputs(MetaItems.FIELD_GENERATOR_ZPM.getStackForm(2))
                .inputs(MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(64))
                .inputs(MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(64))
                .input(OrePrefix.wireGtSingle, Materials.EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L * 8))
                .fluidInputs(Materials.YttriumBariumCuprate.getFluid(GTValues.L * 8))
                .outputs(MetaTileEntities.FUSION_REACTOR[2].getStackForm())
                .duration(1000)
                .EUt(VA[ZPM])
                .buildAndRegister();
    }
}
