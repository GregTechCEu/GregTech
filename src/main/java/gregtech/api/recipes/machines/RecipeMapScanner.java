package gregtech.api.recipes.machines;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.util.AssemblyLineManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class RecipeMapScanner extends RecipeMap<SimpleRecipeBuilder> implements IScannerRecipeMap {

    public RecipeMapScanner(String unlocalizedName, int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs, SimpleRecipeBuilder defaultRecipe, boolean isHidden) {
        super(unlocalizedName, maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs, defaultRecipe, isHidden);
    }

    @Override
    @Nullable
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, boolean exactVoltage) {
        Recipe recipe = super.findRecipe(voltage, inputs, fluidInputs, exactVoltage);

        // Data stick copying - min of 2 inputs required
        if (recipe == null && inputs.size() > 1) {
            // try the data recipe both ways, prioritizing overwriting the first
            recipe = createDataRecipe(inputs.get(0), inputs.get(1));
            if (recipe != null) return recipe;

            return createDataRecipe(inputs.get(1), inputs.get(0));
        }
        return recipe;
    }

    @Nullable
    private static Recipe createDataRecipe(@Nonnull ItemStack first, @Nonnull ItemStack second) {
        NBTTagCompound compound = second.getTagCompound();
        if (compound == null) return null;

        boolean isFirstDataItem = AssemblyLineManager.isStackDataItem(first, true);
        if (!isFirstDataItem) return null;
        boolean isSecondDataItem = AssemblyLineManager.isStackDataItem(second, true);
        if (isSecondDataItem) {
            ItemStack output = first.copy();
            output.setTagCompound(compound.copy());
            return RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(first)
                    .notConsumable(second)
                    .outputs(output)
                    .duration(100).EUt(2).build().getResult();
        }
        return null;
    }
}
