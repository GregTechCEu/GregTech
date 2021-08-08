package gregtech.api.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import javax.annotation.Nonnull;

public class DummyContainer extends Container {

    public DummyContainer() {
    }

    @Override
    public void detectAndSendChanges() {
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return true;
    }

}
