package gregtech.common.metatileentities.multi.multiblockpart.appeng.slot;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.storage.data.IAEItemStack;
import org.jetbrains.annotations.NotNull;

public class ExportOnlyAEItemList extends NotifiableItemStackHandler {

    protected final int size;
    protected ExportOnlyAEItemSlot[] inventory;

    public ExportOnlyAEItemList(MetaTileEntity holder, int slots, MetaTileEntity entityToNotify) {
        super(holder, slots, entityToNotify, false);
        this.size = slots;
        createInventory(holder);
    }

    protected void createInventory(MetaTileEntity holder) {
        this.inventory = new ExportOnlyAEItemSlot[size];
        for (int i = 0; i < size; i++) {
            this.inventory[i] = new ExportOnlyAEItemSlot();
        }
        for (ExportOnlyAEItemSlot slot : this.inventory) {
            slot.setTrigger(this::onContentsChanged);
        }
    }

    public ExportOnlyAEItemSlot[] getInventory() {
        return inventory;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        for (int index = 0; index < size; index++) {
            if (nbt.hasKey("#" + index)) {
                NBTTagCompound slotTag = nbt.getCompoundTag("#" + index);
                this.inventory[index].deserializeNBT(slotTag);
            }
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        for (int index = 0; index < size; index++) {
            NBTTagCompound slot = this.inventory[index].serializeNBT();
            nbt.setTag("#" + index, slot);
        }
        return nbt;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        // NO-OP
    }

    @Override
    public int getSlots() {
        return size;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot >= 0 && slot < size) {
            return this.inventory[slot].getStackInSlot(0);
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= 0 && slot < size) {
            return this.inventory[slot].extractItem(0, amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    public void clearConfig() {
        for (var slot : inventory) {
            slot.setConfig(null);
            slot.setStock(null);
        }
    }

    public boolean hasStackInConfig(ItemStack stack, boolean checkExternal) {
        if (stack == null || stack.isEmpty()) return false;
        for (ExportOnlyAEItemSlot slot : inventory) {
            IAEItemStack config = slot.getConfig();
            if (config != null && config.isSameType(stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAutoPull() {
        return false;
    }

    public boolean isStocking() {
        return false;
    }
}
