package gregtech.api.util;

import gregtech.api.recipes.KeySharedStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class OverlayedItemHandler {
    private final OverlayedItemHandlerSlot[] originalSlots;
    private final OverlayedItemHandlerSlot[] slots;
    private final IItemHandler overlayedHandler;

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

    /**
     * Populates the {@code originalSlots} and {@code slots}arrays with the current state of the inventory.
     *
     * @param slot the slot to populate
     */


    private void initSlot(int slot) {
        if (this.originalSlots[slot] == null) {
            ItemStack stackToMirror = overlayedHandler.getStackInSlot(slot);
            int slotLimit = overlayedHandler.getSlotLimit(slot);
            this.originalSlots[slot] = new OverlayedItemHandlerSlot(stackToMirror, slotLimit);
            this.slots[slot] = new OverlayedItemHandlerSlot(stackToMirror, slotLimit);
        }
    }


    public int insertStackedItemStackKey(ItemStackKey key, int amountToInsert) {
        //loop through all slots, looking for ones matching the key
        for (int i = 0; i < this.slots.length; i++) {
            //populate the slot if it's not already populated
            initSlot(i);
            //if its the same item
            if (this.slots[i].getItemStackKey() == key) {
                //if the slot its not full
                int canInsertUpTo = this.slots[i].slotLimit - this.slots[i].count;
                if (canInsertUpTo > 0) {
                    int insertedAmount = Math.min(canInsertUpTo, amountToInsert);
                    this.slots[i].setItemStackKey(key);
                    this.slots[i].setCount(this.slots[i].getCount() + insertedAmount);
                    amountToInsert -= insertedAmount;
                }
            }
        }
        //if the amountToInsert is still greater than 0, we need to insert it into a new slot
        if (amountToInsert > 0) {
            //loop through all slots, again, looking for empty ones.
            for (int i = 0; i < this.slots.length; i++) {
                OverlayedItemHandlerSlot slot = this.slots[i];
                //if the slot is empty
                if (slot.getItemStackKey() == null) {
                    int canInsertUpTo = Math.min(key.getMaxStackSize(), slot.slotLimit);
                    if (canInsertUpTo > 0) {
                        int insertedAmount = Math.min(canInsertUpTo, amountToInsert);
                        slot.setItemStackKey(key);
                        slot.setCount(insertedAmount);
                        amountToInsert -= insertedAmount;
                    }
                    if (amountToInsert == 0) {
                        return 0;
                    }
                }
            }
        }
        //return the amount that wasn't inserted
        return amountToInsert;
    }

    private static class OverlayedItemHandlerSlot {
        private ItemStackKey itemStackKey = null;
        private int count = 0;
        private int slotLimit = 0;

        OverlayedItemHandlerSlot(ItemStack stackToMirror, int slotLimit) {
            if (!stackToMirror.isEmpty()) {
                this.itemStackKey = KeySharedStack.getRegisteredStack(stackToMirror);
                this.count = stackToMirror.getCount();
                this.slotLimit = Math.min(itemStackKey.getMaxStackSize(), slotLimit);
            } else {
                this.slotLimit = slotLimit;
            }
        }

        OverlayedItemHandlerSlot(ItemStackKey itemStackKey, int slotLimit, int count) {
            this.itemStackKey = itemStackKey;
            this.count = count;
            this.slotLimit = slotLimit;
        }

        public int getSlotLimit() {
            return slotLimit;
        }

        public int getCount() {
            return count;
        }

        public ItemStackKey getItemStackKey() {
            return itemStackKey;
        }

        public void setItemStackKey(ItemStackKey itemStackKey) {
            if (this.itemStackKey != itemStackKey) {
                this.itemStackKey = itemStackKey;
                this.slotLimit = Math.min(itemStackKey.getMaxStackSize(), slotLimit);
            }
        }

        public void setCount(int count) {
            this.count = count;
        }

        OverlayedItemHandlerSlot copy() {
            return new OverlayedItemHandlerSlot(this.itemStackKey, this.slotLimit, this.count);
        }
    }
}
