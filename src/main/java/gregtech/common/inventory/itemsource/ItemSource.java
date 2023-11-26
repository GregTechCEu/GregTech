package gregtech.common.inventory.itemsource;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

public abstract class ItemSource {

    public abstract int getPriority();

    public abstract void update();

    public abstract void computeItemHandler();

    /**
     * @return items stored in this inventory
     */
    public abstract Object2IntMap<ItemStack> getStoredItems();

    /**
     * @return amount of items inserted into the inventory
     */
    public abstract int insertItem(ItemStack itemStack, int amount, boolean simulate, Object2IntMap<ItemSource> map);

    /**
     * @return amount of items extracted from the inventory
     */
    public abstract int extractItem(ItemStack itemStack, int amount, boolean simulate, Object2IntMap<ItemSource> map);
}
