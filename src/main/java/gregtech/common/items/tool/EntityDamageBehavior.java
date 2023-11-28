package gregtech.common.items.tool;

import gregtech.api.damagesources.DamageSources;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Add to tools to have them deal bonus damage to specific mobs.
 * Pass null for the mobType parameter to ignore the tooltip.
 */
public class EntityDamageBehavior implements IToolBehavior {

    private final List<Function<EntityLivingBase, Float>> shouldDoBonusList = new ArrayList<>();
    private final String mobType;

    public EntityDamageBehavior(float bonus, Class<?>... entities) {
        this(null, bonus, entities);
    }

    public EntityDamageBehavior(Map<Class<?>, Float> entities) {
        this(null, entities);
    }

    public EntityDamageBehavior(String mobType, float bonus, Class<?>... entities) {
        this.mobType = mobType;
        for (Class<?> entity : entities) {
            shouldDoBonusList.add(e -> entity.isAssignableFrom(e.getClass()) ? bonus : 0);
        }
    }

    public EntityDamageBehavior(String mobType, Map<Class<?>, Float> entities) {
        this.mobType = mobType;
        for (Map.Entry<Class<?>, Float> entry : entities.entrySet()) {
            Class<?> entity = entry.getKey();
            float bonus = entry.getValue();
            shouldDoBonusList.add(e -> entity.isAssignableFrom(e.getClass()) ? bonus : 0);
        }
    }

    @Override
    public void hitEntity(@NotNull ItemStack stack, @NotNull EntityLivingBase target,
                          @NotNull EntityLivingBase attacker) {
        float damageBonus = shouldDoBonusList.stream().map(func -> func.apply(target)).filter(f -> f > 0).findFirst()
                .orElse(0f);
        if (damageBonus != 0f) {
            DamageSource source = attacker instanceof EntityPlayer ?
                    DamageSources.getPlayerDamage((EntityPlayer) attacker) : DamageSources.getMobDamage(attacker);
            target.attackEntityFrom(source, damageBonus);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        if (mobType != null && !mobType.isEmpty()) {
            tooltip.add(I18n.format("item.gt.tool.behavior.damage_boost",
                    I18n.format("item.gt.tool.behavior.damage_boost_" + mobType)));
        }
    }
}
