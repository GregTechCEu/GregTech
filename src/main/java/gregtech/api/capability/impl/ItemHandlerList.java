package gregtech.api.capability.impl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Efficiently delegates calls into multiple item handlers
 */
public class ItemHandlerList extends AbstractList<IItemHandler> implements IItemHandlerModifiable {

    private final Int2ObjectMap<IItemHandler> handlerBySlotIndex = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<IItemHandler> baseIndexOffset = new Object2IntArrayMap<>();

    private final List<IItemHandler> handlerList = new ArrayList<>();

    public ItemHandlerList(List<? extends IItemHandler> itemHandlerList) {
        addAll(itemHandlerList);
    }

    public int getIndexOffset(IItemHandler handler) {
        return baseIndexOffset.getOrDefault(handler, -1);
    }

    @Override
    public int getSlots() {
        return size();
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (invalidSlot(slot)) return;
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        int actualSlot = slot - baseIndexOffset.get(itemHandler);
        if (itemHandler instanceof IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(actualSlot, stack);
        } else {
            itemHandler.extractItem(actualSlot, Integer.MAX_VALUE, false);
            itemHandler.insertItem(actualSlot, stack, false);
        }
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (invalidSlot(slot)) return ItemStack.EMPTY;
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.getStackInSlot(slot - baseIndexOffset.getInt(itemHandler));
    }

    @Override
    public int getSlotLimit(int slot) {
        if (invalidSlot(slot)) return 0;
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.getSlotLimit(slot - baseIndexOffset.getInt(itemHandler));
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (invalidSlot(slot)) return stack;
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.insertItem(slot - baseIndexOffset.getInt(itemHandler), stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (invalidSlot(slot)) return ItemStack.EMPTY;
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.extractItem(slot - baseIndexOffset.getInt(itemHandler), amount, simulate);
    }

    @NotNull
    public Collection<IItemHandler> getBackingHandlers() {
        return Collections.unmodifiableCollection(handlerList);
    }

    @Override
    public int size() {
        return handlerList.size();
    }

    @Override
    public boolean add(IItemHandler handler) {
        int s = size();
        add(s, handler);
        return s != size();
    }

    @Override
    public void add(int index, IItemHandler element) {
        if (invalidIndex(index)) return;
        int currentSlotIndex = handlerBySlotIndex.size();
        if (baseIndexOffset.containsKey(element)) {
            throw new IllegalArgumentException("Attempted to add item handler " + element + " twice");
        }
        handlerList.add(element);
        baseIndexOffset.put(element, currentSlotIndex);
        for (int slotIndex = 0; slotIndex < element.getSlots(); slotIndex++) {
            handlerBySlotIndex.put(currentSlotIndex + slotIndex, element);
        }
    }

    @Override
    public IItemHandler get(int index) {
        return handlerList.get(index);
    }

    @Override
    public IItemHandler remove(int index) {
        if (invalidIndex(index)) return null;
        var handler = get(index);
        for (int i = index; i < size(); i++) {
            int offset = baseIndexOffset.getInt(get(i));
            for (int j = 0; j < get(index).getSlots(); j++) {
                handlerBySlotIndex.remove(offset + j);
            }
            baseIndexOffset.removeInt(handler);
        }
        return handler;
    }

    private boolean invalidIndex(int index) {
        return index < 0 || index >= handlerList.size();
    }
}
