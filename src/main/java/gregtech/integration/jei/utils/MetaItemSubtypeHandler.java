package gregtech.integration.jei.utils;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;

import net.minecraft.item.ItemStack;

import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import org.jetbrains.annotations.NotNull;

public class MetaItemSubtypeHandler implements ISubtypeInterpreter {

    @NotNull
    @Override
    public String apply(ItemStack itemStack) {
        MetaItem<?> metaItem = (MetaItem<?>) itemStack.getItem();
        MetaValueItem metaValueItem = metaItem.getItem(itemStack);
        String additionalData = "";
        if (metaValueItem != null) {
            additionalData = metaValueItem.getSubItemHandler().getItemSubType(itemStack);
        }
        return String.format("%d;%s", itemStack.getMetadata(), additionalData);
    }
}
