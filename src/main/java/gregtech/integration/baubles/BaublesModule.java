package gregtech.integration.baubles;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.common.items.MetaItems;
import gregtech.integration.IntegrationSubmodule;
import gregtech.modules.GregTechModules;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@GregTechModule(
                moduleID = GregTechModules.MODULE_BAUBLES,
                containerID = GTValues.MODID,
                modDependencies = GTValues.MODID_BAUBLES,
                name = "GregTech Baubles Integration",
                description = "Baubles Integration Module")
public class BaublesModule extends IntegrationSubmodule {

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(BaublesModule.class);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerItems(RegistryEvent.Register<Item> event) {
        MetaItems.ITEM_MAGNET_LV.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.ITEM_MAGNET_HV.addComponents(new BaubleBehavior(BaubleType.TRINKET));

        MetaItems.BATTERY_ULV_TANTALUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_LV_CADMIUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_LV_LITHIUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_LV_SODIUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_MV_CADMIUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_MV_LITHIUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_MV_SODIUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_HV_CADMIUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_HV_LITHIUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_HV_SODIUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.ENERGIUM_CRYSTAL.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.LAPOTRON_CRYSTAL.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_EV_VANADIUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_IV_VANADIUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_LUV_VANADIUM.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_ZPM_NAQUADRIA.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.BATTERY_UV_NAQUADRIA.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.ENERGY_LAPOTRONIC_ORB.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.ENERGY_LAPOTRONIC_ORB_CLUSTER.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.ENERGY_MODULE.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.ENERGY_CLUSTER.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.ZERO_POINT_MODULE.addComponents(new BaubleBehavior(BaubleType.TRINKET));
        MetaItems.ULTIMATE_BATTERY.addComponents(new BaubleBehavior(BaubleType.TRINKET));
    }

    public static IInventory getBaublesWrappedInventory(@NotNull EntityPlayer player) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        return new BaublesWrappedInventory(handler, player);
    }
}
