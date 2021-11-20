package gregtech.api.util;

import gregtech.api.recipes.KeySharedStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class OverlayedItemHandler {
    private final OverlayedItemHandlerSlot[] originalSlots;
    private OverlayedItemHandlerSlot[] slots;
    private final IItemHandler mirrored;
    private ItemStackKey lastISK;
    private ItemStack lastStack;

    public OverlayedItemHandler(IItemHandler toMirror) {
        this.slots = new OverlayedItemHandlerSlot[toMirror.getSlots()];
        this.originalSlots = new OverlayedItemHandlerSlot[toMirror.getSlots()];
        this.mirrored = toMirror;
        for (int slot = 0; slot < toMirror.getSlots(); slot++) {
            ItemStack stackToMirror = toMirror.getStackInSlot(slot);
            if (!stackToMirror.isEmpty()) {
                this.originalSlots[slot] = new OverlayedItemHandlerSlot(stackToMirror);
                this.slots[slot] = new OverlayedItemHandlerSlot(stackToMirror);
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
                this.slots[slot] = new OverlayedItemHandlerSlot(toInsert, remainingCount);
                lastStack = remainder;
            } else {
                this.slots[slot] = new OverlayedItemHandlerSlot(stack);
                lastStack = null;
            }
            lastISK = toInsert;
            return remainingCount;
        } else {
            lastISK = null;
            return amount;
        }
    }

    private static class OverlayedItemHandlerSlot {
        private final ItemStackKey itemStackKey;
        private final int count;

        OverlayedItemHandlerSlot(ItemStack stackToMirror) {
            this.itemStackKey = KeySharedStack.getRegisteredStack(stackToMirror);
            this.count = stackToMirror.getCount();
        }

        OverlayedItemHandlerSlot(ItemStackKey itemStackKey, int count) {
            this.itemStackKey = itemStackKey;
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public ItemStackKey getItemStackKey() {
            return itemStackKey;
        }
    }
}
