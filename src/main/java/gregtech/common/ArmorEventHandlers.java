package gregtech.common;

import gregtech.api.GTValues;
import gregtech.api.items.armoritem.ItemGTArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = GTValues.MODID)
public class ArmorEventHandlers {

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        EntityEquipmentSlot slot = event.getSlot();
        if (slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND) {
            return;
        }
        if (!(event.getEntityLiving() instanceof EntityPlayer player)) {
            return;
        }
        // maybe unnecessary sanity check to make sure this same item wasn't immediately re-equipped
        if (event.getFrom().isItemEqual(event.getTo())) {
            return;
        }

        if (event.getFrom().getItem() instanceof ItemGTArmor armor) {
            armor.onArmorUnequip(player.getEntityWorld(), player, event.getFrom());
        }
        if (event.getTo().getItem() instanceof ItemGTArmor armor) {
            armor.onArmorEquip(player.getEntityWorld(), player, event.getTo());
        }
    }
}
