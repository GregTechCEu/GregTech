package gregtech.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Efficiently delegates calls into multiple item handlers
 */
public class ItemHandlerList extends AbstractList<IItemHandler> implements IItemHandlerModifiable {

    private final Int2ObjectMap<IItemHandler> handlerBySlotIndex = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<IItemHandler> baseIndexOffset = new Object2IntArrayMap<>();

    public ItemHandlerList() {}

    public ItemHandlerList(Collection<? extends IItemHandler> itemHandlerList) {
        addAll(itemHandlerList);
    }

    public ItemHandlerList(ItemHandlerList parent, IItemHandler... additional) {
        addAll(parent.getBackingHandlers());
        Collections.addAll(this, additional);
    }

    public int getIndexOffset(IItemHandler handler) {
        return baseIndexOffset.getOrDefault(handler, -1);
    }

    @Override
    public int getSlots() {
        return handlerBySlotIndex.size();
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

    private boolean invalidSlot(int slot) {
        if (handlerBySlotIndex.isEmpty()) return false;
        return slot < 0 || slot >= handlerBySlotIndex.size();
    }

    @NotNull
    public Collection<IItemHandler> getBackingHandlers() {
        return Collections.unmodifiableCollection(baseIndexOffset.keySet());
    }

    @Override
    public int size() {
        return baseIndexOffset.size();
    }

    @Override
    public boolean add(IItemHandler handler) {
        int s = size();
        add(s, handler);
        return s != size();
    }

    @Override
    public void add(int unused, IItemHandler element) {
        Objects.requireNonNull(element);
        if (baseIndexOffset.containsKey(element)) {
            throw new IllegalArgumentException("Attempted to add item handler " + element + " twice");
        }
        if (element instanceof ItemHandlerList list) {
            // possible infinite recursion
            // throw instead?
            addAll(list);
            return;
        }

        int offset = handlerBySlotIndex.size();
        baseIndexOffset.put(element, offset);
        for (int slotIndex = 0; slotIndex < element.getSlots(); slotIndex++) {
            handlerBySlotIndex.put(offset + slotIndex, element);
        }
    }

    @Override
    public IItemHandler get(int index) {
        if (invalidIndex(index)) throw new IndexOutOfBoundsException();
        ObjectIterator<IItemHandler> itr = baseIndexOffset.keySet().iterator();
        itr.skip(index); // skip n-1 elements
        return itr.next(); // get nth element
    }

    @Override
    public IItemHandler remove(int index) {
        if (invalidIndex(index)) {
            throw new IndexOutOfBoundsException();
        }

        IItemHandler handler = get(index);

        // remove handler
        int lower = baseIndexOffset.removeInt(handler);

        // remove slot indices
        int upper = lower + handler.getSlots();
        for (int i = lower; i < upper; i++) {
            handlerBySlotIndex.remove(i);
        }

        // update slot indices ahead of the removed handler
        for (int slot = upper; slot < getSlots() + handler.getSlots(); slot++) {
            IItemHandler remove = handlerBySlotIndex.remove(slot);
            handlerBySlotIndex.put(slot - upper, remove);
        }

        // update handlers ahead of the removed handler
        for (IItemHandler h : baseIndexOffset.keySet()) {
            int offset = baseIndexOffset.getInt(h);
            if (offset > lower) {
                baseIndexOffset.put(h, offset - handler.getSlots());
            }
        }

        return handler;
    }

    public boolean invalidIndex(int index) {
        if (baseIndexOffset.isEmpty()) return false;
        return index < 0 || index >= baseIndexOffset.size();
    }
}
