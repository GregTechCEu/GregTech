package gregtech.common.inventory.itemsource;

import gregtech.api.util.ItemStackKey;

import java.util.Map;
import java.util.Set;

public abstract class ItemSource {

    public abstract int getPriority();

    public abstract void setInvalidationCallback(Runnable invalidatedRunnable);

    public abstract void setStoredItemsChangeCallback(StoredItemsChangeCallback callback);

    public abstract UpdateResult update();

    /**
     * @return items stored in this inventory
     */
    public abstract Map<ItemStackKey, Integer> getStoredItems();

    /**
     * @return amount of items inserted into the inventory
     */
    public abstract int insertItem(ItemStackKey itemStackKey, int amount, boolean simulate);

    /**
     * @return amount of items extracted from the inventory
     */
    public abstract int extractItem(ItemStackKey itemStackKey, int amount, boolean simulate);

    @FunctionalInterface
    public interface StoredItemsChangeCallback {
        void onStoredItemsUpdated(Map<ItemStackKey, Integer> itemAmount, Set<ItemStackKey> removedItems);
    }
}
