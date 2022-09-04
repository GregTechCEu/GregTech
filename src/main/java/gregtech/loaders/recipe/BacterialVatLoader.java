package gregtech.loaders.recipe;

import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityRadiationHatch;

public class BacterialVatLoader {

    public static void init() {
        MetaTileEntityRadiationHatch.addRadiationItem(GTRecipeOreInput.getOrCreate(OrePrefix.ingot, Materials.Uranium238), 1.0F);
    }
}
