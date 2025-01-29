package gregtech.common.inventory.handlers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class SingleItemStackHandler extends ItemStackHandler {

    public SingleItemStackHandler(int size) {
        super(size);
    }

    public SingleItemStackHandler(ItemStack itemStack) {
        super(1);
        setStackInSlot(0, itemStack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }
}
