package gregtech.api.recipes.machines;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMapBackend;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FurnaceBackend<R extends RecipeBuilder<R>> extends RecipeMapBackend<R> {

    public FurnaceBackend(@Nonnull String unlocalizedName, @Nonnull RecipeBuilder<R> defaultRecipebuilder) {
        super(unlocalizedName, defaultRecipebuilder);
    }

    @Override
    @Nullable
    public Recipe findRecipe(long voltage, @Nonnull List<ItemStack> inputs, @Nonnull List<FluidStack> fluidInputs, boolean exactVoltage) {
        Recipe normalRecipe = super.findRecipe(voltage, inputs, fluidInputs, exactVoltage);
        if (normalRecipe != null || inputs.isEmpty())
            return normalRecipe;

        for (ItemStack input : inputs) {
            ItemStack output = ModHandler.getSmeltingOutput(input);

            if (!output.isEmpty()) {
                return this.defaultRecipebuilder.copy()
                        .inputs(GTUtility.copyAmount(1, input))
                        .outputs(output)
                        .duration(128).EUt(4)
                        .build().getResult();
            }
        }

        return null;
    }
}
