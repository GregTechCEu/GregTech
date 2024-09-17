package gregtech.api.recipes.output;

import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;

public class EmptyFluidOutput implements FluidOutputProvider {

    private static final Pair<List<FluidStack>, List<ChancedFluidOutput>> EMPTY = Pair.of(Collections.emptyList(),
            Collections.emptyList());

    @Override
    public @NotNull List<FluidStack> computeOutputs(@UnmodifiableView @NotNull List<ItemStack> inputItems,
                                                    @UnmodifiableView @NotNull List<FluidStack> inputFluids,
                                                    @UnmodifiableView @NotNull PropertySet propertySet, int recipeTier,
                                                    int machineTier, @NotNull ChanceBoostFunction boostFunction,
                                                    int parallel, int trimLimit) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Pair<@UnmodifiableView @NotNull List<FluidStack>, @UnmodifiableView @NotNull List<ChancedFluidOutput>> getCompleteOutputs(
                                                                                                                                              @UnmodifiableView @NotNull List<ItemStack> inputItems,
                                                                                                                                              @UnmodifiableView @NotNull List<FluidStack> inputFluids) {
        return EMPTY;
    }

    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getMaximumOutputs() {
        return 0;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
