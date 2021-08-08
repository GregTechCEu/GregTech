package gregtech.common.inventory.handlers;

import net.minecraftforge.items.ItemStackHandler;

public class SingleItemStackHandler extends ItemStackHandler {

    public SingleItemStackHandler(int size) {
        super(size);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }
}
