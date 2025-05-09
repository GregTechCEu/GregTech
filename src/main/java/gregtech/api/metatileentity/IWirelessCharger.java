package gregtech.api.metatileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface IWirelessCharger {

    boolean canChargePlayerItems(EntityPlayer player);

    void chargePlayerItems(List<ItemStack> stacksToCharge);
}
