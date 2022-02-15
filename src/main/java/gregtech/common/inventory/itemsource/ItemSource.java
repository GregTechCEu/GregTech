package gregtech.common.inventory.itemsource;

import gregtech.api.util.ItemStackKey;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraftforge.items.IItemHandler;

import java.util.Map;

public abstract class ItemSource {

    public abstract int getPriority();

    public abstract void update();

    public abstract void computeItemHandler();

    /**
     * @return items stored in this inventory
     */
    public abstract Map<ItemStackKey, Integer> getStoredItems();

    /**
     * @return amount of items inserted into the inventory
     */
    public abstract int insertItem(ItemStackKey itemStackKey, int amount, boolean simulate, Object2IntMap<ItemSource> map);

    /**
     * @return amount of items extracted from the inventory
     */
    public abstract int extractItem(ItemStackKey itemStackKey, int amount, boolean simulate, Object2IntMap<ItemSource> map);
}
