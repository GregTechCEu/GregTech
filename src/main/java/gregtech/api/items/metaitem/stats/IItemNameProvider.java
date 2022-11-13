package gregtech.api.items.metaitem.stats;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface IItemNameProvider extends IItemComponent {

    String getItemStackDisplayName(ItemStack itemStack, String unlocalizedName);

}
