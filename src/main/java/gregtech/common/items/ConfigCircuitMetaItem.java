package gregtech.common.items;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.ICircuitConfigurationItem;
import gregtech.api.items.metaitem.MetaItem;
import net.minecraft.item.ItemStack;

public class ConfigCircuitMetaItem<T extends MetaItem<?>.MetaValueItem> extends MetaItem {

    public ConfigCircuitMetaItem(short metaItemOffset) {
        super(metaItemOffset);
    }

    @Override
    protected MetaValueItem constructMetaValueItem(short metaValue, String unlocalizedName) {
        return new MetaValueItem(metaValue, unlocalizedName);
    }

    @Override
    protected int getModelIndex(ItemStack itemStack) {
        T metaValueItem = (T) getItem(itemStack);
        if (metaValueItem != null && metaValueItem.getModelIndexProvider() != null) {
            return metaValueItem.getModelIndexProvider().getModelIndex(itemStack);
        }
        ICircuitConfigurationItem circuitItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_CIRCUIT_CONFIGURATION, null);
        if (circuitItem != null) {
            return circuitItem.getConfiguration();
        }
        return 0;
    }
}
