package gregtech.api.recipes.machines;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeMapFurnace extends RecipeMap<SimpleRecipeBuilder> {

    public static final int RECIPE_EUT = 4;
    public static final int RECIPE_DURATION = 128;

    public RecipeMapFurnace(String unlocalizedName, int maxInputs, int maxOutputs, int maxFluidInputs,
                            int maxFluidOutputs, SimpleRecipeBuilder defaultRecipe, boolean isHidden) {
        super(unlocalizedName, maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs, defaultRecipe, isHidden);
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
}
