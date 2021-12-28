package gregtech.common.inventory.itemsource.sources;

import gregtech.api.util.ItemStackKey;
import gregtech.common.inventory.itemsource.ItemSource;
import gregtech.common.inventory.itemsource.UpdateResult;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class InventoryItemSource extends ItemSource {

    protected final World world;
    protected final int priority;
    private Runnable invalidationCallback = null;
    private StoredItemsChangeCallback changeCallback = null;
    protected IItemHandler itemHandler = EmptyHandler.INSTANCE;
    private Map<ItemStackKey, Integer> itemStackByAmountMap = new HashMap<>();
    private boolean cachedRefreshResult = false;
    private long lastItemHandlerUpdateTick = -1L;

    public InventoryItemSource(World world, int priority) {
        this.world = world;
        this.priority = priority;
    }

    public static InventoryItemSource direct(World world, IItemHandler itemHandler1, int priority) {
        return new InventoryItemSource(world, priority) {
            @Override
            protected IItemHandler computeItemHandler() {
                return itemHandler1;
            }
        };
    }

    protected abstract IItemHandler computeItemHandler();

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setInvalidationCallback(Runnable invalidatedRunnable) {
        this.invalidationCallback = invalidatedRunnable;
    }

    @Override
    public void setStoredItemsChangeCallback(StoredItemsChangeCallback callback) {
        this.changeCallback = callback;
    }

    private boolean refreshItemHandler(boolean simulated) {
        IItemHandler newItemHandler = computeItemHandler();
        this.lastItemHandlerUpdateTick = world.getTotalWorldTime();
        if (newItemHandler == null) {
            if (!simulated && invalidationCallback != null) {
                invalidationCallback.run();
            }
            this.cachedRefreshResult = false;
            return false;
        }
        if (!newItemHandler.equals(itemHandler) || newItemHandler.getSlots() != itemHandler.getSlots()) {
            this.itemHandler = newItemHandler;
            if (!simulated) {
                recomputeItemStackCount();
            }
            this.cachedRefreshResult = false;
            return false;
        }
        this.cachedRefreshResult = true;
        return true;
    }

    @Override
    public UpdateResult update() {
        return recomputeItemStackCount() ? UpdateResult.CHANGED : UpdateResult.STANDBY;
    }

    private boolean checkItemHandlerValid(boolean simulated) {
        long currentUpdateTick = world.getTotalWorldTime();
        if (currentUpdateTick != lastItemHandlerUpdateTick) {
            return refreshItemHandler(simulated);
        }
        return cachedRefreshResult;
    }

    /**
     * @return amount of items inserted into the inventory
     */
    public int insertItem(ItemStackKey itemStackKey, int amount, boolean simulate) {
        if (!checkItemHandlerValid(simulate)) {
            return 0;
        }
        int itemsInserted = 0;
        ItemStack itemStack = itemStackKey.getItemStack();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemStack.setCount(amount - itemsInserted);
            ItemStack remainderStack = itemHandler.insertItem(i, itemStack, simulate);
            itemsInserted += (itemStack.getCount() - remainderStack.getCount());
            if (itemsInserted == amount) break;
        }
        if (itemsInserted > 0 && !simulate) {
            recomputeItemStackCount();
        }
        return itemsInserted;
    }

    /**
     * @return amount of items extracted from the inventory
     */
    public int extractItem(ItemStackKey itemStackKey, int amount, boolean simulate) {
        if (!checkItemHandlerValid(simulate)) {
            return 0;
        }
        int itemsExtracted = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;
            if (!itemStackKey.isItemStackEqual(stackInSlot)) continue;
            ItemStack extractedStack = itemHandler.extractItem(i, amount - itemsExtracted, simulate);
            if (!extractedStack.isEmpty()) {
                itemsExtracted += extractedStack.getCount();
            }
            if (itemsExtracted == amount) break;
        }
        if (itemsExtracted > 0 && !simulate) {
            recomputeItemStackCount();
        }
        return itemsExtracted;
    }

    @Override
    public Map<ItemStackKey, Integer> getStoredItems() {
        return Collections.unmodifiableMap(itemStackByAmountMap);
    }

    private boolean recomputeItemStackCount() {
        if (!checkItemHandlerValid(false)) {
            return false;
        }
        HashMap<ItemStackKey, Integer> amountMap = new HashMap<>();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack itemStack = itemHandler.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;
            ItemStackKey stackKey = new ItemStackKey(itemStack);
            amountMap.put(stackKey, amountMap.getOrDefault(stackKey, 0) + itemStack.getCount());
        }
        if (amountMap.equals(itemStackByAmountMap)) {
            return false;
        }
        HashSet<ItemStackKey> removedItems = new HashSet<>(itemStackByAmountMap.keySet());
        removedItems.removeAll(amountMap.keySet());
        this.itemStackByAmountMap = amountMap;
        if (changeCallback != null) {
            changeCallback.onStoredItemsUpdated(amountMap, removedItems);
        }
        return true;
    }
}

