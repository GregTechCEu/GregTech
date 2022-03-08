package gregtech.common.inventory;

import gregtech.api.util.ItemStackKey;

import javax.annotation.Nullable;
import java.util.Set;

public interface IItemList {

    Set<ItemStackKey> getStoredItems();

    @Nullable
    IItemInfo getItemInfo(ItemStackKey stackKey);

    default boolean hasItemStored(ItemStackKey itemStackKey) {
        return getItemInfo(itemStackKey) != null;
    }

    int insertItem(ItemStackKey itemStack, int amount, boolean simulate, InsertMode insertMode);

    int extractItem(ItemStackKey itemStack, int amount, boolean simulate);

    enum InsertMode {
        LOWEST_PRIORITY,
        HIGHEST_PRIORITY,
    }
}
