package gregtech.common.covers.filter;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.readers.BaseFilterReader;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class ItemFilter implements Filter<ItemStack> {

    private IDirtyNotifiable dirtyNotifiable;
    private BaseItemFilterReader filterReader;

    protected void setFilterReader(BaseItemFilterReader reader) {
        this.filterReader = reader;
    }

    public ItemStack getContainerStack() {
        return this.filterReader.getContainer();
    }

    public final void setBlacklistFilter(boolean blacklistFilter) {
        this.filterReader.setBlacklistFilter(blacklistFilter);
        markDirty();
    }

    public final boolean isBlacklistFilter() {
        return filterReader.isBlacklistFilter();
    }

    public final int getMaxTransferSize() {
        return this.filterReader.getMaxTransferRate();
    }

    public final void setMaxTransferSize(int maxStackSize) {
        this.filterReader.setMaxTransferRate(maxStackSize);
    }

    public final void onMaxStackSizeChange() {
        this.filterReader.onTransferRateChange();
    }

    public abstract boolean showGlobalTransferLimitSlider();

    public int getTransferLimit(int matchSlot, int globalTransferLimit) {
        return 0;
    }

    public int getTransferLimit(ItemStack stack, int globalTransferLimit) {
        return 0;
    }

    /** Deprecated, uses old builtin MUI */
    @Deprecated
    public abstract void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup);

    /** Uses Cleanroom MUI */
    public abstract @NotNull ModularPanel createPopupPanel(GuiSyncManager syncManager);

    /** Uses Cleanroom MUI */
    public abstract @NotNull ModularPanel createPanel(GuiSyncManager syncManager);

    /** Uses Cleanroom MUI - Creates the widgets standalone so that they can be put into their own panel */

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

    public void readFromNBT(NBTTagCompound tagCompound) {
        this.filterReader.readFromNBT(tagCompound);
        markDirty();
    }

    protected static MatchResult<ItemStack> createResult(boolean matched, ItemStack stack, int index) {
        return MatchResult.create(matched, stack, index);
    }

    public final void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public final void markDirty() {
        if (dirtyNotifiable != null) {
            dirtyNotifiable.markAsDirty();
        }
    }

    protected class BaseItemFilterReader extends BaseFilterReader {

        protected static final String COUNT = "Count";

        public BaseItemFilterReader(ItemStack container, int slots) {
            super(container, slots);
        }

        public void onTransferRateChange() {}

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    }
}
