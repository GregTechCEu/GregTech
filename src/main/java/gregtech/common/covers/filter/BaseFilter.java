package gregtech.common.covers.filter;

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

    public final int getMaxTransferSize() {
        return this.filterReader.getMaxTransferRate();
    }

    public final void setMaxTransferSize(int maxStackSize) {
        this.filterReader.setMaxTransferRate(maxStackSize);
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
