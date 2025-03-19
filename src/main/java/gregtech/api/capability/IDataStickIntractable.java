package gregtech.api.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IDataStickIntractable {

    void onDataStickLeftClick(EntityPlayer player, ItemStack dataStick);

    boolean onDataStickRightClick(EntityPlayer player, ItemStack dataStick);
}
