package gregtech.loaders.recipe.chemistry;

import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class GalliumRecipes {

    public static void init() {
        //FROM BAUXITE
        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(GalliumLiquor.getFluid(1000))
                .notConsumable(OrePrefix.stick, Steel, 1)
                .notConsumable(MetaItems.GRAPHITE_ELECTRODE)
                .fluidOutputs(SodiumHydroxideSolution.getFluid(1000))
                .chancedOutput(dust, Gallium, 1, 1000, 0)
                .duration(150)
                .EUt(24)
                .buildAndRegister();

        //FROM SPHALERITE (WILL ADD LATER)

        //FROM COAL FLY ASH (WILL ADD LATER)
    }
}
