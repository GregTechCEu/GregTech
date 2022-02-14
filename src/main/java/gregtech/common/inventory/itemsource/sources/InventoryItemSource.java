package gregtech.common.inventory.itemsource.sources;

import gregtech.api.recipes.KeySharedStack;
import gregtech.api.util.ItemStackKey;
import gregtech.common.inventory.itemsource.ItemSource;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class InventoryItemSource extends ItemSource {

    protected final World world;
    protected final int priority;
    protected IItemHandler itemHandler = EmptyHandler.INSTANCE;
    private Map<ItemStackKey, Integer> itemStackByAmountMap = new LinkedHashMap<>();

    public InventoryItemSource(World world, int priority) {
        this.world = world;
        this.priority = priority;
    }

    public InventoryItemSource(World world, IItemHandler itemHandler1, int priority) {
        this(world, priority);
        this.itemHandler = itemHandler1;
    }

    public void computeItemHandler() {
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void update() {
        recomputeItemStackCount();
    }

    /**
     * @return amount of items inserted into the inventory
     */
    public int insertItem(ItemStackKey itemStackKey, int amount, boolean simulate, Object2IntMap<ItemSource> insertedMap) {
        int itemsInserted = 0;
        if (itemHandler == null) return itemsInserted;
        ItemStack itemStack = itemStackKey.getItemStack();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemStack.setCount(amount - itemsInserted);
            ItemStack remainderStack = itemHandler.insertItem(i, itemStack, simulate);
            itemsInserted += (itemStack.getCount() - remainderStack.getCount());
            if (itemsInserted == amount) break;
        }
        if (itemsInserted > 0 && !simulate) {
            int finalItemsInserted = itemsInserted;
            insertedMap.computeIfPresent(this, (source, count) -> count + finalItemsInserted);
            insertedMap.putIfAbsent(this, amount);
        }
        return itemsInserted;
    }

    /**
     * @return amount of items extracted from the inventory
     */
    public int extractItem(ItemStackKey itemStackKey, int amount, boolean simulate, Object2IntMap<ItemSource> extractedMap) {
        int itemsExtracted = 0;
        if (itemHandler == null) return itemsExtracted;
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
            int finalItemsExtracted = itemsExtracted;
            extractedMap.computeIfPresent(this, (source, count) -> count + finalItemsExtracted);
            extractedMap.putIfAbsent(this, amount);
        }
        return itemsExtracted;
    }

    @Override
    public Map<ItemStackKey, Integer> getStoredItems() {
        return Collections.unmodifiableMap(itemStackByAmountMap);
    }

    private void recomputeItemStackCount() {
        HashMap<ItemStackKey, Integer> amountMap = new LinkedHashMap<>();
        if (itemHandler == null) {
            this.itemStackByAmountMap = amountMap;
            return;
        }
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack itemStack = itemHandler.extractItem(i, Integer.MAX_VALUE, true);
            if (itemStack.isEmpty()) continue;
            ItemStackKey stackKey = KeySharedStack.getRegisteredStack(itemStack);
            amountMap.put(stackKey, amountMap.getOrDefault(stackKey, 0) + itemStack.getCount());
        }
        this.itemStackByAmountMap = amountMap;
    }
}

