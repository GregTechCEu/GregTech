package gregtech.api.metatileentity.registry;

import gregtech.api.metatileentity.IWirelessCharger;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.IItemHandler;

import baubles.api.BaublesApi;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WirelessChargerManger {

    private static final Set<IWirelessCharger> wirelessChargers = new HashSet<>();
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void playerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (++tickCounter % 20 == 0) {
                for (EntityPlayer player : GTUtility.getOnlinePlayers()) {
                    chargePlayerItems(player);
                }
            }
        }
    }

    public static void addCharger(@NotNull IWirelessCharger wirelessCharger) {
        wirelessChargers.add(wirelessCharger);
    }

    public static void removeCharger(@NotNull IWirelessCharger wirelessCharger) {
        wirelessChargers.remove(wirelessCharger);
    }

    private static void chargePlayerItems(@NotNull EntityPlayer player) {
        List<ItemStack> itemsToCharge = new ArrayList<>();

        for (ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty()) {
                itemsToCharge.add(stack);
            }
        }

        for (ItemStack stack : player.inventory.armorInventory) {
            if (!stack.isEmpty()) {
                itemsToCharge.add(stack);
            }
        }

        if (Mods.Baubles.isModLoaded()) {
            IItemHandler baubleHandler = BaublesApi.getBaublesHandler(player);
            for (int index = 0; index < baubleHandler.getSlots(); index++) {
                ItemStack stack = baubleHandler.getStackInSlot(index);
                if (!stack.isEmpty()) {
                    itemsToCharge.add(stack);
                }
            }
        }

        for (IWirelessCharger wirelessCharger : wirelessChargers) {
            if (wirelessCharger.canChargePlayerItems(player)) {
                wirelessCharger.chargePlayerItems(itemsToCharge);
            }
        }
    }
}
