package gregtech.api.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import org.jetbrains.annotations.NotNull;

public class DummyContainer extends Container {

    public DummyContainer() {}

    @Override
    public void detectAndSendChanges() {}

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return true;
    }
}
