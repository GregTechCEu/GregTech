package gregtech.common.inventory.handlers;


import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class CycleItemStackHandler extends ItemStackHandler {
    private int index;

    public CycleItemStackHandler(NonNullList<ItemStack> stacks) {
        super(stacks);
        index = 0;
    }

    public void update(){
        index = (index + 1) % stacks.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return super.getStackInSlot(index);
    }
}
