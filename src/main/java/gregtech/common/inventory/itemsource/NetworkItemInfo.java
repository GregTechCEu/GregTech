package gregtech.common.inventory.itemsource;

import gregtech.api.util.ItemStackKey;
import gregtech.common.inventory.IItemInfo;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class NetworkItemInfo implements IItemInfo {

    private final ItemStackKey itemStackKey;
    private int totalItemAmount = 0;
    private final Map<ItemSource, Integer> inventories = new TreeMap<>(Comparator.comparingInt(ItemSource::getPriority));

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

    void addToSource(ItemSource itemSource, int amount) {
        inventories.computeIfPresent(itemSource, (key, value) -> value + amount);
        inventories.putIfAbsent(itemSource, amount);
        totalItemAmount += amount;
    }

    void removeFromSource(ItemSource itemSource, int amount) {
        inventories.computeIfPresent(itemSource, (key, value) -> {
            if (amount >= value) {
                return null;
            } else {
                return value - amount;
            }
        });
        totalItemAmount = (Math.max(0, totalItemAmount - amount));
        if (totalItemAmount == 0) {
            inventories.remove(itemSource);
        }
    }
}
