package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;

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
public class ItemHandlerList extends AbstractList<IItemHandler> implements IItemHandlerModifiable, INotifiableHandler {

    protected final Int2ObjectMap<IItemHandler> handlerBySlotIndex = new Int2ObjectOpenHashMap<>();
    protected final Object2IntMap<IItemHandler> baseIndexOffset = new Object2IntArrayMap<>();

    // this is only used for get()
    protected IItemHandler[] handlers = new IItemHandler[0];

    public ItemHandlerList(Collection<? extends IItemHandler> itemHandlerList) {
        addAll(itemHandlerList);
        baseIndexOffset.defaultReturnValue(-1);
    }

    public ItemHandlerList() {
        this(Collections.emptyList());
    }

    public static ItemHandlerList of(IItemHandler... handlers) {
        ItemHandlerList list = new ItemHandlerList();
        if (handlers != null && handlers.length > 0) {
            Collections.addAll(list, handlers);
        }
        return list.toImmutable();
    }

    /**
     * @param handler the handler to get the slot offset of
     * @return the slot offset, or {@code -1} if the handler does not exist
     */
    public int getIndexOffset(IItemHandler handler) {
        return baseIndexOffset.get(handler);
    }

    @NotNull
    protected IItemHandler getHandlerBySlot(int slot) {
        if (invalidSlot(slot)) throw new IndexOutOfBoundsException();
        return handlerBySlotIndex.get(slot);
    }

    protected int getInternalSlot(int slot) {
        return slot - getIndexOffset(getHandlerBySlot(slot));
    }

    @Override
    public int getSlots() {
        return handlerBySlotIndex.size();
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (invalidSlot(slot)) return;
        IItemHandler itemHandler = getHandlerBySlot(slot);
        int actualSlot = getInternalSlot(slot);
        if (itemHandler instanceof IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(actualSlot, stack);
        } else {
            // should this no-op instead?
            itemHandler.extractItem(actualSlot, Integer.MAX_VALUE, false);
            itemHandler.insertItem(actualSlot, stack, false);
        }
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (invalidSlot(slot)) return ItemStack.EMPTY;
        return getHandlerBySlot(slot).getStackInSlot(getInternalSlot(slot));
    }

    @Override
    public int getSlotLimit(int slot) {
        if (invalidSlot(slot)) return 0;
        return getHandlerBySlot(slot).getSlotLimit(getInternalSlot(slot));
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (invalidSlot(slot)) return stack;
        return getHandlerBySlot(slot).insertItem(getInternalSlot(slot), stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (invalidSlot(slot)) return ItemStack.EMPTY;
        return getHandlerBySlot(slot).extractItem(getInternalSlot(slot), amount, simulate);
    }

    @NotNull
    public Collection<IItemHandler> getBackingHandlers() {
        return Collections.unmodifiableCollection(baseIndexOffset.keySet());
    }

    @Override
    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        for (IItemHandler handler : this) {
            if (handler instanceof INotifiableHandler notifiableHandler) {
                notifiableHandler.addNotifiableMetaTileEntity(metaTileEntity);
            }
        }
    }

    @Override
    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        for (IItemHandler handler : this) {
            if (handler instanceof INotifiableHandler notifiableHandler) {
                notifiableHandler.removeNotifiableMetaTileEntity(metaTileEntity);
            }
        }
    }

    @Override
    public int size() {
        return baseIndexOffset.size();
    }

    @Override
    public boolean add(IItemHandler handler) {
        if (this == handler) {
            throw new IllegalArgumentException("Cannot add a handler list to itself!");
        }
        int s = size();
        if (handler instanceof ItemHandlerList list) {
            addAll(s, list);
        } else {
            add(s, handler);
        }
        return s != size();
    }

    @Override
    public void add(int unused, @NotNull IItemHandler element) {
        Objects.requireNonNull(element);
        if (baseIndexOffset.containsKey(element)) {
            throw new IllegalArgumentException("Attempted to add item handler " + element + " twice");
        }

        int offset = getSlots();
        baseIndexOffset.put(element, offset);
        for (int slotIndex = 0; slotIndex < element.getSlots(); slotIndex++) {
            handlerBySlotIndex.put(offset + slotIndex, element);
        }
    }

    @Override
    public @NotNull Iterator<IItemHandler> iterator() {
        return baseIndexOffset.keySet().iterator();
    }

    @Override
    public IItemHandler get(int index) {
        if (invalidIndex(index)) throw new IndexOutOfBoundsException();
        updateHandlerArray();
        return handlers[index];
    }

    @Override
    public IItemHandler remove(int index) {
        if (invalidIndex(index)) throw new IndexOutOfBoundsException();

        IItemHandler removed = get(index);
        int removedSlots = removed.getSlots();

        // remove handler
        int lower = baseIndexOffset.removeInt(removed);

        // update slot indices ahead of the removed handler and
        // remove slot indices
        int upper = lower + removedSlots;
        int size = getSlots(); // slot size will be mutated
        for (int i = lower; i < size; i++) {
            if (i < upper) {
                handlerBySlotIndex.put(i, getHandlerBySlot(i + removedSlots));
            } else {
                handlerBySlotIndex.remove(i);
            }
        }

        // update handlers ahead of the removed handler
        for (IItemHandler h : this) {
            int offset = getIndexOffset(h);
            if (offset > lower) {
                baseIndexOffset.put(h, offset - removedSlots);
            }
        }

        return removed;
    }

    @Override
    public int indexOf(@NotNull Object o) {
        for (int i = 0; i < size(); i++) {
            if (Objects.equals(o, get(i)))
                return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(@NotNull Object o) {
        for (int i = size() - 1; i >= 0; i--) {
            if (Objects.equals(o, get(i)))
                return i;
        }
        return -1;
    }

    private boolean invalidSlot(int slot) {
        return slot < 0 || slot >= handlerBySlotIndex.size();
    }

    private boolean invalidIndex(int index) {
        return index < 0 || index >= baseIndexOffset.size();
    }

    private void updateHandlerArray() {
        if (handlers.length != size()) {
            handlers = new IItemHandler[size()];
            int i = 0;
            for (IItemHandler h : baseIndexOffset.keySet()) {
                handlers[i++] = h;
            }
        }
    }

    public ItemHandlerList toImmutable() {
        return new Immutable(this);
    }

    private static class Immutable extends ItemHandlerList {

        private Immutable(ItemHandlerList list) {
            this.handlers = list.handlers;
            this.baseIndexOffset.putAll(list.baseIndexOffset);
            this.handlerBySlotIndex.putAll(list.handlerBySlotIndex);
        }

        @Override
        public void add(int unused, @NotNull IItemHandler element) {
            // no op?
            throw new UnsupportedOperationException();
        }

        @Override
        public IItemHandler remove(int index) {
            // no op?
            throw new UnsupportedOperationException();
        }

        @Override
        public IItemHandler get(int index) {
            return handlers[index];
        }
    }
}
