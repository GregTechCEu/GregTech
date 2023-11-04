package gregtech.api.cover;

import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public interface CoverWithUI extends Cover, IUIHolder {

    default void openUI(EntityPlayerMP player) {
        CoverUIFactory.INSTANCE.openUI(this, player);
    }

    ModularUI createUI(EntityPlayer player);

    @Override
    default boolean isValid() {
        return getCoverable().isValid();
    }

    @Override
    default boolean isRemote() {
        return getCoverable().getWorld().isRemote;
    }

    @Override
    default void markAsDirty() {
        getCoverable().markDirty();
    }
}
