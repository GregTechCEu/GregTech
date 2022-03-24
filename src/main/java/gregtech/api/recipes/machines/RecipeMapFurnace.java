package gregtech.api.recipes.machines;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public class RecipeMapFurnace extends RecipeMap<SimpleRecipeBuilder> {

    public RecipeMapFurnace(String unlocalizedName, int minInputs, int maxInputs, int minOutputs, int maxOutputs, int minFluidInputs, int maxFluidInputs, int minFluidOutputs, int maxFluidOutputs, SimpleRecipeBuilder defaultRecipe, boolean isHidden) {
        super(unlocalizedName, minInputs, maxInputs, minOutputs, maxOutputs, minFluidInputs, maxFluidInputs, minFluidOutputs, maxFluidOutputs, defaultRecipe, isHidden);
    }

    @Override
    @Nullable
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, int outputFluidTankCapacity, boolean exactVoltage) {
        Recipe normalRecipe = super.findRecipe(voltage, inputs, fluidInputs, outputFluidTankCapacity, exactVoltage);
        if (normalRecipe != null || inputs.size() == 0)
            return normalRecipe;

        for (ItemStack input : inputs) {
            ItemStack output = ModHandler.getSmeltingOutput(input);

            if (!output.isEmpty()) {
                return this.recipeBuilder()
                        .inputs(GTUtility.copyAmount(1, input))
                        .outputs(output)
                        .duration(128).EUt(4)
                        .build().getResult();
            }
        }

        return null;
    }
}
