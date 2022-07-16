package gregtech.api.util;

import codechicken.lib.inventory.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ItemContainerSwitchShim implements IItemHandler {

    IItemHandlerModifiable container;

    public ItemContainerSwitchShim(IItemHandlerModifiable container) {
        changeInventory(container);
    }

    public void changeInventory(IItemHandler container) {
        if (container == null) {
            throw new IllegalArgumentException("Shim container must be an IItemHandler!");
        }
        this.container = (IItemHandlerModifiable) container;
        GTLog.logger.warn("container switch shim has changed!");
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

    public IItemHandler GetContainer(){
        return this.container;
    }
    /*
    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack itemStack) {
        this.container.setStackInSlot(slot, itemStack);
    }
*/
}
