package gregtech.api.recipes.machines.util;

import gregtech.api.recipes.ingredients.GTFluidIngredient;
import gregtech.api.recipes.ingredients.nbt.NBTMatcher;
import gregtech.api.recipes.lookup.flag.FluidStackMatchingContext;

import net.minecraftforge.fluids.FluidStack;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.Collections;

@Desugar
public record ExactMatchFluidIngredient(FluidStack stack, int amount) implements GTFluidIngredient {

    public ExactMatchFluidIngredient(@NotNull FluidStack stack, int amount) {
        this.stack = stack;
        this.amount = amount;
    }

    @Override
    public boolean matches(FluidStack stack) {
        return stack.isFluidEqual(stack);
    }

    @Override
    public @Range(from = 1, to = Long.MAX_VALUE) long getRequiredCount() {
        return amount;
    }

    @Override
    public @NotNull Collection<FluidStack> getMatchingStacksWithinContext(
                                                                          @NotNull FluidStackMatchingContext context) {
        if (context == FluidStackMatchingContext.FLUID_NBT) return Collections.singletonList(stack);
        return Collections.emptyList();
    }

    @Override
    public @Nullable NBTMatcher getMatcher() {
        return null;
    }
}
