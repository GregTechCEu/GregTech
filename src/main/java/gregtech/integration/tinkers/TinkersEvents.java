package gregtech.integration.tinkers;

import gregtech.integration.tinkers.effect.GTTinkerEffects;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TinkersEvents {

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntityLiving().getActivePotionEffect(GTTinkerEffects.POTION_UNHEALING) != null) {
            event.setCanceled(true);
        }
    }
}
