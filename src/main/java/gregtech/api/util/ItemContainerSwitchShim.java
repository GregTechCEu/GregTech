package gregtech.api.util;

import codechicken.lib.inventory.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ItemContainerSwitchShim implements IItemHandlerModifiable {

    IItemHandlerModifiable container;

    public ItemContainerSwitchShim(IItemHandlerModifiable container) {
        changeInventory(container);
    }

    public void changeInventory(IItemHandlerModifiable container) {
        if (container == null) {
            throw new IllegalArgumentException("Shim container must be an IItemHandler!");
        }
        this.container = container;
    }

    @Override
    public int getSlots() {
        return this.container.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.container.getStackInSlot((slot));
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack itemStack, boolean simulate) {
        return this.container.insertItem(slot, itemStack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.container.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.container.getSlotLimit(slot);
    }

    @Override
    public void setStackInSlot(int i, @Nonnull ItemStack itemStack) {
        this.container.setStackInSlot(i, itemStack);
    }
}
