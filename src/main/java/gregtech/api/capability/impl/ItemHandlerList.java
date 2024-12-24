package gregtech.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * Efficiently delegates calls into multiple item handlers
 */
public class ItemHandlerList implements IItemHandlerModifiable {

    private final Int2ObjectMap<IItemHandler> handlerBySlotIndex = new Int2ObjectOpenHashMap<>();
    private final Reference2IntOpenHashMap<IItemHandler> baseIndexOffset = new Reference2IntOpenHashMap<>();

    public ItemHandlerList(Collection<? extends IItemHandler> itemHandlerList) {
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
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        IItemHandler itemHandler = getHandlerBySlot(slot);
        if (!(itemHandler instanceof IItemHandlerModifiable))
            throw new UnsupportedOperationException("Handler " + itemHandler + " does not support this method");
        ((IItemHandlerModifiable) itemHandler).setStackInSlot(slot - getOffsetByHandler(itemHandler), stack);
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        IItemHandler itemHandler = getHandlerBySlot(slot);
        return itemHandler.getStackInSlot(slot - getOffsetByHandler(itemHandler));
    }

    @Override
    public int getSlotLimit(int slot) {
        IItemHandler itemHandler = getHandlerBySlot(slot);
        return itemHandler.getSlotLimit(slot - getOffsetByHandler(itemHandler));
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        IItemHandler itemHandler = getHandlerBySlot(slot);
        return itemHandler.insertItem(slot - getOffsetByHandler(itemHandler), stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        IItemHandler itemHandler = getHandlerBySlot(slot);
        return itemHandler.extractItem(slot - getOffsetByHandler(itemHandler), amount, simulate);
    }

    @NotNull
    @UnmodifiableView
    public Collection<IItemHandler> getBackingHandlers() {
        return handlerBySlotIndex.values();
    }

    public IItemHandler getHandlerBySlot(int slot) {
        return handlerBySlotIndex.get(slot);
    }

    public int getOffsetByHandler(IItemHandler handler) {
        return baseIndexOffset.getInt(handler);
    }
}
