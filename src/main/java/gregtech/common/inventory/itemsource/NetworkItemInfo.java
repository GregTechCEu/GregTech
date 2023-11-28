package gregtech.common.inventory.itemsource;

import gregtech.common.inventory.IItemInfo;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.Comparator;

public class NetworkItemInfo implements IItemInfo {

    private final ItemStack itemStack;
    private int totalItemAmount = 0;
    private final Object2IntMap<ItemSource> inventories = new Object2IntAVLTreeMap<>(
            Comparator.comparingInt(ItemSource::getPriority));

    public NetworkItemInfo(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public int getTotalItemAmount() {
        return totalItemAmount;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
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
