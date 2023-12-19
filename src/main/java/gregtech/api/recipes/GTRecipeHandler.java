package gregtech.api.recipes;

import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class GTRecipeHandler {

    /**
     * Removes all Recipes matching given inputs and fluid inputs from a given RecipeMap.
     * An example of how to use it:
     *
     * <cr>
     * removeRecipesByInputs(RecipeMaps.CHEMICAL_RECIPES,
     * new ItemStack[]{
     * OreDictUnifier.get(OrePrefix.dust, Materials.SodiumHydroxide, 3)
     * },
     * new FluidStack[]{
     * Materials.HypochlorousAcid.getFluid(1000),
     * Materials.AllylChloride.getFluid(1000)
     * });
     * </cr>
     *
     * This method also has varargs parameter methods for when there is only ItemStack or FluidStack inputs.
     *
     * @param map         The RecipeMap to search over.
     * @param itemInputs  The ItemStack[] containing all Recipe item inputs.
     * @param fluidInputs The FluidStack[] containing all Recipe fluid inputs.
     *
     * @return true if a recipe was removed, false otherwise.
     */
    public static <R extends RecipeBuilder<R>> boolean removeRecipesByInputs(RecipeMap<R> map, ItemStack[] itemInputs,
                                                                             FluidStack[] fluidInputs) {
        List<String> fluidNames = new ArrayList<>();
        List<String> itemNames = new ArrayList<>();

        List<ItemStack> itemIn = new ArrayList<>();
        for (ItemStack s : itemInputs) {
            itemIn.add(s);
            if (ConfigHolder.misc.debug) {
                itemNames.add(String.format("%s x %d", s.getDisplayName(), s.getCount()));
            }
        }

        List<FluidStack> fluidIn = new ArrayList<>();
        for (FluidStack s : fluidInputs) {
            fluidIn.add(s);
            if (ConfigHolder.misc.debug) {
                fluidNames.add(String.format("%s x %d", s.getFluid().getName(), s.amount));
            }
        }

        Recipe recipeFound = map.findRecipe(Long.MAX_VALUE, itemIn, fluidIn);
        boolean wasRemoved = recipeFound != null && map.removeRecipe(recipeFound);
        if (ConfigHolder.misc.debug) {
            if (wasRemoved)
                GTLog.logger.info("Removed Recipe for inputs: Items: {} Fluids: {}", itemNames, fluidNames);
            else GTLog.logger.error("Failed to Remove Recipe for inputs: Items: {} Fluids: {}", itemNames, fluidNames);
        }
        return wasRemoved;
    }

    public static <R extends RecipeBuilder<R>> boolean removeRecipesByInputs(RecipeMap<R> map,
                                                                             ItemStack... itemInputs) {
        return removeRecipesByInputs(map, itemInputs, new FluidStack[0]);
    }

    public static <R extends RecipeBuilder<R>> boolean removeRecipesByInputs(RecipeMap<R> map,
                                                                             FluidStack... fluidInputs) {
        return removeRecipesByInputs(map, new ItemStack[0], fluidInputs);
    }

    /**
     * Removes all Recipes from a given RecipeMap. This method cannot fail at recipe removal, but if called at
     * the wrong time during recipe registration, it may be an incomplete or overly-complete recipe removal.
     * An example of how to use it:
     *
     * <cr>
     * removeAllRecipes(RecipeMaps.BREWING_RECIPES);
     * </cr>
     *
     * @param map The RecipeMap to clear all recipes from.
     */
    public static <R extends RecipeBuilder<R>> void removeAllRecipes(RecipeMap<R> map) {
        map.removeAllRecipes();
        if (ConfigHolder.misc.debug) {
            GTLog.logger.info("Removed all recipes for Recipe Map: {}", map.unlocalizedName);
        }
    }
}
