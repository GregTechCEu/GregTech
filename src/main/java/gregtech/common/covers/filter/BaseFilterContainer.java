package gregtech.common.covers.filter;

import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class BaseFilterContainer<R, T extends Filter<R>> implements INBTSerializable<NBTTagCompound> {

    private final IDirtyNotifiable dirtyNotifiable;
    private int maxTransferSize = 1;
    private @Nullable T currentFilter;
    private @Nullable Runnable onFilterInstanceChange;
    private int transferSize;
    protected final FilterSlotHandler filterInventory;

    public BaseFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
        filterInventory = new FilterSlotHandler();
    }

    public @NotNull ItemStackHandler getFilterInventory() {
        return filterInventory;
    }

    public int getMaxTransferSize() {
        return hasFilter() ? currentFilter.getMaxTransferSize() : this.maxTransferSize;
    }

    public void setMaxTransferSize(int maxTransferSize) {
        this.maxTransferSize = MathHelper.clamp(maxTransferSize, 1, Integer.MAX_VALUE);
        this.transferSize = MathHelper.clamp(this.transferSize, 1, this.maxTransferSize);
        if (hasFilter()) currentFilter.setMaxTransferSize(this.maxTransferSize);
    }

    public final boolean hasFilter() {
        return currentFilter != null;
    }

    public final @Nullable T getFilter() {
        return currentFilter;
    }

    public final void setFilter(@Nullable T newFilter) {
        this.currentFilter = newFilter;
        if (hasFilter()) {
            currentFilter.setDirtyNotifiable(dirtyNotifiable);
        }
        if (onFilterInstanceChange != null) {
            this.onFilterInstanceChange.run();
        }
    }

    public void onFilterInstanceChange() {
        dirtyNotifiable.markAsDirty();
    }

    public void setOnFilterInstanceChange(@Nullable Runnable onFilterInstanceChange) {
        this.onFilterInstanceChange = onFilterInstanceChange;
    }

    public boolean showGlobalTransferLimitSlider() {
        return getMaxTransferSize() > 0 &&
                (isBlacklistFilter() || !hasFilter() || currentFilter.showGlobalTransferLimitSlider());
    }

    public int getTransferLimit(int slotIndex) {
        if (isBlacklistFilter() || currentFilter == null) {
            return getTransferSize();
        }
        return currentFilter.getTransferLimit(slotIndex, getTransferSize());
    }

    public boolean test(R toTest) {
        return !hasFilter() || getFilter().test(toTest);
    }

    public MatchResult<R> match(R toMatch) {
        if (!hasFilter())
            return MatchResult.create(true, toMatch, -1);

        return getFilter().match(toMatch);
    }

    public int getTransferLimit(R stack) {
        if (isBlacklistFilter() || currentFilter == null) {
            return getTransferSize();
        }
        return currentFilter.getTransferLimit(stack, getTransferSize());
    }

    /**
     * Called when the filter slot has changed to a different filter or has been removed
     * 
     * @param notify if true, call {@code onFilterInstanceChange()}
     */
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

    /** Uses Cleanroom MUI */
    public abstract IWidget initUI(ModularPanel main, GuiSyncManager manager);

    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeItemStack(this.filterInventory.getStackInSlot(0));
    }

    public ItemStack readFilterStack(PacketBuffer buffer) {
        var stack = ItemStack.EMPTY;
        try {
            stack = buffer.readItemStack();
        } catch (IOException ignore) {}
        this.filterInventory.setStackInSlot(0, stack);
        return stack;
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
