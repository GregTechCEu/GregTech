package gregtech.api.items.metaitem.stats;

import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public interface IItemDurabilityManager extends IItemComponent {

    /** The durability remaining on this item (double from 0 to 1). */
    double getDurabilityForDisplay(ItemStack itemStack);

    /** The first and last colors of a gradient. Default to Green durability gradient (null Pair). */
    @Nullable
    default Pair<Color, Color> getDurabilityColorsForDisplay(ItemStack itemStack) {
        return null;
    }

    /** Whether to show the durability as red when at the last 1/4th durability. Default true */
    default boolean doDamagedStateColors(ItemStack itemStack) {
        return true;
    }

    /**
     * Whether to show the durability bar when {@link IItemDurabilityManager#getDurabilityForDisplay(ItemStack)} is 0.
     * Default true
     */
    default boolean showEmptyBar(ItemStack itemStack) {
        return true;
    }

    /**
     * Whether to show the durability bar when {@link IItemDurabilityManager#getDurabilityForDisplay(ItemStack)} is 1.
     * Default true
     */
    default boolean showFullBar(ItemStack itemStack) {
        return true;
    }
}
