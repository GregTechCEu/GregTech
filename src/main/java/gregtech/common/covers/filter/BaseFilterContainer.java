package gregtech.common.covers.filter;

import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import org.jetbrains.annotations.NotNull;

public abstract class BaseFilterContainer<R, T extends Filter<R>> implements INBTSerializable<NBTTagCompound> {
    private final IDirtyNotifiable dirtyNotifiable;
    private int maxTransferSize = 1;
    private T currentItemFilter;
    private Runnable onFilterInstanceChange;
    private int transferSize;
    protected final FilterSlotHandler filterInventory;

    public BaseFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
        filterInventory = new FilterSlotHandler();
    }

    public ItemStackHandler getFilterInventory() {
        return filterInventory;
    }

    public int getMaxTransferSize() {
        return hasFilter() ? currentItemFilter.getMaxTransferSize() : this.maxTransferSize;
    }

    public void setMaxTransferSize(int maxTransferSize) {
        this.maxTransferSize = MathHelper.clamp(maxTransferSize, 1, Integer.MAX_VALUE);
        this.transferSize = MathHelper.clamp(this.transferSize, 1, this.maxTransferSize);
        if (hasFilter()) currentItemFilter.setMaxTransferSize(this.maxTransferSize);
    }

    public boolean hasFilter() {
        return currentItemFilter != null;
    }

    public T getFilter() {
        return currentItemFilter;
    }

    public final void setFilter(T newFilter) {
        this.currentItemFilter = newFilter;
        if (currentItemFilter != null) {
            currentItemFilter.setDirtyNotifiable(dirtyNotifiable);
        }
        if (onFilterInstanceChange != null) {
            this.onFilterInstanceChange.run();
        }
    }

    public void onFilterInstanceChange() {
        dirtyNotifiable.markAsDirty();
    }

    public void setOnFilterInstanceChange(Runnable onFilterInstanceChange) {
        this.onFilterInstanceChange = onFilterInstanceChange;
    }

    public boolean showGlobalTransferLimitSlider() {
        return getMaxTransferSize() > 0 && (isBlacklistFilter() || !hasFilter() || currentItemFilter.showGlobalTransferLimitSlider());
    }

    public int getTransferLimit(int slotIndex) {
        if (isBlacklistFilter() || currentItemFilter == null) {
            return getTransferSize();
        }
        return currentItemFilter.getTransferLimit(slotIndex, getTransferSize());
    }

    public boolean testItemStack(R toTest) {
        return !hasFilter() || getFilter().test(toTest);
    }

    public int getTransferLimit(R stack) {
        if (isBlacklistFilter() || currentItemFilter == null) {
            return getTransferSize();
        }
        return currentItemFilter.getTransferLimit(stack, getTransferSize());
    }

    protected abstract void onFilterSlotChange(boolean notify);

    public int getTransferSize() {
        if (!showGlobalTransferLimitSlider()) {
            return getMaxTransferSize();
        }
        return this.transferSize;
    }

    public void setTransferSize(int transferSize) {
        this.transferSize = MathHelper.clamp(transferSize, 1, getMaxTransferSize());
        onFilterInstanceChange();
    }

    @Deprecated
    public void adjustTransferStackSize(int amount) {
        setTransferSize(transferSize + amount);
    }

    public void setBlacklistFilter(boolean blacklistFilter) {
        if (hasFilter()) getFilter().setBlacklistFilter(blacklistFilter);
        onFilterInstanceChange();
    }

    public boolean isBlacklistFilter() {
        return hasFilter() && getFilter().isBlacklistFilter();
    }

    public void onMatch(R toTest, Filter.OnMatch<R> onMatch) {
        this.getFilter().setOnMatched(onMatch);
        this.getFilter().match(toTest);
    }

    /** Uses Cleanroom MUI*/
    public abstract IWidget initUI(ModularPanel main, GuiSyncManager manager);

    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeItemStack(this.filterInventory.getStackInSlot(0));
    }

    public abstract void readInitialSyncData(@NotNull PacketBuffer packetBuffer);

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setInteger("MaxStackSize", getMaxTransferSize());
        tagCompound.setInteger("TransferStackSize", getTransferSize());
        tagCompound.setTag("FilterInventory", filterInventory.serializeNBT());
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tagCompound) {
        this.maxTransferSize = tagCompound.getInteger("MaxStackSize");
        this.transferSize = tagCompound.getInteger("TransferStackSize");
        this.filterInventory.deserializeNBT(tagCompound.getCompoundTag("FilterInventory"));
    }

    protected class FilterSlotHandler extends ItemStackHandler {

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onLoad() {
            onFilterSlotChange(false);
        }

        @Override
        protected void onContentsChanged(int slot) {
            onFilterSlotChange(true);
        }
    }
}
