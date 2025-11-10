package gregtech.api.items.metaitem.stats;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface IItemModelDispatcher extends IItemComponent {

    /// Get the model index for the given item stack.
    /// The index range will be checked at [gregtech.api.items.metaitem.MetaItem#getModelIndex(ItemStack)]
    ///
    /// @param itemStack The specific item stack.
    /// @param maxIndex The max model index, from [gregtech.api.items.metaitem.MetaItem.MetaValueItem#getModelAmount()]`
    /// - 1`
    /// @return The model index for the specific stack, should be ranged between `0` (inclusive) and `maxIndex`
    /// (inclusive).
    int getModelIndex(ItemStack itemStack, int maxIndex);
}
