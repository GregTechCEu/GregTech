package gregtech.api.util;

import gregtech.api.damagesources.DamageSources;
import gregtech.common.advancement.GTTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nonnull;

public class EntityDamageUtil {

    private static final int FROST_WALKER_ID = 9;

    /**
     * @param entity      the entity to damage
     * @param temperature the temperature of the fluid in the pipe
     * @param multiplier  the multiplier on the damage taken
     * @param maximum     the maximum damage to apply to the entity, use -1 for no maximum
     */
    public static void applyTemperatureDamage(@Nonnull EntityLivingBase entity, int temperature, float multiplier, int maximum) {
        if (temperature > 320) {
            int damage = (int) ((multiplier * (temperature - 300)) / 50.0F);
            if (maximum > 0) {
                damage = Math.min(maximum, damage);
            }
            applyHeatDamage(entity, damage);
        } else if (temperature < 260) {
            int damage = (int) ((multiplier * (273 - temperature)) / 25.0F);
            if (maximum > 0) {
                damage = Math.min(maximum, damage);
            }
            applyFrostDamage(entity, damage);
        }
    }

    /**
     * @param entity the entity to damage
     * @param damage the damage to apply
     */
    public static void applyHeatDamage(@Nonnull EntityLivingBase entity, int damage) {
        // do not attempt to damage by 0
        if (damage <= 0) return;
        if (!entity.isEntityAlive()) return;
        // fire/lava mobs cannot be burned
        if (entity instanceof EntityBlaze || entity instanceof EntityMagmaCube || entity instanceof EntityWitherSkeleton || entity instanceof EntityWither)
            return;
        // fire resistance entities cannot be burned
        if (entity.getActivePotionEffect(MobEffects.FIRE_RESISTANCE) != null) return;

        entity.attackEntityFrom(DamageSources.getHeatDamage(), damage);
        if (entity instanceof EntityPlayerMP)
            GTTriggers.HEAT_DEATH.trigger((EntityPlayerMP) entity);
    }

    /**
     * @param entity the entity to damage
     * @param damage the damage to apply
     */
    public static void applyFrostDamage(@Nonnull EntityLivingBase entity, int damage) {
        // do not attempt to damage by 0
        if (damage <= 0) return;
        if (!entity.isEntityAlive()) return;
        // snow/frost mobs cannot be chilled
        if (entity instanceof EntitySnowman || entity instanceof EntityPolarBear || entity instanceof EntityStray)
            return;
        // frost walker entities cannot be chilled
        ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        // check for empty in order to force damage to be applied if armor breaks
        if (!stack.isEmpty()) {
            for (NBTBase base : stack.getEnchantmentTagList()) {
                NBTTagCompound compound = (NBTTagCompound) base;
                if (compound.getShort("id") == FROST_WALKER_ID) {
                    stack.damageItem(1, entity);
                    return;
                }
            }
        }

        entity.attackEntityFrom(DamageSources.getFrostDamage(), damage);
        if (entity instanceof EntityPlayerMP) {
            GTTriggers.COLD_DEATH.trigger((EntityPlayerMP) entity);
        }
    }

    /**
     * @param entity the entity to damage
     * @param damage the damage to apply
     */
    public static void applyChemicalDamage(@Nonnull EntityLivingBase entity, int damage) {
        // do not attempt to damage by 0
        if (damage <= 0) return;
        if (!entity.isEntityAlive()) return;
        // skeletons cannot breathe in the toxins
        if (entity instanceof AbstractSkeleton) return;

        entity.attackEntityFrom(DamageSources.getChemicalDamage(), damage);
        entity.addPotionEffect(new PotionEffect(MobEffects.POISON, damage * 100, 1));
        if (entity instanceof EntityPlayerMP) GTTriggers.CHEMICAL_DEATH.trigger((EntityPlayerMP) entity);
    }
}
