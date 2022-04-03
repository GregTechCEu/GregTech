package gregtech.api.util;

import codechicken.lib.inventory.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ItemContainerSwitchShim implements IItemHandler, IInventory{

    IInventory container;

    public ItemContainerSwitchShim(IInventory container) {changeInventory(container)}

    public void changeInventory(IInventory container)
    {
        if (!(container instanceof IItemHandler)) {
            throw new IllegalArgumentException("Shim container must be both IInventory and IItemHandler!");
        }
        this.container = container;
    }

    @Override
    public int getSlots() { return container.getSizeInventory(); }

    @Override
    public int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int i) { return container.getStackInSlot((i)); }

    @Override
    public ItemStack decrStackSize(int i, int i1) {
        return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int i) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemStack) {

    }

    @Override
    public int getInventoryStackLimit() {
        return 0;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityPlayer) {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer entityPlayer) {

    }

    @Override
    public void closeInventory(EntityPlayer entityPlayer) {

    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        return false;
    }

    @Override
    public int getField(int i) {
        return 0;
    }

    @Override
    public void setField(int i, int i1) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack itemStack, boolean simulate) {
        ItemStack itemReturnable = itemStack.copy();
        itemReturnable.setCount(InventoryUtils.insertItem(container, itemStack, simulate));
        return itemReturnable;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        IInventory containerCopy = container;
        if (simulate)
            return InventoryUtils.decrStackSize(containerCopy, slot, amount);
        else
            return InventoryUtils.decrStackSize(container, slot, amount);
    }

    @Override
    public int getSlotLimit(int i) {
        return 64;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }
}
