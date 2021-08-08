package gregtech.common.inventory.itemsource;

import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackKey;
import gregtech.common.inventory.IItemInfo;
import gregtech.common.inventory.IItemList;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ItemSourceList implements IItemList, ITickable {

    protected final World world;
    protected final List<ItemSource> handlerInfoList = new CopyOnWriteArrayList<>();
    protected final Map<ItemStackKey, NetworkItemInfo> itemInfoMap = new LinkedHashMap<>();
    private final Comparator<ItemSource> comparator = Comparator.comparing(ItemSource::getPriority);
    private final Set<ItemStackKey> storedItemsView = Collections.unmodifiableSet(itemInfoMap.keySet());
    protected Runnable itemListChangeCallback = null;
    private boolean callbackWasCalled = false;
    private boolean disableCallback = false;

    public ItemSourceList(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public void addItemListChangeCallback(Runnable changeCallback) {
        this.itemListChangeCallback = GTUtility.combine(itemListChangeCallback, changeCallback);
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

    public void disableCallback() {
        this.disableCallback = true;
        this.callbackWasCalled = false;
    }

    public void enableCallback() {
        this.disableCallback = false;
        if (callbackWasCalled && itemListChangeCallback != null) {
            this.callbackWasCalled = false;
            itemListChangeCallback.run();
        }
    }

    @Override
    public void update() {
        this.handlerInfoList.forEach(ItemSource::update);
    }

    @Override
    public int insertItem(ItemStackKey itemStack, int amount, boolean simulate, InsertMode insertMode) {
        int amountToInsert = amount;
        if (insertMode == InsertMode.HIGHEST_PRIORITY) {
            for (ItemSource itemSource : handlerInfoList) {
                int inserted = itemSource.insertItem(itemStack, amountToInsert, simulate);
                amountToInsert -= inserted;
                if (amountToInsert == 0) break;
            }
        } else {
            for (int i = handlerInfoList.size() - 1; i >= 0; i--) {
                ItemSource itemSource = handlerInfoList.get(i);
                int inserted = itemSource.insertItem(itemStack, amountToInsert, simulate);
                amountToInsert -= inserted;
                if (amountToInsert == 0) break;
            }
        }
        return amount - amountToInsert;
    }

    @Override
    public int extractItem(ItemStackKey itemStack, int amount, boolean simulate) {
        NetworkItemInfo itemInfo = (NetworkItemInfo) getItemInfo(itemStack);
        if (itemInfo == null) {
            return 0;
        }
        return itemInfo.extractItem(amount, simulate);
    }

    public void notifyPriorityUpdated() {
        this.handlerInfoList.sort(comparator);
    }

    public boolean addItemHandler(ItemSource handlerInfo) {
        if (!handlerInfoList.contains(handlerInfo)) {
            handlerInfo.setStoredItemsChangeCallback((storedItems, removedItems) -> updateStoredItems(handlerInfo, storedItems, removedItems));
            if (handlerInfo.update() == UpdateResult.INVALID) return false;
            handlerInfo.setInvalidationCallback(() -> removeItemHandler(handlerInfo));
            this.handlerInfoList.add(handlerInfo);
            addItemHandlerPost(handlerInfo);
            notifyPriorityUpdated();
            return true;
        }
        return false;
    }

    public void removeItemHandler(ItemSource handlerInfo) {
        if (this.handlerInfoList.remove(handlerInfo)) {
            handlerInfo.setStoredItemsChangeCallback(null);
            handlerInfo.setInvalidationCallback(null);
            for (ItemStackKey itemStackKey : itemInfoMap.keySet()) {
                NetworkItemInfo itemInfo = itemInfoMap.get(itemStackKey);
                itemInfo.removeInventory(handlerInfo);
            }
            removeItemHandlerPost(handlerInfo);
        }
    }

    protected void addItemHandlerPost(ItemSource handlerInfo) {
    }

    protected void removeItemHandlerPost(ItemSource handlerInfo) {
    }

    void updateStoredItems(ItemSource handlerInfo, Map<ItemStackKey, Integer> itemAmount, Set<ItemStackKey> removedItems) {
        boolean updatedItemAmount = false;
        for (ItemStackKey itemStackKey : itemAmount.keySet()) {
            int extractedAmount = handlerInfo.extractItem(itemStackKey, 1, true);
            if (extractedAmount > 0) {
                NetworkItemInfo itemInfo = itemInfoMap.get(itemStackKey);
                if (itemInfo == null) {
                    itemInfo = new NetworkItemInfo(itemStackKey);
                    this.itemInfoMap.put(itemStackKey, itemInfo);
                }
                updatedItemAmount |= itemInfo.addInventory(handlerInfo, itemAmount.get(itemStackKey));
            }
        }
        for (ItemStackKey removedItem : removedItems) {
            NetworkItemInfo itemInfo = itemInfoMap.get(removedItem);
            if (itemInfo != null) {
                updatedItemAmount |= itemInfo.removeInventory(handlerInfo);
                if (itemInfo.getTotalItemAmount() == 0) {
                    this.itemInfoMap.remove(removedItem);
                }
            }
        }
        if (updatedItemAmount && itemListChangeCallback != null) {
            if (!disableCallback) {
                itemListChangeCallback.run();
            } else {
                this.callbackWasCalled = true;
            }
        }
    }

}
