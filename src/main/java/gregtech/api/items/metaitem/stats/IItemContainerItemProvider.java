package gregtech.api.items.metaitem.stats;

import gregtech.api.items.toolitem.behaviour.IToolBehaviour;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface IItemContainerItemProvider extends IItemComponent, IToolBehaviour {

    ItemStack getContainerItem(ItemStack itemStack);
}
