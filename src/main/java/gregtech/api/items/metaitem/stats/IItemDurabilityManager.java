package gregtech.api.items.metaitem.stats;

import gregtech.client.utils.ToolChargeBarRenderer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public interface IItemDurabilityManager extends IItemComponent {

    /** The durability remaining on this item (double from 0 to 1). */
    double getDurabilityForDisplay(@NotNull ItemStack itemStack);

    /**
     * The left and right gradient bounds as two ARGB encoded integers packed into a long.<br/>
     * See {@link gregtech.api.util.ColorUtil#packTwoARGB(int, int)} on how to make the long in the proper format.
     */
    default long getDurabilityColorsForDisplay(@NotNull ItemStack itemStack) {
        return ToolChargeBarRenderer.defaultGradient;
    }

    /** Whether to show the durability as red when at the last 1/4th durability. Default true */
    default boolean doDamagedStateColors(@NotNull ItemStack itemStack) {
        return true;
    }

    /**
     * Whether to show the durability bar when {@link IItemDurabilityManager#getDurabilityForDisplay(ItemStack)} is 0.
     * Default true
     */
    default boolean showEmptyBar(@NotNull ItemStack itemStack) {
        return true;
    }

    /**
     * Whether to show the durability bar when {@link IItemDurabilityManager#getDurabilityForDisplay(ItemStack)} is 1.
     * Default true
     */
    default boolean showFullBar(@NotNull ItemStack itemStack) {
        return true;
    }
}
