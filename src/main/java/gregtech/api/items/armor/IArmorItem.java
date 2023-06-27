package gregtech.api.items.armor;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

public interface IArmorItem {

    void damageArmor(EntityLivingBase entity, ItemStack itemStack, DamageSource source, int damage, int slot);
}
