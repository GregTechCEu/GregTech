package gregtech.api.metatileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IWirelessCharger {

    boolean canChargePlayerItems(@NotNull EntityPlayer player);

    void chargePlayerItems(@NotNull List<ItemStack> stacksToCharge);
}
