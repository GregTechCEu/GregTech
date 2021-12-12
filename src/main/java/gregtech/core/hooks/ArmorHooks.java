package gregtech.core.hooks;

import gregtech.api.items.armor.IArmorItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;

@SuppressWarnings("unused")
public class ArmorHooks {

    public static void damageArmor(float damage, EntityLivingBase entity, NonNullList<ItemStack> inventory, DamageSource damageSource) {
        double armorDamage = Math.max(1.0F, damage / 4.0F);
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.get(i);
            if (itemStack.getItem() instanceof IArmorItem) {
                ((IArmorItem) itemStack.getItem()).damageArmor(entity, itemStack, damageSource, (int) armorDamage, i);
                if (inventory.get(i).getCount() == 0) {
                    inventory.set(i, ItemStack.EMPTY);
                }
            }
        }
    }
}
