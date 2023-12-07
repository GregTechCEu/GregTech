package gregtech.integration.baubles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import baubles.api.cap.IBaublesItemHandler;
import org.jetbrains.annotations.NotNull;

/** Wrapped player inventory and Baubles inventory. */
public class BaublesWrappedInventory implements IInventory {

    private final IBaublesItemHandler handler;
    private final EntityPlayer player;

    public BaublesWrappedInventory(IBaublesItemHandler handler, EntityPlayer player) {
        this.handler = handler;
        this.player = player;
    }

    // wrap everything to check the baubles first, then the player inventory

    @Override
    public int getSizeInventory() {
        return handler.getSlots() + player.inventory.getSizeInventory();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int index) {
        if (index < handler.getSlots()) {
            return handler.getStackInSlot(index);
        }
        return player.inventory.getStackInSlot(index - handler.getSlots());
    }

    @Override
    public @NotNull ItemStack decrStackSize(int index, int count) {
        if (index < handler.getSlots()) {
            return handler.extractItem(index, count, false);
        }
        return player.inventory.decrStackSize(index - handler.getSlots(), count);
    }

    @Override
    public @NotNull ItemStack removeStackFromSlot(int index) {
        if (index < handler.getSlots()) {
            ItemStack out = handler.getStackInSlot(index);
            handler.setStackInSlot(index, ItemStack.EMPTY);
            return out;
        }
        return player.inventory.removeStackFromSlot(index - handler.getSlots());
    }

    @Override
    public void setInventorySlotContents(int index, @NotNull ItemStack stack) {
        if (index < handler.getSlots()) {
            handler.setStackInSlot(index, stack);
        } else {
            player.inventory.setInventorySlotContents(index - handler.getSlots(), stack);
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, @NotNull ItemStack stack) {
        if (index < handler.getSlots()) {
            return handler.isItemValidForSlot(index, stack, player);
        }
        return player.inventory.isItemValidForSlot(index - handler.getSlots(), stack);
    }

    // less important overrides

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUsableByPlayer(@NotNull EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(@NotNull EntityPlayer player) {}

    @Override
    public void closeInventory(@NotNull EntityPlayer player) {}

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {}

    @Override
    public @NotNull String getName() {
        return "GTWrappedBaublesInventory";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public @NotNull ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }
}
