package gregtech.mixins.forge;

import gregtech.api.items.armor.IArmorItem;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ISpecialArmor;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// TODO, needs verification as transformed class dumps cannot see inner classes
@Mixin(ISpecialArmor.ArmorProperties.class)
public class SpecialArmorPropertiesMixin {

    @ModifyExpressionValue(method = "applyArmor",
                           at = @At(value = "INVOKE_ASSIGN",
                                    target = "Lnet/minecraft/util/CombatRules;getDamageAfterAbsorb(FFF)F"),
                           remap = false)
    private static double adjustArmorAbsorption(double originalDamage, float damage, float totalArmor,
                                                float totalToughness, @Local EntityLivingBase entity,
                                                @Local NonNullList<ItemStack> inventory,
                                                @Local DamageSource damageSource) {
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
