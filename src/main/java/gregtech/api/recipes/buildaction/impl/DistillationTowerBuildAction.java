package gregtech.api.recipes.buildaction.impl;

import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.buildaction.RecipeBuildAction;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static gregtech.api.recipes.logic.OverclockingLogic.STD_DURATION_FACTOR_INV;

public final class DistillationTowerBuildAction implements RecipeBuildAction<SimpleRecipeBuilder> {

    public static final DistillationTowerBuildAction INSTANCE = new DistillationTowerBuildAction();

    private DistillationTowerBuildAction() {}

    @Override
    public void accept(@NotNull SimpleRecipeBuilder prototype) {
        for (int i = 0; i < prototype.getFluidOutputs().size(); i++) {
            SimpleRecipeBuilder builder = RecipeMaps.DISTILLERY_RECIPES.recipeBuilder().copy()
                    .volts(Math.max(1, prototype.getVoltage() / 4)).circuitMeta(i + 1);

            int ratio = getRatioForDistillery(prototype.getFluidInputs().get(0).getAllMatchingStacks().get(0),
                    prototype.getFluidOutputs().get(i),
                    !prototype.getItemOutputs().isEmpty() ? prototype.getItemOutputs().get(0) : null);

            int recipeDuration = (int) (prototype.getDuration() * STD_DURATION_FACTOR_INV);

            boolean shouldDivide = ratio != 1;

            boolean fluidsDivisible = isFluidStackDivisibleForDistillery(
                    prototype.getFluidInputs().get(0).getAllMatchingStacks().get(0),
                    ratio) &&
                    isFluidStackDivisibleForDistillery(prototype.getFluidOutputs().get(i), ratio);

            FluidStack dividedInputFluid = new FluidStack(
                    prototype.getFluidInputs().get(0).getAllMatchingStacks().get(0),
                    GTUtility.safeCastLongToInt(
                            Math.max(1, prototype.getFluidInputs().get(0).getRequiredCount() / ratio)));
            FluidStack dividedOutputFluid = new FluidStack(prototype.getFluidOutputs().get(i),
                    Math.max(1, prototype.getFluidOutputs().get(i).amount / ratio));

            if (shouldDivide && fluidsDivisible)
                builder.fluidInputs(dividedInputFluid)
                        .fluidOutputs(dividedOutputFluid)
                        .duration(Math.max(1, recipeDuration / ratio));

            else if (!shouldDivide) {
                builder.ingredient(prototype.getFluidInputs().get(0))
                        .fluidOutputs(prototype.getFluidOutputs().get(i))
                        .outputs(prototype.getItemOutputs())
                        .duration(recipeDuration)
                        .cleanroom(prototype.getCleanroom())
                        .buildAndRegister();
                continue;
            }

            if (!prototype.getItemOutputs().isEmpty()) {
                boolean itemsDivisible = prototype.getItemOutputs().get(0).getCount() % ratio == 0 && fluidsDivisible;

                if (fluidsDivisible && itemsDivisible) {
                    ItemStack stack = prototype.getItemOutputs().get(0).copy();
                    stack.setCount(stack.getCount() / ratio);

                    builder.outputs(stack);
                }
            }
            builder.buildAndRegister();
        }
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
}
