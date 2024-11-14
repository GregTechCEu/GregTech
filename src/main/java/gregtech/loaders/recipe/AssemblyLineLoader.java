package gregtech.loaders.recipe;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials.Tier;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.BlockFusionCasing.CasingType.FUSION_COIL;
import static gregtech.common.blocks.BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL;
import static gregtech.common.blocks.MetaBlocks.FUSION_CASING;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.metatileentities.MetaTileEntities.FUSION_REACTOR;

public class AssemblyLineLoader {

    public static void init() {
        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(FUSION_CASING.getItemVariant(SUPERCONDUCTOR_COIL))
                .inputItem(circuit, Tier.ZPM, 4)
                .inputItem(plateDouble, Plutonium241)
                .inputItem(plateDouble, Osmiridium)
                .inputItem(FIELD_GENERATOR_IV, 2)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .inputItem(wireGtSingle, IndiumTinBariumTitaniumCuprate, 32)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(NiobiumTitanium.getFluid(L * 8))
                .outputs(FUSION_REACTOR[0].getStackForm())
                .scannerResearch(b -> b
                        .researchStack(OreDictUnifier.get(wireGtSingle, IndiumTinBariumTitaniumCuprate))
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(800).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(FUSION_CASING.getItemVariant(FUSION_COIL))
                .inputItem(circuit, Tier.UV, 4)
                .inputItem(plateDouble, Naquadria)
                .inputItem(plateDouble, Europium)
                .inputItem(FIELD_GENERATOR_LuV, 2)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 32)
                .inputItem(wireGtSingle, UraniumRhodiumDinaquadide, 32)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(VanadiumGallium.getFluid(L * 8))
                .outputs(FUSION_REACTOR[1].getStackForm())
                .stationResearch(b -> b
                        .researchStack(FUSION_REACTOR[0].getStackForm())
                        .CWUt(16)
                        .EUt(VA[ZPM]))
                .duration(1000).EUt(61440).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(FUSION_CASING.getItemVariant(FUSION_COIL))
                .inputItem(circuit, Tier.UHV, 4)
                .inputItem(QUANTUM_STAR)
                .inputItem(plateDouble, Americium)
                .inputItem(FIELD_GENERATOR_ZPM, 2)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .inputItem(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .inputItem(wireGtSingle, EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(YttriumBariumCuprate.getFluid(L * 8))
                .outputs(FUSION_REACTOR[2].getStackForm())
                .stationResearch(b -> b
                        .researchStack(FUSION_REACTOR[1].getStackForm())
                        .CWUt(96)
                        .EUt(VA[UV]))
                .duration(1000).EUt(VA[ZPM]).buildAndRegister();
    }
}
