package gregtech.api.items.metaitem.stats;

import gregtech.api.items.toolitem.behaviour.IToolBehaviour;
import net.minecraft.item.ItemStack;

public interface IItemDurabilityManager extends IItemComponent, IToolBehaviour {

    boolean showsDurabilityBar(ItemStack itemStack);

    double getDurabilityForDisplay(ItemStack itemStack);

    int getRGBDurabilityForDisplay(ItemStack itemStack);
}
