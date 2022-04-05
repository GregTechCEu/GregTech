package gregtech.api.util;

import codechicken.lib.inventory.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class ItemContainerSwitchShim implements IItemHandlerModifiable, IInventory{

    // private static final IInventory container;
    // protected ItemStack[] items;
    protected VirtualContainerRegistry.VirtualContainer container;
    private boolean isDirty = false;

    public ItemContainerSwitchShim(VirtualContainerRegistry.VirtualContainer container) {
        changeInventory(container);
    }

    public void changeInventory(VirtualContainerRegistry.VirtualContainer container)
    {
        if (!(container instanceof IItemHandler)) {
            throw new IllegalArgumentException("Shim container must be both IInventory and IItemHandler!");
        }
        this.container = container;
    }

    @Override
    public int getSlots() {
        return container.getSlots();
    }


    @Override
    public int getSizeInventory() {
        return container.getSizeInventory();
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return container.getStackInSlot((slot));
    }

    @Override
    public ItemStack decrStackSize(int slot, int amt) {
        return container.decrStackSize(slot, amt);
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return container.removeStackFromSlot(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack) {
        container.setInventorySlotContents(slot, itemStack);
    }

    @Override
    public int getInventoryStackLimit() {
        return container.getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
        isDirty = true;
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
    public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
        return container.isItemValidForSlot(slot, itemStack);
    }

    @Override
    public int getField(int slot) {
        return container.getField(slot);
    }

    @Override
    public void setField(int slot, int value) {
        container.setField(slot, value);
    }

    @Override
    public int getFieldCount() {
        return container.getFieldCount();
    }

    @Override
    public void clear() {
        container.clear();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack itemStack, boolean doInsert) {
        return container.insertItem(slot, itemStack, doInsert);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean doExtract) {
        return container.extractItem(slot, amount, doExtract);
    }

    @Override
    public int getSlotLimit(int slot) {
        return container.getSlotLimit(slot);
    }

    @Override
    public String getName() {
        return container.getName();
    }

    @Override
    public boolean hasCustomName() {
        return container.hasCustomName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return container.getDisplayName();
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack itemStack) {
        container.setStackInSlot(slot, itemStack);
    }
}
