package gregtech.api.items.metaitem.stats;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.awt.*;

public interface IItemDurabilityManager extends IItemComponent {

    /** The durability remaining on this item (double from 0 to 1). */
    double getDurabilityForDisplay(ItemStack itemStack);

    /** The first and last colors of a gradient. Default to Green durability gradient if null Pair. */
    @Nullable
    Pair<Color, Color> getDurabilityColorsForDisplay(ItemStack itemStack);

    /** Whether to show the durability as red when at the last 1/4th durability. */
    boolean doDamagedStateColors(ItemStack itemStack);

    /** Whether to show the durability bar when {@link IItemDurabilityManager#getDurabilityForDisplay(ItemStack)} is 0. */
    default boolean showEmptyBar(ItemStack itemStack) {
        return true;
    }

    /** Whether to show the durability bar when {@link IItemDurabilityManager#getDurabilityForDisplay(ItemStack)} is 1. */
    default boolean showFullBar(ItemStack itemStack) {
        return true;
    }
}
