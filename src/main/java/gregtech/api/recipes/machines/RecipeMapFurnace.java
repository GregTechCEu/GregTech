package gregtech.api.recipes.machines;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeIterator;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ui.RecipeMapUIFunction;
import gregtech.api.util.GTUtility;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

@ApiStatus.Internal
public class RecipeMapFurnace extends RecipeMap<SimpleRecipeBuilder> {

    public static final int RECIPE_EUT = 4;
    public static final int RECIPE_DURATION = 128;

    public RecipeMapFurnace(@NotNull String unlocalizedName, @NotNull SimpleRecipeBuilder defaultRecipeBuilder,
                            @NotNull RecipeMapUIFunction recipeMapUI) {
        super(unlocalizedName, defaultRecipeBuilder, recipeMapUI, 1, 1, 0, 0);
        setSound(GTSoundEvents.FURNACE);
    }

    @Override
    @Nullable
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, boolean exactVoltage) {
        Recipe normalRecipe = super.findRecipe(voltage, inputs, fluidInputs, exactVoltage);
        if (normalRecipe != null || inputs.isEmpty())
            return normalRecipe;

        for (ItemStack input : inputs) {
            ItemStack output = ModHandler.getSmeltingOutput(input);

            if (!output.isEmpty()) {
                return this.recipeBuilder()
                        .inputs(GTUtility.copy(1, input))
                        .outputs(output)
                        .duration(RECIPE_DURATION).EUt(RECIPE_EUT)
                        .build().getResult();
            }
        }

        return null;
    }

    // probably can just extend Iterator<Recipe> directly.
    static class FurnaceRecipeIterator implements Iterator<Recipe> {
        Stack<Recipe> recipe = new Stack<>();
        FurnaceRecipeIterator(Recipe recipe) {
            this.recipe.add(recipe);
        }
        @Override
        public boolean hasNext() { return !recipe.isEmpty(); }
        @Override
        public Recipe next() {
            if (recipe.isEmpty()) return null;
            return recipe.pop();
        }
    }

    @Override
    @NotNull
    public Iterator<Recipe> getRecipeIterator(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs,
                                              boolean exactVoltage) {
        return new FurnaceRecipeIterator(this.findRecipe(voltage, inputs, fluidInputs, exactVoltage));
    }
}
