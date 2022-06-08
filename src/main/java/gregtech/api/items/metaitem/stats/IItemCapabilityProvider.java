package gregtech.api.items.metaitem.stats;

import gregtech.api.items.toolitem.behaviour.IToolBehaviour;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

@FunctionalInterface
public interface IItemCapabilityProvider extends IItemComponent, IToolBehaviour {

    ICapabilityProvider createProvider(ItemStack itemStack);

}
