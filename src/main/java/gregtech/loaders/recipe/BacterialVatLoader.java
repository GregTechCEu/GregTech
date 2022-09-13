package gregtech.loaders.recipe;

import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityRadiationHatch;

import static gregtech.api.recipes.RecipeMaps.BACTERIAL_VAT_RECIPES;
import static gregtech.api.unification.material.MarkerMaterials.Tier;
import static gregtech.api.unification.material.Materials.SterileGrowthMedium;
import static gregtech.api.GTValues.*;

public class BacterialVatLoader {

    public static void init() {
        MetaTileEntityRadiationHatch.addRadiationItem(GTRecipeOreInput.getOrCreate(OrePrefix.ingot, Materials.Uranium238), 1.0F);

        BACTERIAL_VAT_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit, Tier.ZPM)
                .output(MetaItems.WETWARE_PROCESSOR_ASSEMBLY_ZPM)
                .fluidInputs(SterileGrowthMedium.getFluid(50))
                .duration(40)
                .EUt(VA[ZPM])
                .rads(69)
                .buildAndRegister();
    }
}
