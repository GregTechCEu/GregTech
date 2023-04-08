package gregtech.api.util.enderlink;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class ItemContainerSwitchShim extends SwitchShimBase implements IItemHandlerModifiable {

    IItemHandlerModifiable container;

    public ItemContainerSwitchShim(IItemHandler container) {
        super(container);
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
