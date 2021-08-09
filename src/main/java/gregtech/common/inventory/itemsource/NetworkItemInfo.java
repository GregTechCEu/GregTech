package gregtech.common.inventory.itemsource;

import gregtech.api.util.ItemStackKey;
import gregtech.common.inventory.IItemInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkItemInfo implements IItemInfo {

    private final ItemStackKey itemStackKey;
    private int totalItemAmount = 0;
    private final Map<ItemSource, Integer> inventories = new ConcurrentHashMap<>();

    public NetworkItemInfo(ItemStackKey itemStackKey) {
        this.itemStackKey = itemStackKey;
    }

    @Override
    public int getTotalItemAmount() {
        return totalItemAmount;
    }

    @Override
    public ItemStackKey getItemStackKey() {
        return itemStackKey;
    }

    int extractItem(int amount, boolean simulate) {
        int amountToExtract = amount;
        for (ItemSource itemSource : inventories.keySet()) {
            amountToExtract -= itemSource.extractItem(itemStackKey, amountToExtract, simulate);
            if (amountToExtract == 0) break;
        }
        int extracted = amount - amountToExtract;
        if (!simulate && extracted > 0) {
            recomputeItemAmount();
        }
        return extracted;
    }

    boolean addInventory(ItemSource inventory, int amount) {
        if (inventories.getOrDefault(inventory, 0) != amount) {
            this.inventories.put(inventory, amount);
            return recomputeItemAmount();
        }
        return false;
    }

    boolean removeInventory(ItemSource inventory) {
        if (inventories.containsKey(inventory)) {
            this.inventories.remove(inventory);
            return recomputeItemAmount();
        }
        return false;
    }

    private boolean recomputeItemAmount() {
        int oldTotalItemAmount = totalItemAmount;
        this.totalItemAmount = inventories.values().stream()
                .mapToInt(Integer::intValue).sum();
        return totalItemAmount != oldTotalItemAmount;
    }
}
