package gregtech.api.items.metaitem.stats;

import gregtech.api.items.toolitem.behaviour.IToolBehaviour;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface IItemNameProvider extends IItemComponent, IToolBehaviour {

    String getItemStackDisplayName(ItemStack itemStack, String unlocalizedName);

}
