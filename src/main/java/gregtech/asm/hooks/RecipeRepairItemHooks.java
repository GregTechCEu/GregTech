package gregtech.asm.hooks;

import com.google.common.collect.Lists;
import gregtech.api.items.toolitem.IGTTool;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import java.util.List;

public class RecipeRepairItemHooks {

    public static boolean matchesGTTool(InventoryCrafting inv) {
        System.out.println("Hello from hook");
        // from MC
        List<ItemStack> list = Lists.newArrayList();

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);

            if (!itemstack.isEmpty()) {
                list.add(itemstack);

                if (list.size() > 1) {
                    ItemStack stack = list.get(0);
                    if (!stack.getItem().isRepairable()) return false;
                    if (stack.getCount() != 1 || itemstack.getCount() != 1) return false;
                    if (stack.getItem() instanceof IGTTool) {
                        // whatever the gt logic is
                    } else if (itemstack.getItem() != stack.getItem()) {
                        return false;
                    }
                }
            }
        }

        return list.size() == 2;
    }
}
