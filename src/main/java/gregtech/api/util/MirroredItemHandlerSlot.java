package gregtech.api.util;

import gregtech.api.recipes.KeySharedStack;
import net.minecraft.item.ItemStack;

public class MirroredItemHandlerSlot {
    private final ItemStackKey itemStackKey;
    private final int count;
    private boolean filled;

    MirroredItemHandlerSlot(ItemStack stackToMirror) {
        this.itemStackKey = KeySharedStack.getRegisteredStack(stackToMirror);
        this.count = stackToMirror.getCount();
    }

    MirroredItemHandlerSlot(ItemStackKey itemStackKey, int count) {
        this.itemStackKey = itemStackKey;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public ItemStackKey getItemStackKey() {
        return itemStackKey;
    }

    public void setFilled(){
        this.filled = true;
    }
}
