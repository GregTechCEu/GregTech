package gregtech.api.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class MirroredItemHandler {
    private final MirroredItemHandlerSlot[] originalSlots;
    private MirroredItemHandlerSlot[] slots;
    private final IItemHandler mirrored;
    private ItemStackKey lastISK;
    private ItemStack lastStack;

    public MirroredItemHandler(IItemHandler toMirror) {
        this.slots = new MirroredItemHandlerSlot[toMirror.getSlots()];
        this.originalSlots = new MirroredItemHandlerSlot[toMirror.getSlots()];
        this.mirrored = toMirror;
        for (int slot = 0; slot < toMirror.getSlots(); slot++) {
            ItemStack stackToMirror = toMirror.getStackInSlot(slot);
            if (!stackToMirror.isEmpty()) {
                this.originalSlots[slot] = new MirroredItemHandlerSlot(stackToMirror);
                this.slots[slot] = new MirroredItemHandlerSlot(stackToMirror);
            }
        }
    }

    /**
     * Resets the {slots} array to the state when the handler was
     * first mirrored
     */

    public void reset() {
        this.slots = originalSlots.clone();
    }

    public int getSlots() {
        return mirrored.getSlots();
    }

    public int insertItemStackKey(int slot, @Nonnull ItemStackKey toInsert, int amount) {
        ItemStack stack = null;
        if (toInsert == lastISK) {
            if (lastStack != null) {
                stack = lastStack;
            }
        }

        ItemStack remainder;
        if (this.slots[slot] == null || this.slots[slot].getItemStackKey() == toInsert) {
            if (stack == null) {
                stack = toInsert.getItemStack();
            }
            stack.setCount(amount);
            remainder = mirrored.insertItem(slot, stack, true);
            int remainingCount = remainder.getCount();
            if (!remainder.isEmpty()) {
                this.slots[slot] = new MirroredItemHandlerSlot(toInsert, remainingCount);
                if (remainingCount == amount){
                    this.slots[slot].setFilled();
                }
                lastStack = remainder;
            } else {
                this.slots[slot] = new MirroredItemHandlerSlot(stack);
                lastStack = null;
            }
            lastISK = toInsert;
            return remainingCount;
        } else {
            lastISK = null;
            return amount;
        }
    }
}
