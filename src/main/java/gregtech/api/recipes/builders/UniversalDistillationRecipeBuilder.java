package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ValidationResult;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class UniversalDistillationRecipeBuilder extends RecipeBuilder<UniversalDistillationRecipeBuilder> {

    public UniversalDistillationRecipeBuilder() {
    }

    public UniversalDistillationRecipeBuilder(Recipe recipe, RecipeMap<UniversalDistillationRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public UniversalDistillationRecipeBuilder(RecipeBuilder<UniversalDistillationRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public UniversalDistillationRecipeBuilder copy() {
        return new UniversalDistillationRecipeBuilder(this);
    }

    @Override
    public void buildAndRegister() {
        for (int i = 0; i < fluidOutputs.size(); i++) {
            IntCircuitRecipeBuilder builder = RecipeMaps.DISTILLERY_RECIPES.recipeBuilder().copy().EUt(this.EUt / 4).circuitMeta(i + 1);

            int ratio = getRatioForDistillery(this.fluidInputs.get(0), this.fluidOutputs.get(i), this.outputs.size() > 0 ? this.outputs.get(0) : null);

            int recipeDuration = this.EUt > 16 ? (int) (this.duration * 2.8f) : this.duration * 2;

            boolean shouldDivide = ratio != 1;

            boolean fluidsDivisible = isFluidStackDivisibleForDistillery(this.fluidInputs.get(0), ratio) &&
                    isFluidStackDivisibleForDistillery(this.fluidOutputs.get(i), ratio);

            FluidStack dividedInputFluid = new FluidStack(this.fluidInputs.get(0),  Math.max(1, this.fluidInputs.get(0).amount / ratio));
            FluidStack dividedOutputFluid = new FluidStack(this.fluidOutputs.get(i), Math.max(1, this.fluidOutputs.get(i).amount / ratio));

            if (shouldDivide && fluidsDivisible)
                builder.fluidInputs(dividedInputFluid)
                        .fluidOutputs(dividedOutputFluid)
                        .duration(recipeDuration / ratio);

            else if (!shouldDivide) {
                builder.fluidInputs(this.fluidInputs.get(0))
                        .fluidOutputs(this.fluidOutputs.get(i))
                        .outputs(this.outputs)
                        .duration(recipeDuration)
                        .buildAndRegister();
                continue;
            }

            if (this.outputs.size() > 0) {
                boolean itemsDivisible = GTUtility.isItemStackCountDivisible(this.outputs.get(0), ratio) && fluidsDivisible;

                if (fluidsDivisible && itemsDivisible) {
                    ItemStack stack = this.outputs.get(0).copy();
                    stack.setCount(stack.getCount() / ratio);

                    builder.outputs(stack);
                }
            }
            builder.buildAndRegister();
        }

        super.buildAndRegister();
    }

    public ValidationResult<Recipe> build() {
        return ValidationResult.newResult(finalizeAndValidate(),
                new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs, duration, EUt, hidden));
    }

    private int getRatioForDistillery(FluidStack fluidInput, FluidStack fluidOutput, ItemStack output) {
        int[] divisors = new int[]{2, 5, 10, 25, 50};
        int ratio = -1;

        for (int divisor : divisors) {

            if (!(isFluidStackDivisibleForDistillery(fluidInput, divisor)))
                continue;

            if (!(isFluidStackDivisibleForDistillery(fluidOutput, divisor)))
                continue;

            if (output != null && !(GTUtility.isItemStackCountDivisible(output, divisor)))
                continue;

            ratio = divisor;
        }

        return Math.max(1, ratio);
    }

    private boolean isFluidStackDivisibleForDistillery(FluidStack fluidStack, int divisor) {
        return GTUtility.isFluidStackAmountDivisible(fluidStack, divisor) && fluidStack.amount / divisor >= 25;
    }

}
