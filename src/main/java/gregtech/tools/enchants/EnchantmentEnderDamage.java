package gregtech.tools.enchants;

import gregtech.api.util.GTUtility;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.PotionEffect;

import org.jetbrains.annotations.NotNull;

public class EnchantmentEnderDamage extends Enchantment {

    public static final EnchantmentEnderDamage INSTANCE = new EnchantmentEnderDamage();

    private EnchantmentEnderDamage() {
        super(Rarity.UNCOMMON, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
        this.setRegistryName(GTUtility.gregtechId("disjunction"));
        this.setName("disjunction");
    }

    @Override
    public int getMinEnchantability(int level) {
        return 5 + (level - 1) * 8;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return this.getMinEnchantability(level) + 20;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public void onEntityDamaged(@NotNull EntityLivingBase user, @NotNull Entity target, int level) {
        String entityName = EntityList.getEntityString(target);
        if (target instanceof EntityLivingBase && (target instanceof EntityEnderman || target instanceof EntityDragon ||
                target instanceof EntityEndermite ||
                (entityName != null && entityName.toLowerCase().contains("ender")))) {
            ((EntityLivingBase) target)
                    .addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, level * 200, Math.max(1, (5 * level) / 7)));
            ((EntityLivingBase) target)
                    .addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, level * 200, Math.max(1, (5 * level) / 7)));
        }
    }
}
