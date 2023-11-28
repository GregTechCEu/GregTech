package gregtech.common.inventory;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface IItemList {

    Set<ItemStack> getStoredItems();

    @Nullable
    IItemInfo getItemInfo(ItemStack stack);

    default boolean hasItemStored(ItemStack stack) {
        return getItemInfo(stack) != null;
    }

    int insertItem(ItemStack itemStack, int amount, boolean simulate, InsertMode insertMode);

    int extractItem(ItemStack itemStack, int amount, boolean simulate);

    enum InsertMode {
        LOWEST_PRIORITY,
        HIGHEST_PRIORITY,
    }
}
