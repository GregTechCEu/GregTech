package gregtech.api.items.gui;

import gregtech.api.gui.ModularUI;
import gregtech.api.items.metaitem.stats.IItemComponent;

import net.minecraft.entity.player.EntityPlayer;

public interface ItemUIFactory extends IItemComponent {

    /**
     * Creates new UI basing on given holder. Holder contains information
     * about item stack and hand, and also player
     */
    ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer);
}
