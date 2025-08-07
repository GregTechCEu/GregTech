package gtqt.api.util;

import net.minecraft.item.ItemStack;

public class ItemStackUtility {

    public static ItemStack setStack(ItemStack itemstack, int amount) {
        itemstack.setCount(amount);
        return itemstack;
    }

}
