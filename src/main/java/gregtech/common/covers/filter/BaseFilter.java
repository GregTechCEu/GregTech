package gregtech.common.covers.filter;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.readers.BaseFilterReader;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;

public abstract class BaseFilter implements IFilter {

    protected IDirtyNotifiable dirtyNotifiable;
    private BaseFilterReader filterReader;

    protected final void setFilterReader(BaseFilterReader filterReader) {
        this.filterReader = filterReader;
    }

    public final ItemStack getContainerStack() {
        return this.filterReader.getContainer();
    }

    public final void setBlacklistFilter(boolean blacklistFilter) {
        this.filterReader.setBlacklistFilter(blacklistFilter);
        markDirty();
    }

    @Override
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

    @Override
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

    @Override
    public final int getTransferLimit(Object o, int transferSize) {
        if (o instanceof ItemStack stack) {
            return getTransferLimit(stack, transferSize);
        } else if (o instanceof FluidStack stack) {
            return getTransferLimit(stack, transferSize);
        }
        return 0;
    }

    public int getTransferLimit(FluidStack stack, int transferSize) {
        return 0;
    }

    public int getTransferLimit(ItemStack stack, int transferSize) {
        return 0;
    }

    public final boolean isBlacklistFilter() {
        return filterReader.isBlacklistFilter();
    }

    public IWidget createBlacklistUI() {
        return new ParentWidget<>().coverChildren()
                .child(new CycleButtonWidget()
                        .value(new BooleanSyncValue(
                                this::isBlacklistFilter,
                                this::setBlacklistFilter))
                        .textureGetter(state -> GTGuiTextures.BUTTON_BLACKLIST[state])
                        .addTooltip(0, IKey.lang("cover.filter.blacklist.disabled"))
                        .addTooltip(1, IKey.lang("cover.filter.blacklist.enabled")));
    }

    public final int getMaxTransferSize() {
        return this.filterReader.getMaxTransferRate();
    }

    public final void setMaxTransferSize(int maxStackSize) {
        this.filterReader.setMaxTransferRate(maxStackSize);
    }

    public boolean showGlobalTransferLimitSlider() {
        return isBlacklistFilter();
    }

    public final void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
        this.filterReader.setDirtyNotifiable(dirtyNotifiable);
    }

    public final void markDirty() {
        if (dirtyNotifiable != null) {
            dirtyNotifiable.markAsDirty();
        }
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        this.filterReader.deserializeNBT(tagCompound);
        markDirty();
    }
}
