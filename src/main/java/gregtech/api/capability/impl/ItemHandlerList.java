package gregtech.api.capability.impl;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Efficiently delegates calls into multiple item handlers
 */
public class ItemHandlerList implements IItemHandlerModifiable {

    private final TIntObjectMap<IItemHandler> handlerBySlotIndex = new TIntObjectHashMap<>();
    private final Map<IItemHandler, Integer> baseIndexOffset = new IdentityHashMap<>();

    public ItemHandlerList(List<? extends IItemHandler> itemHandlerList) {
        int currentSlotIndex = 0;
        for (IItemHandler itemHandler : itemHandlerList) {
            if (baseIndexOffset.containsKey(itemHandler)) {
                throw new IllegalArgumentException("Attempted to add item handler " + itemHandler + " twice");
            }
            baseIndexOffset.put(itemHandler, currentSlotIndex);
            int slotsCount = itemHandler.getSlots();
            for (int slotIndex = 0; slotIndex < slotsCount; slotIndex++) {
                handlerBySlotIndex.put(currentSlotIndex + slotIndex, itemHandler);
            }
            currentSlotIndex += slotsCount;
        }
    }

    @Override
    public int getSlots() {
        return handlerBySlotIndex.size();
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        if (!(itemHandler instanceof IItemHandlerModifiable))
            throw new UnsupportedOperationException("Handler " + itemHandler + " does not support this method");
        ((IItemHandlerModifiable) itemHandler).setStackInSlot(slot - baseIndexOffset.get(itemHandler), stack);
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        int realSlot = slot - baseIndexOffset.get(itemHandler);
        return itemHandler.getStackInSlot(slot - baseIndexOffset.get(itemHandler));
    }

    @Override
    public int getSlotLimit(int slot) {
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.getSlotLimit(slot - baseIndexOffset.get(itemHandler));
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.insertItem(slot - baseIndexOffset.get(itemHandler), stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.extractItem(slot - baseIndexOffset.get(itemHandler), amount, simulate);
    }

}
