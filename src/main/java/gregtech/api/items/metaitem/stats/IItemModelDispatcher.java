package gregtech.api.items.metaitem.stats;

import gregtech.api.items.metaitem.MetaItem;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface IItemModelDispatcher extends IItemComponent {

    /**
     * Get the model index for the given item stack. <br>
     * The index range will be checked at
     * {@link MetaItem#getModelIndex(ItemStack)}.
     *
     * @param itemStack The specific item stack.
     * @param maxIndex  The max model index, from
     *                  {@link MetaItem.MetaValueItem#getModelAmount()} - 1.
     * @return The model index for the specific stack, should be ranged between
     *         {@code 0} (inclusive) and {@code maxIndex} (inclusive).
     */
    int getModelIndex(ItemStack itemStack, int maxIndex);
}
