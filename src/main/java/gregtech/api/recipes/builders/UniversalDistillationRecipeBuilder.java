package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import static gregtech.api.recipes.logic.OverclockingLogic.STANDARD_OVERCLOCK_DURATION_DIVISOR;

public class UniversalDistillationRecipeBuilder extends RecipeBuilder<UniversalDistillationRecipeBuilder> {

    private boolean doDistilleryRecipes = true;

    public UniversalDistillationRecipeBuilder() {}

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
        if (!this.doDistilleryRecipes) {
            super.buildAndRegister();
            return;
        }

        for (int i = 0; i < fluidOutputs.size(); i++) {
            SimpleRecipeBuilder builder = RecipeMaps.DISTILLERY_RECIPES.recipeBuilder().copy()
                    .EUt(Math.max(1, this.EUt / 4)).circuitMeta(i + 1);

            int ratio = getRatioForDistillery(this.fluidInputs.get(0).getInputFluidStack(), this.fluidOutputs.get(i),
                    !this.outputs.isEmpty() ? this.outputs.get(0) : null);

            int recipeDuration = (int) (this.duration * STANDARD_OVERCLOCK_DURATION_DIVISOR);

            boolean shouldDivide = ratio != 1;

            boolean fluidsDivisible = isFluidStackDivisibleForDistillery(this.fluidInputs.get(0).getInputFluidStack(),
                    ratio) &&
                    isFluidStackDivisibleForDistillery(this.fluidOutputs.get(i), ratio);

            FluidStack dividedInputFluid = new FluidStack(this.fluidInputs.get(0).getInputFluidStack(),
                    Math.max(1, this.fluidInputs.get(0).getAmount() / ratio));
            FluidStack dividedOutputFluid = new FluidStack(this.fluidOutputs.get(i),
                    Math.max(1, this.fluidOutputs.get(i).amount / ratio));

            if (shouldDivide && fluidsDivisible)
                builder.fluidInputs(dividedInputFluid)
                        .fluidOutputs(dividedOutputFluid)
                        .duration(Math.max(1, recipeDuration / ratio));

            else if (!shouldDivide) {
                builder.fluidInputs(this.fluidInputs.get(0))
                        .fluidOutputs(this.fluidOutputs.get(i))
                        .outputs(this.outputs)
                        .duration(recipeDuration)
                        .cleanroom(getCleanroom())
                        .buildAndRegister();
                continue;
            }

            if (!this.outputs.isEmpty()) {
                boolean itemsDivisible = this.outputs.get(0).getCount() % ratio == 0 && fluidsDivisible;

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

    private static int getRatioForDistillery(FluidStack fluidInput, FluidStack fluidOutput,
                                             @Nullable ItemStack output) {
        int[] divisors = new int[] { 2, 5, 10, 25, 50 };
        int ratio = -1;

        for (int divisor : divisors) {

            if (!isFluidStackDivisibleForDistillery(fluidInput, divisor))
                continue;

            if (!isFluidStackDivisibleForDistillery(fluidOutput, divisor))
                continue;

            if (output != null && output.getCount() % divisor != 0)
                continue;

            ratio = divisor;
        }

        return Math.max(1, ratio);
    }

    private static boolean isFluidStackDivisibleForDistillery(FluidStack fluidStack, int divisor) {
        return fluidStack.amount % divisor == 0 && fluidStack.amount / divisor >= 25;
    }

    // todo expose to CT
    public UniversalDistillationRecipeBuilder disableDistilleryRecipes() {
        this.doDistilleryRecipes = false;
        return this;
    }
}
