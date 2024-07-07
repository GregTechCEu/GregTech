package gregtech.mixins.forge;

import gregtech.api.items.armor.IArmorItem;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ISpecialArmor;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ISpecialArmor.ArmorProperties.class)
public class SpecialArmorPropertiesMixin {

    @ModifyExpressionValue(method = "applyArmor",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/util/CombatRules;getDamageAfterAbsorb(FFF)F"),
                           remap = false)
    private static float adjustArmorAbsorption(float originalDamage, EntityLivingBase entity,
                                               NonNullList<ItemStack> inventory,
                                               DamageSource damageSource, double damage) {
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

        return originalDamage;
    }
}
