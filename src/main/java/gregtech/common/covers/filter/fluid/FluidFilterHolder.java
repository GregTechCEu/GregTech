package gregtech.common.covers.filter.fluid;

import gregtech.api.cover.filter.FilterHolder;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.FilterTypeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class FluidFilterHolder extends FilterHolder<FluidStack, FluidFilter> {

    private final BooleanSupplier shouldShowTip;
    private final int maxSize;

    public FluidFilterHolder(IDirtyNotifiable dirtyNotifiable) {
        this(dirtyNotifiable, () -> false, 1000);
    }

    public FluidFilterHolder(IDirtyNotifiable dirtyNotifiable, BooleanSupplier shouldShowTip) {
        this(dirtyNotifiable, shouldShowTip, 1000);
    }

    public FluidFilterHolder(IDirtyNotifiable dirtyNotifiable, BooleanSupplier shouldShowTip, int maxSize) {
        super(new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return FilterTypeRegistry.getFluidFilterForStack(stack) != null;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            /*@Override
            protected void onLoad() {
                onFilterSlotChange(false);
            }

            @Override
            protected void onContentsChanged(int slot) {
                onFilterSlotChange(true);
            }*/
        }, 0, dirtyNotifiable);
        this.shouldShowTip = shouldShowTip;
        this.maxSize = maxSize;
    }

    @Override
    public Class<FluidFilter> getFilterClass() {
        return FluidFilter.class;
    }

    @Override
    public void onFilterChanged(@Nullable FluidFilter oldFilter, @Nullable FluidFilter newFilter) {
        if (oldFilter != null) {
            oldFilter.setShouldShowTip(null);
        }
        if (newFilter != null) {
            newFilter.setShouldShowTip(this.shouldShowTip);
            newFilter.setMaxSize(this.maxSize);
        }
    }
}
