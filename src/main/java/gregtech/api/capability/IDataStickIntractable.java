package gregtech.api.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IDataStickIntractable {

    boolean onDataStickShiftRightClick(EntityPlayer player, ItemStack dataStick);

    boolean onDataStickRightClick(EntityPlayer player, ItemStack dataStick);
}
