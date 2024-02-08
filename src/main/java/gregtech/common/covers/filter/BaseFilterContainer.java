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

public abstract class BaseFilterContainer extends ItemStackHandler {

    private int maxTransferSize = 1;
    private int transferSize;
    private @Nullable IFilter currentFilter;
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

    public void onFilterInstanceChange() {
        dirtyNotifiable.markAsDirty();
    }

    public void setOnFilterInstanceChange(@Nullable Runnable onFilterInstanceChange) {
        this.onFilterInstanceChange = onFilterInstanceChange;
    }

    public final @NotNull ItemStack getFilterStack() {
        return this.getStackInSlot(0);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (ItemStack.areItemStacksEqual(stack, getFilterStack()))
            return;

        if (stack.isEmpty()) {
            setFilter(null);
        } else if (FilterTypeRegistry.isFilter(stack)) {
            setFilter(FilterTypeRegistry.getFilterForStack(stack));
        }

        super.setStackInSlot(slot, stack);
    }

    // todo update stack for insert and extract, though that shouldn't be called normally
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    public final void setFilterStack(ItemStack stack) {
        setStackInSlot(0, stack);
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

    public @Nullable IFilter getFilter() {
        return currentFilter;
    }

    public final void setFilter(@Nullable IFilter newFilter) {
        this.currentFilter = newFilter;
        if (hasFilter()) {
            this.currentFilter.setDirtyNotifiable(dirtyNotifiable);
        }
        if (onFilterInstanceChange != null) {
            this.onFilterInstanceChange.run();
        }
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
        setFilter(getFilterStack().isEmpty() ? null : FilterTypeRegistry.getFilterForStack(getFilterStack()));
        if (hasFilter()) getFilter().readFromNBT(nbt);
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
