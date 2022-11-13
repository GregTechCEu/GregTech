package gregtech.api.items.metaitem.stats;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface IItemContainerItemProvider extends IItemComponent {

    ItemStack getContainerItem(ItemStack itemStack);
}
