package gregtech.api.util;

import gregtech.api.recipes.KeySharedStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class OverlayedItemHandler {
    private final OverlayedItemHandlerSlot[] originalSlots;
    private final OverlayedItemHandlerSlot[] slots;
    private final IItemHandler overlayedHandler;
    private ItemStackKey cachedStackKey;
    private ItemStack cachedStack;

    public OverlayedItemHandler(IItemHandler toOverlay) {
        this.slots = new OverlayedItemHandlerSlot[toOverlay.getSlots()];
        this.originalSlots = new OverlayedItemHandlerSlot[toOverlay.getSlots()];
        this.overlayedHandler = toOverlay;
    }

    /**
     * Resets the {slots} array to the state when the handler was
     * first mirrored
     */

    public void reset() {
        for (int i = 0; i < this.originalSlots.length; i++) {
            if (this.originalSlots[i] != null) {
                this.slots[i] = this.originalSlots[i].copy();
            }
        }
    }

    public int getSlots() {
        return overlayedHandler.getSlots();
    }

    private void initSlot(int slot) {
        if (this.originalSlots[slot] == null) {
            ItemStack stackToMirror = overlayedHandler.getStackInSlot(slot);
            this.originalSlots[slot] = new OverlayedItemHandlerSlot(stackToMirror);
            this.slots[slot] = new OverlayedItemHandlerSlot(stackToMirror);
        }
    }

    public int insertItemStackKey(int slot, @Nonnull ItemStackKey toInsert, int amount) {
        initSlot(slot);

        ItemStack stack = null;
        if (toInsert == cachedStackKey) {
            if (cachedStack != null) {
                stack = cachedStack;
            }
        }

        ItemStack remainder;
        if (this.slots[slot].getItemStackKey() == null || this.slots[slot].getItemStackKey() == toInsert) {
            if (stack == null) {
                stack = toInsert.getItemStack();
            }
            stack.setCount(amount);
            remainder = overlayedHandler.insertItem(slot, stack, true);
            int remainingCount = remainder.getCount();
            if (remainder == stack) {
                cachedStack = remainder;
            } else if (!remainder.isEmpty()) {
                this.slots[slot].setItemStackKey(toInsert);
                this.slots[slot].setCount(amount - remainingCount);
                cachedStack = remainder;
            } else {
                this.slots[slot].setItemStackKey(toInsert);
                this.slots[slot].setCount(amount);
                cachedStack = null;
            }
            cachedStackKey = toInsert;
            return remainingCount;
        } else {
            return amount;
        }
    }

    private static class OverlayedItemHandlerSlot {
        private ItemStackKey itemStackKey = null;
        private int count = 0;

        OverlayedItemHandlerSlot(ItemStack stackToMirror) {
            if (!stackToMirror.isEmpty()) {
                this.itemStackKey = KeySharedStack.getRegisteredStack(stackToMirror);
                this.count = stackToMirror.getCount();
            }
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

        public void setItemStackKey(ItemStackKey itemStackKey) {
            this.itemStackKey = itemStackKey;
        }

        public void setCount(int count) {
            this.count = count;
        }

        OverlayedItemHandlerSlot copy() {
            return new OverlayedItemHandlerSlot(this.itemStackKey, this.count);
        }
    }
}
