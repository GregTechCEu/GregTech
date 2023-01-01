package gregtech.asm.hooks;

import gregtech.api.damagesources.DamageSources;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

@SuppressWarnings("unused")
public class DamageSourceHooks {

    public static DamageSource causePlayerDamage(EntityPlayer player) {
        return DamageSources.getPlayerDamage(player);
    }
}
