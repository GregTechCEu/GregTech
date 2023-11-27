package gregtech.api.capability.impl;

import gregtech.api.capability.IFilter;

import net.minecraftforge.fluids.FluidStack;

import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Basic filter with multiple fluid templates. Can be either whitelist or blacklist.
 */
public final class MultiFluidFilter implements IFilter<FluidStack> {

    private final boolean blacklist;
    private final FluidStack[] fluids;

    public MultiFluidFilter(boolean blacklist, @NotNull FluidStack... fluids) {
        this.blacklist = blacklist;
        this.fluids = fluids;
    }

    public MultiFluidFilter(boolean blacklist, Iterable<FluidStack> fluids) {
        this.blacklist = blacklist;
        this.fluids = Iterables.toArray(fluids, FluidStack.class);
    }

    @NotNull
    public List<FluidStack> getFluids() {
        return Collections.unmodifiableList(Arrays.asList(fluids));
    }

    public boolean isWhitelist() {
        return !blacklist;
    }

    public boolean isBlacklist() {
        return blacklist;
    }

    @Override
    public boolean test(@NotNull FluidStack fluidStack) {
        for (FluidStack fluid : this.fluids) {
            if (fluid.isFluidEqual(fluid)) return true;
        }
        return false;
    }

    @Override
    public int getPriority() {
        return this.fluids.length == 0 ? IFilter.noPriority() :
                this.blacklist ? IFilter.blacklistPriority(this.fluids.length) :
                        IFilter.whitelistPriority(this.fluids.length);
    }

    @NotNull
    @Override
    public IFilter<FluidStack> negate() {
        return new MultiFluidFilter(!this.blacklist, this.fluids);
    }

    @Override
    public String toString() {
        return "MultiFluidFilter{" +
                "blacklist=" + blacklist +
                ", fluids=" + Arrays.toString(fluids) +
                '}';
    }
}
