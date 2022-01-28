package gregtech.loaders.recipe;

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
                .input(circuit, Tier.ZPM)
                .input(circuit, Tier.ZPM)
                .input(circuit, Tier.ZPM)
                .input(circuit, Tier.ZPM)
                .input(plateDouble, Plutonium241)
                .input(plateDouble, Osmiridium)
                .input(FIELD_GENERATOR_IV, 2)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .input(wireGtSingle, IndiumTinBariumTitaniumCuprate, 32)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(NiobiumTitanium.getFluid(L * 8))
                .outputs(FUSION_REACTOR[0].getStackForm())
                .duration(800).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(FUSION_CASING.getItemVariant(FUSION_COIL))
                .input(circuit, Tier.UV)
                .input(circuit, Tier.UV)
                .input(circuit, Tier.UV)
                .input(circuit, Tier.UV)
                .input(plateDouble, Naquadria)
                .input(plateDouble, Europium)
                .input(FIELD_GENERATOR_LuV, 2)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 48)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 48)
                .input(wireGtSingle, UraniumRhodiumDinaquadide, 32)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(VanadiumGallium.getFluid(L * 8))
                .outputs(FUSION_REACTOR[1].getStackForm())
                .duration(1000).EUt(61440).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(FUSION_CASING.getItemVariant(FUSION_COIL))
                .input(circuit, Tier.UHV)
                .input(circuit, Tier.UHV)
                .input(circuit, Tier.UHV)
                .input(circuit, Tier.UHV)
                .input(QUANTUM_STAR)
                .input(plateDouble, Americium)
                .input(FIELD_GENERATOR_ZPM, 2)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .input(wireGtSingle, EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(YttriumBariumCuprate.getFluid(L * 8))
                .outputs(FUSION_REACTOR[2].getStackForm())
                .duration(1000).EUt(VA[ZPM]).buildAndRegister();
    }
}
