package gregtech.common.covers.filter;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.readers.BaseFilterReader;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class FluidFilter implements Filter<FluidStack> {

    private IDirtyNotifiable dirtyNotifiable;
    private BaseFilterReader filterReader;

    protected void setFilterReader(BaseFilterReader filterReader) {
        this.filterReader = filterReader;
    }

    public int getTransferLimit(FluidStack fluidStack, int transferSize) {
        return 0;
    }

    @Override
    public int getTransferLimit(int slot, int transferSize) {
        return 0;
    }

    public int getTransferLimit(FluidStack stack) {
        return getTransferLimit(stack, getMaxTransferSize());
    }

    @Deprecated
    public abstract void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup);

    protected static MatchResult<FluidStack> createResult(boolean matched, FluidStack fluidStack, int index) {
        return MatchResult.create(matched, fluidStack, index);
    }

    public @NotNull Widget<?> createWidgets(GuiSyncManager syncManager) {
        var blacklist = new BooleanSyncValue(this.filterReader::isBlacklistFilter,
                this.filterReader::setBlacklistFilter);
        return new ParentWidget<>().coverChildren()
                .child(new CycleButtonWidget()
                        .value(blacklist)
                        .textureGetter(state -> GTGuiTextures.BUTTON_BLACKLIST[state])
                        .addTooltip(0, IKey.lang("cover.filter.blacklist.disabled"))
                        .addTooltip(1, IKey.lang("cover.filter.blacklist.enabled")));
    }

    public boolean showGlobalTransferLimitSlider() {
        return false;
    }

    @Override
    public final int getMaxTransferSize() {
        return this.filterReader.getMaxTransferRate();
    }

    @Override
    public final void setMaxTransferSize(int maxTransferSize) {
        this.filterReader.setMaxTransferRate(maxTransferSize);
    }

    public final void onMaxStackSizeChange() {
        this.filterReader.onTransferRateChange();
    }

    public abstract void readFromNBT(NBTTagCompound tagCompound);

    public final void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
        this.filterReader.setDirtyNotifiable(dirtyNotifiable);
    }

    public abstract void configureFilterTanks(int amount);

    @Override
    public boolean isBlacklistFilter() {
        return this.filterReader.isBlacklistFilter();
    }

    public void setBlacklistFilter(boolean blacklist) {
        this.filterReader.setBlacklistFilter(blacklist);
    }

    public final void markDirty() {
        if (dirtyNotifiable != null) {
            dirtyNotifiable.markAsDirty();
        }
    }
}
