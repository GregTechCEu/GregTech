package gregtech.common.covers.newFilter.item;

import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.newFilter.FilterHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.IItemHandlerModifiable;

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

    @Override
    public Class<ItemFilter> getFilterClass() {
        return ItemFilter.class;
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
        this.dirtyNotifiable.markAsDirty();
    }

    public void adjustTransferStackSize(int amount) {
        setTransferStackSize(transferStackSize + amount);
    }

    public void setMaxStackSize(int maxStackSizeLimit) {
        this.maxStackSizeLimit = maxStackSizeLimit;
        this.transferStackSize = MathHelper.clamp(transferStackSize, 1, getMaxStackSize());
        this.dirtyNotifiable.markAsDirty();
    }

    public boolean showGlobalTransferLimitSlider() {
        return getCurrentFilter() == null || getCurrentFilter().showGlobalTransferLimitSlider();
    }

    public int getSlotTransferLimit(Object slotIndex) {
        int globalTransferLimit = getTransferStackSize();
        if (getCurrentFilter() == null || getCurrentFilter().isInverted()) {
            return globalTransferLimit;
        }
        return getCurrentFilter().getTransferLimit(slotIndex, globalTransferLimit);
    }

    public Object matchItemStack(ItemStack itemStack) {
        if (getCurrentFilter() == null) {
            return null;
        }
        Object result = getCurrentFilter().matchItemStack(itemStack);
        if (result instanceof ItemStack) {
            throw new IllegalStateException("Item Filter should not return a ItemStack as match result, since they can't be used properly in maps!");
        }
        return result;
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
