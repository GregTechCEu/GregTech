package gregtech.integration;

import gregtech.api.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

/**
 * Contains utilities for recipe compatibility with scripting mods
 */
public final class RecipeCompatUtil {

    private RecipeCompatUtil() {/**/}

    /**
     * @param recipe the recipe to retrieve from
     * @return the first output in a human-readable form
     */
    @Nonnull
    public static String getFirstOutputString(@Nonnull Recipe recipe) {
        String output = "";
        if (!recipe.getOutputs().isEmpty()) {
            ItemStack item = recipe.getOutputs().get(0);
            output = item.getDisplayName() + " * " + item.getCount();
        } else if (!recipe.getFluidOutputs().isEmpty()) {
            FluidStack fluid = recipe.getFluidOutputs().get(0);
            output = fluid.getLocalizedName() + " * " + fluid.amount;
        }
        return output;
    }

}
