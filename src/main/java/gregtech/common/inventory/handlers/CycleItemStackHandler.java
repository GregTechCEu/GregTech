package gregtech.common.inventory.handlers;


import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class CycleItemStackHandler extends ItemStackHandler {

    public CycleItemStackHandler(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return stacks.isEmpty() ? ItemStack.EMPTY : super.getStackInSlot(Math.abs((int)(System.currentTimeMillis() / 1000) % stacks.size()));
    }
}
