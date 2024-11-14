package gregtech.api.recipes.output;

import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;

public class EmptyFluidOutput implements FluidOutputProvider {

    @Override
    public @NotNull List<FluidStack> computeOutputs(@UnmodifiableView @NotNull List<ItemStack> inputItems,
                                                    @UnmodifiableView @NotNull List<FluidStack> inputFluids,
                                                    @UnmodifiableView @NotNull PropertySet propertySet, int recipeTier,
                                                    int machineTier,
                                                    int parallel, int trimLimit) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull @UnmodifiableView List<FluidStack> getCompleteOutputs(int parallel, int trimLimit,
                                                                          @UnmodifiableView @NotNull List<ItemStack> inputItems,
                                                                          @UnmodifiableView @NotNull List<FluidStack> inputFluids) {
        return Collections.emptyList();
    }

    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getMaximumOutputs(
                                                                          @Range(from = 1,
                                                                                 to = Integer.MAX_VALUE) int parallel) {
        return 0;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
