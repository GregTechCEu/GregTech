package gregtech.api.items.metaitem.stats;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

@FunctionalInterface
public interface IItemCapabilityProvider extends IItemComponent {

    ICapabilityProvider createProvider(ItemStack itemStack);
}
