package gtrmcore.common.items;

import net.minecraft.item.Item;

public class BaseItem extends Item {

    public BaseItem(String registryName) {
        super();
        setRegistryName(registryName);
        setTranslationKey(registryName);
    }
}
