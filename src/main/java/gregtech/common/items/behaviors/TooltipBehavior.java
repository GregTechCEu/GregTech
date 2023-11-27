package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * Adds tooltips with multiple translation keys
 */
public class TooltipBehavior implements IItemBehaviour {

    private final Consumer<List<String>> tooltips;

    /**
     * @param tooltips a consumer adding translated tooltips to the tooltip list
     */
    public TooltipBehavior(@NotNull Consumer<List<String>> tooltips) {
        this.tooltips = tooltips;
    }

    @Override
    public void addInformation(ItemStack itemStack, @NotNull List<String> lines) {
        tooltips.accept(lines);
    }
}
