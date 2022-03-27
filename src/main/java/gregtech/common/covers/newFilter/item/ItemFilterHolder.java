package gregtech.common.covers.newFilter.item;

import gregtech.api.util.IDirtyNotifiable;
import gregtech.api.util.ItemStackKey;
import gregtech.common.covers.newFilter.FilterHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Set;

public class ItemFilterHolder extends FilterHolder<ItemStack, ItemFilter> {

    private static final Object MATCH_RESULT_TRUE = new Object();

    private int maxStackSizeLimit = 1;
    private int transferStackSize;

    public ItemFilterHolder(IDirtyNotifiable dirtyNotifiable) {
        super(dirtyNotifiable);
    }

    public ItemFilterHolder(IItemHandlerModifiable filterInventory, int filterSlotIndex, IDirtyNotifiable dirtyNotifiable) {
        super(filterInventory, filterSlotIndex, dirtyNotifiable);
    }

    public int getMaxStackSize() {
        return maxStackSizeLimit;
    }

    public int getTransferStackSize() {
        if (!showGlobalTransferLimitSlider()) {
            return getMaxStackSize();
        }
        return transferStackSize;
    }

    public void setTransferStackSize(int transferStackSize) {
        this.transferStackSize = MathHelper.clamp(transferStackSize, 1, getMaxStackSize());
        this.maxStackSizeLimit = getTransferStackSize();
        this.dirtyNotifiable.markAsDirty();
    }

    public void adjustTransferStackSize(int amount) {
        setTransferStackSize(transferStackSize + amount);
    }

    public void setMaxStackSize(int maxStackSizeLimit) {
        this.maxStackSizeLimit = maxStackSizeLimit;
        setTransferStackSize(transferStackSize);
    }

    public boolean showGlobalTransferLimitSlider() {
        return getMaxStackSize() > 1 && getCurrentFilter() != null && getCurrentFilter().showGlobalTransferLimitSlider();
    }

    public int getSlotTransferLimit(Object slotIndex, Set<ItemStackKey> matchedStacks) {
        int globalTransferLimit = getTransferStackSize();
        if (getCurrentFilter() == null || getCurrentFilter().isInverted()) {
            return globalTransferLimit;
        }
        return getCurrentFilter().getSlotTransferLimit(slotIndex, matchedStacks, globalTransferLimit);
    }

    public Object matchItemStack(ItemStack itemStack) {
        return getCurrentFilter() == null ? MATCH_RESULT_TRUE : getCurrentFilter().matchItemStack(itemStack);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setInteger("MaxStackSize", maxStackSizeLimit);
        nbt.setInteger("TransferStackSize", transferStackSize);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        setMaxStackSize(nbt.getInteger("MaxStackSize"));
        setTransferStackSize(nbt.getInteger("TransferStackSize"));
    }
}
