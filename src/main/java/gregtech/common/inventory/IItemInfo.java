package gregtech.common.inventory;

import net.minecraft.item.ItemStack;

public interface IItemInfo {

    int getTotalItemAmount();

    ItemStack getItemStack();
}
