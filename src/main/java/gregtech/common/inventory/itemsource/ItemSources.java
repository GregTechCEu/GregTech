package gregtech.common.inventory.itemsource;

import gregtech.api.util.ItemStackKey;
import gregtech.common.inventory.IItemInfo;
import gregtech.common.inventory.IItemList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class ItemSources implements IItemList {

    protected final World world;
    protected final List<ItemSource> handlerInfoList = new ArrayList<>();
    protected final Map<ItemStackKey, NetworkItemInfo> itemInfoMap = new LinkedHashMap<>();
    private final Comparator<ItemSource> comparator = Comparator.comparing(ItemSource::getPriority);
    private final Set<ItemStackKey> storedItemsView = Collections.unmodifiableSet(itemInfoMap.keySet());

    public ItemSources(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public Set<ItemStackKey> getStoredItems() {
        return storedItemsView;
    }

    @Nullable
    @Override
    public IItemInfo getItemInfo(ItemStackKey stackKey) {
        return itemInfoMap.get(stackKey);
    }

    public void update() {
        itemInfoMap.clear();
        this.handlerInfoList.forEach(is -> {
            is.computeItemHandler();
            is.update();
        });
        this.handlerInfoList.forEach(source -> {
            source.getStoredItems().forEach((stackKey, amount) -> {
                itemInfoMap.putIfAbsent(stackKey, new NetworkItemInfo(stackKey));
                itemInfoMap.computeIfPresent(stackKey, (stackKey1, itemInfo) -> {
                    itemInfo.addToSource(source, source.getStoredItems().get(stackKey));
                    return itemInfo;
                });
            });
        });
    }

    @Override
    public int insertItem(ItemStackKey itemStack, int amount, boolean simulate, InsertMode insertMode) {
        Object2IntMap<ItemSource> itemSourceMap = new Object2IntOpenHashMap<>();
        int amountToInsert = amount;
        if (insertMode == InsertMode.HIGHEST_PRIORITY) {
            for (ItemSource itemSource : handlerInfoList) {
                int inserted = itemSource.insertItem(itemStack, amountToInsert, simulate, itemSourceMap);
                amountToInsert -= inserted;
                if (amountToInsert == 0) break;
            }
        } else {
            for (int i = handlerInfoList.size() - 1; i >= 0; i--) {
                ItemSource itemSource = handlerInfoList.get(i);
                int inserted = itemSource.insertItem(itemStack, amountToInsert, simulate, itemSourceMap);
                amountToInsert -= inserted;
                if (amountToInsert == 0) break;
            }
        }
        if (!simulate) {
            for (ItemSource itemSource : handlerInfoList) {
                if (itemSourceMap.get(itemSource) != null) {
                    itemInfoMap.putIfAbsent(itemStack, new NetworkItemInfo(itemStack));
                    itemInfoMap.get(itemStack).addToSource(itemSource, itemSourceMap.get(itemSource));
                }
            }
        }
        return amount - amountToInsert;
    }

    @Override
    public int extractItem(ItemStackKey itemStack, int amount, boolean simulate) {
        Object2IntMap<ItemSource> itemSourceMap = new Object2IntOpenHashMap<>();
        int totalExtracted = 0;
        for (ItemSource itemSource : handlerInfoList) {
            int extractedAmount = 0;
            if (itemInfoMap.get(itemStack) != null && itemInfoMap.get(itemStack).getTotalItemAmount() > 0) {
                extractedAmount += itemSource.extractItem(itemStack, amount, simulate, itemSourceMap);
                amount -= extractedAmount;
                totalExtracted += extractedAmount;
                if (!simulate && extractedAmount > 0) {
                    itemInfoMap.get(itemStack).removeFromSource(itemSource, extractedAmount);
                }
            }
            if (amount == 0) break;
        }
        return totalExtracted;
    }

    public void notifyPriorityUpdated() {
        this.handlerInfoList.sort(comparator);
    }

    public void addItemHandler(ItemSource handlerInfo) {
        if (!handlerInfoList.contains(handlerInfo)) {
            this.handlerInfoList.add(handlerInfo);
            handlerInfo.computeItemHandler();
            notifyPriorityUpdated();
        }
    }
}
