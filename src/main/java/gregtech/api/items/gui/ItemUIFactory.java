package gregtech.api.items.gui;

import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import gregtech.api.guiOld.ModularUI;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ItemUIFactory extends IItemComponent {

    /**
     * Creates new UI basing on given holder. Holder contains information
     * about item stack and hand, and also player
     */
    @Deprecated
    default ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        return null;
    }

    default ModularWindow createWindow(UIBuildContext buildContext, ItemStack item) {
        return null;
    }

}
