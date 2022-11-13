package gregtech.api.items.metaitem.stats;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface ISubItemHandler extends IItemComponent {

    String getItemSubType(ItemStack itemStack);

    void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems);

}
