package gregtech.common.items.tool;

import gregtech.api.damagesources.DamageSources;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class EntityDamageBehavior implements IToolBehavior {

    private final Map<Class<?>, Float> entityDamageMap;

    public EntityDamageBehavior(float bonus, Class<?>... entities) {
        entityDamageMap = new HashMap<>();
        for (Class<?> entity : entities) {
            entityDamageMap.put(entity, bonus);
        }
    }

    public EntityDamageBehavior(Map<Class<?>, Float> entities) {
        entityDamageMap = entities;
    }

    @Override
    public void hitEntity(@Nonnull ItemStack stack, @Nonnull EntityLivingBase target, @Nonnull EntityLivingBase attacker) {
        float damageBonus = entityDamageMap.getOrDefault(target.getClass(), 0f);
        if (damageBonus != 0f) {
            DamageSource source = attacker instanceof EntityPlayer
                    ? DamageSources.getPlayerDamage((EntityPlayer) attacker)
                    : DamageSources.getMobDamage(attacker);
            target.attackEntityFrom(source, damageBonus);
        }
    }
}
