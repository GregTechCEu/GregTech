package gregtech.common.covers.filter;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.readers.BaseFilterReader;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class BaseFilter implements IFilter {

    private IDirtyNotifiable dirtyNotifiable;
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
