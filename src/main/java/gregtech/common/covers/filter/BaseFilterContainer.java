package gregtech.common.covers.filter;

import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class BaseFilterContainer<R> extends ItemStackHandler {

    private int maxTransferSize = 1;
    private int transferSize;
    private @Nullable Filter<R> currentFilter;
    private @Nullable Runnable onFilterInstanceChange;
    private final IDirtyNotifiable dirtyNotifiable;

    protected BaseFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        super();
        this.dirtyNotifiable = dirtyNotifiable;
    }

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

    public void onFilterInstanceChange() {
        dirtyNotifiable.markAsDirty();
    }

    public void setOnFilterInstanceChange(@Nullable Runnable onFilterInstanceChange) {
        this.onFilterInstanceChange = onFilterInstanceChange;
    }

    /**
     * Called when the filter slot has changed to a different filter or has been removed
     *
     * @param notify if true, call {@code onFilterInstanceChange()}
     */
    protected abstract void onFilterSlotChange(boolean notify);

    public final @NotNull ItemStack getFilterStack() {
        return this.getStackInSlot(0);
    }

    @SuppressWarnings("unchecked") // really need to stop doing this
    public final void setFilterStack(ItemStack stack) {
        this.setStackInSlot(0, stack);
        if (FilterTypeRegistry.isItemFilter(stack)) {
            setFilter((Filter<R>) FilterTypeRegistry.getItemFilterForStack(stack));
        } else {
            setFilter((Filter<R>) FilterTypeRegistry.getFluidFilterForStack(stack));
        }
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

    public final @Nullable Filter<R> getFilter() {
        return currentFilter;
    }

    public final void setFilter(@Nullable Filter<R> newFilter) {
        this.currentFilter = newFilter;
        if (hasFilter()) {
            this.currentFilter.setDirtyNotifiable(dirtyNotifiable);
        }
        if (onFilterInstanceChange != null) {
            this.onFilterInstanceChange.run();
        }
    }

    public boolean test(R toTest) {
        return !hasFilter() || getFilter().test(toTest);
    }

    public MatchResult<R> match(R toMatch) {
        if (!hasFilter())
            return MatchResult.create(true, toMatch, -1);

        return getFilter().match(toMatch);
    }

    public boolean showGlobalTransferLimitSlider() {
        return getMaxTransferSize() > 0 &&
                (isBlacklistFilter() || !hasFilter() || getFilter().showGlobalTransferLimitSlider());
    }

    public void setBlacklistFilter(boolean blacklistFilter) {
        if (hasFilter()) getFilter().setBlacklistFilter(blacklistFilter);
        onFilterInstanceChange();
    }

    public final boolean isBlacklistFilter() {
        return hasFilter() && getFilter().isBlacklistFilter();
    }

    public int getTransferSize() {
        if (!showGlobalTransferLimitSlider()) {
            return getMaxTransferSize();
        }
        return this.transferSize;
    }

    public int getTransferLimit(int slotIndex) {
        if (isBlacklistFilter()) {
            return getTransferSize();
        }
        return this.currentFilter.getTransferLimit(slotIndex, getTransferSize());
    }

    public int getTransferLimit(R stack) {
        if (isBlacklistFilter()) {
            return getTransferSize();
        }
        return this.currentFilter.getTransferLimit(stack, getTransferSize());
    }

    public void setTransferSize(int transferSize) {
        this.transferSize = MathHelper.clamp(transferSize, 1, getMaxTransferSize());
        onFilterInstanceChange();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setTag("FilterInventory", super.serializeNBT());
        tagCompound.setInteger("MaxStackSize", getMaxTransferSize());
        tagCompound.setInteger("TransferStackSize", getTransferSize());
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt.getCompoundTag("FilterInventory"));
        this.maxTransferSize = nbt.getInteger("MaxStackSize");
        this.transferSize = nbt.getInteger("TransferStackSize");
    }

    /** Uses Cleanroom MUI */
    public abstract IWidget initUI(ModularPanel main, GuiSyncManager manager);

    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeItemStack(this.getFilterStack());
    }

    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        var stack = ItemStack.EMPTY;
        try {
            stack = packetBuffer.readItemStack();
        } catch (IOException ignore) {}
        this.setFilterStack(stack);
    }
}
