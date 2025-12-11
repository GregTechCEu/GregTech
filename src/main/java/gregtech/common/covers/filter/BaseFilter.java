package gregtech.common.covers.filter;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.readers.BaseFilterReader;

import gregtech.common.items.behaviors.filter.BaseFilterUIManager;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class BaseFilter implements IItemComponent {

    public static final BaseFilter ERROR_FILTER = new BaseFilter() {

        @Override
        public BaseFilterReader getFilterReader() {
            return null;
        }

        @Override
        public void updateFilterReader(ItemStack stack) {}

        @Override
        public FilterType getType() {
            return FilterType.ERROR;
        }
    };

    public abstract BaseFilterReader getFilterReader();

    public abstract void updateFilterReader(ItemStack stack);

    public BaseFilterUIManager getUI() {
        if (getContainerStack().getItem() instanceof MetaItem<?>metaItem) {
            return Optional.ofNullable(metaItem.getItem(getContainerStack()))
                    .map(o -> (BaseFilterUIManager) o.getUIManager())
                    .orElseThrow(IllegalStateException::new);
        }
        throw new IllegalStateException();
    }

    public final ItemStack getContainerStack() {
        return this.getFilterReader().getContainer();
    }

    public static @NotNull BaseFilter getFilterFromStack(ItemStack stack) {
        if (stack.getItem() instanceof MetaItem<?>metaItem) {
            var metaValueItem = metaItem.getItem(stack);
            var filter = metaValueItem == null ? null : metaValueItem.getFilterBehavior();
            if (filter != null)
                return filter;
        }
        return ERROR_FILTER;
    }

    public final void setBlacklistFilter(boolean blacklistFilter) {
        this.getFilterReader().setBlacklistFilter(blacklistFilter);
    }

    public final MatchResult match(Object toMatch) {
        if (toMatch instanceof ItemStack stack) {
            return matchItem(stack);
        } else if (toMatch instanceof FluidStack stack) {
            return matchFluid(stack);
        }
        return MatchResult.NONE;
    }

    public MatchResult matchFluid(FluidStack fluidStack) {
        return MatchResult.NONE;
    }

    public MatchResult matchItem(ItemStack itemStack) {
        return MatchResult.NONE;
    }

    public final boolean test(Object toTest) {
        boolean b = false;
        if (toTest instanceof ItemStack stack) {
            b = testItem(stack);
        } else if (toTest instanceof FluidStack stack) {
            b = testFluid(stack);
        }
        return b != isBlacklistFilter();
    }

    public boolean testFluid(FluidStack toTest) {
        return false;
    }

    public boolean testItem(ItemStack toTest) {
        return false;
    }

    public final int getTransferLimit(Object o, int transferSize) {
        if (o instanceof ItemStack stack) {
            return getTransferLimit(stack, transferSize);
        } else if (o instanceof FluidStack stack) {
            return getTransferLimit(stack, transferSize);
        }
        return 0;
    }

    public int getTransferLimit(int slot, int transferSize) {
        return transferSize;
    }

    public int getTransferLimit(FluidStack stack, int transferSize) {
        return 0;
    }

    public int getTransferLimit(ItemStack stack, int transferSize) {
        return 0;
    }

    public final boolean isBlacklistFilter() {
        return getFilterReader().isBlacklistFilter();
    }

    public final int getMaxTransferSize() {
        return this.getFilterReader().getMaxTransferRate();
    }

    public final void setMaxTransferSize(int maxStackSize) {
        this.getFilterReader().setMaxTransferRate(maxStackSize);
    }

    public boolean showGlobalTransferLimitSlider() {
        return isBlacklistFilter();
    }

    public final void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable) {
        this.getFilterReader().setDirtyNotifiable(dirtyNotifiable);
    }

    public void readFromNBT(NBTTagCompound tag) {
        this.getFilterReader().deserializeNBT(tag);
    }

    public void writeInitialSyncData(PacketBuffer packetBuffer) {}

    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {}

    public abstract FilterType getType();

    public boolean isItem() {
        return getType() == FilterType.ITEM;
    }

    public boolean isFluid() {
        return getType() == FilterType.FLUID;
    }

    public boolean isError() {
        return getType() == FilterType.ERROR;
    }

    public enum FilterType {
        ITEM,
        FLUID,
        ERROR
    }
}
