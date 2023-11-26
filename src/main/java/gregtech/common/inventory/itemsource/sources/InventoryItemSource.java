package gregtech.common.inventory.itemsource.sources;

import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.inventory.itemsource.ItemSource;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;

public class InventoryItemSource extends ItemSource {

    protected final World world;
    protected final int priority;
    protected IItemHandler itemHandler = EmptyHandler.INSTANCE;
    private Object2IntMap<ItemStack> itemStackByAmountMap = new Object2IntLinkedOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());

    public InventoryItemSource(World world, int priority) {
        this.world = world;
        this.priority = priority;
    }

    public InventoryItemSource(World world, IItemHandler itemHandler1, int priority) {
        this(world, priority);
        this.itemHandler = itemHandler1;
    }

    public void computeItemHandler() {}

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
    public int insertItem(ItemStack stack, int amount, boolean simulate, Object2IntMap<ItemSource> insertedMap) {
        int itemsInserted = 0;
        if (itemHandler == null) return itemsInserted;
        ItemStack itemStack = stack.copy();
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
    public int extractItem(ItemStack stack, int amount, boolean simulate, Object2IntMap<ItemSource> extractedMap) {
        int itemsExtracted = 0;
        if (itemHandler == null) return itemsExtracted;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;
            if (!ItemStackHashStrategy.comparingAllButCount().equals(stack, stackInSlot)) continue;
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
    public Object2IntMap<ItemStack> getStoredItems() {
        return Object2IntMaps.unmodifiable(itemStackByAmountMap);
    }

    private void recomputeItemStackCount() {
        Object2IntMap<ItemStack> amountMap = new Object2IntLinkedOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        if (itemHandler == null) {
            this.itemStackByAmountMap = amountMap;
            return;
        }
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack itemStack = itemHandler.extractItem(i, Integer.MAX_VALUE, true);
            if (itemStack.isEmpty()) continue;
            amountMap.put(itemStack, amountMap.getInt(itemStack) + itemStack.getCount());
        }
        this.itemStackByAmountMap = amountMap;
    }
}
