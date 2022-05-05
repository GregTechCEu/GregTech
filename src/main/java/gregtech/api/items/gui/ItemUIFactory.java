package gregtech.api.items.gui;

import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import gregtech.api.guiOld.ModularUI;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.entity.player.EntityPlayer;

public interface ItemUIFactory extends IItemComponent {

    /**
     * Creates new UI basing on given holder. Holder contains information
     * about item stack and hand, and also player
     */
    ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer);

    default ModularWindow createWindow(UIBuildContext buildContext) {
        return null;
    }

}
